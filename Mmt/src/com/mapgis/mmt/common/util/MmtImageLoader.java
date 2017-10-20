package com.mapgis.mmt.common.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import com.mapgis.mmt.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MmtImageLoader {

    private static MmtImageLoader mmtImageLoader;

    /**
     * 一级缓存，强引用
     */
    private final LruCache<String, Bitmap> firstCache = new LruCache<String, Bitmap>(
            ((int) (Runtime.getRuntime().maxMemory() / 1024)) / 8);

    /**
     * 二级缓存，软引用：如果一个对象只具有软引用，则内存空间足够，垃圾回收器就不会回收它
     */
    private final ConcurrentHashMap<String, SoftReference<Bitmap>> secondCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(20);

    /**
     * 三级缓存，弱引用：弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，
     * 不管当前内存空间足够与否，都会回收它的内存。
     */
    private final ConcurrentHashMap<String, WeakReference<Bitmap>> thirdCache = new ConcurrentHashMap<String, WeakReference<Bitmap>>(20);

    /**
     * 线程池，线程数量限制为3个
     */
    private final ExecutorService executorService;

    public static MmtImageLoader getInstance() {
        if (mmtImageLoader == null)
            mmtImageLoader = new MmtImageLoader();

        return mmtImageLoader;
    }

    private MmtImageLoader() {
        this.executorService = Executors.newFixedThreadPool(3);
    }

    /**
     * 将图片显示在指定的空间上
     *
     * @param url       图片所在网络路径
     * @param filePath  图片下载到本地的路径
     * @param imageView 需要显示图片的控件
     */
    public void showBitmap(String url, String filePath, ImageView imageView) {

        if (url == null || filePath == null || url == null)
            return;

        Bitmap bitmap = getBitmapFromCache(url, filePath);

        if (bitmap == null) {
            new DownImageAsyncTask(imageView).executeOnExecutor(executorService, url, filePath);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 从缓存中获取图片
     *
     * @param url  图片网络路径
     * @param path 图片本地路径
     * @return
     */
    private Bitmap getBitmapFromCache(String url, String path) {
        // 一级缓存有数据，从一级缓存取出数据
        Bitmap bitmap = firstCache.get(url);

        if (bitmap != null)
            return bitmap;

        // 二级缓存有数据，从二级缓存取出数据，并将数据插入到一级缓存中
        if (secondCache.get(url) == null) {
            bitmap = null;
        } else {
            bitmap = secondCache.get(url).get();
        }

        if (bitmap != null) {
            firstCache.put(url, bitmap);
            return bitmap;
        }

        // 三级缓存有数据，从三级缓存取出数据，并将数据插入到一级和二级缓存中
        if (thirdCache.get(url) == null) {
            bitmap = null;
        } else {
            bitmap = thirdCache.get(url).get();
        }

        if (bitmap != null) {
            firstCache.put(url, bitmap);

            synchronized (secondCache) {
                secondCache.put(url, new SoftReference<Bitmap>(bitmap));
            }

            return bitmap;
        }

        // 本地SD卡缓存，取出后，并插入到前三级缓存中
        bitmap = getBitmap(path);

        if (bitmap != null) {
            firstCache.put(url, bitmap);

            synchronized (secondCache) {
                secondCache.put(url, new SoftReference<Bitmap>(bitmap));
            }

            synchronized (thirdCache) {
                thirdCache.put(url, new WeakReference<Bitmap>(bitmap));
            }

            return bitmap;
        }

        return bitmap;
    }

    private Bitmap getBitmap(String path) {
        return decode(path, 4);
    }

    private Bitmap decode(String url, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false; /* 不进行图片抖动处理 */
        options.inPreferredConfig = null; /* 设置让解码器以最佳方式解码 */
        options.inSampleSize = size; /* 图片长宽方向缩小倍数 */

        try {
            return (BitmapFactory.decodeFile(url, options));
        } catch (OutOfMemoryError e) {
            return decode(url, size + 1);
        }
    }

    /**
     * 从服务器下载图片到本地。<br>
     * 参数1：图片url地址<br>
     * 参数2：图片需要存储的本地路径<br>
     * 传入参数为key，下载成功后，加入到一级缓存和二级缓存中，并显示出来。
     */
    class DownImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private String key;

        public DownImageAsyncTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            FileOutputStream outputStream = null;
            BufferedInputStream inputStream = null;

            try {

                key = params[0];
                String filePath = params[1];

                File file = new File(filePath);

                // 文件若存在，则直接返回
                if (file.exists())
                    return getBitmap(file.getAbsolutePath());

                File dir = file.getParentFile();

                if (!dir.exists())
                    dir.mkdirs();

                // 创建空文件
                file.createNewFile();

                // 下载图片
                byte[] bytes = NetUtil.executeHttpGetBytes(30, key);

                outputStream = new FileOutputStream(file);

                inputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));

                byte[] buffer = new byte[10 * 1024];

                int length = 0;

                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();

                return getBitmap(file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (inputStream != null)
                        inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            if (result != null) {

                if (imageView != null)
                    imageView.setImageBitmap(result);

                firstCache.put(key, result);

                synchronized (secondCache) {
                    secondCache.put(key, new SoftReference<Bitmap>(result));
                }

                synchronized (thirdCache) {
                    thirdCache.put(key, new WeakReference<Bitmap>(result));
                }

            } else {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageBitmap(BitmapFactory.decodeResource(imageView.getResources(), R.drawable.load_error));
            }

        }
    }

}
