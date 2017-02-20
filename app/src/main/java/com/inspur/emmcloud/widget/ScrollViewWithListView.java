package com.inspur.emmcloud.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ListView;

public class ScrollViewWithListView extends ListView {

	public ScrollViewWithListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollViewWithListView(Context context) {
		super(context);
	}

	public ScrollViewWithListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
