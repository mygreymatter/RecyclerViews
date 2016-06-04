package com.mayo.recyclerview;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.mayo.recyclerview.layouts.DeckHeaderLayoutManager;
import com.mayo.recyclerview.layouts.Timer;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements Callback {

    private RecyclerView mRecycler;
    private NamesHeaderAdapter adapter;
    private DeckHeaderLayoutManager manager;
    private GestureDetectorCompat mDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDetector = new GestureDetectorCompat(this, new MyGesture());

        adapter = new NamesHeaderAdapter();
        manager = new DeckHeaderLayoutManager(this, getScreenDensity());

        mRecycler = (RecyclerView) findViewById(R.id.names_list);
        mRecycler.setLayoutManager(manager);
        mRecycler.setHasFixedSize(true);

        mRecycler.setAdapter(adapter);
        mRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mDetector.onTouchEvent(event))
                    return true;
                return false;
            }
        });

     /*   new CountDownTimer(30000,1000){
            @Override
            public void onTick(long tick) {
                Logger.print("Timer Tick: " + tick/1000);
            }

            @Override
            public void onFinish() {
                Logger.print("Timer onFinish");
            }
        }.start();
*/

        Timer timer = new Timer(this);
        //timer.getExpiryTime();
        Calendar c = Calendar.getInstance();

        Date today = c.getTime();
        Logger.print("Today: " + today);

        c.add(Calendar.DATE, 1);

        Date tomorrow = c.getTime();
        Logger.print("Tomorrow: " + tomorrow);

        Logger.print("Diff: " + getDateDiff(today,tomorrow,TimeUnit.MINUTES));
    }

    private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //setFlingAction(-50);
            }
        }, 1000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return false;
    }

    @Override
    public void setFlingAction(final int dy) {
        //Logger.print("distanceY: " + dy);
        mRecycler.smoothScrollBy(0, dy);
    }

    @Override
    public void notifyDataChange() {
        //if (!mRecycler.isComputingLayout())
        Logger.print("notifyDataChange");
        adapter.notifyDataSetChanged();
    }


    private int getScreenDensity() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

//        Logger.print("Density: " + metrics.densityDpi + " " + metrics.density);
        return (int) metrics.density;
    }

    class MyGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            //Logger.print("onDown");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Logger.print("onScroll: " + distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
            //true means the onScroll is handled
            //return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Logger.print("onFling yVelocity: " + velocityY + " " + (e2.getY() - e1.getY()));
            setFlingAction(manager.getFlingDisplacement(velocityY));
            return true;
            //return true;
        }


    }

}
