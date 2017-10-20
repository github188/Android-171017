package com.mapgis.mmt.common.util;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.module.relevance.DeviceMedia;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Created by Comclay on 2017/5/8.
 * 多媒体挂接
 */

public class GisMediaUtil {
    public final static String url = ServerConnectConfig.getInstance().getBaseServerPath()
            + "/Services/Zondy_MapGISCitySvr_Media/REST/MediaREST.svc/" + MobileConfig.MapConfigInstance.VectorService
            + "/MediaServer/UpLoadDeviceMediaFile";

    /**
     * gis多媒体属性上报
     *
     * @param context       上下文
     * @param deviceMedia   上报时的实体对象
     * @param relativePaths 服务器上相对路径
     * @param absolutePaths 本地绝对路径
     */
    public static void uploadGISMedia(Context context, DeviceMedia deviceMedia, String relativePaths, String absolutePaths) {
        if (BaseClassUtil.isNullOrEmptyString(absolutePaths)) {
            Toast.makeText(context, "未拍摄照片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (BaseClassUtil.isNullOrEmptyString(MobileConfig.MapConfigInstance.VectorService)) {
            Toast.makeText(context, "请配置<VectorService>节点信息", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> relativePathList = BaseClassUtil.StringToList(relativePaths, ",");
        List<String> absolutePathList = BaseClassUtil.StringToList(absolutePaths, ",");

        String result = null;

        for (int i = 0; i < relativePathList.size(); i++) {
            String absolutePath = absolutePathList.get(i);
            if (!new File(absolutePath).exists()) {
                continue;
            }

            deviceMedia.path = relativePathList.get(i);
            String jsonStr = new Gson().toJson(deviceMedia, DeviceMedia.class);
            ReportInBackEntity entity = new ReportInBackEntity(jsonStr, MyApplication.getInstance().getUserId(),
                    ReportInBackEntity.REPORTING, url, UUID.randomUUID().toString(), "拍照上报", absolutePath, deviceMedia.path);

            long m = entity.insert();
            if (m < 0) {
                result = "插入本地数据库失败";
            }
        }

        if (BaseClassUtil.isNullOrEmptyString(result)) {
            AppManager.finishActivity();
            Toast.makeText(context, "保存成功,等待上传", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
}
