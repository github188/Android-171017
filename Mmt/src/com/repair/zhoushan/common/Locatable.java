package com.repair.zhoushan.common;

import com.zondy.mapgis.geometry.Dot;

/**
 * The abstract locatable entity.
 */
public interface Locatable {
    /**
     * Get coordinate dot
     */
    Dot getLocationDot();

    /**
     * Get map annotation title
     */
    String getAnnotationTitle();

    /**
     * Get map annotation desc
     */
    String getAnnotationDesc();
}
