package com.repair.shaoxin.water.highrisesearch;

import com.zondy.mapgis.android.graphic.Graphic;
import com.zondy.mapgis.geometry.Dot;

import java.util.List;

public class HRCVUser {
    public List<HRCVUserAttr> FildInfoList;

    public double x;
    public double y;

    private Graphic graphic;

    private String pipeNo;

    @Override
    public String toString() {
        return showInfo();
    }

    /** 获取坐标信息 */
    public Dot getPoint() {

        if (x != 0 && y != 0) {
            return new Dot(x, y);
        }

        for (HRCVUserAttr attr : FildInfoList) {

            if ((attr.FiledName.equals("横座标") || attr.FiledName.equals("X坐标")) && attr.FiledVal.trim().length() > 0) {
                x = Double.valueOf(attr.FiledVal);
            }

            if ((attr.FiledName.equals("纵座标") || attr.FiledName.equals("Y坐标")) && attr.FiledVal.trim().length() > 0) {
                y = Double.valueOf(attr.FiledVal);
            }

            if (x != 0 && y != 0) {
                return new Dot(x, y);
            }
        }

        return null;
    }

    public String getPipeNo() {
        if (pipeNo == null) {
            for (HRCVUserAttr attr : FildInfoList) {
                if (attr.FiledName.equals("编号")) {
                    pipeNo = attr.FiledVal;
                    break;
                }
            }
        }
        return pipeNo;
    }

    public Graphic getGraphic() {
        graphic = new Graphic();

        if (FildInfoList != null) {
            for (HRCVUserAttr attr : FildInfoList) {
                graphic.setAttributeValue(attr.FiledName, attr.FiledVal);
//                graphic.setGeometry(getPoint());
            }
        }

        return graphic;
    }

    private String showInfo() {
        if (FildInfoList == null || FildInfoList.size() == 0) {
            return "";
        }

        for (HRCVUserAttr attr : FildInfoList) {
            if (attr.FiledName.equals("用户号")) {
                return "用户号: " + attr.FiledVal;
            }
        }

        return "";
    }

}