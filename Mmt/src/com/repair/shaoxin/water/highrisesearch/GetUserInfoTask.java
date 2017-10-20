package com.repair.shaoxin.water.highrisesearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.patrolproduct.module.projectquery.queryutil.QueryLayerUtil;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据表卡号获取用户信息
 * 获取用户的信息
 */
public class GetUserInfoTask extends AsyncTask<String, Void, List<HRCVUser>> {
    //    private final SherlockActivity context;
//    private final BaseActivity context;
    private final Context context;
    private ProgressDialog loadingDialog;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public GetUserInfoTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        loadingDialog = MmtProgressDialog.getLoadingProgressDialog(context, "正在查询卡号用户");
        loadingDialog.show();
    }

    @Override
    protected List<HRCVUser> doInBackground(String... params) {
        HighRiseCloseValveConstant.USER_GRAPHICS.clear();

        List<HRCVUser> resultAttrs = new ArrayList<>();
        try {
            MapLayer layer = new QueryLayerUtil(context, (MapGISFrame) context)
                    .findLayerByName(HighRiseCloseValveConstant.LAYER_NAME);
            FeaturePagedResult featurePagedResult = FeatureQuery.query(
                    (VectorLayer) layer
                    , HighRiseCloseValveConstant.LAYER_FIELDS + " like '%" + params[0] + "%'"
                    , null
                    , FeatureQuery.SPATIAL_REL_OVERLAP
                    , true
                    , true
                    , ""
                    , 10);

            if (featurePagedResult != null && featurePagedResult.getTotalFeatureCount() > 0) {
                List<Graphic> graphics = HighRiseCloseValveConstant.exchangeToGraphics(featurePagedResult);
                for (Graphic graphic : graphics) {
                    HRCVUser user = new HRCVUser();
                    List<HRCVUserAttr> list = new ArrayList<>();
                    for (int i = 0; i < graphic.getAttributeNum(); i++) {
                        HRCVUserAttr attr = new HRCVUserAttr();
                        attr.FiledName = graphic.getAttributeName(i);
                        attr.FiledVal = graphic.getAttributeValue(i);
                        list.add(attr);
                    }

                    user.FildInfoList = new ArrayList<>(list);
                    resultAttrs.add(user);
                }

                HighRiseCloseValveConstant.USER_GRAPHICS.addAll(graphics);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultAttrs;
    }

    @Override
    protected void onPostExecute(List<HRCVUser> result) {
        try {

            if (result == null || result.size() == 0) {
                Toast.makeText(context, "返回结果为空", Toast.LENGTH_SHORT).show();
                return;
            }

            HighRiseCloseValveConstant.queryUser.clear();
            HighRiseCloseValveConstant.queryUser.addAll(result);

            new GetCloseValveInfoTask(context).execute(HighRiseCloseValveConstant.MeterNo);

            HighRiseCloseValveConstant.showQueryUser();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadingDialog.dismiss();
        }
    }
}
