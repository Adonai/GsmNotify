package com.adonai.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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

    private int getContentHeight() {
        // Get the height of Status Bar
        int statusBarHeight = getStatusBarHeight();
        // Get the height occupied by the decoration contents
        int titleBarHeight =getActionBarHeight();

        // By now we got the height of titleBar & statusBar
        // Now lets get the screen size
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;

        return screenHeight - statusBarHeight - titleBarHeight;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getActionBarHeight() {
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = getContentHeight() - getPaddingTop() - getPaddingBottom();
        int childHeightMeasureSpec;

        if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        final int childrenCount = getChildCount();
        //get the available size of child view
        int childLeft = this.getPaddingLeft();
        int childTop = this.getPaddingTop();
        int childRight = width - this.getPaddingRight();
        int childWidth = childRight - childLeft;

        int curLeft = childLeft;
        int curTop = childTop;

        int buttonsInAColumn = 0, columnCount = 0, viewHeight = 0, viewWidth = 0;
        //walk through each child, and arrange it from left to right
        for (int i = 0; i < childrenCount; i++) {
            View child = getChildAt(i);
            //Get the height of one button on first run
            if (columnCount == 0) {
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec);
                viewHeight = child.getMeasuredHeight();
                buttonsInAColumn = height / viewHeight;
                columnCount = (int) Math.max(1,  Math.ceil((double) childrenCount / buttonsInAColumn)); // at least one column
                viewWidth = childWidth / columnCount;

                if(columnCount > 5) { // width of views will be too small
                    columnCount = 5;
                    buttonsInAColumn = (int) Math.ceil((double) childrenCount / columnCount);
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
            height = Math.max(height, curTop + viewHeight);
        }

        setMeasuredDimension(width, height);
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
                viewHeight = child.getMeasuredHeight();
                buttonsInAColumn = childHeight / viewHeight;
                columnCount = (int) Math.max(1,  Math.ceil((double) childrenCount / buttonsInAColumn)); // at least one column
                viewWidth = childWidth / columnCount;

                if(columnCount > 5) { // width of views will be too small
                    columnCount = 5;
                    buttonsInAColumn = (int) Math.ceil((double) childrenCount / columnCount);
                    viewWidth = childWidth / columnCount;
                }
            }

            // fix text alignment
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
