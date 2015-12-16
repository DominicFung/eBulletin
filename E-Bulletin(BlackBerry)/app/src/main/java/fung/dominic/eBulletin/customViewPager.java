package fung.dominic.eBulletin;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class customViewPager extends ViewPager {

    private boolean isEnabled = true;

    public customViewPager(Context context) {
        super(context);
    }

    public customViewPager(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.isEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.isEnabled && super.onInterceptTouchEvent(ev);
    }

    public void setPSEnabled(boolean b){
        this.isEnabled = b;
    }

    public boolean isPSEnabled() {
        return this.isEnabled;
    };
}
