package com.mayo.recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LinearLayoutManager manager = new LinearLayoutManager(this);
        //FixedGridLayoutManager manager = new FixedGridLayoutManager();
        MagneticLayoutManager manager = new MagneticLayoutManager();
        //JustLayoutManager manager = new JustLayoutManager();
        //manager.setTotalColumnCount(2);

        mRecycler = (RecyclerView) findViewById(R.id.names_list);
        mRecycler.setLayoutManager(manager);
        mRecycler.setAdapter(new NamesAdapter());

    }
}
