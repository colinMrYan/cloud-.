package com.inspur.emmcloud.ui.chat.mvp.model.bean;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

/**
 * Created by libaochao on 2019/10/18.
 */

public class NoScrollGridLayoutManager extends GridLayoutManager {

    private boolean mCanVerticalScroll = true;


    public NoScrollGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NoScrollGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public NoScrollGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }


    @Override
    public boolean canScrollVertically() {
        if (!mCanVerticalScroll) {
            return false;
        } else {
            return super.canScrollVertically();
        }
    }

    public void setmCanVerticalScroll(boolean b) {
        mCanVerticalScroll = b;
    }

}
