package com.customform.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;

import com.customform.entity.OnBeanFragmentActionListener;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zoro at 2017/9/1.
 */
public abstract class MmtBaseView implements OnBeanFragmentActionListener {

    protected static final String EMPTY_STRING = "";
    private static final Matcher phoneNumberMatcher
            = Pattern.compile("(((\\+86)|(86))?1\\d{10})|(((\\d{3,4})|(\\d{3,4}-))?\\d{7,8})").matcher("");

    protected final Context context;
    protected final GDControl control;
    protected View view;

    private Map<String, Integer> controlIds;

    public MmtBaseView(Context context, GDControl control) {
        this.context = context;
        this.control = control;
    }

    public FragmentActivity getActivity() {
        return (FragmentActivity) context;
    }

    FlowBeanFragment getBeanFragment() {
        for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
            if (fragment instanceof FlowBeanFragment)
                return (FlowBeanFragment) fragment;
        }

        return null;
    }

    protected String getDefaultValue() {
        if (!TextUtils.isEmpty(control.Value)) {
            return control.Value;
        } else if (!TextUtils.isEmpty(control.DefaultValues)) {
            return control.DefaultValues;
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * 根据控件名称 Name查找控件 View，未找到返回 null.
     */
    public View findViewByName(String controlName) {
        if (TextUtils.isEmpty(controlName))
            return null;

        Integer viewId = controlIds.get(controlName);

        if (viewId == null)
            return null;

        return getActivity().findViewById(viewId);
    }

    View findViewByType(String type) {
        View containView = null;

        for (String name : controlIds.keySet()) {
            View view = findViewByName(name);

            // 若不是需要反馈的视图，则继续循环
            if (!(view instanceof FeedBackView)) {
                continue;
            }

            GDControl control = (GDControl) view.getTag();

            if (control.Type.contains(type)) {
                containView = view;
            }

            // 不严谨，有多个地址控件在一个界面会有问题
            if (control.Type.equals(type)) {
                return view;
            }
        }

        return containView;
    }

    @Override
    public void onViewCreated(Map<String, Integer> controlIds) {
        this.controlIds = controlIds;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public boolean onStart(Intent intent) {
        return false;
    }

    @Override
    public boolean onEventBusCallback(Object tag) {
        return false;
    }

    protected View buildReadonlyView() {
        ImageTextView view = new ImageTextView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (!TextUtils.isEmpty(control.Unit)) {
            view.setModifier(control.Unit);
        }

        String defaultValue = getDefaultValue();
        view.setValue(defaultValue);

        if (phoneNumberMatcher.reset(defaultValue).find()) {
            view.getValueTextView().setAutoLinkMask(Linkify.PHONE_NUMBERS);
            view.getValueTextView().setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            view.getValueTextView().setTextIsSelectable(true);
        }

        return view;
    }

    @DrawableRes
    protected abstract int getIconRes();

    protected abstract View build();

    public View generate() {

        View view;
        if (control.isReadOnly() && !ReadonlyHandleable.class.isInstance(this)) {
            view = buildReadonlyView();
        } else {
            view = build();
        }
        this.view = view;
        return view;
    }

    public static MmtBaseView newInstance(Context context, GDControl control) {
        MmtBaseView baseView = null;

        try {
            if (control.isReadOnly()) {
                control.addEnable = false;
            }

            String type = control.Type;
            switch (type) {
                case "短文本":// 短文本
                    baseView = new MmtEditView(context, control);
                    break;
                case "多值":// 多值
                    baseView = new MmtMultiView(context, control);
                    break;
                case "保留字":// 保留字
                    baseView = new MmtTextView(context, control);
                    break;
                case "标签":// 标签
                    baseView = new MmtLabelView(context, control);
                    break;
                case "是否":// 是否
                    baseView = new MmtSwitchView(context, control);
                    break;
                case "浮点型":// 浮点型 (仅旧版使用)
                    control.ValidateRule += ",number";
                    baseView = new MmtEditView(context, control);
                    break;
                case "下拉框":// 下拉框
                    baseView = new MmtMultiView(context, control);
                    break;
                case "长文本":// 长文本
                    baseView = new MmtEditView(context, control);
                    break;
                case "联动框":// 联动框
                    baseView = new MmtTwoMultiView(context, control);
                    break;
                case "百度地址":// 百度地址
                    baseView = new MmtBDAddressView(context, control);
                    break;
                case "当前地址":// 当前地址，总是以百度地址方式实时获取当前地址
                    baseView = new MmtCurrentBDAddressView(context, control);
                    break;
                /////////////////////////////////////////////
                case "选择器":// 选择器，可选值在字典中配置，由服务获取
                    baseView = new MmtMultiTwoView(context, control);
                    break;

                case "仅时间": // 仅时间，单独选择时间不选日期
                case "仅日期": // 仅日期，单独选择日期不选择时间
                case "日期框":// 日期框，选择时间和日期
                    baseView = new MmtDateTimeView(context, control);
                    break;
                // 新版产品不使用 "仅日期V2","日期框V2"
                case "仅日期V2":// 仅日期V2 (和"仅日期"的区别是没有默认取当前日期)
                case "日期框V2":// 日期框V2 (和"日期框"的区别是没有默认取当前时间)
                    control.ConfigInfo = "默认为空";
                    baseView = new MmtDateTimeView(context, control);
                    break;
                case "时间v2":// 时间V2 有值后不再获取时间
                    baseView = new MmtServerDateOnceView(context, control);
                    break;
                case "时间":// 时间
                    baseView = new MmtServerDateView(context, control);
                    break;
                case "日期":// 日期
                    baseView = new MmtServerDateView(context, control);
                    break;

                case "值选择器":// 值选择器，可选值给定的下拉框
                    baseView = new MmtFixedMultiView(context, control);
                    break;
                case "平铺值选择器":// 平铺值选择器，可选值给定的RadioButton
                    baseView = new MmtRadioButtonView(context, control);
                    break;
                case "站点选择器":// 站点选择器，通过服务获取用户的所属站点列表
                    baseView = new MmtStationSelectorView(context, control);
                    break;
                case "本人姓名":// 本人姓名，无论是不是只读，有值显示值，无值显示当前用户的真实名字
                    baseView = new MmtCurrentUserNameView(context, control);
                    break;
                case "本人部门":// 本人部门，无论是不是只读，有值显示值，无值显示当前用户的部门
                    baseView = new MmtCurrentUserDeptView(context, control);
                    break;
                case "常用语":// 常用语，最多支持二级的数据字典
                    baseView = new MmtCommonPhrasesView(context, control);
                    break;
                case "值复选器":// 值复选器，可选值给定的CheckBox
                    baseView = new MmtFixedMultiSelectView(context, control);
                    break;
                case "可编辑值选择器":// 可编辑值选择器，可选值给定的下拉框同时可以编辑
                    baseView = new MmtEditableMultiView(context, control);
                    break;
                //////////////////////////////////////////////////
                case "当前坐标": // 当前坐标,显示当前位置的坐标，不提供修改
                    baseView = new MmtCurrentDotView(context, control);
                    break;
                case "距离": // 距离，计算当前位置坐标与给定坐标间的距离(逻辑使用时处理)
                    baseView = new MmtDistanceView(context, control);
                    break;
                case "动态值选择器": // 动态值选择器 buildFixedMultiView
                    baseView = new MmtFixedMultiView(context, control);
                    break;
                case "二级选择器": // 二级选择器
                    baseView = new MmtTwoLevelSelectorView(context, control);
                    break;
                case "区域控件": // 区域控件
                    baseView = new MmtSelectAreaView(context, control);
                    break;
                case "场站设备选择器": // 场站设备选择器
                    baseView = new MmtStationDeviceSelectorView(context, control);
                    break;
                case "设备选择": // 设备选择
                    baseView = new MmtDeviceSelectorView(context, control);
                    break;
                case "人员选择器": // 人员选择器
                    baseView = new MmtPersonSelectorView(context, control);
                    break;
                case "浓度": // 汉威检漏仪-浓度
                    baseView = new MmtConcentrationView(context, control);
                    break;
                case "图片":
                    control.canSelect = true;
                    baseView = new MmtImageView(context, control);
                    break;
                case "拍照"://仅拍照
                    baseView = new MmtImageView(context, control);
                    break;
                case "录音":
                    baseView = new MmtRecorderView(context, control);
                    break;
                case "坐标": //坐标，长按地图选点
                    baseView = new MmtDotView(context, control);
                    break;
                case "附件"://附件
                    baseView = new MmtAttachmentView(context, control);
                    break;
                case "坐标V2"://坐标V2，指针选点
                    baseView = new MmtDotTwoView(context, control);
                    break;
                case "坐标V3"://坐标V3，指针选点
                    baseView = new MmtDotThreeView(context, control);
                    break;
                case "视频":// 视频
                    control.canSelect = true;
                    baseView = new MmtVideoView(context, control);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return baseView;
    }

    @Override
    public String toString() {
        if (this.control != null)
            return this.control.toString();
        else
            return super.toString();
    }

    /**
     * Marker interface for tagging a class which can handle its readonly state by itself.
     */
    public interface ReadonlyHandleable {
    }
}