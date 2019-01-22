package com.inspur.emmcloud.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by yufuchang on 2017/11/21.
 */

public class ECMRecyclerViewLinearLayoutManager extends LinearLayoutManager {
    private boolean isVerticalScrollEnabled = true;
    private boolean isHorizontalScrollEnabled = true;
    public ECMRecyclerViewLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public boolean canScrollVertically() {
        return isVerticalScrollEnabled && super.canScrollVertically();
    }

    @Override
    public boolean canScrollHorizontally() {
        return isHorizontalScrollEnabled && super.canScrollHorizontally();
    }

    /**
     * 设置可以纵向滑动
     * @param canScrollVertically
     */
    public void setCanScrollVerticallyScrollEnabled(boolean canScrollVertically) {
        this.isVerticalScrollEnabled = canScrollVertically;
    }

    /**
     * 设置可以横向滑动
     * @param canScrollHorizontally
     */
    public void setCanScrollHorizontally(boolean canScrollHorizontally){
        isHorizontalScrollEnabled = canScrollHorizontally;
    }
}
