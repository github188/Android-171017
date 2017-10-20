// ICityMobile.aidl
package com.aidl;

import com.mapgis.mmt.module.login.ServerConfigInfo;
import com.patrol.entity.TaskInfo;
import com.patrol.entity.KeyPoint;

interface ICityMobile {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    ServerConfigInfo getConfigInfo();

    List<TaskInfo> getAllTaskBase();

    TaskInfo getTaskInfo(int id);

    List<TaskInfo> refreshMyPlan();

    void onKeyPointFeedback(int taskID,int keyPointID);

    void setTaskState(int id,String taskState);

    List<KeyPoint> fetchPipeLines(int id, double xmin, double ymin, double xmax, double ymax);
}
