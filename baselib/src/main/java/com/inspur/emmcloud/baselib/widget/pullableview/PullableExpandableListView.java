package com.inspur.emmcloud.baselib.widget.pullableview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class PullableExpandableListView extends ExpandableListView implements
        Pullable {

    private boolean canpullup = true;
    private boolean canpulldown = true;

    public PullableExpandableListView(Context context) {
        super(context);
    }

    public PullableExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableExpandableListView(Context context, AttributeSet attrs,
                                      int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCanpullup(boolean canpullup) {
        this.canpullup = canpullup;
    }

    public void setCanpulldown(boolean canpulldown) {
        this.canpulldown = canpulldown;
    }

    @Override
    public boolean canPullDown() {
        if (canpulldown == false) {
            return canpulldown;
        }
        if (getCount() == 0) {
            // 没有item的时候也可以下拉刷新
            return true;
        } else if (getChildAt(0) == null) {  //解决上拉点击crash的问题
            return false;
        } else // 滑到顶部了
            return getFirstVisiblePosition() == 0
                    && getChildAt(0).getTop() >= 0;
    }

    @Override
    public boolean canPullUp() {
        if (canpullup == false) {
            return canpullup;
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
