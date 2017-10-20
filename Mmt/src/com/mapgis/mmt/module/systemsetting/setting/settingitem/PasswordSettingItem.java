package com.mapgis.mmt.module.systemsetting.setting.settingitem;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.systemsetting.setting.BaseMoreSettingItem;
import com.mapgis.mmt.global.MmtBaseTask;

/**
 * Created by Comclay on 2017/6/14.
 * 修改密码
 */

public class PasswordSettingItem extends BaseMoreSettingItem {

    public PasswordSettingItem(Context context, View itemView) {
        super(context, itemView);
    }

    @Override
    public void init() {

    }

    @Override
    public void save() {

    }

    @Override
    public void performClicked() {
        try {
            final EditText txtPwd = new EditText(mContext);

            txtPwd.setLines(1);
            txtPwd.setBackgroundResource(R.drawable.edit_text_default);

            OkCancelDialogFragment fragment = new OkCancelDialogFragment("请输入新密码", txtPwd);

            fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    try {
                        String pwd = txtPwd.getText().toString();

                        if (TextUtils.isEmpty(pwd)) {
                            Toast.makeText(mContext, "密码不能为空", Toast.LENGTH_SHORT).show();

                            return;
                        }

                        new MmtBaseTask<String, Integer, ResultWithoutData>(mContext) {
                            @Override
                            protected ResultWithoutData doInBackground(String... params) {
                                try {
                                    String url = ServerConnectConfig.getInstance().getMobileBusinessURL() + "/BaseREST.svc/ChangePwd";
                                    String userID = String.valueOf(MyApplication.getInstance().getUserId());

                                    String json = NetUtil.executeHttpGet(url, "userID", userID, "pwd", params[0]);

                                    return new Gson().fromJson(json, ResultWithoutData.class);
                                } catch (Exception ex) {
                                    ex.printStackTrace();

                                    return null;
                                }
                            }

                            @Override
                            protected void onSuccess(ResultWithoutData data) {
                                try {
                                    if (data == null)
                                        Toast.makeText(mContext, "修改密码失败，可能服务不存在", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(mContext, data.ResultMessage, Toast.LENGTH_SHORT).show();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }.mmtExecute(pwd);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            fragment.show(((FragmentActivity)mContext).getSupportFragmentManager(), "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
