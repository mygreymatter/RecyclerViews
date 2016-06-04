package com.mayo.recyclerview.layouts;

import android.content.Context;
import android.os.CountDownTimer;

import com.mayo.recyclerview.Logger;
import com.mayo.recyclerview.TimeZoneHandler;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by mayo on 3/6/16.
 */
public class Timer{

//    private UpdateTimer mUpdateTimer;
//    private ExitRestaurantMode mExitRestaurantMode;

    private int hours_tens = 0;
    private int hours_units = 0;
    private int minutes_tens = 0;
    private int minutes_units = 0;
    private int seconds_tens = 0;
    private int seconds_units = 0;

    private boolean isTimerRunning = false;

    public Timer(Context context) {
//        this.mUpdateTimer = (UpdateTimer) context;
//        this.mExitRestaurantMode = (ExitRestaurantMode) context;
    }

    public void setHoursTens(int hours_tens) {
        this.hours_tens = hours_tens;
    }

    public void setHoursUnits(int hours_units) {
        this.hours_units = hours_units;
    }

    public void setMinutesTens(int minutes_tens) {
        this.minutes_tens = minutes_tens;
    }

    public void setMinutesUnits(int minutes_units) {
        this.minutes_units = minutes_units;
    }

    public void setSecondsTens(int seconds_tens) {
        this.seconds_tens = seconds_tens;
    }

    public void setSecondsUnits(int seconds_units) {
        this.seconds_units = seconds_units;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public void resetTimer() {
        hours_tens = 0;
        hours_units = 0;
        minutes_tens = 0;
        minutes_units = 0;
        seconds_tens = 0;
        seconds_units = 0;

        isTimerRunning = false;
        timer.cancel();
        timer = null;
    }

    // public static int hours_tens = 0;
    // public static int hours_units = 0;
    // public static int minutes_tens = 0;
    // public static int minutes_units = 0;
    // public static int seconds_tens = 0;
    // public static int seconds_units = 0;
    private CountDownTimer timer;

    private long total_time_milli = -1;

    // private long count = 0;

    // Gets the expiry time of valid Zapp token
    public long getExpiryTime() {

        /*String expiryTimeStamp = TimeZoneHandler.changeUTCToLOCAL(GazappSession
                .getInstance().expiryTime);*/


        String expiryTimeStamp = null;
        long difference = TimeZoneHandler
                .diffBetweenLOCALTimes(expiryTimeStamp);

        long seconds = difference / 1000 % 60;
        long minutes = difference / (60 * 1000) % 60;
        long hours = difference / (60 * 60 * 1000) % 24;

        seconds_units = (int) (seconds % 10);
        seconds_tens = (int) (seconds / 10);
        minutes_units = (int) (minutes % 10);
        minutes_tens = (int) (minutes / 10);
        hours_units = (int) (hours % 10);
        hours_tens = (int) (hours / 10);

        total_time_milli = difference + 1000;

        return total_time_milli;
    }

    public void setTimer() {
//		Log.i("MAYO", "Timer set");

/*		Log.i("MAYO", "Timer Seconds Units: " + Integer.toString(seconds_units));
		Log.i("MAYO", "Timer Seconds Tens: " + Integer.toString(seconds_tens));
		Log.i("MAYO", "Timer Minutes Units: " + Integer.toString(minutes_units));
		Log.i("MAYO", "Timer Minutes Tens: " + Integer.toString(minutes_tens));
		Log.i("MAYO", "Timer Hour Units: " + Integer.toString(hours_units));
		Log.i("MAYO", "Timer Hour Tens: " + Integer.toString(hours_tens));*/

//        mUpdateTimer.updateTimer(seconds_units,0);
//        mUpdateTimer.updateTimer(seconds_tens,1);
//        mUpdateTimer.updateTimer(minutes_units,2);
//        mUpdateTimer.updateTimer(minutes_tens,3);
//        mUpdateTimer.updateTimer(hours_units,4);
//        mUpdateTimer.updateTimer(hours_tens,5);
    }

    // Sets the timer and steps after finishing the timer
    public void startTimer() {
//		Log.i("MAYO", "Timer start");
        isTimerRunning = true;
        timer = new CountDownTimer(getExpiryTime(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // count++;
                // Log.i("MAYO","Tick: "+Long.toString(millisUntilFinished));
                // Log.i("MAYO","Count: "+Long.toString(count));
                seconds_units--;
                if (seconds_units == -1) {
                    seconds_units = 9;
                    seconds_tens--;
                    if (seconds_tens == -1) {
                        seconds_tens = 5;
                        minutes_units--;
                        if (minutes_units == -1) {
                            minutes_units = 9;
                            minutes_tens--;
                            if (minutes_tens == -1) {
                                minutes_tens = 5;
                                hours_units--;
                                if (hours_units == -1) {
                                    hours_units = 9;
                                    hours_tens--;
                                    if (hours_tens == -1) {
                                        return;
                                    }// check tens position of minute

//                                    mUpdateTimer.updateTimer(hours_tens, 5);
                                }// check tens position of minute

//                                mUpdateTimer.updateTimer(hours_units, 4);
                            }// check tens position of minute

//                            mUpdateTimer.updateTimer(minutes_tens, 3);
                        }// check units position of minute

//                        mUpdateTimer.updateTimer(minutes_units, 2);
                    }// check tens position of second

//                    mUpdateTimer.updateTimer(seconds_tens, 1);
                }// check units position of second

//                mUpdateTimer.updateTimer(seconds_units, 0);
            }

            @Override
            public void onFinish() {
                // Log.i("MAYO", "Finish timer: " + "Count: " +
                // Long.toString(count));
             /*   mUpdateTimer.updateTimer(0, 0);
                // ActGlobal.exitRestaurantMode();
                mExitRestaurantMode.exitRestaurantMode();*/
            }

        }.start();
    }

}

