package com.mapgis.mmt.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;

import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.featureservice.Feature;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Convert {
    public static List<Graphic> fromFeaturesToGraphics(List<Feature> features, String layerName) {
        List<Graphic> graphics = new ArrayList<Graphic>();

        for (int i = 0; i < features.size(); i++) {
            graphics.add(fromFeatureToGraphic(features.get(i), layerName));
        }

        return graphics;
    }

    public static Graphic fromFeatureToGraphic(Feature feature) {
        return feature.toGraphics(true).get(0);
    }

    public static Graphic fromFeatureToGraphic(Feature feature, String layerName) {
        Graphic graphic = feature.toGraphics(true).get(0);

        graphic.setAttributeValue("$图层名称$", layerName);
        if (graphic.getAttributeValue("编号") == null) {
            graphic.setAttributeValue("编号", "");
        }
        return graphic;
    }

    public static double FormatDoubleSmart(double value) {
        boolean isNegative = false;

        if (value < 0) {
            isNegative = true;

            value = Math.abs(value);
        }

        if (value > 100)
            value = (int) value;
        else if (value > 10)
            value = Convert.FormatDouble(value, ".0");
        else if (value > 1)
            value = Convert.FormatDouble(value, ".00");
        else
            value = Convert.FormatDouble(value, ".000");

        return isNegative ? (-1 * value) : value;
    }

    public static double FormatDouble(double value) {
        return FormatDouble(value, ".000");
    }

    public static String FormatDouble(String value) {
        try {
            if (TextUtils.isEmpty(value)) {
                return "0.000";
            }

            DecimalFormat decimalFormat = new DecimalFormat(".000");

            return decimalFormat.format(value);
        } catch (Exception ex) {
            ex.printStackTrace();

            return "0.000";
        }
    }

    public static double FormatDouble(double value, String pattern) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);

        return Double.valueOf(decimalFormat.format(value));
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static String FormatLength(double len) {
        if (len < 1000)
            return (int) len + "米";
        else {
            len = len / 1000;

            len = FormatDouble(len, ".0");

            return len + "公里";
        }
    }

    public static int FormatInt(String val, int... defaultVal) {
        try {
            if (!TextUtils.isEmpty(val)) {
                return Integer.parseInt(val);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (defaultVal != null && defaultVal.length > 0)
            return defaultVal[0];
        else
            return 0;
    }

    public static String FormatString(String val, String... defaultVal) {
        try {
            if (!TextUtils.isEmpty(val)) {
                return val;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (defaultVal != null && defaultVal.length > 0)
            return defaultVal[0];
        else
            return "";
    }
}
