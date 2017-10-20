package com.customform.view;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

/**
 * 时间、日期
 * Created by zoro at 2017/9/1.
 */
public class MmtServerDateView extends MmtBaseView {
    public MmtServerDateView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_date_time;
    }

    /**
     * 创建时间类型类型视图,该视图时间从服务端获取
     */
    @Override
    public ImageButtonView build() {
        final ImageButtonView view = new ImageButtonView(context);
        view.setTag(this.control);
        view.setKey(this.control.DisplayName);
        view.setImage(getIconRes());

        if (this.control.Validate.equals("1")) {
            view.setRequired(true);
        }

        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<String, String, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        String url = ServerConnectConfig.getInstance()
                                .getBaseServerPath()
                                + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/SystemCurrentTime";

                        return NetUtil.executeHttpGet(url, "");
                    }

                    @Override
                    protected void onPostExecute(String result) {

                        if (BaseClassUtil.isNullOrEmptyString(result))
                            return;

                        ResultData<String> resultData = new Gson().fromJson(
                                result, new TypeToken<ResultData<String>>() {
                                }.getType());

                        if (resultData.ResultCode < 0)
                            return;

                        String timeStr = resultData.getSingleData();

                        String[] timeArr = timeStr.split(" ");

                        if (timeArr.length != 2) {
                            return;
                        }

                        if ("仅时间_不可选择".equals(control.ConfigInfo)) {
                            timeStr = timeArr[1];
                        }

                        if ("仅日期_不可选择".equals(control.ConfigInfo)) {
                            timeStr = timeArr[0];
                        }

                        if (!BaseClassUtil.validateDateValue(timeStr)) {
                            timeStr = "";
                        }
                        view.setValue(timeStr);
                    }
                }.executeOnExecutor(MyApplication.executorService);
            }
        });


        if (control.Value.length() != 0) {
            String timeStr = control.Value;
            String[] timeArr = control.Value.split(" ");

            if (timeArr.length == 2) {
                if ("仅时间_不可选择".equals(control.ConfigInfo)) {
                    timeStr = timeArr[1];
                }

                if ("仅日期_不可选择".equals(control.ConfigInfo)) {
                    timeStr = timeArr[0];
                }
            }

            if (!BaseClassUtil.validateDateValue(timeStr)) {
                timeStr = "";
            }
            view.setValue(timeStr);
        } else {
            if (!control.isReadOnly()) {
                view.getButton().performClick();
            }
        }

        return view;
    }

    @Override
    protected View buildReadonlyView() {
        ImageTextView view = new ImageTextView(context);
        view.setTag(control);
        view.setKey(control.DisplayName);
        view.setImage(getIconRes());

        String defaultValue = getDefaultValue();
        view.setValue(BaseClassUtil.validateDateValue(defaultValue) ? defaultValue : EMPTY_STRING);
        return view;
    }
}
