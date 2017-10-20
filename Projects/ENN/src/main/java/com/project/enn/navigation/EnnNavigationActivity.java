package com.project.enn.navigation;

import android.view.View;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.systemsetting.download.DownloadService;
import com.mapgis.mmt.net.update.MmtUpdateService;
import com.project.enn.util.ExitAppUtil;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Comclay on 2017/6/2.
 * 新奥主菜单
 */

public class EnnNavigationActivity extends NavigationActivity {

    @Override
    public void onBackPressed() {
        if (MyApplication.getInstance().getConfigValue("NavigationBackMode", 0) == 1) {

            String exitTip = "确认退出" + getString(com.mapgis.mmt.R.string.app_name);
            if (MmtUpdateService.isRunning || DownloadService.isRunning) {
                exitTip = "地图更新中," + exitTip;
            }
            OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment(exitTip);
            okCancelDialogFragment.setLeftBottonText("继续使用");
            okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    ExitAppUtil.exitDHZondy(EnnNavigationActivity.this);
                }
            });
            okCancelDialogFragment.show(getSupportFragmentManager(), "");

        } else {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - touchTime) > 2000) {
                MyApplication.getInstance().showMessageWithHandle("再按一次退出程序");

                touchTime = currentTime;
            } else {
                if (DownloadService.isRunning || MmtUpdateService.isRunning) {
                    final OkCancelDialogFragment fragment = new OkCancelDialogFragment(
                            "正在更新地图");

                    fragment.setLeftBottonText("强制退出");
                    fragment.setRightBottonText("继续下载");

                    fragment.setOnLeftButtonClickListener(new OkCancelDialogFragment.OnLeftButtonClickListener() {

                        @Override
                        public void onLeftButtonClick(View view) {
                            ExitAppUtil.exitDHZondy(EnnNavigationActivity.this);
                        }
                    });

                    fragment.show(getSupportFragmentManager(), "");
                } else {
                    ExitAppUtil.exitDHZondy(EnnNavigationActivity.this);
                }
            }
        }
    }

    /*切换站点时结束大华相关的服务*/
    @Override
    protected void exchangeStation(final ServerConfigInfo info) {
        try {
            List<String> corps = new ArrayList<>(info.Corps);

            corps.remove(info.CorpName);

            ListDialogFragment dialogFragment = new ListDialogFragment("切换站点", corps);

            dialogFragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                @Override
                public void onListItemClick(int arg2, String value) {
                    new MmtBaseTask<String, Integer, ResultData<ServerConfigInfo>>(EnnNavigationActivity.this, true, "正在切换，请稍候...") {
                        @Override
                        protected ResultData<ServerConfigInfo> doInBackground(String... params) {
                            return getServerConfigData(params[0], info);
                        }

                        @Override
                        protected void onPostExecute(final ResultData<ServerConfigInfo> data) {
                            try {
                                loadingDialog.dismiss();
                                if (data == null) {
                                    Toast.makeText(EnnNavigationActivity.this, "切换失败,可能企业中不存在当前用户", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                ExitAppUtil.exitDHZondy(EnnNavigationActivity.this, false, new ExitAppUtil.ExitCallback() {
                                    @Override
                                    public void onPostExit() {
                                        renterLoginActivity(data.getSingleData());
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.mmtExecute(value);
                }
            });

            dialogFragment.show(getSupportFragmentManager(), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
