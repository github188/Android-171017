package com.mapgis.mmt.common.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.global.OnResultListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Compress and add water mark.
 */
public final class CompressWaterMarkTask extends AsyncTask<Void, Void, Boolean> {

    public static final int DEFAULT_IMAGE_QUALITY = 60;

    private ProgressDialog dialog;
    private final Context context;
    private final List<String> srcPaths;
    private final List<String> destPaths;
    private final boolean compress;
    private final boolean addWaterMark;
    private final List<String> waterMarkText;
    private final boolean adjustOrientation;
    private final int imgQuality;

    private final OnResultListener<Boolean> onResultListener;

    public static class Builder {

        private Context context;
        private List<String> srcPaths;
        private List<String> destPaths;
        private boolean compress;
        private boolean addWaterMark;
        private List<String> waterMarkText;
        private boolean adjustOrientation;
        private int imgQuality;
        private OnResultListener<Boolean> onResultListener;

        public Builder(Context context, List<String> srcPaths) {
            this.context = context;
            this.srcPaths = srcPaths;
            this.addWaterMark = true;
            this.compress = true;
            this.adjustOrientation = true;
            this.imgQuality = DEFAULT_IMAGE_QUALITY;
        }

        public Builder setDestPaths(List<String> destPaths) {
            this.destPaths = destPaths;
            return this;
        }

        public Builder setAddWaterMark(boolean add) {
            this.addWaterMark = add;
            return this;
        }

        public Builder setWaterMarkText(List<String> waterMarkText) {
            this.waterMarkText = waterMarkText;
            return this;
        }

        public Builder setCompress(boolean compress) {
            this.compress = compress;
            return this;
        }

        public Builder setAdjustOrientation(boolean adjustOrientation) {
            this.adjustOrientation = adjustOrientation;
            return this;
        }

        public Builder setImageQuality(int imgQuality) {
            this.imgQuality = imgQuality;
            return this;
        }

        public Builder setOnResultListener(OnResultListener<Boolean> listener) {
            this.onResultListener = listener;
            return this;
        }

        public CompressWaterMarkTask build() {
            return new CompressWaterMarkTask(this);
        }
    }

    private CompressWaterMarkTask(Builder builder) {
        if (builder.context == null || builder.srcPaths == null) {
            throw new NullPointerException("context == null || srcPaths == null");
        }
        if (builder.destPaths != null && (builder.srcPaths.size() != builder.destPaths.size())) {
            throw new IllegalArgumentException("srcPaths.size() != destPaths.size()");
        }
        if (builder.imgQuality <  0 || builder.imgQuality > 100) {
            throw new IllegalArgumentException("quality must be 0..100");
        }
        this.context = builder.context;
        this.srcPaths = builder.srcPaths;
        this.destPaths = builder.destPaths != null ? builder.destPaths : builder.srcPaths;
        this.compress = builder.compress;
        this.addWaterMark = builder.addWaterMark;
        this.waterMarkText = builder.waterMarkText != null ? builder.waterMarkText : getDefaultWaterText();
        this.onResultListener = builder.onResultListener;
        this.adjustOrientation = builder.adjustOrientation;
        this.imgQuality = builder.imgQuality;
    }

    @Override
    protected void onPreExecute() {
        try {
            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage(addWaterMark ? "正在绘制水印，请稍候" : "正在处理，请稍候");
            dialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            if (srcPaths == null || srcPaths.size() == 0) {
                return false;
            }

            for (int i = 0, length = srcPaths.size(); i < length; i++) {

                String imgFilePath = srcPaths.get(i);

                // Pic compress
                Bitmap bitmap;
                if (compress) {
                    bitmap = BitmapUtil.getBitmapForCompress(imgFilePath);
                } else {
                    bitmap = BitmapUtil.getBitmapForCompress(imgFilePath, Bitmap.Config.ARGB_8888, true, Integer.MAX_VALUE);
                }

                if (bitmap == null) {
                    return false;
                }

                // Adjust orientation
                if (adjustOrientation) {
                    int degree = BitmapUtil.getImageDegree(imgFilePath);
                    if (degree > 0) {
                        bitmap = BitmapUtil.rotateBitMap(bitmap, degree);
                    }
                }

                // Add water mark
                if (addWaterMark) {
                    addWaterMarkOnBitmap(bitmap, waterMarkText);
                }

                // Write bitmap to destination file
                try {
                    BitmapUtil.overwriteBitmap(bitmap, destPaths.get(i), imgQuality);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            if (!dialog.isShowing()) {
               return;
            }
            dialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (onResultListener != null) {
            if (result) {
                onResultListener.onSuccess(true);
            } else {
                onResultListener.onFailed(addWaterMark ? "图片添加水印失败" : "处理失败");
            }
        }
    }

    private List<String> getDefaultWaterText() {
        List<String> waterMark = new ArrayList<>();
        if (context.getPackageName().equals("com.project.hefei")) {
            waterMark.add(MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName);

            GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();
            String xy = (xyz != null) ? (xyz.getX() + "," + xyz.getY()) : "0.0,0.0";
            waterMark.add(xy);
        }
        waterMark.add(BaseClassUtil.getSystemTime());
        return waterMark;
    }

    // 给图片添加水印
    private void addWaterMarkOnBitmap(final Bitmap src, List<String> waterTexts) {

        int w = src.getWidth();

        Canvas canvas = new Canvas(src);

        Paint p = new Paint();
        Typeface font = Typeface.create("宋体", Typeface.NORMAL);
        p.setColor(Color.parseColor("#ffffffff"));
        p.setTypeface(font);
        //水印大小, 规定：宽度为768 采用20大小字体
        float rate = w / (float) 768.0;
        float textSize = 28 * rate;
        p.setTextSize(textSize);
        p.setAntiAlias(true);

        float baseX = canvas.getWidth() - 20 * rate;
        float MarginY = 20 * rate;
        float baseY = canvas.getHeight() - MarginY;

        Rect rect = new Rect();
        for (int i = waterTexts.size() - 1; i >= 0; i--) {
            String text = waterTexts.get(i);
            p.getTextBounds(text, 0, text.length(), rect);
            canvas.drawText(text, baseX - rect.width(),
                    baseY - rect.height() * (waterTexts.size() - 1 - i) - MarginY * (waterTexts.size() - 1 - i), p);
        }

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
    }

}
