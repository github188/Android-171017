package com.mapgis.mmt.common.util;

import com.zondy.mapgis.geometry.Dot;

/**
 * Created by Comclay on 2017/2/6.
 */

public interface Dotor<T> {
    Dot getDot(T o);
}
