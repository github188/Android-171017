package com.maintainproduct.entity;

import com.mapgis.mmt.entity.ResultData;

import java.util.ArrayList;

public class ResultDataWC<T> {
    public ResultDataWCStatus say;

    public ArrayList<T> getMe;

    public int currentPageIndex;
    public int totalRcdNum;

    public ResultData<T> toResultData() {
        ResultData<T> data = new ResultData<>();

        try {
            data.ResultCode = Integer.parseInt(say.statusCode);

            if (data.ResultCode == 0)
                data.ResultCode = 200;
        } catch (Exception ex) {
            data.ResultCode = 500;
            ex.printStackTrace();
        }

        data.ResultMessage = say.errMsg;

        data.DataList = getMe;

        return data;
    }
}