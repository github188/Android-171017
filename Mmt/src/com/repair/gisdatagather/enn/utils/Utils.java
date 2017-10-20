package com.repair.gisdatagather.enn.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapgis.mmt.module.gis.onliemap.OnlineLayerInfo;
import com.zondy.mapgis.android.graphic.GraphicPoint;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by liuyunfan on 2015/12/21.
 */
public class Utils {
    public static  OnlineLayerInfo getLayerByName(OnlineLayerInfo[] layers,String name) {
        if (layers == null) {
            return null;
        }

        for (OnlineLayerInfo layer : layers) {
            if (name.equals(layer.name)) {
                return layer;
            }
        }

        return null;
    }

    public static TextView createTextView(Context context, String text, View.OnClickListener listener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        TextView textView = new TextView(context);
        textView.setLayoutParams(layoutParams);
        textView.setTextAppearance(context, com.mapgis.mmt.R.style.default_text_small_purity);
        textView.setGravity(Gravity.CENTER);
        textView.setOnClickListener(listener);
        textView.setBackgroundResource(com.mapgis.mmt.R.drawable.layout_focus_bg);
        textView.setText(text);
        return textView;
    }


    public static ImageView createDivider(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setImageResource(com.mapgis.mmt.R.color.default_line_bg);
        return imageView;
    }

    public static GraphicPoint createGraphicPoint(Dot dot) {
        GraphicPoint graphicPoint = new GraphicPoint(dot, 15);
        graphicPoint.setAttributeValue("SN", "SN_POINT");
        graphicPoint.setColor(Color.BLUE);
        return graphicPoint;
    }
}
