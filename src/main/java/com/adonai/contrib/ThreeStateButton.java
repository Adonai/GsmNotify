package com.adonai.contrib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;

import com.adonai.GsmNotify.R;

/**
 * A button designed with three states: unpressed, pressed (cross) and checked (tick)
 *
 * @author Dinesh Harjani (goldrunner192287@gmail.com)
 */
public class ThreeStateButton extends CheckBox {

    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_NO = 2;
    public static final int STATE_YES = 1;
    public static final int NUMBER_OF_STATES = 3;

    private int state;
    private Paint redPaint;
    private Paint greenPaint;

    public interface OnStateChangedListener {
        void onStateChanged(View v, int newState);
    }

    private OnStateChangedListener onStateChangedListener = null;

    /**
     * @param context
     */
    public ThreeStateButton(Context context) {
        super(context);
        initConfig();
    }

    /**
     * @param context
     * @param attrs
     */
    public ThreeStateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initConfig();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public ThreeStateButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initConfig();
    }

    private void nextState() {
        state++;
        state = state % ThreeStateButton.NUMBER_OF_STATES;
        //this.setPressed(false);
        // forces to redraw the view
        invalidate();
        if (onStateChangedListener != null) {
            onStateChangedListener.onStateChanged(this, state);
        }
    }

    private void initConfig() {
        // initialize variables
        state = ThreeStateButton.STATE_UNKNOWN;
        redPaint = new Paint();
        redPaint.setAntiAlias(true);
        redPaint.setColor(Color.RED);
        redPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.paint_font));
        greenPaint = new Paint();
        greenPaint.setAntiAlias(true);
        greenPaint.setColor(Color.argb(255, 0, 198, 0));
        greenPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.paint_font));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) && (event.getAction() == KeyEvent.ACTION_UP)) {
            nextState();
            this.setPressed(false);
        }
        return false;
    }

    @Override
    public boolean performClick() {
        nextState();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (state) {
            case STATE_NO:
                // draw an X using redPaint
                canvas.drawText("✗", (canvas.getWidth() - redPaint.measureText("✗")) / 2.0f, canvas.getHeight() * 0.68f, redPaint);
                //canvas.drawLine(getCompoundPaddingLeft(), getCompoundPaddingTop(), getWidth() - getCompoundPaddingRight(), getHeight() - getCompoundPaddingBottom(), redPaint);
                //canvas.drawLine(getWidth() - getCompoundPaddingRight(), getCompoundPaddingTop(), getCompoundPaddingLeft(), getHeight() - getCompoundPaddingBottom(), redPaint);
                break;
            case STATE_YES:
                // draw a tick

                canvas.drawText("✔", (canvas.getWidth() - greenPaint.measureText("✔")) / 2.0f, canvas.getHeight() * 0.68f, greenPaint);
                //canvas.drawLine(getWidth() - getCompoundPaddingRight(), getCompoundPaddingTop(), (float) (0 + (0.4f) * getWidth()), getHeight() - getCompoundPaddingBottom(), greenPaint);
                //canvas.drawLine(0 + (0.425f) * getWidth(), getHeight() - (0.9f) * getCompoundPaddingBottom(), getCompoundPaddingLeft(), getHeight() - (2f) * getCompoundPaddingBottom(), greenPaint);
                break;
            default:
                break;
        }
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        onStateChangedListener = listener;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        // forces to redraw the view
        invalidate();
    }

}