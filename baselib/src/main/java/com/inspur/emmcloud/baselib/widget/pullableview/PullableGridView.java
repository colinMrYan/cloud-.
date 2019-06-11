package com.inspur.emmcloud.baselib.widget.pullableview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class PullableGridView extends GridView implements Pullable {
    private boolean canPullDown = true;
    private boolean canPullUp = false;

    public PullableGridView(Context context) {
        super(context);
    }

    public PullableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCanPullDown(boolean canPullDown) {
        this.canPullDown = canPullDown;
    }

    public void setCanPullUp(boolean canPullUp) {
        this.canPullUp = canPullUp;
    }

    @Override
    public boolean canPullDown() {
        //	return canPullDown;
        if (!canPullDown) {
            return false;
        }
        if (getCount() == 0) {
            // 没有item的时候也可以下拉刷新
            return true;
        } else // 滑到顶部了
            return getFirstVisiblePosition() == 0
                    && getChildAt(0).getTop() >= getPaddingTop();
    }

    @Override
    public boolean canPullUp() {
        if (!canPullUp) {
            return false;
        }
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return false;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(
                    getLastVisiblePosition()
                            - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
                return true;
        }
        return false;
    }

}
