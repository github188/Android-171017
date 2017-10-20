package com.patrol.module.patrolanalyze;

import java.util.ArrayList;

public class AnalyzeResultBean {
//    public int currentPageIndex;
    public ArrayList<ResultBody> getMe;
//    public int totalRcdNum;

    class ResultBody {
        public String arriveNum;
        public String coverLineLength;
        public String feedbackNum;
        public String onLineRoadDaySum;
        public String onLineRoadSum;
        public String onLineTimeDaySum;
        public String onLineTimeSum;
        public int reportIncidentNum;
        public String stationName;
        public String userName;

        // 兼容新旧服务。。。。。。。。。。。
        public ArrayList<ResultBody> list;
        /*public String arriveNumSum;
        public String coverLineLengthSum;
        public String feedbackNumSum;
        public String onLineRoadDaySumTotal;
        public String onLineRoadSumTotal;
        public String onLineTimeDaySumTotal;
        public String onLineTimeSumTotal;
        public String reportIncidentNumSum;*/
    }
}