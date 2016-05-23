package com.mayo.recyclerview;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mayo.recyclerview.layouts.CardSlideLayoutManager;

public class MainActivity extends AppCompatActivity implements Callback {

    private RecyclerView mRecycler;
    private NamesAdapter adapter;
    private CardSlideLayoutManager manager;
    private GestureDetectorCompat mDetector;
    private RelativeLayout mHeader;
    private int mHeaderTop = 0;
    private int mFirstItem = 0;

    //directions
    private static final int DIRECTION_UP = 0;
    private static final int DIRECTION_DOWN = 1;
    private int mDirection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeader = (RelativeLayout) findViewById(R.id.header);
        //LinearLayoutManager manager = new LinearLayoutManager(this);
        //final FixedGridLayoutManager manager = new FixedGridLayoutManager();
        //manager = new MagneticLayoutManager(this);
        manager = new CardSlideLayoutManager(this, getScreenDensity());
        //JustLayoutManager manager = new JustLayoutManager();
        //manager.setTotalColumnCount(2);
        manager.setScrolling(true);

        adapter = new NamesAdapter();

        mRecycler = (RecyclerView) findViewById(R.id.names_list);
        mRecycler.setLayoutManager(manager);
        mRecycler.setAdapter(adapter);

        mDetector = new GestureDetectorCompat(this, new MyGestureDetector());

        toggleRecyclerTouchListener(true);

    }

    private void toggleRecyclerTouchListener(boolean set){
        if(set){
            Logger.print("Touch Listener Set");
            mRecycler.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int y = /*(int) (event.getY() / 10)*/10;
                    Logger.print("Recyler: " + event.getY() + " " +  y);
                    if(mHeaderTop > -1120){
                        setHeaderTop(y);
                    }
                    return true;
                }
            });
        }else{
            mRecycler.setOnTouchListener(null);
            Logger.print("Touch Listener Removed");
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            Logger.print("onDown");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float x, final float y) {
            //Logger.print("onScroll: " + distanceY);
            setHeaderTop((int) y);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Logger.print("onFling: " + velocityY);
            return true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //Logger.print("Height of RecyclerView: " + mRecycler.getHeight());

    }

    @Override
    public int getHeaderTop() {

        return mHeaderTop;
    }

    @Override
    public void setFirstItem(int item) {
        mFirstItem = item;
    }

    @Override
    public void setHeaderTop(final int y) {
        mHeader.post(new Runnable() {
            @Override
            public void run() {
                if (y > 0) {
                    mDirection = DIRECTION_UP;

                    //Logger.print("Before Top: " + mHeaderTop + " Y: " + y);
                    mHeaderTop -= y;
                    if (mHeaderTop < -1120) {
                        mHeaderTop = -1120;
                        toggleRecyclerTouchListener(false);
                    }

                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeader.getLayoutParams();
                    lp.setMargins(0, mHeaderTop, 0, 0);

                    //Logger.print("After Top: " + mHeaderTop + " Y: " + y);

                    mHeader.requestLayout();
                } else {
                    mDirection = DIRECTION_DOWN;
                    //Logger.print("Before Top: " + mHeaderTop + " Y: " + y);
                    if(mHeaderTop >= -1120 && mFirstItem == 0){
                        toggleRecyclerTouchListener(true);
                    }
                    mHeaderTop -= y;
                    if (mHeaderTop > 0) {
                        mHeaderTop = 0;
                    }

                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeader.getLayoutParams();
                    lp.setMargins(0, mHeaderTop, 0, 0);

                    //Logger.print("After Top: " + mHeaderTop + " Y: " + y);
                    mHeader.requestLayout();
                }
            }
        });


        // Obtain MotionEvent object
        /*long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = top;

        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_SCROLL,
                x,
                y,
                metaState
        );

        this.dispatchTouchEvent(motionEvent);*/
    }

    @Override
    public void setFlingAction(final int dy) {
        mRecycler.smoothScrollBy(0, dy);
    }

    private int getScreenDensity() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        Logger.print("Density: " + metrics.densityDpi + " " + metrics.density);
        return (int) metrics.density;
    }
}
