package com.inspur.emmcloud.componentservice.mail;

import android.app.Activity;

import com.inspur.emmcloud.componentservice.CoreService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

/**
 * Created by chenmch on 2019/7/8.
 */

public interface MailService extends CoreService {
    String getExchangeMailAccount();

    String getExchangeMailPassword();

    void exchangeLogin(Activity activity, OnExchangeLoginListener onExchangeLoginListener);

    ContactUser getContactUserByUidOrEmail(boolean isEmail, String uidOrMail);

}
