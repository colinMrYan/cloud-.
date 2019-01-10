package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2019/1/9.
 */

@ContentView(R.layout.activity_mail_setting)
public class MailSettingActivity extends BaseActivity {
    @ViewInject( R.id.rl_back)
    RelativeLayout mailSettingLayout;
    @ViewInject( R.id.ibt_mail_setting_password )
    ImageButton settingPasswordButton;
    @ViewInject( R.id.tv_setting_mail_password )
    TextView    passwordTextView;

    private boolean isSeePassword=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
    }


    private void initView(){

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.rl_setting_back:
                finish();
            break;
            case R.id.tv_save_setting:

            break;
            case R.id.ibt_mail_setting_password:
                LogUtils.LbcDebug( "mail setting password" );
             String mailPassWord = PreferencesByUsersUtils.getString( MailSettingActivity.this,Constant.MAIL_LOG_KEY,"");
             isSeePassword = isSeePassword?false:true;
             settingPasswordButton.setImageResource(isSeePassword?R.drawable.icon_mail_password_yes:R.drawable.icon_mail_password_no);
             passwordTextView.setText( isSeePassword?mailPassWord:"**********" );
             break;
            case R.id.tv_setting_mail_account_delect:
                PreferencesByUsersUtils.putString( MailSettingActivity.this, Constant.MAIL_LOG_ADDRESS,"");
                PreferencesByUsersUtils.putString( MailSettingActivity.this,Constant.MAIL_LOG_KEY,"");
                EventBus.getDefault().post( new SimpleEventMessage(Constant.EVENTBUS_TAG_DELECTE_MAIL_HOME_ACTIVITY, ""));
                IntentUtils.startActivity( MailSettingActivity.this,MailLoginActivity.class,true );
            break;
            case R.id.rl_certificate:
                IntentUtils.startActivity(this,MailCertificateInstallActivity.class);
            break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
