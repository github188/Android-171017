package com.repair.auxiliary.conditionquery.module;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.BaseActivity;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.ShowMapPointCallback;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.AuxUtils;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.AuxDataResult;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.ConditionQueryAdapterData;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.FindResult;
import com.mapgis.mmt.module.gis.toolbar.query.auxiliary.conditionquery.entity.Geometry;
import com.mapgis.mmt.R;
import com.patrol.entity.KeyPoint;
import com.patrol.module.KeyPoint.PointDetailFragment;

import java.util.HashMap;
import java.util.List;

/**
 * Created by liuyunfan on 2016/4/20.
 */
public class ConditionQueryAuxAdapter extends BaseAdapter {
    private final List<ConditionQueryAdapterData> dataList;
    private final LayoutInflater mLayoutInflater;
    private final Activity mContext;

    public ConditionQueryAuxAdapter(Activity mActivity, List<ConditionQueryAdapterData> dataList) {
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
            convertView = mLayoutInflater.inflate(R.layout.single_textview_btn_item, parent, false);
        }
        final ConditionQueryAdapterData data = dataList.get(position);
        final HashMap<String, String> attrs = data.atts;
        StringBuilder sb = new StringBuilder();

        //是否配置了列表
        //附属数据表名:item1,item2,item3,item4
        String listConfig = MyApplication.getInstance().getConfigValue(data.auxTableName);

        if (TextUtils.isEmpty(listConfig)) {

            //列表中只显示前5个
            int j = 1;
            for (String key : attrs.keySet()) {
                if (j > 5) {
                    break;
                }
                if (!BaseClassUtil.isContainChinese(key)) {
                    continue;
                }
                String val = attrs.get(key);

                sb.append(key + ": " + val + "\n");
                j++;
            }
        } else {
            String[] items = listConfig.split(",");
            for (String item : items) {
                if (attrs.containsKey(item)) {
                    sb.append(item + ": " + attrs.get(item) + "\n");
                }
            }
        }
        String content = sb.toString();
        if (content.length() > 2) {
            content = content.substring(0, content.length() - 1);
        }
        ((TextView) MmtViewHolder.get(convertView, R.id.index)).setText((position + 1) + ".");
        ((TextView) MmtViewHolder.get(convertView, R.id.content)).setText(content);
        MmtViewHolder.get(convertView, R.id.maintenanceListItemLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String layerID = data.layerID;
                if (TextUtils.isEmpty(layerID)) {
                    Toast.makeText(mContext, "图层未知，无法定位", Toast.LENGTH_SHORT).show();
                    return;
                }
                String OID = data.atts.get("OID");
                if (TextUtils.isEmpty(OID)) {
                    Toast.makeText(mContext, "OID未知，无法定位", Toast.LENGTH_SHORT).show();
                    return;
                }
                String strAuxTableName = data.auxTableName;
                if (TextUtils.isEmpty(strAuxTableName)) {
                    Toast.makeText(mContext, "附属数据表未知，无法定位", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (AuxUtils.isWebgis3()) {
                    final AuxDataResult auxDataResult = new AuxDataResult();
                    auxDataResult.getSingleAuxDataResult(mContext, layerID, OID, "0", strAuxTableName, new AuxUtils.AfterOnsucess() {
                        @Override
                        public void afterSucess() {
                            try {
                                Geometry geometry = auxDataResult.Result.get(0).features[0].geometry;
                                BaseMapCallback callback = new ShowMapPointCallback(mContext, geometry.x + "," + geometry.y,
                                        "", "", -1);
                                MyApplication.getInstance().sendToBaseMapHandle(callback);
                            } catch (Exception ex) {
                                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    final FindResult findResult = new FindResult();
                    findResult.getSingleFindResult(mContext, layerID, OID, "0", strAuxTableName, new AuxUtils.AfterOnsucess() {
                        @Override
                        public void afterSucess() {
                            try {
                                Geometry geometry = findResult.features[0].geometry;
                                BaseMapCallback callback = new ShowMapPointCallback(mContext, geometry.x + "," + geometry.y,
                                        "", "", -1);
                                MyApplication.getInstance().sendToBaseMapHandle(callback);
                            } catch (Exception ex) {
                                Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        MmtViewHolder.get(convertView, R.id.maintenanceListItemDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lookDeatil(attrs);
            }
        });

        return convertView;
    }

    public void lookDeatil(HashMap<String, String> attrs) {
        PointDetailFragment fragment = new PointDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("kp", new KeyPoint());
        args.putSerializable("attr", attrs);
        fragment.setArguments(args);
        fragment.show(((BaseActivity) mContext).getSupportFragmentManager(), "");
    }
}
