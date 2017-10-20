package com.mapgis.mmt.entity.docment;

import android.content.Context;
import android.os.Parcel;
import android.widget.ImageView;

/**
 * Created by Comclay on 2016/12/15.
 * 视频对象
 */

public class VideoDocument extends ImageDocument{
    // 视频所对应的预览图对象
    private ImageDocument preImageDocument;

    public VideoDocument(){
        this(MimeType.Audio);
    }
    public VideoDocument(int iconId){
        super(iconId);
    }

    /**
     * 上传的文件名,即在数据库中的值
     * @return
     */
    public String getDatabaseValue(){
        return relativePath + this.name;
    }

    /**
     * 上传时的服务器的相对路径
     * @return
     */
    public String getServerRelativePath(){
        StringBuilder sb = new StringBuilder();
        sb.append(relativePath).append(this.name)
                .append(",")
                .append(relativePath).append(preImageDocument.getName());
        return sb.toString();
    }

    /**
     * 上传的文件在本地绝对路径
     * @return
     */
    public String getLocalAbsolutePath(){
        return path + "," + preImageDocument.getPath();
    }

    public VideoDocument(MimeType video) {
        this.iconId = video.getResId();
        this.mimeTypeName = video.getTypeName();
    }

    public ImageDocument getPreImageDocument() {
        return preImageDocument;
    }

    public void setPreImageDocument(ImageDocument preImageDocument) {
        this.preImageDocument = preImageDocument;
    }

    @Override
    protected void setIconResourse(Context context,ImageView view) {
        super.setIconResourse(context,view);
    }

    @Override
    public void setIconToView(Context context, ImageView view) {
        if (preImageDocument != null){
            preImageDocument.setIconToView(context,view);
        }else{
            setIconResourse(context,view);
        }
    }

    @Override
    public void setIconId(int iconId) {
        preImageDocument.setIconId(iconId);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.preImageDocument, flags);
    }

    protected VideoDocument(Parcel in) {
        this.preImageDocument = in.readParcelable(ImageDocument.class.getClassLoader());
    }

    public static final Creator<VideoDocument> CREATOR = new Creator<VideoDocument>() {
        @Override
        public VideoDocument createFromParcel(Parcel source) {
            return new VideoDocument(source);
        }

        @Override
        public VideoDocument[] newArray(int size) {
            return new VideoDocument[size];
        }
    };
}
