package com.mayo.recyclerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mayo on 18/5/16.
 */
public class MagneticLayoutManager extends RecyclerView.LayoutManager {

    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int mVisibleRowCount;
    private int mFirstItem;
    private int mSecondItem;
    private int mPrevFirstItem;
    private int mFirstItemTop;
    private int mSecondItemTop;
    private int mFirstItemHeight;
    private int mSecondItemHeight;
    private int recyclerViewHeight;

    //directions
    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;
    private boolean isCalledOnce;
    private boolean canChangeOver;//true when second item overlaps the first item completely

    private View v;
    private Context mContext;
    private RelativeLayout r;


    public MagneticLayoutManager(Context context) {
        mContext = context;
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

            detachAndScrapView(scrap, recycler);
        }

        recyclerViewHeight = getVerticalSpace();
        mFirstItemHeight = (int) (recyclerViewHeight * 0.75f);
        mDecoratedChildHeight = 201;
        mSecondItemTop = mFirstItemHeight;
        mSecondItemHeight = mDecoratedChildHeight;

        //Logger.print("DecoratedHeight: " + mDecoratedChildHeight);

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

        int restOfArea = recyclerViewHeight - mSecondItemTop - mSecondItemHeight;
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

        //Logger.print("Rows: " + mVisibleRowCount);
    }

    /**
     * updates the position and height of the second item after scroll
     *
     * @param scrolledBy - amount of displacment of the list
     */
    private void updateSecondItem(int scrolledBy) {
        //reduces the distance from the top. scrolledBy is positive

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
            //Logger.print("Top: " + mFirstItemTop + " " + mSecondItemTop + " " + mSecondItemHeight);

            //detects when the first item is moved down
            if(mSecondItemTop == mFirstItemHeight && mFirstItem > 0){
                //reset the mFirstItem adapter index and its top
                mFirstItem--;
                Logger.print("-----------------Down Transition------------------------");
                mFirstItemTop = 0;
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

        Logger.print("Updated Second Item Top: " + mSecondItemTop + "  Height: " + mSecondItemHeight + " scrolledBy: " + scrolledBy);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void layoutViews(int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //Logger.print("Layout Views");

        int startTopOffset = 0;

        if (getChildCount() > 0) {
            //Logger.print("Removing Views");
            removeAllViews();
        }
        Logger.print("---------------------------------------------------------");
        int adapterPostion;
        int vTop = startTopOffset;

        for (int i = 0; i < mVisibleRowCount; i++) {
            adapterPostion = getAdapterPosition(i, mFirstItem);
            //Logger.print("Adapter Position: " + adapterPostion + " " + i + " FirstItem: " + mFirstItem);

            if (adapterPostion == -1)
                continue;

            v = recycler.getViewForPosition(adapterPostion);
            measureChildWithMargins(v, 0, 0);
            switch (i) {
                case 0:
                    final RelativeLayout r = (RelativeLayout) v.findViewById(R.id.inner_layout);
                    r.getLayoutParams().height = mFirstItemHeight;

                    if (direction == DIRECTION_UP || direction == DIRECTION_NONE) {
                        layoutDecorated(v, 0, 0,
                                mDecoratedChildWidth,
                                mFirstItemHeight);
                    } else if (direction == DIRECTION_DOWN/* && adapterPostion > 0*/) {
                        layoutDecorated(v, 0, 0,
                                mDecoratedChildWidth,
                                mFirstItemHeight);
                    }

                    /*Logger.print(adapterPostion + " FirstItem: " + mFirstItem + " Top: 0" +
                            " Height: " + mFirstItemHeight);*/
                    v.setBackgroundResource(android.R.color.holo_orange_light);
                    break;
                case 1:
                    final RelativeLayout rr = (RelativeLayout) v.findViewById(R.id.inner_layout);
                    rr.getLayoutParams().height = mSecondItemHeight;

                    /*Logger.print(adapterPostion + " FirstItem: " + mFirstItem + " Top: " + mSecondItemTop +
                            " Height: " + mSecondItemHeight);*/

                    if (direction == DIRECTION_UP || direction == DIRECTION_NONE) {
                        layoutDecorated(v, 0, mSecondItemTop,
                                mDecoratedChildWidth,
                                mSecondItemTop + mSecondItemHeight);
                    } else if (direction == DIRECTION_DOWN) {
                        layoutDecorated(v, 0, mSecondItemTop,
                                mDecoratedChildWidth,
                                mSecondItemTop + mSecondItemHeight);
                    }

                    v.setBackgroundResource(android.R.color.holo_red_light);
                    vTop = mSecondItemTop;
                    vTop += mSecondItemHeight;

                    break;
                default:
                    /*Logger.print(adapterPostion + " FirstItem: " + mFirstItem + " Top: " + vTop +
                            " Height: " + mDecoratedChildHeight);*/
                    layoutDecorated(v, 0, vTop,
                            mDecoratedChildWidth,
                            vTop + mDecoratedChildHeight);

                    vTop += mDecoratedChildHeight;

                    break;
            }

            addView(v);

            /*Logger.print("Adapter Pos: "+ adapterPostion +
                    " Top: " + vTop +
                    " Bottom: " + (vTop + mDecoratedChildHeight));*/

        }

        //check if the second item becomes the first
        if (mSecondItemTop == 0) {
            //set the current second as the first
            mFirstItem++;
            mSecondItemTop = mFirstItemHeight;
            mSecondItemHeight = mDecoratedChildHeight;
            Logger.print("----------------------Transition--------------------------");
        }

    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {


        if (getChildCount() == 0)
            return 0;

        int delta;
        boolean bottomBoundReached = false;
        boolean topBoundReached = false;

        Logger.print("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());

        if (mFirstItem == getItemCount() - 2) {
            Logger.print("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());
            bottomBoundReached = true;
        } else if (mFirstItem == 0) {
            v = getChildAt(1);
            int top = getDecoratedTop(v);
            //Logger.print("Top: " + top +" dy: " + dy);

            if (-dy + top > mFirstItemHeight) {
                dy = mFirstItemHeight - top;
                //Logger.print("Changed dy: " + dy);
            }

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

        updateSecondItem(dy);
        updateMagnetVisibleRowCount();
        //updateVisibleFirstItem(dy);
        //updateVisibleRowCount();

        if (dy > 0) {
            layoutViews(DIRECTION_UP, recycler, state);
        } else {
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

    /**
     * @return the adapter position of the item that is visible at the end of the screen
     */
    private int getLastItem() {
        return mFirstItem + mVisibleRowCount;
    }

    @Override
    public void calculateItemDecorationsForChild(View child, Rect outRect) {
        Logger.print("CalculateItemDecor");
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        //Logger.print("onMeasure: " + widthSpec);
    }

    @Override
    public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
        super.setMeasuredDimension(childrenBounds, wSpec, hSpec);
        Logger.print("setMeasuredDimensions");
    }
}
