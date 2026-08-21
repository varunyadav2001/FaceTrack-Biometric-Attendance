package com.example.attendance_system.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String getCurrentDateDb() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDateDisplay(String dbDate) {
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault());
            Date date = inFormat.parse(dbDate);
            if (date != null) {
                SimpleDateFormat outFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
                return outFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dbDate;
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + "m ago";
        if (diff < 86400000) return (diff / 3600000) + "h ago";
        if (diff < 604800000) return (diff / 86400000) + "d ago";

        return formatDateDisplay(new SimpleDateFormat(Constants.DATE_FORMAT_DB, Locale.getDefault()).format(new Date(timestamp)));
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentDay() {
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
    }
}
