package com.mapgis.mmt.config;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnLeftButtonClickListener;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.common.widget.fragment.ProgressDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ProgressDialogFragment.OnButtonClickListener;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.navigation.NavigationActivity;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 软件版本自动检测升级线程
 *
 * @author Zoro
 */
public class AppVersionManager extends Thread {
    private final int UPDATE_CLIENT = 0;
    private final int JUST_PASS = 1;
    private final int DOWN_ERROR_FILE = 3;
    private final int NET_ERROR = 4;

    private final BaseActivity activity;

    private String serverDateStr;
    private String serverFileName;

    /**
     * 当没有检测到 新版本 时 是否 仍然 弹出 更新提示框
     */
    public Boolean alertWhenNoNewVersion = true;
    /**
     * 是否强制更新，从服务配置读取，默认不强制
     */
    private boolean isForceUpdate = false;

    /**
     * serverDateStr,serverFileName参数若为空,则调用查询服务查询信息
     *
     * @param activity       窗体
     * @param serverDateStr  服务端文件最后修改时间
     * @param serverFileName 服务端文件名
     */
    public AppVersionManager(BaseActivity activity, String serverDateStr, String serverFileName) {
        this.activity = activity;
        this.serverDateStr = serverDateStr;
        this.serverFileName = serverFileName;
        this.isForceUpdate = MyApplication.getInstance().getConfigValue("ForceUpdate", 0) == 1;
    }

    /**
     * 从服务器获取json解析并进行比对版本号
     */
    @Override
    public void run() {
        try {
            Thread.currentThread().setName(this.getClass().getName());

            // 若传过来的没有时间及文件信息,则进行网络查询更新
            if (BaseClassUtil.isNullOrEmptyString(serverDateStr) || BaseClassUtil.isNullOrEmptyString(serverFileName)) {

                String url = ServerConnectConfig.getInstance().getMobileBusinessURL()
                        + "/BaseREST.svc/AppModifyTime";
                String appName = activity.getResources().getString(R.string.app_name);
                String json = NetUtil.executeHttpGet(url, "appName", appName);

                if (TextUtils.isEmpty(json))
                    return;

                ResultData<String> data = new Gson().fromJson(json, new TypeToken<ResultData<String>>() {
                }.getType());

                if (data != null && data.ResultCode > 0) {
                    serverFileName = data.ResultMessage.split(",")[0];
                    serverDateStr = data.ResultMessage.split(",")[1];
                } else {
                    return;
                }
            }

            // 若有文件信息,则进行更新
            if (!BaseClassUtil.isNullOrEmptyString(serverDateStr) && !BaseClassUtil.isNullOrEmptyString(serverFileName)) {
                String nativeSaveTime = MyApplication.getInstance().getSystemSharedPreferences().getString("AppModifyTime", null);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Date nativeDate = simpleDateFormat.parse(nativeSaveTime);
                Date serverDate = simpleDateFormat.parse(serverDateStr);

                // 判断本地和服务器端的版本号，只有当服务端版本号高于本地版本时才提示更新
                if (serverDate.getTime() > nativeDate.getTime()) {
                    handler.sendEmptyMessage(UPDATE_CLIENT);
                    return;
                }

                handler.sendEmptyMessage(JUST_PASS);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        handler.sendEmptyMessage(NET_ERROR);
    }

    private boolean isShow = true;

    /**
     * 是否弹出Toast提示信息
     */
    public void showToast(boolean isShow) {
        this.isShow = isShow;
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            try {
                activity.setBaseProgressBarVisibility(false);

                switch (msg.what) {
                    case UPDATE_CLIENT:
                        showUpdateDialog("新版本更新");
                        break;
                    case JUST_PASS:
                        if (alertWhenNoNewVersion) {
                            showUpdateDialog("已经是最新版本,是否仍然更新");
                        }
                        break;
                    case 2:
                        Toast.makeText(activity, "下载新版本失败", Toast.LENGTH_SHORT).show();
                        break;
                    case DOWN_ERROR_FILE:
                        Toast.makeText(activity, "下载的文件长度过小,可能是异常文件", Toast.LENGTH_SHORT).show();
                        break;
                    case NET_ERROR:
                        if (!isShow) {
                            return;
                        }
                        Toast.makeText(activity, "网络异常,获取文件信息失败，可能是网络不通畅，或服务器端更新信息文件不存在", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    /**
     * 弹出对话框通知用户更新程序
     * <p/>
     * 弹出对话框的步骤： <br>
     * 1.创建alertDialog的builder. <br>
     * 2.要给builder设置属性, 对话框的内容,样式,按钮<br>
     * 3.通过builder 创建一个对话框 4.对话框show()出来
     */
    protected void showUpdateDialog(String tip) {

        String desc;
        if (isForceUpdate) {
            desc = "为使应用正常使用，请更新到最新版本： " + serverDateStr;
        } else {
            desc = "最新版本： " + serverDateStr;
        }
        View view = initContentView(desc);

        OkCancelDialogFragment fragment = new OkCancelDialogFragment(tip, view);
        fragment.setCancelable(false);

        if (isForceUpdate) {
            fragment.setHideLeftButton();
        } else {
            fragment.setLeftBottonText("下次再说");
            fragment.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    NavigationActivity.updateChecked = true;
                }
            });
        }

        fragment.setRightBottonText("立即下载");
        fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                try {
                    downLoadApk();
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.showToast("下载更新文件失败");
                }
            }
        });

        fragment.show(activity.getSupportFragmentManager(), "1");
    }

    private View initContentView(String msgStr) {

        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(activity);
        textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            textView.setTextAppearance(activity, R.style.default_text_medium_1);
        } else {
            textView.setTextAppearance(R.style.default_text_medium_1);
        }

        textView.setText(msgStr);

        linearLayout.addView(textView);

        return linearLayout;
    }

    /**
     * 从服务器中下载APK
     */
    protected void downLoadApk() throws Exception {
        // 如果相等的话表示当前的外置存储卡挂载在手机上并且是可用的
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        // APP更新文件存放路径
        File fileDir = new File(Battle360Util.getFixedMapGISPath(true)+"Update/");

        if (!fileDir.exists()) {
            if (!fileDir.mkdirs())
                throw new Exception("文件操作失败");
        }

        String updateAPKFileName = serverFileName.substring(serverFileName.indexOf("/") + 1, serverFileName.length());

        File file = new File(Battle360Util.getFixedMapGISPath(true)+"Update/", updateAPKFileName);

        if (file.exists()) {
            if (!file.delete())
                throw new Exception("文件操作失败");
        }

        if (!file.createNewFile())
            throw new Exception("文件操作失败");

        final ProgressDialogFragment fragment = new ProgressDialogFragment();

        Bundle bundle = new Bundle();

        bundle.putString("title", "更新程序");
        bundle.putString("tip", "正在下载更新文件");

        fragment.setArguments(bundle);

        fragment.setCancelable(false);
        fragment.createShowHorizontal(true);

        fragment.show(activity.getSupportFragmentManager(), "");

        String myUrl = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/DownloadApk?FileName="
                + Uri.encode(serverFileName);

        FinalHttp fh = new FinalHttp();

        // 调用download方法开始下载
        fh.download(myUrl, file.getAbsolutePath(), false, new AjaxCallBack<File>() {

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                try {
                    handler.sendEmptyMessage(NET_ERROR);

                    fragment.dismissAllowingStateLoss();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onLoading(long count, long current) {
                try {
                    fragment.setMax(count);
                    fragment.setProgress(current);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(final File t) {
                try {
                    if (t.length() < 5 * 1024 * 1024) {
                        handler.obtainMessage(DOWN_ERROR_FILE).sendToTarget();
                        fragment.dismiss();
                        return;
                    }

                    // 更新完后,下次产品包更新,需有提示框
                    SharedPreferences sharedPreferences = MyApplication.getInstance().getSystemSharedPreferences();

                    Editor editor = sharedPreferences.edit();

                    editor.putBoolean("isShowAppUpdateDialog", true);
                    editor.putString("AppModifyTime", serverDateStr);

                    editor.apply();

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    fragment.setButtonText("立即安装");
                    fragment.setButtonVisibility(View.VISIBLE);
                    fragment.setOnButtonClickListener(new OnButtonClickListener() {
                        @Override
                        public void onButtonClick(View view) {
                            installApk(t);
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // 安装apk
    protected void installApk(File file) {
        try {
            Intent intent = new Intent();

            //解决部分手机上安装一半闪退，后台偷偷继续安装的问题
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 执行动作
            intent.setAction(Intent.ACTION_VIEW);

            // 执行的数据类型
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");

            activity.startActivityForResult(intent, 1001);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}