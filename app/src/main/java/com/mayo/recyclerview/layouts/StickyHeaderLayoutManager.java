package com.mayo.recyclerview.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mayo.recyclerview.DeckHeaderCallback;
import com.mayo.recyclerview.LogBuilder;
import com.mayo.recyclerview.R;

/**
 * Created by mayo on 18/6/16.
 */
public class StickyHeaderLayoutManager extends RecyclerView.LayoutManager {


    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;
    boolean bottomBoundReached = false;
    boolean topBoundReached = false;
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
    private int mHeaderHeight;
    private int mRecyclerViewHeight;
    private int mRowsCanFit;
    //directions
    private int mDirection;
    private View v;
    private RelativeLayout r;
    private DeckHeaderCallback mDeckHeaderCallback;
    private boolean hasTransition;
    private int vTop;
    private int incrementedBy = 0;
    private boolean justStarted;
    private boolean canScrollToBorders;
    private int mPrevFirstItem;
    private boolean canAnimate;
    private RecyclerView.Recycler mRecyler;

    public StickyHeaderLayoutManager(Context context) {
        mDeckHeaderCallback = (DeckHeaderCallback) context;
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

        mRecyclerViewHeight = getVerticalSpace();
        mRecyler = recycler;

        if (getChildCount() == 0) {
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            //assume that the size of each child view is same
            //calculate the decorated values upfront
            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap);
            LogBuilder.build("Header Height: " + mDecoratedChildHeight);

            mHeaderHeight = mDecoratedChildHeight;

            r = (RelativeLayout) scrap.findViewById(R.id.inner_layout);
            LinearLayout l = (LinearLayout) r.findViewById(R.id.store_details_layout);
            TextView t = (TextView) l.findViewById(R.id.spot_rewards);

            LogBuilder.build("Height: " + t.getMeasuredHeight());

            HEADER_VISIBLE_AREA = t.getMeasuredHeight();

            detachAndScrapView(scrap, recycler);

            scrap = recycler.getViewForPosition(1);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            LogBuilder.build("First Item Height: " + getDecoratedMeasuredHeight(scrap));

            //assume that the size of each child view is same
            //calculate the decorated values upfront
            mFirstItemHeight = getDecoratedMeasuredHeight(scrap);
//            mFirstItemHeight = mHeaderHeight - HEADER_VISIBLE_AREA + 10;
            LogBuilder.build("Item Height: " + mFirstItemHeight);

            r = (RelativeLayout) scrap.findViewById(R.id.inner_layout);
            t = (TextView) r.findViewById(R.id.reward_name);
            t.measure(0, 0);

//            LogBuilder.build("Height: " + t.getMeasuredHeight());
            mDecoratedChildHeight = t.getMeasuredHeight();

            HEADER_TOP = HEADER_VISIBLE_AREA - mHeaderHeight;

            UP_THRESHOLD = (int) (0.7 * mRecyclerViewHeight);
            DOWN_THRESHOLD = (int) (0.18 * mRecyclerViewHeight);

            mSecondItemTop = mFirstItemHeight;

            mFirstItem = 1;
            mPrevFirstItem = 1;
            //initialize the tops
            mHeaderTop = 0;
            mFirstItemTop = mHeaderHeight;
            mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;

            int areaLeft = mRecyclerViewHeight - mHeaderHeight;
            int otherRows = areaLeft / mDecoratedChildHeight;

            //num of rows when the app is started
            mRowsCanFit = areaLeft / mDecoratedChildHeight + 1;

            if (areaLeft - (otherRows * mDecoratedChildHeight) > 0) {
                mRowsCanFit++;
            }

            updateMagnetVisibleRowCount();
        }

//        LogBuilder.build("RH: " + mRecyclerViewHeight +
//                " FH: " + mFirstItemHeight +
//                " DH: " + mDecoratedChildHeight +
//                " Header Top: " + HEADER_TOP +
//                " Header Visible Area: " + HEADER_VISIBLE_AREA +
//                " RowsCanFit: " + mRowsCanFit);

        detachAndScrapAttachedViews(recycler);
        layoutViews(0, recycler);
    }

    /**
     * update the rows that can be shown after magnetic scroll
     */
    private void updateMagnetVisibleRowCount() {
        mVisibleRowCount = mRowsCanFit;
//        if (!Gazapp.getGazapp().hasExpanded) {
        for (int t = vTop; t < mRecyclerViewHeight; t += mDecoratedChildHeight) {
            mVisibleRowCount++;
        }
//        }
//        LogBuilder.build("Rows: " + mVisibleRowCount);
    }

    private void updateHeaderTop(int scrolledBy) {
        if (scrolledBy > 0) {
            mHeaderTop -= scrolledBy;
            if (mHeaderTop < HEADER_TOP) {
                mHeaderTop = HEADER_TOP;

            }
        } else {
            if (mSecondItemTop >= mFirstItemHeight + HEADER_VISIBLE_AREA && mFirstItem == 1) {//check whether the second item is at the bottom
                mHeaderTop -= scrolledBy;
                if (mHeaderTop > 0) {
                    mHeaderTop = 0;
                }
            }

            if (mFirstItem > 1 && mSecondItemTop >= HEADER_VISIBLE_AREA + mFirstItemHeight) {
//                LogBuilder.build("------------------Down Transition------------------------");
                mPrevFirstItem = mFirstItem;
                mFirstItem--;
                hasTransition = true;
            }
        }

        mDeckHeaderCallback.setStickyHeader(mHeaderTop == HEADER_TOP, mFirstItem);

//        LogBuilder.build("Header Top: " + mHeaderTop + " scrolled: " + scrolledBy +
//                " FirstTop: " + mFirstItemTop + " sTop: " + mSecondItemTop + " " + mDirection + " Transition: " + hasTransition);
    }

    /*private void setElasticEffect(int scrolledBy) {
        if (incrementedBy == 0) {
            v = getChildAt(0);
            final RelativeLayout mLayoutLinear = (RelativeLayout) v.findViewById(R.id.inner_layout);
            ImageView iv = (ImageView) mLayoutLinear.findViewById(R.id.store_image);

            Gazapp.getGazapp().imageDimension = iv.getHeight();
            mHeaderImageInitialHeight = iv.getHeight();
//            LogBuilder.build("Initialize: " + iv.getHeight());
            incrementedBy = -scrolledBy;
        }

        if (scrolledBy < 0) {
            if (Gazapp.getGazapp().imageDimension >= mHeaderImageInitialHeight + 300) {
                Gazapp.getGazapp().imageDimension = mHeaderImageInitialHeight + 300;
            } else {
                incrementedBy -= scrolledBy;
                Gazapp.getGazapp().imageDimension -= scrolledBy;
            }
//            LogBuilder.build("Up Computed Dimension: " + Gazapp.getGazapp().imageDimension);
            Gazapp.getGazapp().hasExpanded = true;

        } else {

            if (Gazapp.getGazapp().imageDimension <= mHeaderImageInitialHeight) {
                Gazapp.getGazapp().imageDimension = mHeaderImageInitialHeight;
                Gazapp.getGazapp().hasExpanded = false;
            } else {
                incrementedBy -= scrolledBy;
                if (Gazapp.getGazapp().imageDimension - scrolledBy < mHeaderImageInitialHeight)
                    Gazapp.getGazapp().imageDimension = mHeaderImageInitialHeight;
                else
                    Gazapp.getGazapp().imageDimension -= scrolledBy;
            }

//            LogBuilder.build("Down Computed Dimension: " + Gazapp.getGazapp().imageDimension + " " + incrementedBy);
        }
    }*/

    private void updateFirstItem(int scrolledBy) {
        if (scrolledBy > 0) {

            if ((mHeaderHeight + mHeaderTop) <= HEADER_VISIBLE_AREA) {
                mFirstItemTop = HEADER_VISIBLE_AREA;
            } else {
                mFirstItemTop = mHeaderHeight + mHeaderTop;
            }
        } else if (scrolledBy < 0) {
            //LogBuilder.build("FirstItem: " + mFirstItem + " SecondTop: " + mSecondItemTop);
            mFirstItemTop = mHeaderHeight + mHeaderTop;
        }

//        LogBuilder.build("Updated FirstItemTop: " + mFirstItemTop);
    }

    private void updateSecondItem(int scrolledBy) {
        if (scrolledBy > 0) {
//            LogBuilder.build("Second Up------------------");
            if (mFirstItemTop == HEADER_VISIBLE_AREA) {//when the r_lengthy_header becomes sticky
                if ((mSecondItemTop - scrolledBy) <= HEADER_VISIBLE_AREA) {
                    mPrevFirstItem = mFirstItem;
                    mFirstItem++;
                    if (mFirstItem < getItemCount() - 2) {
                        mFirstItemTop = mSecondItemTop;
                    }
                    mSecondItemTop = mFirstItemTop + mFirstItemHeight;
//                    LogBuilder.build("Second Up Transition FirstItem: " + mFirstItem + " Prev: " + (mFirstItem - 1) + " " + mFirstItemTop);
                    mDeckHeaderCallback.setStickyHeader(true, mFirstItem);
                } else {
                    mSecondItemTop -= scrolledBy;
                }
//                LogBuilder.build("if Second Top: " + mSecondItemTop);
            } else {
                if ((mSecondItemTop - scrolledBy) <= (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                    mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
//                    LogBuilder.build("else if Second Top: " + mSecondItemTop);
                } else {
                    mSecondItemTop -= scrolledBy;
//                    LogBuilder.build("else if else Second Top: " + mSecondItemTop);
                }

                if (mFirstItem == 1 && mFirstItemTop == mHeaderHeight) {
                    mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;
//                    LogBuilder.build("else if else if Second Top: " + mSecondItemTop);
                }
            }

        } else if (scrolledBy < 0) {
//            LogBuilder.build("Second Down-------------------------------");
//            LogBuilder.build("Before Else Second Item Top: " + mSecondItemTop + " FirstTop: " + mFirstItemTop);
            if (hasTransition) {
                vTop = mSecondItemTop;
                mSecondItemTop = mFirstItemTop - scrolledBy;
//                LogBuilder.build(" Transition: " + mSecondItemTop);
            } else {
                if (mFirstItem == 1) {
                    mSecondItemTop -= scrolledBy;

//                    LogBuilder.build("FirstItemTop: " + mFirstItemTop + " " + mSecondItemTop +
//                            " " + (mSecondItemTop - mFirstItemTop) + " " + mDecoratedChildHeight);

                    if (mFirstItemTop == HEADER_VISIBLE_AREA) {
                        if (mSecondItemTop >= (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                            mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                        }
                    } else if (mFirstItemTop > HEADER_VISIBLE_AREA) {

                        if ((mSecondItemTop - mFirstItemTop) <= mDecoratedChildHeight) {
                            mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;
//                            LogBuilder.build("If : " + mSecondItemTop);
                        } else {
                            mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                            if ((mSecondItemTop - mFirstItemTop) < mDecoratedChildHeight) {
                                mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;
                            }
//                            LogBuilder.build("Else : " + mSecondItemTop);
                        }
                        /*if ((mSecondItemTop - mFirstItemTop) > mDecoratedChildHeight) {
                            mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                            LogBuilder.build("If : " + mSecondItemTop);
                        }else {
                            mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;
                            LogBuilder.build("Else : "  + mSecondItemTop);
                        }*/
                    }
                } else {
                    mSecondItemTop -= scrolledBy;
//                    LogBuilder.build("else if else SecondTop: " + mSecondItemTop);
                    if (mSecondItemTop > (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                        mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
//                        LogBuilder.build("else if else if SecondTop: " + mSecondItemTop);
                    }
                }
            }
        }

//        LogBuilder.build("UpdateSecondTop - sTop: " + mSecondItemTop + " FirstTop " + mFirstItemTop);
    }

    private int getThirdItem(int scrolledBy) {
        if (!hasTransition)
            vTop = 0;

        if (mDirection == DIRECTION_NONE) {
            vTop = mSecondItemTop + mDecoratedChildHeight;
        } else if (mDirection == DIRECTION_UP) {

            vTop = mSecondItemTop + mDecoratedChildHeight;
//            LogBuilder.build("Third Top Current Visible: " + vTop + " " + mDecoratedChildHeight);
            if (vTop < HEADER_VISIBLE_AREA + mFirstItemHeight) {
                //if true, set the top of next item to be after the current visible item
                vTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
//                LogBuilder.build("If Third Top Current Visible: " + vTop);
            }

        } else if (mDirection == DIRECTION_DOWN) {
            if (hasTransition) {
//                LogBuilder.build("Before Has Transition vTop: " + vTop);
                vTop -= scrolledBy;
                hasTransition = false;
//                LogBuilder.build("After Has Transition vTop: " + vTop);
            } else {
                //if (mSecondItemTop < UP_THRESHOLD) {
                if (((HEADER_VISIBLE_AREA + mFirstItemHeight) - mSecondItemTop) > mDecoratedChildHeight) {
                    vTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
//                LogBuilder.build("if Third Top Down vTop: " + vTop + " sTop: " + mSecondItemTop);
                } else {

                    if (mFirstItem == 1 && mFirstItemTop > UP_THRESHOLD) {
                        vTop = mSecondItemTop + mDecoratedChildHeight;
                    } else {
                        vTop = /*HEADER_VISIBLE_AREA + mFirstItemHeight*/mSecondItemTop + mDecoratedChildHeight;
                    }
                }
            }
        }

//        LogBuilder.build("updated vTop: " + vTop);
        return vTop;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void layoutViews(int dy, RecyclerView.
            Recycler recycler) {

//        LogBuilder.build("\n------------------------------------------------------------");
        SparseArray<View> viewCache = new SparseArray<>(getChildCount());

        if (getChildCount() > 0) {

//            LogBuilder.build("Prev FirstItem: " + mPrevFirstItem + " Children: " + getItemCount() + " Adapter Children: " + getChildCount());
            //cache all views by their exisiting position, before updating counts
            for (int i = 0; i < getChildCount(); i++) {

                if (i == 0) {
//                    LogBuilder.build("Adapter Position: " + 0);
                    viewCache.put(0, getChildAt(0));
                } else {
//                    LogBuilder.build("Adapter Position: " + getAdapterPosition(i, mPrevFirstItem));
                    viewCache.put(getAdapterPosition(i, mPrevFirstItem), getChildAt(i));
                }

            }

//            LogBuilder.build("Cached Views Count: " + viewCache.size());
            //detach all views
            for (int i = 0; i <= viewCache.size(); i++) {

                if (i == 0) {
//                    LogBuilder.build("Detaching Position: " + 0);
                    detachView(viewCache.get(0));
                } else {
//                    LogBuilder.build("Detaching Position: " + getAdapterPosition(i, mPrevFirstItem));
                    detachView(viewCache.get(getAdapterPosition(i, mPrevFirstItem)));
                }

            }

            //removeAllViews();
            mPrevFirstItem = mFirstItem;
        }

        int adapterPostion;
        int vTop = 0;
        boolean created;

        for (int i = 0; i < mVisibleRowCount; i++) {

            //set header
            if (i == 0) {
                v = viewCache.get(0);
                if (v == null) {
                    v = recycler.getViewForPosition(0);
                    addView(v);
//                    LogBuilder.build("Created New View: " + 0);
                } else {
//                    LogBuilder.build("Removing ViewCache: " + 0);
                    attachView(v);
                    viewCache.remove(0);
                }

                layoutDecorated(v, 0, mHeaderTop,
                        mDecoratedChildWidth,
                        mHeaderHeight + incrementedBy);
//                LogBuilder.build(i + " Height: " + " Top: " + mHeaderTop + " mFirstTop: " + mFirstItemTop);

                continue;
            }

            adapterPostion = getAdapterPosition(i, mFirstItem);
//            LogBuilder.build("Adapter Position: " + adapterPostion + " i: " + i + " FirstItem: " + mFirstItem);

            created = false;

            if (adapterPostion == -1 || adapterPostion >= getItemCount())
                continue;
            else {
                v = viewCache.get(adapterPostion);

                if (v == null) {
                    v = recycler.getViewForPosition(adapterPostion);
                    created = true;
//                    LogBuilder.build("Created New View: " + adapterPostion);
                }
            }

            measureChildWithMargins(v, 0, 0);

            boolean hasAnimCondMet = mDirection != DIRECTION_NONE && getItemCount() > 2;

            switch (i) {

                case 1:
                    /*if (Gazapp.getGazapp().hasExpanded) {
                        mFirstItemTop = mHeaderHeight + incrementedBy;
                    }*/

                    layoutDecorated(v, 0, mFirstItemTop,
                            mDecoratedChildWidth,
                            mFirstItemTop + mFirstItemHeight);

//                    LogBuilder.build(i + " Top: " + mFirstItemTop + " Height: " + mFirstItemHeight);

                    if (hasAnimCondMet && canAnimate) {
                        mDeckHeaderCallback.animateView(v, mFirstItemTop);
                    }

                    break;
                case 2:

                    if (adapterPostion == getItemCount() - 1 && mSecondItemTop < HEADER_VISIBLE_AREA + mFirstItemHeight) {
                        mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
//                        LogBuilder.build("second Top set");
                    }

                    /*if (Gazapp.getGazapp().hasExpanded)
                        mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;*/

                    layoutDecorated(v, 0, mSecondItemTop,
                            mDecoratedChildWidth,
                            mSecondItemTop + mFirstItemHeight);

                    vTop = getThirdItem(dy);

//                    LogBuilder.build(i + " Top: " + mSecondItemTop);

                    /*if (mDirection != DIRECTION_NONE)
                        setAnimations(v, mSecondItemTop);*/

                    if (hasAnimCondMet && canAnimate) {
                        mDeckHeaderCallback.animateView(v, mSecondItemTop);
                    }

                    /*if (Gazapp.getGazapp().hasExpanded)
                        vTop = mSecondItemTop + mDecoratedChildHeight;*/

                    break;
                default:

                    layoutDecorated(v, 0, vTop,
                            mDecoratedChildWidth,
                            vTop + mFirstItemHeight);

     /*               LogBuilder.build(adapterPostion + " Top: " + vTop +
                            " Next Top: " + (vTop + mDecoratedChildHeight) +
                            " Height: " + mFirstItemHeight);*/

                    if (hasAnimCondMet && canAnimate) {
                        mDeckHeaderCallback.animateView(v, vTop);
                    }

                    vTop += mDecoratedChildHeight;
                    break;
            }


            if (created) {
                addView(v);
            } else {
//                LogBuilder.build("Removing ViewCache: " + adapterPostion + " index: " + i);
                attachView(v);
                viewCache.remove(adapterPostion);
            }

            //no need to display children whose top is beyond the recyclerView's viewport
            if (vTop > mRecyclerViewHeight) {
                break;
            }
        }

//        LogBuilder.build("Remaining views: " + viewCache.size());
        for (int i = 0; i < viewCache.size(); i++) {
            final View removingView = viewCache.valueAt(i);
            recycler.recycleView(removingView);
        }

        mDeckHeaderCallback.setHeader(mFirstItem);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.
            State state) {

//        LogBuilder.build("---------------------------------------------------");
//        LogBuilder.build("Scrolled By: " + dy + " " + mHeaderTop);

        if (getChildCount() == 0)
            return 0;

/*        if (Gazapp.getGazapp().currentState == State.ZAPP_IN)
            return 0;*/

        /*if(!Gazapp.getGazapp().hasExpanded && dy > 0 && getItemCount() == 2){
            return 0;
        }
        */

        //condition 1 : prevents scrolling when there are only header and the blank
        //condition 2 : prevents scrolling down when the header is completely visible
        if ((getItemCount() == 2) || (mHeaderTop == 0 && dy < 0)) {
            return 0;
        }

        int delta;
        bottomBoundReached = false;
        topBoundReached = false;

//        LogBuilder.build("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());

        if (getItemCount() > 3 && mFirstItem == getItemCount() - 2) {
//            LogBuilder.build("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());
            bottomBoundReached = true;
        } else if (mFirstItem == 0) {
            v = getChildAt(1);
            int top = getDecoratedTop(v);
            //LogBuilder.build("Top: " + top +" dy: " + dy);

            //LogBuilder.build("Changed dy: " + dy + " Top: " + top + " " + mFirstItemHeight);
            if (top == mFirstItemHeight) {
                topBoundReached = true;
            }
        }

        if (dy > 0) {
            if (bottomBoundReached) {
//                LogBuilder.build("BottomBound Reached!");
                return 0;
            }
        } /*else {
            if (topBoundReached) {
                LogBuilder.build("TopBound Reached!");
                return 0;
            }
            //LogBuilder.build("Scrolling Down");
        }*/

        delta = -dy;
        //the list scrolled with displacement of delta
        offsetChildrenVertical(delta);

        updateHeaderTop(dy);
        updateFirstItem(dy);
        updateSecondItem(dy);
        updateMagnetVisibleRowCount();

        if (dy > 0) {
            mDirection = DIRECTION_UP;
            //LogBuilder.build("Direction UP: " + dy);
            layoutViews(dy, recycler);
        } else {
            mDirection = DIRECTION_DOWN;
            //LogBuilder.build("Direction DOWN: " + dy);
            layoutViews(dy, recycler);
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
//                LogBuilder.build("IDLE: " + " fTop: " + mFirstItemTop + " HeaderArea: " + HEADER_VISIBLE_AREA +
//                        " sTop: " + mSecondItemTop + " UP: " + UP_THRESHOLD + " DOWN: " + DOWN_THRESHOLD + " " + mDirection);

                /*if (Gazapp.getGazapp().hasExpanded) {
                    mDeckHeaderCallback.setFlingAction(incrementedBy);
                    justStarted = true;
                    return;
                }*/

                if (canScrollToBorders) {
                    switch (mDirection) {
                        case DIRECTION_UP:
                            if (!bottomBoundReached) {
                                if (mFirstItem == 1 && mFirstItemTop > HEADER_VISIBLE_AREA) {
                                    if (mFirstItemTop < UP_THRESHOLD && !justStarted) {
//                                    LogBuilder.build("Up If: " + justStarted);
                                        mDeckHeaderCallback.setFlingAction(mFirstItemTop - HEADER_VISIBLE_AREA);
                                    } else {
//                                    LogBuilder.build("Up else: " + justStarted);
                                        mDeckHeaderCallback.setFlingAction(mHeaderTop);
                                    }
                                } else if (mFirstItem >= 1 && mFirstItemTop == HEADER_VISIBLE_AREA) {
                                    if (mSecondItemTop < UP_THRESHOLD) {
//                                    LogBuilder.build("else if : " + justStarted);
                                        mDeckHeaderCallback.setFlingAction(mSecondItemTop - mFirstItemTop);
                                    } else {
//                                    LogBuilder.build("else if else: " + justStarted);
                                        mDeckHeaderCallback.setFlingAction(mSecondItemTop - (HEADER_VISIBLE_AREA + mFirstItemHeight));
                                    }
                                }
                            }

                            break;
                        case DIRECTION_DOWN:

                            if (mFirstItemTop == HEADER_VISIBLE_AREA) {
                                if (mSecondItemTop > DOWN_THRESHOLD)
                                    mDeckHeaderCallback.setFlingAction(mSecondItemTop - (mFirstItemHeight + HEADER_VISIBLE_AREA));
                                else
                                    mDeckHeaderCallback.setFlingAction(mSecondItemTop - HEADER_VISIBLE_AREA);
                            } else {
                                if (mFirstItemTop < DOWN_THRESHOLD) {
//                                LogBuilder.build("Down Else IF  if: " + justStarted);
                                    mDeckHeaderCallback.setFlingAction(mFirstItemTop - HEADER_VISIBLE_AREA);
                                } else {
                                    mDeckHeaderCallback.setFlingAction(mFirstItemTop - mHeaderHeight);
//                                LogBuilder.build("Down Else IF  else: " + justStarted);
                                    if (justStarted)
                                        justStarted = false;
                                }
                            }
                            break;
                    }
                }

                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                //                LogBuilder.build("Scroll Dragging");
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                //                LogBuilder.build("Scroll Settling: " + mFirstItemTop);
                break;
        }
    }


    public int getFlingDisplacement(float velocityY) {

        int displacement = 0;
        if (velocityY <= -100) {
            if (mFirstItemTop > HEADER_VISIBLE_AREA) {
                displacement = mFirstItemTop - HEADER_VISIBLE_AREA;
            } else if (mSecondItemTop > HEADER_VISIBLE_AREA) {
                displacement = mSecondItemTop - HEADER_VISIBLE_AREA;
            }
        } else if (velocityY > 100) {
            //displacement must be negative
//            LogBuilder.build("Negative Velocity sTop: " + mSecondItemTop + " Displacement: " + displacement + " velocity: " + velocityY);
            if (mSecondItemTop > HEADER_VISIBLE_AREA && mSecondItemTop < mFirstItemHeight + HEADER_VISIBLE_AREA) {
                displacement = mSecondItemTop - (mFirstItemHeight + HEADER_VISIBLE_AREA/* + mDecoratedChildHeight*/);
//                LogBuilder.build("If sTop: " + mSecondItemTop + " Displacement: " + displacement);
            } else if (mFirstItemTop < mFirstItemHeight) {
                displacement = mFirstItemTop - mHeaderHeight;
//                LogBuilder.build("else sTop: " + mSecondItemTop + " Displacement: " + displacement);
            }
        }

//        LogBuilder.build("Displacement: FirstItem: " + mFirstItem +
//                /*" FirstItem Top: " + mFirstItemTop +*/
//                " SecondTop: " + mSecondItemTop +
//                " Displacement: " + displacement);
        return displacement;
    }

    /**
     * @param canReset the item scrolls to top or bottom if true
     */
    public void setScrollingToBorders(boolean canReset) {
        this.canScrollToBorders = canReset;
    }

    /**
     * @param canAnimate - callbacks listener that animates the item if true
     */
    public void setItemAnimatation(boolean canAnimate) {
        this.canAnimate = canAnimate;
    }


    /**
     * sets the adapter position pos as the firstitem without scrolling
     *
     * @param pos - adapterposition
     */
    public void moveToPosition(int pos) {
        mFirstItem = pos;

        mHeaderTop = HEADER_TOP;
        mFirstItemTop = HEADER_VISIBLE_AREA;
        mSecondItemTop = mFirstItemTop + mFirstItemHeight;
        vTop = mSecondItemTop + mDecoratedChildHeight;

        layoutViews(10, mRecyler);
        LogBuilder.build("setFirstItem: " + pos + " " + mRecyclerViewHeight);
    }

}
