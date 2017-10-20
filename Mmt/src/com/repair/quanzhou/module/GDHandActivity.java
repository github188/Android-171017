package com.repair.quanzhou.module;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.customform.view.MmtAttachmentView;
import com.google.gson.Gson;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.BottomUnitView;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.customview.ImageRadioButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.quanzhou.entity.DealPerson;
import com.repair.quanzhou.entity.FeedBack;
import com.repair.quanzhou.entity.MalfunctionType;
import com.repair.quanzhou.entity.QZResultData;
import com.repair.quanzhou.entity.ReportInfo;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by liuyunfan on 2016/1/21.
 */
public class GDHandActivity extends BaseActivity {
    private static QZResultData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleAndClear("工单处理");
        data = new Gson().fromJson(getIntent().getStringExtra("data"), QZResultData.class);
        addFragment(new GDHandFragment());

    }

    public static class GDHandFragment extends Fragment {
        private BaseActivity context;
        private LinearLayout parentLayout;
        private HashMap<String, String> localeDealPersonDic = new HashMap<>();
        private HashMap<String, String> DealPersonTelDic = new HashMap<>();
        private HashMap<String, String> BugResonsDic = new HashMap<>();
        private HashMap<String, Integer> feedBackDic = new HashMap<>();
        //3:工单处理，4：工单结束
        private int handType = 3;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            context = (BaseActivity) getActivity();
            ScrollView scrollView = new ScrollView(context);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentLayout = new LinearLayout(context);
            parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            parentLayout.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(parentLayout);

            createView(parentLayout);
            return scrollView;
        }

        public void createView(LinearLayout parentLayout) {

            final GDControl resultText1 = new GDControl("operateType", "工单类别：", "下拉框", "工单处理", "工单处理,工单结束");
            // final GDControl resultText1 = new GDControl("operateType", "工单类别：", "下拉框", "工单处理", "工单处理");

            final ImageButtonView resultTextView1 = (ImageButtonView) resultText1.createView(context);
            parentLayout.addView(resultTextView1);

            GDControl resultText101 = new GDControl("isCompleteConstruct", "是否处理好：", "平铺值选择器", "是", "是,否");
            resultText101.ConfigInfo = "是,否";
            final ImageRadioButtonView resultTextView101 = (ImageRadioButtonView) resultText101.createView(context);
            resultTextView101.setVisibility(View.GONE);
            parentLayout.addView(resultTextView101);

            GDControl resultText102 = new GDControl("malfunctionTypeId", "故障主要原因：", "下拉框", "", getBugResons());
            final ImageButtonView resultTextView102 = (ImageButtonView) resultText102.createView(context);
            resultTextView102.setVisibility(View.GONE);
            parentLayout.addView(resultTextView102);


            resultTextView1.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListDialogFragment fragment = new ListDialogFragment(
                            resultText1.DisplayName, Arrays.asList(resultText1.DefaultValues.split(",")));
                    fragment.show(context.getSupportFragmentManager(), "");
                    fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            resultTextView1.setValue(value);
                            if (value.equals("工单处理")) {
                                handType = 3;
                                resultTextView101.setVisibility(View.GONE);
                                resultTextView102.setVisibility(View.GONE);
                            } else {
                                handType = 4;
                                resultTextView101.setVisibility(View.VISIBLE);
                                resultTextView102.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            });


            StringBuffer names = new StringBuffer(), tels = new StringBuffer();
            getDefaultHandManValues(names, tels);
            final GDControl resultText2 = new GDControl("localeDealPerson", "现场处理人：", "下拉框", "", names.length() > 0 ? names.substring(0, names.length() - 1) : "");
            resultText2.Validate = "1";
            View resultTextView2 = resultText2.createView(context);
            parentLayout.addView(resultTextView2);

            GDControl resultText3 = new GDControl("localeDealPersonTel", "移动电话：", "联动框", "", tels.length() > 0 ? tels.substring(0, tels.length() - 1) : "");
            final ImageButtonView resultTextView3 = (ImageButtonView) resultText3.createView(context);
            parentLayout.addView(resultTextView3);

            if (resultTextView2 instanceof ImageButtonView) {
                final ImageButtonView resultTextView2ibv = (ImageButtonView) resultTextView2;
                resultTextView2ibv.getButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListDialogFragment fragment = new ListDialogFragment(
                                resultText2.DisplayName, Arrays.asList(resultText2.DefaultValues.split(",")));
                        fragment.show(context.getSupportFragmentManager(), "");
                        fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                            @Override
                            public void onListItemClick(int arg2, String value) {
                                resultTextView2ibv.setValue(value);
                                resultTextView3.setValue(DealPersonTelDic.get(value));
                            }
                        });
                    }
                });
            }


            GDControl resultText5 = new GDControl("localeDealTime", "处理时间：", "日期框", "");
            View resultTextView5 = resultText5.createView(context);
            parentLayout.addView(resultTextView5);

            GDControl resultText6 = new GDControl("endLocaleDealTime", "结束时间：", "日期框", "");
            View resultTextView6 = resultText6.createView(context);
            parentLayout.addView(resultTextView6);

            GDControl resultText7 = new GDControl("seed", "事件原由：", "长文本", "");
            View resultTextView7 = resultText7.createView(context);
            parentLayout.addView(resultTextView7);

            GDControl resultText8 = new GDControl("memo", "处理过程：", "长文本", "");
            View resultTextView8 = resultText8.createView(context);
            parentLayout.addView(resultTextView8);

            GDControl resultText9 = new GDControl("stateMemo", "处理结果：", "长文本", "");
            View resultTextView9 = resultText9.createView(context);
            parentLayout.addView(resultTextView9);

            GDControl resultText10 = new GDControl("isDealRerutn", "用户回单：", "平铺值选择器", "有", "有,无");
            resultText10.Validate = "1";
            resultText10.ConfigInfo = "有,无";
            ImageRadioButtonView resultTextView10 = (ImageRadioButtonView) resultText10.createView(context);
            parentLayout.addView(resultTextView10);

            String feedbacks = getFeedBacks();
            GDControl resultText11 = new GDControl("feedBackStateId", "用户意见：", "下拉框", "", feedbacks);
            resultText11.Validate = "1";
            resultText11.ConfigInfo = feedbacks;
            final View resultTextView11 = resultText11.createView(context);
            parentLayout.addView(resultTextView11);

            GDControl resultText13 = new GDControl("feedBackMemo", "无回单原因：", "长文本", "");
            resultText13.Validate = "1";
            final View resultTextView13 = resultText13.createView(context);
            resultTextView13.setVisibility(View.GONE);
            parentLayout.addView(resultTextView13);

            resultTextView10.getRadioGroup().setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup getRadioGroup, int checkedId) {
                    RadioButton rb = (RadioButton) getRadioGroup.findViewById(checkedId);
                    if (rb.getText().equals("有")) {
                        resultTextView13.setVisibility(View.GONE);
                        resultTextView11.setVisibility(View.VISIBLE);
                    } else {
                        resultTextView13.setVisibility(View.VISIBLE);
                        resultTextView11.setVisibility(View.GONE);
                    }
                }
            });

            GDControl control = new GDControl("file", "现场照片：", "拍照", "");

            control.setAddEnable(true);
            control.canSelect = true;

            View resultTextView12 = new MmtAttachmentView(context, control).build();

            parentLayout.addView(resultTextView12);

            BottomUnitView bottomUnitView1 = new BottomUnitView(context);
            bottomUnitView1.setBackgroundResource(R.drawable.button_radius);
            bottomUnitView1.setContent("回单");
            bottomUnitView1.textView.setTextColor(Color.WHITE);
            bottomUnitView1.textView.setTextSize(25);
            context.addBottomUnitView(bottomUnitView1, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handGD();
                }
            });
        }

        public void handGD() {
            final ReportInfo ri = new ReportInfo();
            if (setReportInfoValue(ri)) {
                setDefaultValues(ri);
                new MmtBaseTask<Void, String, ResultData<Integer>>(context) {
                    @Override
                    protected ResultData<Integer> doInBackground(Void... params) {
                        try {
                            List<String> absolutePaths = BaseClassUtil.StringToList(ri.file, ",");
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < absolutePaths.size(); i++) {
                                String s = absolutePaths.get(i);
                                sb.append(new File(s).getName());
                                if (i != absolutePaths.size()) {
                                    sb.append(",");
                                }
                            }

                            String data = new Gson().toJson(ri);

                            String url = ServerConnectConfig.getInstance().getBaseServerPath();
//                            String url = "http://192.168.12.133:8888/cityinterface";
                            url += "/Services/CitySvr_Biz_QZ_HotLine/REST/BizQZHotLineRest.svc/UploadHotLineWorkTask";

                            ReportInBackEntity backEntity = new ReportInBackEntity(data, MyApplication.getInstance().getUserId(),
                                    ReportInBackEntity.REPORTING, url, UUID.randomUUID().toString(), "热线任务", ri.file,
                                    sb.toString());


                            // 外网服务
//                            List<String> paramList = new ArrayList<String>();
//                            Map<String, File> files = new HashMap<String, File>();
//                            ri.setValues2Params(paramList, files);
//                            String url2 = Utils.baseUrl + "/workTaskDeal.do?state=phoneDeal";
//                            String s = NetUtil.executeMultipartHttpPost(url2, paramList, files, "gbk", "", null);
//                            System.out.println("hotline" + s);

                            return backEntity.report(this);

                        } catch (Exception ex) {
                            return null;
                        }
                    }

                    @Override
                    protected void onSuccess(ResultData<Integer> result) {
                        super.onSuccess(result);
                        try {
                            if (result == null) {
                                return;
                            }
                            System.out.println("HotLine:  " + result.ResultMessage);
                            if (result.ResultCode > 0 && result.ResultMessage.contains("true")) {
                                Toast.makeText(context, "处理成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "处理错误", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
//                        if (BaseClassUtil.isNullOrEmptyString(s)) {
//                            Toast.makeText(context, "网络错误", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                        QZResultWithoutData data = new Gson().fromJson(s, QZResultWithoutData.class);
//                        if (data == null || !data.success) {
//                            Toast.makeText(context, data == null ? "处理错误" : data.msg, Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                        Toast.makeText(context, "处理成功", Toast.LENGTH_LONG).show();
                    }
                }.executeOnExecutor(MyApplication.executorService);
            }

        }

        public void setDefaultValues(ReportInfo ri) {
            ri.workTaskSeq = data.workTaskBean.workTaskSeq;
            ri.operateType = handType;
            ri.inputWorkerID = data.workTaskBean.workerID;
        }

        /**
         * 遍历视图赋值
         *
         * @param ri
         * @return
         */
        public boolean setReportInfoValue(ReportInfo ri) {
            boolean isNoBackReason = true;
            // 遍历所有视图
            for (int i = 0; i < parentLayout.getChildCount(); i++) {

                View view = parentLayout.getChildAt(i);

                // 若不是需要反馈的视图，则继续循环
                if (!(view instanceof FeedBackView)) {
                    continue;
                }

                GDControl control = (GDControl) view.getTag();

                // 类型为 保留字 的反馈视图不需要反馈
                if (control.Type.equals("保留字")) {
                    continue;
                }

                //  FeedItem item = new FeedItem();

                FeedBackView feedBackView = (FeedBackView) view;

                // 对应服务器端数据库所要存储的表的列名
                String name = control.Name;

                // 用户所填写的值
                String value = feedBackView.getValue();

                // 若反馈视图是Fragment的类型
                if (view instanceof ImageFragmentView) {

                    ImageFragmentView fragmentView = (ImageFragmentView) view;

                    HashMap<String, String> dataMap = fragmentView.getKeyValue();

                    // 这里取绝对路径
                    value = dataMap.get(ImageFragmentView.ABSOLUTE_KEY_STRING);
                }

                if (control.Name.equals("isDealRerutn") && control.Value.equals("有")) {
                    // 用户回单
                    isNoBackReason = false;
                }
                // 判断是否为必填项，若是并且没有填写，则给出提示
                // 是上报状态并且是必填写项并且未填写任何信息，给出提示
                if (control.Validate.equals("1") && BaseClassUtil.isNullOrEmptyString(value) && isNoBackReason) {
                    ((BaseActivity) getActivity()).showErrorMsg("<" + control.DisplayName + "> 为必填项，请填写后再上报!");
                    return false;
                }

                if (control.Name.equals("malfunctionTypeId")) {
                    //故障主要原因
                    if (BugResonsDic.containsKey(value)) {
                        value = BugResonsDic.get(value);
                    } else {
                        value = "0";
                    }

                } else if (control.Name.equals("isCompleteConstruct")) {
                    //是否已处理好
                    value = value.equals("是") ? "1" : "0";

                } else if (control.Name.equals("isDealRerutn")) {
                    // 用户回单:
                    value = value.equals("有") ? "0" : "1";
                } else if (control.Name.equals("feedBackStateId")) {
                    // 用户意见:必选
                    if (feedBackDic.containsKey(value)) {
                        value = feedBackDic.get(value) + "";
                    }
                } else if (control.Name.equals("localeDealPerson")) {
                    value = localeDealPersonDic.get(value);
                }
                ri.setValue(control.Name, value);
            }
            return true;
        }

        public String getFeedBacks() {
            if (data.feedbackList != null) {
                StringBuffer sb = new StringBuffer();
                for (FeedBack feedBack : data.feedbackList) {
                    if (!BaseClassUtil.isNullOrEmptyString(feedBack.feedbackName)) {
                        sb.append(feedBack.feedbackName + ",");
                        if (!feedBackDic.containsKey(feedBack.feedbackName)) {
                            feedBackDic.put(feedBack.feedbackName, feedBack.feedbackID);
                        }
                    }
                }
                return sb.toString().substring(0, sb.toString().length() - 1);
            }
            return "";
        }

        public String getBugResons() {
            if (data.malfunctionTypeList != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(",");
                for (MalfunctionType malfunctionType : data.malfunctionTypeList) {
                    if (!BaseClassUtil.isNullOrEmptyString(malfunctionType.malfunctionTypeName)) {
                        sb.append(malfunctionType.malfunctionTypeName + ",");
                        if (!BugResonsDic.containsKey(malfunctionType.malfunctionTypeName)) {
                            BugResonsDic.put(malfunctionType.malfunctionTypeName, malfunctionType.malfunctionTypeId);
                        }
                    }
                }
                return sb.toString().substring(0, sb.toString().length() - 1);
            }
            return " ,";
        }

        public void getDefaultHandManValues(StringBuffer names, StringBuffer tels) {
            if (data.dealPersonList != null) {
                for (DealPerson dealPerson : data.dealPersonList) {
                    if (!BaseClassUtil.isNullOrEmptyString(dealPerson.sname)) {
                        names.append(dealPerson.sname + ",");
                        String tel = dealPerson.mobileTel != null ? dealPerson.mobileTel : "";
                        if (!localeDealPersonDic.containsKey(dealPerson.sname)) {
                            localeDealPersonDic.put(dealPerson.sname, "" + dealPerson.workerID);
                            DealPersonTelDic.put(dealPerson.sname, tel);
                        }
                        tels.append(tel + ",");
                    }
                }
            }
        }
    }
}
