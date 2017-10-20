package com.mapgis.mmt.module.systemsetting;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.DialogManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.AppVersionManager;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.systemsetting.download.DownloadManager;
import com.mapgis.mmt.module.systemsetting.download.DownloadService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppAboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addFragment(new AppAboutFragment());

        getBaseTextView().setText("关于");
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
    }

    class AppAboutFragment extends Fragment implements OnClickListener {
        private String serverDateStr;
        private String serverFileName;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.system_settings_about, container, false);

            addViewListener(view);

            view.findViewById(R.id.appShouldUpdateTip).setVisibility(View.GONE);

            new ServerAppVersion().executeOnExecutor(MyApplication.executorService);

            return view;
        }

        private void addViewListener(View view) {
            // 更新功能
            view.findViewById(R.id.appUpdateLayout).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppAboutActivity.this.setBaseProgressBarVisibility(true);
                    AppVersionManager appVersionManager = new AppVersionManager(AppAboutActivity.this, serverDateStr,
                            serverFileName);
                    appVersionManager.showToast(true);
                    appVersionManager.start();
                }
            });

            view.findViewById(R.id.mapUpdateLayout).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DownloadService.isRunning){
                        showToast("正在下载，暂停后再检查更新");
                        return;
                    }
                    DownloadManager.getInstance().checkMobileFileUpdate(AppAboutActivity.this, true);
                }
            });

            view.findViewById(R.id.layoutUpdateSLIB).setOnClickListener(this);
            view.findViewById(R.id.layoutUpdateCLIB).setOnClickListener(this);
            view.findViewById(R.id.layoutSystemReset).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.layoutUpdateSLIB) {
                new UpdateLibTask(getActivity()).mmtExecute("slib");
            } else if (v.getId() == R.id.layoutUpdateCLIB) {
                new UpdateLibTask(getActivity()).mmtExecute("clib");
            } else if(v.getId()==R.id.layoutSystemReset){
                DialogManager.showNormalDialog(AppAboutActivity.this,
                        R.drawable.icon_worning,
                        "警告",
                        "此操作将删除所有数据(包括应用数据和地图数据)，恢复APP初始设置，并重新启动APP，是否确认重置？",
                        new DialogManager.onDialogClickListener() {
                            @Override
                            public void onPositive() {

                                new SystemResetTask(getActivity()).mmtExecute();
                            }

                            @Override
                            public void onNegative() {

                            }
                        });
            }
        }
        class SystemResetTask extends MmtBaseTask<Void,Integer,Boolean>{

            public SystemResetTask(Context context) {
                super(context);
            }

            @Override
            protected void onSuccess(Boolean aBoolean) {
                if(aBoolean){
                    showToast("操作成功，系统将在3秒后重启");
                    AppManager.restartApp();
                }else {
                    showToast("操作失败");
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    //删除文件夹，删除偏好设置
                    AppManager.clearAppData();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }
        }

        class UpdateLibTask extends MmtBaseTask<String, Integer, Boolean> {
            public UpdateLibTask(Context context) {
                super(context);
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    String name = params[0];

                    String url = ServerConnectConfig.getInstance().getHostPath() + "/BufFile/Mobile/Map/" + name + ".zip";

                    File file = NetUtil.downloadTempFile(url);

                    String dir = Environment.getExternalStorageDirectory() + File.separator + "MapGIS" + File.separator
                            + name + File.separator;

                    File oldDir = new File(dir);

                    if (oldDir.exists())
                        oldDir.delete();

                    // 解压文件
                    FileZipUtil.unZip(file, dir);

                    String newDir = Battle360Util.getFixedPath(name);
                    if (newDir.contains(Battle360Util.newMapGISName)) {
                        File newDirFile = new File(newDir);

                        if (newDirFile.exists())
                            newDirFile.delete();

                        // 解压文件
                        FileZipUtil.unZip(file, newDir);
                    }

                    file.delete();

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();

                    return false;
                }
            }

            @Override
            protected void onSuccess(Boolean isSuccess) {
                if (isSuccess)
                    Toast.makeText(AppAboutActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(AppAboutActivity.this, "没有找到更新", Toast.LENGTH_SHORT).show();
            }
        }

        class ServerAppVersion extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... params) {
                try {

                    String serverResult = NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getMobileBusinessURL()
                            + "/BaseREST.svc/AppModifyTime", "");

                    ResultData<String> resultData = new Gson().fromJson(serverResult, new TypeToken<ResultData<String>>() {
                    }.getType());

                    if (resultData.ResultCode > 0) {
                        return resultData.ResultMessage;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {

                if (BaseClassUtil.isNullOrEmptyString(result)) {
                    return;
                }

                try {

                    serverFileName = result.split(",")[0];
                    serverDateStr = result.split(",")[1];

                    String nativeSaveTime = MyApplication.getInstance().getSystemSharedPreferences()
                            .getString("AppModifyTime", null);

                    if (nativeSaveTime != null) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date nativeDate = simpleDateFormat.parse(nativeSaveTime);
                        Date serverDate = simpleDateFormat.parse(serverDateStr);

                        // 判断本地和服务器端的版本号，只有当服务端版本号高于本地版本时才提示更新
                        if (serverDate.getTime() > nativeDate.getTime()) {
                            getView().findViewById(R.id.appShouldUpdateTip).setVisibility(View.VISIBLE);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
