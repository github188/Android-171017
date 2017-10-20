package com.customform.view;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 多值、下拉框
 * Created by zoro at 2017/9/1.
 */
class MmtMultiView extends MmtBaseView {
    MmtMultiView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_dropdown_radiobutton;
    }

    // class GetDefaultValuesTask
    private class GetDefaultValuesTask extends AsyncTask<String, Void, ResultData<String>> {
        private List<String> values;

        GetDefaultValuesTask(List<String> values) {
            this.values = values;
        }

        @Override
        protected ResultData<String> doInBackground(String... params) {
            try {
                String resultStr = NetUtil.executeHttpGet(
                        ServerConnectConfig.getInstance().getBaseServerPath()
                                + "/Services/MapgisCity_WorkFlowForm/REST/WorkFlowFormREST.svc/GetDefaultValuesBySQL",
                        "sql", params[0], "uid", MyApplication.getInstance().getUserId() + "");

                if (BaseClassUtil.isNullOrEmptyString(resultStr))
                    return null;

                return new Gson().fromJson(resultStr,
                        new TypeToken<ResultData<String>>() {
                        }.getType());

            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultData<String> result) {

            if (result == null) {
                return;
            }

            if (result.ResultCode < 0) {
                return;
            }
            this.values.addAll(result.DataList);
        }
    }

    /**
     * 创建多值类型视图
     */
    @Override
    public ImageButtonView build() {
        final List<String> values = new ArrayList<>();
        if (control.DefaultValues.toLowerCase().contains("select")) {
            new GetDefaultValuesTask(values).execute(control.DefaultValues);
        } else if (control.DefaultValues.contains(",")) {
            values.addAll(Arrays.asList(control.DefaultValues.split(",")));
        } else {
            values.add(control.DefaultValues);
        }

        final ImageButtonView view = new ImageButtonView(context);

        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());
        view.setValue(values.size() > 0 ? values.get(0) : "");

        if (control.Validate.equals("1")) {
            view.setRequired(true);
        }

        if (control.Value.length() != 0) {
            view.setValue(control.Value);
        }

        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDialogFragment fragment = new ListDialogFragment(
                        control.DisplayName, values);
                fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "");
                fragment.setListItemClickListener(new ListDialogFragment.OnListItemClickListener() {
                    @Override
                    public void onListItemClick(int arg2, String value) {
                        view.setValue(value);
                    }
                });
            }
        });

        return view;
    }
}
