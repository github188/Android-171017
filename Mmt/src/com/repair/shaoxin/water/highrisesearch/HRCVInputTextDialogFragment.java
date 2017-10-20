package com.repair.shaoxin.water.highrisesearch;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.mapgis.mmt.R;
import com.zondy.mapgis.map.MapLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询结果显示对话框。用于显示查询的图层以及查询到的结果的数量，并且可以勾选图层显示。
 */
public class HRCVInputTextDialogFragment extends DialogFragment {

    private TextView mTvTitle;
    private EditText editText;
    private ListView listView;
    private Button hrcvQuery;
    private TextView hrcvLoading;

    private ArrayAdapter<String> adapter;
    private final List<String> waterMeterNums = new ArrayList<String>();// 模糊匹配结果的所要显示的数据集

    private MapLayer layer;// 进行模糊匹配检索的图层

    private boolean isRun = true;// 模糊匹配线程是否在进行

    public HRCVInputTextDialogFragment() {
    }

    public static HRCVInputTextDialogFragment newInstance() {
        return new HRCVInputTextDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 该方法必须在添加content之前调用
//        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.high_rise_close_valve_input, container, false);
//        getDialog().requestWindowFeature(Window.getDefaultFeatures(getActivity()));

        initView(view);

        initData();

        return view;
    }

    private void initView(View view) {
        mTvTitle = (TextView) view.findViewById(R.id.textTitle);
        editText = (EditText) view.findViewById(R.id.hrcvInput);
        listView = (ListView) view.findViewById(R.id.hrcvList);
        hrcvQuery = (Button) view.findViewById(R.id.hrcvQuery);
        hrcvLoading = (TextView) view.findViewById(R.id.hrcvLoading);

//        this.getDialog().setTitle("请输入" + "水表卡号");
        mTvTitle.setText("请输入" + "水表卡号");
        listView.setVisibility(View.GONE);
        hrcvLoading.setVisibility(View.GONE);
    }

    private void initData() {
        adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_list_item_1, waterMeterNums);
        listView.setAdapter(adapter);

        addMyFunction();

        //        PatrolEquipmentsTask.searchLayerNameAndId();

//        layer = MyApplication.getInstance().mapGISFrame.getMapView().getMap().getLayer(//
//                SessionManager.LayerNameAndIdHashtable.get(HighRiseCloseValveConstant.LAYER_NAME));
    }

    @Override
    public void onStart() {
        super.onStart();
        final Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
//        DisplayMetrics dm = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
//
////        this.getDialog().setTitle("请输入" + "水表卡号");
////        getDialog().getWindow().setLayout(dm.widthPixels, getDialog().getWindow().getAttributes().height);
////        dm.heightPixels - DimenTool.dip2px(mContext,50)
        window.setLayout(-2, -2);  // 除去标题栏
//        window.setLayout(-1, -1);//这2行,和上面的一样,注意顺序就行;

        // 设置底部虚拟按键透明
//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }


    /**
     * 给功能添加监听器
     */
    private void addMyFunction() {
        QueryByFieldsTask.start();

        hrcvQuery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isRun = false;
                // 先关闭软键盘
                closeSoftKeyboard();

                HighRiseCloseValveConstant.queryUser.clear();
                HighRiseCloseValveConstant.queryValve.clear();

                HighRiseCloseValveConstant.MeterNo = editText.getText().toString().trim();

                new GetUserInfoTask(getActivity()).execute(editText.getText().toString().trim());

//                getActivity().finish();
                getDialog().dismiss();
            }

        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                editText.setText(arg0.getItemAtPosition(arg2).toString());
                hrcvQuery.performClick();
            }
        });
    }

    /**
     * 关闭软键盘
     */
    private void closeSoftKeyboard() {
        View view = this.getDialog().getWindow().peekDecorView();
        if (view != null) {
            // 获取输入法管理器
            InputMethodManager systemService = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            systemService.hideSoftInputFromInputMethod(view.getWindowToken(), 0);
        }
    }

    /**
     * 模糊匹配与界面交互
     */
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

    /**
     * 根据数据的信息进行模糊匹配检索
     */

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

                    // 这个服务有
//                    String uri = ServerConnectConfig.getInstance().getBaseServerPath()
//                            + "/Services/Zondy_MapGISCitySvr_HighFloor_SX/REST/HighFloorREST.svc/GetMeterNo?svrName="
//                            + MobileConfig.MapConfigInstance.VectorService + "&likeVal="
//                            + Uri.encode(thisStr, "utf-8");
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
    public void onDestroy() {
        isRun = false;
        super.onDestroy();
    }
}
