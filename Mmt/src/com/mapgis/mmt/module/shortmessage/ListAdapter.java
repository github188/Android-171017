package com.mapgis.mmt.module.shortmessage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.entity.ShortMessageBean;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private List<ShortMessageBean> msgs;
    private String mType = "发件人";

    public ListAdapter(Context context, ArrayList<ShortMessageBean> msgs) {
        inflater = LayoutInflater.from(context);
        this.msgs = msgs;
    }

    public void setMsgsList(ArrayList<ShortMessageBean> msgs) {
        this.msgs = msgs;
    }

    public void setType(String type){
        this.mType = type;
    }

    @Override
    public int getCount() {
        return msgs.size();
    }

    @Override
    public Object getItem(int position) {
        return msgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.msg_item, null);
            holder.msgItemImage = (ImageView) convertView.findViewById(R.id.msgItemImage);
            holder.msgItemDate = (TextView) convertView.findViewById(R.id.msgItemDate);
            holder.msgItemDetail = (TextView) convertView.findViewById(R.id.msgItemDetail);
            holder.msgItemReciever = (TextView) convertView.findViewById(R.id.msgItemReciever);
            holder.msgItemDelete = (ImageButton) convertView.findViewById(R.id.msgItemDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (msgs.get(position).getMsgDetail().endsWith(",")) {
            final String detail = msgs.get(position).getMsgDetail().split("#")[0];
            holder.msgItemDetail.setText(detail);
            holder.msgItemReciever.setText("收件人: "
                    + msgs.get(position).getMsgDetail().split("#")[1].substring(0,
                    msgs.get(position).getMsgDetail().split("#")[1].length() - 1));
        } else {
            holder.msgItemDetail.setText(msgs.get(position).getMsgDetail().split("#")[0]);
            if (msgs.get(position).getMsgDetail().split("#").length > 1) {
                holder.msgItemReciever.setText(String.format("%s: %s", mType,msgs.get(position).getMsgDetail().split("#")[1]));
            }
        }

        String time = msgs.get(position).getMsgTime();
        if (time.contains(" ")) {
            time = time.replace(" ", "\n  ");
        }

        holder.msgItemDate.setText(time);

        if (msgs.get(position).getMsgState() != 0) {
            holder.msgItemImage.setVisibility(View.INVISIBLE);
        } else {
            holder.msgItemImage.setVisibility(View.VISIBLE);
        }

        holder.msgItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMsg(msgs.get(position));
                msgs.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    // 删除短信息
    private void deleteMsg(ShortMessageBean msg) {
        DatabaseHelper.getInstance().delete(ShortMessageBean.class, "msgId = " + msg.getMsgId());
    }

    final class ViewHolder {
        public ImageView msgItemImage;
        public TextView msgItemDate;
        public TextView msgItemDetail;
        public TextView msgItemReciever;
        public ImageButton msgItemDelete;
    }
}