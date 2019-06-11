package com.inspur.emmcloud.baselib.widget;

import android.text.style.ForegroundColorSpan;

/**
 * Created by chenmch on 2018/3/20.
 */

public class MyForegroundColorSpan extends ForegroundColorSpan {
    private String id;

    public MyForegroundColorSpan(int color, String id) {
        super(color);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
