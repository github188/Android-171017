package com.mapgis.mmt;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.util.ResourceUtil;
import com.mapgis.mmt.config.Product;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.module.navigation.NavigationController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 *
 * @author user
 */
public class CrashHandler implements UncaughtExceptionHandler, Runnable {

    public static final String TAG = "CrashHandler";

    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();
    // 程序的Context对象
    private Context mContext;
    // 用来存储设备信息和异常信息
    private final LinkedHashMap<String, String> infos = new LinkedHashMap<>();

    // 用于格式化日期,作为日志文件名的一部分
    private final DateFormat formatter = new SimpleDateFormat("yyMMdd-HHmmss", Locale.CHINA);
    Throwable ex;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context 内容
     */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e("CrashHandler", "进入uncaughtException");

        this.ex = ex;

        if (ex != null) {
            if (!TextUtils.isEmpty(ex.getMessage()))
                Log.e("CrashHandler", ex.getMessage());

            ex.printStackTrace();
        }

        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常对象
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        Toast.makeText(mContext, "很抱歉,程序即将退出...", Toast.LENGTH_SHORT).show();

        // 收集设备参数信息
        collectDeviceInfo(mContext);

        // 保存日志文件
        MyApplication.getInstance().submitExecutorService(this);

        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx 内容
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            infos.put("VERSION", ResourceUtil.getVersionName(ctx));
            infos.put("TIME", BaseClassUtil.getSystemTime());

            infos.put("SERVER", ServerConnectConfig.getInstance().getBaseServerPath());
            infos.put("TITLE", Uri.encode(Product.getInstance().Title, "utf-8"));
            infos.put("LOGIN", MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).LoginName + "\n");

            Field[] fields = Build.class.getDeclaredFields();

            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    infos.put(field.getName(), field.get(null).toString());
                    Log.d(TAG, field.getName() + " : " + field.get(null));
                } catch (Exception e) {
                    Log.e(TAG, "an error occured when collect crash info", e);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 保存错误信息到文件中
     */
    @Override
    public void run() {
        try {
            Thread.currentThread().setName(this.getClass().getSimpleName());

            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                sb.append(key);
                sb.append("=");
                sb.append(value);
                sb.append("\n");
            }

            sb.append("\n");

            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);

            ex.printStackTrace(printWriter);

            Throwable cause = ex.getCause();

            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }

            printWriter.close();

            String result = writer.toString();
            sb.append(result);

            String time = formatter.format(new Date());

            String fileName = MyApplication.getInstance().getString(R.string.app_name) + "-crash-" + time + ".txt";

            final byte[] buffer = sb.toString().getBytes();

            try {
                // 上传服务器
                NetUtil.executeHttpPost(ServerConnectConfig.getInstance().getMobileBusinessURL()
                        + "/BaseREST.svc/UploadExceptionFile?FileName=" + Uri.encode(fileName, "utf-8"), buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 写入本地
            String path = Battle360Util.getFixedPath("crash");

            File dir = new File(path);

            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }


            FileOutputStream fos = new FileOutputStream(path + fileName);
            try {
                fos.write(buffer);
            } finally {
                fos.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        } finally {
            NavigationController.restartApp(mContext);
            NavigationController.exitAppSilent(mContext);
        }
    }


}
