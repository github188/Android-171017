package com.mapgis.mmt;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.android.volley.toolbox.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import libcore.io.DiskLruCache;

public class CacheUtils implements ImageLoader.ImageCache {
    private static CacheUtils utils = null;

    private final LruCache<String, Object> appCache;

    private DiskLruCache mDiskLruCache;

    private CacheUtils(Context context) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int appMemory = maxMemory / 8;

        appCache = new LruCache<String, Object>(appMemory) {
            @Override
            protected int sizeOf(String key, Object value) {
                if (value instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) value;

                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024; //这里是按多少KB来算
                } else
                    return super.sizeOf(key, value);
            }
        };

        try {
            File cacheDir = getDiskCacheDir(context, "temp");

            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                return;
            }

            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static CacheUtils getInstance(Context context) {
        if (utils == null) {
            utils = new CacheUtils(context);
        }

        return utils;
    }

    public synchronized static CacheUtils getInstance() {
        if (utils == null) {
            utils = new CacheUtils(MyApplication.getInstance());
        }

        return utils;
    }

    /**
     * 获取程序版本号
     */
    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，否则就调用getCacheDir()
     * 方法来获取缓存路径。<br>
     * 前者获取到的就是 /sdcard/Android/data/<application package>/cache 这个路径，而后者获取到的是
     * /data/data/<application package>/cache 这个路径。
     *
     * @param context    Context
     * @param uniqueName 唯一文件夹名
     * @return 缓存路径
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = "";

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir() != null ? context.getExternalCacheDir().getPath() : "";
        }

        if (TextUtils.isEmpty(cachePath)) {
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 将字符串进行MD5编码。让key（通常是URL地址）和图片的URL能够一一对应。
     *
     * @param key 缓存文件的文件名
     * @return MD5编码
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;

        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");

            mDigest.update(key.getBytes());

            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }

        return cacheKey;
    }

    /**
     * 将字节转换为只含16进制的数据的字符串
     *
     * @param bytes 待转换字节流
     * @return 16进制数据的字符串
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);

            if (hex.length() == 1) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString();
    }

    /**
     * 写入字符串内存缓存
     *
     * @param key   缓存键值
     * @param value 缓存内容
     */
    public void put(String key, String value) {
        appCache.put(key, value);
    }

    /**
     * 获取字符串内存缓存
     *
     * @param key 缓存键值
     * @return 缓存内容
     */
    public String get(String key) {
        return (String) appCache.get(key);
    }

    /**
     * 获取图片内存缓存
     *
     * @param key 缓存键值
     * @return 缓存图片
     */
    @Override
    public Bitmap getBitmap(String key) {
        Object value = appCache.get(key);

        if (value == null)
            return getBitmapFromDisk(key);
        else
            return (Bitmap) value;
    }

    /**
     * 写入图片内存缓存
     *
     * @param key   缓存键值
     * @param value 缓存图片
     */
    @Override
    public void putBitmap(final String key, final Bitmap value) {
        if (value != null) {
            appCache.put(key, value);

            MyApplication.getInstance().submitExecutorService(new Runnable() {
                @Override
                public void run() {
                    putBitmapToDisk(key, value);
                }
            });
        }
    }

    /**
     * 写入字符串磁盘缓存
     *
     * @param key   缓存键值
     * @param value 缓存内容
     */
    public void putStringToDisk(String key, String value) {
        OutputStream outputStream = null;

        try {
            key = hashKeyForDisk(key);

            // 写入缓存
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);

            outputStream = editor.newOutputStream(0);

            outputStream.write(value.getBytes());

            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取字符串磁盘缓存
     *
     * @param key 缓存键值
     * @return 缓存内容
     */
    public String getStringFromDisk(String key) {
        key = hashKeyForDisk(key);

        return getStringFromDiskByHashKey(key);
    }

    /**
     * 获取字符串磁盘缓存
     *
     * @param hashKey 缓存键值
     * @return 缓存内容
     */
    public String getStringFromDiskByHashKey(String hashKey) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;

        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(hashKey);

            if (snapShot != null) {
                inputStream = snapShot.getInputStream(0);

                outputStream = new ByteArrayOutputStream();
                int i;

                while ((i = inputStream.read()) != -1) {
                    outputStream.write(i);
                }

                return outputStream.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();

                if (inputStream != null)
                    inputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return "";
    }

    /**
     * 写入图片磁盘缓存
     *
     * @param key   缓存键值
     * @param value 缓存图片
     */
    public void putBitmapToDisk(String key, Bitmap value) {
        OutputStream outputStream = null;

        try {
            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

            if (key.endsWith(".png"))
                format = Bitmap.CompressFormat.PNG;

            key = hashKeyForDisk(key);

            // 写入缓存
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);

            outputStream = editor.newOutputStream(0);

            boolean isSuccess = value.compress(format, 100, outputStream);

            if (isSuccess)
                editor.commit();
            else
                editor.abort();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取图片磁盘缓存
     *
     * @param key 缓存键值
     * @return 缓存图片
     */
    public Bitmap getBitmapFromDisk(String key) {
        InputStream inputStream = null;

        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);

            if (snapShot == null)
                return null;

            inputStream = snapShot.getInputStream(0);

            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * DiskLruCache参考的使用实例
     */
    @SuppressWarnings("unused")
    private void sample(Context context) throws Exception {
        DiskLruCache mDiskLruCache = CacheUtils.getInstance(context).mDiskLruCache;

        String imageUrl = "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
        String key = hashKeyForDisk(imageUrl);

        // 写入缓存
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);

        OutputStream outputStream = editor.newOutputStream(0);

        outputStream.write(key.getBytes());

        editor.commit();
        // or editor.abort();

        // 将内存中的操作记录同步到日志文件，DiskLruCache能够正常工作的前提就是要依赖于journal文件中的内容。
        // 但其实并不是每次写入缓存都要调用一次flush()方法的，频繁地调用并不会带来任何好处
        mDiskLruCache.flush();

        // 读取缓存
        DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);

        if (snapShot != null) {
            InputStream inputStream = snapShot.getInputStream(0);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int i;

            while ((i = inputStream.read()) != -1) {
                byteArrayOutputStream.write(i);
            }

            String str = byteArrayOutputStream.toString();
        }

        // 删除缓存
        // DiskLruCache会根据我们在调用open()方法时设定的缓存最大值来自动删除多余的缓存，所以不需要我没玩手动维护
        mDiskLruCache.remove(key);

        // 删除所有缓存
        mDiskLruCache.delete();
    }

    public void flush() {
        try {
            this.mDiskLruCache.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
