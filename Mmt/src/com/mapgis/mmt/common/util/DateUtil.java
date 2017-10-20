package com.mapgis.mmt.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static Date[] getDateSpan(String span) {
        Date[] dates = null;

        switch (span) {
            case "昨天":
                dates = getLastDay();
                break;
            case "当天":
            case "今天":
                dates = getCurrentDay();
                break;
            case "上周":
                dates = getLastWeek();
                break;
            case "本周":
                dates = getCurrentWeek();
                break;
            case "上月":
                dates = getLastMonth();
                break;
            case "本月":
                dates = getCurrentMonth();
                break;
            case "本年":
                dates = getCurrentYear();
                break;
        }

        return dates;
    }

    public static String[] getDateSpanString(String span) {
        Date[] dates = getDateSpan(span);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        if (dates != null) {
            return new String[]{df.format(dates[0]), df.format(dates[1])};
        } else {
            return new String[]{"", ""};
        }
    }

    public static Date[] getLastWeek() {
        Date[] dates = new Date[2];

        long tick = getCurrentWeek()[0].getTime();

        dates[0] = new Date(tick - 7 * 24 * 3600 * 1000);
        dates[1] = new Date(tick - 1 * 1000);

        return dates;
    }

    public static Date[] getCurrentYear() {
        Date[] dates = new Date[2];

        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0);

        dates[0] = cal.getTime();

        cal.set(cal.get(Calendar.YEAR) + 1, 0, 1, 0, 0, 0);
        long tick = cal.getTimeInMillis() - 1 * 1000;

        dates[1] = new Date(tick);

        return dates;
    }

    public static Date[] getCurrentDay() {
        Date[] dates = new Date[2];

        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        dates[0] = cal.getTime();

        long tick = dates[0].getTime() + (1 * 24 * 3600 - 1) * 1000;

        dates[1] = new Date(tick);

        return dates;
    }

    public static Date[] getLastDay() {
        Date[] dates = new Date[2];

        dates[0] = new Date(getCurrentDay()[0].getTime() - 24 * 3600 * 1000);

        long tick = dates[0].getTime() + (1 * 24 * 3600 - 1) * 1000;

        dates[1] = new Date(tick);

        return dates;
    }

    public static Date[] getCurrentWeek() {
        Date[] dates = new Date[2];

        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        dates[0] = cal.getTime();

        long tick = dates[0].getTime() + (7 * 24 * 3600 - 1) * 1000;

        dates[1] = new Date(tick);

        return dates;
    }

    public static Date[] getCurrentMonth() {
        Date[] dates = new Date[2];

        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), 1, 0, 0, 0);

        dates[0] = cal.getTime();

        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        dates[1] = cal.getTime();

        return dates;
    }

    public static Date[] getLastMonth() {
        Date[] dates = new Date[2];

        long tick = getCurrentMonth()[0].getTime() - 1 * 1000;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tick);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), 1, 0, 0, 0);

        dates[0] = cal.getTime();
        dates[1] = new Date(tick);

        return dates;
    }
}
