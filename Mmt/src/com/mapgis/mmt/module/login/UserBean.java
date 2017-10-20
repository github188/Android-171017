package com.mapgis.mmt.module.login;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.common.util.FileZipUtil;
import com.mapgis.mmt.common.util.NetUtil;
import com.mapgis.mmt.global.MmtBaseTask;

import java.io.File;
import java.util.ArrayList;

/**
 * 用户信息
 *
 * @author Administrator
 */
public class UserBean {
    /********** 服务器获取的用户信息 ***********************/

    /**
     * 用户ID
     */
    public int UserID;

    /**
     * 巡检计划的流程ID
     */
    public int PatrolPlanID;

    /**
     * 手持权限角色名称
     */
    public String Role;

    /**
     * 用户所有权限的角色名称
     */
    public String FullRole;

    /**
     * 所在部门名称
     */
    public String Department;

    /**
     * 所在部门编码
     */
    public String[] DepartCode;

    /**
     * 所在部门名称
     */
    public String[] DepartName;

    /**
     * 组织编码
     */
    public int GroupCode;

    /**
     * 累计登录次数
     */
    public int LoginCount;

    /**
     * 登录用户名
     */
    public String LoginName;

    /**
     * 上次登录时间
     */
    public String LoginTime;

    /**
     * 用户姓名
     */
    public String TrueName;

    /********** 程序运行时所需的内置变量 ***********************/

    /**
     * 是否离线模式
     */
    public boolean isOffline = false;

    /**
     * 用户密码
     */
    public String password;
    /**
     * 用户头像名
     */
    public String Icon;
    /**
     * 用户电话号码
     */
    public String TelPhone;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /**
     * 获取用户所属的站点
     *
     * @return 所属站点列表，若没有所属站定，则返回 size() == 0的列表
     */
    public ArrayList<String> getBelongStation() {
        ArrayList<String> stationList = new ArrayList<>();
        if (!TextUtils.isEmpty(FullRole)) {
            String[] stations = FullRole.split(",");
            for (String station : stations) {
                if (station.startsWith("站点_")) {
                    stationList.add(station);
                }
            }
        }
        return stationList;
    }

    public String getHeadIcoAbsPath() {
        return "UserImage/" + UserID + "user.png";
    }

    public String getHeadIcoLocaFullPath() {
        return Battle360Util.getFixedPath("UserImage") + UserID + "user.png";
    }

    public void setHeadIco(Context context, final ImageView iv) {
        if (iv == null) {
            return;
        }
        iv.setImageResource(R.drawable.default_user);
        //图标指定，不从Icon取，因为和webgis不兼容
        final String icoPath = getHeadIcoAbsPath();
        String icoLoaclPath = getHeadIcoLocaFullPath();

        final File icoFile = new File(icoLoaclPath);
        if (icoFile.exists()) {
            Bitmap userIcoBitmap = FileZipUtil.getBitmapFromFile(icoFile);
            if (userIcoBitmap != null) {
                iv.setImageBitmap(userIcoBitmap);
            }
            return;
        }

        new MmtBaseTask<Void, Void, Void>(context) {
            @Override
            protected Void doInBackground(Void... params) {
                NetUtil.downloadFile(icoPath, icoFile);
                return null;
            }

            @Override
            protected void onSuccess(Void aVoid) {
                super.onSuccess(aVoid);
                if (icoFile.exists()) {
                    Bitmap userIcoBitmap = FileZipUtil.getBitmapFromFile(icoFile);
                    if (userIcoBitmap != null) {
                        iv.setImageBitmap(userIcoBitmap);
                    }
                }
            }
        }.mmtExecute();

    }
}