package com.patrol.common;

import com.mapgis.mmt.config.ServerConnectConfig;

public class MyPlanUtil {
    public static String getStandardURL() {
        return ServerConnectConfig.getInstance().getBaseServerPath()
                + "/Services/MapGISCitySvr_Patrol_Standard/REST/PatrolStandardRest.svc";
    }
}
