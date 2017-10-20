package com.maintainproduct.module;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageDotView;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.customview.ImageLineView;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton;
import com.mapgis.mmt.common.widget.customview.MultiSwitchButton.OnScrollListener;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.doinback.ReportInBackEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BeanFragment extends Fragment {
    private final GDFormBean data;

    private MultiSwitchButton switchButton;
    private LinearLayout maintenanceFormTop;
    private FrameLayout maintenanceFormMid;

    public Fragment[] fragments;

    private String absolutePaths = "";
    private String relativePaths = "";

    private String fragmentFileRelativePath;

    private Class<?> cls;
    private String position;

    /**
     * 当前显示的Fragment
     */
    private int showFragmentIndex = 0;

    private boolean addEnable = true;

    //是否加水印的标志，！=null且 size> 则加水印
    public ArrayList<String> waterTexts;

    public OnBeanFragmentViewCreatedListener onBeanFragmentViewCreatedListener;

    public GDFormBean getData() {
        return data;
    }

    public interface OnBeanFragmentViewCreatedListener {
        void onBeanFragmentViewCreated();
    }

    public BeanFragment(GDFormBean data) {
        this.data = data;
    }

    /**
     * 父级联动被选择时触发的监听
     */
    public TwoSpinnerSelectListener twoSpinnerSelectListener;

    public interface TwoSpinnerSelectListener {
        void onSelect(String value);
    }

    /**
     * 子级联动被选择时触发的监听
     */
    public TwoSpinnerChildSelectListener twoSpinnerChildSelectListener;

    public interface TwoSpinnerChildSelectListener {
        void onChildSelect(String value);
    }

    /**
     * 获取所有文件的绝对路径，多个按,分割
     */
    public String getAbsolutePaths() {
        return absolutePaths.length() > 0 ? absolutePaths.substring(0, absolutePaths.length() - 1) : "";
    }

    /**
     * 获取所有文件的相对路径，多个按,分割
     */
    public String getRelativePaths() {
        return relativePaths.length() > 0 ? relativePaths.substring(0, relativePaths.length() - 1) : "";
    }

    /**
     * 设置文件存储的相对路径 Repair/CASENO-1111-11111/
     */
    public void setFragmentFileRelativePath(String fragmentFileRelativePath) {
        this.fragmentFileRelativePath = fragmentFileRelativePath;
    }

    /**
     * 设置坐标回调请求类
     */
    public void setCls(Class<?> cls) {
        this.cls = cls;
    }

    public String getPosition() {
        return position;
    }

    /**
     * 录音及图片是否能添加
     */
    public void setAddEnable(boolean addEnable) {
        this.addEnable = addEnable;
    }

    /**
     * 将所有内容显示为只读
     */
    public void setFormOnlyShow() {
        data.setOnlyShow();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.maintenance_form, container, false);

        maintenanceFormMid = (FrameLayout) view.findViewById(R.id.maintenanceFormMid);
        maintenanceFormTop = (LinearLayout) view.findViewById(R.id.maintenanceFormTop);

        switchButton = (MultiSwitchButton) view.findViewById(R.id.maintenanceFormTitle);
        switchButton.setOnScrollListener(new OnScrollListener() {
            @Override
            public void OnScrollComplete(int index) {
                showFragment(index);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

		/*---------------------------------------------------顶部视图和中部Fragment初始化------------------------------------------------------*/
        List<String> groupNames = new ArrayList<String>();

        fragments = new Fragment[data.Groups.length];

        // 将每一个Group都生成对应的Fragment
        for (int i = 0; i < data.Groups.length; i++) {
            GDGroup group = data.Groups[i];

            groupNames.add(group.Name);

            GroupFragment fragment = new GroupFragment(group.Controls);
            fragment.onViewCreatedListener = new OnViewCreatedListener() {
                @Override
                public void onViewCreatedExe() {
                    if (onBeanFragmentViewCreatedListener != null) {
                        onBeanFragmentViewCreatedListener.onBeanFragmentViewCreated();
                    }
                }
            };
            fragments[i] = fragment;

            FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
            frameLayout.setId(frameLayout.hashCode());

            maintenanceFormMid.addView(frameLayout);

            ft.replace(frameLayout.getId(), fragment);
        }

        // 隐藏所有的Fragment
        for (Fragment fragment : fragments) {
            ft.hide(fragment);
        }

        ft.commit();

        // 显示第一个Fragment
        showFragment(0);

        // 若只有一个标题，则隐藏标题栏
        if (groupNames.size() == 0) {
            groupNames.add("未知标题");
        }

        // 加载滑动标题栏
        switchButton.setContent(groupNames);

        // 若只有一个标题，则不显示
        if (groupNames.size() == 1) {
            maintenanceFormTop.setVisibility(View.GONE);
        }

        // Fragment创建完毕后提供一个接口
        if (beanFragmentOnCreate != null) {
            beanFragmentOnCreate.onCreated();
        }
    }

    /**
     * 获取组名
     */
    public List<String> getGroupNames() {
        return switchButton.getContent();
    }

    /**
     * 获得当前显示的Fragment的位置
     *
     * @return
     */
    public int getShowFragmentIndex() {
        return showFragmentIndex;
    }

    /**
     * 用Fragment替换指定index的Fragment
     *
     * @param fragment
     * @param index
     */
    public void replaceFragment(Fragment fragment, int index) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(fragments[index].getId(), fragment);
        ft.commitAllowingStateLoss();
        fragments[index] = fragment;
    }

    /**
     * 显示指定的Fragment
     *
     * @param index 位置
     */
    public void showFragment(int index) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        ft.hide(fragments[showFragmentIndex]);
        ft.show(fragments[index]);

        showFragmentIndex = index;

        ft.commit();
    }

    public void hideFragment(int index) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.hide(fragments[index]);
        ft.commit();
    }

    /**
     * 存储所有表单信息到获取的数据中
     *
     * @param 操作状态 主要作用是在立即上报状态下，为必填项做检查
     * @return 反馈信息列表
     */
    public List<FeedItem> getFeedbackItems(int status) {
        absolutePaths = "";
        relativePaths = "";

        List<FeedItem> items = new ArrayList<FeedItem>();

        if (fragments == null || fragments.length == 0) {
            return items;
        }

        // 获取所有表单中的信息
        for (Fragment fragment : fragments) {

            if (!(fragment instanceof GroupFragment)) {
                continue;
            }

            GroupFragment groupFragment = (GroupFragment) fragment;

            List<FeedItem> controlItems = groupFragment.getFeedbackItems(status);

            // 当操作状态为上报状态时候且有必填项没有填写时，返回null;
            if (controlItems == null) {
                return null;
            }

            items.addAll(controlItems);
        }

        return items;
    }

    public interface OnViewCreatedListener {
        void onViewCreatedExe();
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 根据GDGroups生成的表单界面
     */
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class GroupFragment extends Fragment {
        private final GDControl[] controls;

        public LinearLayout parentLayout;
        public OnViewCreatedListener onViewCreatedListener;

        public GroupFragment(GDControl[] controls) {
            this.controls = controls;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ScrollView scrollView = new ScrollView(getActivity());
            scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            parentLayout = new LinearLayout(getActivity());
            parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            parentLayout.setOrientation(LinearLayout.VERTICAL);

            scrollView.addView(parentLayout);

            if (controls == null) {
                return scrollView;
            }

            // 根据配置项对象生成表单项
            for (final GDControl control : controls) {
                control.setAdditionalParas(cls, addEnable, fragmentFileRelativePath);

                View formView = control.createView(getActivity());

                if (formView == null)
                    continue;

                if (control.DisplayName.contains("坐标") || control.Type.equals("坐标")) {
                    position = control.Value;
                }

                if (control.Type.equals("联动框") && !control.isReadOnly()) {
                    initUnionView((ImageButtonView) formView);
                }

                parentLayout.addView(formView);

                if (control.IsVisible != null && control.IsVisible.equals("false")) {
                    formView.setVisibility(View.GONE);
                }

                if (formView.getVisibility() == View.GONE) {
                    continue;
                }

                // 分割线
                ImageLineView lineView = new ImageLineView(getActivity());
                lineView.setTag(control.DisplayName);
                parentLayout.addView(lineView);
            }

            return scrollView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (onViewCreatedListener != null) {
                onViewCreatedListener.onViewCreatedExe();
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            // 在地图界面长按返回后，在这里查找到坐标反馈视图，并用坐标值进行填充
            if (getActivity().getIntent().getStringExtra("location") != null
                    && !getActivity().getIntent().getStringExtra("location").equals("")) {

                for (int i = 0; i < parentLayout.getChildCount(); i++) {
                    if (parentLayout.getChildAt(i) instanceof ImageDotView) {
                        ImageDotView dotView = (ImageDotView) parentLayout.getChildAt(i);
                        dotView.setValue(getActivity().getIntent().getStringExtra("location"));
                        getActivity().getIntent().removeExtra("location");
                    }
                }
                AppManager.resetActivityStack(getActivity());
            }

            if (getActivity().getIntent().getStringExtra("loc") != null
                    && !getActivity().getIntent().getStringExtra("loc").equals("")) {

                for (int i = 0; i < parentLayout.getChildCount(); i++) {
                    if (parentLayout.getChildAt(i) instanceof ImageDotView) {
                        ImageDotView dotView = (ImageDotView) parentLayout.getChildAt(i);
                        dotView.setValue(getActivity().getIntent().getStringExtra("loc"));
                        getActivity().getIntent().removeExtra("loc");
                    }
                }
                AppManager.resetActivityStack(getActivity());
            }
        }

        /**
         * 存储当前表单信息封装成FeedItem存储
         */
        public List<FeedItem> getFeedbackItems(int status) {
            List<FeedItem> items = new ArrayList<FeedItem>();

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

                FeedItem item = new FeedItem();

                FeedBackView feedBackView = (FeedBackView) view;

                // 对应服务器端数据库所要存储的表的列名
                item.Name = control.Name;

                // 用户所填写的值
                item.Value = feedBackView.getValue();

                // 若反馈视图是Fragment的类型
                if (view instanceof ImageFragmentView) {

                    ImageFragmentView fragmentView = (ImageFragmentView) view;

                    HashMap<String, String> dataMap = fragmentView.getKeyValue();

                    item.Value = dataMap.get(ImageFragmentView.RELATIVE_KEY_STRING);

//					if (BaseClassUtil.isNullOrEmptyString(item.Value)) {
//						continue;
//					}

                    // 绝对路径，文件存储在手持本地的真实路径
                    absolutePaths = absolutePaths + dataMap.get(ImageFragmentView.ABSOLUTE_KEY_STRING) + ",";

                    // 相对路径
                    relativePaths = relativePaths + item.Value + ",";
                }

                // 判断是否为必填项，若是并且没有填写，则给出提示
                // 是上报状态并且是必填写项并且未填写任何信息，给出提示
                if (status == ReportInBackEntity.REPORTING && control.Validate.equals("1") && BaseClassUtil.isNullOrEmptyString(item.Value)) {
                    ((BaseActivity) getActivity()).showErrorMsg("<" + control.DisplayName + "> 为必填项，请填写后再上报!");
                    return null;
                }
                items.add(item);

            }

            return items;

        }

        /**
         * 将联动框关联起来,目前只支持二级联动
         */
        public void initUnionView(final ImageButtonView parentView) {

            // 查询父联动框的数据信息
            final GDControl control = (GDControl) parentView.getTag();

            parentView.getButton().setOnClickListener(new OnClickListener() {
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
                        fragment.setListItemClickListener(new OnListItemClickListener() {
                            @Override
                            public void onListItemClick(int arg2, String value) {
                                // 点击选项，将信息填入到文本框中
                                parentView.setValue(value);

                                if (twoSpinnerSelectListener != null && !control.DisplayName.contains(";")) {
                                    twoSpinnerSelectListener.onSelect(value);
                                }
                                if (twoSpinnerChildSelectListener != null && control.DisplayName.contains(";")) {
                                    twoSpinnerChildSelectListener.onChildSelect(value);
                                }

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
                                if (twoSpinnerChildSelectListener != null) {
                                    twoSpinnerChildSelectListener.onChildSelect(unionStr.get(0));
                                }

                                // 子联动框点击事件
                                unionView.getButton().setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        String dialogTitleName = control.DisplayName;
                                        if (control.DisplayName.contains(";")) {// 子联动框
                                            dialogTitleName = control.DisplayName.split(";")[1];
                                        }

                                        ListDialogFragment fragment = new ListDialogFragment(dialogTitleName, unionStr);
                                        fragment.show(getActivity().getSupportFragmentManager(), "");
                                        fragment.setListItemClickListener(new OnListItemClickListener() {
                                            @Override
                                            public void onListItemClick(int arg2, String value) {
                                                unionView.setValue(value);
                                                if (twoSpinnerChildSelectListener != null) {
                                                    twoSpinnerChildSelectListener.onChildSelect(value);
                                                }
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
                if (twoSpinnerSelectListener != null && !control.DisplayName.contains(";")) {
                    twoSpinnerSelectListener.onSelect(initValue);
                }
                if (twoSpinnerChildSelectListener != null && control.DisplayName.contains(";")) {
                    twoSpinnerChildSelectListener.onChildSelect(initValue);
                }
            }

        }

        /**
         * 根据类型和展示名找到指定的联动框
         */
        private ImageButtonView findUnionViewByTypeAndDisplanName(String type, String displayName) {
            for (int i = 0; i < parentLayout.getChildCount(); i++) {
                View view = parentLayout.getChildAt(i);

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

    private BeanFragmentOnCreate beanFragmentOnCreate;

    public void setBeanFragmentOnCreate(BeanFragmentOnCreate beanFragmentOnCreate) {
        this.beanFragmentOnCreate = beanFragmentOnCreate;
    }

    public interface BeanFragmentOnCreate {
        void onCreated();
    }

}
