package com.inspur.emmcloud.componentservice.mail;

import android.app.Activity;

import com.inspur.emmcloud.componentservice.CoreService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

import java.util.ArrayList;

/**
 * Created by chenmch on 2019/7/8.
 */

public interface MailService extends CoreService {
    String getExchangeMailAccount();

    String getExchangeMailPassword();

    void exchangeLogin(Activity activity, OnExchangeLoginListener onExchangeLoginListener);

    ContactUser getContactUserByUidOrEmail(boolean isEmail, String uidOrMail);

    void startContactSearchActivityForResult(Activity activity, int type, ArrayList<String> memberUidList, boolean multiSelect, String title, int qequestCode);

}
