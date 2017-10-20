package com.repair.huangdao.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maintainproduct.entity.GDButton;
import com.maintainproduct.entity.GDControl;
import com.maintainproduct.entity.GDFormBean;
import com.maintainproduct.entity.GDGroup;
import com.maintainproduct.entity.MaintainSimpleInfo;
import com.maintainproduct.entity.ResultDataWC;
import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.maintainproduct.module.maintenance.detail.FetchCaseProcedureFragment;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.widget.fragment.BackHandledFragment;
import com.mapgis.mmt.common.widget.fragment.MultiSwitchFragment;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.doinback.ReportInBackEntity;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.entity.ResultData;
import com.mapgis.mmt.module.gis.MapGISFrame;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.login.UserBean;
import com.mapgis.mmt.R;
import com.repair.common.CaseItem;
import com.repair.common.ReportPostTask;
import com.repair.entity.CaseFullyDetail;
import com.repair.huangdao.CaseInfo;
import com.repair.huangdao.CaseItemV21;
import com.repair.huangdao.FeedbackHDAcitivy;
import com.repair.huangdao.MyCaseHDActivity;
import com.repair.huangdao.list.CaseListFragment;

import java.util.ArrayList;

/**
 * Created by liuyunfan on 2015/12/3.
 * 包含办理过程和工单任务的fragment
 */
public class CaseDetailFragmentFull extends BackHandledFragment implements View.OnClickListener {
    private BaseActivity activity;
    private CaseItem caseItem;
    private String from;
    private View layoutOpers;
    private CaseFullyDetail detail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wx_my_case_detail_hd_full, container, false);
        layoutOpers = view.findViewById(R.id.layoutOpers);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        activity = (BaseActivity) getActivity();
        from = getArguments().getString("from");
        caseItem = getArguments().getParcelable("caseItem");
        detail = new CaseFullyDetail();
        detail.BaseInfo = caseItem;

        ((TextView) view.findViewById(R.id.baseActionBarTextView)).setText(detail.BaseInfo.CaseCode);

        if (!TextUtils.isEmpty(detail.BaseInfo.Position) && !TextUtils.isEmpty(from) && !from.equals("map")) {
            ImageButton button = (ImageButton) view.findViewById(R.id.baseActionBarRightImageView);

            button.setVisibility(View.VISIBLE);
            button.setImageResource(R.drawable.common_location);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMapCallback callback = new ShowMapPointCallback(activity, detail.BaseInfo.Position,
                            detail.BaseInfo.CaseCode, detail.BaseInfo.Address, -1);

                    MyApplication.getInstance().sendToBaseMapHandle(callback);
                }
            });
        }


        view.findViewById(R.id.baseActionBarImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        view.findViewById(R.id.tvRollback).setOnClickListener(this);
        view.findViewById(R.id.tvNeedHelp).setOnClickListener(this);
        view.findViewById(R.id.tvHandover).setOnClickListener(this);


        MultiSwitchFragment fragment = new MultiSwitchFragment();
        MaintainSimpleInfo info = new MaintainSimpleInfo();
        info.CaseNo = caseItem.CaseNO;
        fragment.setCurrentIndex(1);

        CaseDetailFragment fragmentinfo = new CaseDetailFragment();

        Bundle bundle = new Bundle();

        bundle.putParcelable("caseItem", caseItem);
        bundle.putString("from", from);
        fragmentinfo.setArguments(bundle);

        fragment.setDate(new String[]{"办理过程", "工单信息"}, new Fragment[]{new FetchCaseProcedureFragment(info), fragmentinfo});
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.hdDetailFragment, fragment);
        ft.commitAllowingStateLoss();
    }

    @Override
    public boolean onBackPressed() {

        if (!TextUtils.isEmpty(from) && from.equals("map")) {
            Intent intent = new Intent(getActivity(), MapGISFrame.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            getActivity().startActivity(intent);
            MyApplication.getInstance().startActivityAnimation(getActivity());
        } else {
            FragmentManager manager = getActivity().getSupportFragmentManager();

            FragmentTransaction transaction = manager.beginTransaction();

            transaction.remove(CaseDetailFragmentFull.this);
            transaction.show(manager.findFragmentByTag(CaseListFragment.class.getName()));

            transaction.commitAllowingStateLoss();
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvRollback) {
            final EditText txtOpinion = new EditText(activity);

            txtOpinion.setHint("请输入退单原因");
            txtOpinion.setLines(3);
            txtOpinion.setBackgroundResource(R.drawable.edit_text_default);

            OkCancelDialogFragment fragment = new OkCancelDialogFragment("退单", txtOpinion);

            fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    if (txtOpinion.getText().length() == 0) {
                        MyApplication.getInstance().showMessageWithHandle("请输入退单原因");

                        return;
                    }

                    CaseInfo info = new CaseInfo();

                    info.CaseID = detail.BaseInfo.CaseID;
                    info.CaseNO = detail.BaseInfo.CaseNO;
                    info.IsAssist = 0;
                    info.IsOver = 0;
                    info.activeID = Integer.valueOf(detail.BaseInfo.ActiveID);
                    info.activeName = detail.BaseInfo.ActiveName;
                    info.direction = 1;
                    info.nextActiveID = "";
                    info.opinion = txtOpinion.getText().toString();
                    info.stepID = Integer.valueOf(detail.BaseInfo.ID0);

                    UserBean userBean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);

                    info.userID = String.valueOf(userBean.UserID);
                    info.userTrueName = userBean.TrueName;

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/MapgisCity_WXYH_Huangdao/REST/ServiceManageREST.svc/CaseHanback";

                    String json = new Gson().toJson(info, CaseInfo.class);

                    ReportInBackEntity entity = new ReportInBackEntity(json, userBean.UserID,
                            ReportInBackEntity.REPORTING, url, info.CaseID, "退单", null, null);

                    new ReportPostTask(activity) {
                        @Override
                        protected void onSuccess(ResultData<Integer> data) {
                            super.onSuccess(data);

                            if (data.ResultCode > 0) {
                                ((MyCaseHDActivity) activity).shouldRefresh += "退单,";
                                layoutOpers.setVisibility(View.GONE);
                            }
                        }
                    }.mmtExecute(entity);
                }
            });

            fragment.show(activity.getSupportFragmentManager(), "");
        } else if (v.getId() == R.id.tvNeedHelp) {
            final EditText txtOpinion = new EditText(activity);

            txtOpinion.setHint("请输入请求协助原因");
            txtOpinion.setLines(3);
            txtOpinion.setBackgroundResource(R.drawable.edit_text_default);

            OkCancelDialogFragment fragment = new OkCancelDialogFragment("协助", txtOpinion);

            fragment.setOnRightButtonClickListener(new OkCancelDialogFragment.OnRightButtonClickListener() {
                @Override
                public void onRightButtonClick(View view) {
                    if (txtOpinion.getText().length() == 0) {
                        MyApplication.getInstance().showMessageWithHandle("请输入请求协助原因");

                        return;
                    }

                    CaseInfo info = new CaseInfo();

                    info.CaseID = detail.BaseInfo.CaseID;
                    info.CaseNO = detail.BaseInfo.CaseNO;
                    info.IsAssist = 1;
                    info.IsOver = 0;
                    info.activeID = Integer.valueOf(detail.BaseInfo.ActiveID);
                    info.activeName = detail.BaseInfo.ActiveName;
                    info.direction = 1;
                    info.nextActiveID = "";
                    info.opinion = txtOpinion.getText().toString();
                    info.stepID = Integer.valueOf(detail.BaseInfo.ID0);

                    UserBean userBean = MyApplication.getInstance().getConfigValue("UserBean", UserBean.class);

                    info.userID = String.valueOf(userBean.UserID);
                    info.userTrueName = userBean.TrueName;

                    String url = ServerConnectConfig.getInstance().getBaseServerPath()
                            + "/Services/MapgisCity_WXYH_Huangdao/REST/ServiceManageREST.svc/CaseHanback";

                    String json = new Gson().toJson(info, CaseInfo.class);

                    ReportInBackEntity entity = new ReportInBackEntity(json, userBean.UserID,
                            ReportInBackEntity.REPORTING, url, info.CaseID, "协助", null, null);

                    new ReportPostTask(activity) {
                        @Override
                        protected void onSuccess(ResultData<Integer> data) {
                            super.onSuccess(data);

                            if (data.ResultCode > 0) {
                                ((MyCaseHDActivity) activity).shouldRefresh += "协助,";
                                layoutOpers.setVisibility(View.GONE);
                            }
                        }
                    }.mmtExecute(entity);
                }
            });

            fragment.show(activity.getSupportFragmentManager(), "");
        } else if (v.getId() == R.id.tvHandover) {
            new FetchDetailTask(activity) {
                @Override
                protected void onSuccess(ResultDataWC<CaseItemV21> data) {
                    if (data.getMe == null || data.getMe.size() == 0) {
                        String tip = "未获取到详细信息";

                        if (data.say != null && !TextUtils.isEmpty(data.say.errMsg))
                            tip = data.say.errMsg;

                        Toast.makeText(activity, tip, Toast.LENGTH_SHORT).show();

                        return;
                    }

                    CaseItemV21 item = data.getMe.get(0);

                    MaintainSimpleInfo info = new MaintainSimpleInfo();

                    info.ID = Integer.valueOf(detail.BaseInfo.CaseID);
                    info.ID0 = Integer.valueOf(detail.BaseInfo.ID0);
                    info.FlowName = detail.BaseInfo.FlowName;
                    info.ActiveID = Integer.valueOf(detail.BaseInfo.ActiveID);
                    info.ActiveName = detail.BaseInfo.ActiveName;
                    info.CaseNo = detail.BaseInfo.CaseNO;

                    ArrayList<GDControl> controls = new ArrayList<>();
                    boolean isRead=false;
                    //审核节点不允许编辑
                    if(info.ActiveName.contains("审核")){
                        isRead=true;
                    }

                    controls.add(new GDControl("开始时间", "时间", item.fbStartTime,isRead));
                    controls.add(new GDControl("维修管径", "浮点型", item.fbDiameter,isRead));
                    controls.add(new GDControl("埋深", "浮点型", item.fbDepth,isRead));
                    controls.add(new GDControl("停水影响范围", "短文本", item.fbStopRange,isRead));
                    controls.add(new GDControl("用户告知情况", "短文本", item.fbUserNotice,isRead));
                    controls.add(new GDControl("临时水供应情况", "短文本", item.fbTempWater,isRead));
                    controls.add(new GDControl("工作面恢复情况", "短文本", item.fbWorkRetore,isRead));
                    controls.add(new GDControl("工建队伍", "短文本", item.fbTroops,isRead));
                    controls.add(new GDControl("机械设备", "短文本", item.fbDevice,isRead));
                    controls.add(new GDControl("用料情况", "短文本", item.fbMaterial,isRead));
                    controls.add(new GDControl("维修前照片", "图片", item.fbPhotoBefore));
                    controls.add(new GDControl("维修中照片", "图片", item.fbPhotoMid));
                    controls.add(new GDControl("维修后照片", "图片", item.fbPhotoAfter));
                    controls.add(new GDControl("事故原因", "短文本", item.fbReason,isRead));
                    controls.add(new GDControl("维修情况", "短文本", item.fbSituation,isRead));
                    controls.add(new GDControl("结束时间", "时间", item.fbEndTime,isRead));

                    GDFormBean bean = new GDFormBean();

                    bean.TableName = "CIV_GD_WX_CASELIST";
                    bean.TopURL = bean.MiddleURL = bean.BottomURL = bean.Desc = "";
                    bean.BottomButtons = new GDButton[0];

                    GDGroup group = new GDGroup();

                    group.Name = "维修工单反馈";
                    group.Controls = controls.toArray(new GDControl[controls.size()]);

                    bean.Groups = new GDGroup[]{group};

                    Intent intent = new Intent(activity, FeedbackHDAcitivy.class);

                    intent.putExtra("ListItemEntity", info);
                    intent.putExtra("GDFormBean", bean);

                    startActivityForResult(intent, MaintenanceConstant.DEFAULT_REQUEST_CODE);
                    MyApplication.getInstance().startActivityAnimation(activity);
                }
            }.mmtExecute(this.detail.BaseInfo.CaseID);
        }
    }
}

