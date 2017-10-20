package com.customform.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.DataDictNode;
import com.maintainproduct.entity.DataDictParentNode;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择器
 * Created by zoro at 2017/9/1.
 */
class MmtMultiTwoView extends MmtBaseView {

    MmtMultiTwoView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_radiobutton;
    }

    /**
     * 用于获取单个数据字典的值
     */
    private class GetLevelOneDictValuesTask extends MmtBaseTask<String, Void, List<String>> {

        GetLevelOneDictValuesTask(Context context, boolean showLoading, OnWxyhTaskListener<List<String>> listener) {
            super(context, showLoading, listener);
        }

        @Override
        protected List<String> doInBackground(String... params) {

            ArrayList<DataDictParentNode> resultList;
            List<String> resultData = null;

            try {
                String resultStr = NetUtil.executeHttpGet(ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/MapgisCity_WorkFlow/REST/WorkFlowREST.svc/GetDataDictionaryList"
                        , "nodeNameArr", params[0]);

                if (BaseClassUtil.isNullOrEmptyString(resultStr))
                    return null;

                resultList = new Gson().fromJson(resultStr,
                        new TypeToken<ArrayList<DataDictParentNode>>() {
                        }.getType());

                if (resultList == null || resultList.size() < 1) {
                    return null;
                }

                DataDictParentNode dataDictParentNode = resultList.get(0);

                if (dataDictParentNode.childList.size() > 0) {
                    resultData = new ArrayList<>();

                    for (DataDictNode dictNode : dataDictParentNode.childList) {
                        // resultData.add(dictNode.NODEVALUE);
                        resultData.add(dictNode.NODENAME);
                    }
                }

                return resultData;

            } catch (Exception ex) {
                return null;
            }
        }
    }

    public View build() {

        final ImageButtonView view = new ImageButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        new GetLevelOneDictValuesTask(context, false, new MmtBaseTask.OnWxyhTaskListener<List<String>>() {
            @Override
            public void doAfter(final List<String> strings) {

                try {
                    if (strings == null) {
                        return;
                    }
                    final List<String> values = new ArrayList<>(strings);

                    String defaultValue = getDefaultValue();
                    view.setValue(!TextUtils.isEmpty(defaultValue) ? defaultValue :
                            (values.size() > 0 ? values.get(0) : ""));

                    view.getButton().setTag(values);
                    view.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ListDialogFragment fragment = new ListDialogFragment(control.DisplayName, values);
                            fragment.show(getActivity().getSupportFragmentManager(), "");
                            fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int arg2, String value) {
                                    view.setValue(value);
                                    if (control.onSelectedChangedListener != null) {
                                        control.onSelectedChangedListener.onSelectedChanged(control, value);
                                    }
                                }
                            });
                        }
                    });
                } finally {
                    if (control.onAsyncSelectorLoadFinishedListener != null) {
                        control.onAsyncSelectorLoadFinishedListener.onSingleSelectorLoaded(control.Name, strings);
                    }
                }
            }
        }).mmtExecute(control.ConfigInfo);

        return view;
    }
}
