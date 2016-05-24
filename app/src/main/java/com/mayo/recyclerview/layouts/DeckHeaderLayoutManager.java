package com.mayo.recyclerview.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.mayo.recyclerview.Callback;
import com.mayo.recyclerview.Logger;
import com.mayo.recyclerview.R;

/**
 * Created by mayo on 18/5/16.
 */
public class DeckHeaderLayoutManager extends RecyclerView.LayoutManager {

    private int densityOfScreen;//DPPX - 1 DP(Density-Independent Pixel) has pixels
    private int UP_RANGE = 1100;
    private int DOWN_RANGE = 300;
    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int mVisibleRowCount;
    private int mFirstItem;
    private int mHeaderTop;
    private int mFirstItemTop;
    private int mSecondItemTop;
    private int mThirdItemTop;
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
    private Callback mCallback;
    private boolean hasTransition;
    private int vTop;

    public DeckHeaderLayoutManager(Context context, int density) {
        mContext = context;
        mCallback = (Callback) context;

        densityOfScreen = density;
        //UP_RANGE = 100 * density;
        //DOWN_RANGE = 30 * density;
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

        mRecyclerViewHeight = getVerticalSpace();
        mDecoratedChildHeight = (30 + 20 + 10) * densityOfScreen;
        mSecondItemTop = mFirstItemHeight;

        mFirstItem = 1;
        //initialize the tops
        mHeaderTop = 0;
        mFirstItemTop = mFirstItemHeight;
        mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;

        int areaLeft = mRecyclerViewHeight - mFirstItemHeight;
        int otherRows = areaLeft / mDecoratedChildHeight;

        //num of rows when the app is started
        mRowsCanFit = areaLeft / mDecoratedChildHeight/*other children*/ + 1 /*First Item*/;

        if (areaLeft - (otherRows * mDecoratedChildHeight) > 0) {
            mRowsCanFit++;
        }

        Logger.print("Recycler Height: " + mRecyclerViewHeight +
                " FirstItem Height: " + mFirstItemHeight +
                " DecoratedHeight: " + mDecoratedChildHeight +
                " RowsCanFit: " + mRowsCanFit);

        updateMagnetVisibleRowCount();
        detachAndScrapAttachedViews(recycler);

        layoutViews(DIRECTION_NONE, 0, recycler, state);

        if (isCalledOnce) {
            //scrollVerticallyBy(1100, recycler, state);
        }
        isCalledOnce = true;
    }

    /**
     * update the rows that can be shown after magnetic scroll
     */
    private void updateMagnetVisibleRowCount() {
        mVisibleRowCount = mRowsCanFit;
        int total = getItemCount();

        if(mDirection != DIRECTION_NONE){
            int count = (mRecyclerViewHeight - vTop)/mDecoratedChildHeight;

            if((mRecyclerViewHeight - vTop) - (count * mDecoratedChildHeight) > 0)
                count++;

            mVisibleRowCount += count;

        }

        /*if (mSecondItemTop < 165 + mFirstItemHeight && mDirection == DIRECTION_UP)
            mVisibleRowCount++;
        else if (mDirection == DIRECTION_DOWN) {
            mVisibleRowCount++;
            if (mSecondItemTop > DOWN_RANGE && mSecondItemTop == mFirstItemHeight) {
                mVisibleRowCount--;
            }
        }*/

        Logger.print("Rows: " + mVisibleRowCount + " canFit: " + mRowsCanFit + " First Item: " + mFirstItem);

    }

    private void updateHeaderTop(int scrolledBy) {
        if (scrolledBy > 0) {
            mHeaderTop -= scrolledBy;
            if (mHeaderTop < -1120)
                mHeaderTop = -1120;

            if(mSecondItemTop <= 165){
                //hasTransition = true;
                mFirstItem++;
                Logger.print("------------------UP Transition------------------------ First Item: " + mFirstItem);
                mFirstItemTop = mSecondItemTop;
                mSecondItemTop = mFirstItemTop + mFirstItemHeight;
            }

            //Logger.print("Header Top: " + mHeaderTop);
        } else {
            //check whether the second item is at the bottom
            if (mSecondItemTop == mFirstItemHeight + mDecoratedChildHeight) {
                mHeaderTop -= scrolledBy;
                //Logger.print("If Header Top: " + mHeaderTop + " scrolled: " + scrolledBy);
                if (mHeaderTop > 0) {
                    mHeaderTop = 0;
                }
            }

            if(mFirstItem > 1 && mSecondItemTop >= 165 + mFirstItemHeight){
                Logger.print("------------------Down Transition------------------------");
                mFirstItem--;
                hasTransition = true;
            }

            //Logger.print("Header First Item: " + mFirstItem + " Second Top: " + mSecondItemTop + " Transition: " + hasTransition);
        }

        Logger.print("Header Top: " + mHeaderTop + " scrolled: " + scrolledBy +
                " FirstTop: " + mFirstItemTop + " sTop: " + mSecondItemTop + " " + mDirection + " Transition: " + hasTransition);
    }

    private void updateFirstItem(int scrolledBy) {
        if (scrolledBy > 0) {
            mFirstItemTop = mFirstItemHeight + mHeaderTop;
        } else if (scrolledBy < 0) {
            //Logger.print("FirstItem: " + mFirstItem + " SecondTop: " + mSecondItemTop);
            mFirstItemTop = mFirstItemHeight + mHeaderTop;
        }

//        Logger.print("First Item Top: " + mFirstItemTop);
    }

    private void updateSecondItem(int scrolledBy) {
        if (scrolledBy > 0) {

            if (mFirstItemTop == 165) {//when the header becomes sticky
                mSecondItemTop -= scrolledBy;
                Logger.print("if Second Top: " + mSecondItemTop);
            } else if (mFirstItemTop < UP_RANGE) {//starts to push the 2nd row
                mSecondItemTop = mFirstItemHeight + mDecoratedChildHeight - (UP_RANGE - mFirstItemTop);
                Logger.print("else if Second Top: " + mSecondItemTop);
                if (mSecondItemTop < mFirstItemHeight + 165) {//stick the 2nd row
                    mSecondItemTop = mFirstItemHeight + 165;
                    Logger.print("else if if Second Top: " + mSecondItemTop);
                }
            } else {//during the first row being moved up
                mSecondItemTop = mFirstItemHeight + mDecoratedChildHeight;
                Logger.print("else Second Top: " + mSecondItemTop);
            }

        } else if (scrolledBy < 0) {
            Logger.print("Before Else Second Item Top: " + mSecondItemTop + " First Bottom: " + (mFirstItemHeight + 165));
            if(hasTransition){
                mSecondItemTop = mFirstItemTop - scrolledBy;
            }else{
                mSecondItemTop -= scrolledBy;
            }

            Logger.print("After Else Second Item Top: " + mSecondItemTop + " First Bottom: " + (mFirstItemHeight + 165));
            if(mSecondItemTop > mFirstItemHeight + mDecoratedChildHeight) {
                mSecondItemTop = mFirstItemHeight + mDecoratedChildHeight ;
                Logger.print("Else IF Second Item Top: " + mSecondItemTop + " " + (165 + mFirstItemHeight));
            }
        }

        Logger.print("UpdateSecondTop - sTop: " + mSecondItemTop  + " FirstTop " + mFirstItemTop);
    }

    private int getThirdItem(int scrolledBy) {
        vTop = 0;

        if (mDirection == DIRECTION_NONE) {
            vTop = mSecondItemTop + mDecoratedChildHeight;
        } else if (mDirection == DIRECTION_UP) {

            vTop = mSecondItemTop + mDecoratedChildHeight;

            if (vTop < 165 + mFirstItemHeight) {
                //if true, set the top of next item to be after the current visible item
                //Logger.print("Third Top Current Visible");
                vTop = 165 + mFirstItemHeight;
            }

        } else if (mDirection == DIRECTION_DOWN) {
            if(hasTransition){
                vTop = /*165 + mFirstItemHeight + scrolledBy*/mSecondItemTop + mFirstItemHeight;
                hasTransition = false;
            }else if(mSecondItemTop < UP_RANGE + 100){
                vTop = 165 + mFirstItemHeight;
//                Logger.print("if Third Top Down vTop: " + vTop);
            }else{
                vTop = mSecondItemTop + mDecoratedChildHeight - (UP_RANGE - mSecondItemTop);
                //Logger.print("If Third Top Down vTop: " + vTop);
                if(vTop > mFirstItemHeight + (2 * mDecoratedChildHeight)){
                    vTop = mFirstItemHeight + (2 * mDecoratedChildHeight);
                    //Logger.print("Else if Third Top Down vTop: " + vTop);
                }


            }
        }

        return vTop;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void layoutViews(int direction, int dy, RecyclerView.
            Recycler recycler, RecyclerView.State state) {
//        Logger.print("Layout Views");

        int startTopOffset = 0;

        //Logger.print("\n------------------------------------------------------------");
        if (getChildCount() > 0) {
            v = getChildAt(0);
            //startTopOffset = getDecoratedTop(v);
            if (direction == DIRECTION_UP) {
                //startTopOffset += dy;
            }
            removeAllViews();
        }

        int adapterPostion;
        int vTop = startTopOffset;

//        Logger.print("FirstItem: " + mFirstItem);
        for (int i = 0; i < mVisibleRowCount; i++) {
            adapterPostion = getAdapterPosition(i, mFirstItem);
            //Logger.print("Adapter Position: " + adapterPostion + " " + i + " FirstItem: " + mFirstItem);

            if (adapterPostion == -1 || adapterPostion >= getItemCount())
                continue;
            else if (i == 0) {
                v = recycler.getViewForPosition(0);
            } else {
                v = recycler.getViewForPosition(adapterPostion);
            }

            r = (RelativeLayout) v.findViewById(R.id.inner_layout);
            measureChildWithMargins(v, 0, 0);

            switch (i) {
                case 0:
                    layoutDecorated(v, 0, mHeaderTop,
                            mDecoratedChildWidth,
                            mFirstItemHeight);

                    //Logger.print(i + " Top: " + mHeaderTop + " FirstItem: " + mFirstItem);

                    //vTop += mHeaderTop;

                    break;
                case 1:
                    layoutDecorated(v, 0, mFirstItemTop,
                            mDecoratedChildWidth,
                            mFirstItemTop + mFirstItemHeight);

                    //Logger.print(adapterPostion + " Top: " + mFirstItemTop);

                    break;
                case 2:

                    layoutDecorated(v, 0, mSecondItemTop,
                            mDecoratedChildWidth,
                            mSecondItemTop + mFirstItemHeight);

                    vTop = getThirdItem(dy);

                    Logger.print(adapterPostion +
                            " Top: " + mSecondItemTop +
                            " Next Top: " + vTop);

                    break;
                default:


                    layoutDecorated(v, 0, vTop,
                            mDecoratedChildWidth,
                            vTop + mFirstItemHeight);

                    Logger.print(adapterPostion + " Top: " + vTop + " Next Top: " + (vTop + mDecoratedChildHeight));
                    vTop += mDecoratedChildHeight;

                    break;
            }

            addView(v);
        }

    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.
            State state) {

        /*if(dy < 0){
            return 0;
        }*/
        Logger.print("---------------------------------------------------");
        Logger.print("Scrolled By: " + dy);


        if (getChildCount() == 0)
            return 0;

        int delta;
        boolean bottomBoundReached = false;
        boolean topBoundReached = false;

        Logger.print("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());

        if (mFirstItem == getItemCount() - 2) {
            //Logger.print("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());
            bottomBoundReached = true;
        } else if (mFirstItem == 0) {
            v = getChildAt(1);
            int top = getDecoratedTop(v);
            //Logger.print("Top: " + top +" dy: " + dy);

            //Logger.print("Changed dy: " + dy + " Top: " + top + " " + mFirstItemHeight);
            if (top == mFirstItemHeight) {
                topBoundReached = true;
            }
        }

        if (dy > 0) {
            if (bottomBoundReached) {
                Logger.print("BottomBound Reached!");
                return 0;
            }
            //Logger.print("Scrolling Up");
        } else {
            if (topBoundReached) {
                Logger.print("TopBound Reached!");
                return 0;
            }
            //Logger.print("Scrolling Down");
        }

        delta = -dy;
        //the list scrolled with displacement of delta
        offsetChildrenVertical(delta);

        updateHeaderTop(dy);
        updateFirstItem(dy);
        updateSecondItem(dy);
        updateMagnetVisibleRowCount();

        if (dy > 0) {
            mDirection = DIRECTION_UP;
            //Logger.print("Direction UP: " + dy);
            layoutViews(DIRECTION_UP, dy, recycler, state);
        } else {
            mDirection = DIRECTION_DOWN;
            //Logger.print("Direction DOWN: " + dy);
            layoutViews(DIRECTION_DOWN, dy, recycler, state);
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
        /*int pos = firstItem + i;
        if (pos >= getItemCount())
            return -1;
        return firstItem + i;*/
        return firstItem + i - 1;
    }

    /**
     * @return the height of the recyclerview
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }


    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
                //Logger.print("Scroll IDLE: " + (mDirection == DIRECTION_UP ? " UP" : "Down"));

                if (mSecondItemTop < UP_RANGE && mDirection == DIRECTION_UP) {
                    //mCallback.setFlingAction(mSecondItemTop - (mFirstItemHeight+mHeaderTop));
                } else if (mSecondItemTop > DOWN_RANGE && mDirection == DIRECTION_DOWN) {
                    //mCallback.setFlingAction(mSecondItemTop - mFirstItemHeight);
                }

                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
//                Logger.print("Scroll Dragging");
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
//                Logger.print("Scroll Settling");
                break;
        }
    }

    public void setScrolling(boolean scrolling) {
        canScroll = scrolling;
    }
}
