package com.mayo.recyclerview;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mayo on 20/5/16.
 */
public class Gazapp extends Application {

    private static Gazapp mInstance = null;
    public Map<Integer,Integer> viewHeights = new HashMap<>();
    public boolean hasExpanded;
    public int imageDimension;
    public boolean hasHeaderSticky;
    public int firstItem;


    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static Gazapp getGazapp(){
        return mInstance;
    }
}
