package com.patrolproduct.module.myplan.feedback;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapgis.mmt.AppManager;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.DimenTool;
import com.mapgis.mmt.common.widget.customview.ImageButtonView;
import com.mapgis.mmt.common.widget.customview.ImageEditView;
import com.mapgis.mmt.common.widget.customview.ImageFragmentView;
import com.mapgis.mmt.common.widget.customview.ImageSwitchView;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment;
import com.mapgis.mmt.common.widget.fragment.ListDialogFragment.OnListItemClickListener;
import com.mapgis.mmt.common.widget.fragment.PhotoFragment;
import com.mapgis.mmt.constant.ActivityAlias;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.SavedReportInfo;
import com.mapgis.mmt.module.gps.GpsReceiver;
import com.mapgis.mmt.module.gps.trans.GpsXYZ;
import com.patrolproduct.entity.PatrolDevice;
import com.patrolproduct.module.myplan.DeviceStatusChangedCallback;
import com.patrolproduct.module.myplan.KeyDotStatusTxtCallback;
import com.patrolproduct.module.myplan.MyPlanUtil;
import com.patrolproduct.module.myplan.entity.PatrolTask;
import com.sleepbot.base.DatePickerDialog;
import com.sleepbot.base.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyPlanFeedbackFragment extends Fragment {
    private final static String ARG_POINT_MODEL = "PointFeedbackWordsModel";

    private final ArrayList<FeedItem> items = new ArrayList<>();

    public MyPlanFeedbackFragment() {
    }

    public static MyPlanFeedbackFragment newInstance(ArrayList<PointFeedbackWordsModel> models){
        MyPlanFeedbackFragment myPlanFeedbackFragment = new MyPlanFeedbackFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_POINT_MODEL,models);
        myPlanFeedbackFragment.setArguments(args);
        return myPlanFeedbackFragment;
    }

    ScrollView scrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        scrollView = new ScrollView(getActivity());
        scrollView.setLayoutParams(params);
        scrollView.setBackgroundColor(getResources().getColor(R.color.white));

        return scrollView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayList<PointFeedbackWordsModel> models;
        if (getArguments() != null){
            models = getArguments().getParcelableArrayList(ARG_POINT_MODEL);
        }else{
            models = new ArrayList<>();
        }
        addView(models);
    }

    private ImageButtonView buildTimeView(String key) {
        final ImageButtonView view = new ImageButtonView(getActivity());

        view.setTag(key);
        view.setKey(key);
        view.setImage(R.drawable.setting_clock);
        view.setValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        view.setRequired(true);

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
                        timePickerDialog.show(getActivity().getSupportFragmentManager(), "");

                        int m = month + 1;
                        view.setValue(year + "-" + (m >= 10 ? m : "0" + m) + "-" + (day >= 10 ? day : "0" + day));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getActivity().getSupportFragmentManager(), "");
            }
        });

        return view;
    }

    private void checkIsMustView(View parent) {
        try {
            if (!((PointFeedbackWordsModel) parent.getTag()).isMust) {
                return;
            }

            if (parent instanceof ImageFragmentView) {
                ((ImageFragmentView) parent).isMustDo(true);
            } else if (parent instanceof ImageEditView) {
                ((ImageEditView) parent).setRequired(true);
            } else if (parent instanceof ImageButtonView) {
                ((ImageButtonView) parent).setRequired(true);
            } else if (parent instanceof LinearLayout) {
                parent.findViewById(R.id.txtMust).setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private View buildYesNoView(String key) {
        View convertView = getActivity().getLayoutInflater().inflate(R.layout.mmt_check_box, null);

        TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        RadioButton btnYes = (RadioButton) convertView.findViewById(R.id.btnYes);
        RadioButton btnNo = (RadioButton) convertView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    ((TextView) ((ViewGroup) v.getParent().getParent()).getChildAt(1)).setTextColor(Color.RED);
                    Object tag = ((ViewGroup) v.getParent().getParent()).getTag();

                    if (tag != null) {
                        checkTrigger((PointFeedbackWordsModel) tag, true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnNo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    ((TextView) ((ViewGroup) v.getParent().getParent()).getChildAt(1)).setTextColor(Color.BLACK);
                    Object tag = ((ViewGroup) v.getParent().getParent()).getTag();

                    if (tag != null) {
                        checkTrigger((PointFeedbackWordsModel) ((ViewGroup) v.getParent().getParent()).getTag(), false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnNo.performClick();

        txtName.setText(key);

        return convertView;
    }

    private void checkTrigger(PointFeedbackWordsModel model, boolean isChecked) {
        if (!model.isTrigger) {
            return;
        }

        boolean expect = true;

        if (!BaseClassUtil.isNullOrEmptyString(model.trigCondition) && model.trigCondition.contains("-")) {
            expect = model.trigCondition.split("-")[1].equals("是");
        }

        if (isChecked == expect) {
            Intent intent = new Intent(getActivity(), ActivityClassRegistry.getInstance().getActivityClass(
                    ActivityAlias.PATROL_REPORT_ACTIVITY));

            PatrolDevice device = (PatrolDevice) getActivity().getIntent().getSerializableExtra("device");

            if (device != null)
                intent.putExtra("identityField", new String[]{device.LayerName, "编号", device.PipeNo});

            getActivity().startActivityForResult(intent, 0);
        }
    }

    /**
     * ********************************** 根据结果生成界面 *******************************************
     */
    private void addView(ArrayList<PointFeedbackWordsModel> models) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        LinearLayout parentLayout = new LinearLayout(getActivity());
        parentLayout.setLayoutParams(params);
        parentLayout.setOrientation(LinearLayout.VERTICAL);

        try {
            for (PointFeedbackWordsModel model : models) {
                final String type = model.feedbackType;
                final String key = model.feedbackDescription;
                View view = null;

                if (type.equals("double") || type.equals("string")) {
                    ImageEditView editView = new ImageEditView(getActivity());

                    editView.setLayoutParams(params);
                    editView.setImage(getDrawableId(type));
                    editView.setKey(key);

                    if (type.equals("double")) {
                        editView.setInputType(ImageEditView.MmtInputType.DECIMAL);
                    }

                    view = editView;
                } else if (type.equals("multi")) {
                    String myKey = model.feedbackDescription.split("\\*")[0];
                    String[] values = model.feedbackDescription.split("\\*")[1].split(",");
                    final ArrayList<String> vList = new ArrayList<String>();

                    for (String str : values) {
                        vList.add(str);
                    }

                    final ImageButtonView buttonView = new ImageButtonView(getActivity());
                    buttonView.setImage(getDrawableId(type));
                    buttonView.setKey(myKey);
                    buttonView.setValue(vList.get(0));

                    buttonView.getButton().setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ListDialogFragment fragment = new ListDialogFragment(key, vList);
                            fragment.show(getActivity().getSupportFragmentManager(), "1");
                            fragment.setListItemClickListener(new OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int arg2, String value) {
                                    buttonView.setValue(value);
                                }
                            });
                        }
                    });

                    view = buttonView;
                } else if (type.equals("bool") || type.equals("yesno")) {
                    view = buildYesNoView(key);
                } else if (type.equals("image")) {
                    ImageFragmentView cameraView = new ImageFragmentView(getActivity());
                    cameraView.setImage(R.drawable.flex_flow_takephoto);
                    cameraView.setKey(key);

                    view = cameraView;
                } else if (type.equals("time")) {
                    view = buildTimeView(key);
                } else {
                    ImageButtonView buttonView = new ImageButtonView(getActivity());
                    buttonView.setImage(getDrawableId(type));
                    buttonView.setKey(key);

                    buttonView.getButton().setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getActivity(), type + "的点击事件..", Toast.LENGTH_SHORT).show();
                        }
                    });

                    view = buttonView;
                }

                view.setTag(model);

                checkIsMustView(view);

                parentLayout.addView(view);

                // 分割线
                ImageView lineView = new ImageView(getActivity());

                lineView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, DimenTool.dip2px(getActivity(), 1)));
                lineView.setImageResource(R.drawable.list_divider);

                parentLayout.addView(lineView);
            }

            scrollView.addView(parentLayout);

            // 把FramLayout替换为照相的Fragment
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            for (int i = 0; i < parentLayout.getChildCount(); i++) {
                if (parentLayout.getChildAt(i) instanceof ImageFragmentView) {
                    ImageFragmentView iView = (ImageFragmentView) parentLayout.getChildAt(i);
                    PhotoFragment takePhotoFragment = new PhotoFragment.Builder("PlanFeedback/")
                            .setSelectEnable(false)
                            .build();
                    ft.replace(iView.getFrameLayout().getId(), takePhotoFragment);
                }
            }
            ft.commit();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "配置的字段不符合格式,请检查", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * ********************************* 根据字符串返回对应的图片资源ID *************************************
     */
    private int getDrawableId(String key) {
        int id = R.drawable.flex_flow_address;

        if (key.contains("string")) {
            id = R.drawable.flex_flow_advice;
        } else if (key.contains("时间") || key.contains("日期")) {
            id = R.drawable.handoverfo_clock;
        } else if (key.contains("multi")) {
            id = R.drawable.flex_flow_type;
        } else if (key.contains("坐标")) {
            id = R.drawable.task_detail_location;
        } else if (key.contains("bool")) {
            id = R.drawable.handoverform_report;
        } else if (key.contains("image")) {
            id = R.drawable.task_detail_date;
        }

        return id;
    }

    /**
     * ********************************* 计划反馈 *************************************
     */
    public void planFeedback() {
        MyApplication.getInstance().submitExecutorService(workRunnable);
    }

    private final Runnable workRunnable = new Runnable() {

        @Override
        public void run() {
            items.clear();

            String currentTime = BaseClassUtil.getSystemTime();

            PatrolDevice device = (PatrolDevice) getActivity().getIntent().getSerializableExtra("device");

            int taskId = 0;

            if (device != null) {
                device.IsFeedbacked = true;

                items.add(new FeedItem("TASKID", "0", device.TaskId + ""));
                items.add(new FeedItem("EQUIPTYPE", "0", device.LayerName));
                items.add(new FeedItem("EQUIPENTITY", "0", device.PipeNo));

                taskId = device.TaskId;
            } else {
                items.add(new FeedItem("TASKID", "0", getActivity().getIntent().getStringExtra("taskId")));
                taskId = Integer.valueOf(getActivity().getIntent().getStringExtra("taskId"));
            }

            items.add(new FeedItem("feedbacktime", "0", currentTime));

            GpsXYZ xyz = GpsReceiver.getInstance().getLastLocalLocation();

            String position = "0,0";

            if (xyz.isUsefull()) {
                position = xyz.getX() + "," + xyz.getY();
            } else {
                if (device != null) {
                    position = device.X + "," + device.Y;
                }
            }

            // 若没有成功获取坐标，则采用区域中心点坐标
            if (position.equals("0,0") && getActivity().getIntent().hasExtra("centerPoint")) {
                position = getActivity().getIntent().getStringExtra("centerPoint");
            }

            items.add(new FeedItem("position", "0", position));

            LinearLayout root = (LinearLayout) scrollView.getChildAt(0);

            String err = searchFeedItems(root);

            if (!BaseClassUtil.isNullOrEmptyString(err)) {
                MyApplication.getInstance().showMessageWithHandle(err);

                return;
            }

            String json = new Gson().toJson(items);

            SavedReportInfo savedReportInfo = new SavedReportInfo(taskId, json, BaseClassUtil.listToString(photoNames), "", "unreported",
                    "unreported", "feedback");

            long i = DatabaseHelper.getInstance().insert(savedReportInfo);

//            // 插入 监控是志 记录
//            if (i > 0) {
//                List<SavedReportInfo> infos = DatabaseHelper.getInstance().query(SavedReportInfo.class,
//                        new SQLiteQueryParameters("taskId='" + taskId + "'"));
//
//                if (infos != null && infos.size() > 0) {
//                    TaskControlEntity entity = new TaskControlEntity(0, BaseClassUtil.getSystemTime(), "事件上报", infos.get(0).getTaskId(),
//                            MyApplication.getInstance().getUserId(), SavedReportInfo.class.getName());
//                    entity.insertData();
//                }
//            }

            MyApplication.getInstance().showMessageWithHandle("反馈信息保存成功,等待反馈!");

            if (device != null) {
                MyApplication.getInstance().sendToBaseMapHandle(new DeviceStatusChangedCallback(device));
            } else {
                MyPlanUtil.updateArriveOrFeedbackState(getActivity().getIntent().getStringExtra("taskId"));
                PatrolTask patrolTask = new PatrolTask();
                patrolTask.TaskID = getActivity().getIntent().getStringExtra("taskId");
                MyApplication.getInstance().sendToBaseMapHandle(new KeyDotStatusTxtCallback(patrolTask));
            }

            AppManager.finishActivity(getActivity());
            MyApplication.getInstance().finishActivityAnimation(getActivity());

            // 暂时注释，上报成功后修改缓存状态，需要后续处理

            // for (ArrayList<PatrolEquipment> equipments :
            // SessionManager.PatrolEquipmentsHashtable.values()) {
            // for (PatrolEquipment patrolEquipment : equipments) {
            // for (Graphic patrolGraphic :
            // patrolEquipment.getFeatureSet().getGraphics()) {
            // if
            // (patrolGraphic.getAttributeValue("Task_ID").equals(info.getTaskId())
            // &&
            // patrolGraphic.getAttributeValue("<图层名称>").equals(String.valueOf(graphic.getAttributeValue("<图层名称>")))
            // &&
            // patrolGraphic.getAttributeValue("编号").equals(String.valueOf(graphic.getAttributeValue("编号"))))
            // {
            // patrolGraphic.setAttributeValue("<反馈状态>", "已反馈");
            // patrolGraphic.setAttributeValue("<保存时间>", currentTime);
            //
            // GpsReceiverHandler.getInstance().sendEmptyMessage(GpsReceiverHandler.GPS_CHECK_EQUIPMENT_STATE);
            // MyPlanUtil.updateArriveOrFeedbackState(info.getTaskId(),
            // graphic.getAttributeValue("<图层名称>"),
            // graphic.getAttributeValue("编号"), false);
            // break;
            // }
            // }
            // }
            // }
        }
    };

    /**
     * ********************************* 遍历视图获取反馈所需要的信息 *************************************
     */
    private final List<String> photoNames = new ArrayList<String>();

    private String searchFeedItems(ViewGroup parentLayout) {

        String err = "";

        if (parentLayout == null) {
            return "无反馈项，不需要反馈";
        }

        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View v = parentLayout.getChildAt(i);

            if (v instanceof ImageView) {
                continue;
            }

            PointFeedbackWordsModel model = v.getTag() != null ? (PointFeedbackWordsModel) v.getTag() : null;

            FeedItem feedItem = new FeedItem();

            feedItem.Name = v.getTag().toString();
            feedItem.Type = "1";

            if (v instanceof ImageButtonView) {
                ImageButtonView view = (ImageButtonView) v;

                feedItem.Value = view.getValue();

                items.add(feedItem);
            } else if (v instanceof ImageEditView) {
                feedItem.Value = ((ImageEditView) v).getValue();

                if (model.feedbackType.equals("double")) {
                    if (!model.isMust && BaseClassUtil.isNullOrEmptyString(feedItem.Value)) {
                        feedItem.Value = "0";
                    } else if (!BaseClassUtil.isNullOrEmptyString(feedItem.Value)) {
                        try {
                            Double.parseDouble(feedItem.Value);
                        } catch (Exception ex) {
                            return "请确保填写的是有效数值——" + feedItem.Name;
                        }
                    }
                }

                items.add(feedItem);
            } else if (v instanceof ImageFragmentView) {
                feedItem.Type = "2";

                ImageFragmentView view = (ImageFragmentView) v;

                PhotoFragment takePhotoFragment = (PhotoFragment) getActivity().getSupportFragmentManager().findFragmentById(
                        view.getFrameLayout().getId());

                feedItem.Value = takePhotoFragment.getNames();

                model.getMediaList().clear();

                items.add(feedItem);

                photoNames.addAll(takePhotoFragment.getAbsolutePhotoList());
            } else if (v instanceof ImageSwitchView) {
                ImageSwitchView view = (ImageSwitchView) v;

                if (view.getValue().equals("是")) {
                    feedItem.Value = "1";
                } else if (view.getValue().equals("否")) {
                    feedItem.Value = "0";
                }

                items.add(feedItem);
            } else if (v instanceof LinearLayout) {
                if (v.findViewById(R.id.btnYes) == null) {
                    continue;
                }

                if (((RadioButton) v.findViewById(R.id.btnYes)).isChecked()) {
                    feedItem.Value = "1";
                } else if (((RadioButton) v.findViewById(R.id.btnNo)).isChecked()) {
                    feedItem.Value = "0";
                } else {
                    return "请点选反馈项——" + ((TextView) v.findViewById(R.id.txtName)).getText();
                }

                items.add(feedItem);
            }

            if (model.isMust && BaseClassUtil.isNullOrEmptyString(feedItem.Value)) {
                return "请填写反馈项——" + feedItem.Name;
            }
        }

        return err;
    }
}
