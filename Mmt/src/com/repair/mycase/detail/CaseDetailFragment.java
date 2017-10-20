package com.repair.mycase.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.MmtAudiosViewer;
import com.mapgis.mmt.common.widget.customview.MmtImagesViewer;
import com.mapgis.mmt.common.widget.fragment.BackHandledFragment;
import com.mapgis.mmt.constant.ActivityClassRegistry;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.patrol.common.ShowGISDetailTask;
import com.patrol.entity.KeyPoint;
import com.repair.common.CaseItem;
import com.repair.mycase.MyCaseActivity;
import com.repair.mycase.list.CaseListFragment;

import java.util.List;

public class CaseDetailFragment extends BackHandledFragment implements OnClickListener {
    private BaseActivity activity;

    private CaseItem caseItem;

    /**
     * 接单按钮
     */
    private TextView receiveView;

    /**
     * 到场按钮
     */
    private TextView arriveView;

    /**
     * 退单按钮
     */
    private TextView backView;

    /**
     * 延期按钮
     */
    private TextView delayView;

    /**
     * 维修按钮
     */
    private TextView repairView;

    /**
     * 完工按钮
     */
    private TextView doneView;

    /**
     * 查看关联的GIS设备属性
     */
    private TextView tvGIS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (BaseActivity) getActivity();

        caseItem = getArguments().getParcelable("caseItem");

        View view = inflater.inflate(R.layout.wx_case_detail, null);

        ((TextView) view.findViewById(R.id.tvEventCode)).setText(caseItem.EventCode);
        ((TextView) view.findViewById(R.id.tvReportTime)).setText(caseItem.OccurTime);
        ((TextView) view.findViewById(R.id.tvReporter)).setText(caseItem.ReportMan);
        ((TextView) view.findViewById(R.id.tvReportDept)).setText(caseItem.ReportDepartment);
        ((TextView) view.findViewById(R.id.tvEventType)).setText(caseItem.EventType);
        ((TextView) view.findViewById(R.id.tvEventContent)).setText(caseItem.EventClass);
        ((TextView) view.findViewById(R.id.tvEventSource)).setText(caseItem.EventSource);
        ((TextView) view.findViewById(R.id.tvEventAddress)).setText(caseItem.Address);
        ((TextView) view.findViewById(R.id.tvEventDesc)).setText(caseItem.Description);

        List<String> images = BaseClassUtil.StringToList(caseItem.Picture, ",");

        if (images != null && images.size() > 0) {
            ((MmtImagesViewer) view.findViewById(R.id.layoutImages)).showByOnline(images);
        } else
            view.findViewById(R.id.layoutImagesRoot).setVisibility(View.GONE);

        List<String> audios = BaseClassUtil.StringToList(caseItem.Recording, ",");

        if (audios != null && audios.size() > 0) {
            ((MmtAudiosViewer) view.findViewById(R.id.layoutAudios)).showByOnline(audios);
        } else
            view.findViewById(R.id.layoutAudiosRoot).setVisibility(View.GONE);

        ((TextView) view.findViewById(R.id.tvAssignTime)).setText(caseItem.DispatchTime);
        ((TextView) view.findViewById(R.id.tvEmergencyLevel)).setText(caseItem.EmergencyLevel);
        ((TextView) view.findViewById(R.id.tvProcessLevel)).setText(caseItem.Level);
        ((TextView) view.findViewById(R.id.tvWishFinishTime)).setText(caseItem.PredictFinishTime);
        ((TextView) view.findViewById(R.id.tvDelayFinishTime)).setText(caseItem.DelayFinishTime);
        ((TextView) view.findViewById(R.id.tvAssigner)).setText(caseItem.DispatchMan);
        ((TextView) view.findViewById(R.id.tvAssignDept)).setText(caseItem.DispatchDepartment);

        TextView tvAssignOptionName = (TextView) view.findViewById(R.id.tvAssignOptionName);

        if (!TextUtils.isEmpty(caseItem.Direction) && Integer.valueOf(caseItem.Direction) < 0) {
            tvAssignOptionName.setText("打回意见：");
            tvAssignOptionName.setTextColor(getResources().getColor(R.color.red));
        }

        ((TextView) view.findViewById(R.id.tvAssignOption)).setText(caseItem.Opinion);

        showDelayState(view);

        this.arriveView = (TextView) view.findViewById(R.id.tvArrive);
        this.backView = (TextView) view.findViewById(R.id.tvRollback);
        this.delayView = (TextView) view.findViewById(R.id.tvDelay);
        this.doneView = (TextView) view.findViewById(R.id.tvFinish);
        this.receiveView = (TextView) view.findViewById(R.id.tvAccept);
        this.repairView = (TextView) view.findViewById(R.id.tvRepair);

        this.tvGIS = (TextView) view.findViewById(R.id.tvGIS);

        this.arriveView.setOnClickListener(this);
        this.backView.setOnClickListener(this);
        this.delayView.setOnClickListener(this);
        this.doneView.setOnClickListener(this);
        this.receiveView.setOnClickListener(this);
        this.repairView.setOnClickListener(this);

        this.tvGIS.setOnClickListener(this);

        if (getString(R.string.app_name).equals("桂林工程管理")) {
            view.findViewById(R.id.layoutGod).setVisibility(View.VISIBLE);

            ((TextView) view.findViewById(R.id.tvGodName)).setText(caseItem.UserTrueName);

            TextView tvTel = ((TextView) view.findViewById(R.id.tvGodTel));

            tvTel.setText(Html.fromHtml("<u>" + caseItem.UserTel + "</u>"));

            tvTel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + caseItem.UserTel));

                        startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            ((TextView) view.findViewById(R.id.tvGodNo)).setText(caseItem.WaterMeterNo);
        } else
            view.findViewById(R.id.layoutGod).setVisibility(View.GONE);

        showBottomButton();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText(caseItem.CaseCode);

        if (!BaseClassUtil.isNullOrEmptyString(caseItem.Position) && !getArguments().getString("from").equals("map")) {
            ImageButton button = (ImageButton) view.findViewById(R.id.baseActionBarRightImageView);

            button.setVisibility(View.VISIBLE);
            button.setImageResource(R.drawable.common_location);

            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowMapPointCallback(activity, caseItem.Position,
                            caseItem.CaseCode, caseItem.Address, -1);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
        }

        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        new FetchDetailTask(getActivity(), true, new MmtBaseTask.OnWxyhTaskListener<ResultData<CaseItem>>() {
//            @Override
//            public void doAfter(ResultData<CaseItem> data) {
//                if (data.ResultCode > 0) {
//                    CaseItem item = data.getSingleData();
//
//                    caseItem.EventCode = item.EventCode;
//
//                    caseItem.DelayRequestState = item.DelayRequestState;
//                    caseItem.DelayRequestTime = item.DelayRequestTime;
//                    caseItem.DelayTargetTime = item.DelayTargetTime;
//
//                    showDelayState();
//                } else {
//                    Toast.makeText(getActivity(), data.ResultMessage, Toast.LENGTH_SHORT).show();
//                }
//            }
//        }).executeOnExecutor(MyApplication.executorService, caseItem);
    }

    private void showDelayState(View view) {
        if (TextUtils.isEmpty(caseItem.DelayRequestState)) {
            view.findViewById(R.id.layoutDelay).setVisibility(View.GONE);

            return;
        } else
            view.findViewById(R.id.layoutDelay).setVisibility(View.VISIBLE);

        TextView tvDelayRequestState = (TextView) view.findViewById(R.id.tvDelayRequestState);

        if (!caseItem.DelayRequestState.equals("审核通过"))
            tvDelayRequestState.setTextColor(getResources().getColor(R.color.red));
        else
            tvDelayRequestState.setTextColor(getResources().getColor(R.color.black));

        tvDelayRequestState.setText(caseItem.DelayRequestState);

        ((TextView) view.findViewById(R.id.tvDelayRequestTime)).setText(caseItem.DelayRequestTime);
        ((TextView) view.findViewById(R.id.tvDelayTargetTime)).setText(caseItem.DelayTargetTime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            CaseItem caseItem = data.getParcelableExtra("caseItem");

            this.caseItem.State = caseItem.State;

            String title = data.getStringExtra("title");
            ((MyCaseActivity) getActivity()).shouldRefresh += title + ",";

            if (title.equals("延期")) {
                this.caseItem.DelayRequestState = caseItem.DelayRequestState;
                this.caseItem.DelayRequestTime = caseItem.DelayRequestTime;
                this.caseItem.DelayTargetTime = caseItem.DelayTargetTime;

                showDelayState(getView());
            }

            showBottomButton();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 隐藏按钮事件
     */

    public void showBottomButton() {
        String state = caseItem.State;

        receiveView.setVisibility(View.GONE);
        arriveView.setVisibility(View.GONE);
        repairView.setVisibility(View.GONE);
        delayView.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);
        doneView.setVisibility(View.GONE);

        tvGIS.setVisibility(View.GONE);

        switch (state) {
            case "待接受":// 允许接单和退单
                receiveView.setVisibility(View.VISIBLE);
                backView.setVisibility(View.VISIBLE);
                break;
            case "已接受":// 允许到场和延期
                arriveView.setVisibility(View.VISIBLE);
                delayView.setVisibility(View.VISIBLE);
                backView.setVisibility(View.VISIBLE);
                break;
            case "已到场":// 允许维修和延期
                repairView.setVisibility(View.VISIBLE);
                delayView.setVisibility(View.VISIBLE);
                backView.setVisibility(View.VISIBLE);
                break;
            case "处理中":// 允许维修，延期和完工
                repairView.setVisibility(View.VISIBLE);
                delayView.setVisibility(View.VISIBLE);
                doneView.setVisibility(View.VISIBLE);
                backView.setVisibility(View.VISIBLE);
                break;
            default:
                ((View) receiveView.getParent()).setVisibility(View.GONE);
                break;
        }

        if (!TextUtils.isEmpty(caseItem.LayerName) && !TextUtils.isEmpty(caseItem.FieldValue))
            tvGIS.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        try {
            if (!(v instanceof TextView))
                return;

            if (v.getId() == R.id.tvGIS) {
                KeyPoint kp = new KeyPoint();

                kp.GisLayer = caseItem.LayerName;
                kp.FieldName = caseItem.FieldName;
                kp.FieldValue = caseItem.FieldValue;

                new ShowGISDetailTask(getActivity()).mmtExecute(kp);

                return;
            }

            String text = ((TextView) v).getText().toString();

            Intent intent = new Intent(getActivity(), CaseDetailOperActivity.class);
            intent.putExtra("caseItem", caseItem);

            switch (text) {
                case "接单":
                    intent.putExtra("title", "接单");
                    break;
                case "到场":
                    intent.putExtra("title", "到场");
                    intent.putExtra("showRecord", true);
                    intent.putExtra("showPhoto", true);
                    break;
                case "退单":
                    intent.putExtra("title", "退单");
                    break;
                case "延期":
                    intent.putExtra("title", "延期");
                    break;
                case "维修":
                    intent.putExtra("title", "维修");
                    intent.putExtra("showRecord", true);
                    intent.putExtra("showPhoto", true);
                    break;
                case "完工":
                    intent.putExtra("title", "完工");
                    intent.putExtra("showRecord", true);
                    intent.putExtra("showPhoto", true);

                    Class<?> cls = ActivityClassRegistry.getInstance().getActivityClass("维修反馈");

                    if (cls != null) {
                        intent.setClass(getActivity(), cls);
                    }
                    break;
                default:
                    return;
            }

            startActivityForResult(intent, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onBackPressed() {
        String from = getArguments().getString("from");

        if (from.equals("map")) {
            Intent intent = new Intent(getActivity(), MapGISFrame.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            getActivity().startActivity(intent);
            MyApplication.getInstance().startActivityAnimation(getActivity());
        } else {
            FragmentManager manager = getActivity().getSupportFragmentManager();

            FragmentTransaction transaction = manager.beginTransaction();

//                transaction.setCustomAnimations(R.anim.slide_out_left,
//                        R.anim.slide_in_right);

            transaction.remove(CaseDetailFragment.this);
            transaction.show(manager.findFragmentByTag(CaseListFragment.class.getName()));

            transaction.commitAllowingStateLoss();
        }

        return true;
    }
}
