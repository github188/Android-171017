package com.patrol.module.posandpath2;

import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.widget.tree.ChildTreeNode;
import com.mapgis.mmt.common.widget.tree.ParentTreeNode;
import com.mapgis.mmt.common.widget.tree.TreeSelectableAdapter;
import com.mapgis.mmt.entity.BaseMapCallback;
import com.mapgis.mmt.module.gis.toolbar.BaseMapMenu;
import com.patrol.module.posandpath2.beans.DeptBean;
import com.patrol.module.posandpath2.beans.PersonInfo;
import com.patrol.module.posandpath2.beans.UserInfo;
import com.zondy.mapgis.geometry.Dot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Comclay on 2016/12/21.
 * 用户选择列表，按照部门来分组
 */

public class UserSelectableAdapter extends TreeSelectableAdapter {

    public UserSelectableAdapter(Context context, List<ParentTreeNode<DeptBean>> mNodeList) {
        super(context, mNodeList);
    }

    /**
     * 用部门来分组
     * 父类中已经复用
     */
    @Override
    protected View getGroupItemView(int groupPosition, boolean isExpanded, ViewGroup parentView) {
        return View.inflate(this.mContext, R.layout.item_group_dept_view, parentView);
    }

    /**
     * 显示部门的名称
     */
    @Override
    protected void initGroupView(GroupHolder groupHolder, int groupPosition) {
        ParentTreeNode group = getGroup(groupPosition);
        Object object = group.getObject();
        if (object instanceof DeptBean) {
            // 统计该部门中的在线人数
            int count = totalOnLineCount(group);
            String deptName = ((DeptBean) object).DeptName;
            ((TextView) groupHolder.view.findViewById(R.id.tv_dept)).setText(
                    String.format("%s  %d/%d", deptName, count, group.getChildNodeSize()));
        }
    }

    /**
     * 统计部门中的在线人数
     *
     * @param group 部门
     */
    private int totalOnLineCount(ParentTreeNode group) {
        int count = 0;
        List<ChildTreeNode> nodeList = group.getNodeList();
        for (ChildTreeNode childNode : nodeList) {
            UserInfo userInfo = (UserInfo) childNode.getObject();
            if ("1".equals(userInfo.Perinfo.IsOline)) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }

    /**
     * 每个部门的用户列表
     */
    @Override
    protected View getChildItemView(int groupPosition, int childPosition, ViewGroup parentView) {
        return View.inflate(this.mContext, R.layout.item_child_user_view, parentView);
    }

    /**
     * 用来显示用户的信息
     */
    @Override
    protected void initChildView(ChildHolder childHolder
            , int groupPosition, int childPosition) {
        ImageView ivIcon = (ImageView) childHolder.view.findViewById(R.id.iv_child_icon);
        TextView tvName = (TextView) childHolder.view.findViewById(R.id.userName);
        ImageButton ibPosition = (ImageButton) childHolder.view.findViewById(R.id.positionButton);
        ImageButton ibTrace = (ImageButton) childHolder.view.findViewById(R.id.viewTraceButton);

        final Object obj = getChild(groupPosition, childPosition).getObject();
        if (obj instanceof UserInfo) {
            PersonInfo perinfo = ((UserInfo) obj).Perinfo;
            if ("1".equals(perinfo.IsOline)) {
                ivIcon.setImageResource(R.drawable.patrol_on);
                tvName.setText(perinfo.name);
                tvName.setTextColor(this.mContext.getResources().getColor(R.color.tv_patrol_on));
                ibPosition.setImageResource(R.drawable.position_normal);
                ibTrace.setImageResource(R.drawable.trace_button_normal);
            } else {
                ivIcon.setImageResource(R.drawable.patrol_off);
                tvName.setText(String.format("%s（离线）", perinfo.name));
                tvName.setTextColor(this.mContext.getResources().getColor(R.color.tv_patrol_off));
                ibPosition.setImageResource(R.drawable.position);
                ibTrace.setImageResource(R.drawable.trace_button);
            }

            ibPosition.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toPosition((UserInfo) obj);
                }
            });

            ibTrace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toPath((UserInfo) obj);
                }
            });
        }
    }

    private void toPosition(final UserInfo userInfo) {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                try {
                    BaseMapMenu menu = mapGISFrame.getFragment().menu;
                    if (menu instanceof PosAndPathMapMenu2) {
                        ((PosAndPathMapMenu2) menu).initMapLayoutView(PosAndPathMapMenu2.Case.CASE_USER_INFO_LIST_POS);
                        ArrayList<UserInfo> infoList = new ArrayList<>();
                        String position = userInfo.point.Position;
                        if (BaseClassUtil.isNullOrEmptyString(position)) {
                            Toast.makeText(mContext, "位置无效", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        String p[] = position.split(",");
                        Dot dot = new Dot(Double.valueOf(p[0]), Double.valueOf(p[1]));
                        mapView.panToCenter(dot, false);
                        infoList.add(userInfo);
                        ((PosAndPathMapMenu2) menu).setmPosUserInfo(userInfo);
                        ((PosAndPathMapMenu2) menu).showUsersOnMap(infoList);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        if (mContext instanceof UserInfoListActivity) {
            ((UserInfoListActivity) mContext).backToMapView();
        }
    }

    private void toPath(final UserInfo userInfo) {
        MyApplication.getInstance().sendToBaseMapHandle(new BaseMapCallback() {
            @Override
            public boolean handleMessage(Message msg) {
                BaseMapMenu menu = mapGISFrame.getFragment().menu;
                if (menu instanceof PosAndPathMapMenu2) {
                    PersonInfo personInfo = userInfo.Perinfo;
                    ((PosAndPathMapMenu2) menu).initMapLayoutView(PosAndPathMapMenu2.Case.CASE_USER_INFO_LIST_TRACE);
                    ((PosAndPathMapMenu2) menu).showPathOnMap(
                            personInfo.USERID
                            , personInfo.name
                            , ((PosAndPathMapMenu2) menu).mSimpleDateFormat.format(new Date()));
                }

                return true;
            }
        });
        if (mContext instanceof UserInfoListActivity) {
            ((UserInfoListActivity) mContext).backToMapView();
        }
    }
}
