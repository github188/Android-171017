package com.repair.mycase.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.customview.ImageLineView;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.common.widget.fragment.RecorderFragment;
import com.mapgis.mmt.entity.ResultWithoutData;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.repair.common.CaseItem;
import com.mapgis.mmt.global.MmtBaseTask.OnWxyhTaskListener;
import com.repair.mycase.detail.task.CaseArriveTask;
import com.repair.mycase.detail.task.CaseBackTask;
import com.repair.mycase.detail.task.CaseDelayTask;
import com.repair.mycase.detail.task.CaseDoneTask;
import com.repair.mycase.detail.task.CaseReceiveTask;
import com.repair.mycase.detail.task.CaseRepairTask;
import com.sleepbot.base.DatePickerDialog;
import com.sleepbot.base.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * 到场，退单，延期，维修通用的上报界面。
 * <p/>
 * 参数设置：<br>
 * title操作标识和标题<br>
 * showRecord是否显示录音<br>
 * showPhoto是否显示照片<br>
 * caseItem案件信息<br>
 */
public class CaseDetailOperActivity extends FragmentActivity {
    /**
     * 意见描述输入框
     */
    private ImageEditView editView;
    /**
     * 日期选择框，延期用
     */
    private ImageButtonView dataTimeView;
    private ImageFragmentView photoView;
    private ImageFragmentView recordView;
    private Button okButton;
    private Button cancelButton;

    private CaseItem caseItem;
    private String title;
    private boolean showRecord;
    private boolean showPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.ok_cancel_dialog);

        title = getIntent().getStringExtra("title");
        showRecord = getIntent().getBooleanExtra("showRecord", false);
        showPhoto = getIntent().getBooleanExtra("showPhoto", false);
        caseItem = getIntent().getParcelableExtra("caseItem");

        AppManager.addActivity(this);

        initView();
        initListener();
    }

    /**
     * 创建界面
     */
    private void initView() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        ((TextView) findViewById(R.id.tv_ok_cancel_dialog_Tips)).setText(title);

        okButton = (Button) findViewById(R.id.btn_ok);
        cancelButton = (Button) findViewById(R.id.btn_cancel);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_ok_cancel_dialog_content);
        layout.setVisibility(View.VISIBLE);

        editView = new ImageEditView(this);
        editView.setImage(R.drawable.setting_dialog);
        editView.setEditTextGravity(Gravity.TOP | Gravity.LEFT);
        editView.setLines(5);

        if (title.equals("延期")) {
            editView.setKey("延期原因");
            dataTimeView = buildTimeView();
            layout.addView(dataTimeView);
            layout.addView(new ImageLineView(this));
        } else {
            editView.setKey("描述");
        }

        layout.addView(editView);

        if (showPhoto) {// 是否显示照片控件
            photoView = new ImageFragmentView(this);
            photoView.setKey("照片");
            photoView.setImage(R.drawable.flex_flow_takephoto);


            layout.addView(new ImageLineView(this));
            layout.addView(photoView);

            String relPathSegment = title + "/" + time + "/"
                    + MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName + "/";
            PhotoFragment photoFragment = new PhotoFragment.Builder(relPathSegment)
                    .setSelectEnable(false).build();
            transaction.replace(photoView.getFrameLayout().getId(), photoFragment);
        }

        if (showRecord) {// 是否显示录音控件
            recordView = new ImageFragmentView(this);
            recordView.setKey("录音");
            recordView.setImage(R.drawable.record);

            RecorderFragment recorderFragment = RecorderFragment.newInstance(title + "/" + time + "/"
                    + MyApplication.getInstance().getConfigValue("UserBean", UserBean.class).TrueName + "/");

            layout.addView(new ImageLineView(this));
            layout.addView(recordView);

            transaction.replace(recordView.getFrameLayout().getId(), recorderFragment);
        }

        transaction.commitAllowingStateLoss();
    }

    /**
     * 创建时间类型视图
     */
    private ImageButtonView buildTimeView() {
        final ImageButtonView view = new ImageButtonView(this);

        view.setImage(R.drawable.setting_clock);
        view.setKey("完成时间");

        view.getButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout v, int hourOfDay, int minute) {
                        view.setValue(view.getValue() + " " + (hourOfDay >= 10 ? hourOfDay : "0" + hourOfDay) + ":"
                                + (minute >= 10 ? minute : "0" + minute) + ":00");
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true, false);

                timePickerDialog.setCloseOnSingleTapMinute(false);

                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                        timePickerDialog.show(getSupportFragmentManager(), "");

                        int m = month + 1;
                        view.setValue(year + "-" + (m >= 10 ? m : "0" + m) + "-" + (day >= 10 ? day : "0" + day));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getSupportFragmentManager(), "");
            }
        });

        return view;
    }

    /**
     * 初始化监听事件。<br>
     * 确定和取消的点击事件。
     */
    private void initListener() {
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (title) {
                    case "接单":
                        receiveCase();
                        break;
                    case "到场":
                        arriveCase();
                        break;
                    case "退单":
                        backCase();
                        break;
                    case "延期":
                        delayCase();
                        break;
                    case "维修":
                        repairCase();
                        break;
                    case "完工":
                        doneCase();
                        break;
                }
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.finishActivity();
            }
        });
    }

    /**
     * 接单操作
     */
    private void receiveCase() {
        CaseReceiveTask receiveCaseTask = new CaseReceiveTask(this, true);
        receiveCaseTask.executeOnExecutor(MyApplication.executorService, caseItem.CaseID + "", editView.getValue());
        receiveCaseTask.setListener(new OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData result) {
                Toast.makeText(CaseDetailOperActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();

                if (result.ResultCode < 0) {
                    return;
                }

                caseItem.State = "已接受";
                doFinish();
            }
        });
    }

    /**
     * 到场操作
     */
    private void arriveCase() {
        String[] data = initFeedbackData();

        CaseArriveTask arriveCaseTask = new CaseArriveTask(this, true);
        arriveCaseTask.executeOnExecutor(MyApplication.executorService, caseItem.CaseID + "", data[0], data[1], data[2]);
        arriveCaseTask.setListener(new OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData result) {
                Toast.makeText(CaseDetailOperActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();

                if (result.ResultCode < 0) {
                    return;
                }

                caseItem.State = "已到场";
                doFinish();
            }
        });
    }

    /**
     * 退单操作
     */
    private void backCase() {
        CaseBackTask backCaseTask = new CaseBackTask(this, true);
        backCaseTask.executeOnExecutor(MyApplication.executorService, caseItem.CaseID, caseItem.ID0, editView.getValue());
        backCaseTask.setListener(new OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData result) {
                Toast.makeText(CaseDetailOperActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();

                if (result.ResultCode < 0) {
                    return;
                }

                caseItem.State = "已退单";
                doFinish();
            }
        });
    }

    private boolean checkTime() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String time = dataTimeView.getValue();

            if (TextUtils.isEmpty(time)) {
                Toast.makeText(this, "请选择选择延期时间", Toast.LENGTH_SHORT).show();

                return false;
            }

            long targetTime = format.parse(time).getTime();

            long currentTime = format.parse(caseItem.PredictFinishTime).getTime();

            if (targetTime <= currentTime) {
                Toast.makeText(this, "延期时间需晚于预计完成时间", Toast.LENGTH_SHORT).show();

                return false;
            }

            currentTime = System.currentTimeMillis();

            if (targetTime <= currentTime) {
                Toast.makeText(this, "延期时间需晚于当前时间", Toast.LENGTH_SHORT).show();

                return false;
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    /**
     * 延期操作
     */
    private void delayCase() {
        if (!checkTime())
            return;

        CaseDelayTask delayCaseTask = new CaseDelayTask(this, true);
        delayCaseTask.executeOnExecutor(MyApplication.executorService, caseItem.CaseID, dataTimeView.getValue(),
                editView.getValue(), caseItem.PredictFinishTime);
        delayCaseTask.setListener(new OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData result) {
                Toast.makeText(CaseDetailOperActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();

                if (result.ResultCode < 0) {
                    return;
                }

                caseItem.DelayRequestState = "待审核";
                caseItem.DelayRequestTime = BaseClassUtil.getSystemTime();
                caseItem.DelayTargetTime = dataTimeView.getValue();

                doFinish();
            }
        });
    }

    /**
     * 维修操作
     */
    private void repairCase() {
        String[] data = initFeedbackData();

        CaseRepairTask repairCaseTask = new CaseRepairTask(this, true);
        repairCaseTask.executeOnExecutor(MyApplication.executorService, caseItem.CaseID + "", data[0], data[1], data[2]);
        repairCaseTask.setListener(new OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData result) {
                Toast.makeText(CaseDetailOperActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();

                if (result.ResultCode < 0) {
                    return;
                }

                caseItem.State = "处理中";
                doFinish();
            }
        });
    }

    // ///////////////////////////////////////完工按钮执行函数///////////////////////////////////////////////////

    /**
     * 执行上报方法
     */
    private void doneCase() {
        String[] data = initFeedbackData();

        CaseDoneTask doneCaseTask = new CaseDoneTask(this, true);
        doneCaseTask.executeOnExecutor(MyApplication.executorService, caseItem.CaseID + "", caseItem.ID0, data[0], data[1],
                data[2]);
        doneCaseTask.setListener(new OnWxyhTaskListener<ResultWithoutData>() {
            @Override
            public void doAfter(ResultWithoutData result) {
                Toast.makeText(CaseDetailOperActivity.this, result.ResultMessage, Toast.LENGTH_SHORT).show();

                if (result.ResultCode < 0) {
                    return;
                }

                caseItem.State = "已完工";
                doFinish();
            }
        });
    }

    // ///////////////////////////////////////完工按钮执行函数///////////////////////////////////////////////////

    /**
     * 上报结束后
     */
    private void doFinish() {
        Intent intent = new Intent();

        intent.putExtra("title", getIntent().getStringExtra("title"));
        intent.putExtra("caseItem", caseItem);

        setResult(Activity.RESULT_OK, intent);

        AppManager.finishActivity();
    }

    /**
     * 将反馈值初始化到一个长度为3的数组中中 [0]为上报数据，[1]为文件路径，[2]文件相对路径
     */
    private String[] initFeedbackData() {
        String[] result = new String[3];

        FeedBackEntity entity = new FeedBackEntity();

        String photosPath = "";
        String photosRelativePath = "";

        String recordsPath = "";
        String recordsRelativePath = "";

        entity.Desc = editView.getValue();

        if (photoView != null) {
            HashMap<String, String> map = photoView.getKeyValue();

            photosPath = map.get(ImageFragmentView.ABSOLUTE_KEY_STRING);

            if (!BaseClassUtil.isNullOrEmptyString(photosPath)) {
                photosRelativePath = map.get(ImageFragmentView.RELATIVE_KEY_STRING);
                entity.Photo = photosRelativePath;
            }
        }

        if (recordView != null) {
            HashMap<String, String> map = recordView.getKeyValue();

            recordsPath = map.get(ImageFragmentView.ABSOLUTE_KEY_STRING);

            if (!BaseClassUtil.isNullOrEmptyString(recordsPath)) {
                recordsRelativePath = map.get(ImageFragmentView.RELATIVE_KEY_STRING);
                entity.Record = recordsRelativePath;
            }
        }

        String allFilePath = "";
        String allFileRelativePath = "";

        if (!BaseClassUtil.isNullOrEmptyString(photosPath) && !BaseClassUtil.isNullOrEmptyString(recordsPath)) {
            allFilePath = photosPath + "," + recordsPath;
            allFileRelativePath = photosRelativePath + "," + recordsRelativePath;
        } else if (!BaseClassUtil.isNullOrEmptyString(photosPath)) {
            allFilePath = photosPath;
            allFileRelativePath = photosRelativePath;
        } else if (!BaseClassUtil.isNullOrEmptyString(recordsPath)) {
            allFilePath = recordsPath;
            allFileRelativePath = recordsRelativePath;
        }

        String data = new Gson().toJson(entity, FeedBackEntity.class);

        result[0] = data;
        result[1] = allFilePath;
        result[2] = allFileRelativePath;

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.finishActivity(this);
    }

    class FeedBackEntity {
        public String Desc;
        public String Photo;
        public String Record;
    }
}
