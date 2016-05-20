package com.mayo.recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements Callback{

    private RecyclerView mRecycler;
    private NamesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LinearLayoutManager manager = new LinearLayoutManager(this);
        //FixedGridLayoutManager manager = new FixedGridLayoutManager();
        MagneticLayoutManager manager = new MagneticLayoutManager(this);
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

        Logger.print("Height of RecyclerView: " + mRecycler.getHeight());
    }

    @Override
    public void setItemHeight(int pos,int height) {
        adapter.setItemHeight(pos,height);
    }
}
