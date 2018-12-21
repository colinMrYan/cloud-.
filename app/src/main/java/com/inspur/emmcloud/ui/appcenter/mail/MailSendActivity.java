package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView( R.layout.activity_mail_send )
public class MailSendActivity extends BaseActivity {
    @ViewInject( R.id.et_recipient)
    private EditText mRecipientEditText;
    @ViewInject( R.id.et_copy_to_recipient )
    private EditText mCopyToEditText;
    @ViewInject( R.id.et_sender_theme )
    private  EditText mSendThemeEditText;

    @ViewInject( R.id.iv_recipients )
    private ImageView mRecipientsImageView;
    @ViewInject( R.id.iv_copy_to_recipients)
    private  ImageView mCopy2RecipientsImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.tv_send_mail:
                break;
            case R.id.iv_recipients:
                break;
            case R.id.iv_copy_to_recipients:
                break;
            default:
                break;
        }
    }


}
