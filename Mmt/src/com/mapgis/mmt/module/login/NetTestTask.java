package com.mapgis.mmt.module.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.global.MmtBaseTask;

import javax.net.ssl.SSLHandshakeException;

public class NetTestTask extends MmtBaseTask<String, Integer, String> {
    private Fragment fragment;
    public boolean isSuccess = false;
    private boolean showSuccessTip = true;

    NetTestTask(Fragment fragment, boolean showSuccessTip) {
        super(new ContextThemeWrapper(fragment.getContext(), R.style.MmtBaseThemeAlertDialog));

        this.fragment = fragment;
        this.showSuccessTip = showSuccessTip;
    }

    public NetTestTask(Fragment fragment) {
        this(fragment, true);
    }

    @Override
    protected String doInBackground(String... params) {
        String err = "";
        String host = "";

        try {

            if (!NetUtil.testNetState())
                return "网络连接失败";

            host = params[0];

            if (!host.endsWith("://"))
                host += "://";

            host += params[1] + ":" + params[2] + "/" + params[3] + "/";

            //String url = host + "Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/TestDB";

            String url = "https://pipenet.enn.cn:8000/enn/cityinterface/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc/TestDB";

            String result = NetUtil.executeHttpGet(10, url);

            if (!TextUtils.isEmpty(result)) {
                ResultWithoutData data = new Gson().fromJson(result, ResultWithoutData.class);

                isSuccess = data.ResultCode > 0;

                return data.ResultMessage;
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            err = "[" + host + "]\n\n[" + ex + "]";

            if (ex instanceof SSLHandshakeException)
                err = "[可能手机时间错误]\n" + err;
            else if (ex instanceof IllegalArgumentException)
                err = "[可能包含特殊字符，如：空格、回车]\n" + err;
            else
                err = "[测试连接失败]\n" + err;
        }

        return TextUtils.isEmpty(err) ? "连接失败，请检查网络设置！" : err;
    }

    @Override
    protected void onSuccess(String msg) {
        try {
            if (isSuccess) {
                if (showSuccessTip)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            } else if (fragment != null && fragment.isVisible()) {
                FragmentActivity activity = fragment.getActivity();
                ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
                LayoutInflater layoutInflater = activity.getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.content_snackbar, rootView, false);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                        DimenTool.dip2px(activity,56),DimenTool.dip2px(activity,rootView.getMeasuredWidth())
                );
                view.setLayoutParams(params);
                View tv = view.findViewById(R.id.tv_right);
                final PopupWindow pw = new PopupWindow(view
                        , WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enterNetCheckActivity();
                        pw.dismiss();
                    }
                });

                pw.setContentView(view);
                pw.setBackgroundDrawable(null);
                pw.setAnimationStyle(R.style.PopupBottomAnimation);
                pw.setOutsideTouchable(true);
                pw.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*进入网络自检页面*/
    private void enterNetCheckActivity() {
        Intent intent = new Intent(fragment.getActivity(), NetCheckActivity.class);
        fragment.getActivity().startActivity(intent);
        MyApplication.getInstance().startActivityAnimation(fragment.getActivity());
    }

    public ProgressDialog getLoadingDialog() {
        return this.loadingDialog;
    }
}
