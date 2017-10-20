package com.repair.beihai.poj.hbpoj.module.userwatermeter;

import android.text.TextUtils;
import android.view.View;

import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.module.FlowBeanFragment;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2016/8/25.
 */
public class UserWatermeterEditActivity extends TableOneRecordActivity {

    private String defaulteditFileds = "表身号,新水表行度,水表产地,水表安装人员,纵坐标,横坐标,装表日期,备注,领表时间,装表状态,是否立即供水,用户确认,水表坐标";

   // private String containsGroups = "工程信息,用户信息,水表信息";

    @Override
    protected void initView() {

        String editFileds = MyApplication.getInstance().getConfigValue("EditWatermeterfields");
        if (TextUtils.isEmpty(editFileds)) {
            editFileds = defaulteditFileds;
        }
        if (!TextUtils.isEmpty(editFileds)) {
            GDFormBean gdFormBean = getGdFormBean();

            if (gdFormBean == null) {
                return;
            }

            List<GDGroup> groups = new ArrayList<>();

            for (GDGroup gdGroup : gdFormBean.Groups) {

//                if (!containsGroups.contains(gdGroup.Name)) {
//                    continue;
//                }

                if("审核信息".equals(gdGroup.Name)){
                    continue;
                }
                groups.add(gdGroup);

            }

            gdFormBean.Groups = groups.toArray(new GDGroup[groups.size()]);


            for (GDGroup gdGroup : gdFormBean.Groups) {

                for (GDControl gdControl : gdGroup.Controls) {
                    gdControl.setReadOnly(true);
                    if (editFileds.contains(gdControl.Name)) {
                        gdControl.setReadOnly(false);
                    }
                }

            }

        }

        super.initView();

        final String ID = getIntent().getStringExtra("ID");
        if (TextUtils.isEmpty(ID)) {
            return;
        }

        flowBeanFragment.setBeanFragmentOnCreate(new FlowBeanFragment.BeanFragmentOnCreate() {
            @Override
            public void onCreated() {
                final View view = flowBeanFragment.findViewByName("施工队");

                if (view == null) {
                    return;
                }
                new MmtBaseTask<Void, Void, String>(UserWatermeterEditActivity.this) {
                    @Override
                    protected String doInBackground(Void... params) {

                        StringBuilder sb = new StringBuilder(ServerConnectConfig.getInstance().getBaseServerPath());
                        sb.append("/Services/MapgisCity_ProjectManage_BH/REST/ProjectManageREST.svc/ProjectManage/GetSGD?ID=")
                                .append(ID);

                        return NetUtil.executeHttpGet(sb.toString());

                    }

                    @Override
                    protected void onSuccess(String s) {
                        super.onSuccess(s);

                        if (!TextUtils.isEmpty(s)) {
                            s= BaseClassUtil.trim(s,"\"");
                            if (view instanceof ImageTextView) {
                                ImageTextView imageTextView = (ImageTextView) view;
                                imageTextView.setValue(s);
                            }

                            if (view instanceof ImageEditView) {
                                ImageEditView imageEditView = (ImageEditView) view;
                                imageEditView.setValue(s);
                            }

                        }
                    }
                }.mmtExecute();

            }
        });
    }

}
