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
import com.mayo.recyclerview.layouts.DeckHeaderLayoutManager;

public class MainActivity extends AppCompatActivity implements Callback {

    private RecyclerView mRecycler;
    private NamesHeaderAdapter adapter;
    private DeckHeaderLayoutManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new NamesHeaderAdapter();
        manager = new DeckHeaderLayoutManager(this, getScreenDensity());
        manager.setScrolling(true);

        mRecycler = (RecyclerView) findViewById(R.id.names_list);
        mRecycler.setLayoutManager(manager);

        mRecycler.setAdapter(adapter);

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
