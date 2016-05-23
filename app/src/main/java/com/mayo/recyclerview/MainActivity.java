package com.mayo.recyclerview;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
    private int samples = 0;

    //direction
    private static final int DIRECTION_UP = 0;
    private static final int DIRECTION_DOWN = 1;
    private int mDirection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeader = (RelativeLayout) findViewById(R.id.header);
        adapter = new NamesAdapter();
        manager = new CardSlideLayoutManager(this, getScreenDensity());
        manager.setScrolling(true);

        mRecycler = (RecyclerView) findViewById(R.id.names_list);
        mRecycler.setLayoutManager(manager);

        mRecycler.setAdapter(adapter);

        mDetector = new GestureDetectorCompat(this,new MyGesture());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public int getHeaderTop() {
        return mHeaderTop;
    }

    @Override
    public void setHeaderTop(final int y) {
        if(y > 0){
            if(mHeaderTop > -1120){
                mHeader.post(new Runnable() {
                    @Override
                    public void run() {

                        samples++;
                        if(samples > 0 && samples >= 3){
                           mDirection = DIRECTION_UP;
                        }

                        if(mDirection == DIRECTION_UP) {
                            mHeaderTop -= y;
                            if (mHeaderTop < -1120)
                                mHeaderTop = -1120;

                            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeader.getLayoutParams();
                            lp.setMargins(0, mHeaderTop, 0, 0);

                            mHeader.requestLayout();
                        }
                    }
                });
            }
        }else{
            if(mHeaderTop < 0){
                mHeader.post(new Runnable() {
                    @Override
                    public void run() {

                        samples--;
                        if(samples < 0 && samples <= -3){
                            mDirection = DIRECTION_DOWN;
                        }

                        if(mDirection == DIRECTION_DOWN) {

                            mHeaderTop -= y;
                            if (mHeaderTop > 0)
                                mHeaderTop = 0;

                            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mHeader.getLayoutParams();
                            lp.setMargins(0, mHeaderTop, 0, 0);

                            mHeader.requestLayout();
                        }


                    }
                });
            }
        }
    }

    @Override
    public void setFirstItem(int item) {

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

    class MyGesture extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            Logger.print("Down");
            samples = 0;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float y) {
            Logger.print("scroll: " + y);
            setHeaderTop((int) y);
            return true;
        }
        
    }

}
