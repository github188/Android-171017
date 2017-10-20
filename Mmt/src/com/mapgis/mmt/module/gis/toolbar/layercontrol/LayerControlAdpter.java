package com.mapgis.mmt.module.gis.toolbar.layercontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.MmtViewHolder;

import java.util.List;

/**
 * Created by lyunfan on 17/3/3.
 */

public class LayerControlAdpter extends BaseAdapter {

    private Context context;
    private List<LayerControlItem> datas;
    private int count = 0;

    public LayerControlAdpter(Context context, List<LayerControlItem> datas) {
        if (datas == null) {
            return;
        }
        this.context = context;
        this.datas = datas;
        count = datas.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layercontrol_item, parent, false);
        }

        ImageView imageView = MmtViewHolder.get(convertView, R.id.dx_img);
        TextView textView = MmtViewHolder.get(convertView, R.id.dx_name);

        LayerControlItem data = datas.get(position);
        imageView.setImageBitmap(data.bitmap);
        textView.setText(data.name);

        if (data.isShow) {
            convertView.setBackgroundResource(R.drawable.dx_image_shape);
            textView.setTextColor(0xff2881a2);
        } else {
            convertView.setBackgroundResource(0);
            textView.setTextColor(0xff333333);

        }

        return convertView;
    }
}
