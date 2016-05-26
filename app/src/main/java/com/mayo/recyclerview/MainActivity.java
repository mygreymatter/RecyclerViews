package com.mayo.recyclerview;

import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements Callback {

    private RecyclerView mRecycler;
    private NamesHeaderAdapter adapter;
    private DeckHeaderLayoutManager manager;
    private GestureDetectorCompat mDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDetector = new GestureDetectorCompat(this,new MyGesture());

        adapter = new NamesHeaderAdapter();
        manager = new DeckHeaderLayoutManager(this, getScreenDensity());

        mRecycler = (RecyclerView) findViewById(R.id.names_list);
        mRecycler.setLayoutManager(manager);
        mRecycler.setHasFixedSize(true);

        mRecycler.setAdapter(adapter);
        mRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mDetector.onTouchEvent(event))
                    return true;
                return false;
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        mDetector.onTouchEvent(event);
        return false;
    }

    @Override
    public void setFlingAction(final int dy) {
        //Logger.print("distanceY: " + dy);
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
            //Logger.print("onDown");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Logger.print("onScroll: " + distanceY);
            return super.onScroll(e1,e2,distanceX,distanceY);
            //true means the onScroll is handled
            //return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Logger.print("onFling yVelocity: " + velocityY + " " + (e2.getY() - e1.getY()) );
            setFlingAction(manager.getFlingDisplacement(velocityY));
            return true;
            //return true;
        }


    }

}
