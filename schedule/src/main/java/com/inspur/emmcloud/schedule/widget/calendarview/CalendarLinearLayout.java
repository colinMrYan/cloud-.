package com.inspur.emmcloud.schedule.widget.calendarview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ScrollView;


/**
 * 如果嵌套各种View出现事件冲突，可以实现这个方法即可
 */
public class CalendarLinearLayout extends LinearLayout implements CalendarLayout.CalendarScrollView {

    private ScrollView scrollView;
    private RecyclerView recyclerView;

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
        if (scrollView == null) {
            if (getChildCount() > 1 && getChildAt(1) instanceof ScrollView) {
                scrollView = (ScrollView) getChildAt(1);
            }
        }
        if (recyclerView == null) {
            if (getChildCount() > 2 && getChildAt(2) instanceof RecyclerView) {
                recyclerView = (RecyclerView) getChildAt(2);
            }
        }
        if (scrollView != null && scrollView.getVisibility() == VISIBLE) {
            return scrollView.getScrollY() == 0;
        }

        if (recyclerView != null && recyclerView.getVisibility() == VISIBLE) {
            return recyclerView.computeVerticalScrollOffset() == 0;
        }
        return false;
    }

}
