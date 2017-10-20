package com.repair.quanzhou.entity;

import com.mapgis.mmt.common.util.BaseClassUtil;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by liuyunfan on 2016/1/21.
 */
public class GDHandReportInfo extends ReportInfo {

    public void setValue(String chosedName, String chosedValue) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (chosedName.equals(fields[i].getName())) {
                String type = fields[i].getGenericType().toString();
                try {
                    if (type.equals("class java.lang.String")) {
                        fields[i].set(this, chosedValue);
                    } else if (type.equals("int")) {
                        if (!BaseClassUtil.isNullOrEmptyString(chosedValue)) {
                            fields[i].set(this, Integer.valueOf(chosedValue));
                        }
                    } else if (type.equals("class java.io.File")) {
                        fields[i].set(this, new File(chosedValue));
                    }
                } catch (Exception ex) {

                }
                break;
            }
        }
    }

}
