package com.network;

import android.util.Log;

import com.AbsRequestParameter;
import com.ecity.android.httpexecutor.*;
import com.ecity.android.httpexecutor.RequestParameter;
import com.event.ResponseEventStatus;
import com.network.request.TestConnectionParameters;

import org.json.JSONObject;

import java.util.Map;

/***
 * Created by maoshoubei on 2017/10/19.
 */

public class LoginService {
    private static LoginService instance;

    static {
        instance = new LoginService();
    }

    private LoginService() {

    }

    public static LoginService getInstance() {
        return instance;
    }

    /*
    * 获取测试连接
    * */
    public void getTestConnectionData(final String userId) {
        RequestExecutor.execute(new ARequestCallback() {
            @Override
            public int getEventId() {
                return ResponseEventStatus.LOGIN_TEST_CONNECTION_DATA;
            }

            @Override
            public boolean isPost() {
                return false;
            }

            @Override
            public String getUrl() {
                return ServiceUrlManager.getInstance().getTestConnectionUrl();
            }

            @Override
            public Map<String, String> getParameter() throws Exception {
                return new AbsRequestParameter() {

                    @Override
                    protected void fillParameters(Map<String, String> map) {
                        map.put("userId", userId);
                    }
                }.toMap();
            }


            @Override
            public Object parseResponse(JSONObject jsonObj) {
                Log.i("Mao", "jsonObject" + jsonObj);
                return super.parseResponse(jsonObj);
            }
        });
    }

    //

}
