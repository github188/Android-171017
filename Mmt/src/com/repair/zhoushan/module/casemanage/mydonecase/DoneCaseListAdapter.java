package com.repair.zhoushan.module.casemanage.mydonecase;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maintainproduct.module.maintenance.MaintenanceConstant;
import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.fragment.OkCancelDialogFragment;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;
import com.mapgis.mmt.global.MmtBaseTask;
import com.repair.zhoushan.entity.CaseInfo;
import com.repair.zhoushan.entity.CaseItem;
import com.repair.zhoushan.module.casemanage.casedetail.CaseBackTask;
import com.repair.zhoushan.module.casemanage.casehandover.CaseHandoverActivity;

import java.util.ArrayList;

public class DoneCaseListAdapter extends BaseAdapter {

    protected final ArrayList<CaseItem> data;

    protected final LayoutInflater inflater;
    protected final BaseActivity activity;

    /**
     * 标记当前的列表排序方式，以控制相应字段的显示透明度
     */
    protected int index = 0;

    /**
     * 当前点击的item的位置，方便从list中移除该item
     */
    public int curClickPos = 0;

    /**
     * 详情按钮的显示文字，默认显示"详情"，不同的文字对应不同的功能
     * 目前已有："详情" "办理" "撤回"
     */
    private String detailBtnText;

    private DoneCaseListAdapter(ArrayList<CaseItem> data, BaseActivity activity) {
        this.data = data;
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
    }

    public DoneCaseListAdapter(ArrayList<CaseItem> data, BaseActivity activity, String detailBtnText) {
        this(data, activity);
        this.detailBtnText = detailBtnText;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.case_list_item, null);
            viewHolder = new ViewHolder();

            viewHolder.maintenanceListItemIndex = (TextView) convertView.findViewById(R.id.maintenanceListItemIndex);

            viewHolder.maintenanceListItemLeftTop = (TextView) convertView.findViewById(R.id.maintenanceListItemLeftTop);
            viewHolder.maintenanceListItemRightTop = (TextView) convertView.findViewById(R.id.maintenanceListItemRightTop);
            viewHolder.maintenanceListItemMiddle = (TextView) convertView.findViewById(R.id.maintenanceListItemMiddle);
            viewHolder.maintenanceListItemLeftBottom = (TextView) convertView.findViewById(R.id.maintenanceListItemLeftBottom);
            viewHolder.maintenanceListItemDate = (TextView) convertView.findViewById(R.id.maintenanceListItemDate);
            viewHolder.maintenanceListItemDistance = (TextView) convertView.findViewById(R.id.maintenanceListItemDistance);

            viewHolder.maintenanceListItemLocation = (TextView) convertView.findViewById(R.id.maintenanceListItemLocation);
            viewHolder.maintenanceListItemDetail = (TextView) convertView.findViewById(R.id.maintenanceListItemDetail);
            viewHolder.maintenanceListItemStatus = (ImageView) convertView.findViewById(R.id.ivStatus);
            viewHolder.maintenanceListItemStatusRightTip = (ImageView) convertView.findViewById(R.id.ivStatusRightTip);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CaseItem caseItem = data.get(position);

        viewHolder.maintenanceListItemLocation.setText("定位");
        viewHolder.maintenanceListItemLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BaseClassUtil.isNullOrEmptyString(caseItem.XY)) {
                    onLocation(position);
                } else {
                    activity.showToast("无效的坐标信息");
                }
            }
        });

        if (!TextUtils.isEmpty(caseItem.NextStepID) && "0".equals(caseItem.IsOver)) {
            viewHolder.maintenanceListItemDetail.setText("撤回");
        } else {
            viewHolder.maintenanceListItemDetail.setText("详情");
        }

        viewHolder.maintenanceListItemDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = ((TextView) v).getText().toString();
                switch (txt) {
                    case "详情":
                        onDetailClick(position);
                        break;
                    case "办理":
                        onHandleCaseClick(position);
                        break;
                    case "撤回":
                        onDrawbackClick(position);
                        break;
                }
            }
        });

        viewHolder.maintenanceListItemIndex.setText((position + 1) + ".");

        viewHolder.maintenanceListItemLeftTop.setText(caseItem.CaseNo);
        viewHolder.maintenanceListItemLeftTop.getPaint().setFakeBoldText(true);
        viewHolder.maintenanceListItemRightTop.setTextColor(Color.parseColor("#773ab7")); //#5677fc #FF8000
        viewHolder.maintenanceListItemRightTop.setText(caseItem.FlowName);

        viewHolder.maintenanceListItemMiddle.setText(caseItem.Summary);
        viewHolder.maintenanceListItemLeftBottom.setText(caseItem.UnderTakeTime);

        String eventState = caseItem.EventState;
        viewHolder.maintenanceListItemDate.setText(eventState);

        viewHolder.maintenanceListItemStatus.setVisibility(View.GONE);
        viewHolder.maintenanceListItemStatusRightTip.setVisibility(View.GONE);
        switch (eventState) {
            case "已办结":
                viewHolder.maintenanceListItemDate.setText("");
                viewHolder.maintenanceListItemStatus.setVisibility(View.VISIBLE);
                break;
            case "退单":
                viewHolder.maintenanceListItemStatusRightTip.setVisibility(View.VISIBLE);
                break;
        }

        viewHolder.maintenanceListItemDistance.setText(caseItem.DistanceStr);
        if (index == 0) {
            // 上报时间排序
            viewHolder.maintenanceListItemLeftBottom.setAlpha(0.87f);
            viewHolder.maintenanceListItemDistance.setAlpha(0.48f);
        } else {
            // 距离排序
            viewHolder.maintenanceListItemLeftBottom.setAlpha(0.48f);
            viewHolder.maintenanceListItemDistance.setAlpha(0.87f);
        }

        return convertView;
    }

    /**
     * 点击定位查看地图位置
     */
    public void onLocation(int position) {

        CaseItem caseItem = data.get(position);
        BaseMapCallback callback = new ShowMapPointCallback(activity, caseItem.XY, caseItem.EventCode, caseItem.Summary, -1);
        MyApplication.getInstance().sendToBaseMapHandle(callback);
    }

    /**
     * 点击列表项CaseItem查看详情
     *
     * @param position
     */
    public void onDetailClick(int position) {

        curClickPos = position;

        Intent intent = new Intent(activity, MyDoneCaseDetailActivity.class);
        intent.putExtra("ListItemEntity", data.get(position));

        activity.startActivityForResult(intent, MaintenanceConstant.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(activity);
    }

    /**
     * 点击处理工单
     */
    private void onHandleCaseClick(int position) {

        curClickPos = position;

        Intent intent = new Intent(activity, CaseHandoverActivity.class);
        intent.putExtra("ListItemEntity", data.get(position));

        activity.startActivityForResult(intent, MaintenanceConstant.DEFAULT_REQUEST_CODE);
        MyApplication.getInstance().startActivityAnimation(activity);
    }

    private void onDrawbackClick(int position) {

        curClickPos = position;

        final CaseItem caseItem = data.get(position);

        if (!TextUtils.isEmpty(caseItem.NextStepID) && "0".equals(caseItem.IsOver)) {

            final OkCancelDialogFragment okCancelDialogFragment = new OkCancelDialogFragment("要撤回此工单?");
            okCancelDialogFragment.setOnRightButtonClickListener(
                    new OkCancelDialogFragment.OnRightButtonClickListener() {
                        @Override
                        public void onRightButtonClick(View view) {

                            CaseInfo caseInfo = caseItem.mapToCaseInfo();
                            caseInfo.NodeName = caseItem.UndertakeNodes;
                            new CaseBackTask(activity, true, "CaseDrawBack", "撤回失败",
                                    new MmtBaseTask.OnWxyhTaskListener<String>() {
                                @Override
                                public void doAfter(String result) {

                                    if (!TextUtils.isEmpty(result)) {
                                        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(activity, "撤回成功", Toast.LENGTH_SHORT).show();
                                        //成功后自己打开自己，达到重置界面的目的
                                        Intent intent = new Intent(activity, MyDoneCaseListActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        activity.startActivity(intent);
                                    }
                                    okCancelDialogFragment.dismiss();
                                }
                            }).mmtExecute(caseInfo);
                        }
                    }
            );
            okCancelDialogFragment.setCancelable(true);
            okCancelDialogFragment.show(activity.getSupportFragmentManager(), "");
        } else {
            Toast.makeText(activity, "已被后续人员处理，无法撤回", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 刷新列表信息
     */
    public void refresh(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    final class ViewHolder {
        public TextView maintenanceListItemIndex;

        public TextView maintenanceListItemLeftTop;
        public TextView maintenanceListItemRightTop;
        public TextView maintenanceListItemMiddle;
        public TextView maintenanceListItemLeftBottom;
        public TextView maintenanceListItemDate;
        public TextView maintenanceListItemDistance;

        public TextView maintenanceListItemLocation;
        public TextView maintenanceListItemDetail;

        public ImageView maintenanceListItemStatus;
        public ImageView maintenanceListItemStatusRightTip;
    }
}
