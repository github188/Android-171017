package com.repair.shaoxin.gas.editdata;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.global.MmtBaseTask;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.net.BaseStringTask;
import com.mapgis.mmt.net.BaseTaskListener;
import com.mapgis.mmt.net.BaseTaskParameters;
import com.zondy.mapgis.android.graphic.Graphic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyunfan on 2015/12/19.
 */
public class EditDataActivity extends BaseActivity {
    protected static HashMap<String, String> graphicMap;
    protected static Graphic graphic;
    private static LinearLayout parentLayout;
    private final int result = 1;
    private final int none = 2;
    private final int getRoleName = 12;
    private ProgressDialog progressDialog;
    //原先的全部可编辑的字段
    private static HashMap<String, String> oldValues = new HashMap<>();
    //新的可编辑的字段
    private HashMap<String, String> newValues = new HashMap<>();
    //改变了的字段
    private HashMap<String, String> hasEditValues = new HashMap<>();

    private static String geometryType = "";
    private static String DefaultValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBaseTextView().setText("属性编辑");
        progressDialog = new ProgressDialog(EditDataActivity.this);
        progressDialog.setTitle("属性编辑");
        progressDialog.setMessage("正在上报属性编辑");
        replaceFragment(new EditGisDataFrament());

        clearAllBottomUnitView();
        BottomUnitView manageUnitView = new BottomUnitView(this);
        manageUnitView.setContent("上报");
        addBottomUnitView(manageUnitView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasChange()) {
                    MyApplication.getInstance().submitExecutorService(getAuditRoleNameRunable);
                } else {
                    Toast.makeText(EditDataActivity.this, "未做任何修改", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean hasChange() {
        if (oldValues.size() > 0) {
            newValues.clear();
            for (int i = 0; i < parentLayout.getChildCount(); i++) {
                View view = parentLayout.getChildAt(i);
                if (!(view instanceof FeedBackView)) {
                    continue;
                }
                GDControl control = (GDControl) view.getTag();
                if (control.Type.equals("保留字")) {
                    continue;
                }
                //不是可编辑字段，跳过
                if (!oldValues.containsKey(control.Name)) {
                    continue;
                }
                FeedBackView feedBackView = (FeedBackView) view;
                newValues.put(control.Name, feedBackView.getValue());
            }
            //开始判断
            for (String key : oldValues.keySet()) {

                if (newValues.containsKey(key) && !(newValues.get(key).equals(oldValues.get(key)))) {
                    //顺便存储已编辑的字段
                    hasEditValues.put(key, newValues.get(key));
                    //return true;
                }

            }
            if (hasEditValues.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isXLK(String attrName, HashMap<String, String> editMap) {
        //数据字典中配置哪些属性为下拉框
        if (editMap.containsKey(attrName)) {
            DefaultValues = editMap.get(attrName);
            return true;
        }
        return false;
    }

    public static boolean isRead(String attrName, String isEditStr) {

        if (!BaseClassUtil.isNullOrEmptyString(isEditStr)) {
            if (isEditStr.contains(attrName)) {
                return false;
            }
        }
        return true;
    }

    public static class EditGisDataFrament extends Fragment {
        BaseActivity activity;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            activity = (BaseActivity) getActivity();
            ScrollView scrollView = new ScrollView(activity);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            parentLayout = new LinearLayout(activity);
            parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            parentLayout.setOrientation(LinearLayout.VERTICAL);

            scrollView.addView(parentLayout);
            try {
                if (activity.getIntent().hasExtra("graphic") && !activity.getIntent().hasExtra("graphicMap")) {
                    graphic = (Graphic) activity.getIntent().getSerializableExtra("graphic");
                    graphicMap = new HashMap<String, String>();
                    for (int i = 0; i < graphic.getAttributeNum(); i++) {
                        graphicMap.put(graphic.getAttributeName(i), graphic.getAttributeValue(i));
                    }
                } else {
                    graphic = (Graphic) activity.getIntent().getSerializableExtra("graphic");
                    graphicMap = (HashMap<String, String>) activity.getIntent().getSerializableExtra("graphicMap");

                }
                if (graphic != null) {
                    if (graphic.getGraphicType().value() == 1) {
                        geometryType = "point";
                    } else if (graphic.getGraphicType().value() == 3) {
                        geometryType = "line";
                    }
                }
                if (graphicMap != null) {
                    geometryType = graphicMap.get("$geometryType$").toString();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return scrollView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            //super.onViewCreated(view, savedInstanceState);
            new MmtBaseTask<Void, Void, ResultData<String>>(activity) {
                @Override
                protected ResultData<String> doInBackground(Void... params) {
                    try {
                        String sql = "";
                        if (geometryType.toLowerCase().contains("line")) {
                            sql = "select [NODENAME],[NODEVALUE] from [SYSDATADICTIONARY] where [PARENTID]=(select top(1) [NODEID] from [SYSDATADICTIONARY] where [NODENAME]='线表可编辑字段')";
                        } else if (geometryType.toLowerCase().contains("point")) {
                            sql = "select [NODENAME],[NODEVALUE] from [SYSDATADICTIONARY] where [PARENTID]=(select top(1) [NODEID] from [SYSDATADICTIONARY] where [NODENAME]='点表可编辑字段')";
                        }
                        if (sql.equals("")) {
                            return new ResultData<String>();
                        }
                        String resultStr = NetUtil
                                .executeHttpGet(
                                        ServerConnectConfig.getInstance()
                                                .getBaseServerPath()
                                                + "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetDictValuesBySQL",
                                        "sql", sql);

                        if (BaseClassUtil.isNullOrEmptyString(resultStr))
                            return null;

                        return new Gson().fromJson(resultStr,
                                new TypeToken<ResultData<String>>() {
                                }.getType());

                    } catch (Exception ex) {
                        return null;
                    }
                }

                @Override
                protected void onSuccess(ResultData<String> stringResultData) {
                    super.onSuccess(stringResultData);
                    if (stringResultData == null) {
                        return;
                    }
                    String xlkStr = "";

                    HashMap<String, String> editMap = new HashMap<>();
                    if (stringResultData.DataList.size() > 1) {
                        xlkStr = stringResultData.DataList.get(0);
                        String valueStr = stringResultData.DataList.get(1);
                        String[] names = xlkStr.split("\\|");
                        String[] values = valueStr.split("\\|");
                        //这有个奇怪的现象，以后面多个 分隔符相连时，split时只有一个
                        for (int i = 0; i < names.length; i++) {
                            if (values.length > i && !BaseClassUtil.isNullOrEmptyString(values[i])) {
                                editMap.put(names[i], values[i]);
                            }
                        }
                    }

                    if (graphicMap != null) {
                        for (String key : graphicMap.keySet()) {
                            boolean isRead = isRead(key, xlkStr);

                            //不可编辑的不展示
                            if (isRead) {
                                continue;
                            }

                            String value = graphicMap.get(key);
                            String type = "短文本";

                            DefaultValues = "";
                            boolean isxlk = isXLK(key, editMap);

                            oldValues.put(key, value);
                            if (key.contains("日期")) {
                                type = "仅日期V2";
                            } else if (isxlk) {
                                type = "下拉框";
                            } else if (key.contains("时间")) {
                                type = "日期框V2";
                            }

                            //都是文本信息
                            GDControl manText = new GDControl(key, type, (BaseClassUtil.isNullOrEmptyString(value) || value.equalsIgnoreCase("null") ? "" : value));

                            if (isxlk && !BaseClassUtil.isNullOrEmptyString(DefaultValues)) {
                                manText.DefaultValues = DefaultValues;
                            }

                            manText.setReadOnly(false);

                            View manTextView = manText.createView(activity);
                            parentLayout.addView(manTextView);
                        }
                    }

                }
            }.executeOnExecutor(MyApplication.executorService);
        }
    }

    Runnable getAuditRoleNameRunable = new Runnable() {
        @Override
        public void run() {
            try {
                String serviceUrl = "http://" + ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress + ":"
                        + ServerConnectConfig.getInstance().getServerConfigInfo().Port + "/"
                        + ServerConnectConfig.getInstance().getServerConfigInfo().VirtualPath
                        + "/Services/Zondy_MapGISCitySvr_Audit/REST/AuditREST.svc/GetAuditRoleName";

                String roleName = MyApplication.getInstance().getConfigValue("roleName");
                if (BaseClassUtil.isNullOrEmptyString(roleName)) {
                    roleName = "管网审核";
                }

                String resultStr = NetUtil.executeHttpGet(serviceUrl, "roleName",
                        roleName);

                JSONObject jsonObject = new JSONObject(resultStr);
                JSONArray jsonArray = jsonObject.getJSONArray("NameList");

                if (jsonArray == null || jsonArray.length() == 0) {
                    handler.obtainMessage(result, "未配置<管网审核>角色或角色下无审核人员").sendToTarget();
                } else {
                    String[] roleNames = new String[jsonArray.length()];

                    for (int i = 0; i < jsonArray.length(); i++) {
                        roleNames[i] = jsonArray.get(i).toString();
                    }

                    handler.obtainMessage(getRoleName, roleNames).sendToTarget();
                }
            } catch (Exception e) {
                handler.obtainMessage(result, "查询审核人员列表异常").sendToTarget();
            }
        }
    };

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // progressDialog.dismiss();

            switch (msg.what) {
                case result:
                    progressDialog.dismiss();
                    String result = (String) msg.obj;
                    Toast.makeText(EditDataActivity.this, result, Toast.LENGTH_SHORT).show();
                    break;
                case getRoleName:
                    createRoleNamaDlg(msg.obj);
                    break;
                case none:
                    Toast.makeText(EditDataActivity.this, "未做任何修改....", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void createRoleNamaDlg(Object msgObj) {

        List<String> items = Arrays.asList((String[]) msgObj);

        ListDialogFragment fragment = new ListDialogFragment("选择处理人", items);

        fragment.show(this.getSupportFragmentManager(), "1");

        fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
            @Override
            public void onListItemClick(int arg2, String value) {
                reportAttEdit(value);
            }
        });
    }

    private void reportAttEdit(String roleName) {

        String newValueString = "", oldValueString = "", attrFieldString = "";

        for (String key : hasEditValues.keySet()) {
            attrFieldString = attrFieldString + key + ",";
            newValueString = newValueString + hasEditValues.get(key) + ",";
            oldValueString = oldValueString + oldValues.get(key) + ",";
        }

        Map<String, String> paraMap = new HashMap<String, String>();

        paraMap.put("NewValue", newValueString.substring(0, newValueString.length() - 1));
        paraMap.put("OldValue", oldValueString.substring(0, oldValueString.length() - 1));

        paraMap.put("EquipFlag", "");
        if (graphic != null) {
            String EquipFlag = graphic.getAttributeValue("OID");
            if (!BaseClassUtil.isNullOrEmptyString(EquipFlag)) {
                paraMap.put("EquipFlag", String.valueOf(EquipFlag));
            }
        }

        if (graphicMap != null) {
            //这里编号实际上存储的是OID的值
            String EquipFlag = graphicMap.get("编号");
            if (!BaseClassUtil.isNullOrEmptyString(EquipFlag)) {
                paraMap.put("EquipFlag", String.valueOf(EquipFlag));
            }
        }
        if (BaseClassUtil.isNullOrEmptyString(paraMap.get("EquipFlag"))) {
            Toast.makeText(this, "未找到设备标志，无法提交", Toast.LENGTH_SHORT).show();
            return;
        }
        paraMap.put("MapFlag", "");
        if (graphic != null) {
            String MapFlag = graphic.getAttributeValue("$图层名称$");
            if (!BaseClassUtil.isNullOrEmptyString(MapFlag)) {
                paraMap.put("MapFlag", String.valueOf(MapFlag));
            }
        }

        if (graphicMap != null) {
            String MapFlag = graphicMap.get("$图层名称$");
            if (!BaseClassUtil.isNullOrEmptyString(MapFlag)) {
                paraMap.put("MapFlag", String.valueOf(MapFlag));
            }
        }
        if (BaseClassUtil.isNullOrEmptyString(paraMap.get("MapFlag"))) {
            Toast.makeText(this, "未找到图层名称，无法提交", Toast.LENGTH_SHORT).show();
            return;
        }
        // paraMap.put("MapFlag", String.valueOf(graphic.getAttributeValue("<图层名称>")));
        paraMap.put("Reporter", MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName);
        paraMap.put("Oper", "数据修正");
        paraMap.put("Time", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
        paraMap.put("AttrField", attrFieldString.substring(0, attrFieldString.length() - 1));
        paraMap.put("AuditUser", roleName);

        BaseStringTask task = new BaseStringTask(new BaseTaskParameters("http://"
                + ServerConnectConfig.getInstance().getServerConfigInfo().IpAddress + ":"
                + ServerConnectConfig.getInstance().getServerConfigInfo().Port
                + "/CityInterface/Services/zondy_mapgiscitysvr_audit/REST/auditrest.svc/InsertAttr", paraMap),
                new BaseTaskListener<String>() {
                    @Override
                    public void onError(Throwable paramThrowable) {
                        handler.obtainMessage(1, "修改属性上报失败").sendToTarget();
                    }

                    @Override
                    public void onCompletion(short completFlg, String localObject1) {
                        handler.obtainMessage(1, localObject1 != null && localObject1.length() > 0 ? localObject1 : "上报成功")
                                .sendToTarget();
                    }
                });
        progressDialog.show();
        MyApplication.getInstance().submitExecutorService(task);

    }
}
