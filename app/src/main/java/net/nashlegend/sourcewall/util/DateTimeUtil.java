package net.nashlegend.sourcewall.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by NashLegend on 16/7/1.
 */

public class DateTimeUtil {

    /**
     * 此时间是否是当天
     */
    public static boolean isTimeToday(long timeInMills) {
        GregorianCalendar past = new GregorianCalendar(Locale.CHINA);
        past.setTimeInMillis(timeInMills);
        GregorianCalendar now = new GregorianCalendar(Locale.CHINA);
        return now.get(Calendar.YEAR) == past.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == past.get(Calendar.DAY_OF_YEAR);
    }


    /**
     * 返回yyyy-MM-dd
     */
    public static String generateDayTime() {
        String timeStamp;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            GregorianCalendar calendar = new GregorianCalendar();
            timeStamp = format.format(new Date(calendar.getTimeInMillis()));
        } catch (Exception e) {
            timeStamp = String.valueOf(System.currentTimeMillis());
        }
        return timeStamp;
    }

    /**
     * 返回yyyy-MM-dd HH:mm:ss SSS
     */
    public static String generateTimeStamp() {
        String timeStamp;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA);
            GregorianCalendar calendar = new GregorianCalendar();
            timeStamp = format.format(new Date(calendar.getTimeInMillis()));
        } catch (Exception e) {
            timeStamp = String.valueOf(System.currentTimeMillis());
        }
        return timeStamp;
    }

    /**
     * @param time 秒
     */
    public static String secondsToHHMMSS(int time) {
        String hh = "00";
        String mm = "00";
        String ss = "00";
        int hhh = time / 3600;
        hh = (hhh < 10 ? "0" : "") + String.valueOf(hhh);
        int mmm = (time % 3600) / 60;
        mm = (mmm < 10 ? "0" : "") + String.valueOf(mmm);
        int sss = time % 60;
        ss = (sss < 10 ? "0" : "") + String.valueOf(sss);
        return hh + ":" + mm + ":" + ss;
    }

    public static String time2HumanReadable(String timeFormat) {
        SimpleDateFormat formatInput = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        try {
            Date date = formatInput.parse(timeFormat);
            GregorianCalendar calendar = new GregorianCalendar(Locale.CHINA);
            calendar.setTimeInMillis(date.getTime());

            GregorianCalendar today = new GregorianCalendar(Locale.CHINA);

            int diff;
            if (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                diff = calendar.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR);
            } else {
                int yearDiff = calendar.get(Calendar.YEAR) - today.get(Calendar.YEAR);
                if (yearDiff == 1) {
                    int max = today.isLeapYear(today.get(Calendar.YEAR)) ? 366 : 365;
                    diff = calendar.get(Calendar.DAY_OF_YEAR) + max - today.get(
                            Calendar.DAY_OF_YEAR);
                } else if (yearDiff == -1) {
                    int max = calendar.isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365;
                    diff = calendar.get(Calendar.DAY_OF_YEAR) - max - today.get(
                            Calendar.DAY_OF_YEAR);
                } else {
                    diff = (int) (calendar.getTimeInMillis() / 86400000
                            - new GregorianCalendar().getTimeInMillis() / 86400000);
                }
            }

            SimpleDateFormat formatOutput = new SimpleDateFormat("M月d日", Locale.CHINA);
            if (diff == -1) {
                formatOutput = new SimpleDateFormat("昨天 HH:mm", Locale.CHINA);
            } else if (diff == -2) {
                formatOutput = new SimpleDateFormat("前天 HH:mm", Locale.CHINA);
            } else if (diff == 0) {
                formatOutput = new SimpleDateFormat("今天 HH:mm", Locale.CHINA);
            } else if (new GregorianCalendar(Locale.CHINA).get(Calendar.YEAR) != calendar.get(
                    Calendar.YEAR)) {
                formatOutput = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            }
            return formatOutput.format(date);
        } catch (ParseException e) {
            ErrorUtils.onException(e);
            return timeFormat;
        }
    }
}
