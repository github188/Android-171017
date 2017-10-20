package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.maintainproduct.entity.GDFormBean;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.common.widget.customview.ImageTextView;
import com.mapgis.mmt.entity.NetLogInfo;
import com.repair.zhoushan.module.FlowBeanFragment;

import java.util.List;

public class RequestDetailActivity extends BaseActivity {
    private NetLogInfo netLogInfo;
    private FlowBeanFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void setDefaultContentView() {
        super.setDefaultContentView();

        getIntentData();
        getBaseTextView().setText("详细信息");
        fragment = new FlowBeanFragment();
        Bundle args = new Bundle();
        GDFormBean data = GDFormBean.generateSimpleForm(
                new String[]{"DisplayName", "请求编号", "Name", "请求编号", "Type", "短文本", "Validate", "1", "Value", String.valueOf(netLogInfo.id)},
                new String[]{"DisplayName", "请求类型", "Name", "请求类型", "Type", "短文本", "Validate", "1", "Value", netLogInfo.requestType},
                new String[]{"DisplayName", "请求接口", "Name", "请求编号", "Type", "短文本", "Validate", "1", "Value", netLogInfo.requestInterface},
                new String[]{"DisplayName", "请求状态", "Name", "请求状态", "Type", "是否", "Validate", "1", "Value", netLogInfo.isSuccess == 1 ? "成功" : "失败"},
                new String[]{"DisplayName", "响应状态", "Name", "响应状态", "Type", "短文本", "Validate", "1", "Value", String.valueOf(netLogInfo.responseCode)},
                new String[]{"DisplayName", "开始时间", "Name", "开始时间", "Type", "时间", "Validate", "1", "Value", netLogInfo.startTime},
                new String[]{"DisplayName", "结束时间", "Name", "结束时间", "Type", "时间", "Validate", "1", "Value", netLogInfo.endTime},
                new String[]{"DisplayName", "请求耗时", "Name", "请求耗时", "Type", "仅时间", "Validate", "1", "Value", netLogInfo.timeSpan + "ms"},
                new String[]{"DisplayName", "发送流量", "Name", "发送流量", "Type", "短文本", "Validate", "1", "Value", formatTraffic(netLogInfo.sendBytes)},
                new String[]{"DisplayName", "接收流量", "Name", "接收流量", "Type", "短文本", "Validate", "1", "Value", formatTraffic(netLogInfo.receiveBytes)},
                new String[]{"DisplayName", "平均网速", "Name", "平均网速", "Type", "短文本", "Validate", "1", "Value", netLogInfo.speed + "KB/s"},
                new String[]{"DisplayName", "请求路径", "Name", "请求路径", "Type", "长文本", "Validate", "1", "Value", netLogInfo.fullURL}
        );
        args.putParcelable("GDFormBean", data);
        fragment.setArguments(args);
        fragment.setFormOnlyShow();

        addFragment(fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();

        View viewURL = fragment.findViewByName("请求路径");
        if (viewURL instanceof ImageTextView) {

            final ImageTextView view = (ImageTextView) viewURL;
            view.getValueTextView().setTextColor(Color.BLUE);
            view.getValueTextView().getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
            view.getValueTextView().getPaint().setAntiAlias(true);//抗锯齿
            view.getValueTextView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(view.getValueTextView().getText().toString()));
                    startActivity(intent);
                }
            });

        }
    }

    private void getIntentData() {

        Intent intent = getIntent();
        NetLogInfo info = intent.getParcelableExtra("data");
        if (info != null) {
            netLogInfo=info;
        }
    }

    private String formatTraffic(long data) {
        if (data < 0) {
            return "0Byte";
        } else {
            if (data > 1000) {
                return Convert.FormatDouble(data / 1000f, ".00") + "KB";
            } else {
                return data + "Byte";
            }
        }
    }

}
