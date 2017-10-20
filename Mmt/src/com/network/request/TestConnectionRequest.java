package com.network.request;

import android.content.Context;

import com.ecity.android.eventcore.EventBusUtil;
import com.ecity.android.eventcore.ResponseEvent;
import com.ecity.android.httpexecutor.ARequestCallback;
import com.ecity.android.httpexecutor.AbsBaseRequest;
import com.ecity.android.httpexecutor.AbsRequestParameter;
import com.ecity.android.httpexecutor.IRequestFinishCallback;
import com.ecity.android.httpexecutor.RequestExecutor;
import com.event.ResponseEventStatus;
import com.mapgis.mmt.common.widget.customview.ToastView;
import com.network.ServiceUrlManager;

import org.json.JSONObject;

import java.util.Map;

/***
 * Created by maoshoubei on 2017/10/20.
 */

public class TestConnectionRequest extends AbsBaseRequest {

    private Context mContext;
    private IRequestFinishCallback mIRequestFinishCallback;

    public TestConnectionRequest(Context context, IRequestFinishCallback iRequestFinishCallback) {
        this.mContext = context;
        this.mIRequestFinishCallback = iRequestFinishCallback;
    }

    @Override
    public void execute() {
        EventBusUtil.register(this);
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
                    }
                }.toMap();
            }

            @Override
            public Object parseResponse(JSONObject jsonObj) {
                return super.parseResponse(jsonObj);
            }
        });

    }

    public void onEventMainThread(ResponseEvent event) {
        if (!event.isOK()) {
            new ToastView(mContext, event.getMessage()).show();
            return;
        }
        switch (event.getId()) {
            case ResponseEventStatus.LOGIN_TEST_CONNECTION_DATA:
                handleGetTestConnectionData(event);
                break;
            default:
                break;
        }
    }

    private void handleGetTestConnectionData(ResponseEvent event) {
        mIRequestFinishCallback.onRequestProcessed(event);
        EventBusUtil.unregister(this);
    }


}
