package com.mayo.recyclerview.layouts;

import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.mayo.recyclerview.Logger;

/**
 * Created by mayo on 18/5/16.
 */
public class JustLayoutManager extends RecyclerView.LayoutManager {

    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int mVisibleRowCount;
    private int mFirstItem;
    private int mPrevFirstItem;
    private int mFirstItemTop;

    //directions
    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;
    private boolean isCalledOnce;
    private View v;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        if (getChildCount() == 0) {
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            //assume that the size of each child view is same
            //calculate the decorated values upfront
            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap);

            detachAndScrapView(scrap, recycler);
        }

        updateVisibleRowCount();
        detachAndScrapAttachedViews(recycler);

        layoutViews(DIRECTION_NONE, recycler, state);

        /*if(isCalledOnce) {
            scrollVerticallyBy(300, recycler, state);
        }
        isCalledOnce = true;*/
    }

    private void updateVisibleRowCount() {
        if(getChildCount() == 0)
            mFirstItem = 0;

        if ((getChildCount() == 0) ||
                ((getChildCount() > 0)  && (mFirstItemTop == 0))) {
            mVisibleRowCount = getVerticalSpace() / mDecoratedChildHeight;
            if (getVerticalSpace() % mDecoratedChildHeight > 0)
                mVisibleRowCount++;

        } else {//mFirstItemTop != 0 mFirstItemTop < 0
            mVisibleRowCount = 0;
            int length = mDecoratedChildHeight + mFirstItemTop;

            do{
                mVisibleRowCount++;
                length += mDecoratedChildHeight;
            }while (length < getVerticalSpace());

            int visiblePartOfFirstItem = 0;
            if(mFirstItemTop < 0)
                visiblePartOfFirstItem = mDecoratedChildHeight + mFirstItemTop;

            int visibleAreaOfList = visiblePartOfFirstItem + (mVisibleRowCount - 1) * mDecoratedChildHeight;
            /*Logger.print("Rows: " + mVisibleRowCount +
                    " H: " + getVerticalSpace() +
                    " Height: " + visibleAreaOfList);*/

            if(getVerticalSpace() -  visibleAreaOfList > 0)
                mVisibleRowCount++;
            //Logger.print("Visible Rows: " + mVisibleRowCount);
        }

        if (mVisibleRowCount > getItemCount())
            mVisibleRowCount = getItemCount();

        //Logger.print("UpdateVisibleRowCount - FirstItem: " + mFirstItem + " RowCount: " + mVisibleRowCount);
    }

    private void layoutViews(int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {

        SparseArray<View> viewCache = new SparseArray<>(getChildCount());
        int startTopOffset = 0;

        if (getChildCount() > 0) {

            v = getChildAt(mFirstItem);
            startTopOffset = mFirstItemTop;

            //cache all views by their exisiting position, before updating counts
            for (int i = 0; i < getChildCount(); i++) {
                viewCache.put(getAdapterPosition(i,mPrevFirstItem), getChildAt(i));
            }

            //detach all views
            for (int i = 0; i < viewCache.size(); i++) {
                detachView(viewCache.get(getAdapterPosition(i,mPrevFirstItem)));
            }

        }

        //View v;
        int adapterPostion;
        int vTop = startTopOffset;

        for (int i = 0; i < mVisibleRowCount; i++) {
            adapterPostion = getAdapterPosition(i,mFirstItem);
            //Logger.print("Adapter Position: " + adapterPostion + " " + i);

            v = viewCache.get(adapterPostion);

            if (v == null) {
                v = recycler.getViewForPosition(adapterPostion);
                addView(v);
                measureChildWithMargins(v, 0, 0);
                layoutDecorated(v, 0, vTop,
                        mDecoratedChildWidth,
                        vTop + mDecoratedChildHeight);
            } else {
                attachView(v);
                viewCache.remove(adapterPostion);
            }
            /*Logger.print("Adapter Pos: "+ adapterPostion +
                    " Top: " + vTop +
                    " Bottom: " + (vTop + mDecoratedChildHeight));*/
            vTop += mDecoratedChildHeight;
        }

        /*
         * Finally, we ask the Recycler to scrap and store any views
         * that we did not re-attach. These are views that are not currently
         * necessary because they are no longer visible.
         */
        for (int i=0; i < viewCache.size(); i++) {
            final View removingView = viewCache.valueAt(i);
            recycler.recycleView(removingView);
        }
    }

    /**
     * updates mFirstItem and mFirstItemTop after the scrolling
     *
     * @param scrolledBy
     */
    private void updateVisibleFirstItem(int scrolledBy) {


        if (scrolledBy > 0) {//scrolled up
            v = getChildAt(0);
            int topOfV = getDecoratedTop(v);
            //Logger.print("ScrolledBy: " + scrolledBy + " Top: " + topOfV);
            topOfV = topOfV < 0 ? -topOfV : topOfV;//changes the sign for convenience

            if (topOfV < mDecoratedChildHeight) {
                //no change of mFirstItem
                mPrevFirstItem = mFirstItem;
                //set the top of the mFirstItem
                mFirstItemTop = -topOfV;
                //Logger.print("FirstItem: " + mFirstItem + " First Top: " + mFirstItemTop + " Rows:" + mVisibleRowCount);
                return;
            } else {
                mPrevFirstItem = mFirstItem;
                mFirstItem += topOfV / mDecoratedChildHeight;

                if(mFirstItem + mVisibleRowCount > getItemCount()){
                    mFirstItem = getItemCount() - mVisibleRowCount;

                    int l =  mVisibleRowCount * mDecoratedChildHeight;

                    if(l > getVerticalSpace()){
                        mFirstItemTop = getVerticalSpace() - l;
                    }else{
                        mVisibleRowCount++;
                        mFirstItem = getItemCount() - mVisibleRowCount;
                        l =  mVisibleRowCount * mDecoratedChildHeight;
                        mFirstItemTop = getVerticalSpace() - l;
                    }
                }else {
                    if ((topOfV % mDecoratedChildHeight == 0))
                        mFirstItemTop = 0;
                    else {
                        int multiple = topOfV / mDecoratedChildHeight;
                        mFirstItemTop = -(topOfV - (multiple * mDecoratedChildHeight));
                    }
                }

            }

            //Logger.print("First: " + mFirstItem + " Top: " + mFirstItemTop);

        } else {//scrolled down
            v = getChildAt(0);
            int topOfV = getDecoratedTop(v);

            if (topOfV < 0) {
                //no change of mFirstItem
                mPrevFirstItem = mFirstItem;
                //set the top of the mFirstItem
                mFirstItemTop = topOfV;
                return;
            }else{
                if(topOfV == 0){
                    mPrevFirstItem = mFirstItem;
                    mFirstItemTop = 0;
                }else if(topOfV > 0){
                    mPrevFirstItem = mFirstItem;

                    if(topOfV < mDecoratedChildHeight){
                        mFirstItem -= 1;
                        mFirstItemTop = topOfV - mDecoratedChildHeight;
                    }else{
                        int multiple = topOfV/mDecoratedChildHeight;
                        mFirstItem -= multiple + 1;
                        mFirstItemTop = ((multiple + 1) * mDecoratedChildHeight) - topOfV;
                        mFirstItemTop = mFirstItemTop > 0? -mFirstItemTop : mFirstItemTop;
                    }

                    if(mFirstItem < 0) {
                        mFirstItem = 0;
                        mFirstItemTop = 0;
                    }

                }
            }
        }
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //Logger.print("ScrollVerticallyBy");

        if (getChildCount() == 0)
            return 0;

        View topView = getChildAt(0);
        View bottomView = getChildAt(getChildCount() - 1);
        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);

        if (viewSpan < getVerticalSpace()) {
            Logger.print("Cannot scroll");
            return 0;
        }

        int delta;
        int heightOfList = getVerticalSpace();
        boolean bottomBoundReached = false;
        boolean topBoundReached = false;

        if(getLastItem() == getItemCount()){
            v = getChildAt(getChildCount() - 1);
            int bottom = getDecoratedBottom(v);

            if(bottom - dy < heightOfList){
                //if not set dy, there will be gap at the bottom
                dy = heightOfList - bottom;
            }
            if(bottom <= getVerticalSpace() + 20) {
                bottomBoundReached = true;
            }
        }else if(mFirstItem == 0){
            v = getChildAt(0);
            int top = getDecoratedTop(v);
            //Logger.print("Top: " + top +" dy: " + dy);

            if(-dy+top > 0){
                dy = top;
            }

            //Logger.print("Changed dy: " + dy);
            if(top > 0){
                topBoundReached = true;
            }
        }

        if (dy > 0) {
            if (bottomBoundReached) {
                //Logger.print("BottomBound Reached!");
                return 0;
            }
            //Logger.print("Scrolling Up");
        } else {
            if (topBoundReached) {
                //Logger.print("TopBound Reached!");
                return 0;
            }
            //Logger.print("Scrolling Down");
        }

        delta = -dy;
        //the list scrolled with displacement of delta
        offsetChildrenVertical(delta);

        updateVisibleFirstItem(dy);
        updateVisibleRowCount();

        if (dy > 0) {
            layoutViews(DIRECTION_UP, recycler, state);
        }else{
            layoutViews(DIRECTION_DOWN, recycler, state);
        }

        return -delta;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    /**
     * @param i the index of the item on the screen
     * @return the index of the item w.r.t adapter
     */
    private int getAdapterPosition(int i,int firstItem) {
        return firstItem + i;
    }

    /**
     * @return the height of the recyclerview
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    /**
     * @return the adapter position of the item that is visible at the end of the screen
     */
    private int getLastItem() {
        return mFirstItem + mVisibleRowCount;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        if(position > getItemCount()){
            Log.e("JustLayoutManager","cannot scroll to the position: " + position);
            return;
        }

        recyclerView.smoothScrollBy(0,200);

        /*
         * LinearSmoothScroller's default behavior is to scroll the contents until
         * the child is fully visible. It will snap to the top-left or bottom-right
         * of the parent depending on whether the direction of travel was positive
         * or negative.
         *//*
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            *//*
             * LinearSmoothScroller, at a minimum, just need to know the vector
             * (x/y distance) to travel in order to get from the current positioning
             * to the target.
             *//*
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return new PointF(0,-300);
            }
        };

        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);*/
    }
}
