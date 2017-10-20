package com.patrol.module.posandpath;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapgis.mmt.R;
import com.patrol.module.posandpath.beans.UserBean;

import java.util.ArrayList;

/**
 * User: zhoukang
 * Date: 2016-03-15
 * Time: 15:30
 * <p/>
 * trunk:
 */
public class PosAndPathAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<UserBean.BodyInfo> Ppoint;

    public PosAndPathAdapter(Context context, ArrayList Ppoint) {
        this.context = context;
        this.Ppoint = Ppoint;
    }

    public void setPpoint(ArrayList<UserBean.BodyInfo> ppoint) {
        Ppoint = ppoint;
    }

    @Override
    public int getCount() {
        return Ppoint.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.list_item_view, null);
            holder.tvName = (TextView) view.findViewById(R.id.tv_userName);
            holder.ivIcon = (ImageView)view.findViewById(R.id.iv_userIcon);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvName.setText(Ppoint.get(i).Perinfo.name);
        if (Ppoint.get(i).Perinfo.IsOline.equals("1")){
            // 在线
            holder.ivIcon.setBackgroundResource(R.drawable.button_default_pressed);
        }else{
            holder.ivIcon.setBackgroundResource(R.drawable.button_default_normal);
        }
        return view;
    }

    static class ViewHolder {
        public TextView tvName;
        public ImageView ivIcon;
    }
}
