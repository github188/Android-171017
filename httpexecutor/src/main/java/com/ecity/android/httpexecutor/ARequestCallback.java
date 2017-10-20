package com.ecity.android.httpexecutor;

import com.ecity.android.eventcore.EventBusUtil;
import com.ecity.android.eventcore.ReservedEvent;
import com.ecity.android.eventcore.ResponseEvent;
import com.ecity.android.log.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;

public abstract class ARequestCallback implements IRequestCallback {
    private static final String TAG = "ARequestCallback";//maoshoubei test modify


    public boolean isForComplexResponse() {
        return true;
    }


    /**
     * Get the event id for this request. This id will be used by EventBusUtil
     * when posting an event.
     *
     * @return
     */
    public abstract int getEventId();

    /**
     * Get the message to display when an error happens in local, or server
     * responds with error, but no message is returned.
     *
     * @return
     */
    public String getErrorMessage() {
        // Override in concrete class.
        return "请求失败";
    }

    public Object parseResponse(JSONObject jsonObj) {
        return (jsonObj == null) ? new JSONObject() : jsonObj;
    }

    @Override
    public void onError(Throwable e) {
        LogUtil.e(TAG, e);

        String msg = e.getMessage();
        if (e instanceof SocketTimeoutException) {
            msg = "请求超时";
        } else {
            msg = isBlankString(msg) ? getErrorMessage() : msg;
        }

        ResponseEvent event = new ResponseEvent(getEventId(), ReservedEvent.Response.ERROR, msg);
        EventBusUtil.post(event);
    }

    @Override
    public void onCompletion(String response) {
        LogUtil.v(this, "Response:\n" + response);

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(response);
        } catch (JSONException e) {
            ResponseEvent event = new ResponseEvent(getEventId(), ReservedEvent.Response.ERROR, "服务器返回结果异常");
            EventBusUtil.post(event);
            return;
        }


        ResponseEvent event = null;
        if (jsonObj == null) {
            event = new ResponseEvent(getEventId(), ReservedEvent.Response.ERROR, "访问服务器出现错误。可能是因为当前网络不可用，或网络限制导致无法访问服务器。");
            EventBusUtil.post(event);
            return;
        }



        boolean isOK = true;

        if (jsonObj.has("error")) {
            isOK = false;
        }

        isOK = jsonObj.optBoolean("isSuccess");
        if (!isOK) {
            isOK = jsonObj.optBoolean("success");
        }

        if (isOK) {
            LogUtil.d(TAG, "request success");
            Object data = parseResponse(jsonObj);
            event = new ResponseEvent(getEventId(), ReservedEvent.Response.OK, data);
        } else {
            String msg = "";
            if (jsonObj.has("error")) {
                JSONObject errorJson = jsonObj.optJSONObject("error");
                msg = errorJson.optString("message");
            } else if (jsonObj.has("msg")) {
                msg = jsonObj.optString("msg");
            } else if (jsonObj.has("message")) {
                msg = jsonObj.optString("message");
            }

            if (isBlankString(msg)) {
                msg = getErrorMessage();
            }
            LogUtil.e(TAG, "request error. " + msg);
            event = new ResponseEvent(getEventId(), ReservedEvent.Response.ERROR, msg);
        }

        EventBusUtil.post(event);
    }

    private boolean isBlankString(String str) {
        return (str == null) || (str.trim().length() == 0);
    }
}