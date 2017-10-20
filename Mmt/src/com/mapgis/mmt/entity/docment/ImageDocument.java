package com.mapgis.mmt.entity.docment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.widget.ImageView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;

import java.io.File;

/**
 * Created by Comclay on 2016/12/15.
 * 图片文件
 */

public class ImageDocument extends Document{
    public ImageDocument() {
        this(MimeType.Image);
    }

    public ImageDocument(int iconId) {
        super(iconId);
    }

    public ImageDocument(MimeType image) {
        super(image);
    }

    public void setIconToView(final Context context, final ImageView view) {
        super.setIconToView(context,view);
        // 首先判断本地是否有该文件，如果没有需要从服务器上下载
        File file = new File(path);
        if (!file.exists() || file.length() <= 0) {
            // 图片在本地不存在或者图片大小为0
            downloadFromServer(new CallBack() {
                @Override
                public void onSuccess(Object o) {
                    setIconResourse(context, view);
                }

                @Override
                public void onFailure(Throwable t, int errorNo, String strMsg) {
                    view.setImageResource(iconId);
                }
            });
        } else {
            setIconResourse(context, view);
        }
    }

    /**
     * 是图片类型的文件可以继承这个方法
     *
     * @param view ImageView 布局
     */
    protected void setIconResourse(final Context context, final ImageView view) {
        // 获取文件的缩略图,将图片缩放到60*60的大小
        // 同时使用异步的方式加载
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return extractThumbnail(ImageDocument.this.path
                        , DimenTool.dip2px(context, context.getResources().getDimension(R.dimen.file_thumbnail_width))
                        , DimenTool.dip2px(context, context.getResources().getDimension(R.dimen.file_thumbnail_height)));
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) {
                    view.setImageResource(iconId);
                } else {
                    view.setImageBitmap(bitmap);
                }
            }
        }.execute();
    }

    /**
     * 得到大图片的缩略图
     *
     * @param path   源图片路径
     * @param width  宽度
     * @param height 高度
     * @return 缩小后的缩略图
     */
    public Bitmap extractThumbnail(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载bitmap到内存中
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected ImageDocument(Parcel in) {
        super(in);
    }

    public static final Creator<ImageDocument> CREATOR = new Creator<ImageDocument>() {
        @Override
        public ImageDocument createFromParcel(Parcel source) {
            return new ImageDocument(source);
        }

        @Override
        public ImageDocument[] newArray(int size) {
            return new ImageDocument[size];
        }
    };
}
