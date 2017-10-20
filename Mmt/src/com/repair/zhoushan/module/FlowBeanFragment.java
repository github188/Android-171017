package com.repair.zhoushan.module;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.customform.entity.KeyValuePair;
import com.customform.entity.OnBeanFragmentActionListener;
import com.customform.entity.StationDeviceEvent;
import com.customform.module.BeanCacheManager;
import com.customform.module.MmtBaseFragment;
import com.customform.view.MmtBaseView;
import com.maintainproduct.entity.FeedItem;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.maintainproduct.entity.OnAsyncSelectorLoadFinishedListener;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.customview.FeedBackView;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageLineView;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.eventbustype.DeviceSelectEvent;
import com.repair.zhoushan.module.devicecare.MaintenanceFeedBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlowBeanFragment extends MmtBaseFragment implements GDControl.OnSelectedChangedListener {

    public static final String SELECTOR_FLAG_LEVEL_ONE = "@@";

    public GDFormBean data;

    public String absolutePaths = "";
    public String relativePaths = "";

    private String fragmentFileRelativePath;

    private Class<?> cls;
    private String position;

    private boolean addEnable = true;
    private boolean showFormOnly = false;
    private List<String> exceptFiledList;

    //region Logic about "动态值选择器".

    private ArrayList<MaintenanceFeedBack> filterCriteria;

    // 记录各个过滤条件字段的当前状态；Key:过滤条件字段，Value:过滤条件字段的当前值
    private Map<String, String> filterFields = new HashMap<>();

    // 过滤条件值的组合
    private List<ArrayList<String>> filterValues;

    // 过滤条件值的组合对应的包含字段集（下标与过滤条件值组合对应）
    private List<ArrayList<String>> includeFields;

    public Map<String, String> getFilterFields() {
        return filterFields;
    }

    public ViewGroup getEventReportMainForm() {
        return eventReportMainForm;
    }

    // 动态值选择器
    public void setFilterCriteria(ArrayList<MaintenanceFeedBack> filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    public List<String> getExceptFiledList() {
        return exceptFiledList;
    }

    HashMap<String, String> keyValueMap;

    public void setFilterCriteria(ArrayList<MaintenanceFeedBack> filterCriteria, HashMap<String, String> keyValueMap) {
        this.filterCriteria = filterCriteria;
        this.keyValueMap = keyValueMap;
    }

    // 动态值选择器
    private void resolveFilterCriteria() {
        if (filterCriteria == null || filterCriteria.size() == 0)
            return;

        this.filterValues = new ArrayList<>();
        this.includeFields = new ArrayList<>();

        ArrayList<String> fValue; // 过滤条件值
        ArrayList<String> fField; // 过滤条件值对应的包含字段集
        for (MaintenanceFeedBack item : filterCriteria) {
            fValue = new ArrayList<>();
            fField = new ArrayList<>();

            fValue.addAll(BaseClassUtil.StringToList(item.filterConditionVal, ","));
            fField.addAll(BaseClassUtil.StringToList(item.fileds, ","));
            filterValues.add(fValue);
            includeFields.add(fField);
        }

        List<String> fFields = BaseClassUtil.StringToList(filterCriteria.get(0).filterConditionFiled, ",");
        if (filterFields.size() == 0) {
            for (String item : fFields) {
                filterFields.put(item, "");
            }
        }

        Set<String> filterKeys = filterFields.keySet();

        //遍历台账表和任务表的详情信息
        for (String key : filterKeys) {
            if (keyValueMap == null)
                break;

            if (keyValueMap.containsKey(key) && !TextUtils.isEmpty(keyValueMap.get(key)))
                filterFields.put(key, keyValueMap.get(key));
        }

        //遍历工单反馈表的字段值
        for (GDGroup gdGroup : data.Groups) {
            for (GDControl gdControl : gdGroup.Controls) {
                if (gdControl.Type.equals("动态值选择器") && filterKeys.contains(gdControl.Name)) {

                    String value = "";

                    if (!TextUtils.isEmpty(gdControl.Value)) {
                        value = gdControl.Value;
                    } else if (!TextUtils.isEmpty(gdControl.DefaultValues)) {
                        value = gdControl.DefaultValues;
                    } else if (!TextUtils.isEmpty(gdControl.ConfigInfo)) {
                        value = BaseClassUtil.StringToList(gdControl.ConfigInfo, ",").get(0);
                    }

                    filterFields.put(gdControl.Name, value);
                }
            }
        }
    }
    //endregion

    // region Logic about data cache:
    // 1.Save in onPause(Activity);
    // 2.Restore in onCreate(Fragment, Activity should pass parameter "CacheSearchParam" to Fragment);
    // 3.Delete after reporting successfully(Activity).

    /**
     * Should be called in host Activity's onPause() method (If data cache is needed).
     */
    public void saveCacheData(int userId, String key) {
        saveCacheData(userId, key, -1);
    }

    public void saveCacheData(int userId, String key, int recordId) {
        saveCacheData(userId, key, recordId, null, null);
    }

    public void saveCacheData(int userId, String key, int recordId, List<String> excludeTypes, List<String> includeFieldNames) {
        try {
            //坐标和位置的缓存还是保留
            List<FeedItem> feedbackItems = getFeedbackItems(ReportInBackEntity.SAVING, excludeTypes, includeFieldNames);

            BeanCacheManager.save(userId, key, recordId, feedbackItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCacheRecordId(int userId, String key) {
        return BeanCacheManager.getRecordId(userId, key);
    }

    /**
     * Should be called when the record is reported successfully.
     */
    public void deleteCacheData(int userId, String key) {
        BeanCacheManager.delete(userId, key);
    }

    /**
     * Get cache data from local database.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();

            if (args != null) {
                this.data = args.getParcelable("GDFormBean");

                if (showFormOnly && data != null) {
                    data.setOnlyShow(showFormOnly, exceptFiledList);
                }

                String cacheSearchParam = args.getString("CacheSearchParam");

                BeanCacheManager.load(cacheSearchParam, data);

                resolveFilterCriteria();
            }

            EventBus.getDefault().register(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //endregion

    public Map<String, Integer> controlIds = new HashMap<>();

    public GDFormBean getData() {
        return data;
    }

    public FlowBeanFragment() {

        this.internalOnAsyncSelectorLoadFinishedListener = new OnAsyncSelectorLoadFinishedListener() {
            @Override
            public void onSingleSelectorLoaded(String fieldName, List<String> fieldValues) {
                // Noop
            }

            @Override
            public void onAllSelectorLoaded() {
                // 当所有的 Selector数据加载完毕后，触发下一级选择器的点击事件以更新对应的二级选择器的值
                for (KeyValuePair<String, ImageButtonView> kv : targetChain) {
                    ImageButtonView val = kv.value;

                    if (kv.key.contains(SELECTOR_FLAG_LEVEL_ONE)) {
                        val.setValue(val.getValue());
                    }
                }
            }
        };
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
     * 设置文件存储的相对路径
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
        setFormOnlyShow(true, null);
    }

    /**
     * 将所有内容显示为只读
     */
    public void setFormOnlyShow(boolean allShow, List<String> exceptFields) {
        showFormOnly = true;
        exceptFiledList = exceptFields;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.zs_event_report, container, false);

        try {
            this.mScrollView = (ScrollView) view.findViewById(R.id.mainScrollView);
            this.eventReportMainForm = (LinearLayout) view.findViewById(R.id.eventReportMainForm);
            this.contentFormView = (LinearLayout) view.findViewById(R.id.contentForm);

            setOnChangedKeyBoardListener();

            initView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return view;
    }

    public void refreshView() {
        final int currentScrollY = mScrollView.getScrollY();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.detach(FlowBeanFragment.this);
        ft.attach(FlowBeanFragment.this);
        ft.commit();
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollTo(0, currentScrollY);
            }
        }, 200);
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

        return eventReportMainForm.findViewById(viewId);
    }

    private ArrayList<String> getFilterFieldList() {
        if (filterCriteria != null) {
            Collection<String> values = filterFields.values();
            for (int i = 0; i < filterValues.size(); i++) {
                if (values.containsAll(filterValues.get(i))) {
                    return includeFields.get(i);
                }
            }
        }
        return null;
    }

    private void resetViewState() {
        controlIds.clear();
        asyncSelectorNames.clear();

        if (targetChain != null) {
            targetChain.clear();
        }
    }

    private void initView() {
        resetViewState();

        final ArrayList<String> includeFieldList = getFilterFieldList();


        for (int i = 0; i < data.Groups.length; i++) {

            final GDGroup group = data.Groups[i];

            LinearLayout groupTitleView = null;
            if (!BaseClassUtil.isNullOrEmptyString(group.Name)) {
                groupTitleView = newGroupTitleView(group.Name);
                eventReportMainForm.addView(groupTitleView);
            }

            int groupChildCount = 0;

            // 根据配置项对象生成表单项
            for (final GDControl control : group.Controls) {

                if (includeFieldList != null && includeFieldList.size() > 0 && !includeFieldList.contains(control.Name))
                    continue;

                groupChildCount++;

                // 选择器异步加载数据的处理逻辑
                if (("选择器".equals(control.Type) || "二级选择器".equals(control.Type)) && !control.isReadOnly()) {

                    asyncSelectorNames.put(control.Name, false);

                    control.setOnAsyncSelectorLoadFinishedListener(new OnAsyncSelectorLoadFinishedListener() {
                        @Override
                        public void onSingleSelectorLoaded(String fieldName, List<String> fieldValues) {

                            asyncSelectorNames.put(fieldName, true);

                            if (asyncSelectorNames.keySet().contains(fieldName)) {
                                internalOnAsyncSelectorLoadFinishedListener.onSingleSelectorLoaded(fieldName, fieldValues);
                                if (onAsyncSelectorLoadFinishedListener != null) {
                                    onAsyncSelectorLoadFinishedListener.onSingleSelectorLoaded(fieldName, fieldValues);
                                }

                                if (!asyncSelectorNames.values().contains(false)) {
                                    internalOnAsyncSelectorLoadFinishedListener.onAllSelectorLoaded();
                                    if (onAsyncSelectorLoadFinishedListener != null) {
                                        onAsyncSelectorLoadFinishedListener.onAllSelectorLoaded();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onAllSelectorLoaded() { // empty here
                        }
                    });
                }

                // TODO: 11/09/2017  Refine
                //region Some arguments transfer
                String relativePath = control.calRelativePath(fragmentFileRelativePath);
                control.setAdditionalParas(cls, addEnable, relativePath);
                if (!TextUtils.isEmpty(control.TriggerProblemValue)) {
                    control.onSelectedChangedListener = FlowBeanFragment.this;
                }
                //endregion

                MmtBaseView mmtBaseView = MmtBaseView.newInstance(getActivity(), control);
                actionListeners.add(mmtBaseView);
                View formView = mmtBaseView.generate();

                // 给 View 设置 Id
                do {
                    Integer newId = BaseClassUtil.generateViewId();
                    if (!controlIds.containsValue(newId)) {
                        formView.setId(newId);
                        controlIds.put(control.Name, newId);
                        break;
                    }
                } while (true);

                eventReportMainForm.addView(formView);

                if ("false".equals(control.IsVisible)) {
                    formView.setVisibility(View.GONE);
                }

                if (formView.getVisibility() == View.GONE) {
                    continue;
                }

                // 分割线
                ImageLineView lineView = new ImageLineView(getActivity());
                lineView.setTag(control.DisplayName);
                eventReportMainForm.addView(lineView);
            }

            if (groupChildCount == 0 && groupTitleView != null) {
                groupTitleView.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    private LinearLayout newGroupTitleView(String groupName) {
        LinearLayout titleView = new LinearLayout(getActivity());
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        titleView.setOrientation(LinearLayout.VERTICAL);
        int padding = DimenTool.dip2px(getActivity(), 6.0f);
        titleView.setPadding(DimenTool.dip2px(getActivity(), 10.0f), padding + 3, padding, padding);
        titleView.setBackgroundColor(Color.parseColor("#eaeaea"));

        TextView groupTitleText = new TextView(getActivity());
        groupTitleText.setText(groupName);
        groupTitleText.setTextSize(16.0f);
        titleView.addView(groupTitleText);

        return titleView;
    }

    public View findViewByType(String type) {
        View containview = null;
        for (int i = 0; i < eventReportMainForm.getChildCount(); i++) {
            View view = eventReportMainForm.getChildAt(i);

            // 若不是需要反馈的视图，则继续循环
            if (!(view instanceof FeedBackView)) {
                continue;
            }

            GDControl control = (GDControl) view.getTag();
            if (control.Type.contains(type)) {
                containview = view;
            }
            // 不严谨，有多个地址控件在一个界面会有问题
            if (control.Type.equals(type)) {
                return view;
            }
        }
        if (containview != null) {
            return containview;
        }
        return null;
    }

    public void updateFormData(Map<String, String> data) {
        if (data == null || data.size() == 0) {
            return;
        }

        for (int i = 0, count = eventReportMainForm.getChildCount(); i < count; i++) {
            View view = eventReportMainForm.getChildAt(i);
            if (!(view instanceof FeedBackView)) {
                continue;
            }

            GDControl control = (GDControl) view.getTag();
            if (data.containsKey(control.Name)) {
                String newValue = data.get(control.Name);
                ((FeedBackView) view).setValue(newValue);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            // Fragment创建完毕后提供一个接口
            if (beanFragmentOnCreate != null) {
                beanFragmentOnCreate.onCreated();
            }

            for (OnBeanFragmentActionListener listener : actionListeners)
                listener.onViewCreated(controlIds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getActivity().getIntent();

        for (OnBeanFragmentActionListener listener : actionListeners) {
            if (listener.onStart(intent))
                return;
        }
    }


    /**
     * 存储所有表单信息到获取的数据中
     *
     * @param status 操作状态:主要作用是在立即上报状态下，为必填项做检查
     * @return 反馈信息列表
     */
    public List<FeedItem> getFeedbackItems(final int status) {
        return getFeedbackItems(status, null, null);
    }

    public List<FeedItem> getFeedbackItems(int status, List<String> excludeTypes, List<String> includeFieldNames) {
        return BeanCacheManager.getFeedbackItems(this, status, excludeTypes, includeFieldNames);
    }

    private BeanFragmentOnCreate beanFragmentOnCreate;

    public void setBeanFragmentOnCreate(BeanFragmentOnCreate beanFragmentOnCreate) {
        this.beanFragmentOnCreate = beanFragmentOnCreate;
    }

    public interface BeanFragmentOnCreate {
        void onCreated();
    }

    private List<OnBeanFragmentActionListener> actionListeners = new ArrayList<>();

    // key:一级的FieldName，Value：
    public List<KeyValuePair<String, ImageButtonView>> targetChain = new ArrayList<>();

    @Override
    public void onDestroy() {
        try {
            EventBus.getDefault().unregister(this);

            for (OnBeanFragmentActionListener listener : actionListeners)
                listener.onDestroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        super.onDestroy();
    }

    //region Eventbus callback methods

    /**
     * 设备选择控件 触发的事件回调
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceSelected(DeviceSelectEvent deviceSelectEvent) {
        for (OnBeanFragmentActionListener listener : actionListeners)
            listener.onEventBusCallback(deviceSelectEvent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStationDeviceSelected(StationDeviceEvent stationDeviceEvent) {
        for (OnBeanFragmentActionListener listener : actionListeners)
            listener.onEventBusCallback(stationDeviceEvent);
    }
    //endregion


    @Override
    public void onSelectedChanged(GDControl gdControl, String selectedValue) {
        if (!TextUtils.isEmpty(gdControl.TriggerProblemValue) && gdControl.TriggerProblemValue.equals(selectedValue)) {
            if (selectedListener != null) {
                selectedListener.onExceptionValueSelected(gdControl.Name, gdControl.TriggerEvent, gdControl.TriggerEventFields);
            }
        }
    }

    private OnExceptionValueSelectedListener selectedListener;

    public void setExceptionValueSelectedListener(OnExceptionValueSelectedListener listener) {
        this.selectedListener = listener;
    }

    public interface OnExceptionValueSelectedListener {
        void onExceptionValueSelected(String controlName, String eventName, String eventFieldGroup);
    }

    private final Map<String, Boolean> asyncSelectorNames = new HashMap<>();

    private OnAsyncSelectorLoadFinishedListener onAsyncSelectorLoadFinishedListener;
    private final OnAsyncSelectorLoadFinishedListener internalOnAsyncSelectorLoadFinishedListener;

    /**
     * 设置异步加载数据的选择器（选择器/二级选择器）数据加载完毕的回调接口
     */
    public void setOnAsyncSelectorLoadFinishedListener(OnAsyncSelectorLoadFinishedListener listener) {
        this.onAsyncSelectorLoadFinishedListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnBeanFragmentActionListener listener : actionListeners) {
            if (listener.onActivityResult(requestCode, resultCode, data))
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}