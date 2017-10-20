package com.maintainproduct.entity;

import java.util.List;

/**
 * Created by liuyunfan on 2016/6/1.
 */
public class UpdateCurrentAddressEvent {
    public String x;
    public String y;
    public String longitude;
    public String latitude;
    public List<String> adds;

    public UpdateCurrentAddressEvent() {
    }

    public UpdateCurrentAddressEvent(List<String> adds, String x, String y, String longitude, String latitude) {
        this.adds = adds;
        this.x = x;
        this.y = y;
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
