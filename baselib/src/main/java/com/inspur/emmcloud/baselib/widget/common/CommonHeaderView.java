package com.inspur.emmcloud.baselib.widget.common;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;

import butterknife.OnClick;

/**
 * Date：2022/12/7
 * Author：wang zhen
 * Description 通用标题栏
 */
public class CommonHeaderView extends RelativeLayout {

    private TextView titleTv;
    private ImageView moreIv;
    private Context context;

    public CommonHeaderView(Context context) {
        this(context, null);
    }

    public CommonHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        this.context = context;
        inflate(context, R.layout.design3_common_header_view, this);
        titleTv = findViewById(R.id.tv_title);
        moreIv = findViewById(R.id.iv_more);

    }

    public void setTitle(String title) {
        titleTv.setText(title);
    }

    public void setRightAttr(int attr) {
        moreIv.setVisibility(VISIBLE);
        moreIv.setImageResource(ResourceUtils.getResValueOfAttr(context, attr));
    }

    public void setTitleVisibility(int visibility) {
        titleTv.setVisibility(visibility);
    }

}
