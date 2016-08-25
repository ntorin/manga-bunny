package com.fruits.ntorin.mango.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by Ntori on 7/13/2016.
 */
public class LockableScrollView extends ScrollView{
    private boolean mLocked = true;

    public LockableScrollView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //return super.onTouchEvent(ev);
        return mLocked;
    }

    public boolean ismLocked() {
        return mLocked;
    }

    public void setmLocked(boolean mLocked) {
        this.mLocked = mLocked;
    }
}
