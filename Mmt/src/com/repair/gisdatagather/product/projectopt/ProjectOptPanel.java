package com.repair.gisdatagather.product.projectopt;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.gisdatagather.common.entity.TextDot;
import com.repair.gisdatagather.product.gisgather.GisGather;
import com.repair.zhoushan.common.Utils;

/**
 * Created by liuyunfan on 2016/5/4.
 */
public class ProjectOptPanel implements View.OnClickListener, CheckBox.OnCheckedChangeListener, ProjectOptInterface {

    public View view;
    private GisGather gisGather;

    public ProjectOptPanel(GisGather gisGather, View view) {
        this.gisGather = gisGather;
        this.view = view;
    }

    public void initProjectOptPanel() {
        view.findViewById(R.id.reset).setOnClickListener(this);
        view.findViewById(R.id.delPoj).setOnClickListener(this);
        view.findViewById(R.id.submit).setOnClickListener(this);

        View todayGather = view.findViewById(R.id.todayGather);
        if (todayGather instanceof CheckBox) {
            CheckBox cb = (CheckBox) todayGather;
            cb.setOnCheckedChangeListener(this);
            cb.setChecked(false);
        }

        View autoLinkPoint = view.findViewById(R.id.autoLinkPoint);
        if (autoLinkPoint instanceof CheckBox) {
            CheckBox cb = (CheckBox) autoLinkPoint;
            cb.setOnCheckedChangeListener(this);
            cb.setChecked(false);
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (gisGather.mapView == null || gisGather.mapView.getMap() == null) {
            gisGather.mapGISFrame.stopMenuFunction();
            return;
        }
        gisGather.mapView.setTapListener(null);
        String text = buttonView.getText().toString();
        switch (text) {
            case "高亮今日":
            case "今日采集":
            case "今日": {
                if (!isChecked) {
                    break;
                }
                lightTodayGisData();
            }
            break;
            case "自动连线": {
                autoLinkLine(isChecked);
            }
            break;
            default: {
                Toast.makeText(gisGather.mapGISFrame, "未知异常", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (gisGather.mapView == null || gisGather.mapView.getMap() == null) {
            gisGather.mapGISFrame.stopMenuFunction();
            return;
        }
        gisGather.mapView.setTapListener(null);
        String text = ((TextView) v).getText().toString();
        switch (text) {
            case "清零":
            case "清空": {
                clearProject();
            }
            break;
            case "删除工程": {
                delProject();
            }
            break;
            case "提交": {
                submitProject();
            }
            break;

            default: {
                Toast.makeText(gisGather.mapGISFrame, "未知异常", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void delProject() {
        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定删除工程?");
        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                GisGather.gisDataProject.deletePoj(gisGather.mapGISFrame, new MmtBaseTask.OnWxyhTaskListener() {
                    @Override
                    public void doAfter(Object o) {

                        if (o == null) {
                            return;
                        }
                        ResultWithoutData resultWithoutData = Utils.resultWithoutDataJson2ResultDataToast(gisGather.mapGISFrame, o.toString(), "工程删除失败", "工程删除成功");
                        if (resultWithoutData == null) {
                            return;
                        }

                        gisGather.mapGISFrame.findViewById(R.id.mapviewClear)
                                .performClick();
                        gisGather.restroeMapFrame();
                        gisGather.mapGISFrame.backByReorder(true);
                    }
                });
            }
        });
        okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
    }

    @Override
    public void clearProject() {
        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("该操作将清空此工程采集的所有gis数据，是否继续？");
        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                GisGather.gisDataProject.resetPoj(gisGather.mapGISFrame);
                gisGather.hasCatchTextDots.clear();
                gisGather.gisOpt.autoLinkLinehasCatchTextDots.clear();
            }
        });
        okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");
    }

    @Override
    public void submitProject() {
        OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("确定提交?");
        okCancelDialogFragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
            @Override
            public void onRightButtonClick(View view) {
                GisGather.gisDataProject.handoverrPoj(gisGather);
            }
        });
        okCancelDialogFragment.show(gisGather.mapGISFrame.getSupportFragmentManager(), "");

    }

    @Override
    public void lightTodayGisData() {
        GisGather.gisDataProject.getTodayGISData().lightToday(gisGather.mapView);
    }

    @Override
    public void autoLinkLine(boolean isAutoLinkLine) {
        if (isAutoLinkLine) {
            if (TextUtils.isEmpty(gisGather.layerShow.uniquenessLineLayer)) {
                MyApplication.getInstance().showMessageWithHandle("无法自动连线：没有管线或管线大于一种");
                isAutoLinkLine = false;
            }

            TextDot textDot = GisGather.gisDataProject.getPojLastDot();

            if (textDot != null) {
                gisGather.gisOpt.autoLinkLinehasCatchTextDots.clear();
            }

            //若上一操作是添点操作，需要将上一步添加的点作为自动连线的起点
            if (GisGather.gisDataProject.getHasAddDoted()) {
                gisGather.gisOpt.autoLinkLinehasCatchTextDots.add(textDot);
            }

        } else {
            gisGather.gisOpt.autoLinkLinehasCatchTextDots.clear();
        }
        gisGather.isAutoLinkLine = isAutoLinkLine;

    }
}
