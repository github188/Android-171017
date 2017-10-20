package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Battle360Util;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;
import com.mapgis.mmt.module.login.ServerConfigInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyunfan on 2015/9/2.
 */
public class IPPortBean implements Serializable, ISQLiteOper {
    private int id;
    private String ip;
    private String port;
    private String virtualPath;

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getVirtualPath() {
        return virtualPath;
    }

    public IPPortBean() {
    }

    public IPPortBean(String ip, String port, String virtualPath) {
        this.ip = ip;
        this.port = port;
        this.virtualPath = virtualPath;
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("ip", this.ip);
        cv.put("port", this.port);
        cv.put("virtualPath", this.virtualPath);
        return cv;
    }

    @Override
    public String getTableName() {
        return "IPPort";
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.ip = cursor.getString(1);
        this.port = cursor.getString(2);
        this.virtualPath = cursor.getString(3);
    }

    @Override
    public String toString() {
        return "ShortMessageBean [id=" + id + ", ip=" + ip + ", port=" + port + ",virtualPath=" + virtualPath + "]";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return new SQLiteQueryParameters();
    }

    @Override
    public String getCreateTableSQL() {
        return "(id integer primary key,ip,port,virtualPath)";
    }

    /**
     * 删除该服务地址，并联动删除该服务的用户
     *
     * @param index
     * @return
     */
    public static void delete(IPPortBean ipPortBean) {
        if (ipPortBean != null && ipPortBean.getId() > 0) {
            if (DatabaseHelper.getInstance().delete(IPPortBean.class, "id= " + ipPortBean.getId() + "") > 0) {
                DatabaseHelper.getInstance().delete(UserPwdBean.class, "ipPortid = " + ipPortBean.getId() + "");
            }
        }
    }

    /**
     * 插入前删除该记录，保持最后插入的数据是最新的一条
     * 并且该服务的用户跟着联动改变
     *
     * @return
     */
    public long insert() {
        int oldid = 0, newid = 0;
        ArrayList<IPPortBean> listOld = DatabaseHelper.getInstance().query(IPPortBean.class,
                new SQLiteQueryParameters("ip = '" + ip + "' and port='" + port + "' and virtualPath='" + virtualPath + "'"));
        if (listOld.size() > 0) {
            oldid = listOld.get(0).getId();
        }
        DatabaseHelper.getInstance().delete(IPPortBean.class, "ip = '" + ip + "' and port='" + port + "' and virtualPath='" + virtualPath + "'");
        ArrayList<IPPortBean> list = DatabaseHelper.getInstance().query(IPPortBean.class,
                new SQLiteQueryParameters("ip = '" + ip + "' and port='" + port + "' and virtualPath='" + virtualPath + "'"));
        if (list.size() == 0) {
            long ret = DatabaseHelper.getInstance().insert(this);
            ArrayList<IPPortBean> listNew = DatabaseHelper.getInstance().query(IPPortBean.class,
                    new SQLiteQueryParameters("ip = '" + ip + "' and port='" + port + "' and virtualPath='" + virtualPath + "'"));
            if (listNew.size() > 0) {
                newid = listNew.get(0).getId();
            }
            if (oldid > 0 && newid > 0) {
                ContentValues cv = new ContentValues();
                cv.put("ipPortid", newid);
                DatabaseHelper.getInstance().update(UserPwdBean.class, cv, "ipPortid=" + oldid);
            }
            return ret;
        }

        return -1;
    }

    /**
     * 不存在就插入
     *
     * @return
     */
    public long insertNoExist() {
        ArrayList<IPPortBean> list = DatabaseHelper.getInstance().query(IPPortBean.class,
                new SQLiteQueryParameters("ip = '" + ip + "' and port='" + port + "' and virtualPath='" + virtualPath + "'"));
        if (list.size() == 0) {
            return DatabaseHelper.getInstance().insert(this);
        }

        return -1;
    }

    public static List<IPPortBean> query(String ip, String port, String virtualPath) {
        return DatabaseHelper.getInstance().query(IPPortBean.class,
                new SQLiteQueryParameters("ip = '" + ip + "' and port='" + port + "' and virtualPath='" + virtualPath + "'"));
    }

    /**
     * 查询所有的，倒序输出
     *
     * @return
     */
    public static List<IPPortBean> query() {
        return DatabaseHelper.getInstance().query(IPPortBean.class,
                new SQLiteQueryParameters("1=1", "id desc"));
    }

    /**
     * 取最新的一条数据
     *
     * @return
     */
    public static IPPortBean queryLastIPPortBean() {
        List<IPPortBean> temp = query();
        if (temp != null && temp.size() > 0) {
            return temp.get(0);
        }
        return null;
    }

    /**
     * 初始化ip和port
     */
    public static void initData(Context context) {
        if (context == null) {
            return;
        }
        List<IPPortBean> remainData= query();
        if(remainData!=null&&remainData.size()>0){
            return;
        }
        //优先从本地json文件读取用户地址
        ServerConfigInfo info = null;
        try {

            File file = new File(Battle360Util.getFixedPath("Data") + MyApplication.getInstance().getPackageName()
                    + "/login_config.json");

            if (!file.exists()) {
                file = new File(Battle360Util.getFixedPath("conf") + "login_config.json");
            }
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                info = new Gson().fromJson(reader, ServerConfigInfo.class);

                if (info != null) {
                    new IPPortBean(info.IpAddress, info.Port, info.VirtualPath).insertNoExist();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            info=null;
        }

        //本地json文件不存在用户服务器地址则读取配置的ip和端口
        if (info == null) {
            String ip = context.getString(R.string.login_server_ip);
            String port = context.getString(R.string.login_server_port);
            String VirtualPath = context.getString(R.string.login_server_virtual_path);
            if (BaseClassUtil.isNullOrEmptyString(ip) || BaseClassUtil.isNullOrEmptyString(port) || BaseClassUtil.isNullOrEmptyString(VirtualPath)) {
                return;
            }
            new IPPortBean(ip, port, VirtualPath).insertNoExist();
        }

    }
}
