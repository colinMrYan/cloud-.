package com.inspur.emmcloud.mail.serviceimpl;

import android.app.Activity;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.componentservice.mail.MailService;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;
import com.inspur.emmcloud.mail.api.MailAPIInterfaceImpl;
import com.inspur.emmcloud.mail.util.ExchangeLoginUtils;

/**
 * Created by libaochao on 2019/7/22.
 */

public class MailServiceImpl extends MailAPIInterfaceImpl implements MailService {
    @Override
    public String getExchangeMailAccount() {
        return null;
    }

    @Override
    public String getExchangeMailPassword() {
        return null;
    }

    @Override
    public void exchangeLogin(Activity activity, OnExchangeLoginListener onExchangeLoginListener) {
        LogUtils.LbcDebug(" exchangeLogin(Activity activity88888888");
        new ExchangeLoginUtils.Builder(activity)
                .setShowLoadingDlg(true)
                .setOnExchangeLoginListener(onExchangeLoginListener).build().login();
    }
}
