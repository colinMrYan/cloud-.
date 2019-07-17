package com.inspur.emmcloud.componentservice.mail;

/**
 * Created by chenmch on 2019/7/9.
 */

public interface OnExchangeLoginListener {
    void onMailLoginSuccess();

    void onMailLoginFail(String error, int errorCode);
}
