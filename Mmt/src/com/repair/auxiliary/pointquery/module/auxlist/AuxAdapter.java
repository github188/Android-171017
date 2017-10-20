package com.repair.auxiliary.pointquery.module.auxlist;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxData;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.pointquery.entity.AuxDic;
import com.mapgis.mmt.R;

import java.util.List;

/**
 * Created by liuyunfan on 2016/4/13.
 */
public class AuxAdapter extends BaseAdapter {
    private final List<AuxData> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Activity mContext;

    public AuxAdapter(Activity mActivity, List<AuxData> dataList) {
        this.mContext = mActivity;
        this.mLayoutInflater = LayoutInflater.from(mActivity);
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.single_textview_item, parent, false);
        }

        AuxData auxData = dataList.get(position);
        List<String> displayFieldList = auxData.displayFieldList;
        StringBuilder sb = new StringBuilder();
        List<AuxDic> attrs = auxData.attributes;
        //列表中只显示前5个
        for (int i = 0; i < displayFieldList.size() && i < 5; i++) {
            String key = displayFieldList.get(i);
            for (AuxDic auxDic : attrs) {
                if (auxDic.Key.equals(key)) {
                    sb.append(key + ":" + auxDic.Value + "\n");
                    break;
                }
            }
        }
        String content = sb.toString();
        if (content.length() > 2) {
            content = content.substring(0, content.length() - 1);
        }
        ((TextView) MmtViewHolder.get(convertView, R.id.index)).setText((position + 1) + ".");
        ((TextView) MmtViewHolder.get(convertView, R.id.content)).setText(content);
        return convertView;
    }
}
