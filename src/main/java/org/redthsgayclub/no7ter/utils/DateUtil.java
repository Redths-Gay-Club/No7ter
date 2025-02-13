package org.redthsgayclub.no7ter.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static String localformatTimestamp(long epoch) {
        final SimpleDateFormat Df = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss");
        //SimpleDateFormat Df = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
        //Calendar currentTime = Calendar.getInstance();
        return Df.format(epoch);
    }

    public static String ESTformatTimestamp(long epoch) {
        final SimpleDateFormat ESTDf = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss 'EST'");
        //SimpleDateFormat ESTDf = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
        final TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");
        ESTDf.setTimeZone(etTimeZone);
        //Calendar currentTime = Calendar.getInstance();
        return ESTDf.format(epoch);
    }

    public static String localformatTimestampday(long epoch) {
        final SimpleDateFormat Df = new SimpleDateFormat("dd/MM/yyyy");
        return Df.format(epoch);
    }

    /**
     * Returns the time since input as a string message
     */
    public static String timeSince(long epoch) {
        final long diff = (new Date()).getTime() - epoch;
        if (diff < 1000 * 60) { // less than 60 sec
            return diff / 1000 + "sec";
        } else if (diff < 1000 * 60 * 60) { // less than 60 minutes
            final long sec;
            final long min;
            sec = diff / 1000;
            min = sec / 60;
            return min + "min" + sec % 60 + "sec";
        } else if (diff < 1000 * 60 * 60 * 24) { // less than 24hours
            final long min;
            final long hours;
            min = diff / (1000 * 60);
            hours = min / 60;
            return hours + "h" + min % 60 + "min";
        } else { // more than a day
            final long min;
            final long hours;
            final long days;
            min = diff / (1000 * 60);
            hours = min / 60;
            days = hours / 24;
            return days + (days == 1 ? "day" : "days") + hours % 24 + "h";
            //return String.valueOf(days) + (days==1?"day":"days") + String.valueOf(hours%24) + "h" + String.valueOf(min%60) + "min";
        }
    }

}
