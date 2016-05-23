package com.mayo.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.mayo.recyclerview.layouts.CardSlideLayoutManager;

public class MainActivity extends AppCompatActivity implements Callback{

    private RecyclerView mRecycler;
    private NamesAdapter adapter;
    private CardSlideLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LinearLayoutManager manager = new LinearLayoutManager(this);
        //final FixedGridLayoutManager manager = new FixedGridLayoutManager();
        //manager = new MagneticLayoutManager(this);
        manager = new CardSlideLayoutManager(this,getScreenDensity());
        //JustLayoutManager manager = new JustLayoutManager();
        //manager.setTotalColumnCount(2);

        adapter = new NamesAdapter();

        mRecycler = (RecyclerView) findViewById(R.id.names_list);
        mRecycler.setLayoutManager(manager);
        mRecycler.setAdapter(adapter);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Logger.print("Height of RecyclerView: " + mRecycler.getHeight());

    }


    @Override
    public void setFlingAction(final int dy) {
        mRecycler.smoothScrollBy(0,dy);
    }

    private int getScreenDensity() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        //screenHeight = metrics.heightPixels;
        //screenWidth = metrics.widthPixels;
        Logger.print("Density: " + metrics.densityDpi + " " + metrics.density);
        return (int) metrics.density;
    }
}
