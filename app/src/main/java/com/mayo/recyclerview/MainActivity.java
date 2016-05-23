package com.mayo.recyclerview;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.mayo.recyclerview.layouts.CardSlideLayoutManager;

public class MainActivity extends AppCompatActivity implements Callback {

    //directions
    private static final int DIRECTION_UP = 0;
    private static final int DIRECTION_DOWN = 1;
    private RecyclerView mRecycler;
    private NamesAdapter adapter;
    private CardSlideLayoutManager manager;
    private GestureDetectorCompat mDetector;
    private RelativeLayout mHeader;
    private int mHeaderTop = 0;
    private int mFirstItem = 0;
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

    }


    @Override
    public int getHeaderTop() {
        return 0;
    }

    @Override
    public void setHeaderTop(int top) {

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


}
