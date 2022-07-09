package com.inspur.emmcloud.basemodule.media.selector.decoration;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author：luck
 * @data：2022/1/16 下午23:50
 * @describe:HorizontalItemDecoration
 */

public class HorizontalItemDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount;
    private final int spacing;

    public HorizontalItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;
        if (position == 0) {
            outRect.left = spacing - column * spacing / spanCount;
        } else {
            outRect.left = column * spacing / spanCount;
        }
        outRect.right = spacing - (column + 1) * spacing / spanCount;
        if (position < spanCount) {
            outRect.top = spacing;
        }
        outRect.bottom = spacing;
    }
}