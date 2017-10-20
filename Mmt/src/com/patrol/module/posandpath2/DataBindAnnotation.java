package com.patrol.module.posandpath2;

import android.graphics.Bitmap;
import android.os.Parcelable;

import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.geometry.Dot;

import java.util.LinkedHashMap;

/**
 * Created by Comclay on 2016/10/21.
 * 绑定有数据的Annotation对象
 */

public class DataBindAnnotation<T extends Parcelable> extends Annotation {
    private LinkedHashMap<String,LinkedHashMap<String,String>> mAttrValues;
    private T t;
    public DataBindAnnotation(T t
            , String s, String s1, Dot dot, Bitmap bitmap) {
        super(s, s1, dot, bitmap);
        this.t = t;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public DataBindAnnotation(LinkedHashMap<String,LinkedHashMap<String,String>> mAttrValues
            , String s, String s1, Dot dot, Bitmap bitmap) {
        super(s, s1, dot, bitmap);
        this.mAttrValues = mAttrValues;
    }

    public LinkedHashMap<String,LinkedHashMap<String,String>> getmAttrValues() {
        return mAttrValues;
    }

    public void setmAttrValues(LinkedHashMap<String,LinkedHashMap<String,String>> mAttrValues) {
        this.mAttrValues = mAttrValues;
    }
}
