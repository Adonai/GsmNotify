package com.adonai.contrib;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import com.adonai.GsmNotify.Utils;

/**
 * Created by adonai on 01.05.15.
 */
public class ButtonWithRedTriangle extends Button {
    
    private boolean isChecked = false;
    
    public ButtonWithRedTriangle(Context context) {
        super(context);
    }

    public ButtonWithRedTriangle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonWithRedTriangle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ButtonWithRedTriangle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isChecked) {
            int leftPad = (int) Utils.convertDpToPixel(5, getContext());
            int topPad = (int) Utils.convertDpToPixel(6, getContext());;
            
            int height = getHeight();
            int width = getWidth();

            Paint paint = new Paint();
            paint.setStrokeWidth(4);
            paint.setColor(android.graphics.Color.RED);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setAntiAlias(true);

            Point a = new Point(leftPad, topPad);
            Point b = new Point(width / 10 + leftPad, topPad);
            Point c = new Point(leftPad, height / 10 + topPad);

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(a.x, a.y);
            path.lineTo(b.x, b.y);
            path.lineTo(c.x, c.y);
            path.close();

            canvas.drawPath(path, paint);
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
