package com.mayo.recyclerview.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.mayo.recyclerview.DeckHeaderCallback;
import com.mayo.recyclerview.Gazapp;
import com.mayo.recyclerview.LogBuilder;
import com.mayo.recyclerview.R;

import java.util.Map;

/**
 * Created by mayo on 18/5/16.
 */
public class MagneticLayoutManager extends RecyclerView.LayoutManager {

    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int mVisibleRowCount;
    private int mFirstItem;
    private int mSecondItemTop;
    private int mFirstItemHeight;
    private int mSecondItemHeight;
    private int mRecyclerViewHeight;

    //directions
    private int mDirection;
    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;

    private boolean isCalledOnce;
    private int numOfPasses = 0;

    private View v;
    private RelativeLayout r;

    private Map<Integer,Integer> mHeights;
    private Context mContext;
    private DeckHeaderCallback mDeckHeaderCallback;

    public MagneticLayoutManager(Context context) {
        mContext = context;
        mDeckHeaderCallback = (DeckHeaderCallback) context;
        mHeights = Gazapp.getGazapp().viewHeights;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        numOfPasses++;


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

        mRecyclerViewHeight = getVerticalSpace();
        mFirstItemHeight = (int) (mRecyclerViewHeight * 0.8f);
        mDecoratedChildHeight = (int) (mRecyclerViewHeight * 0.1f);
        mSecondItemTop = mFirstItemHeight;
        mSecondItemHeight = mDecoratedChildHeight;

        //LogBuilder.build("Gazapp Height: " + mRecyclerViewHeight + " FirstItem Height: " + mFirstItemHeight + " DecoratedHeight: " + mDecoratedChildHeight);

        //updateVisibleRowCount();
        updateMagnetVisibleRowCount();
        detachAndScrapAttachedViews(recycler);

        layoutViews(DIRECTION_NONE, recycler, state);

        if (isCalledOnce) {
            //scrollVerticallyBy(100, recycler, state);
        }
        isCalledOnce = true;
    }

    /**
     * update the rows that can be shown after magnetic scroll
     */
    private void updateMagnetVisibleRowCount() {
        mVisibleRowCount = 1;//first Item always visible

        int restOfArea = mRecyclerViewHeight - mSecondItemTop - mSecondItemHeight;
        mVisibleRowCount++;//second item also visible

        if (restOfArea < mDecoratedChildHeight) {
            mVisibleRowCount++;
        } else {
            int remainingRows = restOfArea / mDecoratedChildHeight/*the rest of items have same height*/;
            mVisibleRowCount += remainingRows;

            int diff = restOfArea - remainingRows * mDecoratedChildHeight;
            if (diff > 0)
                mVisibleRowCount++;//some space left. a row can be accommodated
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

//        LogBuilder.build("---------------------------------------------------------");
        if (scrolledBy > 0) {
            if (mSecondItemHeight + scrolledBy < mFirstItemHeight) {
                mSecondItemHeight += scrolledBy;
            } else {
                mSecondItemHeight = mFirstItemHeight;
            }

            //mSecondItemTop = mFirstItemHeight - scrolledBy;
            mSecondItemTop -= scrolledBy;
            if (mSecondItemTop < 0)
                mSecondItemTop = 0;

        } else if (scrolledBy < 0) {
            //LogBuilder.build("Top: " + mFirstItemTop + " " + mSecondItemTop + " " + mSecondItemHeight);

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
                if(mSecondItemTop < mDecoratedChildHeight)
                    mSecondItemHeight = mFirstItemHeight - mSecondItemTop;
                else {
                    //pushes the last item down slowly
                    int diff = mSecondItemTop - mDecoratedChildHeight;
                    diff = diff > mDecoratedChildHeight ? mDecoratedChildHeight : diff;

                    mSecondItemHeight = mFirstItemHeight + diff - mSecondItemTop;
                }
            } else {
                mSecondItemTop = mFirstItemHeight;
                mSecondItemHeight = mDecoratedChildHeight;
            }
        }

        //LogBuilder.build("Updated Second Item Top: " + mSecondItemTop + "  Height: " + mSecondItemHeight + " scrolledBy: " + scrolledBy);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void layoutViews(int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
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
                    //The first 2 passes are general. The next pass onwards the height changes
                    if(numOfPasses < 2)
                        r.getLayoutParams().height = mFirstItemHeight;
                    else
                        r.getLayoutParams().height = mHeights.get(adapterPostion);

                    //v.animate().setDuration(1000).alpha(0.0f).start();

                    layoutDecorated(v, 0, 0,
                            mDecoratedChildWidth,
                            mFirstItemHeight);

                    mHeights.put(adapterPostion,mFirstItemHeight);

                    /*LogBuilder.build(adapterPostion +
                            " FirstItem: " + mFirstItem +
                            " Top: 0" +
                            " Height: " + mFirstItemHeight + " " + mHeights.get(adapterPostion));*/

                    break;
                case 1:
                    if(numOfPasses < 2)
                        r.getLayoutParams().height = mSecondItemHeight;
                    else
                        r.getLayoutParams().height = mHeights.get(adapterPostion);


                    r.getLayoutParams().height = mSecondItemHeight;

                    layoutDecorated(v, 0, mSecondItemTop,
                            mDecoratedChildWidth,
                            mSecondItemTop + mSecondItemHeight);

                    LogBuilder.build(adapterPostion +
                            " FirstItem: " + mFirstItem +
                            " Top: " + mSecondItemTop +
                            " Height: " + mSecondItemHeight + " " + mHeights.get(adapterPostion));

                    mHeights.put(adapterPostion,mSecondItemHeight);

                    //v.setBackgroundResource(android.R.color.holo_red_light);
                    vTop = mSecondItemTop;
                    vTop += mSecondItemHeight;

                    break;
                default:

                    int h;
                    if(adapterPostion == getItemCount() - 1 && mVisibleRowCount == 3){
                        h = getVerticalSpace() - (mSecondItemTop + mSecondItemHeight);
//                        LogBuilder.build(adapterPostion + " FirstItem: " + mFirstItem + " Top: " + vTop + " Height: " + h + " Found Last");
                    }else{
                        h = mDecoratedChildHeight;
//                        LogBuilder.build(adapterPostion + " FirstItem: " + mFirstItem + " Top: " + vTop + " Height: " + h);
                    }

                    if(numOfPasses < 2 || mHeights.get(adapterPostion) == null)
                        r.getLayoutParams().height = h;
                    else
                        r.getLayoutParams().height = mHeights.get(adapterPostion);

                    layoutDecorated(v, 0, vTop,
                            mDecoratedChildWidth,
                            vTop + h);

                    mHeights.put(adapterPostion,h);

                    vTop += h;

                    break;
            }

            addView(v);
            /*LogBuilder.build("Adapter Pos: "+ adapterPostion +
                    " Top: " + vTop +
                    " Bottom: " + (vTop + mDecoratedChildHeight));*/

        }

        //check if the second item becomes the first
        if (mSecondItemTop == 0) {
            //set the current second as the first
            mFirstItem++;
            mSecondItemTop = mFirstItemHeight;
            mSecondItemHeight = mDecoratedChildHeight;
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
            layoutViews(DIRECTION_UP, recycler, state);
        } else {
            mDirection = DIRECTION_DOWN;
            //LogBuilder.build("Direction DOWN: " + dy);
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
//                LogBuilder.build("Scroll IDLE: " + (mDirection == DIRECTION_UP ? " UP" : "Down"));

                if(mSecondItemHeight > mDecoratedChildHeight && mSecondItemHeight <= mFirstItemHeight){
                    if(mDirection == DIRECTION_UP) {
                        int extendedHeight = mSecondItemHeight - mDecoratedChildHeight;
                        if (extendedHeight > 100){
                            mDeckHeaderCallback.setFlingAction(mSecondItemTop);
                        }else{
                            mDeckHeaderCallback.setFlingAction(mSecondItemTop - mFirstItemHeight);
                        }
                    }else if(mDirection == DIRECTION_DOWN){
                        int contractedHeight = mFirstItemHeight - mSecondItemHeight;
                        if (contractedHeight > 100){

                            mDeckHeaderCallback.setFlingAction(mSecondItemTop - mFirstItemHeight);
                        }else{
                            mDeckHeaderCallback.setFlingAction(mSecondItemTop);
                        }
                    }
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

}
