package com.inspur.emmcloud.baselib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 可以设置最大高度的listview
 */

public class MaxHeightListView extends ListView {
    /**
     * listview最大高度
     */
    private int maxHeight;

    public MaxHeightListView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public MaxHeightListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public MaxHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setViewHeightBasedOnChildren();
    }


    public void setViewHeightBasedOnChildren() {
        ListAdapter listAdapter = this.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int sumHeight = 0;
        int size = listAdapter.getCount();

        for (int i = 0; i < size; i++) {
            //此处的View必须是Linearlayout。因为RelativeLayout的measure方法在Android4.4以下有bug
            View v = listAdapter.getView(i, null, this);
            v.measure(0, 0);
            sumHeight += v.getMeasuredHeight();


        }


        if (sumHeight > maxHeight) {
            sumHeight = maxHeight;
        }
        android.view.ViewGroup.LayoutParams params = this.getLayoutParams();
        // this.getLayoutParams();
        params.height = sumHeight;


        this.setLayoutParams(params);
    }


    public int getMaxHeight() {
        return maxHeight;
    }


    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

}
