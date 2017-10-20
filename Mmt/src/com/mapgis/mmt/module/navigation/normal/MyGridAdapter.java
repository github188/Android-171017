package com.mapgis.mmt.module.navigation.normal;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.MmtViewHolder;
import com.mapgis.mmt.common.util.ResourceUtil;
import com.mapgis.mmt.constant.NavigationMenuRegistry;
import com.mapgis.mmt.module.navigation.NavigationActivity;
import com.mapgis.mmt.module.navigation.NavigationItem;

import java.util.ArrayList;

public class MyGridAdapter extends BaseAdapter implements View.OnClickListener {

    private Context mContext;

    private ArrayList<NavigationItem> items;
    private int count = 0;

    private boolean useCustormMenuIco = false;
    // Whether show exact tip num, rather than "99+".
    private boolean showExactTipNum = false;

    //最后一个菜单图标值
    private int icoVal = 0;

    public MyGridAdapter(Context mContext, ArrayList<NavigationItem> items) {
        try {
            this.mContext = mContext;
            this.items = items;

            count = items.size();
            while (count % 3 != 0) {
                count++;
            }

            useCustormMenuIco = MyApplication.getInstance().getConfigValue("useCustormMenuIco", 0) == 1;
            showExactTipNum = MyApplication.getInstance().getConfigValue("ShowExactTipNum", 0) == 1;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
            }

            TextView tv = MmtViewHolder.get(convertView, R.id.tv_item);
            ImageView iv = MmtViewHolder.get(convertView, R.id.iv_item);
            TextView tvNum = MmtViewHolder.get(convertView, R.id.tvNum);

            if (position < items.size()) {
                // Real items
                NavigationItem item = items.get(position);
                int icon = -1;
                if (useCustormMenuIco) {
                    icon = ResourceUtil.getDrawableResourceId(mContext, item.Function.Icon);
                }

                if (icon <= 0) {
                    icon = NavigationMenuRegistry.getInstance().getMenuInstance((NavigationActivity) mContext, item).getIcons()[1];
                }
                //记录最后菜单图标，方便控制后面空格大小
                if (position == items.size() - 1) {
                    icoVal = icon;
                }
                iv.setImageResource(icon);
                iv.setVisibility(View.VISIBLE);

                tv.setText(item.Function.Alias);

                tvNum.setVisibility(View.INVISIBLE);

                if (!TextUtils.isEmpty(item.Count)) {

                    int num = Integer.valueOf(item.Count.trim());

                    final int visible = num > 0 ? View.VISIBLE : View.INVISIBLE;
                    if (tvNum.getVisibility() != visible) {
                        tvNum.setVisibility(visible);
                    }

                    if (num > 0) {
                        if (showExactTipNum) {
                            tvNum.setText(String.valueOf(num));
                        } else {
                            tvNum.setText(num > 99 ? "99+" : String.valueOf(num));
                        }
                    }
                }

            } else {

                // Empty placeholder
                iv.setVisibility(View.INVISIBLE);
                tvNum.setVisibility(View.INVISIBLE);
                tv.setText("");
                if (icoVal == 0) {
                    icoVal = R.drawable.home_gps;
                }
                iv.setBackgroundResource(icoVal);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return convertView;
    }


    @Override
    public void onClick(View v) {
        try {
            String alias = ((TextView) v.findViewById(R.id.tv_item)).getText().toString();

            for (NavigationItem item : items) {

                if (item.Function.Alias.equals(alias)) {
                    ((NavigationActivity) mContext).onNavigationItemClick(item);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
