package com.inspur.emmcloud.baselib.widget.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.inspur.baselib.R;

/**
 * Date：2022/12/7
 * Author：wang zhen
 * Description 首页tab标题栏
 */
public class HomeHeaderView extends RelativeLayout {
    public HomeHeaderView(Context context) {
        this(context, null);
    }

    public HomeHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        inflate(context, R.layout.home_header_view, this);
    }
}
