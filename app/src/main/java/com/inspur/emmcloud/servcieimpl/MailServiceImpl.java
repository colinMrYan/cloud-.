package com.inspur.emmcloud.servcieimpl;

import android.app.Activity;
import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.componentservice.mail.MailService;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.ExchangeLoginUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

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

    @Override
    public ContactUser getContactUserByUidOrEmail(boolean isEmail, String uidOrMail) {
        ContactUser contactUser = isEmail ? ContactUserCacheUtils.getContactUserByEmail(uidOrMail) :
                ContactUserCacheUtils.getContactUserByUid(uidOrMail);
        return contactUser;
    }

    @Override
    public void startContactSearchActivityForResult(Activity activity, int type, ArrayList<String> memberUidList, boolean multiSelect, String title, int questCode) {
        Intent intent = new Intent();
        intent.putExtra(ContactSearchFragment.EXTRA_TYPE, type);
        intent.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList);
        intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
        intent.putExtra(ContactSearchFragment.EXTRA_TITLE, title);

        intent.setClass(activity, ContactSearchActivity.class);
        startActivityForResult(activity, intent, questCode, null);
    }
}
