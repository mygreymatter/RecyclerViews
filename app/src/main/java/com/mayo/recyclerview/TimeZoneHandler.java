package com.mayo.recyclerview;

import android.annotation.SuppressLint;

import com.mayo.recyclerview.layouts.Timer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by mayo on 3/6/16.
 */
@SuppressLint("SimpleDateFormat")
@SuppressWarnings("unused")
public class TimeZoneHandler {

    private static final String UTC_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private Timer mTimer;

    //Convert the UTC to LOCAL
    public static String changeUTCToLOCAL(String timeStamp) {

        SimpleDateFormat format = new SimpleDateFormat(UTC_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(timeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        // Getting LOCAL Time Zone
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        // converting UTC to LOCAL
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format2.setTimeZone(timeZone);

        return format2.format(date);
    }

    //finds the difference between the future timestamp and the current timestamp
    public static long diffBetweenLOCALTimes(String futureTimeStamp) {
        Date present;
        Date future;

        Calendar calendar = Calendar.getInstance();
        present = calendar.getTime();//getCurrentTimeUTC();

        SimpleDateFormat format = new SimpleDateFormat(UTC_FORMAT);
        format.setTimeZone(TimeZone.getDefault());
        try {
            future  = format.parse(futureTimeStamp);
            //in milliseconds
            long difference = future.getTime() - present.getTime();

            return difference;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return (long) 0.0;
    }

}

