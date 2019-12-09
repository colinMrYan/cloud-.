package com.inspur.emmcloud.baselib.util.toast;

import android.app.Application;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/ToastUtils
 * time   : 2018/11/02
 * desc   : Toast 无通知栏权限兼容
 */
final class SupportToast extends BaseToast {

    private boolean isShowSuccess = true;
    // 吐司弹窗显示辅助类
    private final ToastHelper mToastHelper;

    SupportToast(Application application) {
        super(application);
        mToastHelper = new ToastHelper(this, application);
    }

    @Override
    public void show() {
        // 显示吐司
        isShowSuccess = mToastHelper.show();
    }

    @Override
    public void cancel() {
        // 取消显示
        mToastHelper.cancel();
    }

    public boolean isShowSuccess() {
        return isShowSuccess;
    }

    public void setShowSuccess(boolean showSuccess) {
        isShowSuccess = showSuccess;
    }
}