package com.repair.beihai.poj.hbpoj.module.userwaterecheck;

import android.text.TextUtils;

import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.mapgis.mmt.MyApplication;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;

/**
 * Created by liuyunfan on 2016/8/25.
 */
public class UserWatermeterCheckEditActivity extends TableOneRecordActivity {

    //private String editFileds ="表位合理性,表身号一致性,用户性质相符,重复装表审核,表径相符性,表后管错乱,户表审核说明,审核状态";

    //private String containsGroups="工程信息,用户信息,水表信息";

    @Override
    protected void initView() {

        String editFileds = MyApplication.getInstance().getConfigValue("EditWatermeterfields");
        if (!TextUtils.isEmpty(editFileds)) {
            GDFormBean gdFormBean = getGdFormBean();

            if (gdFormBean == null) {
                return;
            }

//            List<GDGroup> groups=new ArrayList<>();
//
//            for (GDGroup gdGroup : gdFormBean.Groups) {
//
//                if(!containsGroups.contains(gdGroup.Name)){
//                    continue;
//                }
//
//                groups.add(gdGroup);
//
//            }
//
//            gdFormBean.Groups=groups.toArray(new GDGroup[groups.size()]);


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
    }
}
