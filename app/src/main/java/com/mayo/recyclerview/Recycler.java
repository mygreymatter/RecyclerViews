package com.mayo.recyclerview;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mayo on 20/5/16.
 */
public class Recycler extends Application {

    private static Recycler mInstance = null;
    public Map<Integer,Integer> viewHeights = new HashMap<>();
    public boolean hasExpanded;
    public int imageDimension;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static Recycler getInstance(){
        return mInstance;
    }
}
