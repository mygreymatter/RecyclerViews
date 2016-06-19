package com.mayo.recyclerview;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mayo.recyclerview.layouts.StickyHeaderLayoutManager;

public class MainActivity extends AppCompatActivity implements DeckHeaderCallback {

    private RecyclerView mRecycler;
    private NamesHeaderAdapter mAdapter;
    private StickyHeaderLayoutManager mLayoutManager;
    private GestureDetectorCompat mDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRecycler = (RecyclerView) findViewById(R.id.names_list);

        mAdapter = new NamesHeaderAdapter();
        mLayoutManager = new StickyHeaderLayoutManager(this, getScreenDensity());
        mLayoutManager.setScrollingToBorders(true);
        mLayoutManager.setItemAnimatation(true);

        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setAdapter(mAdapter);

        mRecycler.setHasFixedSize(true);


        mDetector = new GestureDetectorCompat(this, new MyGesture());
        mRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });
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


    @Override
    public void setStickyHeader(boolean sticked, int firstItem) {
        if(mRecycler.getChildCount() > 0){
            View v = mRecycler.getChildAt(0);
            RelativeLayout r = (RelativeLayout) v.findViewById(R.id.inner_layout);
            LinearLayout l = (LinearLayout) r.findViewById(R.id.store_details_layout);
            TextView t = (TextView) l.findViewById(R.id.spot_rewards);

            if(sticked) {
                t.setText("Reward " + firstItem);
            }else{
                t.setText("#Spotrewards");
            }

        }
    }

    @Override
    public void animateView(View v, int top) {

        int size = 30;
        float alpha = 0.2f;

        RelativeLayout r = (RelativeLayout) v.findViewById(R.id.inner_layout);
        TextView rewardName = (TextView) r.findViewById(R.id.reward_name);
        LinearLayout ll = (LinearLayout) r.findViewById(R.id.info_layout);

        switch (top / 100) {
            case 1:
                size = 38;
                alpha = 1.0f;
                break;
            case 2:
                size = 36;
                alpha = 0.8f;
                break;
            case 3:
                size = 34;
                alpha = 0.6f;
                break;
            case 4:
                size = 32;
                alpha = 0.4f;
                break;

            default:
                if (top >= 1000) {
                    size = 30;
                    alpha = 0.2f;
                } else if (top < 100) {
                    size = 38;
                    alpha = 1.0f;
                }
                break;
        }

//        LogBuilder.build("Animate Top: " + top + " Alpha: " + alpha + " Size: " + size);

        rewardName.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        //rewardName.setTypeface(rewardName.getTypeface(), Typeface.BOLD);
        ll.setAlpha(alpha);

    }

    @Override
    public void setHeader(int firstItem) {

    }

    private int getScreenDensity() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        LogBuilder.build("Density: " + metrics.densityDpi + " " + metrics.density);
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
            setFlingAction(mLayoutManager.getFlingDisplacement(velocityY));
            return true;
        }


    }

}
