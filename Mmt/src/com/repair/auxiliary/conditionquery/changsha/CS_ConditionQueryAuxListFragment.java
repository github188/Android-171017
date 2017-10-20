package com.repair.auxiliary.conditionquery.changsha;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.repair.auxiliary.conditionquery.module.ConditionQueryAuxListFragment;

import java.util.Arrays;

/**
 * Created by liuyunfan on 2016/4/20.
 */
public class CS_ConditionQueryAuxListFragment extends ConditionQueryAuxListFragment {
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            Object layerIDstr = context.getIntent().getSerializableExtra("layerIDs");
            if (layerIDstr == null) {
                context.showErrorMsg("返回图层id列表错误");
                return;
            }

            String[] layerIDs = (String[]) layerIDstr;

            getAuxDataIDsResult.getGetAuxDataIDsResultFromGisServerByLayerIDS(context, Arrays.asList(layerIDs), geometry, strCon, strAuxTableName, new AuxUtils.AfterOnsucess() {
                @Override
                public void afterSucess() {
                    if (getAuxDataIDsResult.OIDs.size() == 0) {
                        context.showErrorMsg("当前条件无附属数据");
                        return;
                    }
                    listView.setRefreshing(false);
                }
            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.lookDeatil(dataList.get(position - 1).atts);
                }
            });
        } catch (Exception ex) {
        }
    }
}
