package com.mapgis.mmt.entity;

import android.text.TextUtils;

public class ResultStatus {

    public String statusCode = "-1";
    public String errMsg = "";

    public ResultStatus() {
    }

    public ResultStatus(String statusCode, String errMsg) {
        this.statusCode = statusCode;
        this.errMsg = errMsg;
    }

    public ResultWithoutData toResultWithoutData() {

        ResultWithoutData data = new ResultWithoutData();

        data.ResultMessage = this.errMsg;

        try {
            // The rule of FrontPage：ResultCode==0 -> Succeed;  ResultCode!=0 -> Failed.
            data.ResultCode = Integer.parseInt(statusCode);

            if (data.ResultCode == 0) {
                data.ResultCode = 200;
            } else if (TextUtils.isEmpty(data.ResultMessage) && data.ResultCode > 0) {
                // 认为只要上报有错误，则errorMessage就不为空，相当于用 errorMessage 是否为空标志是否成功
                // 两个特殊的状态码ResultCode为新插入记录的id服务：PostEventData 和 PostFlowNodeData

                return data;
            } else {
                data.ResultCode = -100;
            }
        } catch (Exception ex) {
            data.ResultCode = -100;

            ex.printStackTrace();
        }

        return data;
    }
}
