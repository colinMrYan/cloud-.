package com.inspur.emmcloud.baselib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by chenmch on 2018/12/28.
 */

public class NoScrollWebView extends WebView {
    public NoScrollWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NoScrollWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollWebView(Context context) {
        super(context);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        scrollTo(l, 0);
    }
}
