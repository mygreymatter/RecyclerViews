package com.mayo.recyclerview;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ActTimer extends AppCompatActivity {

    TextView mTimer;
    int seconds;
    private Date tomorrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_timer);

        mTimer = (TextView) findViewById(R.id.timer);

        Calendar c = Calendar.getInstance();
//        Logger.print("Today: " + c.getTime().toString());

        c.add(Calendar.DATE,1);
        tomorrow = c.getTime();
//        Logger.print("Tomorrow: " +c.getTime().toString());

        long difference = getDiff(getCurrentTime(),tomorrow) * 2;
        Logger.print("Difference: " + difference);
        mTimer.setText(getFormattedTimeString(difference));

        new CountDownTimer(difference, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimer.setText(getFormattedTimeString(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {

            }
        }.start();

    }

    private Date getCurrentTime(){
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    private long getDiff(Date present,Date tomorrow){
        return tomorrow.getTime()  - present.getTime();
    }

    private String getFormattedTimeString(long timeInSeconds) {
        String timeStr = new String();
        long sec_term = 1;
        long min_term = 60 * sec_term;
        long hour_term = 60 * min_term;
        long result = Math.abs(timeInSeconds);

        int hour = (int) (result / hour_term);
        result = result % hour_term;
        int min = (int) (result / min_term);
        result = result % min_term;
        int sec = (int) (result / sec_term);

        if (timeInSeconds < 0) {
            timeStr = "-";
        }
        if (hour > 0) {
            timeStr += hour + ":";
        }
        if (min > 0) {
            timeStr += min + ":";
        }
        if (sec > 0) {
            timeStr += sec + "";
        }
        return timeStr;
    }
}