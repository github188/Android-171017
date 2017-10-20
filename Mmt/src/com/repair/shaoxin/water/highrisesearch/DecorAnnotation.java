package com.repair.shaoxin.water.highrisesearch;

import android.graphics.Bitmap;

import com.zondy.mapgis.android.annotation.Annotation;
import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.geometry.Dot;

/**
 * Created by Comclay on 2016/9/28.
 */

public class DecorAnnotation extends Annotation {
    private Graphic graphic;
    public DecorAnnotation(Graphic graphic, String s, String s1, Dot dot, Bitmap bitmap){
        super(s, s1, dot, bitmap);
        this.graphic = graphic;
    }

    public DecorAnnotation(String s, String s1, Dot dot, Bitmap bitmap) {
        super(s, s1, dot, bitmap);
    }

    public Graphic getGraphic() {
        return graphic;
    }
}
