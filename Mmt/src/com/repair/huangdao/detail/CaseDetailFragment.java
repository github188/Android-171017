package com.repair.huangdao.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.MmtAudiosViewer;
import com.mapgis.mmt.common.widget.customview.MmtImagesViewer;
import com.mapgis.mmt.R;
import com.repair.common.CaseItem;
import com.repair.entity.CaseBackInfo;
import com.repair.entity.CaseDelayInfo;
import com.repair.entity.CaseFullyDetail;
import com.repair.entity.WorkBillProcess;
import com.repair.huangdao.MyCaseHDActivity;

import java.util.List;

public class CaseDetailFragment extends Fragment{
    private BaseActivity activity;
    private CaseFullyDetail detail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (BaseActivity) getActivity();

        CaseItem caseItem = getArguments().getParcelable("caseItem");

        detail = new CaseFullyDetail();

        detail.BaseInfo = caseItem;

        View view = inflater.inflate(R.layout.wx_my_case_detail_hd, container, false);

        view.findViewById(R.id.layoutDetails).setVisibility(View.GONE);

        return view;
    }

    private void displayData(View view) {
        showReportInfo(view);
        showAssignInfo(view);

        showRepairInfo(view);
        showDelayInfo(view);
        showBackInfo(view);
    }

    /**
     * 显示上报信息
     *
     * @param view 视图
     */
    private void showReportInfo(View view) {
        CaseItem info = detail.BaseInfo;

        ((TextView) view.findViewById(R.id.tvEventCode)).setText(info.EventCode);
        ((TextView) view.findViewById(R.id.tvReportTime)).setText(info.ReportTime);
        ((TextView) view.findViewById(R.id.tvReporter)).setText(info.ReportMan);
        ((TextView) view.findViewById(R.id.tvReportDept)).setText(info.ReportDepartment);
        ((TextView) view.findViewById(R.id.tvEventType)).setText(info.EventType);
        ((TextView) view.findViewById(R.id.tvEventContent)).setText(info.EventClass);
        ((TextView) view.findViewById(R.id.tvEventSource)).setText(info.EventSource);
        ((TextView) view.findViewById(R.id.tvEventAddress)).setText(info.Address);
        ((TextView) view.findViewById(R.id.tvEventDesc)).setText(info.Description);

        view.findViewById(R.id.layoutHotlineUser).setVisibility(View.VISIBLE);

        ((TextView) view.findViewById(R.id.tvHLUserNo)).setText(info.UserCode);
        ((TextView) view.findViewById(R.id.tvHLUserName)).setText(info.UserName);
        ((TextView) view.findViewById(R.id.tvHLUserPhone)).setText(info.UserTel);

        List<String> images = BaseClassUtil.StringToList(info.Picture, ",");

        if (images != null && images.size() > 0) {
            ((MmtImagesViewer) view.findViewById(R.id.layoutImages)).showByOnline(images);
        } else
            view.findViewById(R.id.layoutImagesRoot).setVisibility(View.GONE);

        List<String> audios = BaseClassUtil.StringToList(info.Recording, ",");

        if (audios != null && audios.size() > 0) {
            ((MmtAudiosViewer) view.findViewById(R.id.layoutAudios)).showByOnline(audios);
        } else
            view.findViewById(R.id.layoutAudiosRoot).setVisibility(View.GONE);
    }

    /**
     * 显示分派信息
     *
     * @param view 视图
     */
    private void showAssignInfo(View view) {
        CaseItem info = detail.BaseInfo;

        ((TextView) view.findViewById(R.id.tvAssignTime)).setText(info.DispatchTime);
        ((TextView) view.findViewById(R.id.tvEmergencyLevel)).setText(info.EmergencyLevel);
        ((TextView) view.findViewById(R.id.tvProcessLevel)).setText(info.Level);
        ((TextView) view.findViewById(R.id.tvWishFinishTime)).setText(info.PredictFinishTime);
        ((TextView) view.findViewById(R.id.tvDelayFinishTime)).setText(info.DelayFinishTime);
        ((TextView) view.findViewById(R.id.tvAssigner)).setText(info.DispatchMan);
        ((TextView) view.findViewById(R.id.tvAssignDept)).setText(info.DispatchDepartment);

        TextView tvAssignOptionName = (TextView) view.findViewById(R.id.tvAssignOptionName);

        if (!TextUtils.isEmpty(info.Direction) && Integer.valueOf(info.Direction) < 0) {
            tvAssignOptionName.setText("打回意见：");
            tvAssignOptionName.setTextColor(getResources().getColor(R.color.red));
        }

        ((TextView) view.findViewById(R.id.tvAssignOption)).setText(info.Opinion);

        ((ViewGroup) view.findViewById(R.id.tvEmergencyLevel).getParent()).setVisibility(View.GONE);
        ((ViewGroup) view.findViewById(R.id.tvDelayFinishTime).getParent()).setVisibility(View.GONE);
    }

    /**
     * 显示维修信息
     *
     * @param view 视图
     */
    private void showRepairInfo(View view) {
        if (detail == null || detail.ProcessInfoList == null || detail.ProcessInfoList.size() == 0)
            return;

        view.findViewById(R.id.layoutRepair).setVisibility(View.VISIBLE);

        if (detail.ProcessInfoList.size() > 1) {
            ((TextView) view.findViewById(R.id.tvRepairForMore)).setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.button_for_more, 0);

            view.findViewById(R.id.tvRepairForMore).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvHistoryTitle.setText("维修记录");
                    lvHistory.setAdapter(detail.getProcessInfoAdapt(getActivity()));

                    drawerLayout.openDrawer(GravityCompat.END);
                }
            });
        }

        WorkBillProcess info = detail.ProcessInfoList.get(0);

        ((TextView) view.findViewById(R.id.tvRepairStep)).setText(info.WorkBillState);
        ((TextView) view.findViewById(R.id.tvRepairer)).setText(info.ReporterName + "-" + info.ReporterDept);
        ((TextView) view.findViewById(R.id.tvRepairTime)).setText(info.Time);
        ((TextView) view.findViewById(R.id.tvRepairDesc)).setText(info.Remark);

        List<String> images = BaseClassUtil.StringToList(info.Images, ",");

        if (images != null && images.size() > 0) {
            ((MmtImagesViewer) view.findViewById(R.id.layoutRepairImages)).showByOnline(images);
        } else
            view.findViewById(R.id.layoutRepairImagesRoot).setVisibility(View.GONE);

        List<String> audios = BaseClassUtil.StringToList(info.Audios, ",");

        if (audios != null && audios.size() > 0) {
            ((MmtAudiosViewer) view.findViewById(R.id.layoutRepairAudios)).showByOnline(audios);
        } else
            view.findViewById(R.id.layoutRepairAudiosRoot).setVisibility(View.GONE);
    }

    /**
     * 显示延期信息
     *
     * @param view 视图
     */
    private void showDelayInfo(View view) {
        if (detail == null || detail.DelayInfoList == null || detail.DelayInfoList.size() == 0)
            return;

        view.findViewById(R.id.layoutDelay).setVisibility(View.VISIBLE);

        if (detail.DelayInfoList.size() > 1) {
            ((TextView) view.findViewById(R.id.tvDelayForMore)).setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.button_for_more, 0);

            view.findViewById(R.id.tvDelayForMore).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvHistoryTitle.setText("延期记录");
                    lvHistory.setAdapter(detail.getDelayInfoAdapt(getActivity()));
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            });
        }

        CaseDelayInfo info = detail.DelayInfoList.get(0);

        ((TextView) view.findViewById(R.id.tvDelayer)).setText(info.ApplyMan + "-" + info.ApplyGroup);
        ((TextView) view.findViewById(R.id.tvDelayToTime)).setText(info.ApplyFinishTime);
        ((TextView) view.findViewById(R.id.tvDelayChecker)).setText(info.VerifyMan + "-" + info.VerifyGroup);
        ((TextView) view.findViewById(R.id.tvDelayState)).setText(info.State);
        ((TextView) view.findViewById(R.id.tvDelayReason)).setText(info.Reason);
    }

    /**
     * 显示退单信息
     *
     * @param view 视图
     */
    private void showBackInfo(View view) {
        if (detail == null || detail.BackInfoList == null || detail.BackInfoList.size() == 0)
            return;

        view.findViewById(R.id.layoutBack).setVisibility(View.VISIBLE);

        if (detail.BackInfoList.size() > 1) {
            ((TextView) view.findViewById(R.id.tvBackForMore)).setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.button_for_more, 0);

            view.findViewById(R.id.tvBackForMore).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvHistoryTitle.setText("退单记录");
                    lvHistory.setAdapter(detail.getBackInfoAdapt(getActivity()));
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            });
        }

        CaseBackInfo info = detail.BackInfoList.get(0);

        ((TextView) view.findViewById(R.id.tvBackStep)).setText(info.ActiveName);
        ((TextView) view.findViewById(R.id.tvBacker)).setText(info.BackMan + "-" + info.BackManDepart);
        ((TextView) view.findViewById(R.id.tvBackTime)).setText(info.BackTime);
        ((TextView) view.findViewById(R.id.tvBackReason)).setText(info.Reason);
    }

    private DrawerLayout drawerLayout;
    private ListView lvHistory;
    private TextView tvHistoryTitle;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        lvHistory = (ListView) view.findViewById(R.id.lvHistory);
        tvHistoryTitle = (TextView) view.findViewById(R.id.tvHistoryTitle);

        displayData(view);

        layoutOpers = view.findViewById(R.id.layoutOpers);
        view.findViewById(R.id.layoutDetails).setVisibility(View.VISIBLE);

    }

    private View layoutOpers;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaintenanceConstant.DEFAULT_REQUEST_CODE && resultCode == -1) {
            ((MyCaseHDActivity) activity).shouldRefresh += "移交,";
            layoutOpers.setVisibility(View.GONE);
        }
    }
}
