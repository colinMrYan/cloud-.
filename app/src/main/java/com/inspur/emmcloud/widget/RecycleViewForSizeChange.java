package com.inspur.emmcloud.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by chenmch on 2017/8/15.
 */

public class RecycleViewForSizeChange extends RecyclerView {
    public RecycleViewForSizeChange(Context context) {
        super(context);
    }

    public RecycleViewForSizeChange(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecycleViewForSizeChange(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, final int h, int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getAdapter() != null) {
                    MoveToPosition(getAdapter().getItemCount() - 1);
                }
            }
        }, 50);
    }

    public void MoveToPosition(int position) {
        LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
        int firstItem = manager.findFirstVisibleItemPosition();
        int lastItem = manager.findLastVisibleItemPosition();
        if (position <= firstItem) {
            scrollToPosition(position);
        } else if (position < lastItem) {
            int top = getChildAt(position - firstItem).getTop();
            scrollBy(0, top);
        } else {
            scrollToPosition(position);
        }

    }

}
