package com.repair.zhoushan.module.casemanage.mydonecase;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.entity.Results;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseItem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FetchDoneCaseListTask extends MmtBaseTask<String, Integer, ArrayList<LinkedHashMap<String, String>>> {

    public FetchDoneCaseListTask(Context context, boolean showLoading, OnWxyhTaskListener<ArrayList<LinkedHashMap<String, String>>> listener) {
        super(context, showLoading, listener);
        setCancellable(false);
    }

    @Override
    protected ArrayList<LinkedHashMap<String, String>> doInBackground(String... params) {

        ArrayList<LinkedHashMap<String, String>> data = new ArrayList<LinkedHashMap<String, String>>();

        try {

            // params[0]:用户ID； params[1]:Token
            String url = ServerConnectConfig.getInstance().getBaseServerPath()
                    + "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/CaseBox/" + params[0]
                    + "/StardEventDoneBox?_mid=" + params[1] + "&pageIndex=" + params[2] + "&pageSize=" + params[3]
                    + "&eventInfo=" + Uri.encode((TextUtils.isEmpty(params[4]) ? "" : params[4]))
                    + "&sortFields=ID0&direction=desc";

            String json = NetUtil.executeHttpGetAppointLastTime(60, url);

            if (TextUtils.isEmpty(json))
                return null;

            Results<CaseItem> rawData = new Gson().fromJson(json, new TypeToken<Results<CaseItem>>() {
            }.getType());

            if (rawData == null) {
                return null;
            }

            ResultData<CaseItem> newData = rawData.toResultData();
            if (newData.ResultCode != 200) {
                return null;
            }

            LinkedHashMap<String, String> item;
            Field[] fields = CaseItem.class.getDeclaredFields();

            for (CaseItem caseItem : newData.DataList) {
                item = new LinkedHashMap<>();

                for (Field field : fields) {
                    field.setAccessible(true);
                    item.put(field.getName(), field.get(caseItem).toString());
                }
                data.add(item);
            }

            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

