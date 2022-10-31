package com.inspur.emmcloud.basemodule.mvp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;

public class BasePresenter<V extends BaseView> {
    protected V mView;
    protected Context context;
    protected Activity activity;

    public void attachView(V view) {
        this.mView = view;
        context = mView.getContext();
        activity = mView.getActivity();
    }

    //防止内存泄漏
    public void detachView() {
        this.mView = null;
    }

    public boolean isViewAttached() {
        return mView != null;
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    }
}
