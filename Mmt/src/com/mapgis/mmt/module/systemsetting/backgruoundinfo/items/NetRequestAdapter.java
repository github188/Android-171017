package com.mapgis.mmt.module.systemsetting.backgruoundinfo.items;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.entity.NetLogInfo;

import java.util.ArrayList;

/**
 * zhouxixiang 2017-9-1
 */

public class NetRequestAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<NetLogInfo> data;

    public NetRequestAdapter(Context context, ArrayList<NetLogInfo> data) {
        this.context = context;
        this.data = data;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_net_request, null);

                holder.netRequestItemIndex = (TextView) convertView.findViewById(R.id.netRequestItemIndex);
                holder.netRequestItemCode = (TextView) convertView.findViewById(R.id.netRequestItemCode);
                holder.netRequestItemType = (TextView) convertView.findViewById(R.id.netRequestItemType);
                holder.netRequestItemStatus = (TextView) convertView.findViewById(R.id.netRequestItemStatus);
                holder.netRequestItemInterface = (TextView) convertView.findViewById(R.id.netRequestItemInterface);
                holder.netRequestItemTime = (TextView) convertView.findViewById(R.id.netRequestItemTime);
                holder.netRequestItemSpan = (TextView) convertView.findViewById(R.id.netRequestItemSpan);
                holder.netRequestItemSend = (TextView) convertView.findViewById(R.id.netRequestItemSendTraffic);
                holder.netRequestItemReceive = (TextView) convertView.findViewById(R.id.netRequestItemReceiveTraffic);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            initView(holder, position);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private void initView(ViewHolder holder, final int position) {
        holder.netRequestItemIndex.setText(position + 1 + ".");
        holder.netRequestItemInterface.setText(data.get(position).requestInterface);
        holder.netRequestItemStatus.setText(data.get(position).isSuccess == 1 ? "成功" : "失败");
        holder.netRequestItemStatus.setTextColor(data.get(position).isSuccess == 1 ? Color.BLACK : Color.RED);
        holder.netRequestItemType.setText(data.get(position).requestType);
        holder.netRequestItemCode.setText(String.valueOf(data.get(position).responseCode));
        holder.netRequestItemSend.setText(formatTraffic(data.get(position).sendBytes));
        holder.netRequestItemReceive.setText(formatTraffic(data.get(position).receiveBytes));
        holder.netRequestItemTime.setText(data.get(position).startTime);
        holder.netRequestItemSpan.setText(data.get(position).timeSpan + "ms");

    }

    static class ViewHolder {
        public TextView netRequestItemIndex, netRequestItemCode, netRequestItemType, netRequestItemStatus,
                netRequestItemInterface, netRequestItemTime, netRequestItemSpan, netRequestItemSend, netRequestItemReceive;

    }

    public void showDetail(int position) {
        try {
            Intent intent = new Intent(context, RequestDetailActivity.class);
            intent.putExtra("data", data.get(position));
            context.startActivity(intent);
            MyApplication.getInstance().startActivityAnimation((Activity) context);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String formatTraffic(long data) {
        if (data < 0) {
            return "0byte";
        } else {
            if (data > 1000) {
                return Convert.FormatDouble(data / 1000f, ".00") + "kb";
            } else {
                return data + "byte";
            }
        }
    }
}
