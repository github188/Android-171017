package com.mapgis.mmt.entity;

import java.util.ArrayList;

public class Results<T> {

    public ResultStatus say;
    public ArrayList<T> getMe;

    public int currentPageIndex;
    public int totalRcdNum;

    public Results() {
    }

    public Results(String statusCode, String errorMsg) {
        this.say = new ResultStatus(statusCode, errorMsg);
    }

    public ResultData<T> toResultData() {

        ResultData<T> data = new ResultData<>();

        try {
            // The rule of FrontPage：ResultCode==0 -> Succeed;  ResultCode!=0 -> Failed.
            data.ResultCode = Integer.parseInt(say.statusCode);

            if (data.ResultCode == 0) {
                data.ResultCode = 200;
            } else {
                data.ResultCode = -100;
            }
        } catch (Exception ex) {
            data.ResultCode = -100;
            ex.printStackTrace();
        }

        data.ResultMessage = say.errMsg;

        data.DataList = getMe != null ? getMe : new ArrayList<T>();

        return data;
    }
}