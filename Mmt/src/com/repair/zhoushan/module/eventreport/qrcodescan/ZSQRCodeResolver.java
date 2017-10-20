package com.repair.zhoushan.module.eventreport.qrcodescan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.repair.common.BaseTaskResultData;
import com.mapgis.mmt.global.OnResultListener;
import com.repair.zhoushan.module.QRCodeResolver;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 舟山：事件上报二维码扫描完成表单字段填写
 */
public class ZSQRCodeResolver implements QRCodeResolver<Map<String, String>> {

    private final WeakReference<Context> mContext;
    // tableName;where;columns
    private final String configInfo;

    public ZSQRCodeResolver(Context context, String configInfo) {
        this.mContext = new WeakReference<>(context);
        this.configInfo = configInfo;
    }

    @Override
    public Map<String, String> resolve(String codeData) {

        final Context context = mContext.get();
        if (context == null) {
            return null;
        }
        if (TextUtils.isEmpty(codeData)) {
            Toast.makeText(context, "扫码结果为空", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (!codeData.contains("|")) {
            Toast.makeText(context, "无法解析：" + codeData, Toast.LENGTH_SHORT).show();
            return null;
        }

        // 6713|Z-6R92038|铁皮柜|E1920||临城营业所||||
        // 6713 : 设备代号 1
        // Z-6R92038 : 设备编号 2
        // 铁皮柜 : 设备名称 3
        // E1920 : 规格型号 4
        // 临城营业所 : 使用部门 6

        Map<String, String> result = new HashMap<>();
        String[] snippets = codeData.split("\\|");
        if (snippets.length > 1) {
            result.put("设备代号", snippets[0]);
            result.put("设备编号", snippets[1]);

            if (snippets.length > 2) {
                result.put("设备名称", snippets[2]);

                if (snippets.length > 3) {
                    result.put("规格型号", snippets[3]);

                    if (snippets.length > 5) {
                        result.put("使用部门", snippets[5]);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void resolve(final String codeData, final OnResultListener<Map<String, String>> listener) {

        Context context = mContext.get();
        if (context == null) {
            return;
        }
        final Map<String, String> rawResult = resolve(codeData);
        if (rawResult == null || rawResult.size() == 0) {
            return;
        }

        BaseTaskResultData<String, Void, KeyValuePair> baseTask
                = new BaseTaskResultData<String, Void, KeyValuePair>(context) {
            @NonNull
            @Override
            protected String getRequestUrl() throws Exception {

                String[] configs = configInfo.split(";");
                if (configs.length != 3 || TextUtils.isEmpty(configs[0])) {
                    throw new Exception("Configuration error");
                }

                // tableName/whereClause cannot be empty
                String tableName = configs[0];
                String columns = configs[2];

                StringBuilder whereSb = new StringBuilder();
                String[] whereParams = configs[1].split(",");
                for (int i = 0, length = whereParams.length; i < length; i++) {
                    String param = whereParams[i];
                    if (TextUtils.isEmpty(param)) {
                        continue;
                    }
                    String paramValue = rawResult.get(param);
                    if (!TextUtils.isEmpty(paramValue)) {
                        whereSb.append(param).append("='").append(paramValue).append("' AND ");
                    }
                }
                if (whereSb.length() == 0) {
                    throw new Exception("Configuration error");
                }
                whereSb.delete(whereSb.lastIndexOf("AND"), whereSb.length());

                StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getMobileBusinessURL());
                sb.append("/BaseREST.svc/GetTableSingleRowInfo")
                        .append("?tableName=").append(tableName)
                        .append("&whereClause=").append(whereSb.toString())
                        .append("&columns=").append(columns);
                return sb.toString();

//                sb.append("/BaseREST.svc/GetTableSingleRowInfo")
//                        .append("?tableName=").append("养护台账表")
//                        .append("&whereClause=").append("设备编号 LIKE '").append(equipmentCode).append("'")
//                        .append("&columns=").append("位置,设备编号,设备代号,出厂编号,生产厂家,设备名称,规格型号");
            }

            @Override
            protected void onSuccess(ResultData<KeyValuePair> resultData) {
                super.onSuccess(resultData);
                if (resultData.ResultCode < 0 || resultData.DataList.size() == 0) {
                    listener.onFailed(resultData.ResultMessage);
                    return;
                }
                HashMap<String, String> result = new HashMap<>();
                for (KeyValuePair keyValuePair : resultData.DataList) {
                    result.put(keyValuePair.key, keyValuePair.value);
                }
                listener.onSuccess(result);
            }
        };
        baseTask.setCancellable(false);
        baseTask.mmtExecute();
    }

    public class KeyValuePair {
        public String key;
        public String value;
    }

}
