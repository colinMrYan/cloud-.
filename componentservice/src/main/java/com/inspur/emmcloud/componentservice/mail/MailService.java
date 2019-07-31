package com.inspur.emmcloud.componentservice.mail;

import android.app.Activity;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by chenmch on 2019/7/8.
 */

public interface MailService extends CoreService {
    String getExchangeMailAccount();

    String getExchangeMailPassword();

    void exchangeLogin(Activity activity, OnExchangeLoginListener onExchangeLoginListener);

}
