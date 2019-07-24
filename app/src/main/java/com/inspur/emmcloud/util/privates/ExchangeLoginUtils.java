package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.util.Base64;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;

/**
 * 封装出的Exchange账户登录类
 */
public class ExchangeLoginUtils {
    private LoadingDialog loadingDlg;
    private Activity activity;
    private OnExchangeLoginListener onExchangeLoginListener;
    private String exchangeAccount;
    private String exchangePassword;
    private boolean isShowLoadingDlg;

    private ExchangeLoginUtils(Builder builder) {
        activity = builder.activity;
        onExchangeLoginListener = builder.onExchangeLoginListener;
        exchangeAccount = builder.exchangeAccount;
        exchangePassword = builder.exchangePassword;
        isShowLoadingDlg = builder.isShowLoadingDlg;
        if (StringUtils.isBlank(exchangeAccount)) {
            exchangeAccount = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
        }
        if (StringUtils.isBlank(exchangePassword)) {
            exchangePassword = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
        } else {
            String key = EncryptUtils.stringToMD5(exchangeAccount);
            try {
                exchangePassword = EncryptUtils.encode(exchangePassword, key, Constant.MAIL_ENCRYPT_IV, Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void login() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false) && !StringUtils.isBlank(exchangeAccount) && !StringUtils.isBlank(exchangePassword)) {
            loadingDlg = new LoadingDialog(activity);
            loadingDlg.show(isShowLoadingDlg);
            MailApiService apiService = new MailApiService(MyApplication.getInstance());
            apiService.setAPIInterface(new WebService());
            apiService.loginMail(exchangeAccount, exchangePassword);
        } else {
            callbackLoginFail("", -1);
        }
    }

    private void callbackLoginSuccess() {
        if (isShowLoadingDlg) {
            LoadingDialog.dimissDlg(loadingDlg);
        }
        if (onExchangeLoginListener != null) {
            onExchangeLoginListener.onMailLoginSuccess();
        }
    }

    private void callbackLoginFail(String error, int errorCode) {
        if (isShowLoadingDlg) {
            LoadingDialog.dimissDlg(loadingDlg);
        }
        if (onExchangeLoginListener != null) {
            onExchangeLoginListener.onMailLoginFail(error, errorCode);
        }
    }

    public static class Builder {
        private Activity activity;
        private OnExchangeLoginListener onExchangeLoginListener;
        private String exchangeAccount;
        private String exchangePassword;
        private boolean isShowLoadingDlg;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setOnExchangeLoginListener(OnExchangeLoginListener onExchangeLoginListener) {
            this.onExchangeLoginListener = onExchangeLoginListener;
            return this;
        }

        public Builder setExchangeLoginAccount(String exchangeAccount, String exchangePassword) {
            this.exchangeAccount = exchangeAccount;
            this.exchangePassword = exchangePassword;
            return this;
        }

        public Builder setShowLoadingDlg(boolean isShowLoadingDlg) {
            this.isShowLoadingDlg = isShowLoadingDlg;
            return this;
        }

        public ExchangeLoginUtils build() {
            return new ExchangeLoginUtils(this);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailLoginSuccess() {
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, exchangeAccount);
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, exchangePassword);
            callbackLoginSuccess();

        }

        @Override
        public void returnMailLoginFail(String error, int errorCode) {
            callbackLoginFail(error, errorCode);

        }
    }
}
