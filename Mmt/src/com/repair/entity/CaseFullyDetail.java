package com.repair.entity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.customview.MmtAudiosViewer;
import com.mapgis.mmt.common.widget.customview.MmtImagesViewer;
import com.mapgis.mmt.R;
import com.repair.common.CaseItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 工单详情
 */
public class CaseFullyDetail {
    /**
     * 基本信息，包含上报信息和分派信息
     */
    public CaseItem BaseInfo;

    /**
     * 工单维修信息列表
     */
    public ArrayList<WorkBillProcess> ProcessInfoList;

    /**
     * 工单延期信息列表
     */
    public ArrayList<CaseDelayInfo> DelayInfoList;

    /**
     * 工单退单信息列表
     */
    public ArrayList<CaseBackInfo> BackInfoList;

    public BaseAdapter getProcessInfoAdapt(final Context context) {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                return ProcessInfoList == null ? 0 : ProcessInfoList.size();
            }

            @Override
            public WorkBillProcess getItem(int position) {
                return ProcessInfoList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(context).inflate(R.layout.wx_all_case_detail_item, parent, false);

                WorkBillProcess info = getItem(position);

                ((TextView) convertView.findViewById(R.id.tvTopLeft)).setText((getCount() - position) + "." + info.WorkBillState);
                ((TextView) convertView.findViewById(R.id.tvTopRight)).setText(info.ReporterName + "-" + info.ReporterDept);
                ((TextView) convertView.findViewById(R.id.tvCenterTwo)).setText("描述：" + info.Remark);
                ((TextView) convertView.findViewById(R.id.tvBottomLeft)).setText("");
                ((TextView) convertView.findViewById(R.id.tvBottomRight)).setText(info.Time);

                MmtImagesViewer imagesViewer = (MmtImagesViewer) convertView.findViewById(R.id.layoutImages);

                List<String> images = BaseClassUtil.StringToList(info.Images, ",");

                if (images != null && images.size() > 0) {
                    imagesViewer.setVisibility(View.VISIBLE);
                    imagesViewer.showByOnline(images);
                } else
                    imagesViewer.setVisibility(View.GONE);

                MmtAudiosViewer audiosViewer = (MmtAudiosViewer) convertView.findViewById(R.id.layoutAudios);
                List<String> audios = BaseClassUtil.StringToList(info.Audios, ",");

                if (audios != null && audios.size() > 0) {
                    audiosViewer.setVisibility(View.VISIBLE);
                    audiosViewer.showByOnline(audios);
                } else
                    audiosViewer.setVisibility(View.GONE);

                return convertView;
            }
        };
    }

    public BaseAdapter getDelayInfoAdapt(final Context context) {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                return DelayInfoList == null ? 0 : DelayInfoList.size();
            }

            @Override
            public CaseDelayInfo getItem(int position) {
                return DelayInfoList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(context).inflate(R.layout.wx_all_case_detail_item, parent, false);

                CaseDelayInfo info = getItem(position);

                TextView tvState = (TextView) convertView.findViewById(R.id.tvTopLeft);

                tvState.setText((getCount() - position) + "." + info.State);

                if (info.State.equals("审核通过"))
                    tvState.setTextColor(context.getResources().getColor(R.color.green));
                else
                    tvState.setTextColor(context.getResources().getColor(R.color.red));

                ((TextView) convertView.findViewById(R.id.tvTopRight)).setText(info.ApplyMan + "-" + info.ApplyGroup);

                convertView.findViewById(R.id.tvCenterOne).setVisibility(View.VISIBLE);
                ((TextView) convertView.findViewById(R.id.tvCenterOne)).setText("延到：" + info.ApplyFinishTime);

                ((TextView) convertView.findViewById(R.id.tvCenterTwo)).setText("原因：" + info.Reason);
                ((TextView) convertView.findViewById(R.id.tvBottomLeft)).setText("");
                ((TextView) convertView.findViewById(R.id.tvBottomRight)).setText("审核人：" + info.VerifyMan);

                return convertView;
            }
        };
    }

    public BaseAdapter getBackInfoAdapt(final Context context) {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                return BackInfoList == null ? 0 : BackInfoList.size();
            }

            @Override
            public CaseBackInfo getItem(int position) {
                return BackInfoList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(context).inflate(R.layout.wx_all_case_detail_item, parent, false);

                CaseBackInfo info = getItem(position);

                ((TextView) convertView.findViewById(R.id.tvTopLeft)).setText((getCount() - position) + "." + info.ActiveName);
                ((TextView) convertView.findViewById(R.id.tvTopRight)).setText(info.BackMan);
                ((TextView) convertView.findViewById(R.id.tvCenterTwo)).setText("原因：" + info.Reason);
                ((TextView) convertView.findViewById(R.id.tvBottomLeft)).setText("");
                ((TextView) convertView.findViewById(R.id.tvBottomRight)).setText(info.BackTime);

                return convertView;
            }
        };
    }
}
