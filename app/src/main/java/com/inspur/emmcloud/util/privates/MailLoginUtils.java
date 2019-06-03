package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.util.Base64;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.appcenter.mail.MailHomeActivity;
import com.inspur.emmcloud.ui.appcenter.mail.MailLoginActivity;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * Created by chenmch on 2019/1/4.
 */

public class MailLoginUtils {
    private Activity activity;
    private LoadingDialog loadingDlg;

    public void loginMail(Activity activity) {
        String mail = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
        String password = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
        loginMail(activity, mail, password);
    }

    public void loginMail(Activity activity, String mail, String password) {
        this.activity = activity;
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false) && !StringUtils.isBlank(mail) && !StringUtils.isBlank(password)) {
            loadingDlg = new LoadingDialog(activity);
            loadingDlg.show();
            String key = EncryptUtils.stringToMD5(mail);
            try {
                password = EncryptUtils.encode(password, key, Constant.MAIL_ENCRYPT_IV, Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MailApiService apiService = new MailApiService(MyApplication.getInstance());
            apiService.setAPIInterface(new WebServie());
            apiService.loginMail(mail, password);
        } else {
            IntentUtils.startActivity(activity, MailLoginActivity.class, true);
            //SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_LOGIN_FAIL);
            // EventBus.getDefault().post(simpleEventMessage);
        }
    }


    private class WebServie extends APIInterfaceInstance {
        @Override
        public void returnMailLoginSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            IntentUtils.startActivity(activity, MailHomeActivity.class, true);
//            SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_LOGIN_SUCCESS);
//            EventBus.getDefault().post(simpleEventMessage);
        }

        @Override
        public void returnMailLoginFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            IntentUtils.startActivity(activity, MailLoginActivity.class, true);
//            SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_LOGIN_FAIL,error);
//            EventBus.getDefault().post(simpleEventMessage);
        }
    }
}
