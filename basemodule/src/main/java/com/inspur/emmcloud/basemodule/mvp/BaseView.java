package com.inspur.emmcloud.basemodule.mvp;

import android.app.Activity;
import android.content.Context;

public interface BaseView {
    //提供context供presenter使用
    Context getContext();

    //提供activity
    Activity getActivity();

    /**
     * 显示加载框
     */
    void showLoading();

    /**
     * 隐藏加载框
     */
    void dismissLoading();

    //网络返回错误
    void showError(String error, int responseCode);
}
