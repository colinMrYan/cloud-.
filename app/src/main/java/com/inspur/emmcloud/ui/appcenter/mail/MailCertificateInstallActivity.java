package com.inspur.emmcloud.ui.appcenter.mail;

import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView(R.layout.activity_mail_certificate_install)
public class MailCertificateInstallActivity extends BaseActivity {
    @ViewInject( R.id.tv_install_certificate)
    private TextView mInstallCertificateTV;
    @ViewInject( R.id.back_btn)
    private ImageView mBackImageView;
    @ViewInject( R.id.tv_certificate_use)
    private TextView mCertificateUseTV;
    @ViewInject( R.id.tv_installed_certificate_title)
    private TextView mInstalledCerTitleTV;
    @ViewInject( R.id.tv_certificate_used_name)
    private TextView mCertificateUseNameTV;
    @ViewInject( R.id.tv_certificate_giver_id)
    private TextView mCertificateGiverIdTV;
    @ViewInject( R.id.tv_certificate_expirty_data)
    private TextView mCertificateExpirtyDataTV;
    @ViewInject( R.id.s_secrity_action1)
    private Switch mSecrity_Action1;
    @ViewInject( R.id.s_secrity_action2)
    private Switch mSecrity_Action2;


    @ViewInject( R.id.tv_new_certificate_title)
    private TextView mNewCertificateTitleTV;
    @ViewInject( R.id.tv_new_certificate_user)
    private TextView mNewCertificateUserTV;
    @ViewInject( R.id.tv_new_certificate_publisher)
    private TextView mNewCertificatePublisherTV;
    @ViewInject( R.id.tv_new_certificate_expirty_data)
    private TextView mNewCertificateExpirtyDataTV;







}
