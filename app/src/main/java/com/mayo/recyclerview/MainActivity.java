package com.mayo.recyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements Callback{

    private RecyclerView mRecycler;
    private NamesAdapter adapter;
    private MagneticLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LinearLayoutManager manager = new LinearLayoutManager(this);
        //final FixedGridLayoutManager manager = new FixedGridLayoutManager();
        manager = new MagneticLayoutManager(this);
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
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.print("Scroll");
                mRecycler.smoothScrollBy(0,dy);
            }
        },0);*/
    }
}
