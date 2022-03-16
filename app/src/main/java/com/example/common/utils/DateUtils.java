package com.example.common.utils;

import com.example.common.model.main.DateOfMonth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 时间格式工具类
 */
public final class DateUtils {

    public static final long ONE_MINUTE_MILLIONS = 60 * 1000;
    public static final long ONE_HOUR_MILLIONS = 60 * ONE_MINUTE_MILLIONS;
    public static final long ONE_DAY_MILLIONS = 24 * ONE_HOUR_MILLIONS;

    public static final String[] WEEK_DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    public static final String[] MONTHS = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private DateUtils() {
    }

    /**
     * 判断俩日期是否为同一年
     */
    public static boolean isSameYear(Long compareTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);

        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);
        int comYear = compareCalendar.get(Calendar.YEAR);

        return year == comYear;
    }

    /**
     * 判断俩日期是否为同一月
     */
    public static boolean isSameMonth(Long compareTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int month = calendar.get(Calendar.MONTH);

        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);
        int comMonth = compareCalendar.get(Calendar.MONTH);

        return month == comMonth;
    }

    /**
     * 获取月日期
     */
    public static List<DateOfMonth> getDatesOfMonth(Long compareTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);

        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);
        int dates = compareCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int comYear = compareCalendar.get(Calendar.YEAR);
        int comMonth = compareCalendar.get(Calendar.MONTH);
        int comDate = compareCalendar.get(Calendar.DATE);

        List<DateOfMonth> datesOfMonth = new ArrayList<>();
        for (int i = 0; i < dates; i++) {
            compareCalendar.set(Calendar.DAY_OF_MONTH, i + 1);
            DateOfMonth dateOfMonth = new DateOfMonth();
            dateOfMonth.setDate(i + 1 + "");
            dateOfMonth.setTime(compareCalendar.getTimeInMillis());
            dateOfMonth.setWeekDate(getWeekOfDate(compareCalendar.getTimeInMillis()));
            if (year == comYear) {
                if (month == comMonth) {
                    dateOfMonth.setType(i < date - 1 ? 1 : 0);
                } else if (month > comMonth) {
                    dateOfMonth.setType(1);
                } else {
                    dateOfMonth.setType(0);
                }
            } else if (year > comYear) {
                dateOfMonth.setType(1);
            } else {
                dateOfMonth.setType(0);
            }
            datesOfMonth.add(dateOfMonth);
        }
        return datesOfMonth;
    }

    /**
     * 获取当前日期是星期几<br>
     */
    public static String getWeekOfDate(Long compareTime) {
        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);

        int w = compareCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return WEEK_DAYS[w];
    }

    public static String getMonth(Long compareTime) {
        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);
        return MONTHS[compareCalendar.get(Calendar.MONTH)];
    }

    public static int getYear(Long compareTime) {
        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);
        return compareCalendar.get(Calendar.YEAR);
    }

    /**
     * 获取上个月
     */
    public static long lastMonth(Long compareTime) {
        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);
        int month = compareCalendar.get(Calendar.MONTH);
        compareCalendar.set(Calendar.MONTH, month - 1);
        return compareCalendar.getTimeInMillis();
    }

    /**
     * 获取下个月
     */
    public static long nextMonth(Long compareTime) {
        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.clear();
        compareCalendar.setTimeInMillis(compareTime);
        int month = compareCalendar.get(Calendar.MONTH);
        compareCalendar.set(Calendar.MONTH, month + 1);
        return compareCalendar.getTimeInMillis();
    }

    public static String formatDate(int date) {
        String result = "";
        if (date > 0 && date <= 9) {
            result = "0" + date;
        }
        if (date == 1 || date == 11 || date == 21 || date == 31) {
            result = result + "st";
        } else if (date == 2 || date == 12 || date == 22) {
            result = result + "nd";
        } else if (date == 3 || date == 13 || date == 23) {
            result = result + "rd";
        } else {
            result = result + "th";
        }
        return result;
    }
}
