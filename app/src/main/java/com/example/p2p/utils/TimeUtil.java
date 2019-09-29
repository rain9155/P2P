package com.example.p2p.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 * Created by 陈健宇 at 2019/9/29
 */
public class TimeUtil {

    public static String getTime(long time) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(new Date());
        Calendar unKnownCalender = Calendar.getInstance();
        unKnownCalender.setTimeInMillis(time);
        if (sameDay(todayCalendar, unKnownCalender)) {
            return "今天";
        } else if (sameWeek(todayCalendar, unKnownCalender)) {
            return "本周";
        } else if (sameMonth(todayCalendar, unKnownCalender)) {
            return "这个月";
        } else {
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.CANADA);
            return sdf.format(date);
        }
    }

    public static boolean sameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean sameWeek(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR);
    }

    public static boolean sameMonth(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }


}
