package com.repair.beihai.poj.hbpoj.module.construct;

import android.os.Bundle;
import android.text.TextUtils;

import com.maintainproduct.entity.FeedItem;
import com.repair.zhoushan.module.casemanage.casedetail.FeebReportActivity;

import java.util.List;

/**
 * Created by liuyunfan on 2016/7/22.
 */
public class ConstructReportActivity extends FeebReportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //管道施工进度，户表施工进度
        if(!bizName.contains("施工进度")){
            return;
        }

        validateListener = new ValidateListener() {
            @Override
            public boolean isValidated(List<FeedItem> feedbackItems) {
                try {
                    if (feedbackItems == null) {
                        showErrorMsg("反馈项不能为空");
                        return false;
                    }

                    for (FeedItem fi : feedbackItems) {
                        String name = fi.Name;
                        String val = fi.Value;
                        if ("施工进度百分比".equals(name)) {

                            if(TextUtils.isEmpty(val)){
                                showErrorMsg("施工进度百分比不能为空");
                                return false;
                            }

                            int rate=Integer.parseInt(val);

                            if(rate>100){
                                showErrorMsg("施工进度百分比不能大于100");
                                return false;
                            }
                            if(rate<0){
                                showErrorMsg("施工进度百分比不能小于0");
                                return false;
                            }
                        }

                        if ("施工长度".equals(name)) {

                            if(TextUtils.isEmpty(val)){
                                showErrorMsg("施工长度不能为空");
                                return false;
                            }

                            int len=Integer.parseInt(val);

                            if(len<0){
                                showErrorMsg("施工长度不能小于0");
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    showErrorMsg(e.getMessage());
                    return false;
                }
                return true;
            }
        };
    }



}
