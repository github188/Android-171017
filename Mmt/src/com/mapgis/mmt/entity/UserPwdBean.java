package com.mapgis.mmt.entity;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.db.ISQLiteOper;
import com.mapgis.mmt.db.SQLiteQueryParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuyunfan on 2015/9/2.
 */
public class UserPwdBean implements Serializable, ISQLiteOper {
    private int id;
    private String username;
    private String password;
    private int ipPortid;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getIpPortid() {
        return ipPortid;
    }

    public UserPwdBean() {
    }

    public UserPwdBean(String username, String password, int ipPortid) {
        this.username = username;
        this.password = password;
        this.ipPortid = ipPortid;
    }

    @Override
    public ContentValues generateContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("username", this.username);
        cv.put("password", this.password);
        cv.put("ipPortid", this.ipPortid);
        return cv;
    }

    @Override
    public String getTableName() {
        return "UserPwd";
    }

    @Override
    public void buildFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.username = cursor.getString(1);
        this.password = cursor.getString(2);
        this.ipPortid = cursor.getInt(3);
    }

    @Override
    public String toString() {
        return "ShortMessageBean [id=" + id + ",username=" + username + ", password=" + password + ",ipPortid=" + ipPortid + "]";
    }

    @Override
    public SQLiteQueryParameters getSqLiteQueryParameters() {
        return new SQLiteQueryParameters();
    }

    @Override
    public String getCreateTableSQL() {
        return "(id integer primary key,username,password,ipPortid)";
    }

    public long insert() {

        ArrayList<UserPwdBean> list = DatabaseHelper.getInstance().query(UserPwdBean.class,
                new SQLiteQueryParameters("username = '" + username + "' and ipPortid=" + ipPortid + ""));

        if (list.size() == 0) {
            return DatabaseHelper.getInstance().insert(this);
        }else{
            //修改密码
            ContentValues cv = new ContentValues();
            cv.put("password", password);
            return DatabaseHelper.getInstance().update(UserPwdBean.class,cv,"username = '" + username + "' and ipPortid=" + ipPortid + "");
        }
    }

    public static long delete(String username, String password) {
        IPPortBean ipPortid = IPPortBean.queryLastIPPortBean();
        if (ipPortid == null || ipPortid.getId() < 0) {
            return -1;
        }
        return DatabaseHelper.getInstance().delete(UserPwdBean.class, "username = '" + username + "' and password='" + password + "' and ipPortid=" + ipPortid.getId() + "");
    }

    public static long delete(String username) {
        IPPortBean ipPortid = IPPortBean.queryLastIPPortBean();
        if (ipPortid == null || ipPortid.getId() < 0) {
            return -1;
        }
        return DatabaseHelper.getInstance().delete(UserPwdBean.class, "username = '" + username + "' and ipPortid=" + ipPortid.getId() + "");
    }

    public static long insert(String username, String password) {
        IPPortBean ipPortid = IPPortBean.queryLastIPPortBean();
        if (ipPortid == null || ipPortid.getId() < 0) {
            return -1;
        }
        return new UserPwdBean(username, password, ipPortid.getId()).insert();
    }

    /**
     * 查询最新的服务配置所有的用户密码，倒序输出
     *
     * @return
     */
    public static UserPwdBean querytop() {
        IPPortBean ipPortBean = IPPortBean.queryLastIPPortBean();
        if (ipPortBean == null) {
            return null;
        }
        List<UserPwdBean> userPwdBeans= DatabaseHelper.getInstance().query(UserPwdBean.class,
                new SQLiteQueryParameters("ipPortid=" + ipPortBean.getId(), "id desc"));
        if(userPwdBeans!=null&&userPwdBeans.size()>0){
            return userPwdBeans.get(0);
        }
        return null;
    }
    /**
     * 查询最新的服务配置所有的用户密码，倒序输出
     *
     * @return
     */
    public static List<UserPwdBean> query() {
        IPPortBean ipPortBean = IPPortBean.queryLastIPPortBean();
        if (ipPortBean == null) {
            return null;
        }
        return DatabaseHelper.getInstance().query(UserPwdBean.class,
                new SQLiteQueryParameters("ipPortid=" + ipPortBean.getId(), "id desc"));
    }
    /**
     * 初始化缓存的用户名和密码和默认的用户名和密码
     */
    public static void initData(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = MyApplication.getInstance().getSystemSharedPreferences();
        String usernamesStr = preferences.getString("usernames", "");
        if (BaseClassUtil.isNullOrEmptyString(usernamesStr)) {
            return;
        }
        List<String> usernames = Arrays.asList(usernamesStr.substring(0, usernamesStr.length()).split(","));
        if (usernames == null || usernames.size() <= 0) {
            return;
        }
        for (String username : usernames) {
            String password = preferences.getString(username, "");
            insert(username, password);

            //清除密码缓存
            SharedPreferences  preferencesPwd = MyApplication.getInstance().getSharedPreferences(username, 0);
            if (preferencesPwd != null) {
                preferencesPwd.edit().clear().commit();
            }
        }
        //清除用户名缓存
        SharedPreferences  preferencesUsername = MyApplication.getInstance().getSharedPreferences("usernames", 0);
        if (preferencesUsername != null) {
            preferencesUsername.edit().clear().commit();
        }
        // 不读取配置的用户名和密码
//        insert(preferences.getString("userName",
//                context.getString(R.string.login_default_user_name)), preferences.getString("password",
//                context.getString(R.string.login_default_password)));

    }
}
