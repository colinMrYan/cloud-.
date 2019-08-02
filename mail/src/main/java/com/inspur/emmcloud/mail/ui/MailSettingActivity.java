package com.inspur.emmcloud.mail.ui;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.R2;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/1/9.
 */

public class MailSettingActivity extends BaseActivity {
    @BindView(R2.id.tv_mail_account)
    TextView mailAccountText;
    @BindView(R2.id.tv_mail_password)
    TextView mailPasswrodText;
    @BindView(R2.id.ibt_mail_password_visible)
    ImageButton mailPasswordVisibleImgBtn;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        ContactService contactService = Router.getInstance().getService(ContactService.class);
        if (contactService != null) {

            String mail = PreferencesByUsersUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
            String password = PreferencesByUsersUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
            mailAccountText.setText(mail);
            mailPasswrodText.setText(password);
            mailPasswrodText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_mail_setting;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.tv_setting_save) {
            finish();

        } else if (i == R.id.ibt_mail_password_visible) {
            if (mailPasswrodText.getTransformationMethod() instanceof HideReturnsTransformationMethod) {
                mailPasswrodText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mailPasswordVisibleImgBtn.setImageResource(R.drawable.app_edittext_eye_open);
            } else {
                mailPasswrodText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                mailPasswordVisibleImgBtn.setImageResource(R.drawable.app_edittext_eye_close);
            }

        } else if (i == R.id.tv_mail_account_delete) {
            PreferencesByUsersUtils.putString(MailSettingActivity.this, Constant.PREF_MAIL_ACCOUNT, "");
            PreferencesByUsersUtils.putString(MailSettingActivity.this, Constant.PREF_MAIL_PASSWORD, "");
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_ACCOUNT_DELETE, ""));
            IntentUtils.startActivity(MailSettingActivity.this, MailLoginActivity.class, true);

        } else if (i == R.id.rl_mail_cert) {
            IntentUtils.startActivity(this, MailCertificateInstallActivity.class);

        }

    }

}
