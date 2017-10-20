package com.repair.shaoxin.water.highrisesearch;


import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.R;
import com.zondy.mapgis.map.MapLayer;

public class HRCVInputTextDialog extends BaseActivity {
    private EditText editText;
    private ListView listView;
    private Button hrcvQuery;
    private TextView hrcvLoading;

    private ArrayAdapter<String> adapter;
    private final List<String> waterMeterNums = new ArrayList<>();// 模糊匹配结果的所要显示的数据集

    private MapLayer layer;// 进行模糊匹配检索的图层

    private boolean isRun = true;// 模糊匹配线程是否在进行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.high_rise_close_valve_input);

        this.setTitle("请输入" + "水表卡号"/*HighRiseCloseValveConstant.LAYER_FIELDS*/);

        editText = (EditText) findViewById(R.id.hrcvInput);
        listView = (ListView) findViewById(R.id.hrcvList);
        hrcvQuery = (Button) findViewById(R.id.hrcvQuery);
        hrcvLoading = (TextView) findViewById(R.id.hrcvLoading);

        adapter = new ArrayAdapter<>(this, R.layout.simple_list_item_1, waterMeterNums);
        listView.setAdapter(adapter);

        listView.setVisibility(View.GONE);
        hrcvLoading.setVisibility(View.GONE);

        addMyFunction();

//        PatrolEquipmentsTask.searchLayerNameAndId();
//
//        layer = MyApplication.getInstance().mapGISFrame.getMapView().getMap().getLayer(
//                SessionManager.LayerNameAndIdHashtable.get(HighRiseCloseValveConstant.LAYER_NAME));
    }

    /** 给功能添加监听器 */
    private void addMyFunction() {
        QueryByFieldsTask.start();

        hrcvQuery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                HighRiseCloseValveConstant.queryUser.clear();
                HighRiseCloseValveConstant.queryValve.clear();

                HighRiseCloseValveConstant.MeterNo = editText.getText().toString().trim();

                new GetUserInfoTask(HRCVInputTextDialog.this).execute(editText.getText().toString().trim());

                finish();
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                editText.setText(arg0.getItemAtPosition(arg2).toString());
                hrcvQuery.performClick();
            }
        });
    }

    /** 模糊匹配与界面交互 */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {

                case 0:// 显示加载字样
                    hrcvLoading.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    break;

                case 1:// 解析数据
                    @SuppressWarnings("unchecked")
                    List<String> meterNos = (List<String>) msg.obj;

                    waterMeterNums.clear();

                    waterMeterNums.addAll(meterNos);

                    adapter.notifyDataSetChanged();

                case 2:// 显示结果
                    hrcvLoading.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    /** 根据数据的信息进行模糊匹配检索 */

    Thread QueryByFieldsTask = new Thread(new Runnable() {
        private String lastStr;

        @Override
        public void run() {

            while (isRun) {
                try {

                    String thisStr = editText.getText().toString().trim();

                    if (thisStr.length() < 3 || thisStr.equals(lastStr)) {
                        continue;
                    }

                    handler.sendEmptyMessage(0);

                    String uri = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/Zondy_MapGISCitySvr_HighFloor_SX/REST/HighFloorREST.svc/GetMeterNo?svrName="
                            + MobileConfig.MapConfigInstance.VectorService + "&likeVal="
                            + Uri.encode(thisStr, "utf-8");

                    String result = NetUtil.executeHttpGet(uri);

                    lastStr = thisStr;
                    thisStr = editText.getText().toString().trim();

                    if (result == null || result.length() == 0 || !thisStr.equals(lastStr)) {
                        handler.sendEmptyMessage(2);
                        continue;
                    }

                    List<String> meterNos = new Gson().fromJson(result, new TypeToken<List<String>>() {
                    }.getType());

                    Message msg = handler.obtainMessage();
                    msg.arg1 = 1;
                    msg.obj = meterNos;
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onDestroy() {
        isRun = false;
        super.onDestroy();
    }

}
