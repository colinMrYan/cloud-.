package com.inspur.emmcloud.util.privates;

import android.util.Base64;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chenmch on 2019/1/4.
 */

public class MailLoginUtils {
    private static MailLoginUtils mInstance;

    public static MailLoginUtils getInstance() {
        if (mInstance == null) {
            synchronized (OauthUtils.class) {
                if (mInstance == null) {
                    mInstance = new MailLoginUtils();
                }
            }
        }
        return mInstance;
    }

    public void loginMail() {
        String mail = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
        String password = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
        loginMail(mail, password);
    }

    public void loginMail(String mail, String password) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(),false) && !StringUtils.isBlank(mail) && !StringUtils.isBlank(password)) {
            String key = EncryptUtils.stringToMD5(mail);
            try {
                password = EncryptUtils.encode(password, key, Constant.MAIL_ENCRYPT_IV, Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MailApiService apiService = new MailApiService(MyApplication.getInstance());
            apiService.setAPIInterface(new WebServie());
            apiService.loginMail(mail, password);
        }else {
            SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_LOGIN_FAIL);
            EventBus.getDefault().post(simpleEventMessage);
        }
    }


    private class WebServie extends APIInterfaceInstance {
        @Override
        public void returnMailLoginSuccess() {
            SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_LOGIN_SUCCESS);
            EventBus.getDefault().post(simpleEventMessage);
        }

        @Override
        public void returnMailLoginFail(String error, int errorCode) {
            SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_LOGIN_FAIL,error);
            EventBus.getDefault().post(simpleEventMessage);
        }
    }
}
