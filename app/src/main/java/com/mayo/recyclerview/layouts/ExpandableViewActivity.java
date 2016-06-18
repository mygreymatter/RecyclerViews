package com.mayo.recyclerview.layouts;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mayo.recyclerview.LogBuilder;
import com.mayo.recyclerview.R;

public class ExpandableViewActivity extends AppCompatActivity {

    private GestureDetectorCompat mDetector;
    //private RelativeLayout mLayout;
    private LinearLayout mLayoutLinear;
    private float initialScale = 1.0f;
    private int mLayoutHeight;
    private int mImageMarginTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_expandable_view_linear);

        mLayoutLinear = (LinearLayout) findViewById(R.id.inner_layout);
        mDetector = new GestureDetectorCompat(this, new MyGesture());

        mLayoutLinear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mDetector.onTouchEvent(event))
                    return true;
                return false;
            }
        });
    }

    class MyGesture extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            LogBuilder.build("onDown");
            //incrementedBy = 0;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //LogBuilder.build("onScroll");
            if (distanceY < 0) {
                moveAndScaleUp();
            } else {
                scaleDownAndMove();
            }
            return true;
        }
    }

    boolean hasExpanded;
    int incrementedBy = 0;
    int mImageInitialHeight;


    private void moveAndExpand2() {
        final ImageView iv = (ImageView) mLayoutLinear.findViewById(R.id.store_image);
        LogBuilder.build("Expand W: " + iv.getWidth() + " H: " + iv.getHeight());
        //set initial dimensions
        if (incrementedBy == 0) {
            mImageInitialHeight = iv.getHeight();
            LogBuilder.build("Expand Initial H: " + mImageInitialHeight);
        }

        if (incrementedBy < (mImageInitialHeight - 100)) {
            incrementedBy += 10;

            mLayoutLinear.post(new Runnable() {
                @Override
                public void run() {
                    iv.getLayoutParams().height = mImageInitialHeight + incrementedBy;
                    iv.getLayoutParams().width = mImageInitialHeight + incrementedBy;

                    mLayoutLinear.requestLayout();
                }
            });
        }
        hasExpanded = true;
    }

    private void collapseAndMove2() {
        if (!hasExpanded)
            return;
        //LogBuilder.build("collapse And Move");

        final ImageView iv = (ImageView) mLayoutLinear.findViewById(R.id.store_image);
        LogBuilder.build("Collapse W: " + iv.getWidth() + " H: " + iv.getHeight());

        if (incrementedBy > 0) {
            incrementedBy -= 10;

            mLayoutLinear.post(new Runnable() {
                @Override
                public void run() {
                    iv.getLayoutParams().height = mImageInitialHeight + incrementedBy;
                    iv.getLayoutParams().width = mImageInitialHeight + incrementedBy;
                    mLayoutLinear.requestLayout();
                }
            });
        }
    }

    private void moveAndScaleUp() {
//        LogBuilder.build("Move And Expand");

        final ImageView iv = (ImageView) mLayoutLinear.findViewById(R.id.store_image);
        final TextView title = (TextView) mLayoutLinear.findViewById(R.id.reward_name);
        final LinearLayout mDetailsLayout = (LinearLayout) mLayoutLinear.findViewById(R.id.store_details_layout);

        LogBuilder.build("Expand T: " + iv.getTop() + " " + incrementedBy);

        //set initial dimensions
        if (incrementedBy == 0) {
            mLayoutHeight = mLayoutLinear.getHeight();
            mImageMarginTop = 191;
        }

        if (initialScale >= 2.0f)
            initialScale = 2.0f;
        else
            initialScale += 0.0150f;

        if (incrementedBy < 700) {
            incrementedBy += 10;

            mLayoutLinear.post(new Runnable() {
                @Override
                public void run() {
                    //LogBuilder.build("B H: " + mLayoutLinear.getLayoutParams().height + " " + iv.getHeight() + " " + mDetailsLayout.getTop());
                    //mLayoutLinear.getLayoutParams().height = mLayoutHeight + incrementedBy;
                    //mLayoutLinear.requestLayout();
                    //LogBuilder.build("A H: " + mLayoutLinear.getLayoutParams().height);

                    //iv.getLayoutParams().height = 450 + (incrementedBy / 2);
                    //iv.requestLayout();

                    //mDetailsLayout.setTop(iv.getLayoutParams().height + 100);


                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
                    params.setMargins(0,mImageMarginTop + (incrementedBy/2),0,0);
                    iv.setLayoutParams(params);

                    iv.setScaleY(initialScale);
                    iv.setScaleX(initialScale);

                    mLayoutLinear.requestLayout();
                }
            });

        }

        hasExpanded = true;
    }

    private void scaleDownAndMove() {
        if (!hasExpanded)
            return;
        //LogBuilder.build("collapse And Move");

        final ImageView iv = (ImageView) mLayoutLinear.findViewById(R.id.store_image);
        final LinearLayout mDetailsLayout = (LinearLayout) mLayoutLinear.findViewById(R.id.store_details_layout);

        LogBuilder.build("Collapse T: " + iv.getTop() + " H: " + iv.getHeight() + " Details Top: " + mDetailsLayout.getTop() + " " + incrementedBy);
        //set initial dimensions
        /*if (incrementedBy == 0) {
            mLayoutHeight = mLayoutLinear.getHeight();
            initialScale = iv.getScaleY();
        }*/

        if (initialScale <= 1.0f)
            initialScale = 1.0f;
        else
            initialScale -= 0.0150f;

        if (incrementedBy > 0) {
            incrementedBy -= 10;

            mLayoutLinear.post(new Runnable() {
                @Override
                public void run() {
                    iv.setScaleY(initialScale);
                    iv.setScaleX(initialScale);

                    iv.getLayoutParams().height = 450 + (incrementedBy / 2);

                    mDetailsLayout.setTop(iv.getLayoutParams().height + 100);
                    mLayoutLinear.getLayoutParams().height = mLayoutHeight + incrementedBy;

                    mLayoutLinear.requestLayout();
                }
            });

        }
    }

}
