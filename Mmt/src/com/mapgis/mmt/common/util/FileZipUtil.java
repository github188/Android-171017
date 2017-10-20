package com.mapgis.mmt.common.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.net.multhreaddownloader.DownloadProgressListener;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/**
 * 文件处理类，主要针对图片处理
 *
 * @author ZoroWu
 */
public class FileZipUtil {
    /**
     * 根据文件名称列表获取ZIP压缩文件路径
     *
     * @param fileList 文件列表
     * @return 生成的ZIP压缩文件路径
     */
    public static String Zip(List<String> fileList) {
        ArrayList<File> files = new ArrayList<>();

        for (String file : fileList) {
            files.add(new File(file.trim()));
        }

        String path = MyApplication.getInstance().getMediaPathString() + UUID.randomUUID() + ".zip";

        if (Zip(files.toArray(new File[files.size()]), path)) {
            return path;
        } else {
            return "";
        }
    }

    public static boolean Zip(File[] sourceFiles, String zipFilePath) {
        boolean flag = false;

        FileOutputStream fos;
        ZipOutputStream zos = null;

        try {
            File zipFile = new File(zipFilePath);

            if (zipFile.exists()) {
                if (!zipFile.delete())
                    return false;
            }

            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));

            for (File f : sourceFiles) {
                byte[] buffer = DecodeFile(f);
                String fileName = f.getName();
                // 创建ZIP实体,并添加进压缩包
                ZipEntry zipEntry = new ZipEntry(fileName);

                zos.putNextEntry(zipEntry);

                zos.write(buffer, 0, buffer.length);
            }

            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            try {
                if (null != zos) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return flag;
    }

    /**
     * 解压缩ZIP文件
     *
     * @param file       需要解压的文件
     * @param folderPath 目标路径
     * @param ignoreDir  是否忽略目录
     * @param listener   进度回调监听器
     * @return 解压文件列表
     */
    public static List<String> unZip(File file, String folderPath, boolean ignoreDir, DownloadProgressListener listener) {
        List<String> names = new ArrayList<>();

        try {
            long total = 0;
            long current = 0;

            if (listener != null) {
                listener.onStart();
            }

            File desDir = new File(folderPath);

            if (!desDir.exists()) {
                if (!desDir.mkdirs())
                    return names;
            }

            ZipFile zipFile = new ZipFile(file, "gbk");

            Enumeration<?> e = zipFile.getEntries();
            ZipEntry zipEntry;

            while (e.hasMoreElements()) {
                zipEntry = (ZipEntry) e.nextElement();

                String name = new String(zipEntry.getName().getBytes(zipFile.getEncoding()), "gbk");

                File entryFile = new File(folderPath + name);

                if (zipEntry.isDirectory()) {
                    if (ignoreDir)
                        continue;

                    if (!entryFile.mkdirs())
                        Log.e("Zoro", "创建文件夹失败");
                } else {
                    if (ignoreDir && (name.contains("\\") || name.contains("/"))) {
                        name = entryFile.getName();
                        entryFile = new File(folderPath + name);
                    }

                    InputStream in = null;
                    BufferedInputStream bis;
                    FileOutputStream fos = null;
                    BufferedOutputStream bos;

                    try {
                        total += zipEntry.getSize();

                        names.add(name);

                        if (entryFile.exists()) {
                            if (!entryFile.delete())
                                continue;
                        } else if (!entryFile.getParentFile().exists()) {
                            if (!entryFile.getParentFile().mkdirs())
                                continue;
                        }

                        if (!entryFile.createNewFile())
                            continue;

                        in = zipFile.getInputStream(zipEntry);
                        bis = new BufferedInputStream(in);
                        fos = new FileOutputStream(entryFile);
                        bos = new BufferedOutputStream(fos);

                        byte[] buffer = new byte[100 * 1024];
                        int length;

                        while ((length = bis.read(buffer, 0, buffer.length)) != -1) {
                            current += length;

                            bos.write(buffer, 0, length);

                            if (listener != null) {
                                listener.onLoading(current, total);
                            }
                        }

                        bos.flush();

                        bos.close();
                        bis.close();
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }

                        if (in != null) {
                            in.close();
                        }
                    }
                }
            }

            if (listener != null) {
                listener.onSuccess(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return names;
    }

    public static List<String> unZip(File file, String folderPath) throws IOException {
        return unZip(file, folderPath, false, null);
    }

    /**
     * 提出图片文件，先根据图片大小判断缩放比例，再压缩转档成jpg，最后输出内容byte[]
     *
     * @param file 图片文件
     * @return 处理后的字节数组
     */
    public static byte[] DecodeFile(File file) {
        Bitmap bitmap = getBitmapFromFile(file, 400, 600);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(CompressFormat.JPEG, 100, baos);

        byte[] buffer = baos.toByteArray();

        if (!bitmap.isRecycled()) {
            bitmap.recycle(); // 回收图片所占的内存
        }

        return buffer;
    }

    public static Bitmap getBitmapFromFile(File file, int width, int height) {
        if (null != file && file.exists()) {
            try {
                BitmapFactory.Options opts;

                if (width > 0 && height > 0) {
                    opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(file.getPath(), opts);

                    // 计算图片缩放比例
                    final int minSideLength = Math.min(width, height);
                    opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
                    opts.inJustDecodeBounds = false;
                    opts.inInputShareable = true;
                    opts.inPurgeable = true;

                    return BitmapFactory.decodeFile(file.getPath(), opts);
                } else {
                    return getBitmapFromFile(file);
                }

            } catch (OutOfMemoryError e) {
                e.printStackTrace();

                return getBitmapFromFile(file);
            }
        }

        return null;
    }

    public static Bitmap getBitmapFromFile(File file) {
        BitmapFactory.Options opts = new BitmapFactory.Options();

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

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
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

    /**
     * Deletes all files and subdirectories under "dir".
     *
     * @param dir Directory to be deleted
     * @return boolean Returns "true" if all deletions were successful. If a
     * deletion fails, the method stops attempting to delete and returns
     * "false".
     */
    public static boolean DeleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();

            for (String c : children) {
                boolean success = DeleteDir(new File(dir, c));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so now it can be smoked
        return dir.delete();
    }
}
