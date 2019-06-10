package com.inspur.emmcloud.baselib.widget.pullableview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class PullableListView extends ListView implements Pullable {
    private boolean canpullup = false;
    private boolean canpulldown = true;
    private boolean canSelectBottom = false;

    public PullableListView(Context context) {
        super(context);
    }

    public PullableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCanPullDown(boolean canPullDown) {
        this.canpulldown = canPullDown;
    }

    public void setCanPullUp(boolean canPullUp) {
        this.canpullup = canPullUp;
    }

    public void setCanSelectBottom(boolean canSelectBottom) {
        this.canSelectBottom = canSelectBottom;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        if (canSelectBottom) {
            setSelection(getCount());
        }
    }

    @Override
    public boolean canPullDown() {
        try {
            // return canPullDown;
            if (canpulldown == false) {
                return canpulldown;
            }
            if (getCount() == 0) {
                // 没有item的时候也可以下拉刷新
                return true;
            } else // 滑到ListView的顶部了
                return getFirstVisiblePosition() == 0
                        && getChildAt(0).getTop() >= 0;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }

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
