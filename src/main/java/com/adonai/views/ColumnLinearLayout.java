package com.adonai.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by adonai on 02.02.15.
 */
public class ColumnLinearLayout extends ViewGroup {
    public ColumnLinearLayout(Context context) {
        super(context);
    }

    public ColumnLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColumnLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childrenCount = getChildCount();

        //get the available size of child view
        int childLeft = this.getPaddingLeft();
        int childTop = this.getPaddingTop();
        int childRight = this.getMeasuredWidth() - this.getPaddingRight();
        int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
        int childWidth = childRight - childLeft;
        int childHeight = childBottom - childTop;

        int curLeft = childLeft;
        int curTop = childTop;

        int buttonsInAColumn = 0, columnCount = 0, viewHeight = 0, viewWidth = 0;
        //walk through each child, and arrange it from left to right
        for (int i = 0; i < childrenCount; i++) {
            View child = getChildAt(i);
            //Get the height of one button on first run
            if (columnCount == 0) {
                child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
                viewHeight = child.getMeasuredHeight();
                buttonsInAColumn = childHeight / viewHeight;
                columnCount = childrenCount / (buttonsInAColumn + 1) + 1; // increase on overflow of current column
                viewWidth = childWidth / columnCount;

                if(columnCount >= 5) { // width of views will be too small
                    columnCount = 5;
                    buttonsInAColumn = childrenCount / columnCount;
                    viewWidth = childWidth / columnCount;
                }
            }

            // fix text alignment
            child.measure(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
            child.layout(curLeft, curTop, curLeft + viewWidth, curTop + viewHeight);
            curTop += viewHeight;

            // check if next button would be too fat
            if ((i + 1) % buttonsInAColumn == 0) { // start next column
                curTop = childTop;
                curLeft += viewWidth;
            }
        }
    }

}
