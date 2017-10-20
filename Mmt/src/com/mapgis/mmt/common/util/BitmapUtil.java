package com.mapgis.mmt.common.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.mapgis.mmt.CacheUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BitmapUtil {

    public static boolean isJPG(String imgPath) {
        if (TextUtils.isEmpty(imgPath)) {
            return false;
        }
        int index = imgPath.lastIndexOf(".");
        if (index == -1 || index == imgPath.length() - 1) {
            return false;
        }
        String suffix = imgPath.substring(index + 1);
        return "jpg".equalsIgnoreCase(suffix) || "jpeg".equalsIgnoreCase(suffix);
    }

    public static Bitmap getBitmap(Resources res, int resourceId) {
        return BitmapFactory.decodeResource(res, resourceId);
    }

    public static byte[] Bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap bytes2Bitmap(byte[] bytes) {
        if (bytes.length == 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;

        Bitmap bitmap = Bitmap.createBitmap(w, h, config);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);

        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.setScale(scaleWidth, scaleHeight);
        Bitmap newBp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newBp;
    }

    /**
     * 获得圆角图片
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);

        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rectF, paint);

        return output;
    }

    /**
     * 获得带倒影的图片
     */
    public static Bitmap createReflectionImageWidthOrigin(Bitmap bitmap) {
        final int reflectionGap = 4;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, h / 2, w,
                h / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(w, (h + h / 2),
                Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);

        Paint defaultPaint = new Paint();
        canvas.drawRect(0, h, w, h + reflectionGap, defaultPaint);

        canvas.drawBitmap(reflectionImage, 0, h + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
                0x70ffffff, TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawRect(0, h, w, bitmapWithReflection.getHeight()
                + reflectionGap, paint);

        return bitmapWithReflection;
    }

    public static Bitmap compressBySize(InputStream is, int targetWidth,
                                        int targetHeight) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            baos.write(buff, 0, len);
        }

        byte[] data = baos.toByteArray();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        int minSideLength = Math.min(targetWidth, targetHeight);
        opts.inSampleSize = FileZipUtil.computeSampleSize(opts, minSideLength,
                targetWidth * targetHeight);
        // // 得到图片的宽度、高度；
        // int imgWidth = opts.outWidth;
        // int imgHeight = opts.outHeight;
        // // 分别计算图片宽度、高度与目标宽度、高度的比例；取大于该比例的最小整数；
        // int widthRatio = (int) Math.ceil(imgWidth / (float) targetWidth);
        // int heightRatio = (int) Math.ceil(imgHeight / (float) targetHeight);
        // if (widthRatio > 1 || heightRatio > 1) {
        // if (widthRatio > heightRatio) {
        // opts.inSampleSize = heightRatio;
        // } else {
        // opts.inSampleSize = widthRatio;
        // }
        // }
        // 设置好缩放比例后，加载图片进内存；
        opts.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        return bitmap;
    }

    /**
     * 获得圆图
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    /**
     * bitmap 转base64
     *
     * @param bit
     * @return
     */
    public static String bitmap2Base64(Bitmap bit) {
        if (bit == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(CompressFormat.JPEG, 100, bos);// 参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * base64转bitmap
     */
    public static Bitmap base642Bitmap(String base64Str) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(base64Str, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static String saveBitmap(Bitmap mBitmap, String path) {
        FileOutputStream fOut = null;

        try {
            String key = CacheUtils.hashKeyForDisk(path);

            if (BaseClassUtil.isNullOrEmptyString(key))
                key = UUID.randomUUID().toString();

            CompressFormat format = CompressFormat.PNG;

            if (path.toLowerCase().endsWith(".jpg")) {
                format = CompressFormat.JPEG;
                path =Battle360Util.getFixedPath("Temp") + key + ".jpg";
            } else {
                path = Battle360Util.getFixedPath("Temp") + key + ".png";
            }

            File file = new File(path);

            if (file.exists())
                return path;

            fOut = new FileOutputStream(file);

            mBitmap.compress(format, 100, fOut);

            fOut.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (fOut != null)
                    fOut.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return path;
    }

    /**
     * 从给定路径加载图片
     */
    public static Bitmap loadBitmap(String imgpath) {
        return BitmapFactory.decodeFile(imgpath);
    }

    /**
     * 从给定的路径加载图片，并指定是否自动旋转方向
     */
    public static Bitmap loadBitmap(String imgpath, boolean adjustOritation) {
        if (!adjustOritation) {
            return loadBitmap(imgpath);
        } else {
            Bitmap bm = loadBitmap(imgpath);

            int degree = getImageDegree(imgpath);
            if (degree != 0) {
                bm = rotateBitMap(bm, degree);
            }
            return bm;
        }
    }

    public static boolean overwriteBitmap(Bitmap bitmap, String destPath) {
        return overwriteBitmap(bitmap, destPath, 100);
    }

    public static boolean overwriteBitmap(Bitmap bitmap, String destPath, int quality) {
        FileOutputStream fOut = null;

        try {
            CompressFormat format = CompressFormat.JPEG;
            if (destPath.toLowerCase().endsWith(".png")) {
                format = CompressFormat.PNG;
            }

            File file = new File(destPath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                return false;
            }

            if (file.exists() && !file.delete()) {
                return false;
            }

            fOut = new FileOutputStream(file);
            bitmap.compress(format, quality, fOut);
            fOut.flush();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        } finally {
            try {
                if (fOut != null)
                    fOut.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean rotateBitmap(String imgPath) {
        try {
            int degree = getImageDegree(imgPath);
            if (degree == 0) {
                return true;
            }
            Bitmap bm = getBitmapForCompress(imgPath);
            bm = rotateBitMap(bm, degree);

            return overwriteBitmap(bm, imgPath);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static Bitmap rotateBitMap(Bitmap srcBitmap, int rotateDegree) {
        if (srcBitmap == null) {
            return null;
        }
        rotateDegree %= 360;
        if (rotateDegree <= 0) {
            return srcBitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        return Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param imgPath 图片绝对路径
     * @return degree旋转的角度
     */
    public static int getImageDegree(String imgPath) {

        int degree = 0;
        if (!isJPG(imgPath)) {
            return degree;
        }

        ExifInterface exif;
        try {
            exif = new ExifInterface(imgPath);
        } catch (IOException e) {
            e.printStackTrace();
            return degree;
        }
        // 读取图片中相机方向信息
        int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        // 计算旋转角度
        switch (ori) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                degree = 0;
                break;
        }
        return degree;
    }

    public static Bitmap getBitmapFromFile(String path, int width, int height) {
        return getBitmapFromFile(path, width, height, false);
    }

    public static Bitmap getBitmapFromFile(String path, int width, int height, boolean isLow) {
        return getBitmapFromFile(new File(path), width, height, isLow, false);
    }

    public static Bitmap getBitmapFromFile(File file, int width, int height) {
        return getBitmapFromFile(file, width, height, false, false);
    }

    public static Bitmap getMutableBitmapFromFile(File file, int width, int height) {
        return getBitmapFromFile(file, width, height, false, true);
    }

    public static Bitmap getBitmapFromFile(File file, int width, int height, boolean isLow, boolean isMutable) {

        if (null != file && file.exists()) {
            try {
                if (width <= 0 || height <= 0) {
                    return getBitmapFromFile(file, isMutable);
                }
                return decodeSampledBitmapFromFile(file.getPath(), width, height, isLow, isMutable);

            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return getBitmapFromFile(file, isMutable);
            }
        }
        return null;
    }

    public static Bitmap getBitmapFromFile(File file, boolean isMutable) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        if (isMutable) {
            opts.inMutable = true;
        }

        // 数字越大读出的图片占用的heap越小 不然总是溢出
        if (file.length() < 20480) { // 0-20k
            opts.inSampleSize = 1;
        } else if (file.length() < 51200) { // 20-50k
            opts.inSampleSize = 2;
        } else if (file.length() < 307200) { // 50-300k
            opts.inSampleSize = 4;
        } else if (file.length() < 819200) { // 300-800k
            opts.inSampleSize = 6;
        } else if (file.length() < 1048576) { // 800-1024k
            opts.inSampleSize = 8;
        } else {
            opts.inSampleSize = 10;
        }

        return BitmapFactory.decodeFile(file.getPath(), opts);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath, int width, int height, boolean isLow, boolean isMutable) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        // 计算图片缩放比例
        final int minSideLength = Math.min(width, height);
        opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
        // opts.inSampleSize = calculateInSampleSize(opts, width, height);
        opts.inJustDecodeBounds = false;

        opts.inDither = false; // 不进行图片抖动处理
        opts.inPreferredConfig = isLow ? Config.RGB_565 : Config.ARGB_8888;
        opts.inMutable = isMutable;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            opts.inInputShareable = true;
            opts.inPurgeable = true;
        }

        return BitmapFactory.decodeFile(filePath, opts);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static File convertBitmap2File(Bitmap bitmap, File file) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    //region Used for handling image compression.

    public static final int DEFAULT_LEAST_COMPRESS_SIZE = 100 << 10; // 100KB

    /**
     * Used for handling image compression, never used for image display.
     */
    public static Bitmap getBitmapForCompress(String imgPath) {
        return getBitmapForCompress(imgPath, Config.ARGB_8888, true, DEFAULT_LEAST_COMPRESS_SIZE);
    }

    /**
     * Used for handling image compression, never used for image display.
     */
    public static Bitmap getBitmapForCompress(String imgPath, Config imgConfig, boolean isMutable, int leastCompressSize) {

        File source = new File(imgPath);
        if (!source.exists()) {
            return null;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();

        if (source.length() > leastCompressSize) {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgPath, opts);
            opts.inSampleSize = calculateInSampleSizeForCompress(opts.outWidth, opts.outHeight);
        }

        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = imgConfig;
        opts.inMutable = isMutable;

        return BitmapFactory.decodeFile(imgPath, opts);
    }

    private static int calculateInSampleSizeForCompress(int srcWidth, int srcHeight) {
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide >= 1664 && longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }
    //endregion
}
