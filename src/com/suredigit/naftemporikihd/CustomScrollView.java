package com.suredigit.naftemporikihd;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView {
    private GestureDetector mGestureDetector;
    View.OnTouchListener mGestureListener;
    
    private float mLastX;
    private float mLastY;
    private float mDiffX;
    private float mDiffY;    

    
    private boolean mScrollable = true;
    
    public void setIsScrollable(boolean scrollable) {
        mScrollable = scrollable;
    }
    
    public boolean getIsScrollable() {
        return mScrollable;
    }
    
    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
//    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                // reset difference values
//                mDiffX = 0;
//                mDiffY = 0;
//
//                mLastX = ev.getX();
//                mLastY = ev.getY();
//                break;
//
//            case MotionEvent.ACTION_MOVE:
//                final float curX = ev.getX();
//                final float curY = ev.getY();
//                mDiffX += Math.abs(curX - mLastX);
//                mDiffY += Math.abs(curY - mLastY);
//                mLastX = curX;
//                mLastY = curY;
//
//                // don't intercept event, when user tries to scroll horizontally
//                if (mDiffX > mDiffY) {
//                	System.out.println("WE ARE NOT INTERCEPTING");
//                    return false;
//                }
//        }
//
//        return super.onInterceptTouchEvent(ev);
//    }    
    
    // Return false if we're scrolling in the x direction  

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }    
    
    class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceY) > Math.abs(distanceX)) {
                return true;
            }
            return false;
        }
    }
}
