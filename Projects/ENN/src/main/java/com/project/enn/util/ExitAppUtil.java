package com.project.enn.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;

import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.navigation.NavigationController;
import com.project.enn.ENNApplication;
import com.project.enn.dahua.Constant;
import com.project.enn.dahua.ServiceHelper;
import com.project.enn.dahua.service.AlarmHeartService;
import com.project.enn.dahua.service.DaHuaService;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * Created by Comclay on 2017/6/2.
 * 退出大华并退出中地
 */

public class ExitAppUtil {

    public static void exitDHZondy(final Context context) {
        exitDHZondy(context, true, new ExitCallback() {
            @Override
            public void onPostExit() {
                NavigationController.exitApp(context);
            }
        });
    }

    /**
     * 退出大华模块
     *
     * @param context 上下文
     * @param isShowDialog 是否显示进度
     * @param callback 退出大华后的回调接口
     */
    public static void exitDHZondy(Context context, final boolean isShowDialog, final ExitCallback callback) {
        if (!ENNApplication.getInstance().hasConnDHPermission()) {
            if (callback != null) callback.onPostExit();
            return;
        }

        final Service heartService = AppManager.getService(AlarmHeartService.class);

        if (heartService == null) {
            if (callback != null) callback.onPostExit();
            return;
        }
        new MmtBaseTask<Void, Void, String>(context, isShowDialog, "正在退出大华，请稍后！") {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DaHuaService service = (DaHuaService) AppManager.getService(DaHuaService.class);
                ServiceHelper.getInstance().unbindHeartService(service);
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    String sb = ServerConnectConfig.getInstance().getBaseServerPath() +
                            "/Services/MapgisCity_PatrolRepair_Xinao/REST/CaseManageREST.svc/LogoutDHApp?" +
                            "userName=" + Constant.getName();
                    return NetUtil.executeHttpGet(sb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                try {
                    if (context instanceof Activity && ((Activity) context).isFinishing())
                        return;

                    if (isShowDialog && loadingDialog != null && loadingDialog.isShowing())
                        loadingDialog.dismiss();

                    if (callback != null) callback.onPostExit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // 退出大华之后的回调接口
    public interface ExitCallback {
        void onPostExit();
    }
}
