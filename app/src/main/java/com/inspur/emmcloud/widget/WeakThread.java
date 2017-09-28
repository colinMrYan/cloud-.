package com.inspur.emmcloud.widget;

import java.lang.ref.WeakReference;

/**
 * Created by chenmch on 2017/9/19.
 */

public class WeakThread<T> extends Thread {
    protected WeakReference<T> reference;

    public WeakThread(T reference) {
        this.reference = new WeakReference<>(reference);
    }

    @Override
    public void run() {
        super.run();
        T t = reference.get();
        if (t == null)
            return;
    }
}
