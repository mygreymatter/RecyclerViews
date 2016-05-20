package com.mayo.recyclerview;

/**
 * Created by mayo on 20/5/16.
 */
public interface Callback {
    void setFirstItem(int firstItem,int height);
    void setSecondItemHeight(int height);
    void setOtherItemHeight(int height);
    void setItemHeight(int pos,int height);
    //void setOtherItemHeight(int height);
}
