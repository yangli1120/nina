package crazysheep.io.nina.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import crazysheep.io.nina.R;

/**
 * format time
 *
 * Created by crazysheep on 16/1/23.
 */
public class TimeUtils {

    /**
     * parse timestamp from date
     *
     * @param dateStr Twitter's create_at, such like:"Sat Nov 21 15:06:23 +0000 2015"
     * */
    public static long getTimeFromDate(@NonNull String dateStr) {
        DateFormat dateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy",
                Locale.ENGLISH);
        try {
            Date date = dateFormat.parse(dateStr);

            return date.getTime();
        } catch (ParseException pe) {
            pe.printStackTrace();

            L.d("time from date error: " + Log.getStackTraceString(pe));
        }

        return System.currentTimeMillis();
    }

    /**
     * comparse timestamp to current time, format time such like "10mins", "10days", "2weeks",
     * "2months"
     *
     * @param timestamp The timestamp to format
     * */
    public static String formatTimestamp(@NonNull Context context, long timestamp) {
        int result = (int)((System.currentTimeMillis() - timestamp) / 1000); // seconds
        if(result > 0) {
            int min = Math.round(result * 1f / 60); // minutes
            int hour = Math.round(min * 1f/ 60);
            int day = Math.round(hour * 1f / 24);
            int month = Math.round(day * 1f / 30);

            if(month == 1)
                return context.getString(R.string.time_month, month);
            if(month > 1)
                return context.getString(R.string.time_month_s, month);

            if(day == 1)
                return context.getString(R.string.time_day, day);
            if(day > 1)
                return context.getString(R.string.time_day_s, day);

            if(hour == 1)
                return context.getString(R.string.time_hour, hour);
            if(hour > 1)
                return context.getString(R.string.time_hour_s, hour);

            if(min == 1)
                return context.getString(R.string.time_min, min);
            if(min > 1)
                return context.getString(R.string.time_min_s, min);
        }

        return context.getString(R.string.time_just_now);
    }

    /**
     * format timestamp as human readable date pattern, such like "yyyy-MM-dd"
     * */
    public static String formatDate(long timestamp, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault())
                .format(new Date(timestamp));
    }

    public static String formatDate(long timestamp) {
        return formatDate(timestamp, "yyyy-MM-dd HH:mm:ss");
    }

}
