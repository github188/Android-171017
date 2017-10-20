package com.repair.shaoxin.water.gis;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.gis.investigation.ProtertyEditDialog;
import com.mapgis.mmt.global.MmtBaseTask;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class AttributeEditActivityForSX extends BaseActivity implements OnClickListener {
    protected ArrayList<String> arrayList;
    private final ArrayList<String> editList = new ArrayList<>();
    protected HashMap<String, String> graphicMap;

    protected EmsPipeDetailActivityAdapter adapter;
    private PhotoFragment photoFragment;

    @Override
    protected void setDefaultContentView() {
        try {
            setContentView(R.layout.attr_edit_sx);

            defaultBackBtn = findViewById(R.id.baseActionBarImageView);

            defaultBackBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCustomBack();
                }
            });

            getBaseTextView().setText("属性确认");

            findViewById(R.id.btnMakeSure).setOnClickListener(this);
            findViewById(R.id.btnFinish).setOnClickListener(this);

            photoFragment = new PhotoFragment.Builder(BaseClassUtil.getPhotoDir("属性编辑")).build();
            getSupportFragmentManager().beginTransaction().replace(R.id.layoutPhoto, photoFragment).commit();

            arrayList = new ArrayList<>();

            graphicMap = (HashMap<String, String>) getIntent().getSerializableExtra("graphicMap");

            for (String key : graphicMap.keySet()) {
                String value = graphicMap.get(key);

                if ("emapgisid".equalsIgnoreCase(key)) {
                    ((TextView) findViewById(R.id.detail_title)).setText(MessageFormat.format("{0}:{1}", key, value));
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

            adapter = new EmsPipeDetailActivityAdapter(this, arrayList);

            ((ListView) findViewById(R.id.ListView_asset_detail)).setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnMakeSure) {
            new MmtBaseTask<String, Integer, InsertRnt>(this) {

                @Override
                protected InsertRnt doInBackground(String... params) {
                    try {
                        ArrayList<String> args = new ArrayList<>();

                        args.add("EquipFlag");
                        args.add(getIntent().getStringExtra("pipeNo"));

                        args.add("MapFlag");
                        args.add(getIntent().getStringExtra("layerName"));

                        args.add("Reporter");
                        args.add(MyApplication.getInstance().getUserBean().LoginName);

                        args.add("Oper");
                        args.add("确认无误");

                        args.add("Time");
                        args.add(BaseClassUtil.getSystemTime());

                        args.add("NewValue");
                        args.add("");

                        args.add("OldValue");
                        args.add("");

                        args.add("AttrField");
                        args.add("");

                        args.add("AuditUser");
                        args.add("");

                        args.add("EventID");
                        args.add("-1");

                        String url = ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/zondy_mapgiscitysvr_audit/REST/auditrest.svc/InsertAttr";

                        String json = NetUtil.executeHttpGet(url, args.toArray(new String[args.size()]));

                        if (TextUtils.isEmpty(json))
                            return null;

                        return new Gson().fromJson(json, InsertRnt.class);
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        return null;
                    }
                }

                @Override
                protected void onSuccess(InsertRnt data) {
                    try {
                        String msg = "确认无误";

                        msg += (data == null || !data.IsSuccess) ? "失败" : "成功";

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.mmtExecute();
        } else if (v.getId() == R.id.btnFinish) {
            if (editList.size() == 0) {
                Toast.makeText(AttributeEditActivityForSX.this, "未做任何修改",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            new ReportTask(AttributeEditActivityForSX.this).mmtExecute();
        }
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
                convertView = mInflater.inflate(R.layout.asset_detail_props_item, parent, false);

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
        Intent intent = new Intent(AttributeEditActivityForSX.this, ProtertyEditDialog.class);

        intent.putExtra("isFromProtertyEditActivity", true);
        intent.putExtra("key", key);
        intent.putExtra("value", value);
        intent.putExtra("position", position);

        startActivityForResult(intent, 888);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 888) {
            if (resultCode != RESULT_OK)
                return;

            String editValue = data.getStringExtra("editValue");
            String value = data.getStringExtra("value");

            int position = data.getIntExtra("position", -1);

            String key = data.getStringExtra("key");

            arrayList.set(position, key + "`" + editValue);

            if (!value.equals(editValue)) {
                editList.add(key + "`" + editValue);
            }

            adapter.notifyDataSetChanged();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    class ReportTask extends MmtBaseTask<ReportInBackEntity, String, ResultWithoutData> {
        public ReportTask(Context context) {
            super(context);
        }

        @Override
        protected ResultWithoutData doInBackground(ReportInBackEntity... params) {
            try {
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

                    oldValueString += graphicMap.get(nameString) + lastString;

                    attrFieldString += nameString + lastString;
                }

                AttEditInfo info = new AttEditInfo();

                info.NewValue = newValueString;
                info.OldValue = oldValueString;

                info.EquipFlag = getIntent().getStringExtra("pipeNo");
                info.MapFlag = getIntent().getStringExtra("layerName");

                info.Reporter = MyApplication.getInstance().getUserBean().TrueName;
                info.Oper = "数据修正";

                String time = BaseClassUtil.getSystemTime();

                info.Time = time;
                info.AttrField = attrFieldString;

                info.AuditUser = MyApplication.getInstance().getConfigValue("roleName");
                info.EventID = -1;

                int userID = MyApplication.getInstance().getUserId();

                info.UserID = userID;
                info.RoadName = graphicMap.get("位置");
                info.Position = getIntent().getStringExtra("xy");
                info.Images = photoFragment.getRelativePhoto();

                String url = ServerConnectConfig.getInstance().getBaseServerPath()
                        + "/Services/CitySvr_Biz_SXGS/REST/BizSXGSRest.svc/InsertAttr";

                ReportInBackEntity entity = new ReportInBackEntity(new Gson().toJson(info), userID, ReportInBackEntity.REPORTING,
                        url, time, "属性编辑", photoFragment.getAbsolutePhoto(), photoFragment.getRelativePhoto());
                return entity.report(this);
            } catch (Exception ex) {
                ex.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onSuccess(ResultWithoutData data) {
            if (data != null) {
                Toast.makeText(context, data.ResultMessage, Toast.LENGTH_SHORT).show();

                if (data.ResultCode > 0)
                    onBackPressed();
            } else
                Toast.makeText(context, "属性编辑上报失败", Toast.LENGTH_SHORT).show();
        }
    }
}