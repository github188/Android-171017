package com.mapgis.mmt.module.gatherdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.MyApplication;
import com.mapgis.mmt.R;
import com.mapgis.mmt.common.util.BaseClassUtil;
import com.mapgis.mmt.common.util.Convert;
import com.mapgis.mmt.config.CitySystemConfig;
import com.mapgis.mmt.config.MapConfig;
import com.mapgis.mmt.config.MobileConfig;
import com.mapgis.mmt.db.DatabaseHelper;
import com.mapgis.mmt.common.util.GisQueryUtil;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.android.mapview.MapView;
import com.zondy.mapgis.featureservice.FeaturePagedResult;
import com.zondy.mapgis.featureservice.FeatureQuery;
import com.zondy.mapgis.geometry.Dot;
import com.zondy.mapgis.geometry.GeomType;
import com.zondy.mapgis.geometry.Rect;
import com.zondy.mapgis.map.LayerEnum;
import com.zondy.mapgis.map.MapLayer;
import com.zondy.mapgis.map.VectorLayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GatherDataUtils {

    /**
     * 创建功能菜单的子视图
     */
    public static TextView createTextView(Context context, String text, OnClickListener listener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;

        TextView textView = new TextView(context);
        textView.setLayoutParams(layoutParams);
        textView.setTextAppearance(context, R.style.default_text_small_purity);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(listener);
        textView.setBackgroundResource(R.drawable.layout_focus_bg);
        textView.setText(text);
        return textView;
    }

    /**
     * 创建分割线
     */
    public static ImageView createDivider(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LayoutParams(1, LayoutParams.MATCH_PARENT));
        imageView.setImageResource(R.color.default_line_bg);
        return imageView;
    }

    /**
     * 插入数据字典表的信息，用来保存属性编辑过的信息，方便下次直接读取
     *
     * @param key
     * @param value
     */
    public static void insertAttrInfo(String key, String value) {
        List<String> values = queryAttrInfo(key);

        // 如果键值存在，则不插入到数据库中
        for (String v : values) {
            if (v.equals(value))
                return;
        }

        CitySystemConfig config = new CitySystemConfig(key, value, MyApplication.getInstance().getUserId());
        DatabaseHelper.getInstance().insert(config);
    }

    /**
     * 查询该键下，曾填写上报过的信息
     *
     * @param key
     * @return 保存在本地的填写过的信息
     */
    public static List<String> queryAttrInfo(String key) {
        List<String> values = new ArrayList<String>();

        List<CitySystemConfig> configs = DatabaseHelper.getInstance().query(CitySystemConfig.class,
                "UserID = " + MyApplication.getInstance().getUserId() + " AND ConfigKey = '" + key + "'");

        if (configs == null || configs.size() == 0) {
            return values;
        }

        for (CitySystemConfig config : configs) {
            values.add(config.ConfigValue);
        }

        return values;
    }

    /**
     * 查询指定点的点设备信息
     *
     * @param mapView
     * @param mapDot
     * @return 查询到的第一个结果，否则返回null
     */
    public static Graphic searchTargetGeomLayer(MapView mapView, Dot mapDot) {

        try {

            MapLayer layer = null;

            // 获取 经过 mobileconfig.json 的 PointQueryLayerFilter 值 过滤后的 可查 图层列表
            List<String> visibleVectorLayerNames = GisQueryUtil.getPointQueryVectorLayerNames(mapView);

            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();

            while ((layer = layerEnum.next()) != null) {
                if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(GeomType.GeomPnt)) {
                    continue;
                }

                Rect rect = new Rect();

                rect.setXMin(mapDot.x - mapView.getResolution(mapView.getZoom()) * 20);
                rect.setYMin(mapDot.y - mapView.getResolution(mapView.getZoom()) * 20);
                rect.setXMax(mapDot.x + mapView.getResolution(mapView.getZoom()) * 20);
                rect.setYMax(mapDot.y + mapView.getResolution(mapView.getZoom()) * 20);

                // 存储要素查询结果
                FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "", new FeatureQuery.QueryBound(
                        rect), FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", 20);

                if (featurePagedResult.getTotalFeatureCount() > 0) {
                    return Convert.fromFeaturesToGraphics(featurePagedResult.getPage(1), layer.getName()).get(0);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * 查询指定类型设备信息，50cm范围
     *
     * @param mapView
     * @param mapDot
     * @return 查询到的第一个结果，否则返回null
     */
    public static Graphic searchTargetGeomLayer(MapView mapView, Dot mapDot, GeomType geomType) {

        try {

            MapLayer layer = null;

            // 获取 经过 mobileconfig.json 的 PointQueryLayerFilter 值 过滤后的 可查 图层列表
            List<String> visibleVectorLayerNames = GisQueryUtil.getPointQueryVectorLayerNames(mapView);

            LayerEnum layerEnum = mapView.getMap().getLayerEnum();
            layerEnum.moveToFirst();

            while ((layer = layerEnum.next()) != null) {
                if (!visibleVectorLayerNames.contains(layer.getName()) || !layer.GetGeometryType().equals(geomType)) {
                    continue;
                }

                Rect rect = new Rect();

                rect.setXMin(mapDot.x - 0.5);
                rect.setYMin(mapDot.y - 0.5);
                rect.setXMax(mapDot.x + 0.5);
                rect.setYMax(mapDot.y + 0.5);

                // 存储要素查询结果
                FeaturePagedResult featurePagedResult = FeatureQuery.query((VectorLayer) layer, "", new FeatureQuery.QueryBound(
                        rect), FeatureQuery.SPATIAL_REL_MBROVERLAP, true, true, "", 20);

                if (featurePagedResult.getTotalFeatureCount() > 0) {
                    return Convert.fromFeaturesToGraphics(featurePagedResult.getPage(1), layer.getName()).get(0);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * 对传进来的属性信息按DB数据库的字段顺序排序
     *
     * @param attrMap 属性信息
     * @return
     */
    public static LinkedHashMap<String, String> sortByDbColumn(LinkedHashMap<String, String> attrMap) {
        LinkedHashMap<String, String> sortedAttrMap = new LinkedHashMap<String, String>();

        Object[] columnNames = getGISFields(attrMap.get("$图层名称$"));

        if (columnNames == null) {
            columnNames = attrMap.keySet().toArray();
        }

        for (Object str : columnNames) {
            if (!attrMap.containsKey(str)) {
                continue;
            }

            String key = (String) str;

            if ("emapgisid".equalsIgnoreCase(key)) {
                continue;
            }

            // 判断key是否包含中文，如果没有中文不做显示
            boolean isExistChinese = false;

            for (char k : key.toCharArray()) {
                isExistChinese = String.valueOf(k).matches("[\\u4e00-\\u9fa5]+");

                if (isExistChinese) {
                    break;
                }
            }

            if (!isExistChinese) {
                continue;
            }

            sortedAttrMap.put(key, attrMap.get(key));
        }

        return sortedAttrMap;
    }

    /**
     * 获取DB数据该图层的属性字段，目的是要获取顺序，用来对查询到的数据进行排序
     *
     * @param layerName 图层名称
     * @return
     */
    public static String[] getGISFields(String layerName) {

        if (BaseClassUtil.isNullOrEmptyString(layerName)) {
            return null;
        }

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {

            if (MobileConfig.MapConfigInstance == null) {
                return null;
            }

            String name = MobileConfig.MapConfigInstance.getMapType(MapConfig.MOBILE_EMS).Name;
            String path = MyApplication.getInstance().getMapFilePath() + name + "/" + name + ".db";

            database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

            cursor = database.rawQuery("SELECT * FROM " + layerName + " WHERE 1=-1", null);

            String[] columnNames = cursor.getColumnNames();

            return columnNames;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (database != null) {
                database.close();
            }
        }
    }
}
