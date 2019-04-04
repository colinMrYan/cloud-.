package com.inspur.emmcloud.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.inspur.emmcloud.widget.calendarview.CalendarLayout;


/**
 * 如果嵌套各种View出现事件冲突，可以实现这个方法即可
 */
public class CalendarLinearLayout extends LinearLayout implements CalendarLayout.CalendarScrollView {

    private ScrollView scrollView;

    public CalendarLinearLayout(Context context) {
        super(context);
    }

    public CalendarLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 如果你想让下拉无效，return false
     *
     * @return isScrollToTop
     */
    @Override
    public boolean isScrollToTop() {
        if(scrollView == null){
            if (getChildCount() > 1 && getChildAt(1) instanceof ScrollView) {
                scrollView = (ScrollView) getChildAt(1);
            }
        }
        return scrollView != null && scrollView.getScrollY() == 0;
    }

}
