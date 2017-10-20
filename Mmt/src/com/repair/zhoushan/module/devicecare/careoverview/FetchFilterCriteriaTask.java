package com.repair.zhoushan.module.devicecare.careoverview;

import android.content.Context;

import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;

public final class FetchFilterCriteriaTask extends MmtBaseTask<String, Void, String[]> {

    static final String FILTER_START_TIME = "开始时间";
    static final String FILTER_TASK_STATE = "任务状态";

    static final String FILTER_STATION_NAME = "场站名称";
    static final String FILTER_DEVICE_TYPE = "设备类型";
    static final String FILTER_AREA_NAME = "区域";
    static final String FILTER_CARE_PERSON = "养护人";
    static final String FILTER_COMP_TYPE = "部件类型";

    private final String URL_STATION_NAME = "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/%s/StationNames?bizName=%s&level=1";
    private final String URL_DEVICE_TYPE = "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/DeviceManage/%s/DeviceNames?bizName=%s&level=2";
    private final String URL_AREA_NAME = "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/%s/BizAreaName?bizName=%s";
    private final String URL_CARE_PERSON = "/Services/Zondy_MapGISCitySvr_CaseManage/REST/CaseManageREST.svc/Maintenance/GetDepartTree?bizName=%s&userID=%s";

    private final String BASE_URL;

    public FetchFilterCriteriaTask(Context context, boolean showLoading, boolean isCancellable, OnWxyhTaskListener<String[]> listener) {
        super(context, showLoading, listener);
        setCancellable(isCancellable);
        BASE_URL = ServerConnectConfig.getInstance().getBaseServerPath();
    }

    /**
     * @param params bizName
     */
    @Override
    protected String[] doInBackground(String... params) {

        final String bizName = params[0];

        String[] filterTypes = getFilterTypesByBiz(bizName);
        String[] jsonResults = new String[filterTypes.length * 2]; // even:bizName; odd:url

        for (int i = 0; i < filterTypes.length; i++) {

            switch (filterTypes[i]) {

                case FILTER_STATION_NAME:
                    jsonResults[i * 2] = FILTER_STATION_NAME;
                    jsonResults[i * 2 + 1] = BASE_URL + String.format(URL_STATION_NAME, userID, bizName);
                    break;
                case FILTER_DEVICE_TYPE:
                    jsonResults[i * 2] = FILTER_DEVICE_TYPE;
                    jsonResults[i * 2 + 1] = BASE_URL + String.format(URL_DEVICE_TYPE, userID, bizName);
                    break;
                case FILTER_AREA_NAME:
                    jsonResults[i * 2] = FILTER_AREA_NAME;
                    jsonResults[i * 2 + 1] = BASE_URL + String.format(URL_AREA_NAME, userID, bizName);
                    break;
                case FILTER_CARE_PERSON:
                    jsonResults[i * 2] = FILTER_CARE_PERSON;
                    jsonResults[i * 2 + 1] = BASE_URL + String.format(URL_CARE_PERSON, bizName, userID);
                    break;
            }
        }

        for (int i = 1; i < jsonResults.length; i += 2) {
            jsonResults[i] = NetUtil.executeHttpGet(jsonResults[i]);  // even:bizName; odd:jsonResult
        }

        return jsonResults;
    }

    /**
     * 获取业务类型对应的过滤条件列表
     */
    private String[] getFilterTypesByBiz(final String bizName) {

        String[] filterTypes;
        switch (bizName) {
            case "场站设备":
                filterTypes = new String[]{FILTER_STATION_NAME, FILTER_DEVICE_TYPE};
                break;
            case "场站设备检定":
                filterTypes = new String[]{FILTER_STATION_NAME, FILTER_DEVICE_TYPE};
                break;
            case "车用设备":
                filterTypes = new String[]{};
                break;
            case "车用设备检定":
                filterTypes = new String[]{FILTER_STATION_NAME, FILTER_DEVICE_TYPE};
                break;
            case "调压器养护":
                filterTypes = new String[]{FILTER_AREA_NAME, FILTER_CARE_PERSON};
                break;
            case "阀门养护":
                filterTypes = new String[]{FILTER_CARE_PERSON};
                break;
            case "防腐层检测":
                filterTypes = new String[]{FILTER_CARE_PERSON};
                break;
            case "工商户安检":
                filterTypes = new String[]{FILTER_AREA_NAME, FILTER_CARE_PERSON};
                break;
            case "工商户表具":
                filterTypes = new String[]{};
                break;
            case "工商户表具检定":
                filterTypes = new String[]{};
                break;
            case "工商户抄表":
                filterTypes = new String[]{};
                break;
            case "工商户台账":
                filterTypes = new String[]{};
                break;
            case "阴极保护桩检测":
                filterTypes = new String[]{};
                break;
            default:
                filterTypes = new String[0];
        }
        return filterTypes;
    }
}
