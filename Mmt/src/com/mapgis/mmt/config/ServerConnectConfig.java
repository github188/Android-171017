package com.mapgis.mmt.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.entity.IPPortBean;
import com.mapgis.mmt.entity.UserPwdBean;
import com.mapgis.mmt.module.login.ServerConfigInfo;

/**
 * 服务器连接信息（IP地址、端口号、虚拟目录）加载类
 *
 * @author Zoro
 */
public class ServerConnectConfig {
    private static ServerConnectConfig instance = null;

    public static ServerConnectConfig getInstance() {
        if (instance == null) {
            instance = new ServerConnectConfig();
        }

        return instance;
    }

    public static void setInstance(ServerConnectConfig instance) {
        ServerConnectConfig.instance = instance;
    }

    private ServerConfigInfo info;

    public ServerConfigInfo getServerConfigInfo() {
        return info;
    }

    public void setInfo(ServerConfigInfo info) {
        this.info = info;
    }

    public void setGpsReceiver(String receiver) {
        if (this.info != null)
            this.info.GpsReceiver = receiver;
    }

    /**
     * 加载登录所需参数信息，包括默认登录名及密码，IP地址，端口号，虚拟目录
     */
    public void loadLoginInfo(Context context, boolean fromSetting) {
        info = new ServerConfigInfo();

        info.IpAddress = context.getString(R.string.login_server_ip);
        info.Port = context.getString(R.string.login_server_port);
        info.VirtualPath = context.getString(R.string.login_server_virtual_path);
        info.HttpProtocol = context.getString(R.string.login_server_http_protocol_type);

        info.LoginName = context.getString(R.string.login_default_user_name);
        info.LoginPassword = context.getString(R.string.login_default_password);

        IPPortBean bean = IPPortBean.queryLastIPPortBean();
        if (bean == null) {
            return;
        }

        SharedPreferences sf = MyApplication.getInstance().getSystemSharedPreferences();
        String key = sf.getString("LoginStyle", "Unify");

        if (!key.equals("Unify") || MyApplication.getInstance().getConfigValue("NeedUnifyLogin", 0) <= 0 || fromSetting) {
            info.IpAddress = bean.getIp();
            info.Port = bean.getPort();
            info.VirtualPath = bean.getVirtualPath();
        }

        if (MyApplication.getInstance().getConfigValue("NeedUnifyLogin", 0) > 0 && key.equals("Unify")) {
            info.HttpProtocol = context.getString(R.string.login_server_http_protocol_type);
        } else {
            info.HttpProtocol = sf.getString("HttpProtocol", "http");
        }

        UserPwdBean userPwdBean = UserPwdBean.querytop();
        if (userPwdBean == null)
            return;

        info.LoginName = userPwdBean.getUsername();
        info.LoginPassword = userPwdBean.getPassword();
    }

    public String getHostPath() {

        String hostPath = getHttpProtocol() + info.IpAddress;

        if (!info.Port.equals("80")) {
            hostPath += (":" + info.Port);
        }
        return hostPath;
    }

    @NonNull
    public String getHttpProtocol() {

        String protocol = MyApplication.getInstance().getString(R.string.login_server_http_protocol_type);

        if (TextUtils.isEmpty(protocol)) {
            protocol = "http";
        } else if ("https".equals(protocol) && !"https".equals(info.HttpProtocol)) {
            protocol = "http";
        }
        return protocol + "://";
    }

    public String getBaseServerPath() {
        return getHostPath() + "/" + info.VirtualPath;
    }

    public String getMobileBusinessURL() {
        return getBaseServerPath() + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST";
    }

    public String getCityServerMobileBufFilePath() {
        String prePath = "";
        int index = info.VirtualPath.lastIndexOf("/");
        if (index > -1) {
            prePath = info.VirtualPath.substring(0, index);
        }
        return getHostPath() + (TextUtils.isEmpty(prePath) ? "" : "/" + prePath) + "/BufFile";
    }

    public String getCityServerMobileConfigPath() {
        return getBaseServerPath() + "/Services/Zondy_MapGISCitySvr_Mobile/Conf";
    }

    public String getWebWXYHProductURL() {
        return getBaseServerPath() + "/Services/MapgisCity_WXYH_Product/REST/CaseManageREST.svc/CaseManageV2";
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getRTMPLiveUrl() {
        String ip = MyApplication.getInstance().getConfigValue("RtmpIPAddress");
        if (BaseClassUtil.isNullOrEmptyString(ip)){
            ip = info.IpAddress;
        }
        return String.format("rtmp://%s:1935/%s/",ip,MyApplication.getInstance().getConfigValue("RtmpAppName"));
    }

    public String getRTMPLiveUrl(String streamName){
        return getRTMPLiveUrl() + streamName;
    }
}
