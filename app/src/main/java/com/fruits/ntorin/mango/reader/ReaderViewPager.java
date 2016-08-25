package com.fruits.ntorin.mango.reader;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

/**
 * Created by Ntori on 6/12/2016.
 */
public class ReaderViewPager extends ViewPager{
    public ReaderViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Log.d("ReaderViewPager", "fragment touched");
        return super.onInterceptTouchEvent(ev);
    }
}
