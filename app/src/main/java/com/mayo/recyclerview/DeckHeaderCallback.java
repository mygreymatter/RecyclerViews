package com.mayo.recyclerview;

import android.view.View;

/**
 * Created by mayo on 20/5/16.
 */
public interface DeckHeaderCallback {
    void setFlingAction(int dy);
    void setStickyHeader(boolean sticked,int firstItem);
    void setHeader(int firstItem);

    void animateView(View v, int top);
}
