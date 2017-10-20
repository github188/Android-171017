package com.repair.zhoushan.module.projectmanage.threepartconstruct.caregiver;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maintainproduct.entity.GDControl;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;


public class PopActivity extends FragmentActivity {

    private Button okButton;
    private Button cancelButton;
    private CaseItem caseItem;
    private String title;

    ImageEditView edit1View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.ok_cancel_dialog);

        title = getIntent().getStringExtra("title");
        caseItem = getIntent().getParcelableExtra("caseItem");

        AppManager.addActivity(this);

        initView();
        initListener();
    }

    private void initView() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_ok_cancel_dialog_content);
        layout.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tv_ok_cancel_dialog_Tips)).setText(title);
        okButton = (Button) findViewById(R.id.btn_ok);
        cancelButton = (Button) findViewById(R.id.btn_cancel);


        switch (title) {
            case "接单": {
                setReceiveView(layout);
            }
            break;
            case "申请降级": {
                setLevelLowView(layout);
            }
            break;
            case "申请换人": {
                setChangeManView(layout);
            }
            break;
            case "申请暂停": {
                setStopView(layout);
            }
            break;
            case "申请关单": {
                setCloseView(layout);
            }
            break;
            case "申请升级": {
                setLevelUpView(layout);
            }
            break;
            default: {
                Toast.makeText(PopActivity.this, "开发中", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    public void setReceiveView(LinearLayout layout) {
        GDControl takePhoto1 = new GDControl("照片", "拍照");

        takePhoto1.setRelativePath("第三方施工/流程管理/接单");
        takePhoto1.setAddEnable(true);

        View takePhoto1View = takePhoto1.createView(PopActivity.this);
        layout.addView(takePhoto1View);

        GDControl edit1 = new GDControl("描述", "长文本");
        View edit1View = edit1.createView(PopActivity.this);
        layout.addView(edit1View);

    }

    public void setLevelUpView(LinearLayout layout) {
        GDControl edit1 = new GDControl("申请原因", "长文本");
        edit1View = (ImageEditView) edit1.createView(PopActivity.this);
        // edit1View.setLines(3);
        layout.addView(edit1View);
    }

    public void setLevelLowView(LinearLayout layout) {
        GDControl edit1 = new GDControl("申请原因", "长文本");
        View edit1View = edit1.createView(PopActivity.this);
        layout.addView(edit1View);
    }

    public void setChangeManView(LinearLayout layout) {
        GDControl edit1 = new GDControl("申请原因", "长文本");
        View edit1View = edit1.createView(PopActivity.this);
        layout.addView(edit1View);
    }

    public void setStopView(LinearLayout layout) {
        GDControl edit1 = new GDControl("申请原因", "长文本");
        View edit1View = edit1.createView(PopActivity.this);
        layout.addView(edit1View);

        GDControl time1 = new GDControl("暂停开始", "日期框");
        View time1View = time1.createView(PopActivity.this);
        layout.addView(time1View);

        GDControl time2 = new GDControl("暂停结束", "日期框");
        View time2View = time2.createView(PopActivity.this);
        layout.addView(time2View);

    }

    private void UpdateState() {
        CaseInfo caseinfo = caseItem.mapToCaseInfo();
        caseinfo.UpdateEvent = title;
        caseinfo.Opinion = edit1View.getValue();
        UpdateStateTask updateStateTask = new UpdateStateTask(this, true, caseinfo);
        updateStateTask.executeOnExecutor(MyApplication.executorService, edit1View.getValue());
        updateStateTask.setListener(new MmtBaseTask.OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData resultWithoutData) {

                if (resultWithoutData.ResultCode < 0) {
                    Toast.makeText(PopActivity.this, resultWithoutData.ResultMessage, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(PopActivity.this, "申请成功", Toast.LENGTH_SHORT).show();
                }

                //caseItem.State = "已到场";
                doFinish();
            }
        });
//        String[] data = initFeedbackData();
//
//        ApplyLevelUpTask applyLevelUpTask = new ApplyLevelUpTask(this, true);
//        applyLevelUpTask.executeOnExecutor(MyApplication.executorService, caseItem.CaseID + "", data[0], data[1], data[2]);
//        arriveCaseTask.setListener(new MmtBaseTask.OnWxyhTaskListener<ResultWithoutData>() {
//            @Override
//            public void doAfter(ResultWithoutData result) {
////                Toast.makeText(CaseDetailOperActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();
////
////                if (result.ResultCode < 0) {
////                    return;
////                }
////
////                //caseItem.State = "已到场";
////               // doFinish();
//            }
//        });
    }

    public void setCloseView(LinearLayout layout) {
        GDControl takePhoto1 = new GDControl("照片依据", "拍照");

        takePhoto1.setRelativePath("第三方施工/流程管理/关单");
        takePhoto1.setAddEnable(true);

        View takePhoto1View = takePhoto1.createView(PopActivity.this);
        layout.addView(takePhoto1View);

        GDControl edit1 = new GDControl("备注", "长文本");
        View edit1View = edit1.createView(PopActivity.this);
        layout.addView(edit1View);
    }

    private void initListener() {

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                switch (title) {
//                    case "申请升级": {
//                        caseItem.EventState = title;
//                        UpdateState();
//                    }
//                    break;
//                    default: {
//                        Toast.makeText(PopActivity.this, "开发中", Toast.LENGTH_SHORT).show();
//                    }
//                }
                UpdateState();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.finishActivity();
            }
        });
    }


    private void doFinish() {
//        Intent intent = new Intent();
//        intent.putExtra("title", title);
        setResult(Activity.RESULT_OK);

        AppManager.finishActivity();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.finishActivity(this);
    }
}
