package com.patrolproduct.module.projectquery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.R;
import com.patrolproduct.module.projectquery.queryutil.QueryLayerUtil;
import com.zondy.mapgis.featureservice.Feature;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by KANG on 2016/9/6.
 * 工程名称查询结果的数据适配器
 */
public class ListPagerAdapter extends BaseAdapter {
    private ArrayList<Feature> listFeature;
    private Context context;
//    private String layerName;

    public ListPagerAdapter(Context context, ArrayList listFeature) {
        this.context = context;
        this.listFeature = listFeature;
//        this.layerName = layerName;

//        this.layer = layer;
    }

    @Override
    public int getCount() {
        return listFeature == null ? 0 : listFeature.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_projectquery_view, null);

            holder.itemIndex = (TextView) convertView.findViewById(R.id.itemIndex);
            holder.itemLeftTop = (TextView) convertView.findViewById(R.id.itemLeftTop);
            holder.itemDate = (TextView) convertView.findViewById(R.id.itemDate);
            holder.itemRightTop = (TextView) convertView.findViewById(R.id.itemRightTop);
            holder.itemMiddle = (TextView) convertView.findViewById(R.id.itemMiddle);
            holder.itemLeftBottom = (TextView) convertView.findViewById(R.id.itemLeftBottom);
            holder.itemRightBottom = (TextView) convertView.findViewById(R.id.itemRightBottom);
            holder.itemLocation = (TextView) convertView.findViewById(R.id.itemLocation);
            holder.itemDetail = (TextView) convertView.findViewById(R.id.itemDetail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final HashMap<String, String> map = QueryLayerUtil.featureToHashMap(listFeature.get(position));

        holder.itemIndex.setText((position + 1) + "");
        holder.itemDate.setText(map.get("探测年份"));
        holder.itemLeftTop.setText(map.get("编号"));
        holder.itemRightTop.setText(map.get("工程名称"));
        holder.itemMiddle.setText(map.get("探测单位"));
        holder.itemLeftBottom.setText(map.get("道路名称"));
        holder.itemRightBottom.setText(map.get("地面建筑物"));

        holder.itemLocation.setText("定位");
        holder.itemLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dot dot = new Dot(Double.valueOf(map.get("X坐标")), Double.valueOf(map.get("Y坐标")));
//
//                MyApplication.getInstance().sendToBaseMapHandle(
//                        new TaskLocationOnMapCallback(dot, map.get("编号"), ""));
//                Intent intent = new Intent((QueryProjectActivity)context, MapGISFrame.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                context.startActivity(intent);

                BaseMapCallback callback = new ShowMapPointCallback(context, dot.toString(),
                        map.get("编号"), null, -1);

                MyApplication.getInstance().sendToBaseMapHandle(callback);
            }
        });

        holder.itemDetail.setText("详情");
//        holder.itemDetail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        return convertView;
    }


    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        public TextView itemIndex;
        public TextView itemLeftTop;
        public TextView itemDate;
        public TextView itemRightTop;
        public TextView itemMiddle;
        public TextView itemLeftBottom;
        public TextView itemRightBottom;
        public TextView itemLocation;
        public TextView itemDetail;
    }
}
