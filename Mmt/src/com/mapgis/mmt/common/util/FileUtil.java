package com.mapgis.mmt.common.util;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.FileInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

/**
 * 文件处理工具类 *
 */
public class FileUtil {

    /**
     * 判断 dirPath文件夹下 是否 存在 后缀 为 suffix 的 文件， 递归检查 dirPath 里的子文件夹是否包含 后缀名为
     * suffix 的 文件夹
     *
     * @param dirPath 文件夹 的 全路径
     * @param suffix  后缀名 ， 如： .mapx
     * @return
     */
    public static boolean isExistInDir(String dirPath, String suffix) {

        File dirFile = new File(dirPath);

        if (!dirFile.exists()) {
            return false;
        }

        File[] sonFiles = dirFile.listFiles();
        if (sonFiles == null) {
            return false;
        }

        for (File f : sonFiles) {
            if ((f.isFile() && f.getName().endsWith(suffix)) || (f.isDirectory() && isExistInDir(f.getAbsolutePath(), suffix))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取SD路径 *
     */
    public static String getSDPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取文件信息 *
     */
    public static FileInfo getFileInfo(File f) {
        FileInfo info = new FileInfo();
        info.Name = f.getName();
        info.IsDirectory = f.isDirectory();
        calcFileContent(info, f);
        return info;
    }

    /**
     * 计算文件内容 *
     */
    private static void calcFileContent(FileInfo info, File f) {
        if (f.isFile()) {
            info.Size += f.length();
        }
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; ++i) {
                    File tmp = files[i];
                    if (tmp.isDirectory()) {
                        info.FolderCount++;
                    } else if (tmp.isFile()) {
                        info.FileCount++;
                    }
                    if (info.FileCount + info.FolderCount >= 10000) { // 超过一万不计算
                        break;
                    }
                    calcFileContent(info, tmp);
                }
            }
        }
    }

    /**
     * 转换文件大小 *
     */
    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = fileS + " B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + " K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + " M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + " G";
        }
        return fileSizeString;
    }

    /**
     * 合并路径 *
     */
    public static String combinPath(String path, String fileName) {
        return path + (path.endsWith(File.separator) ? "" : File.separator) + fileName;
    }

    /**
     * 复制文件 *
     */
    public static boolean copyFile(File src, File tar) throws Exception {
        if (src.isFile()) {
            InputStream is = new FileInputStream(src);
            OutputStream op = new FileOutputStream(tar);
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(op);
            byte[] bt = new byte[1024 * 8];
            int len = bis.read(bt);
            while (len != -1) {
                bos.write(bt, 0, len);
                len = bis.read(bt);
            }
            bis.close();
            bos.close();
        }
        if (src.isDirectory()) {
            File[] f = src.listFiles();
            tar.mkdir();
            for (int i = 0; i < f.length; i++) {
                copyFile(f[i].getAbsoluteFile(), new File(tar.getAbsoluteFile() + File.separator + f[i].getName()));
            }
        }
        return true;
    }

    /**
     * 移动文件 *
     */
    public static boolean moveFile(File src, File tar) throws Exception {
        if (copyFile(src, tar)) {
            deleteFile(src);
            return true;
        }
        return false;
    }

    /**
     * 删除文件 *
     */
    public static boolean deleteFile(File f) {
        if (!f.exists()) {
            return true;
        }
        if (f.isFile()) {
            return f.delete();
        }
        if (f.canRead()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    deleteFile(file);
                }
            }
        }
        return f.delete();
    }

    /**
     *   删除文件夹
     * @param folder 文件夹路径
     */
    public static void deleteDirectory(File folder){
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * 获取MIME类型 *
     */
    public static String getMIMEType(String name) {
        String type = "";
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("apk")) {
            return "application/vnd.android.package-archive";
        } else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp") || end.equals("rmvb")) {
            type = "video";
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg")
                || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        } else if (end.equals("txt") || end.equals("log")) {
            type = "text";
        } else {
            type = "*";
        }
        type += "/*";
        return type;
    }

    /**
     * 获取文件的扩展名
     *
     * @return 扩展名
     */
    public static String getFileExtension(File file) {
        String extension = "";
        if (file.isDirectory()) {
            return extension;
        }
        String path = file.getAbsolutePath();
        try {
            int lastIndex = path.lastIndexOf(".");
            if (lastIndex >= 0) {
                extension = path.substring(lastIndex + 1);
            } else {
                extension = "unknow";  // 位置类型的文件
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extension;
    }

    public static String getFileExtension(String path) {
        return getFileExtension(new File(path));
    }

    /**
     * 文件转换为字节数
     */
    public static byte[] file2byte(File file) throws Exception {
        FileInputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] b = new byte[1024];

        int n = 0;

        while ((n = inputStream.read(b)) != -1) {
            bos.write(b, 0, n);
        }

        inputStream.close();
        bos.close();

        return bos.toByteArray();

    }

    /**
     * 使用文件通道的方式复制文件
     *
     * @param s 源文件
     * @param t 复制到的新文件
     */
    public static void copyFileByChannel(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null, out = null;

        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);

            in = fi.getChannel();// 得到对应的文件通道
            out = fo.getChannel();// 得到对应的文件通道

            in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void dropRubbish(String name, Object rubbish) {
        try {

            String dir = Battle360Util.getFixedPath("crash");

            File crashFile = new File(dir);
            if (!crashFile.exists()) {
                crashFile.mkdirs();
            }

            if (BaseClassUtil.isNullOrEmptyString(name)) {
                name = rubbish.getClass().getSimpleName();
            }

            String path = dir + name + "-" + BaseClassUtil.getSystemTimeForFile() + ".txt";

            byte[] buffer = null;

            if (rubbish instanceof byte[]) {
                buffer = (byte[]) rubbish;
            } else if (rubbish instanceof String) {
                buffer = ((String) rubbish).getBytes();
            } else {
                buffer = new Gson().toJson(rubbish).getBytes();
            }

            FileOutputStream fos = new FileOutputStream(path);

            fos.write(buffer);
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeFile(String path, Object rubbish) {
        try {
            File file = new File(path);

            File p = file.getParentFile();

            if (!p.exists() && !p.mkdirs()) {
                return;
            }

            if (!file.exists() && !file.createNewFile()) {
                return;
            }

            byte[] buffer;

            if (rubbish instanceof byte[]) {
                buffer = (byte[]) rubbish;
            } else if (rubbish instanceof String) {
                buffer = ((String) rubbish).getBytes();
            } else {
                buffer = new Gson().toJson(rubbish).getBytes();
            }

            FileOutputStream fos = new FileOutputStream(path);

            fos.write(buffer);

            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean copyAssetToSD(String assetPath, String filePath) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {
            inStream = MyApplication.getInstance().getAssets().open(assetPath);

            File parent = new File(filePath).getParentFile();

            if (!parent.exists()) {
                parent.mkdirs();
            }

            outStream = new FileOutputStream(filePath);

            byte[] buffer = new byte[10 * 1024];

            int count = 0;

            while ((count = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, count);
            }

            outStream.flush();

            return new File(filePath).exists();
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }

                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFile(String path) {
        String content = "";
        BufferedReader reader = null;

        try {
            File file = new File(path);

            if (!file.exists()) {
                return content;
            }

            InputStreamReader is = new InputStreamReader(new FileInputStream(file));
            reader = new BufferedReader(is);

            String line;

            while ((line = reader.readLine()) != null) {
                content += line + " ";
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return content;
    }

    /**
     * 判断手机应用数据所在的sd卡是否存储空间不足
     *
     * @return ture存储不足，false 还有存储空间
     */
    public static boolean isDeviceStorageLow() {
        boolean b = false;
        File parentFile = new File(Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media))
                .getParentFile().getParentFile();
        StatFs statFs = new StatFs(parentFile.getPath());
//            int blockSize = statFs.getBlockSize();
        int availableBlocks = statFs.getAvailableBlocks();
        int blockCount = statFs.getBlockCount();

        if (((double) availableBlocks / blockCount - 0.1) < 0) {
            // 内存小于10%
            b = true;
        }
        return b;
    }

    public static void deleteMediaCache() {
        try {
            // 1，得到要清理的文件的路径
            // 媒体文件
            String mediaPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media);
            // 录音文件
            String recordPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Record);

            // 2，逐一遍历子文件，如果是文件则立马删除，如果是文件夹则继续遍历其子文件
            deleteMediaFile(new File(recordPath));
            deleteMediaFile(new File(mediaPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param file 遍历的目录
     */
    public static void deleteMediaFile(File file) throws Exception {
        if (!isDelete(file)) {
            return;
        }
        // 1,路径不存在
        if (file == null || !file.exists()) {
            return;
        }
        // 1,路径不是一个目录
        if (!file.isDirectory() || file.listFiles().length == 0) {  // 如果是一个目录必须是空目录
            Log.i("DeleteFile", "删除文件：" + file.getName() + "    " + (System.currentTimeMillis() - file.lastModified()));
            file.delete();
            return;
        }

        // 2,遍历路径下所有的文件
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            // 递归循环删除
            deleteMediaFile(childFile);
        }

        // 3，删除了文件中的所有子文件，最后再删除该目录
        Log.i("DeleteFile", "删除文件夹：" + file.getName() + "    " + (System.currentTimeMillis() - file.lastModified()));
        file.delete();
    }

    /**
     * 删除文件夹或者30天以前的文件
     *
     * @param file will delete file
     * @return true 删除，false不删除
     */
    public static boolean isDelete(File file) {
        return file.isDirectory() || System.currentTimeMillis() - file.lastModified() >= 2592000000L;
    }

    public static void initPrivateMediaVisible() {

        final boolean isShow = MyApplication.getInstance().getConfigValue("ShowPrivateMedia", 0) != 0;

        String mediaPath = Battle360Util.getFixedPath(Battle360Util.GlobalPath.Media);
        File file = new File(mediaPath);
        if (file.exists()) {
            File hideFile = new File(mediaPath + ".nomedia");
            if (isShow) {
                if (hideFile.exists()) {
                    try {
                        hideFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (!hideFile.exists()) {
                    try {
                        hideFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        String recordPath = Battle360Util.getFixedPath("Record");
        File recordFile = new File(recordPath);
        if (recordFile.exists()) {
            File hideFile = new File(recordPath + ".nomedia");
            if (isShow) {
                if (hideFile.exists()) {
                    try {
                        hideFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (!hideFile.exists()) {
                    try {
                        hideFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
