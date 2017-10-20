package com.mapgis.mmt.module.gis.toolbar.accident2.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mapgis.mmt.R;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Comclay on 2017/3/3.
 * 爆管分析图标工厂
 */

public class IconFactory {
    private static final String TAG = "AnnotationFactory";

    public final static String PIPE_ANNOTATION = "pipe_annotation";
    /*  private final static int[] icons = {
              R.drawable.broken_point
              , R.drawable.broken_need_close_valve
              , R.drawable.broken_stop_water_user
              , R.drawable.broken_stop_water_pipe
              , R.drawable.broken_res_center
              , R.drawable.broken_uneffect_close_eqpt
              , R.drawable.broken_need_open_valve
              , R.drawable.broken_assist_close_valve
              , R.drawable.broken_need_closed_valve
      };*/
    private Map<String, Integer> iconIdMap;
    private Map<String, WeakReference<Bitmap>> mBmpMap;
    private Resources mRes;

    public static IconFactory create(Resources res) {
        return new IconFactory(res);
    }

    /**
     * 获取Bitmap
     *
     * @param type 类型
     * @return bitmap对象
     */
    public Bitmap getBitmap(String type) {
        WeakReference<Bitmap> bitmapWeakReference = mBmpMap.get(type);
        if (bitmapWeakReference == null || bitmapWeakReference.get() == null) {
            int resId = iconIdMap.get(type);
            Bitmap bitmap = BitmapFactory.decodeResource(this.mRes, resId);
            bitmapWeakReference = new WeakReference<>(bitmap);
            mBmpMap.put(type, bitmapWeakReference);
            Log.e(TAG, type + "====图标加载完成");
            return bitmap;
        } else {
            return bitmapWeakReference.get();
        }
    }

    private IconFactory(Resources res) {
        this.mRes = res;
        initBitmap();
    }

    /**
     * 初始化图标集合
     */
    private void initBitmap() {
        initIconIdMap();
        mBmpMap = new HashMap<>();
        for (String type : iconIdMap.keySet()) {
            mBmpMap.put(type, null);
        }
    }

    private void initIconIdMap() {
        if (iconIdMap != null && iconIdMap.size() > 0) {
            return;
        }

        if (iconIdMap == null) {
            iconIdMap = new HashMap<>();
        }
        iconIdMap.put(MetaType.TYPE_INCIDENT_POINT, R.drawable.broken_point);
        iconIdMap.put(MetaType.TYPE_SWITCH, R.drawable.broken_need_close_valve);
        iconIdMap.put(MetaType.TYPE_SWI_EFFECT, R.drawable.broken_stop_water_user);
        iconIdMap.put(MetaType.TYPE_PIPE_LINE, R.drawable.broken_stop_water_pipe);
        iconIdMap.put(MetaType.TYPE_RES_CENTER, R.drawable.broken_res_center);
        iconIdMap.put(MetaType.TYPE_INVALIDATE_SWITCH, R.drawable.broken_uneffect_close_eqpt);
        iconIdMap.put(MetaType.TYPE_SHOULD_OPEN_SWITCH, R.drawable.broken_need_open_valve);
        iconIdMap.put(MetaType.TYPE_ASSIST_SWITCH, R.drawable.broken_assist_close_valve);
        iconIdMap.put(MetaType.TYPE_CLOSED_SWITCH, R.drawable.broken_need_closed_valve);
        iconIdMap.put(PIPE_ANNOTATION, R.drawable.icon_mark_normal);
    }
}
