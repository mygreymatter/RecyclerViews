package com.mayo.recyclerview;

/**
 * Created by mayo on 20/5/16.
 */
public interface DeckHeaderCallback {
    void setFlingAction(int dy);
    void setStickyHeader(boolean sticked,int firstItem);
}
