package com.mapgis.mmt.module.gis.toolbar.online.query.spatial;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.MmtProgressDialog;
import com.mapgis.mmt.common.widget.customview.SwitchImageButton;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo.OnlineLayerAttribute;
import com.mapgis.mmt.module.gis.toolbar.online.query.OnlineQueryService;
import com.repair.gisdatagather.enn.getgisdataTask.GetLayerAttributeTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineWhereQueyActivity extends BaseActivity {
    private OnlineLayerInfo onlineLayer;
    private OnlineLayerAttribute selectedAttribute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseTextView().setText("高级查询");

        new GetLayerAttributeTask().executeOnExecutor(MyApplication.executorService, getIntent().getStringExtra("layer"));
    }

    class OnlineWhereQueyFragment extends Fragment {
        private List<String> wordStrings;

        private final String[] operatorStrings = {"等于", "大于", "小于", "大于等于", "小于等于", "不等于", "相似"};
        private final String[] operators = {"=", ">", "<", ">=", "<=", "!=", "LIKE"};
        private final String[] choices = {"AND", "OR"};

        private String strWhere;
        private String strDate = "";

        private final Calendar c = Calendar.getInstance();

        private final List<Map<String, Object>> lists = new ArrayList<>();

        private ListView listView;

        private TextView wordsText;
        private ImageButton wordsButton;

        private TextView operatorText;
        private ImageButton operatorButton;

        private EditText conditionEditText;

        private SwitchImageButton toggleButton;// 连接符

        private Button okButton;// 确定按钮
        private Button addButton;// 添加按钮

        private String operatorString = operators[0];// 比较运算符

        private String choice = choices[0];// 连接符

        private ListViewAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.activity_info_query, container, false);

            initData();

            initView(view);

            addListener();

            return view;
        }

        private void initData() {
            wordStrings = onlineLayer.getFieldsNames();
        }

        private void initView(View view) {
            wordsText = (TextView) view.findViewById(R.id.wordsText);
            wordsButton = (ImageButton) view.findViewById(R.id.wordsButton);

            operatorText = (TextView) view.findViewById(R.id.operatorText);
            operatorButton = (ImageButton) view.findViewById(R.id.operatorButton);

            toggleButton = (SwitchImageButton) view.findViewById(R.id.toggleButton);

            addButton = (Button) view.findViewById(R.id.addButton);
            okButton = (Button) view.findViewById(R.id.okButton);

            listView = (ListView) view.findViewById(R.id.list);

            conditionEditText = (EditText) view.findViewById(R.id.conditionEditText);// 输入条件值
        }

        private void addListener() {

            ((View) wordsButton.getParent()).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    wordsButton.performClick();
                }
            });

            wordsButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = new ListDialogFragment("选取字段名", wordStrings);
                    fragment.show(getActivity().getSupportFragmentManager(), "1");
                    fragment.setListItemClickListener(new OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            selectedAttribute = onlineLayer.fields[arg2];

                            wordsText.setText(value);

                            if (selectedAttribute.type.equals("TIMESTAMP_TYPE")) {
                                c.setTimeInMillis(System.currentTimeMillis());
                                int mYear = c.get(Calendar.YEAR);
                                int mMonth = c.get(Calendar.MONTH);
                                int mDay = c.get(Calendar.DAY_OF_MONTH);
                                new DatePickerDialog(OnlineWhereQueyActivity.this, new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        c.set(Calendar.YEAR, year);
                                        c.set(Calendar.MONTH, monthOfYear);
                                        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                        strDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " 00:00:00";
                                        addButton.performClick();
                                    }
                                }, mYear, mMonth, mDay).show();
                            }
                        }
                    });
                }
            });

            ((View) operatorButton.getParent()).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    operatorButton.performClick();
                }
            });

            operatorButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = new ListDialogFragment("选取运算符", Arrays.asList(operatorStrings));
                    fragment.show(getActivity().getSupportFragmentManager(), "2");
                    fragment.setListItemClickListener(new OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            operatorString = operators[arg2];
                            operatorText.setText(operatorString);
                        }
                    });
                }
            });

            // 添加信息
            addButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getList();
                    adapter = new ListViewAdapter(OnlineWhereQueyActivity.this, lists);
                    listView.setAdapter(adapter);
                    strWhere = adapter.getInfo();
                    conditionEditText.setText("");
                }
            });

            toggleButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!toggleButton.isChecked()) {
                        toggleButton.setChecked(true);
                        choice = choices[1];
                    } else {
                        toggleButton.setChecked(false);
                        choice = choices[0];
                    }
                }
            });

            okButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (BaseClassUtil.isNullOrEmptyString(strWhere)) {
                        showToast("请选择条件然后进行查询");
                        return;
                    }

                    setResult(ResultCode.RESULT_WHERE_FETCHED,
                            getIntent().putExtra(getString(R.string.online_query_where), strWhere)
                                    .putExtra(getString(R.string.online_query_layername), getIntent().getStringExtra("layer")));
                    finish();
                }
            });
        }

        // 更新lists
        private void getList() {
            Map<String, Object> map = new HashMap<>();

            if (selectedAttribute == null) {
                Toast.makeText(OnlineWhereQueyActivity.this, "查询条件不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            map.put("type", selectedAttribute.type);

            map.put("word", selectedAttribute.name);
            map.put("operator", operatorString);

            // 日期选项
            if (selectedAttribute.type.equals("TIMESTAMP_TYPE")) {
                map.put("condition", strDate);
                strDate = "";
            } else {
                map.put("condition", conditionEditText.getText().toString());
            }

            map.put("choice", choice);

            lists.add(map);
        }

        /**
         * 适配器
         */
        class ListViewAdapter extends BaseAdapter {

            private ViewHolder holder = null;

            private LayoutInflater inflater;
            private final List<Map<String, Object>> lists;

            public ListViewAdapter(Context context, List<Map<String, Object>> lists) {
                inflater = LayoutInflater.from(context);
                this.lists = lists;
            }

            @Override
            public int getCount() {
                return lists.size();
            }

            @Override
            public Object getItem(int position) {
                return lists.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    holder = new ViewHolder();

                    convertView = inflater.inflate(R.layout.activity_info_result, parent, false);

                    holder.button = (ImageButton) convertView.findViewById(R.id.deleteButton);
                    holder.wordTextView = (TextView) convertView.findViewById(R.id.wordsTextView);
                    holder.operatorTextView = (TextView) convertView.findViewById(R.id.operatorTextView);
                    holder.conditonTextView = (TextView) convertView.findViewById(R.id.conditionTextView);
                    holder.choiceTextView = (TextView) convertView.findViewById(R.id.choiceTextView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                // 删除按钮
                holder.button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lists.remove(position);
                        ListViewAdapter.this.notifyDataSetChanged();
                        strWhere = getInfo();
                    }
                });

                holder.wordTextView.setText(String.valueOf(lists.get(position).get("word")));
                holder.operatorTextView.setText(String.valueOf(lists.get(position).get("operator")));
                holder.conditonTextView.setText(String.valueOf(lists.get(position).get("condition")));
                holder.choiceTextView.setText(String.valueOf(lists.get(position).get("choice").equals("AND") ? "且" : "或"));
                return convertView;
            }

            // 获取list中的信息
            public String getInfo() {
                strWhere = "";

                if (lists == null || lists.size() == 0) {
                    return strWhere;
                }

                for (Map<String, Object> kv : lists) {

                    String key = kv.get("word").toString();
                    String operator = kv.get("operator").toString();
                    String value = kv.get("condition").toString();
                    String link = kv.get("choice").toString();

                    String fieldType = kv.get("type").toString();
                    //客户纠结当选＝号时，得到的是like查询的结果
//                    if (!TextUtils.isEmpty(fieldType)) {
//                        if (fieldType.equals("STR_TYPE") || operator.equals("LIKE")) {
//                            strWhere += key + " LIKE '%" + value + "%'";
//                        } else if (fieldType.equals("TIMESTAMP_TYPE")) {
//                            strWhere += key + " " + operator + " '" + value + "'";
//                        } else {
//                            strWhere += key + " " + operator + " " + value;
//                        }
//                    } else {
//                        strWhere += key + " " + operator + " '" + value + "'";
//                    }
                    if (!TextUtils.isEmpty(fieldType)) {
                        if (operator.equals("LIKE")) {
                            strWhere += key + " LIKE '%" + value + "%'";
                        } else if (fieldType.equals("TIMESTAMP_TYPE")||fieldType.equals("STR_TYPE")) {
                            strWhere += key + " " + operator + " '" + value + "'";
                        } else {
                            strWhere += key + " " + operator + " " + value;
                        }
                    } else {
                        strWhere += key + " " + operator + " '" + value + "'";
                    }

                    strWhere += " " + link + " ";
                }

                if (strWhere.length() > 0) {
                    strWhere = strWhere.substring(0,
                            strWhere.endsWith("AND ") ? strWhere.length() - 4 : strWhere.length() - 3);
                }

                return strWhere;
            }

            final class ViewHolder {
                public ImageButton button;
                public TextView wordTextView;
                public TextView operatorTextView;
                public TextView conditonTextView;
                public TextView choiceTextView;
            }
        }

    }

    class GetLayerAttributeTask extends AsyncTask<String, String, String> {
        private ProgressDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            loadingDialog = MmtProgressDialog.getLoadingProgressDialog(OnlineWhereQueyActivity.this, "正在加载信息");
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String url = OnlineQueryService.getLayerAttributeService(params[0]);

            return NetUtil.executeHttpGet(url, "f", "json");
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                if (result == null || result.length() == 0) {
                    showToast("未查询到信息!");
                    return;
                }

                onlineLayer = new Gson().fromJson(result, OnlineLayerInfo.class);

                OnlineWhereQueyFragment fragment = new OnlineWhereQueyFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.baseFragment, fragment);
                ft.show(fragment);
                ft.commit();
            } catch (Exception e) {
                showToast("查询信息异常!");

                e.printStackTrace();
            } finally {
                loadingDialog.cancel();

            }
        }
    }
}