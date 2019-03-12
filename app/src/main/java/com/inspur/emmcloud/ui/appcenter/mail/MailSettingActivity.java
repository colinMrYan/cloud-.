package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2019/1/9.
 */

@ContentView(R.layout.activity_mail_setting)
public class MailSettingActivity extends BaseActivity {
    @ViewInject(R.id.tv_mail_account)
    TextView mailAccountText;
    @ViewInject(R.id.tv_mail_password)
    TextView mailPasswrodText;
    @ViewInject(R.id.ibt_mail_password_visible)
    ImageButton mailPasswordVisibleImgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mail = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
        String password = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
        mailAccountText.setText(mail);
        mailPasswrodText.setText(password);
        mailPasswrodText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_setting_save:
                finish();
                break;
            case R.id.ibt_mail_password_visible:
                if (mailPasswrodText.getTransformationMethod() instanceof HideReturnsTransformationMethod) {
                    mailPasswrodText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mailPasswordVisibleImgBtn.setImageResource(R.drawable.app_edittext_eye_open);
                } else {
                    mailPasswrodText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mailPasswordVisibleImgBtn.setImageResource(R.drawable.app_edittext_eye_close);
                }
                break;
            case R.id.tv_mail_account_delete:
                PreferencesByUsersUtils.putString(MailSettingActivity.this, Constant.PREF_MAIL_ACCOUNT, "");
                PreferencesByUsersUtils.putString(MailSettingActivity.this, Constant.PREF_MAIL_PASSWORD, "");
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_ACCOUNT_DELETE, ""));
                IntentUtils.startActivity(MailSettingActivity.this, MailLoginActivity.class, true);
                break;
            case R.id.rl_mail_cert:
                IntentUtils.startActivity(this, MailCertificateInstallActivity.class);
                break;
        }

    }

}
