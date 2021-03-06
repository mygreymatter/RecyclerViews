package com.mayo.recyclerview.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mayo.recyclerview.DeckHeaderCallback;
import com.mayo.recyclerview.Gazapp;
import com.mayo.recyclerview.LogBuilder;
import com.mayo.recyclerview.R;

import org.w3c.dom.Text;

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
    private int mHeaderHeight;
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

    private Context mContext;
    private DeckHeaderCallback mDeckHeaderCallback;
    private boolean hasTransition;
    private int vTop;
    private int mHeaderImageInitialHeight;
    private int incrementedBy = 0;
    private boolean justStarted;

    public DeckHeaderLayoutManager(Context context, int density) {
        mContext = context;
        mDeckHeaderCallback = (DeckHeaderCallback) context;

        densityOfScreen = density;
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
            LogBuilder.build("Header Height: " + mDecoratedChildHeight);

            mHeaderHeight = mDecoratedChildHeight;

            r = (RelativeLayout) scrap.findViewById(R.id.inner_layout);
            LinearLayout l = (LinearLayout) r.findViewById(R.id.store_details_layout);
            TextView t = (TextView) l.findViewById(R.id.spot_rewards);
            t.measure(0, 0);

            LogBuilder.build("Height: " + t.getMeasuredHeight());
            HEADER_VISIBLE_AREA = t.getMeasuredHeight();

            detachAndScrapView(scrap, recycler);

            scrap = recycler.getViewForPosition(1);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            //assume that the size of each child view is same
            //calculate the decorated values upfront
            mFirstItemHeight = getDecoratedMeasuredHeight(scrap);
            LogBuilder.build("Item Height: " + mFirstItemHeight);

            r = (RelativeLayout) scrap.findViewById(R.id.inner_layout);
            t = (TextView) r.findViewById(R.id.reward_name);
            t.measure(0, 0);

            LogBuilder.build("Height: " + t.getMeasuredHeight());
            mDecoratedChildHeight = t.getMeasuredHeight();

            HEADER_TOP = HEADER_VISIBLE_AREA - mHeaderHeight;
        }

        mRecyclerViewHeight = getVerticalSpace();
        UP_THRESHOLD = (int) (0.7 * mRecyclerViewHeight);
        DOWN_THRESHOLD = (int) (0.18 * mRecyclerViewHeight);

        mSecondItemTop = mFirstItemHeight;

        mFirstItem = 1;
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

        LogBuilder.build("RH: " + mRecyclerViewHeight +
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
        if (!Gazapp.getGazapp().hasExpanded) {
            /*if (mHeaderTop == HEADER_TOP) {
                if (mSecondItemTop <= (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                    mVisibleRowCount++;

                    for(int t = vTop; t < mRecyclerViewHeight; t += mDecoratedChildHeight)
                        mVisibleRowCount++;

                    LogBuilder.build("Rows if: " + mVisibleRowCount);
                }
            }else{
                if (mSecondItemTop <= (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                    mVisibleRowCount++;
                    LogBuilder.build("Rows Else if: " + mVisibleRowCount);
                }

            }*/
//            LogBuilder.build("Rows : " + vTop + " " + mDecoratedChildHeight);
            for (int t = vTop; t < mRecyclerViewHeight; t += mDecoratedChildHeight) {
                mVisibleRowCount++;
            }
        }
//        LogBuilder.build("Rows: " + mVisibleRowCount + " sTop: " + mSecondItemTop + " " + (HEADER_VISIBLE_AREA + mFirstItemHeight) + " " + vTop + " " + mRecyclerViewHeight);

        Gazapp.getGazapp().firstItem = mFirstItem;
    }

    private void updateHeaderTop(int scrolledBy) {
        if (scrolledBy > 0) {
            if (mHeaderTop == 0 && Gazapp.getGazapp().hasExpanded) {
                if (incrementedBy > 0) {
                    setElasticEffect(scrolledBy);
                } else {
                    Gazapp.getGazapp().hasExpanded = false;
                }
            } else {
                mHeaderTop -= scrolledBy;
                if (mHeaderTop < HEADER_TOP) {
                    mHeaderTop = HEADER_TOP;

                }
            }
//            LogBuilder.build("Up Header Top: " + mHeaderTop + " " + Gazapp.getGazapp().hasHeaderSticky);
        } else {
            if (mHeaderTop == 0) {
                //LogBuilder.build("Header is at top.");
                setElasticEffect(scrolledBy);
                //check whether the second item is at the bottom
            } else if (mSecondItemTop >= mFirstItemHeight + HEADER_VISIBLE_AREA && mFirstItem == 1) {
                mHeaderTop -= scrolledBy;
//                LogBuilder.build("If Header Top: " + mHeaderTop + " scrolled: " + scrolledBy);
                if (mHeaderTop > 0) {
                    mHeaderTop = 0;
                }
            }

            if (mFirstItem > 1 && mSecondItemTop >= HEADER_VISIBLE_AREA + mFirstItemHeight) {
                LogBuilder.build("------------------Down Transition------------------------");
                mFirstItem--;
                hasTransition = true;
            }
//            LogBuilder.build("Header First Item: " + mFirstItem + " Second Top: " + mSecondItemTop + " Transition: " + hasTransition);
        }

        if (mHeaderTop == HEADER_TOP) {
            Gazapp.getGazapp().hasHeaderSticky = true;
        } else {
            Gazapp.getGazapp().hasHeaderSticky = false;
        }
        /*LogBuilder.build("Header Top: " + mHeaderTop + " scrolled: " + scrolledBy +
                " FirstTop: " + mFirstItemTop + " sTop: " + mSecondItemTop + " " + mDirection + " Transition: " + hasTransition);*/
    }

    private void setElasticEffect(int scrolledBy) {
        if (incrementedBy == 0) {
            v = getChildAt(0);
            final RelativeLayout mLayoutLinear = (RelativeLayout) v.findViewById(R.id.inner_layout);
            ImageView iv = (ImageView) mLayoutLinear.findViewById(R.id.store_image);

            Gazapp.getGazapp().imageDimension = iv.getHeight();
            mHeaderImageInitialHeight = iv.getHeight();
            //LogBuilder.build("Initialize: " + iv.getHeight());
            incrementedBy = -scrolledBy;
        }

        if (scrolledBy < 0) {
            if (Gazapp.getGazapp().imageDimension >= mHeaderImageInitialHeight + 300) {
                Gazapp.getGazapp().imageDimension = mHeaderImageInitialHeight + 300;
            } else {
                incrementedBy -= scrolledBy;
                Gazapp.getGazapp().imageDimension -= scrolledBy;
            }
//            LogBuilder.build("Computed Dimension: " + Gazapp.getGazapp().imageDimension);
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

//            LogBuilder.build("Computed Dimension: " + Gazapp.getGazapp().imageDimension + " " + incrementedBy);
        }
    }

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

        LogBuilder.build("Updated FirstItemTop: " + mFirstItemTop);
    }

    private void updateSecondItem(int scrolledBy) {
        if (scrolledBy > 0) {
            LogBuilder.build("Second Up------------------");
            if (mFirstItemTop == HEADER_VISIBLE_AREA) {//when the r_lengthy_header becomes sticky
                if ((mSecondItemTop - scrolledBy) <= HEADER_VISIBLE_AREA) {
                    mFirstItem++;
                    mFirstItemTop = mSecondItemTop;
                    mSecondItemTop = mFirstItemTop + mFirstItemHeight;
                    LogBuilder.build("Second Up Transition------------------FirstItem: " + mFirstItem + " Prev: " + (mFirstItem - 1));
                } else {
                    mSecondItemTop -= scrolledBy;
                }
                LogBuilder.build("if Second Top: " + mSecondItemTop);
            } else {
                if ((mSecondItemTop - scrolledBy) <= (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                    mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                    LogBuilder.build("else if Second Top: " + mSecondItemTop);
                } else {
                    mSecondItemTop -= scrolledBy;
                    LogBuilder.build("else if else Second Top: " + mSecondItemTop);
                }

                if (mFirstItem == 1 && mFirstItemTop == mHeaderHeight) {
                    mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;
                    LogBuilder.build("else if else if Second Top: " + mSecondItemTop);
                }
            }

        } else if (scrolledBy < 0) {
            LogBuilder.build("Second Down-------------------------------");
//            LogBuilder.build("Before Else Second Item Top: " + mSecondItemTop + " First Bottom: " + (mFirstItemHeight + HEADER_VISIBLE_AREA));
            if (hasTransition) {
                vTop = mSecondItemTop;
                mSecondItemTop = mFirstItemTop - scrolledBy;
//                LogBuilder.build(" Transition: " + mSecondItemTop);
            } else {
                if (mFirstItem == 1) {
                    mSecondItemTop -= scrolledBy;

                    if (mFirstItemTop == HEADER_VISIBLE_AREA) {
                        if (mSecondItemTop >= (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                            mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                        }
                    } else if (mFirstItemTop > HEADER_VISIBLE_AREA) {
                        if ((mSecondItemTop - mFirstItemTop) > mDecoratedChildHeight)
                            mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                        else
                            mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;
                    }
                } else {
                    mSecondItemTop -= scrolledBy;
                    LogBuilder.build("else if else SecondTop: " + mSecondItemTop);
                    if (mSecondItemTop > (HEADER_VISIBLE_AREA + mFirstItemHeight)) {
                        mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                        LogBuilder.build("else if else if SecondTop: " + mSecondItemTop);
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

        return vTop;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void layoutViews(int direction, int dy, RecyclerView.
            Recycler recycler, RecyclerView.State state) {

        //LogBuilder.build("\n------------------------------------------------------------");

        if (getChildCount() > 0) {
            removeAllViews();
        }

        int adapterPostion;
        int vTop = 0;


        for (int i = 0; i < mVisibleRowCount; i++) {
            adapterPostion = getAdapterPosition(i, mFirstItem);
            //LogBuilder.build("Adapter Position: " + adapterPostion + " " + i + " FirstItem: " + mFirstItem);

            if (adapterPostion == -1 || adapterPostion >= getItemCount())
                continue;
            else if (i == 0) {
                v = recycler.getViewForPosition(0);
            } else {
                v = recycler.getViewForPosition(adapterPostion);
            }

            measureChildWithMargins(v, 0, 0);

            switch (i) {
                case 0:
                    layoutDecorated(v, 0, mHeaderTop,
                            mDecoratedChildWidth,
                            mHeaderHeight + incrementedBy);

                    LogBuilder.build(i + " Height: " + (mFirstItemHeight + incrementedBy) + " " +
                            Gazapp.getGazapp().hasExpanded + " " + incrementedBy);

                    break;
                case 1:
                    if (Gazapp.getGazapp().hasExpanded)
                        mFirstItemTop = mFirstItemHeight + incrementedBy;

                    layoutDecorated(v, 0, mFirstItemTop,
                            mDecoratedChildWidth,
                            mFirstItemTop + mFirstItemHeight);

                    LogBuilder.build(adapterPostion + " Top: " + mFirstItemTop + " Height: " + mFirstItemHeight +
                            " Expanding: " + Gazapp.getGazapp().hasExpanded +
                            " JustStarted: " + justStarted + " " + incrementedBy);

                    if (mDirection != DIRECTION_NONE)
                        setAnimations(v, mFirstItemTop);

                    break;
                case 2:

                    if (adapterPostion == getItemCount() - 1 && mSecondItemTop < HEADER_VISIBLE_AREA + mFirstItemHeight) {
                        mSecondItemTop = HEADER_VISIBLE_AREA + mFirstItemHeight;
                        LogBuilder.build("second Top set");
                    }

                    if (Gazapp.getGazapp().hasExpanded)
                        mSecondItemTop = mFirstItemTop + mDecoratedChildHeight;

                    layoutDecorated(v, 0, mSecondItemTop,
                            mDecoratedChildWidth,
                            mSecondItemTop + mFirstItemHeight);

                    vTop = getThirdItem(dy);

                    LogBuilder.build(adapterPostion +
                            " Top: " + mSecondItemTop +
                            " Next vTop: " + vTop +
                            " Height: " + mFirstItemHeight);

                    if (mDirection != DIRECTION_NONE)
                        setAnimations(v, mSecondItemTop);

                    if (Gazapp.getGazapp().hasExpanded)
                        vTop = mSecondItemTop + mDecoratedChildHeight;

                    break;
                default:

                    layoutDecorated(v, 0, vTop,
                            mDecoratedChildWidth,
                            vTop + mFirstItemHeight);

                    LogBuilder.build(adapterPostion + " Top: " + vTop +
                            " Next Top: " + (vTop + mDecoratedChildHeight) +
                            " Height: " + mFirstItemHeight);

                    vTop += mDecoratedChildHeight;
                    break;
            }

            addView(v);

            //no need to display children whose top is beyond the recyclerView's viewport
            if (vTop > mRecyclerViewHeight) {
                break;
            }
        }
    }

    private void setAnimations(View v, int top) {
        int size = 30;
        float alpha = 0.2f;
        boolean canBold = false;

        r = (RelativeLayout) v.findViewById(R.id.inner_layout);
        TextView rewardName = (TextView) r.findViewById(R.id.reward_name);
        LinearLayout ll = (LinearLayout) r.findViewById(R.id.info_layout);

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

        //LogBuilder.build("Top: "+ top + " Size: " + size);
        rewardName.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        if (canBold) {
            rewardName.setTypeface(rewardName.getTypeface(), Typeface.BOLD);
            ll.setAlpha(1.0f);
        } else {
            rewardName.setTypeface(rewardName.getTypeface(), Typeface.NORMAL);
            ll.setAlpha(alpha);
        }
        //LogBuilder.build("Alpha");
    }

    boolean bottomBoundReached = false;
    boolean topBoundReached = false;

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.
            State state) {

//        LogBuilder.build("---------------------------------------------------");
//        LogBuilder.build("Scrolled By: " + dy);

        if (getChildCount() == 0)
            return 0;

        /*if (mHeaderTop == 0 && dy < 0)
            return 0;*/

        int delta;
        bottomBoundReached = false;
        topBoundReached = false;

        //LogBuilder.build("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());

        if (mFirstItem == getItemCount() - 2) {
            //LogBuilder.build("First Item: " + mFirstItem + " Rows: " + mVisibleRowCount + " TotalItems: " + getItemCount());
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
                LogBuilder.build("BottomBound Reached!");
                return 0;
            }
            //LogBuilder.build("Scrolling Up");
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
            layoutViews(DIRECTION_UP, dy, recycler, state);
        } else {
            mDirection = DIRECTION_DOWN;
            //LogBuilder.build("Direction DOWN: " + dy);
            layoutViews(DIRECTION_DOWN, dy, recycler, state);
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
                LogBuilder.build("IDLE: " + " fTop: " + mFirstItemTop + " HeaderArea: " + HEADER_VISIBLE_AREA +
                        " sTop: " + mSecondItemTop + " UP: " + UP_THRESHOLD + " DOWN: " + DOWN_THRESHOLD + " " + mDirection);

                if (Gazapp.getGazapp().hasExpanded) {
                    mDeckHeaderCallback.setFlingAction(incrementedBy);

                    justStarted = true;

                    return;
                }

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
                        } else if (mFirstItemTop != HEADER_VISIBLE_AREA) {
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
                LogBuilder.build("If sTop: " + mSecondItemTop + " Displacement: " + displacement);
            } else if (mFirstItemTop < mFirstItemHeight) {
                displacement = mFirstItemTop - mHeaderHeight;
                LogBuilder.build("else sTop: " + mSecondItemTop + " Displacement: " + displacement);
            }
        }

        if (Gazapp.getGazapp().hasExpanded && mHeaderTop == 0 && Gazapp.getGazapp().imageDimension > 450) {
            onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE);
        }

//        LogBuilder.build("Displacement: FirstItem: " + mFirstItem +
//                /*" FirstItem Top: " + mFirstItemTop +*/
//                " SecondTop: " + mSecondItemTop +
//                " Displacement: " + displacement);
        return displacement;
    }
}
