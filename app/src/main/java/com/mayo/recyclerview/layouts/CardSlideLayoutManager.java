package com.mayo.recyclerview.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.mayo.recyclerview.DeckHeaderCallback;
import com.mayo.recyclerview.LogBuilder;
import com.mayo.recyclerview.R;

/**
 * Created by mayo on 18/5/16.
 */
public class CardSlideLayoutManager extends RecyclerView.LayoutManager {

    private int densityOfScreen;//DPPX - 1 DP(Density-Independent Pixel) has pixels
    private int UP_RANGE;
    private int DOWN_RANGE;
    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int mVisibleRowCount;
    private int mFirstItem;
    private int mSecondItemTop;
    private int mFirstItemHeight;
    private int mRecyclerViewHeight;
    private int mRowsCanFit;

    //directions
    private int mDirection;
    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;

    private boolean isCalledOnce;

    private View v;
    private RelativeLayout r;
    private boolean canScroll;

    //private Map<Integer,Integer> mHeights;
    private Context mContext;
    private DeckHeaderCallback mDeckHeaderCallback;

    public CardSlideLayoutManager(Context context,int density) {
        mContext = context;
        mDeckHeaderCallback = (DeckHeaderCallback) context;

        densityOfScreen = density;
        UP_RANGE = 333 * density;
        DOWN_RANGE = 30 * density;
    }

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

            mFirstItemHeight = mDecoratedChildHeight;
            detachAndScrapView(scrap, recycler);
        }

        mRecyclerViewHeight = /*getVerticalSpace()*/1536;
        mDecoratedChildHeight = (30 + 20 + 10) * densityOfScreen;
        mSecondItemTop = mFirstItemHeight;

        int areaLeft = mRecyclerViewHeight - mFirstItemHeight;
        int otherRows = areaLeft/mDecoratedChildHeight;

        //num of rows when the app is started
        mRowsCanFit = areaLeft/mDecoratedChildHeight/*other children*/ + 1 /*First Item*/;

        if(areaLeft - (otherRows * mDecoratedChildHeight) > 0){
            mRowsCanFit++;
        }

        /*LogBuilder.build("Gazapp Height: " + mRecyclerViewHeight +
                " FirstItem Height: " + mFirstItemHeight +
                " DecoratedHeight: " + mDecoratedChildHeight +
                " RowsCanFit: " + mRowsCanFit);*/

        updateMagnetVisibleRowCount();
        detachAndScrapAttachedViews(recycler);

        layoutViews(DIRECTION_NONE,0, recycler, state);

        if (isCalledOnce) {
            //scrollVerticallyBy(100, recycler, state);
        }
        isCalledOnce = true;
    }

    /**
     * update the rows that can be shown after magnetic scroll
     */
    private void updateMagnetVisibleRowCount() {

        mVisibleRowCount = mRowsCanFit;
        if(mSecondItemTop < UP_RANGE && mDirection == DIRECTION_UP)
            mVisibleRowCount++;
        else if(mDirection == DIRECTION_DOWN){
            mVisibleRowCount++;
            if(mSecondItemTop > DOWN_RANGE && mSecondItemTop == mFirstItemHeight){
                mVisibleRowCount--;
            }
        }

        //LogBuilder.build("Rows: " + mVisibleRowCount);

    }

    /**
     * updates the position and height of the second item after scroll
     *
     * @param scrolledBy - amount of displacment of the list
     */
    private void updateSecondItem(int scrolledBy) {
        //reduces the distance from the top. scrolledBy is positive

        //LogBuilder.build("---------------------------------------------------------");
        if (scrolledBy > 0) {
            /*if (mSecondItemHeight + scrolledBy < mFirstItemHeight) {
                mSecondItemHeight += scrolledBy;
            } else {
                mSecondItemHeight = mFirstItemHeight;
            }*/

            //mSecondItemTop = mFirstItemHeight - scrolledBy;
            mSecondItemTop -= scrolledBy;
            if (mSecondItemTop < 0)
                mSecondItemTop = 0;

        } else if (scrolledBy < 0) {
            //LogBuilder.build("Top: " + 0 + " " + mSecondItemTop + " " + mSecondItemTop);

            //detects when the first item is moved down
            if(mSecondItemTop == mFirstItemHeight && mFirstItem > 0){
                //reset the mFirstItem adapter index and its top
                mFirstItem--;
                //LogBuilder.build("-----------------Down Transition------------------------");
                mSecondItemTop = 0;
            }

            scrolledBy = -scrolledBy;
            if (mSecondItemTop + scrolledBy < mFirstItemHeight) {
                mSecondItemTop += scrolledBy;

                //prevents the pushing the last item down instantly
                /*if(mSecondItemTop < mDecoratedChildHeight)
                    mSecondItemHeight = mFirstItemHeight - mSecondItemTop;
                else {
                    //pushes the last item down slowly
                    int diff = mSecondItemTop - mDecoratedChildHeight;
                    diff = diff > mDecoratedChildHeight ? mDecoratedChildHeight : diff;

                    mSecondItemHeight = mFirstItemHeight + diff - mSecondItemTop;
                }*/
            } else {
                mSecondItemTop = mFirstItemHeight;
                //mSecondItemHeight = mDecoratedChildHeight;
            }
        }

        //LogBuilder.build("Updated Second Item Top: " + mSecondItemTop /*+ "  Height: " + mSecondItemHeight*/ + " scrolledBy: " + scrolledBy);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void layoutViews(int direction,int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //LogBuilder.build("Layout Views");

        int startTopOffset = 0;

        //LogBuilder.build("\n------------------------------------------------------------");
        if (getChildCount() > 0) {
            //LogBuilder.build("Removing Views");
            removeAllViews();
        }

        int adapterPostion;
        int vTop = startTopOffset;

        for (int i = 0; i < mVisibleRowCount; i++) {
            adapterPostion = getAdapterPosition(i, mFirstItem);
            //LogBuilder.build("Adapter Position: " + adapterPostion + " " + i + " FirstItem: " + mFirstItem);

            if (adapterPostion == -1)
                continue;

            v = recycler.getViewForPosition(adapterPostion);
            r = (RelativeLayout) v.findViewById(R.id.inner_layout);
            measureChildWithMargins(v, 0, 0);
            switch (i) {
                case 0:

                    layoutDecorated(v, 0, vTop,
                            mDecoratedChildWidth,
                            mFirstItemHeight);

                    //mHeights.put(adapterPostion,mFirstItemHeight);

                    /*LogBuilder.build(adapterPostion +
                            " FirstItem: " + mFirstItem +
                            " Top: 0" +
                            " Height: " + mFirstItemHeight);*/

                    break;
                case 1:

                    layoutDecorated(v, 0, mSecondItemTop,
                            mDecoratedChildWidth,
                            mSecondItemTop + /*mSecondItemHeight*/mFirstItemHeight);

                    /*LogBuilder.build(adapterPostion +
                            " FirstItem: " + mFirstItem +
                            " Top: " + mSecondItemTop +
                            " Height: " + mFirstItemHeight);*/

                    //mHeights.put(adapterPostion,mSecondItemHeight);

                    //vTop = mSecondItemTop;

                    //set the top of next item
                    if(direction == DIRECTION_UP){
                        if(mSecondItemTop > UP_RANGE) {
                            vTop = mFirstItemHeight + mDecoratedChildHeight;
                            //LogBuilder.build("If Direction: " + mDirection + " Next Top: " + vTop + " Second Top: " + mSecondItemTop + " dy: " + dy);
                        }else {
                            vTop = mFirstItemHeight + mDecoratedChildHeight - (UP_RANGE - mSecondItemTop);
                            if(vTop < mFirstItemHeight){
                                vTop = mFirstItemHeight;
                            }
                            //LogBuilder.build("Else Direction: " + mDirection + " Next Top: " + vTop + " Second Top: " + mSecondItemTop + " dy: " + dy);
                        }
                    }else if(direction == DIRECTION_DOWN){
                        if(mSecondItemTop < DOWN_RANGE){
                            vTop = mFirstItemHeight;
                        }else{
                            vTop = mFirstItemHeight + (mSecondItemTop - DOWN_RANGE);
                            if(vTop > mFirstItemHeight + mDecoratedChildHeight){
                                vTop = mFirstItemHeight + mDecoratedChildHeight;
                            }
                        }
                    }else if(direction == DIRECTION_NONE){
                        //LogBuilder.build("Else If Direction: " + mDirection + " Next Top: " + vTop + " Second Top: " + mSecondItemTop + " dy: " + dy);
                        vTop = mFirstItemHeight + mDecoratedChildHeight;
                    }

                    /*LogBuilder.build(adapterPostion + " FirstItem: " + mFirstItem +
                            " Direction: " + mDirection +
                            " Second Top: " + mSecondItemTop + " Next Top: " + vTop);*/

                    //vTop += mDecoratedChildHeight;

                    break;
                default:

                    int h;
                    /*if(adapterPostion == getItemCount() - 1 && mVisibleRowCount == 3){
                        h = getVerticalSpace() - (mSecondItemTop + mSecondItemHeight);
                        LogBuilder.build(adapterPostion + " FirstItem: " + mFirstItem + " Top: " + vTop + " Height: " + h + " Found Last");
                    }else{
                        h = mFirstItemHeight;
                        LogBuilder.build(adapterPostion + " FirstItem: " + mFirstItem + " Top: " + vTop + " Height: " + h);
                    }*/


                    layoutDecorated(v, 0, vTop,
                            mDecoratedChildWidth,
                            vTop + mFirstItemHeight);

                    //LogBuilder.build(adapterPostion + " FirstItem: " + mFirstItem + " Top: " + vTop);

                    vTop += mDecoratedChildHeight;

                    break;
            }

            addView(v);
        }

        //check if the second item becomes the first
        if (mSecondItemTop == 0) {
            //set the current second as the first
            mFirstItem++;
            mSecondItemTop = mFirstItemHeight;
            //mSecondItemHeight = mDecoratedChildHeight;
            //LogBuilder.build("----------------------Transition--------------------------");
        }

    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //LogBuilder.build("Scrolled By: " + dy);

        if (getChildCount() == 0)
            return 0;

      /*LogBuilder.build("scrollVertical: " + isSettling +
                  " Prev Direction: "  + (mDirection == DIRECTION_UP ? " UP" : "Down") +
                  " dy: " + dy);*/
        /*if(isSettling && mDirection == DIRECTION_DOWN && dy > 0)
            return 0;*/

        int delta;
        boolean bottomBoundReached = false;
        boolean topBoundReached = false;

        //LogBuilder.build("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());

        if (mFirstItem == getItemCount() - 2) {
            //LogBuilder.build("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());
            bottomBoundReached = true;
        } else if (mFirstItem == 0) {
            v = getChildAt(1);
            int top = getDecoratedTop(v);
            //LogBuilder.build("Top: " + top +" dy: " + dy);

            /*if (-dy + top > mFirstItemHeight) {
                dy = mFirstItemHeight - top;
                LogBuilder.build("Changed dy: " + dy);
            }
*/
            //LogBuilder.build("Changed dy: " + dy + " Top: " + top + " " + mFirstItemHeight);
            if (top == mFirstItemHeight) {
                topBoundReached = true;
            }
        }

        if (dy > 0) {
            if (bottomBoundReached) {
                LogBuilder.build("BottomBound Reached!");
                return 0;
            }
            //LogBuilder.build("Scrolling Up");
        } else {
            if (topBoundReached) {
                LogBuilder.build("TopBound Reached!");
                return 0;
            }
            //LogBuilder.build("Scrolling Down");
        }

        delta = -dy;
        //the list scrolled with displacement of delta
        offsetChildrenVertical(delta);

        updateSecondItem(dy);
        updateMagnetVisibleRowCount();

        if (dy > 0) {
            mDirection = DIRECTION_UP;
            //LogBuilder.build("Direction UP: " + dy);
            layoutViews(DIRECTION_UP,dy, recycler, state);
        } else {
            mDirection = DIRECTION_DOWN;
            //LogBuilder.build("Direction DOWN: " + dy);
            layoutViews(DIRECTION_DOWN,dy, recycler, state);
        }

        return -delta;
    }

    @Override
    public boolean canScrollVertically() {
        return canScroll;
    }

    /**
     * @param i the index of the item on the screen
     * @return the index of the item w.r.t adapter
     */
    private int getAdapterPosition(int i, int firstItem) {
        int pos = firstItem + i;
        if (pos >= getItemCount())
            return -1;
        return firstItem + i;
    }

    /**
     * @return the height of the recyclerview
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }


    @Override
    public void onScrollStateChanged(int state) {
        switch (state){
            case RecyclerView.SCROLL_STATE_IDLE:
                //LogBuilder.build("Scroll IDLE: " + (mDirection == DIRECTION_UP ? " UP" : "Down"));

                if(mSecondItemTop < UP_RANGE && mDirection == DIRECTION_UP){
                        mDeckHeaderCallback.setFlingAction(mSecondItemTop);
                            //mDeckHeaderCallback.setFlingAction(mSecondItemTop);
                        /*}else{
                            mDeckHeaderCallback.setFlingAction(mSecondItemTop - mFirstItemHeight);
                        }*/
                }else if(mSecondItemTop > DOWN_RANGE && mDirection == DIRECTION_DOWN){
                    mDeckHeaderCallback.setFlingAction(mSecondItemTop - mFirstItemHeight);
                }


                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
//                LogBuilder.build("Scroll Dragging");
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
//                LogBuilder.build("Scroll Settling");
                break;
        }
    }

    public void setScrolling(boolean scrolling){
        canScroll = scrolling;
    }
}
