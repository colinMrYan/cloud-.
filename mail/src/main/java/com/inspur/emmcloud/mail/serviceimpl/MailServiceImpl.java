package com.inspur.emmcloud.mail.serviceimpl;

import android.app.Activity;

import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.componentservice.mail.MailService;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;
import com.inspur.emmcloud.mail.api.MailAPIInterfaceImpl;

import java.util.ArrayList;

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

    }

    @Override
    public ContactUser getContactUserByUidOrEmail(boolean isEmail, String uidOrMail) {
        return null;
    }

    @Override
    public void startContactSearchActivityForResult(Activity context, int type, ArrayList<String> memberUidList, boolean multiSelect, String title, int qequestCode) {

    }
}
