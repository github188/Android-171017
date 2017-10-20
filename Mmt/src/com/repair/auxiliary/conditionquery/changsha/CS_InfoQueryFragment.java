package com.repair.auxiliary.conditionquery.changsha;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.FastSearch_ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.repair.auxiliary.conditionquery.module.InfoQueryFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by liuyunfan on 2016/5/20.
 */
public class CS_InfoQueryFragment extends InfoQueryFragment {

    private List<String> layerIDs = new ArrayList<>();

    public void setData(List<String> wordStrings, String layerID, String auxTabName, String Envelope) {
        this.wordStrings = wordStrings;
        this.layerID = layerID;
        this.auxTabName = auxTabName;
        this.Envelope = Envelope;
    }

    public void setLayerIDs(List<String> layerIDs) {
        this.layerIDs = layerIDs;
    }

    @Override
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
                    Toast.makeText(context, "条件未加载完成或加载错误", Toast.LENGTH_SHORT).show();
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
                    if (TextUtils.isEmpty(wordString)) {
                        return false;
                    }
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

                Intent intent = new Intent(context, CS_ConditionQueryAuxListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("layerID", layerID);
                bundle.putString("geometry", "");
                bundle.putString("strCon", resultInfo);
                bundle.putString("strAuxTableName", auxTabName);
                intent.putExtra("bundle", bundle);
                intent.putExtra("layerIDs", layerIDs.toArray(new String[layerIDs.size()]));
                startActivity(intent);

            }
        });
    }
}
