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

import com.mayo.recyclerview.layouts.StickyHeaderLayoutManager;

public class MainActivity extends AppCompatActivity implements DeckHeaderCallback {

    private RecyclerView mRecycler;
    private NamesHeaderAdapter mLengthyAdapter;
    private StickyHeaderLayoutManager mLengthyManager;
    private GestureDetectorCompat mDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRecycler = (RecyclerView) findViewById(R.id.names_list);

        mLengthyAdapter = new NamesHeaderAdapter();
        mLengthyManager = new StickyHeaderLayoutManager(this, getScreenDensity());
        mRecycler.setLayoutManager(mLengthyManager);
        mRecycler.setAdapter(mLengthyAdapter);

        mRecycler.setHasFixedSize(true);


        /*
        mDetector = new GestureDetectorCompat(this, new MyGesture());
        mRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mDetector.onTouchEvent(event))
                    return true;
                return false;
            }
        });*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return false;
    }

    @Override
    public void setFlingAction(final int dy) {
        //LogBuilder.build("distanceY: " + dy);
        mRecycler.smoothScrollBy(0, dy);
    }

    /*public void scrollUp(View v){
        mRecycler.smoothScrollBy(0, 100);
    }*/


    @Override
    public void setStickyHeader(boolean sticked, int firstItem) {

    }

    private int getScreenDensity() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

//        LogBuilder.build("Density: " + metrics.densityDpi + " " + metrics.density);
        return (int) metrics.density;
    }

    class MyGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            //LogBuilder.build("onDown");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //LogBuilder.build("onScroll: " + distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
            //true means the onScroll is handled
            //return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            LogBuilder.build("onFling yVelocity: " + velocityY + " " + (e2.getY() - e1.getY()));
            setFlingAction(mLengthyManager.getFlingDisplacement(velocityY));
            return true;
        }


    }

}
