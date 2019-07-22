package com.inspur.emmcloud.basemodule.ui;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.basemodule.mvp.BaseView;

public class BaseMvpActivity<T extends BasePresenter> extends BaseActivity implements BaseView {
    protected T mPresenter;

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void dismissLoading() {

    }

    @Override
    public void showError(String error, int responseCode) {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachView();
        }
        super.onDestroy();
    }
}
