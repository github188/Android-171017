package com.repair.beihai.poj.hbpoj.module.userwatermeter;

import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.repair.zhoushan.module.tablecommonhand.TableOneRecordActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lyunfan on 16/8/31.
 */
public class UserWatermeterPojDetail extends TableOneRecordActivity {
    @Override
    protected void initView() {

        GDFormBean gdFormBean = getGdFormBean();

        if (gdFormBean == null) {
            return;
        }

        List<GDGroup> groups=new ArrayList<>();


        String containGroupNames="报装申请信息,工程基本信息,施工信息";

        for (GDGroup gdGroup : gdFormBean.Groups) {

            if(!containGroupNames.contains(gdGroup.Name)){
               continue;
            }

            groups.add(gdGroup);

        }

        gdFormBean.Groups=groups.toArray(new GDGroup[groups.size()]);

        super.initView();
    }
}
