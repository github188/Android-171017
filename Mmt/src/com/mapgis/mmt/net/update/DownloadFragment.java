package com.mapgis.mmt.net.update;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment.OnRightButtonClickListener;
import com.mapgis.mmt.config.CitySystemConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.module.systemsetting.DownloadMap;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 下载进度对话框
 */
public class DownloadFragment extends DialogFragment {
    private final static String PARAM_DOWNLOAD_MAPS = "download_maps";
    private LinearLayout parentLayout;
    private List<DownloadMap> downloadMaps;
    private ArrayList<HandleObj> objs = new ArrayList<>();

    private final String url = ServerConnectConfig.getInstance().getCityServerMobileBufFilePath() + "/Mobile/Map/";
    private final String mapPath = MyApplication.getInstance().getMapFilePath();

    public DownloadFragment() {

    }

    public static DownloadFragment newInstance(List<DownloadMap> mapList) {
        DownloadFragment fragment = new DownloadFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PARAM_DOWNLOAD_MAPS, (ArrayList<DownloadMap>) mapList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.getArguments() != null) {
            downloadMaps = this.getArguments().getParcelableArrayList(PARAM_DOWNLOAD_MAPS);
        } else {
            downloadMaps = new ArrayList<>();
        }

        View v = inflater.inflate(R.layout.ok_cancel_dialog, container, false);

        ((TextView) v.findViewById(R.id.tv_ok_cancel_dialog_Tips)).setText("下载查看");

        Button btnCancel = (Button) v.findViewById(R.id.btn_cancel);

        btnCancel.setText("后台下载");
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button btnOK = (Button) v.findViewById(R.id.btn_ok);

        btnOK.setVisibility(View.GONE);
        btnOK.setText("重新启动");

        btnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
            }
        });

        parentLayout = (LinearLayout) v.findViewById(R.id.layout_ok_cancel_dialog_content);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        getDialog().setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent arg2) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();

                    Toast.makeText(getActivity(), "已切换到后台继续下载离线地图", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setCancelable(false);

        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager.getActiveNetworkInfo() == null) {
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_SHORT).show();

            return;
        }

        if (manager.getActiveNetworkInfo().getType() != ConnectivityManager.TYPE_WIFI) {
            OkCancelDialogFragment fragment = new OkCancelDialogFragment("存在更新，但当前是移动网络，是否确认下载？");

            fragment.setOnRightButtonClickListener(new OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    start();
                }
            });

            fragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    DownloadFragment.this.dismiss();
                }
            });

            fragment.show(getActivity().getSupportFragmentManager(), "");
        } else {
            start();
        }
    }

    BroadcastReceiver receiver;

    private void start() {
        try {
            File file = new File(mapPath);

            if (!file.exists()) {
                file.mkdirs();
            }

            for (DownloadMap downloadMap : downloadMaps) {
                LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.map_down_item, null);

                parentLayout.addView(layout);

                HandleObj obj = new HandleObj();

                obj.downloadMap = downloadMap;
                obj.layout = layout;

                objs.add(obj);

                ((TextView) layout.findViewById(R.id.txtOper)).setText("下载中");

                Intent intent = new Intent(getActivity(), MmtUpdateService.class);

                intent.putExtra("url", url + downloadMap.MapName + ".zip").putExtra("path", mapPath);

                intent.putExtra("mapName", downloadMap.MapName).putExtra("serverTime", downloadMap.ServerTime)
                        .putExtra("userID", MyApplication.getInstance().getUserId());

                getActivity().startService(intent);
            }

            receiver = new MmtUpdateBroadcastReceiver(objs, handler);
            getActivity().registerReceiver(receiver, new IntentFilter(MmtUpdateService.class.getName()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 0) {
                    displayOkButton();

                    return;
                }

                HandleObj obj = (HandleObj) msg.obj;

                DownloadMap downloadMap = obj.downloadMap;
                LinearLayout layout = obj.layout;

                TextView txtState = (TextView) layout.findViewById(R.id.txtState);
                TextView txtOper = (TextView) layout.findViewById(R.id.txtOper);

                switch (msg.what) {
                    case MmtUpdateService.FLAG_LOADING:
                        showStatus(obj, downloadMap, layout, txtState);
                        break;
                    case MmtUpdateService.FLAG_DOWNLOADED:
                        txtOper.setText("解压中");
                        txtState.setText("正在解压");
                        break;
                    case MmtUpdateService.FLAG_COMPLETE:
                    case MmtUpdateService.FLAG_DEL_ZIP_FAILED:
                        txtOper.setText("已完成");
                        txtState.setText("解压以及后续处理完毕");

                        if (msg.what == MmtUpdateService.FLAG_DEL_ZIP_FAILED) {
                            Toast.makeText(getActivity(), "自动删除下载的ZIP文件失败,请联系技术人员", Toast.LENGTH_SHORT).show();
                        }

                        layout.setTag(true);
                        displayOkButton();
                        break;
                    case MmtUpdateService.FLAG_RENAME_FAILED:
                    case MmtUpdateService.FLAG_UNCATCH_EXCEPTION:
                        txtState.setText("文件下载发生错误");

                        if (msg.what == MmtUpdateService.FLAG_RENAME_FAILED) {
                            Toast.makeText(getActivity(), "临时文件夹重命名失败,请联系技术人员", Toast.LENGTH_SHORT).show();
                        }

                        layout.setTag(true);
                        displayOkButton();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void displayOkButton() {
            for (int i = 0; i < parentLayout.getChildCount(); i++) {
                View view = parentLayout.getChildAt(i);

                if (view.getTag() == null) {
                    return;
                }

                if (!(Boolean) view.getTag()) {
                    return;
                }
            }

            getView().findViewById(R.id.btn_ok).setVisibility(View.VISIBLE);
        }

        public void showStatus(HandleObj obj, DownloadMap downloadMap, LinearLayout layout, TextView txtState) {
            double current = obj.current;
            double total = obj.total;

            ProgressBar pbStatus = (ProgressBar) layout.findViewById(R.id.pbDownload);

            pbStatus.setMax(Integer.MAX_VALUE);
            pbStatus.setProgress((int) ((current / total) * Integer.MAX_VALUE));

            ((TextView) layout.findViewById(R.id.txtRadio)).setText((int) (current / total * 100) + "%");

            DecimalFormat df = new DecimalFormat("#.00");

            String progMb = (current < 1024 * 1024 ? ((int) (current / 1024) + "KB") : (df.format(current / (1024 * 1024)) + "MB"));

            String maxMb = (total < 1024 * 1024 ? ((int) (total / 1024) + "KB") : (df.format(total / (1024 * 1024)) + "MB"));

            txtState.setText(downloadMap.MapName + "(" + progMb + "/" + maxMb + ")");

            double t = (new Date().getTime() - obj.startTick) / 1000.0;// 消耗时间，单位：秒
            double sp = (current - obj.preSize) / 1024 / t;// 瞬时速度，单位：KB/S
            double remain = sp > 0 ? (total - current) / 1024 / sp : 0;// 剩余时间，单位：秒

            df = new DecimalFormat("00");

            String speed = (int) sp + "KB/S  " + df.format(remain / 3600) + ":" + df.format((remain % 3600) / 60.0) + ":"
                    + df.format((remain % 3600) % 60);

            ((TextView) layout.findViewById(R.id.txtSpeed)).setText(speed);
        }
    };

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            super.onDestroy();
        }
    }

    /**
     * 重启程序
     */
    private void restart() {
        try {
            DatabaseHelper.getInstance().delete(CitySystemConfig.class, "ConfigKey = 'MapPromptTime'");

            // 重新启动
            Intent intent = new Intent();

            // 参数1：包名，参数2：程序入口的activity
            intent.setClassName(MyApplication.getInstance().getPackageName(), ActivityClassRegistry.getInstance().getActivityClass("登录界面").getName());
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent restartIntent = PendingIntent.getActivity(MyApplication.getInstance().getApplicationContext(), 0, intent, 0);

            AlarmManager mgr = (AlarmManager) MyApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis(), restartIntent);

            AppManager.finishProgram();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
