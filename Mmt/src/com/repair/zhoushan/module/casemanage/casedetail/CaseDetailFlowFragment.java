package com.repair.zhoushan.module.casemanage.casedetail;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.maintainproduct.v2.module.EventTypeItem;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.customview.ExpandableLayout;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageLineView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.eventreport.FetchEventTypeTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有的已办节点放融合到一个页面进行展示
 */
public class CaseDetailFlowFragment extends Fragment {

    private ArrayList<String> groupTitles;
    private ArrayList<GDFormBean> gdFormBeans;

    private LinearLayout eventReportMainForm;

    private Class<?> cls;
    public ArrayList<String> waterTexts;
    private boolean addEnable;

    public CaseDetailFlowFragment() {
        this.groupTitles = new ArrayList<>();
        this.gdFormBeans = new ArrayList<>();
        this.addEnable = false;
    }

    public CaseDetailFlowFragment(ArrayList<String> groupTitles, ArrayList<GDFormBean> gdFormBeans) {
        this.groupTitles = groupTitles;
        this.gdFormBeans = gdFormBeans;
        this.addEnable = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.cls = getActivity().getClass();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.zs_event_report, container, false);

        eventReportMainForm = (LinearLayout) view.findViewById(R.id.eventReportMainForm);

        initView();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);

            View childView = eventReportMainForm.getChildAt(0);
            if (null != childView) {
                childView.performClick();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initView() {

        GDFormBean data = null;
        GDGroup group = null;
        for (GDFormBean gdFormBean : gdFormBeans) {
            data = gdFormBean;

            for (int i = 0; i < data.Groups.length; i++) {

                group = data.Groups[i];

                final ExpandableLayout expandableLayout = new ExpandableLayout(getActivity());
                expandableLayout.setId(expandableLayout.hashCode());
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                expandableLayout.setLayoutParams(params);

                // The title of the group.
                // -- Start Group Title --
                RelativeLayout titleView = new RelativeLayout(getActivity());
                titleView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT));
                int padding = DimenTool.dip2px(getActivity(), 10.0f);
                titleView.setPadding(padding, padding, padding, padding);
                titleView.setBackgroundColor(Color.parseColor("#eaeaea"));

                TextView titleText = new TextView(getActivity());
                titleText.setText(group.Name);
                titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f);
                // groupTitleText.getPaint().setFakeBoldText(true); //字体加粗
                titleView.addView(titleText);

                final ImageView titleImage = new ImageView(getActivity());
                titleImage.setId(titleImage.hashCode());
                titleImage.setImageResource(R.drawable.ic_expand_more_grey);
                RelativeLayout.LayoutParams titleElemlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                titleElemlp.addRule(RelativeLayout.CENTER_VERTICAL);
                titleElemlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                titleElemlp.setMargins(0, 0, padding, 0);
                titleImage.setLayoutParams(titleElemlp);
                titleView.addView(titleImage);
                titleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (expandableLayout.isExpanded()) {
                            expandableLayout.collapse();
                        } else {
                            expandableLayout.expand();
                        }
                    }
                });
                // -- End Group Title --

                // -- Start Group Content --
                LinearLayout contentView = new LinearLayout(getActivity());
                contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                contentView.setOrientation(LinearLayout.VERTICAL);
                contentView.setId(contentView.hashCode());

                // 根据配置项对象生成表单项
                for (final GDControl control : group.Controls) {
                    control.setAddEnable(addEnable);

                    switch (control.Type) {
                        case "坐标":
                        case "坐标V2":
                            control.setLocateBackClass(cls);
                            break;
                        case "图片":
                        case "拍照":
                            control.setRelativePath(control.ConfigInfo + "/图片/");
                            break;
                        case "录音":
                            control.setRelativePath(control.ConfigInfo + "/录音/");
                            break;
                        case "附件":
                            control.setRelativePath(control.ConfigInfo + "/附件/");
                            break;
                    }

                    View formView = control.createView(getActivity());

                    if (formView == null)
                        continue;

//                    if (control.DisplayName.contains("坐标") || control.Type.equals("坐标")) {
//                        position = control.Value;
//                    }

                    if (control.Type.equals("联动框") && control.isReadOnly()) {
                        if (control.DisplayName.contains(";")) {
                            ((ImageTextView) formView).setKey(control.DisplayName.split(";")[1]);
                        }
                    }

                    if (control.Type.equals("联动框") && !control.isReadOnly()) {

                        final View view = formView;

                        String levelOneName;
                        String levelTwoName;
                        if (control.ConfigInfo.contains(".")) {
                            // 二级菜单
                            levelOneName = control.UploadArgs;
                            levelTwoName = control.ConfigInfo.split("\\.")[1]; //String以“.”为分隔符划分
                        } else {
                            //一级菜单
                            levelOneName = control.ConfigInfo;
                            levelTwoName = control.UploadArgs;
                        }

                        new FetchEventTypeTask(getActivity(), false, new MmtBaseTask.OnWxyhTaskListener<ResultData<EventTypeItem>>() {
                            @Override
                            public void doAfter(ResultData<EventTypeItem> resultData) {

                                StringBuilder sb = new StringBuilder();

                                if (control.ConfigInfo.contains(".")) {

                                    for (EventTypeItem pItem : resultData.DataList) {

                                        for (EventTypeItem cItem : pItem.SubItem) {
                                            sb.append(cItem.NODENNAME).append(",");
                                        }
                                        sb.replace(sb.length() - 1, sb.length(), ";");
                                    }

                                } else {
                                    for (EventTypeItem eventTypeItem : resultData.DataList) {
                                        sb.append(eventTypeItem.NODENNAME).append(",");
                                    }
                                }

                                control.DefaultValues = sb.substring(0, sb.length() - 1);

                                String defValues = control.DefaultValues.split(";")[0];
                                ((ImageButtonView) view).setValue(defValues.split(",")[0]);

                                initUnionView((ImageButtonView) view);
                            }
                        }).mmtExecute(levelOneName, levelTwoName);
                    }
                    contentView.addView(formView);

                    if (control.IsVisible != null && control.IsVisible.equals("false")) {
                        formView.setVisibility(View.GONE);
                    }

                    if (formView.getVisibility() == View.GONE) {
                        continue;
                    }

                    // 分割线
                    ImageLineView lineView = new ImageLineView(getActivity());
                    lineView.setTag(control.DisplayName);
                    contentView.addView(lineView);
                }
                // -- End Group Content --

                expandableLayout.addView(contentView);
                expandableLayout.setOnExpansionEndListener(new ExpandableLayout.OnExpansionEndListener() {
                    @Override
                    public void onExpansionEnd(boolean isExpanded) {
                        if (isExpanded) {
                            titleImage.setImageResource(R.drawable.ic_expand_less_grey);
                        } else {
                            titleImage.setImageResource(R.drawable.ic_expand_more_grey);
                        }
                    }
                });

                eventReportMainForm.addView(titleView); // Add Title
                eventReportMainForm.addView(expandableLayout); // Add Content
                eventReportMainForm.addView(new ImageLineView(getActivity())); // Add Divide Line
            }
        }
    }

    /**
     * 将联动框关联起来,目前只支持二级联动
     */
    public void initUnionView(final ImageButtonView parentView) {

        // 查询父联动框的数据信息
        final GDControl control = (GDControl) parentView.getTag();

        parentView.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    /** 标识 是否 是 第二级 */
                    String showName = control.DisplayName;
                    String defValues = control.DefaultValues;
                    if (control.DisplayName.contains(";")) {// 子联动框
                        showName = control.DisplayName.split(";")[1];
                        defValues = BaseClassUtil.StringToList(control.DefaultValues, ";").get(0);
                    }

                    ListDialogFragment fragment = new ListDialogFragment(showName, BaseClassUtil.StringToList(defValues, ","));
                    fragment.show(getActivity().getSupportFragmentManager(), "");
                    fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int arg2, String value) {
                            // 点击选项，将信息填入到文本框中
                            parentView.setValue(value);

//                            if (twoSpinnerSelectListener != null && !control.DisplayName.contains(";")) {
//                                twoSpinnerSelectListener.onSelect(value);
//                            }
//                            if (twoSpinnerChildSelectListener != null && control.DisplayName.contains(";")) {
//                                twoSpinnerChildSelectListener.onChildSelect(value);
//                            }

                            // 查询父联动框对应的子联动框
                            final ImageButtonView unionView = findUnionViewByTypeAndDisplanName("联动框", control.DisplayName);

                            if (unionView == null) {
                                // ((BaseActivity)
                                // getActivity()).showErrorMsg("未查询到联动子选项框");
                                return;
                            }

                            // 子联动框的数据信息
                            final GDControl unionControl = (GDControl) unionView.getTag();

                            // 根据父联动框的点击信息获取,获取对应的子联动框的显示列表信息
                            final List<String> unionStr = BaseClassUtil.StringToList(
                                    BaseClassUtil.StringToList(unionControl.DefaultValues, ";").get(arg2), ",");

                            if (unionStr.size() == 0 || BaseClassUtil.isNullOrEmptyString(unionStr.get(0))) {
                                ((BaseActivity) getActivity()).showErrorMsg("未配置正确的子联动框信息");
                                return;
                            }

                            // 当父级变动时， 改变子级的数据源，并默认显示第一个数据
                            unionView.setValue(unionStr.get(0));
//                            if (twoSpinnerChildSelectListener != null) {
//                                twoSpinnerChildSelectListener.onChildSelect(unionStr.get(0));
//                            }

                            // 子联动框点击事件
                            unionView.getButton().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    String dialogTitleName = control.DisplayName;
                                    if (control.DisplayName.contains(";")) {// 子联动框
                                        dialogTitleName = control.DisplayName.split(";")[1];
                                    }

                                    ListDialogFragment fragment = new ListDialogFragment(dialogTitleName, unionStr);
                                    fragment.show(getActivity().getSupportFragmentManager(), "");
                                    fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                                        @Override
                                        public void onListItemClick(int arg2, String value) {
                                            unionView.setValue(value);
//                                            if (twoSpinnerChildSelectListener != null) {
//                                                twoSpinnerChildSelectListener.onChildSelect(value);
//                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 初次加载时 即 触发 一下
        if (BaseClassUtil.StringToList(control.DefaultValues, ",").size() > 0) {
            String initValue = BaseClassUtil.StringToList(control.DefaultValues, ",").get(0);
//            if (twoSpinnerSelectListener != null && !control.DisplayName.contains(";")) {
//                twoSpinnerSelectListener.onSelect(initValue);
//            }
//            if (twoSpinnerChildSelectListener != null && control.DisplayName.contains(";")) {
//                twoSpinnerChildSelectListener.onChildSelect(initValue);
//            }
        }

    }

    /**
     * 根据类型和展示名找到指定的联动框
     */
    private ImageButtonView findUnionViewByTypeAndDisplanName(String type, String displayName) {
        for (int i = 0; i < eventReportMainForm.getChildCount(); i++) {
            View view = eventReportMainForm.getChildAt(i);

            // 若不是需要反馈的视图，则继续循环
            if (!(view instanceof ImageButtonView)) {
                continue;
            }

            GDControl control = (GDControl) view.getTag();

            // 类型时下拉框，并且不是自己，并且以父控件的名字开头
            if (control.Type.equals("联动框") && control.DisplayName.length() != displayName.length()
                    && control.DisplayName.startsWith(displayName)) {
                return (ImageButtonView) view;
            }
        }
        return null;
    }
}
