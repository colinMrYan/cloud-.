package com.inspur.emmcloud.application.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by yufuchang on 2017/4/7.
 */

public class ECMSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public ECMSpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildPosition(view) >= 0) {
            outRect.left = space;
            outRect.right = space;
        }
    }
}