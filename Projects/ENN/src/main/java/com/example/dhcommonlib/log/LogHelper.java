package com.example.dhcommonlib.log;

import android.util.Log;

/**
 * 功能说明：
 * 版权申明：浙江大华技术股份有限公司
 * 创建标记：Chen_LiJian 2017-03-24
 */
public class LogHelper {

    private static String projectTag = "YYS";
    private static boolean isLogEnabled = true;

    public static void d(String tag, String content){
        if (isLogEnabled) {
            Log.d(tag, content);
        }
    }

    public static void v(String tag, String content){
        if (isLogEnabled) {
            Log.v(tag, content);
        }
    }

    public static void w(String tag, String content){
        if (isLogEnabled) {
            Log.w(tag, content);
        }
    }

    public static void e(String tag, String content){
        if (isLogEnabled) {
            Log.e(tag, content);
        }
    }

    public static void i(String tag, String content){
        if (isLogEnabled) {
            Log.i(tag, content);
        }
    }

    public static void setLogEnabled(boolean isLogEnabled) {
        LogHelper.isLogEnabled = isLogEnabled;
    }

    public static void setTag(String tag) {
        LogHelper.projectTag = tag;
    }

    public static void verbose(String log) {
        Log.v(projectTag, log);
    }

    public static void debug(String log) {
        Log.d(projectTag, log);
    }

    public static void info(String log) {
        Log.i(projectTag, log);
    }

    public static void warn(String log) {
        Log.w(projectTag, log);
    }

    public static void error(String log) {
        Log.e(projectTag, log);
    }

}
