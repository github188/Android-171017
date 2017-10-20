package com.patrol.module;

import android.text.TextUtils;

import com.mapgis.mmt.R;
import com.patrol.entity.KeyPoint;

/**
 * Created by lyunfan on 17/3/24.
 */

public class PatrolUtils {
    public static int getIco(KeyPoint kp) {
        if (kp == null) {
            return R.drawable.map_patrol_unarrived;
        }
        if (kp.IsFeedback == 1) {
            return R.drawable.map_patrol_feedbacked;
        }
        if (kp.IsArrive == 1) {
            if (kp.IsFeedback != 1 && !TextUtils.isEmpty(kp.KClass) && kp.KClass.equals("0")) {
                return R.drawable.map_patrol_arrived_noneet_fb;
            }
            return R.drawable.map_patrol_arrived;
        }
        return R.drawable.map_patrol_unarrived;
    }
}
