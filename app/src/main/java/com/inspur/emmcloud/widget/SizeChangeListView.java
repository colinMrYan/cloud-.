package com.inspur.emmcloud.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by chenmch on 2017/8/15.
 */

public class SizeChangeListView extends ListView{
    public SizeChangeListView(Context context) {
        super(context);
    }

    public SizeChangeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizeChangeListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setSelection(getCount());
    }

}
