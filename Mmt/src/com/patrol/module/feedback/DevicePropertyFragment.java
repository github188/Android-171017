package com.patrol.module.feedback;

import android.os.Bundle;
import android.view.View;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.mapgis.mmt.common.util.GisUtil;
import com.mapgis.mmt.module.gis.spatialquery.PipeDetailFragment;
import com.patrol.entity.KeyPoint;
import com.mapgis.mmt.global.MmtBaseTask;
import com.zondy.mapgis.android.graphic.Graphic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DevicePropertyFragment extends PipeDetailFragment {
    private String[] columnNames;
    private KeyPoint keyPoint;

    public static DevicePropertyFragment createNewInstance(KeyPoint keyPoint) {

        Bundle args = new Bundle();

        args.putParcelable("keyPoint", keyPoint);

        args.putString("layerName", keyPoint.GisLayer);
        args.putString("filedVal", keyPoint.FieldValue);
        args.putSerializable("xy", keyPoint.Position);

        args.putString("patrolNo", String.valueOf(keyPoint.ID));
        args.putInt("isArrive", keyPoint.IsArrive);

        DevicePropertyFragment fragment = new DevicePropertyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        keyPoint = args.getParcelable("keyPoint");

        fetchGisDetail(keyPoint.GisLayer, keyPoint.FieldName, keyPoint.FieldValue);
    }

    @Override
    protected void afterViewCreated(View view) {
        super.afterViewCreated(view);

        View rootView = getView();
        if (rootView != null) {
            rootView.findViewById(R.id.layoutActionBar).setVisibility(View.GONE);
        }
    }

    private void fetchGisDetail(final String gisLayer, final String fieldName, final String fieldValue) {

        new MmtBaseTask<String, Void, HashMap<String, String>>(getActivity()) {
            @Override
            protected HashMap<String, String> doInBackground(String... params) {
                LinkedHashMap<String, String> attr = new LinkedHashMap<>();

                try {
                    columnNames = null;

                    List<Graphic> graphics = GisQueryUtil.conditionQuery(gisLayer, fieldName + "='" + fieldValue + "'");

                    if (graphics == null || graphics.size() == 0)
                        throw new Exception("没有查询到属性信息");

                    Graphic graphic = graphics.get(0);

                    for (int i = 0; i < graphic.getAttributeNum(); i++)
                        attr.put(graphic.getAttributeName(i), graphic.getAttributeValue(i));

                    if (GisQueryUtil.isUseOfflineQuery())
                        columnNames = GisUtil.getGISFields(gisLayer);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return attr;
            }

            @Override
            protected void onSuccess(HashMap<String, String> hashMap) {
                try {
                    hashMap.put("图层", keyPoint.GisLayer);

                    if (hashMap.size() == 1) {
                        if (columnNames != null) {
                            for (String column : columnNames) {
                                hashMap.put(column, "");
                            }
                        } else {
                            hashMap.put(keyPoint.FieldName, keyPoint.FieldValue);
                        }
                    }

                    graphicMap = hashMap;

                    getActivity().getIntent().putExtra("layerName", keyPoint.GisLayer);
                    getActivity().getIntent().putExtra("pipeNo", keyPoint.FieldValue);
                    getActivity().getIntent().putExtra("xy", keyPoint.Position);

                    getActivity().getIntent().putExtra("graphicMap", graphicMap);

                    ArrayList<String> result = resolveGraphicMap(hashMap, columnNames);

                    ArrayList<String> data = getPropertyData();
                    data.clear();
                    data.addAll(result);
                    adapter.notifyDataSetChanged();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }.mmtExecute();
    }
}
