package com.mayo.recyclerview;

/**
 * Created by mayo on 20/5/16.
 */
public interface Callback {
    int getHeaderTop();
    void setFirstItem(int item);
    void setHeaderTop(int top);
    void setFlingAction(int dy);
}
