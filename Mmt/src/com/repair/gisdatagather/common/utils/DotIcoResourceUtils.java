package com.repair.gisdatagather.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Base64;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.module.gis.onliemap.MapServiceInfo;
import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;

import java.util.HashMap;
import java.util.Map;

import static android.graphics.BitmapFactory.decodeResource;

/**
 * Created by lyunfan on 16/11/24.
 */

public class DotIcoResourceUtils {
    private static Bitmap wzBitmap;
//  ／
    private static final Map<String, Bitmap> icoBitmaps = new HashMap<>();

//    private static final int FMChanged = R.drawable.gis_fm0;
//    private static final int FMUnChange = R.drawable.gis_fm1;
//
//    private static final int QTGDChanged = R.drawable.gis_qtgd0;
//    private static final int QTGDUnChange = R.drawable.gis_qtgd1;
//
//    private static final int SBChanged = R.drawable.gis_sb0;
//    private static final int SBUnChange = R.drawable.gis_sb1;
//
//    private static final int SBXChanged = R.drawable.gis_sbx0;
//    private static final int SBXUnChange = R.drawable.gis_sbx1;
//
//    private static final int XQJLBChanged = R.drawable.gis_xqjlb0;
//    private static final int XQJLBUnChange = R.drawable.gis_xqjlb1;
//
//    private static final int XFSChanged = R.drawable.gis_xfs0;
//    private static final int XFSUnChange = R.drawable.gis_xfs1;
//
//    private static final int BJChanged = R.drawable.gis_bj0;
//    private static final int BJUnChange = R.drawable.gis_bj1;
//
//    private static final int CLDChanged = R.drawable.gis_cld0;
//    private static final int CLDUnChange = R.drawable.gis_cld1;
//
//    private static final int CYDChanged = R.drawable.gis_cyd0;
//    private static final int CYDUnChange = R.drawable.gis_cyd1;
//
//    private static final int PQFChanged = R.drawable.gis_pqf0;
//    private static final int PQFUnChange = R.drawable.gis_pqf1;
//
//    private static final int PWFChanged = R.drawable.gis_pwf0;
//    private static final int PWFUnChange = R.drawable.gis_pwf1;
//
//    private static final int GQChanged = R.drawable.gis_gd0;
//    private static final int GQUnChange = R.drawable.gis_gq1;

    static String scaleStr = MyApplication.getInstance().getConfigValue("IcoScale");


    private static Bitmap scaleImg(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    public static Bitmap getDotIcoResource(Context context, String layerName, int state) {

        if (context == null) {
            if (wzBitmap == null) {
                wzBitmap = decodeResource(context.getResources(), R.drawable.map_patrol_unarrived);
            }
            return wzBitmap;
        }

        if (TextUtils.isEmpty(layerName)) {
            if (wzBitmap == null) {
                wzBitmap = decodeResource(context.getResources(), R.drawable.map_patrol_unarrived);
            }
            return wzBitmap;
        }

        if (icoBitmaps.containsKey(layerName)) {
            return icoBitmaps.get(layerName);
        }

        float scale = 2.0f;
        if (!TextUtils.isEmpty(scaleStr)) {
            scale = Float.parseFloat(scaleStr);
        }

        Bitmap bitmap = null;
        OnlineLayerInfo layerInfo = MapServiceInfo.getInstance().getLayerByName(layerName);
        if (layerInfo != null && !TextUtils.isEmpty(layerInfo.Legend)) {
            byte[] bytes = Base64.decode(layerInfo.Legend, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            if (scale != 1.5) {
                bitmap = scaleImg(bitmap, scale);
            }

        }
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.map_patrol_unarrived);
        }
        icoBitmaps.put(layerName, bitmap);

        return bitmap;

//        int resourceid = 0;
//
//
//        switch (layerName) {
//
//            case "阀门": {
//                if (state == 1) {
//                    resourceid = FMChanged;
//                } else {
//                    resourceid = FMUnChange;
//                }
//            }
//            break;
//            case "弯头":
//            case "三通":
//            case "其它管点": {
//                if (state == 1) {
//                    resourceid = QTGDChanged;
//                } else {
//                    resourceid = QTGDUnChange;
//                }
//            }
//            break;
//            case "水表": {
//                if (state == 1) {
//                    resourceid = SBChanged;
//                } else {
//                    resourceid = SBUnChange;
//                }
//            }
//            break;
//            case "水表箱": {
//                if (state == 1) {
//                    resourceid = SBXChanged;
//                } else {
//                    resourceid = SBXUnChange;
//                }
//            }
//            break;
//            case "小区计量表": {
//                if (state == 1) {
//                    resourceid = XQJLBChanged;
//                } else {
//                    resourceid = XQJLBUnChange;
//                }
//            }
//            break;
//            case "消火栓":
//            case "消防栓": {
//                if (state == 1) {
//                    resourceid = XFSChanged;
//                } else {
//                    resourceid = XFSUnChange;
//                }
//            }
//            break;
//            case "变径点":
//            case "变径": {
//                if (state == 1) {
//                    resourceid = BJChanged;
//                } else {
//                    resourceid = BJUnChange;
//                }
//            }
//            break;
//
//            case "测流点": {
//                if (state == 1) {
//                    resourceid = CLDChanged;
//                } else {
//                    resourceid = CLDUnChange;
//                }
//            }
//            break;
//            case "测压点": {
//                if (state == 1) {
//                    resourceid = CYDChanged;
//                } else {
//                    resourceid = CYDUnChange;
//                }
//            }
//            break;
//            case "管桥": {
//                if (state == 1) {
//                    resourceid = GQChanged;
//                } else {
//                    resourceid = GQUnChange;
//                }
//            }
//            break;
//            case "排气阀": {
//                if (state == 1) {
//                    resourceid = PQFChanged;
//                } else {
//                    resourceid = PQFUnChange;
//                }
//            }
//            break;
//            case "排污阀": {
//                if (state == 1) {
//                    resourceid = PWFChanged;
//                } else {
//                    resourceid = PWFUnChange;
//                }
//            }
//            break;
//        }
//
//        if (resourceid > 0) {
//            if (state == 1) {
//                if (!icoBitmapsChanged.containsKey(layerName)) {
//                    icoBitmapsChanged.put(layerName, decodeResource(context.getResources(), resourceid));
//                }
//                return icoBitmapsChanged.get(layerName);
//            } else {
//                if (!icoBitmapsUnChange.containsKey(layerName)) {
//                    icoBitmapsUnChange.put(layerName, decodeResource(context.getResources(), resourceid));
//                }
//                return icoBitmapsUnChange.get(layerName);
//            }
//        }
//
//        if (wzBitmap == null) {
//            wzBitmap = decodeResource(context.getResources(), R.drawable.map_patrol_unarrived);
//        }
//        return wzBitmap;
    }
}
