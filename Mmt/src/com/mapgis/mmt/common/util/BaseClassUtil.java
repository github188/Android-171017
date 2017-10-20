package com.mapgis.mmt.common.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.mapgis.mmt.MyApplication;
import com.simplecache.ACache;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseClassUtil {
    public static boolean isNullOrEmptyString(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String getSystemTime(String pattern, Date date) {
        return (new SimpleDateFormat(pattern, Locale.CHINA)).format(date);
    }

    public static String getSystemTime(String pattern) {
        return getSystemTime(pattern, new Date());
    }

    public static String getSystemTime(Date date) {
        return getSystemTime("yyyy-MM-dd HH:mm:ss", date);
    }

    public static String getSystemTime() {
        return getSystemTime("yyyy-MM-dd HH:mm:ss");
    }

    public static String getSystemTimeForFile() {
        return getSystemTime("yyMMdd_HHmmss");
    }

    public static Date parseTime(String time, String pattern) {
        try {
            return (new SimpleDateFormat(pattern, Locale.CHINA)).parse(time);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Date parseTime(String time) {
        if (TextUtils.isEmpty(time))
            return null;

        if (time.contains("-"))
            return parseTime(time, "yyyy-MM-dd HH:mm:ss");
        else if (time.contains("/"))
            return parseTime(time, "yyyy/MM/dd HH:mm:ss");
        else
            return null;
    }

    public static String getSystemDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA); // 设置日期格式

        return df.format(new Date()); // new Date()为获取当前系统时间
    }

    public static String getSystemTimePart() {
        SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss", Locale.CHINA); // 设置日期格式

        return df.format(new Date()); // new Date()为获取当前系统时间
    }

    public static String getPhotoDir(String name) {
        String currentDate = BaseClassUtil.getSystemDate();

        String userName = MyApplication.getInstance().getUserBean().TrueName;

        return name + "/" + currentDate + "/" + userName + "/";
    }

    public static boolean isNum(String str) {
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    public static boolean isFullWebUrl(String path) {
        // Replace all chinese character.
        path = path.replaceAll("[\\u4e00-\\u9fa5]+", "");
        return path.matches("^(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]$");
    }

    /**
     * 将字符串List表转换为字符串
     */
    public static String listToString(List<String> list) {

        StringBuilder builder = new StringBuilder();

        for (String str : list) {
            builder.append(str).append(",");
        }

        return builder.length() == 0 ? "" : builder.substring(0, builder.length() - 1);
    }

    /**
     * 将字符串表转换为字符串List
     */
    public static List<String> StringToList(String arg0, String symbol) {

        List<String> mediaList = new ArrayList<>();

        if (isNullOrEmptyString(arg0)) {
            return mediaList;
        }

        if (arg0.contains(symbol)) {
            mediaList = Arrays.asList(arg0.split(symbol));
        } else {
            mediaList.add(arg0);
        }

        return mediaList;
    }

    /**
     * 以字节为单位读取文件，常用于读二进制文件，如图片、声音、影像等文件。
     */
    public static byte[] readFileByBytes(String fileName) {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in = new FileInputStream(fileName);
            byte[] buf = new byte[1024];

            int length;
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return out.toByteArray();
    }

    public static byte[] getBitmapByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public static byte[] drawNoImage(int width, int height, String msg) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.RED);

        Paint paint = new Paint();

        paint.setColor(Color.WHITE);

        paint.setTextSize(20);

        canvas.drawText(msg, 0, 50, paint);

        return getBitmapByte(bitmap);
    }

    public static boolean isImg(String url) {
        if (BaseClassUtil.isNullOrEmptyString(url)) {
            return false;
        }
        url = url.toLowerCase();
        return url.endsWith("jpg") || url.endsWith("png") || url.endsWith("bmp") || url.endsWith("gif") || url.endsWith("tiff") || url.endsWith("raw") || url.endsWith("jpeg") || url.endsWith("ico") || url.endsWith("tif");
    }

    /**
     * 是否是可预览附件
     *
     * @param url
     * @return
     */
    public static boolean isReadedFile(String url) {
        if (BaseClassUtil.isNullOrEmptyString(url)) {
            return false;
        }
        url = url.toLowerCase();
        if (url.endsWith("pdf")) {
            return true;
        }
        if (url.endsWith("txt")) {
            return true;
        }
        if (url.endsWith("xml")) {
            return true;
        }
        if (url.endsWith("doc") || url.endsWith("docx")) {
            return true;
        }
        if (url.endsWith("xls") || url.endsWith("xlsx")) {
            return true;
        }
        return url.endsWith("ppt") || url.endsWith("pptx");

    }

    /**
     * 判断时间time是否在日期date内
     *
     * @param time
     * @param date
     * @return
     */
    public static boolean isInDate(String time, Date date) {
        try {
            if (TextUtils.isEmpty(time) || date == null) {
                return false;
            }
            Date timeDate = parseTime(time, "yyyy-MM-dd");
            long tiemDates = timeDate.getTime();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long datStart = c.getTime().getTime();

            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);
            long datEnd = c.getTime().getTime();

            return tiemDates >= datStart && tiemDates <= datEnd;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 判断时间time是否小于日期date（在日期的date的左边）
     *
     * @param time
     * @param date
     * @return
     */
    public static boolean isInLeftDate(String time, Date date) {
        try {
            if (TextUtils.isEmpty(time) || date == null) {
                return false;
            }
            Date timeDate = parseTime(time, "yyyy-MM-dd");
            long tiemDates = timeDate.getTime();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long datStart = c.getTime().getTime();

            return tiemDates < datStart;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 判断key是否含有中文
     *
     * @param key
     * @return
     */
    public static boolean isContainChinese(String key) {
        // 判断key是否包含中文，如果没有中文不做显示
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        for (char k : key.toCharArray()) {
            if (String.valueOf(k).matches("[\\u4e00-\\u9fa5]+")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取字符串中非 Ascii字符的个数
     */
    public static int getNonAsciiCount(String str) {
        if (isNullOrEmptyString(str)) {
            return 0;
        }
        int count = 0;
        char[] charArr = str.toCharArray();
        for (char c : charArr) {
            String s = Integer.toBinaryString(c);
            if (s.length() > 8) {
                count++;
            }
        }
        return count;
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in setId(int).
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static <T> T requireNonNull(T o, String message) {
        if (o == null) {
            if (message == null)
                throw new NullPointerException();
            else
                throw new NullPointerException(message);
        }
        return o;
    }

    public static String getApplicationName(Context context) {

        String appName = "";

        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            appName = (String) pm.getApplicationLabel(appInfo);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return appName;
    }

    private static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    private static String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isMainProcess(Context context) {
        String processName = getProcessName(context, android.os.Process.myPid());

        if (TextUtils.isEmpty(processName))
            processName = getProcessName();

        //判断进程名，保证只有主进程运行主进程初始化逻辑
        return !TextUtils.isEmpty(processName) && processName.equals(context.getPackageName());
    }

    //清理缓存时会删除
    public static ACache getACache() {
        ACache mCache;
        File file = new File(Battle360Util.getFixedPath("ACache"));
        if (file.exists()) {
            mCache = ACache.get(file);
        } else {
            if (file.mkdirs()) {
                mCache = ACache.get(file);
            } else {
                mCache = ACache.get(MyApplication.getInstance().getBaseContext());
            }
        }
        return mCache;
    }

    //清理缓存时不会删除
    public static ACache getConfigACache() {
        ACache mCache;
        File file = new File(Battle360Util.getFixedPath("Config"));
        if (file.exists()) {
            mCache = ACache.get(file);
        } else {
            if (file.mkdirs()) {
                mCache = ACache.get(file);
            } else {
                mCache = ACache.get(MyApplication.getInstance().getBaseContext());
            }
        }
        return mCache;
    }

    public static String toString(Object obj) {
        return "[" + android.os.Process.myPid() + "-" + Thread.currentThread().getId() + "]" + obj.getClass().getSimpleName() + '@' + Integer.toHexString(obj.hashCode());
    }

    public static void logv(Object obj, String text) {
        Log.v(toString(obj), text);
    }

    public static void logd(Object obj, String text) {
        Log.d(toString(obj), text);
    }

    public static void logi(Object obj, String text) {
        Log.i(toString(obj), text);
    }

    public static void logw(Object obj, String text) {
        Log.w(toString(obj), text);
    }

    public static void loge(Object obj, String text) {
        Log.e(toString(obj), text);
    }

    /**
     * 验证时间值是否合法
     */
    public static boolean validateDateValue(String value) {
        return !"1900-01-01 00:00:00".equals(value) && !"1900-01-01 00:00:00.000".equals(value);
    }

    public static String trim(String str) {
        return trim(str, "");
    }

    public static String trim(String str, String c) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        if (c == null) {
            return str;
        }
        if (c.length() == 0 || c.equals(" ")) {
            return str.trim();
        }


        if(str.startsWith(c)){
            int index=str.indexOf(c);
            str=str.substring(index+c.length());
        }

        if(str.endsWith(c)){
            int endIndex=str.lastIndexOf(c);
            str=str.substring(0,endIndex);
        }

        return str;
    }
}
