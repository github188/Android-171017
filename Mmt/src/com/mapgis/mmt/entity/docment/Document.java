package com.mapgis.mmt.entity.docment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.FileUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;

import java.io.File;

/**
 * Created by Comclay on 2016/12/13.
 * 文件
 */

public class Document implements Comparable<Document>, Parcelable, DocumentOperate {
    private String TAG = this.getClass().getSimpleName();

    public enum MimeType {
        File("file", R.drawable.file_icon_folder),
        Doc("doc", R.drawable.doc_doc),
        Txt("text", R.drawable.doc_txt),
        Image("image", R.drawable.no_image),
        Video("video", R.drawable.no_image),
        Audio("audio", R.drawable.doc_video),
        Attach("attach", R.drawable.doc_unknow),
        Zip("zip", R.drawable.doc_zip),
        Apk("apk", R.drawable.doc_apk),
        Pdf("pdf",R.drawable.doc_pdf),
        Excel("pdf",R.drawable.doc_excel),
        Unknow("unknow", R.drawable.doc_unknow);

        private String typeName;
        private int resId;

        MimeType(String s, int resid) {
            this.typeName = s;
            this.resId = resid;
        }

        public int getResId() {
            return resId;
        }

        public void setResId(int resId) {
            this.resId = resId;
        }

        public String getTypeName() {
            if (BaseClassUtil.isNullOrEmptyString(this.typeName)) {
                return Unknow.name();
            }
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    protected String mimeTypeName;
    // 文件大小
    protected long size;
    // 文件名
    protected String name;
    // 绝对路径
    protected String path;
    // 上次更改时间
    protected long lastModified;
    // 相对父目录
    public static String relativePath;
    // 默认的文件图标
    protected int iconId = R.drawable.doc_unknow;

    protected boolean flag = false;

    public Document() {
        this(MimeType.Unknow);
    }

    public Document(int iconId) {
        this.iconId = iconId;
    }

    public Document(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public Document(MimeType mimeType) {
        this.iconId = mimeType.getResId();
        this.mimeTypeName = mimeType.getTypeName();
    }

    public static final Creator<Document> CREATOR = new Creator<Document>() {
        @Override
        public Document createFromParcel(Parcel in) {
            return new Document(in);
        }

        @Override
        public Document[] newArray(int size) {
            return new Document[size];
        }
    };

    public boolean isDirectory() {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return file.isDirectory();
    }

    /**
     * 上传时的相对路径
     *
     * @return
     */
    public String getServerRelativePath() {
        return relativePath + this.name;
    }

    /**
     * 上传的文件名
     *
     * @return
     */
    public String getDatabaseValue() {
        return relativePath + this.name;
    }

    /**
     * 上传的绝对路径
     *
     * @return
     */
    public String getLocalAbsolutePath() {
        return path;
    }

    public static String getRelativePath() {
        return relativePath;
    }

    public static void setRelativePath(String relativePath) {
        Document.relativePath = relativePath;
    }

    public String getMimeTypeName() {
        return mimeTypeName;
    }

    public void setMimeTypeName(String mimeTypeName) {
        this.mimeTypeName = mimeTypeName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    /**
     * 按照文件的名称排序
     * 优先文件夹，其次文件
     */
    @Override
    public int compareTo(@NonNull Document another) {
        if (this.equals(another)) {
            return 0;
        }

        if ((this.isDirectory() && another.isDirectory())
                || (!this.isDirectory() && !another.isDirectory())) {
            // 两者都是文件夹或者文件
            return this.name.compareToIgnoreCase(another.getName());
        } else if (this.isDirectory() && !another.isDirectory()) {
            // this is directory
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 有且仅当两个文件对象的绝对路径相同时，表示这两个文件相同
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Document document = (Document) o;

        return path != null ? path.equals(document.path) : document.path == null;

    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "文件路径=" + path + '\n';
    }


    @Override
    public void setIconToView(Context context, ImageView view) {
        view.setImageResource(iconId);
    }

    public String getNetUrl() {
        if (BaseClassUtil.isNullOrEmptyString(path) || BaseClassUtil.isNullOrEmptyString(relativePath)) {
            throw new IllegalArgumentException("文件的路径或者父路径为空！");
        }
        return ServerConnectConfig.getInstance().getCityServerMobileBufFilePath()
                + "/OutFiles/UpLoadFiles"
                + relativePath + this.name;
    }

    @Override
    public void downloadFromServer(final CallBack callBack) {
        try {
            if (BaseClassUtil.isNullOrEmptyString(path)) {
                Log.e(TAG, "下载失败：文件路径为空");
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        File file = new File(path);

                        // 文件夹路径不存在，则创建文件夹
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }

                        // 文件不存在，则创建并下载文件
                        if (!file.exists()) {
                            file.createNewFile();
                            NetUtil.downloadFile(relativePath + Document.this.name, file);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    File file = new File(path);
                    if (file.exists() && file.length() > 0){
                        Log.i(TAG, "文件下载成功" + file.length() + "：" + file.getAbsolutePath());
                        callBack.onSuccess(file);
                    }else{
                        callBack.onFailure(null, -1, "");
                    }
                }
            }.execute();
/*
            FinalHttp fh = new FinalHttp();
            fh.download(getNetUrl(), path, new AjaxCallBack<File>() {
                @Override
                public void onSuccess(File file) {
                    super.onSuccess(file);

                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    super.onFailure(t, errorNo, strMsg);
                }
            });*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void copyFromLocal(final String srcPath, final CallBack callBack) throws Exception {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return FileUtil.copyFile(new File(srcPath), new File(Document.this.path));
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean b) {
                if (callBack == null) return;
                if (b.booleanValue()) {
                    callBack.onSuccess(b);
                } else {
                    callBack.onFailure(null, -1, "文件拷贝失败：" + srcPath);
                }
            }
        }.execute();
    }

    @Override
    public boolean delete() {
        File file = new File(this.path);
        FileUtil.deleteFile(file);
        return file.exists();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.TAG);
        dest.writeString(this.mimeTypeName);
        dest.writeLong(this.size);
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeLong(this.lastModified);
        dest.writeInt(this.iconId);
        dest.writeByte(this.flag ? (byte) 1 : (byte) 0);
    }

    protected Document(Parcel in) {
        this.TAG = in.readString();
        this.mimeTypeName = in.readString();
        this.size = in.readLong();
        this.name = in.readString();
        this.path = in.readString();
        this.lastModified = in.readLong();
        this.iconId = in.readInt();
        this.flag = in.readByte() != 0;
    }
}
