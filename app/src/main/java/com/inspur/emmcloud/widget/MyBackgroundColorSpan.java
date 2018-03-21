package com.inspur.emmcloud.widget;

import android.os.Parcel;
import android.text.style.BackgroundColorSpan;

/**
 * Created by chenmch on 2018/3/20.
 */

public class MyBackgroundColorSpan extends BackgroundColorSpan {
    private String id;
    public MyBackgroundColorSpan(int color) {
        super(color);
    }

    public MyBackgroundColorSpan(Parcel src) {
        super(src);
    }

    public MyBackgroundColorSpan(int color,String id) {
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
