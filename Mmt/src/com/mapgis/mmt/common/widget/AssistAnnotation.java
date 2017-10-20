package com.mapgis.mmt.common.widget;

import android.graphics.Bitmap;

import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by Comclay on 2017/3/3.
 * 携带设备图层和详情信息的Annotation扩展类
 */

public class AssistAnnotation extends Annotation {
    private String mInfo;
    private String mAttribute;

    private AssistAnnotation(String s, String s1, Dot dot, Bitmap bitmap) {
        super(s, s1, dot, bitmap);
    }

    private AssistAnnotation(long l) {
        super(l);
    }

    private AssistAnnotation(String s, String s1, String s2, Dot dot, Bitmap bitmap) {
        super(s, s1, s2, dot, bitmap);
    }

    public static AssistAnnotation create(Dot dot, Bitmap bitmap) {
        return new AssistAnnotation("", "", dot, bitmap);
    }

    public String getInfo() {
        return mInfo;
    }

    public AssistAnnotation setInfo(String info) {
        this.mInfo = info;
        return this;
    }

    public String getAttributes() {
        return mAttribute;
    }

    public AssistAnnotation setAttribute(String attribute) {
        this.mAttribute = attribute;
        return this;
    }

    public AssistAnnotation setTipTitle(String title){
        super.setTitle(title);
        return this;
    }

    public AssistAnnotation setTipDescription(String description){
        super.setDescription(description);
        return this;
    }

    public AssistAnnotation setTipBitmap(Bitmap bitmap){
        super.setImage(bitmap);
        return this;
    }
}
