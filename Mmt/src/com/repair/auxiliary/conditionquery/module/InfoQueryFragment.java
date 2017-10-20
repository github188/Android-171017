package com.repair.auxiliary.conditionquery.module;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.SwitchImageButton;
import com.mapgis.mmt.common.widget.fragment.FastSearch_ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.constant.ResultCode;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.FldValuesResult;
import com.mapgis.mmt.R;
import com.zondy.mapgis.attr.FieldType;
import com.zondy.mapgis.geodatabase.SFeatureCls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyunfan on 2016/5/20.
 */
public class InfoQueryFragment extends Fragment {
    protected List<String> wordStrings;
    protected final String[] operatorStrings = {"相似", "大于", "小于", "大于等于", "小于等于", "不等于", "等于"};
    protected final String[] operators = {"LIKE", ">", "<", ">=", "<=", "!=", "="};
    protected final String[] choices = {"AND", "OR"};
    protected String resultInfo;
    // private String timeString = "";
    protected String dateString = "";

    protected final Calendar c = Calendar.getInstance();

    protected final List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();

    protected ListView listView;

    protected TextView wordsText;
    protected ImageButton wordsButton;

    protected TextView operatorText;
    protected ImageButton operatorButton;

    protected EditText conditionEditText;
    protected ImageButton conditionEditTextoperatorButton;

    protected SwitchImageButton toggleButton;// 连接符

    protected Button okButton;// 确定按钮
    protected Button addButton;// 添加按钮

    protected String wordString;// 字段名
    protected FieldType wordType;// 字段类型
    protected String operatorString = operators[0];// 比较运算符

    protected String choice = choices[0];// 连接符

    protected ListViewAdapter adapter;
    protected SFeatureCls sFeatureCls;

    //条件枚举值
    protected FldValuesResult fldValuesResult = new FldValuesResult();
    protected String layerID;
    protected String auxTabName;
    protected String Envelope;

    protected BaseActivity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_info_query_aux, container, false);
        context = (BaseActivity) getActivity();
        initView(view);
        addListener();
        try {
            Intent intent = context.getIntent();
            Object wordStringstemp = intent.getSerializableExtra("wordStrings");
            if (wordStringstemp == null) {
                return view;
            }
            wordStrings = Arrays.asList((String[]) wordStringstemp);
            if (wordStrings != null && wordStrings.size() > 0) {
                wordString = wordStrings.get(0);
            }
            layerID = intent.getStringExtra("layerID");
            auxTabName = intent.getStringExtra("auxTabName");
            Envelope = intent.getStringExtra("Envelope");

            if (!TextUtils.isEmpty(wordString)) {
                fldValuesResult.getFldValuesResultFromServer(getActivity(), Envelope, layerID, auxTabName, wordString, new AuxUtils.AfterOnsucess() {
                    @Override
                    public void afterSucess() {

                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return view;
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
        conditionEditTextoperatorButton = (ImageButton) view.findViewById(R.id.conditionEditTextoperatorButton);

    }

    protected void addListener() {

        ((View) wordsButton.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsButton.performClick();
            }
        });
        conditionEditTextoperatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fldValuesResult.rtnlist.size() == 0) {
                    Toast.makeText(context, "没有可选的" + wordString + "枚举值", Toast.LENGTH_SHORT).show();
                    return;
                }
                //枚举值选择
                FastSearch_ListDialogFragment fragment = new FastSearch_ListDialogFragment(wordString, fldValuesResult.rtnlist);
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        conditionEditText.setText(value);
                    }
                });
                fragment.show(context.getSupportFragmentManager(), "");
            }
        });
        wordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wordStrings == null || wordStrings.size() == 0) {
                    context.showErrorMsg("没有可选的字段");
                    return;
                }
                ListDialogFragment fragment = new ListDialogFragment("选取字段名", wordStrings);
                fragment.show(getActivity().getSupportFragmentManager(), "1");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        wordString = value;

                        wordsText.setText(wordString);
                        fldValuesResult.rtnlist.clear();

//                            if (sFeatureCls != null) {
//                                wordType = sFeatureCls.getFields().getField((short) arg2).getFieldType();
//                            }


                        fldValuesResult.getFldValuesResultFromServer(getActivity(), Envelope, layerID, auxTabName, wordString, new AuxUtils.AfterOnsucess() {
                            @Override
                            public void afterSucess() {

                            }
                        });
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
                if (TextUtils.isEmpty(wordString)) {
                    context.showToast("请先选择字段名");
                    return;
                }
                if (TextUtils.isEmpty(operatorString)) {
                    context.showToast("请先选择运算符");
                    return;
                }
                if (TextUtils.isEmpty(conditionEditText.getText().toString())) {
                    context.showToast("请先输入条件值");
                    return;
                }
                getList();
                adapter = new ListViewAdapter(context, lists);
                listView.setAdapter(adapter);
                resultInfo = adapter.getInfo();
                conditionEditText.setText("");
            }
        });

        conditionEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (wordString.contains("时间") || wordString.contains("日期")) {
                        c.setTimeInMillis(System.currentTimeMillis());
                        int mYear = c.get(Calendar.YEAR);
                        int mMonth = c.get(Calendar.MONTH);
                        int mDay = c.get(Calendar.DAY_OF_MONTH);
                        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                c.set(Calendar.YEAR, year);
                                c.set(Calendar.MONTH, monthOfYear);
                                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                //   dateString = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " 00:00:00";
                                conditionEditText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth + " 00:00:00");
                            }
                        }, mYear, mMonth, mDay).show();
                    }
                }
                return false;
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
                    context.showToast("请选择条件然后进行查询");
                    return;
                }

                context.setResult(ResultCode.RESULT_WHERE_FETCHED, context.getIntent().putExtra("where", resultInfo));
                context.finish();
            }
        });
    }

    // 更新lists
    protected void getList() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("word", wordString);
        map.put("operator", operatorString);
        map.put("condition", conditionEditText.getText() + "");
        map.put("choice", choice);
        lists.add(map);
    }

    /**
     * 适配器
     */
    protected class ListViewAdapter extends BaseAdapter {

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
            //  List<String> conditions = new ArrayList<>();
            for (Map<String, Object> kv : lists) {
                if (kv.get("operator").equals("LIKE")) {
                    resultInfo += " (" + kv.get("word") + " LIKE  '%" + kv.get("condition") + "%') " + kv.get("choice") + " ";
                } else {
                    resultInfo += " (" + kv.get("word") + " " + kv.get("operator") + " '" + kv.get("condition") + "') " + kv.get("choice") + " ";
                }
            }
//                if (conditions.size() > 0) {
//                    resultInfo = TextUtils.join(" AND ", conditions.toArray(new String[conditions.size()]));
//                }
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
