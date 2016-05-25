package com.mayo.recyclerview.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mayo.recyclerview.Callback;
import com.mayo.recyclerview.Logger;
import com.mayo.recyclerview.R;

/**
 * Created by mayo on 18/5/16.
 */
public class DeckHeaderLayoutManager extends RecyclerView.LayoutManager {


    private int densityOfScreen;//DPPX - 1 DP(Density-Independent Pixel) has pixels
    private int UP_THRESHOLD;
    private int DOWN_THRESHOLD;
    private int HEADER_TOP;
    private int HEADER_VISIBLE_AREA;

    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int mVisibleRowCount;
    private int mFirstItem;
    private int mHeaderTop;
    private int mFirstItemTop;
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
    private Callback mCallback;
    private boolean hasTransition;
    private int vTop;

    public DeckHeaderLayoutManager(Context context, int density) {
        mContext = context;
        mCallback = (Callback) context;

        densityOfScreen = density;
        //UP_THRESHOLD = 100 * density;
        //DOWN_THRESHOLD = 30 * density;
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

            HEADER_VISIBLE_AREA = (30 + 20) * densityOfScreen;
            HEADER_TOP = HEADER_VISIBLE_AREA - mFirstItemHeight;
        }

        mRecyclerViewHeight = getVerticalSpace();
        UP_THRESHOLD = (int) (0.7 * mRecyclerViewHeight);
        DOWN_THRESHOLD = (int) (0.18 * mRecyclerViewHeight);
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

        Logger.print("RH: " + mRecyclerViewHeight +
                " FH: " + mFirstItemHeight +
                " DH: " + mDecoratedChildHeight +
                " Header Top: " + HEADER_TOP +
                " Header Visible Area: " + HEADER_VISIBLE_AREA +
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

        //Logger.print("Default Rows: " + mRowsCanFit + " vTop: " + vTop);
        if (mDirection != DIRECTION_NONE) {

            int count = (mRecyclerViewHeight - vTop) / mDecoratedChildHeight;
//              Logger.print("Before Count: " + count);
            if ((mRecyclerViewHeight - vTop) - (count * mDecoratedChildHeight) > 0) {
                count++;
            }

            //Logger.print("After Count: " + count);
            mVisibleRowCount += count;
        }
        Logger.print("Rows: " + mVisibleRowCount + " canFit: " + mRowsCanFit + " First Item: " + mFirstItem + " vTop: " + vTop);

    }

    private void updateHeaderTop(int scrolledBy) {
        if (scrolledBy > 0) {
            mHeaderTop -= scrolledBy;
            if (mHeaderTop < HEADER_TOP)
                mHeaderTop = HEADER_TOP;

            if (mSecondItemTop <= HEADER_VISIBLE_AREA) {
                //hasTransition = true;
                mFirstItem++;
//                Logger.print("------------------UP Transition------------------------ First Item: " + mFirstItem);
                mFirstItemTop = mSecondItemTop;
                mSecondItemTop = mFirstItemTop + mFirstItemHeight;
            }

            //Logger.print("Header Top: " + mHeaderTop);
        } else {
            //check whether the second item is at the bottom
            if (mSecondItemTop >= mFirstItemHeight + HEADER_VISIBLE_AREA && mFirstItem == 1) {
                mHeaderTop -= scrolledBy;
                Logger.print("If Header Top: " + mHeaderTop + " scrolled: " + scrolledBy);
                if (mHeaderTop > 0) {
                    mHeaderTop = 0;
                }
            }

            if (mFirstItem > 1 && mSecondItemTop >= HEADER_VISIBLE_AREA + mFirstItemHeight) {
//                Logger.print("------------------Down Transition------------------------");
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

//        Logger.print("Updated FirstItemTop: " + mFirstItemTop);
    }

    private void updateSecondItem(int scrolledBy) {
        if (scrolledBy > 0) {
            if (mFirstItemTop == HEADER_VISIBLE_AREA) {//when the header becomes sticky
                mSecondItemTop -= scrolledBy;
                //Logger.print("if Second Top: " + mSecondItemTop);
            } else if (mFirstItemTop < UP_THRESHOLD) {//starts to push the 2nd row
                mSecondItemTop = mFirstItemHeight + mDecoratedChildHeight - (UP_THRESHOLD - mFirstItemTop);
                //Logger.print("else if Second Top: " + mSecondItemTop);
                if (mSecondItemTop < mFirstItemHeight + HEADER_VISIBLE_AREA) {//stick the 2nd row
                    mSecondItemTop = mFirstItemHeight + HEADER_VISIBLE_AREA;
                    //Logger.print("else if if Second Top: " + mSecondItemTop);
                }
            } else {//during the first row being moved up
                mSecondItemTop = mFirstItemHeight + mDecoratedChildHeight;
                //Logger.print("else Second Top: " + mSecondItemTop);
            }
        } else if (scrolledBy < 0) {
            //Logger.print("Before Else Second Item Top: " + mSecondItemTop + " First Bottom: " + (mFirstItemHeight + HEADER_VISIBLE_AREA));
            if (hasTransition) {
                vTop = mSecondItemTop;
                mSecondItemTop = mFirstItemTop - scrolledBy;
            } else {
                mSecondItemTop -= scrolledBy;
                //  Logger.print("After Else Second Item Top: " + mSecondItemTop + " First Bottom: " + (mFirstItemHeight + HEADER_VISIBLE_AREA) + " Offset: " + (mFirstItemHeight + mDecoratedChildHeight));
                if (mSecondItemTop > (HEADER_VISIBLE_AREA + mFirstItemHeight) && mSecondItemTop < (mFirstItemHeight + mDecoratedChildHeight)) {
                    mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                }
            }
            //Logger.print("After Else Second Item Top: " + mSecondItemTop + " First Bottom: " + (mFirstItemHeight + HEADER_VISIBLE_AREA));
            if (mSecondItemTop > mFirstItemHeight + mDecoratedChildHeight) {
                mSecondItemTop = mFirstItemHeight + mDecoratedChildHeight;
                //Logger.print("Else IF Second Item Top: " + mSecondItemTop + " " + (HEADER_VISIBLE_AREA + mFirstItemHeight));
            }
        }

//        Logger.print("UpdateSecondTop - sTop: " + mSecondItemTop + " FirstTop " + mFirstItemTop);
    }

    private int getThirdItem(int scrolledBy) {
        if (!hasTransition)
            vTop = 0;

        if (mDirection == DIRECTION_NONE) {
            vTop = mSecondItemTop + mDecoratedChildHeight;
        } else if (mDirection == DIRECTION_UP) {

            vTop = mSecondItemTop + mDecoratedChildHeight;
            if (vTop < HEADER_VISIBLE_AREA + mFirstItemHeight) {
                //if true, set the top of next item to be after the current visible item
                //Logger.print("Third Top Current Visible");
                vTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
            }

        } else if (mDirection == DIRECTION_DOWN) {
            if (hasTransition) {
//                Logger.print("Before Has Transition vTop: " + vTop);
                vTop -= scrolledBy;
                hasTransition = false;
//                Logger.print("After Has Transition vTop: " + vTop);
            } else if (mSecondItemTop < UP_THRESHOLD) {
                vTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
//                Logger.print("if Third Top Down vTop: " + vTop);
            } else {
                vTop = mSecondItemTop + mDecoratedChildHeight - (UP_THRESHOLD - mSecondItemTop);
//                Logger.print("If Third Top Down vTop: " + vTop);
                if (vTop > mFirstItemHeight + (2 * mDecoratedChildHeight)) {
                    vTop = mFirstItemHeight + (2 * mDecoratedChildHeight) - 15;
//                    Logger.print("Else if Third Top Down vTop: " + vTop);
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

                    Logger.print(i + " Top: " + mHeaderTop + " FirstItem: " + mFirstItem);

                    break;
                case 1:
                    layoutDecorated(v, 0, mFirstItemTop,
                            mDecoratedChildWidth,
                            mFirstItemTop + mFirstItemHeight);
                    Logger.print(adapterPostion + " Top: " + mFirstItemTop);

                    if (mDirection != DIRECTION_NONE)
                        setAnimations(v, mFirstItemTop, i);

                    break;
                case 2:
                    /*Logger.print(adapterPostion + " " + (getItemCount() - 1) +
                            " sTop: " + mSecondItemTop + " " + (HEADER_VISIBLE_AREA + mFirstItemHeight));*/

                    if (adapterPostion == getItemCount() - 1 && mSecondItemTop < HEADER_VISIBLE_AREA + mFirstItemHeight) {
                        mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                        Logger.print("second Top set");
                    }

                    layoutDecorated(v, 0, mSecondItemTop,
                            mDecoratedChildWidth,
                            mSecondItemTop + mFirstItemHeight);
                    vTop = getThirdItem(dy);
                    Logger.print(adapterPostion +
                            " Top: " + mSecondItemTop +
                            " Next Top: " + vTop);
                    if (mDirection != DIRECTION_NONE)
                        setAnimations(v, mSecondItemTop, i);

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
            if (vTop > mRecyclerViewHeight) {
                break;
            }
        }

    }

    private void setAnimations(View v, int top, int pos) {
        int size = 30;
        float alpha = 0.2f;
        boolean canBold = false;

        r = (RelativeLayout) v.findViewById(R.id.inner_layout);
        TextView rewardName = (TextView) r.findViewById(R.id.reward_name);
        LinearLayout ll = (LinearLayout) r.findViewById(R.id.info_layout);
        //ImageView iv = (ImageView) r.findViewById(R.id.store_image);

        switch (top / 100) {
            case 4:
                size = 40;
                alpha = 1.0f;
                canBold = true;
                break;
            case 5:
                size = 38;
                alpha = 0.8f;
                canBold = true;
                break;
            case 6:
                size = 36;
                alpha = 0.6f;
                canBold = true;
                break;
            case 7:
                size = 34;
                alpha = 0.4f;
                break;
            case 8:
                size = 32;
                alpha = 0.2f;
                break;
            case 9:
                size = 30;
                break;
            default:
                if (top >= 1000) {
                    size = 30;
                    alpha = 0.2f;
                } else if (top < 400) {
                    size = 40;
                    alpha = 1.0f;
                    canBold = true;
                }
                break;
        }

        //Logger.print("Top: "+ top + " Size: " + size);
        rewardName.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        if (canBold) {
            rewardName.setTypeface(rewardName.getTypeface(), Typeface.BOLD);
            ll.setAlpha(1.0f);
        }else {
            rewardName.setTypeface(rewardName.getTypeface(), Typeface.NORMAL);
            ll.setAlpha(alpha);
        }
        //Logger.print("Alpha");
    }

    boolean bottomBoundReached = false;
    boolean topBoundReached = false;

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.
            State state) {

        Logger.print("---------------------------------------------------");
        Logger.print("Scrolled By: " + dy);

        if (getChildCount() == 0)
            return 0;

        if (mHeaderTop == 0 && dy < 0)
            return 0;

        int delta;
        bottomBoundReached = false;
        topBoundReached = false;

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
        return firstItem + i - 1;
    }

    /**
     * @return the height of the recyclerview
     */
    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }


    boolean hasFlinged;

    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:
                Logger.print("Scroll IDLE: " + " fTop: " + mFirstItemTop + " HeaderArea: " + HEADER_VISIBLE_AREA +
                        " sTop: " + mSecondItemTop + " UP Range: " + UP_THRESHOLD + " DOWN Range: " + DOWN_THRESHOLD);

                switch (mDirection) {
                    case DIRECTION_UP:

                        if (!bottomBoundReached) {
                            if (mFirstItem == 1 && mFirstItemTop > HEADER_VISIBLE_AREA) {
                                if(mFirstItemTop < UP_THRESHOLD)
                                    mCallback.setFlingAction(mFirstItemTop - HEADER_VISIBLE_AREA);
                                else
                                    mCallback.setFlingAction(mHeaderTop);
                            } else if (mFirstItem >= 1 && mFirstItemTop == HEADER_VISIBLE_AREA) {
                                if(mSecondItemTop < UP_THRESHOLD)
                                    mCallback.setFlingAction(mSecondItemTop - mFirstItemTop);
                                else
                                    mCallback.setFlingAction(mSecondItemTop - (HEADER_VISIBLE_AREA + mFirstItemHeight));
                            }
                        }

                        break;
                    case DIRECTION_DOWN:

                        /*if (mFirstItem == 1 && mHeaderTop < 0) {
                            if(mFirstItemTop > DOWN_THRESHOLD)
                                mCallback.setFlingAction(mFirstItemTop - mFirstItemHeight - 5);
                            else if(mFirstItemTop < DOWN_THRESHOLD && mFirstItemTop != HEADER_VISIBLE_AREA){
                                mCallback.setFlingAction(mFirstItemTop - HEADER_VISIBLE_AREA);
                            }
                        } else if (mFirstItem >= 1 && mFirstItemTop == HEADER_VISIBLE_AREA) {
                            if(mSecondItemTop > DOWN_THRESHOLD)
                                mCallback.setFlingAction(mSecondItemTop - (mFirstItemHeight + HEADER_VISIBLE_AREA));
                            else
                                mCallback.setFlingAction(mSecondItemTop - HEADER_VISIBLE_AREA);
                        }*/

                        if (mFirstItemTop == HEADER_VISIBLE_AREA) {
                            if (mSecondItemTop > DOWN_THRESHOLD)
                                mCallback.setFlingAction(mSecondItemTop - (mFirstItemHeight + HEADER_VISIBLE_AREA));
                            else
                                mCallback.setFlingAction(mSecondItemTop - HEADER_VISIBLE_AREA);
                        }else if(mFirstItemTop != HEADER_VISIBLE_AREA){
                            if(mFirstItemTop < DOWN_THRESHOLD){
                                mCallback.setFlingAction(mFirstItemTop - HEADER_VISIBLE_AREA);
                            }else{
                                mCallback.setFlingAction(mFirstItemTop - mFirstItemHeight);
                            }
                        }
                        break;
                }

                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                Logger.print("Scroll Dragging");
                hasFlinged = false;
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                Logger.print("Scroll Settling");
                break;
        }
    }

    public void setScrolling(boolean scrolling) {
        canScroll = scrolling;
    }


}
