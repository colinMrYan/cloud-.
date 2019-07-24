package com.inspur.emmcloud.servcieimpl;

import android.app.Activity;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.componentservice.mail.MailService;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;
import com.inspur.emmcloud.util.privates.ExchangeLoginUtils;

/**
 * Created by chenmch on 2019/7/9.
 */

public class MailServiceImpl implements MailService {
    @Override
    public String getExchangeMailAccount() {
        return PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
    }

    @Override
    public String getExchangeMailPassword() {
        return PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
    }

    @Override
    public void exchangeLogin(Activity activity, OnExchangeLoginListener onExchangeLoginListener) {
        new ExchangeLoginUtils.Builder(activity)
                .setShowLoadingDlg(true)
                .setOnExchangeLoginListener(onExchangeLoginListener).build().login();
    }
}
