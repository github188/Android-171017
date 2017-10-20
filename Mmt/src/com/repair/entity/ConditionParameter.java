package com.repair.entity;


public class ConditionParameter {
    public int pageIndex = 0;
    public int pageSize = 10;
    public String sortFields = "分派时间";
    public String direction = "desc";
    public String dateFrom = "";
    public String dateTo = "";
    public String eventType = "";
    public String state = "";
    public String eventClass = "";
    public String reportInfo = "";
    public String dispatchDateFrom = "";
    public String dispatchDateTo = "";
    public String dispatchMan = "";

    public String[] generateRequestArgs() {
        return new String[]{
                "pageIndex", String.valueOf(pageIndex), "pageSize", String.valueOf(pageSize), "sortFields", sortFields,
                "direction", direction, "eventClass", eventClass, "eventType", eventType, "reportInfo", reportInfo,
                "dateFrom", dateFrom, "dateTo", dateTo, "dispatchDateFrom", dispatchDateFrom, "dispatchDateTo",
                dispatchDateTo, "dispatchMan", dispatchMan, "state", state
        };
    }
}
