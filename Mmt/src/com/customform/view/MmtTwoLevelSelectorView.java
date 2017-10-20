package com.customform.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;

import com.customform.entity.KeyValuePair;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.DataDictNode;
import com.maintainproduct.entity.DataDictParentNode;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListCheckBoxDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.repair.zhoushan.module.FlowBeanFragment.SELECTOR_FLAG_LEVEL_ONE;

/**
 * 二级选择器
 * Created by zoro at 2017/9/1.
 */
class MmtTwoLevelSelectorView extends MmtBaseView {

    MmtTwoLevelSelectorView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_cascade_selector;
    }

    /**
     * 二级选择器
     */
    public View build() {
        // 二级选择器的一级构造（与普通的选择器相同）
        if (!control.ConfigInfo.contains(".")) {
            View view = new MmtMultiTwoView(context, control).build();
            if (view instanceof ImageButtonView) {
                ImageButtonView imageButtonView = (ImageButtonView) view;
                imageButtonView.setImage(getIconRes());
                init(imageButtonView);
            }
            return view;
        }

        // 二级选择器的二级构造
        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        final String[] configInfos = control.ConfigInfo.split("\\.");
        // 默认单选，当二级 ConfigInfo中最后为 n时，二级为多选 (事件类型.维修工单上报内容.n)
        final boolean isSingleMode = configInfos.length < 3 || !"n".equalsIgnoreCase(configInfos[2].trim());

        new GetLevelTwoDictValuesTask(context, false, new MmtBaseTask.OnWxyhTaskListener<HashMap<String, List<String>>>() {
            @Override
            public void doAfter(HashMap<String, List<String>> stringsList) {

                try {
                    if (stringsList == null) return;

                    // 用 GDControl 的 mTag字段保存二级的所有可选组数据
                    control.mTag = stringsList;

                    List<String> initialSet = null;
                    if (stringsList.size() > 0) {
                        initialSet = stringsList.get(stringsList.keySet().iterator().next());
                    }

                    if (!TextUtils.isEmpty(control.Value)) {
                        view.setValue(control.Value);
                    } else if (!TextUtils.isEmpty(control.DefaultValues)) {
                        view.setValue(control.DefaultValues);
                    } else {
                        if (initialSet != null && initialSet.size() > 0) {
                            view.setValue(initialSet.get(0));
                        } else {
                            view.setValue("");
                        }
                    }

                    // 用 ImageButton的 Tag保存当前列表数据，一级值变化后该二级更新也只是更新该 Tag值
                    ImageButton imageButton = view.getButton();
                    if (initialSet != null) {
                        imageButton.setTag(initialSet);
                    }

                    imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object obj = v.getTag();
                            if (obj != null && obj instanceof List) {

                                ArrayList<String> defValueList = new ArrayList<>();
                                String val = view.getValue();
                                if (!BaseClassUtil.isNullOrEmptyString(val)) {
                                    defValueList.addAll(BaseClassUtil.StringToList(val, ","));
                                }

                                ArrayList<String> dataList = new ArrayList<>((List<String>) obj);
                                ListCheckBoxDialogFragment fragment
                                        = ListCheckBoxDialogFragment.newInstance(control.DisplayName, dataList, defValueList);
                                fragment.setSingleMode(isSingleMode);
                                fragment.setDisallowEmpty(true);
                                fragment.show(getActivity().getSupportFragmentManager(), "");
                                fragment.setOnRightButtonClickListener(new ListCheckBoxDialogFragment.OnRightButtonClickListener() {
                                    @Override
                                    public void onRightButtonClick(View v, List<String> selectedItems) {
                                        String selectedStr = BaseClassUtil.listToString(selectedItems);
                                        view.setValue(selectedStr);
                                    }
                                });
                            }
                        }
                    });
                } finally {
                    if (control.onAsyncSelectorLoadFinishedListener != null) {
                        // 本该为二级选择器的二级单独添加一个回调方法，但一般用不到二级数据的处理，固含糊处理
                        control.onAsyncSelectorLoadFinishedListener.onSingleSelectorLoaded(control.Name, null);
                    }
                }
            }
        }).mmtExecute(configInfos[1]);

        init(view);

        return view;
    }

    private void init(ImageButtonView selectorView) {
        if (control.isReadOnly())
            return;

        final GDControl gdControl = (GDControl) selectorView.getTag();

        final FlowBeanFragment beanFragment = getBeanFragment();

        if (gdControl.ConfigInfo.contains(".")) {
            // 二级(注册监听)
            beanFragment.targetChain.add(new KeyValuePair<>(gdControl.ConfigInfo.split("\\.")[0], selectorView));
        } else {
            beanFragment.targetChain.add(new KeyValuePair<>(gdControl.Name + SELECTOR_FLAG_LEVEL_ONE, selectorView));
            // 一级（绑定触发监听器）
            selectorView.getValueTextView().addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {

                    String newValue = s.toString();

                    GDControl control;
                    Object obj;
                    for (KeyValuePair<String, ImageButtonView> keyValuePair : beanFragment.targetChain) {
                        if (keyValuePair.key.equals(gdControl.Name)) {

                            control = (GDControl) keyValuePair.value.getTag();
                            obj = control.getTag();
                            if (obj != null && obj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                List<String> valueList = ((HashMap<String, List<String>>) obj).get(newValue);
                                keyValuePair.value.getButton().setTag(valueList);
                                if (valueList != null && valueList.size() > 0) {
                                    // 二级选择器的二级存在多选，选中值逗号分隔
                                    String level2OldValues = keyValuePair.value.getValue();
                                    List<String> level2OldValueList = BaseClassUtil.StringToList(level2OldValues, ",");
                                    // 当文本框中的值不是新的二级数据列表的子集，则更新文本框中的值，默认选中第一项
                                    if (!valueList.containsAll(level2OldValueList) || level2OldValueList.size() == 0) {
                                        keyValuePair.value.setValue(valueList.get(0));
                                    }
                                } else {
                                    keyValuePair.value.setValue("");
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 用于获取单个数据字典的值
     */
    private class GetLevelTwoDictValuesTask extends MmtBaseTask<String, Void, HashMap<String, List<String>>> {

        GetLevelTwoDictValuesTask(Context context, boolean showLoading, OnWxyhTaskListener<HashMap<String, List<String>>> listener) {
            super(context, showLoading, listener);
        }

        @Override
        protected HashMap<String, List<String>> doInBackground(String... params) {

            ArrayList<DataDictParentNode> resultList;
            HashMap<String, List<String>> resultData = null;

            try {
                String resultStr = NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/MapgisCity_WorkFlow/REST/WorkFlowREST.svc/GetDataDictionaryList"
                        , "nodeNameArr", params[0]);

                if (BaseClassUtil.isNullOrEmptyString(resultStr))
                    return null;

                resultList = new Gson().fromJson(resultStr, new TypeToken<ArrayList<DataDictParentNode>>() {
                }.getType());

                if (resultList == null || resultList.size() < 1) {
                    return null;
                }

                DataDictParentNode dataDictParentNode = resultList.get(0);

                if (dataDictParentNode.childList.size() > 0) {

                    resultData = new LinkedHashMap<>();

                    List<String> curNodeValueList = new LinkedList<>();
                    String curNodeName = dataDictParentNode.childList.get(0).NODENAME.trim();

                    for (DataDictNode dictNode : dataDictParentNode.childList) {

                        if (dictNode.NODENAME.trim().equals(curNodeName)) {
                            curNodeValueList.add(dictNode.NODEVALUE);
                        } else {

                            if (resultData.containsKey(curNodeName)) {
                                resultData.get(curNodeName).addAll(curNodeValueList);
                            } else {
                                resultData.put(curNodeName, curNodeValueList);
                            }

                            curNodeName = dictNode.NODENAME.trim();
                            curNodeValueList = new LinkedList<>();
                            curNodeValueList.add(dictNode.NODEVALUE);
                        }
                    }
                    if (resultData.containsKey(curNodeName)) {
                        resultData.get(curNodeName).addAll(curNodeValueList);
                    } else {
                        resultData.put(curNodeName, curNodeValueList);
                    }
                }
                return resultData;

            } catch (Exception ex) {
                return null;
            }
        }
    }
}
