package com.mapgis.mmt.module.systemsetting.task;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Comclay on 2017/3/27.
 */

public class ServerAppVersion extends AsyncTask<Void, Void, String> {
    private NewAppCallback mCallback;
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
            String serverDateStr = result.split(",")[1];
            String nativeSaveTime = MyApplication.getInstance().getSystemSharedPreferences()
                    .getString("AppModifyTime", null);

            if (nativeSaveTime != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                Date nativeDate = simpleDateFormat.parse(nativeSaveTime);
                Date serverDate = simpleDateFormat.parse(serverDateStr);

                // 判断本地和服务器端的版本号，只有当服务端版本号高于本地版本时才提示更新
                if (serverDate.getTime() > nativeDate.getTime()) {
                    // app更新
                    if (mCallback != null){
                        mCallback.needUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(NewAppCallback callback){
        this.mCallback = callback;
        this.execute();
    }

    public interface NewAppCallback{
        public void needUpdate();
    }
}
