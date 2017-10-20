package com.mapgis.mmt.module.gis.investigation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.net.BaseStringTask;
import com.mapgis.mmt.net.BaseTaskListener;
import com.mapgis.mmt.net.BaseTaskParameters;
import com.zondy.mapgis.android.graphic.Graphic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AttributeEditActivity extends FragmentActivity {
    private final int result = 1;
    private final int none = 2;
    private final int getRoleName = 12;

    protected ArrayList<String> arrayList;
    private final ArrayList<String> editList = new ArrayList<String>();
    protected HashMap<String, String> graphicMap;
    protected Graphic graphic;

    protected EmsPipeDetailActivityAdapter adapter;
    private ButtonToolbarFragment attEdtToolFrgmt;

    private ProgressDialog progressDialog;

    private final ButtonToolbarFragment.OnBtnClickListener onSaveAttributeBtnClick = new ButtonToolbarFragment.OnBtnClickListener() {
        @Override
        public void onClick() {
            if (editList.size() == 0) {
                Toast.makeText(AttributeEditActivity.this, "未做任何修改",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            MyApplication.getInstance().submitExecutorService(getAuditRoleNameRunable);
            // reportAttEdit("");
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.pipe_detail);

        ((TextView) findViewById(R.id.textview_Title)).setText("属性编辑");

        attEdtToolFrgmt = new ButtonToolbarFragment("保存属性");
        attEdtToolFrgmt.setSaveBtnClickListener(onSaveAttributeBtnClick);
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_pipe_detail_toolbar, attEdtToolFrgmt).commit();

        findViewById(R.id.detail_revert_btn).setOnClickListener(revertOnClickListener);
        findViewById(R.id.detail_loc_btn).setOnClickListener(locOnClickListener);
        //属性编辑不用定位
        findViewById(R.id.detail_loc_btn).setVisibility(View.GONE);
        progressDialog = new ProgressDialog(AttributeEditActivity.this);
        progressDialog.setTitle("属性编辑");
        progressDialog.setMessage("正在上报属性编辑");

        arrayList = new ArrayList<String>();

        try {
            if (getIntent().hasExtra("graphic") && !getIntent().hasExtra("graphicMap")) {
                graphic = (Graphic) getIntent().getSerializableExtra("graphic");
                graphicMap = new LinkedHashMap<>();
                for (int i = 0; i < graphic.getAttributeNum(); i++) {
                    graphicMap.put(graphic.getAttributeName(i), graphic.getAttributeValue(i));
                }
            } else {
              //  graphic = (Graphic) getIntent().getSerializableExtra("graphic");
                graphicMap = (HashMap<String, String>) getIntent().getSerializableExtra("graphicMap");

                if (getIntent().hasExtra("graphicMapStr")) {
                    graphicMap = new Gson().fromJson(getIntent().getStringExtra("graphicMapStr"), new TypeToken<LinkedHashMap<String, String>>() {
                    }.getType());
                }
            }

            if (graphicMap != null) {
                for (String str : graphicMap.keySet()) {
                    String key = str;
                    String value = graphicMap.get(str);

                    if ("emapgisid".equalsIgnoreCase(key)) {
                        ((TextView) findViewById(R.id.detail_title)).setText(key + ":" + value);
                        continue;
                    }

                    // 跳过类似这种自定义的字段 $图层名称$
                    if (key.startsWith("$") && key.endsWith("$")) {
                        continue;
                    }

                    // 判断key是否包含中文，如果没有中文不做显示
                    boolean isExistChinese = false;

                    for (char k : key.toCharArray()) {
                        isExistChinese = String.valueOf(k).matches("[\\u4e00-\\u9fa5]+");

                        if (isExistChinese) {
                            break;
                        }
                    }

                    if (!isExistChinese) {
                        continue;
                    }

                    arrayList.add(key + "`" + ((value.equalsIgnoreCase("null") || value.equalsIgnoreCase("")) ? "-" : value));
                }
            }

            adapter = new EmsPipeDetailActivityAdapter(this, arrayList);
            ((ListView) findViewById(R.id.ListView_asset_detail)).setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回地图
     */
    OnClickListener revertOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * 管件定位
     */
    OnClickListener locOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setResult(ResultCode.RESULT_PIPE_LOCATE);
            finish();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            findViewById(R.id.detail_loc_btn).performClick();
        }

        return super.onKeyDown(keyCode, event);
    }

    public class EmsPipeDetailActivityAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final ArrayList<String> arrayList;

        public EmsPipeDetailActivityAdapter(Context context, ArrayList<String> arrayList) {
            mInflater = LayoutInflater.from(context);
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position >= 0 && position < arrayList.size() ? arrayList.get(position) : ":";
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.asset_detail_props_item, null);
                holder = new ViewHolder();
                holder.assetKey = (TextView) convertView.findViewById(R.id.asset_key);
                holder.assetValue = (TextView) convertView.findViewById(R.id.asset_value);
                holder.editTextView = (TextView) convertView.findViewById(R.id.asset_value_text);
                holder.editTextView.setVisibility(View.VISIBLE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.position = position;

            String keyValue = arrayList.get(position);
            String[] keyValueArr = keyValue.indexOf('`') >= 0 ? keyValue.split("`") : null;
            if (keyValueArr != null) {
                if (keyValueArr.length >= 2) {
                    holder.assetKey.setText(keyValueArr[0] != null ? keyValueArr[0] : "");
                    holder.assetValue.setText(null != keyValueArr[1] ? keyValueArr[1] : "");
                } else {
                    holder.assetKey.setText(keyValueArr[0] != null ? keyValueArr[0] : "");
                    holder.assetValue.setText("");
                }
            }

            holder.editTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDialog(holder.assetKey.getText().toString(), holder.assetValue.getText().toString(), holder.position);
                }
            });

            convertView.setTag(holder);
            return convertView;
        }

        public class ViewHolder {
            public TextView assetKey;
            public TextView assetValue;
            public TextView editTextView;
            public int position;
        }
    }

    private void createDialog(String key, String value, int position) {
        Intent intent = new Intent(AttributeEditActivity.this, ProtertyEditDialog.class);
        intent.putExtra("isFromProtertyEditActivity", true);
        intent.putExtra("key", key);
        intent.putExtra("value", value);
        intent.putExtra("position", position);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String editValue = data.getStringExtra("editValue");
            String value = data.getStringExtra("value");
            int position = data.getIntExtra("position", -1);
            String key = data.getStringExtra("key");
            arrayList.set(position, key + "`" + editValue);

            if (!value.equals(editValue)) {
                editList.add(key + "`" + editValue);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // progressDialog.dismiss();

            switch (msg.what) {
                case result:
                    progressDialog.dismiss();
                    String result = (String) msg.obj;
                    Toast.makeText(AttributeEditActivity.this, result, Toast.LENGTH_SHORT).show();
                    break;
                case getRoleName:
                    createRoleNamaDlg(msg.obj);
                    break;
                case none:
                    Toast.makeText(AttributeEditActivity.this, "未做任何修改....", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void createRoleNamaDlg(Object msgObj) {

        List<String> items = Arrays.asList((String[]) msgObj);

        ListDialogFragment fragment = new ListDialogFragment("选择处理人", items);

        fragment.show(this.getSupportFragmentManager(), "1");

        fragment.setListItemClickListener(new OnListItemClickListener() {
            @Override
            public void onListItemClick(int arg2, String value) {
                reportAttEdit(value);
            }
        });
    }

    private void reportAttEdit(String roleName) {

//        if (editList.size() == 0) {
//            Toast.makeText(AttributeEditActivity.this, "未做任何修改", Toast.LENGTH_SHORT).show();
//            return;
//        }

        String newValueString = "", oldValueString = "", attrFieldString = "";

        for (int i = 0; i < editList.size(); i++) {
            String editString = editList.get(i);

            String nameString = editString.substring(0, editString.indexOf('`'));
            String valueString = editString.substring(editString.indexOf('`') + 1);

            String lastString = ",";

            if (i == editList.size() - 1) {
                lastString = "";
            }

            newValueString += valueString + lastString;
            if (graphic != null) {
                oldValueString += graphic.getAttributeValue(nameString) + lastString;
            } else if (graphicMap != null) {
                oldValueString += graphicMap.get(nameString) + lastString;
            }
            // oldValueString += graphic.getAttributeValue(nameString) + lastString;
            attrFieldString += nameString + lastString;
        }

        Map<String, String> paraMap = new HashMap<String, String>();

        paraMap.put("NewValue", newValueString);
        paraMap.put("OldValue", oldValueString);

        paraMap.put("EquipFlag", "无编号");
        if (graphic != null) {
            String EquipFlag = graphic.getAttributeValue("编号");
            if (!BaseClassUtil.isNullOrEmptyString(EquipFlag)) {
                paraMap.put("EquipFlag", String.valueOf(EquipFlag));
            }
        }
        if (graphicMap != null) {
            String EquipFlag = graphicMap.get("编号");
            if (!BaseClassUtil.isNullOrEmptyString(EquipFlag)) {
                paraMap.put("EquipFlag", String.valueOf(EquipFlag));
            }
        }
        paraMap.put("MapFlag", "无");
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

        // paraMap.put("MapFlag", String.valueOf(graphic.getAttributeValue("<图层名称>")));
        paraMap.put("Reporter", MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName);
        paraMap.put("Oper", "数据修正");
        paraMap.put("Time", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
        paraMap.put("AttrField", attrFieldString);
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

}
