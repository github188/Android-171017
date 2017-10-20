package com.repair.auxiliary.pointquery.module.auxlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxData;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxTablesInfo;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.GetAuxDataResult;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.GisReferenceField;
import com.mapgis.mmt.R;
import com.patrol.entity.KeyPoint;
import com.patrol.module.KeyPoint.PointDetailFragment;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/13.
 */
public class AuxListActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleAndClear("附属数据");
        Fragment fragment = new AuxListFragment();
        replaceFragment(fragment);
    }

    public static class AuxListFragment extends Fragment {
        List<GetAuxDataResult> getAuxDataResultList = new ArrayList<>();
        BaseActivity baseActivity;
        List<AuxData> bakAllDataLists = new ArrayList<>();
        //adapter 数据源
        List<AuxData> dataLists = new ArrayList<>();
        AuxTablesInfo auxTablesInfo;
        HashMap<String, String> graphicMap;
        String layerID;
        List<String> types = new ArrayList<>();
        int auxTotal = 0;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            baseActivity = (BaseActivity) getActivity();
            super.onCreate(savedInstanceState);
            Intent intent = baseActivity.getIntent();
            auxTablesInfo = new Gson().fromJson(intent.getStringExtra("auxTablesInfo"), AuxTablesInfo.class);
            graphicMap = (HashMap<String, String>) intent.getSerializableExtra("graphicMap");
            layerID = baseActivity.getIntent().getStringExtra("layerID");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.aux_list, container, false);
            if (auxTablesInfo == null) {
                baseActivity.showErrorMsg("没有附属数据");
                return view;
            }
            if (graphicMap == null) {
                baseActivity.showErrorMsg("设备数据异常");
                return view;
            }
            if (TextUtils.isEmpty(layerID)) {
                baseActivity.showErrorMsg("无图层ID");
                return view;
            }

            final AuxAdapter adapter = new AuxAdapter(baseActivity, dataLists);
            final ListView lv = (ListView) view.findViewById(R.id.lvDetail);
            final TextView typeView = (TextView) view.findViewById(R.id.auxType);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AuxData auxData = dataLists.get(position);
                    PointDetailFragment fragment = new PointDetailFragment();
                    Bundle args = new Bundle();
                    args.putParcelable("kp", new KeyPoint());
                    args.putSerializable("attr", AuxUtils.dic2HM(auxData.attributes));
                    args.putStringArray("names", auxData.displayFieldList.toArray(new String[auxData.displayFieldList.size()]));
                    fragment.setArguments(args);
                    fragment.show(baseActivity.getSupportFragmentManager(), "");
                }
            });

            new MmtBaseTask<String, Void, String>(baseActivity) {
                @Override
                protected String doInBackground(String... params) {
                    try {
                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/zondy_mapgiscitysvr_auxdata/rest/auxdatarest.svc/" + MobileConfig.MapConfigInstance.VectorService + "/auxdataserver/" + params[0] + "/GetAuxTableData";
                        for (String name : auxTablesInfo.Names) {
                            String OIDS = "";
                            List<GisReferenceField> ReferenceFields = auxTablesInfo.ReferenceFields;
                            for (GisReferenceField gisReferenceField : ReferenceFields) {
                                if (gisReferenceField.TableName.equals(name)) {
                                    if (!TextUtils.isEmpty(gisReferenceField.ReferenceField) && graphicMap.containsKey(gisReferenceField.ReferenceField)) {
                                        OIDS += graphicMap.get(gisReferenceField.ReferenceField) + ",";
                                    }
                                }
                            }
                            if (!TextUtils.isEmpty(OIDS)) {
                                String result = NetUtil.executeHttpGet(url, "tableName", name, "OIDs", OIDS.substring(0, OIDS.length() - 1));
                                if (TextUtils.isEmpty(result)) {
                                    continue;
                                }
                                GetAuxDataResult getAuxDataResult = new Gson().fromJson(result, GetAuxDataResult.class);
                                if (getAuxDataResult != null&&getAuxDataResult.isSuccess) {
                                    getAuxDataResultList.add(getAuxDataResult);
                                    types.add(getAuxDataResult.tableName + "(" + getAuxDataResult.totalRcdNum + ")");
                                    auxTotal += getAuxDataResult.totalRcdNum;
                                }
                            }
                        }
                        types.add(0, "全部(" + auxTotal + ")");
                        dataLists.addAll(AuxUtils.getAuxDataListFromGetAuxDataResult(getAuxDataResultList));
                        bakAllDataLists.addAll(dataLists);
                        return null;
                    } catch (Exception ex) {
                        return ex.getMessage();
                    }
                }

                @Override
                protected void onSuccess(String s) {
                    if (s != null) {
                        baseActivity.showErrorMsg(s);
                    }
                    typeView.setText(types.size() > 0 ? types.get(0) : "全部");
                    adapter.notifyDataSetChanged();
                }
            }.mmtExecute(layerID);
            final ListDialogFragment fragment = new ListDialogFragment("附属数据", types);
            view.findViewById(R.id.layoutType).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.show(baseActivity.getSupportFragmentManager(), "");
                }
            });
            fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                @Override
                public void onListItemClick(int arg2, String value) {
                    typeView.setText(value);
                    dataLists.clear();
                    dataLists.addAll(AuxUtils.findAuxDataListByType(bakAllDataLists, value.split("\\(")[0]));
                    adapter.notifyDataSetChanged();
                }
            });
            lv.setAdapter(adapter);
            return view;
        }
    }
}
