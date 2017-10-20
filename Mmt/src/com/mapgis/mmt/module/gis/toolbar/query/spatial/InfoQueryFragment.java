package com.mapgis.mmt.module.gis.toolbar.query.spatial;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.SwitchImageButton;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.constant.ResultCode;
import com.zondy.mapgis.attr.FieldType;
import com.zondy.mapgis.geodatabase.SFeatureCls;
import com.zondy.mapgis.map.MapLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoQueryFragment extends Fragment {

    private List<String> wordStrings;
    private final String[] operatorStrings = {"相似", "大于", "小于", "大于等于", "小于等于", "不等于", "等于"};
    private final String[] operators = {"LIKE", ">", "<", ">=", "<=", "!=", "="};
    private final String[] choices = {"AND", "OR"};
    private String resultInfo;
    // private String timeString = "";
    private String dateString = "";

    private final Calendar c = Calendar.getInstance();

    private final List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();

    private ListView listView;

    private TextView wordsText;
    private ImageButton wordsButton;

    private TextView operatorText;
    private ImageButton operatorButton;

    private EditText conditionEditText;

    private SwitchImageButton toggleButton;// 连接符

    private Button okButton;// 确定按钮
    private Button addButton;// 添加按钮

    private String wordString;// 字段名
    private FieldType wordType;// 字段类型
    private String operatorString = operators[0];// 比较运算符

    private String choice = choices[0];// 连接符

    private ListViewAdapter adapter;
    private SFeatureCls sFeatureCls;

    private BaseActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_info_query, container, false);

        try {
            initData();

            initView(view);

            addListener();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return view;
    }

    private void initData() {

        String[] hidenFields = ("LAYER,class,OID,AuxFlag,FID1,ID,MediaFlag,LayerID,mpLayer,"
                + "NGCLS,OCLS,TCLS,ElemID,PID,AREARANGE,id2,id1,id4,id3,PATHRANGE,PATHNAME,"
                + "AREAID,PATHID,ID5,$ReturnResultString$,$ClickPoint$,emapgisoid,My_ID,emapgisid,"
                + "mpLength,mpLayer,Task_ID,GUID,FCLS,SYSLAYERNO").split(",");

        sFeatureCls = (SFeatureCls) (((MapLayer) getArguments().getSerializable("layer")).getData());
        wordStrings = new ArrayList<>();
        wordType = sFeatureCls.getFields().getField((short) 0).getFieldType();

        String name = "";

        for (short i = 0; i < sFeatureCls.getFields().getFieldCount(); i++) {
            name = sFeatureCls.getFields().getField(i).getFieldName();

            if (!Arrays.asList(hidenFields).contains(name)) {
                wordStrings.add(name);
            }
        }
        if (wordStrings.size() == 0) {
            wordStrings.add("无属性数据");
        }
        wordString = wordStrings.get(0);
    }

    private void initView(View view) {
        wordsText = (TextView) view.findViewById(R.id.wordsText);
        if (wordStrings != null && wordStrings.size() > 0) {
            wordsText.setText(wordStrings.get(0));
        }
        wordsButton = (ImageButton) view.findViewById(R.id.wordsButton);

        operatorText = (TextView) view.findViewById(R.id.operatorText);
        operatorText.setText(operatorStrings[0]);
        operatorButton = (ImageButton) view.findViewById(R.id.operatorButton);

        toggleButton = (SwitchImageButton) view.findViewById(R.id.toggleButton);

        addButton = (Button) view.findViewById(R.id.addButton);
        okButton = (Button) view.findViewById(R.id.okButton);

        listView = (ListView) view.findViewById(R.id.list);

        conditionEditText = (EditText) view.findViewById(R.id.conditionEditText);// 输入条件值
    }

    private void addListener() {

        ((View) wordsButton.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsButton.performClick();
            }
        });

        wordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment fragment = new ListDialogFragment("选取字段名", wordStrings);
                fragment.show(getActivity().getSupportFragmentManager(), "1");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        wordString = value;

                        if (TextUtils.isEmpty(wordString)) {
                            return;
                        }
                        wordsText.setText(wordString);

                        wordType = sFeatureCls.getFields().getField((short) arg2).getFieldType();

                        if (wordString.contains("时间") || wordString.contains("日期")) {
                            c.setTimeInMillis(System.currentTimeMillis());
                            int mYear = c.get(Calendar.YEAR);
                            int mMonth = c.get(Calendar.MONTH);
                            int mDay = c.get(Calendar.DAY_OF_MONTH);
                            new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    c.set(Calendar.YEAR, year);
                                    c.set(Calendar.MONTH, monthOfYear);
                                    c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    dateString = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " 00:00:00";
                                    addButton.performClick();
                                }
                            }, mYear, mMonth, mDay).show();
                        }
                    }
                });
            }
        });

        ((View) operatorButton.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                operatorButton.performClick();
            }
        });

        operatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment fragment = new ListDialogFragment("选取运算符", Arrays.asList(operatorStrings));
                fragment.show(getActivity().getSupportFragmentManager(), "2");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        operatorString = operators[arg2];
                        operatorText.setText(value);
                    }
                });
            }
        });

        // 添加信息
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getList();
                adapter = new ListViewAdapter(getActivity(), lists);
                listView.setAdapter(adapter);
                resultInfo = adapter.getInfo();
                conditionEditText.setText("");
            }
        });

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                choice = isChecked ? choices[1] : choices[0];
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (BaseClassUtil.isNullOrEmptyString(resultInfo)) {
                    activity.showToast("请选择条件然后进行查询");
                    return;
                }

                Intent intent = new Intent();
                intent.putExtras(getArguments()).putExtra("where", resultInfo);
                activity.setResult(ResultCode.RESULT_WHERE_FETCHED, intent);
                activity.finish();
            }
        });
    }

    // 更新lists
    private void getList() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("word", wordString);
        map.put("operator", operatorString);
        // 日期选项
        if (wordString.equals("录入时间") || wordString.equals("埋设日期")) {
            map.put("condition", dateString);
            dateString = "";
            // timeString = "";
        } else {
            map.put("condition", conditionEditText.getText() + "");
        }
        map.put("wordType", wordType);
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

        public ListViewAdapter(List<Map<String, Object>> lists) {
            super();
            this.lists = lists;
        }

        public ListViewAdapter(Context context, List<Map<String, Object>> lists) {
            inflater = LayoutInflater.from(context);
            this.lists = lists;
        }

        @Override
        public int getCount() {
            int size = lists.size();
            return size;
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
                convertView = inflater.inflate(R.layout.activity_info_result, null);
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
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lists.remove(position);
                    ListViewAdapter.this.notifyDataSetChanged();
                    resultInfo = getInfo();
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
            resultInfo = "";

            if (lists == null || lists.size() == 0) {
                return resultInfo;
            }

            for (Map<String, Object> kv : lists) {
                FieldType fieldType = (FieldType) kv.get("wordType");

                if (fieldType != null) {
                    int value = fieldType.value();

                    if (value == FieldType.fldStr.value() || kv.get("operator").equals("LIKE")) {
                        if ("=".equals(kv.get("operator").toString().trim())) {
                            resultInfo += kv.get("word") + " = " + "'" + kv.get("condition") + "' " + kv.get("choice") + " ";
                        } else {
                            resultInfo += kv.get("word") + " LIKE " + "'%" + kv.get("condition") + "%' " + kv.get("choice") + " ";
                        }
                    } else if (value == FieldType.fldDate.value() || value == FieldType.fldTime.value()
                            || value == FieldType.fldTimeStamp.value()) {
                        resultInfo += kv.get("word") + " " + kv.get("operator") + " '" + kv.get("condition") + "' " + kv.get("choice")
                                + " ";
                    } else {
                        resultInfo += kv.get("word") + " " + kv.get("operator") + " " + kv.get("condition") + " " + kv.get("choice")
                                + " ";
                    }
                } else {
                    resultInfo += kv.get("word") + " = " + kv.get("condition") + " " + kv.get("choice") + " ";
                }
            }

            if (resultInfo.length() > 0) {
                resultInfo = resultInfo.substring(0, resultInfo.endsWith("AND ") ? resultInfo.length() - 4 : resultInfo.length() - 3);
            }

            return resultInfo;
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