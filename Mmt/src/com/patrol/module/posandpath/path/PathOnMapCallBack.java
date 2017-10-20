package com.patrol.module.posandpath.path;

import android.content.Context;
import android.os.Message;

import com.mapgis.mmt.entity.BaseMapCallback;
import com.patrol.module.posandpath.beans.PointInfo;

import java.util.ArrayList;

/**
 * User: zhoukang
 * Date: 2016-03-24
 * Time: 14:41
 * <p/>
 * trunk:
 */
public class PathOnMapCallBack extends BaseMapCallback {
    private Context mContext;
    private ArrayList<PointInfo> points;

    public PathOnMapCallBack(Context context, ArrayList<PointInfo> points) {
        mContext = context;
        this.points = points;
    }

    @Override
    public boolean handleMessage(Message message) {
//        PathOnMapMenu menu = new PathOnMapMenu(mapGISFrame,mContext);
        PathMapMenu menu = new PathMapMenu(mContext,mapGISFrame,points);
        mapGISFrame.getFragment().menu = menu;

        return menu.onOptionsItemSelected();
    }
}
