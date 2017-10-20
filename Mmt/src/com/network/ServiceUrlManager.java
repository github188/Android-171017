package com.network;

import com.mapgis.mmt.config.ServerConnectConfig;
import com.mapgis.mmt.module.login.ServerConfigInfo;

/***
 * Created by maoshoubei on 2017/10/19.
 */

public class ServiceUrlManager {

    private static ServiceUrlManager instance;

    private ServiceUrlManager() {
    }

    public static ServiceUrlManager getInstance() {
        if (null == instance) {
            synchronized (ServiceUrlManager.class) {
                if (null == instance) {
                    instance = new ServiceUrlManager();
                }
            }
        }

        return instance;
    }

    //获取基础服务
    public static String getBaseServerUrl() {
        return getServiceRootUrl() + "/Services/Zondy_MapGISCitySvr_MobileBusiness/REST/BaseREST.svc";
    }

    private static String getServiceRootUrl() {
        ServerConfigInfo cfg = ServerConnectConfig.getInstance().getServerConfigInfo();

        String protocol = cfg.HttpProtocol;
        String ip = cfg.IpAddress;
        String port = cfg.Port;
        String virtualPath = cfg.VirtualPath;

        return protocol + "://" + ip + ":" + port + "/" + virtualPath;
    }

    //获取测试连接接口
    public String getTestConnectionUrl() {
        return getBaseServerUrl() + "/TestDB";
    }

    //获取登录接口
    public String getLoginUrl() {
        return getBaseServerUrl() + "/Login";
    }



}
