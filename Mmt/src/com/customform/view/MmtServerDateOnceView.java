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
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultData;

/**
 * 时间v2
 * Created by zoro on 2017/9/1.
 */
public class MmtServerDateOnceView extends MmtBaseView {

    public MmtServerDateOnceView(Context context, GDControl control) {
        super(context, control);
    }

    @Override
    protected int getIconRes() {
        return R.drawable.form_date_time;
    }

    /**
     * 创建时间类型类型视图,该视图时间从服务端获取,且只获取一次
     */
    public ImageButtonView build() {
        final ImageButtonView view = new MmtServerDateView(context, control).build();

        view.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BaseClassUtil.isNullOrEmptyString(view.getValueTextView().getText().toString())) {
                    new AsyncTask<String, String, String>() {
                        @Override
                        protected String doInBackground(String... params) {
                            String url = ServerConnectConfig.getInstance()
                                    .getBaseServerPath()
                                    + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/SystemCurrentTime";

                            String result = NetUtil.executeHttpGet(url, "");
                            return result;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            ResultData<String> resultData = new Gson()
                                    .fromJson(result, new TypeToken<ResultData<String>>() {
                                            }.getType());
                            view.setValue(resultData.getSingleData());
                        }
                    }.executeOnExecutor(MyApplication.executorService);
                }
            }
        });

        return view;
    }

    @Override
    protected View buildReadonlyView() {
        return new MmtServerDateView(context, control).buildReadonlyView();
    }
}
