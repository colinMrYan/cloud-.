package com.inspur.emmcloud.ui.appcenter.mail;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.bean.appcenter.mail.Mail;
import com.inspur.emmcloud.bean.appcenter.mail.MailCertificateDetail;
import com.inspur.emmcloud.bean.appcenter.mail.MailRecipient;
import com.inspur.emmcloud.bean.appcenter.mail.MailRecipientModel;
import com.inspur.emmcloud.bean.appcenter.mail.MailSend;
import com.inspur.emmcloud.bean.chat.InsertModel;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MailCacheUtils;
import com.inspur.emmcloud.widget.NoScrollWebView;
import com.inspur.emmcloud.widget.RichEdit;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView(R.layout.activity_mail_send)
public class MailSendActivity extends BaseActivity {
    @ViewInject(R.id.richedit_recipients)
    private RichEdit recipientRichEdit;
    @ViewInject(R.id.richedit_cc_recipient)
    private RichEdit ccRecipientRichEdit;
    @ViewInject(R.id.et_theme_send)
    private EditText sendThemeEditText;
    @ViewInject(R.id.tv_recipients_show)
    private TextView recipientsShowText;
    @ViewInject(R.id.tv_cc_recipient_show)
    private TextView ccRecipientsShowText;
    @ViewInject( R.id.et_content_send )
    private EditText contentSendEditText;
    @ViewInject(R.id.iv_fw_tip)
    private ImageView fwTipImageView;
    @ViewInject(R.id.rl_fw_body)
    private RelativeLayout fwBodyLayout;
    @ViewInject(R.id.cb_fw_body)
    private CheckBox fwBodyCheckBox;
    @ViewInject(R.id.noscrollwebview_fw_body)
    private NoScrollWebView fwBodyWebView;
    @ViewInject(R.id.tv_header_title)
    private TextView headerTitleText;

    @ViewInject(R.id.iv_recipients)
    private ImageView recipientsImageView;
    @ViewInject(R.id.iv_cc_recipients)
    private ImageView ccRecipientsImageView;

    private ArrayList<String> memberUidList = new ArrayList<>();
    private ArrayList<MailRecipientModel> recipientList = new ArrayList<>();
    private ArrayList<MailRecipientModel> ccRecipientList = new ArrayList<>();
    public static final String MODEL_NEW = "mail_new";
    public static final String MODEL_REPLY = "mail_replay";
    public static final String MODEL_REPLY_ALL = "mail_replay_ALL";
    public static final String MODEL_FORWARD = "mail_forward";
    public static final String EXTRA_MAIL_ID = "extra_mail_id";
    public static final String EXTRA_MAIL_MODEL = "extra_mail_model";
    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_CC_MEMBER = 3;

    private MailCertificateDetail myCertificate;
    private String sendMailAddress;
    private boolean isForward =false;
    private boolean isReply   =false;
    boolean isHaveOriginalMail =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        recipientRichEdit.setInputWatcher( new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String data;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 1 && ' ' == s.charAt( s.length() - 1 )) {
                    recipientRichEdit.insertLastManualData( 1 );
                }
            }
        } );
        recipientRichEdit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        } );
        recipientRichEdit.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    recipientRichEdit.insertLastManualData( -1 );
                }
            }
        });

        recipientRichEdit.setInsertModelListWatcher( new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
                synchronousRemoveRecipients( recipientList, insertModelList );
                synchronousAddRecipients( recipientList, insertModelList );
            }
        } );

        ccRecipientRichEdit.setInputWatcher( new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 1 && ' ' == s.charAt( s.length() - 1 )) {
                    ccRecipientRichEdit.insertLastManualData( 1 );
                }
            }
        } );

        ccRecipientRichEdit.setInsertModelListWatcher( new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
                synchronousRemoveRecipients( ccRecipientList, insertModelList );
                synchronousAddRecipients( ccRecipientList, insertModelList );
            }
        } );
        ccRecipientRichEdit.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    ccRecipientRichEdit.insertLastManualData( -1 );
                }
            }
        } );

        recipientsShowText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipientsShowText.setVisibility( View.GONE );
                recipientRichEdit.setVisibility( View.VISIBLE );
            }
        } );
        ccRecipientsShowText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ccRecipientRichEdit.setVisibility( View.VISIBLE );
                ccRecipientsShowText.setVisibility( View.GONE );
            }
        } );

        fwBodyCheckBox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    fwBodyWebView.setVisibility( View.VISIBLE );
                } else {
                    fwBodyWebView.setVisibility( View.GONE );
                }
            }
        } );
        Bundle mailBundle = getIntent().getExtras();
        if (null != mailBundle) {
            String replyMailMode = mailBundle.getString( EXTRA_MAIL_MODEL );
            String replyMailId = mailBundle.getString( EXTRA_MAIL_ID );
            Mail replayMail = MailCacheUtils.getMail( replyMailId );
            if (null != replayMail) {
            List<MailRecipient> mailRecipientList = new ArrayList<>();
            switch (replyMailMode) {
                case MODEL_NEW:
                    headerTitleText.setText( "发邮件" );

                    break;
                case MODEL_REPLY:
                    String string = JSON.toJSONString( ((Object) replayMail) );
                    LogUtils.LbcDebug( "JSonData:" + string );

                    mailRecipientList.clear();
                    mailRecipientList.add( replayMail.getFromMailRecipient() );
                    insertReciversFromExtra( recipientRichEdit, mailRecipientList, recipientList );

                    sendThemeEditText.setText( replayMail.getSubject().toString() );
                    headerTitleText.setText( "回复" );
                    isReply = true;
                    isHaveOriginalMail = true;
                    break;
                case MODEL_REPLY_ALL:
                    mailRecipientList.clear();
                    mailRecipientList.add( replayMail.getFromMailRecipient() );
                    insertReciversFromExtra( recipientRichEdit, mailRecipientList, recipientList );

                    mailRecipientList.clear();
                    mailRecipientList = replayMail.getToMailRecipientList();
                    insertReciversFromExtra( recipientRichEdit, mailRecipientList, recipientList );
                    recipientsShowText.setText( recipientRichEdit.getText().toString() );
                    recipientRichEdit.setVisibility( View.GONE );
                    recipientsShowText.setVisibility( View.VISIBLE );

                    mailRecipientList.clear();
                    mailRecipientList = replayMail.getCcMailRecipientList();
                    insertReciversFromExtra( ccRecipientRichEdit, mailRecipientList, ccRecipientList );
                    ccRecipientsShowText.setText( ccRecipientRichEdit.getText().toString() );
                    ccRecipientRichEdit.setVisibility( View.GONE );
                    ccRecipientsShowText.setVisibility( View.VISIBLE );

                    sendThemeEditText.setText( replayMail.getSubject().toString() );

                    isReply = true;
                    isHaveOriginalMail = true;
                    break;
                case MODEL_FORWARD:

                    sendThemeEditText.setText( replayMail.getSubject().toString() );
                    isForward = true;
                    isHaveOriginalMail = true;
                    break;
            }

                String mailBodyText = replayMail.getBodyText();
                if (!StringUtils.isBlank(mailBodyText)) {
                    WebSettings webSettings = fwBodyWebView.getSettings();
                    webSettings.setUseWideViewPort(true);
                    webSettings.setJavaScriptEnabled(true);
                    webSettings.setSupportZoom(true);
                    webSettings.setBuiltInZoomControls(true);
                    webSettings.setDisplayZoomControls(false);
                    webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                    webSettings.setLoadWithOverviewMode(true);
                    webSettings.setDefaultFontSize(40);
                    webSettings.setMinimumFontSize(40);
                    fwBodyWebView.loadDataWithBaseURL(null,mailBodyText, "text/html", "utf-8",null);
                    fwTipImageView.setVisibility( isHaveOriginalMail?View.VISIBLE:View.GONE );
                }
            }
        }

        Object object = PreferencesByUsersUtils.getObject( this, MailCertificateInstallActivity.CERTIFICATER_KEY );
        if (null == object) {
            myCertificate = new MailCertificateDetail();
            myCertificate.setEncryptedMail( false );
            myCertificate.setSignedMail( false );
        } else {
            myCertificate = (MailCertificateDetail) object;
        }




    }


    /**
     *为邮件设置加密加签提示*/


    /**
     * 转发、回复等插入收件人
     **/
    private void insertReciversFromExtra(RichEdit richRdit, List<MailRecipient> mailRecipients, ArrayList<MailRecipientModel> recipientsList) {
        for (int i = 0; i < mailRecipients.size(); i++) {
            MailRecipientModel reciver = new MailRecipientModel();
            reciver.setName( mailRecipients.get( i ).getName() );
            reciver.setAddress( mailRecipients.get( i ).getAddress() );
            boolean isContaion = isListContaionSpecItem( recipientsList, reciver );
            if (!isContaion) {
                recipientsList.add( reciver );
                notifyRichEdit( richRdit, reciver, i );
            }
        }
    }

    /**
     *发送邮件 数据准备*/
    private MailSend prepareSendData(){
     MailSend mail = new MailSend();
     mail.setBody(contentSendEditText.getText().toString());
     mail.setToRecipients(recipientList);
     mail.setCcRecipients(ccRecipientList);
     String mailSenderAddess = PreferencesByUsersUtils.getString(MyApplication.getInstance(),Constant.PREF_MAIL_ACCOUNT);
     LogUtils.LbcDebug( "mailSenderAddress"+mailSenderAddess );
     ContactUser contactUser =ContactUserCacheUtils.getContactUserByEmail( mailSenderAddess );
     mail.setFrom(new MailRecipientModel(contactUser.getName(),contactUser.getEmail()));
     mail.setNeedEncrypt(myCertificate.isEncryptedMail());
     mail.setNeedSign(myCertificate.isSignedMail());
     mail.setOriginalMail("");
     mail.setSubject(StringUtils.isBlank( sendThemeEditText.getText().toString())?"":sendThemeEditText.getText().toString());
     mail.setIsForward(isForward);
     mail.setIsReply( isReply );
     return mail;
    }

    /**
     *发邮件时提醒*/
    private void noSignOrEncryptHintDialog(){
        if(!myCertificate.isSignedMail()||!myCertificate.isEncryptedMail()){
            String signedMail = myCertificate.isSignedMail()?"":"加签";
            String encryptMail= myCertificate.isEncryptedMail()?"":"加密";
            String mailHint   = "该邮件未"+(myCertificate.isSignedMail()?"":"加签")+(myCertificate.isEncryptedMail()?"":"加密")+"确定发送？";
            new MyQMUIDialog.MessageDialogBuilder(MailSendActivity.this)
                    .setMessage(mailHint)
                    .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            try {
                                sendMail();
                            } catch (Exception e) {
                                LogUtils.LbcDebug( "Error" );
                                e.printStackTrace();
                            }
                            dialog.dismiss();

                        }
                    })
                    .show();
        }else{
            try {
                sendMail();
            } catch (Exception e) {
                LogUtils.LbcDebug( "Error" );
                e.printStackTrace();
            }
        }
    }

    /**
     *发送邮件*/
    private void sendMail() throws Exception {
        MailSend mailSend = prepareSendData();
        String   jsonMail = JSON.toJSONString(mailSend);
        String key = EncryptUtils.stringToMD5(mailSend.getFrom().getAddress().toString());
        byte[] mailContent = EncryptUtils.encodeNoBase64(jsonMail, key,Constant.MAIL_ENCRYPT_IV);
        if (NetUtils.isNetworkConnected( this )) {
            LogUtils.LbcDebug( "准备发送邮件" );
            MailApiService apiService = new MailApiService( this );
            apiService.setAPIInterface( new WebService() );
            apiService.sendEncryptMail(mailContent);
        }
    }

    /**
     *点击事件*/
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send_mail:
                if (recipientList.size() == 0) {
                    //showxx();
                    return;
                }
                noSignOrEncryptHintDialog();

                break;
            case R.id.iv_recipients:
                recipientRichEdit.insertLastManualData( 0 );
                Intent intent = new Intent();
                intent.putExtra( ContactSearchFragment.EXTRA_TYPE, 2 );
                intent.putExtra( ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList );
                intent.putExtra( ContactSearchFragment.EXTRA_MULTI_SELECT, true );
                intent.putExtra( ContactSearchFragment.EXTRA_TITLE, "添加收件人" );

                intent.setClass( getApplicationContext(),
                        ContactSearchActivity.class );
                startActivityForResult( intent, QEQUEST_ADD_MEMBER );
                break;
            case R.id.iv_cc_recipients:
                ccRecipientRichEdit.insertLastManualData( 0 );
                Intent intent1 = new Intent();
                intent1.putExtra( ContactSearchFragment.EXTRA_TYPE, 2 );
                intent1.putExtra( ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList );
                intent1.putExtra( ContactSearchFragment.EXTRA_MULTI_SELECT, true );
                intent1.putExtra( ContactSearchFragment.EXTRA_TITLE, "添加抄送人" );
                intent1.setClass( getApplicationContext(),
                        ContactSearchActivity.class );
                startActivityForResult( intent1, QEQUEST_CC_MEMBER );
                break;
            case R.id.iv_fw_tip:
                fwTipImageView.setVisibility( View.GONE );
                fwBodyLayout.setVisibility( View.VISIBLE );
                break;
            case R.id.rl_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case QEQUEST_ADD_MEMBER:
                    String NameEmails = "";
                    List<SearchModel> addMemberList = (List<SearchModel>) data
                            .getSerializableExtra( "selectMemList" );
                    if (addMemberList.size() > 0) {
                        for (int i = 0; i < addMemberList.size(); i++) {
                            MailRecipientModel singleRecipient = new MailRecipientModel();
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid( addMemberList.get( i ).getId() );
                            singleRecipient.setAddress( contactUser.getEmail() );
                            singleRecipient.setName( contactUser.getName() );
                            boolean isContaion = isListContaionSpecItem( recipientList, singleRecipient );
                            if (!isContaion) {
                                recipientList.add( singleRecipient );
                                notifyRichEdit( recipientRichEdit, singleRecipient, i );
                            }
                        }
                    }
                    break;
                case QEQUEST_CC_MEMBER:
                    List<SearchModel> ctAddMemberList = (List<SearchModel>) data
                            .getSerializableExtra( "selectMemList" );
                    if (ctAddMemberList.size() > 0) {
                        for (int i = 0; i < ctAddMemberList.size(); i++) {
                            MailRecipientModel singleRecipient = new MailRecipientModel();
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid( ctAddMemberList.get( i ).getId() );
                            singleRecipient.setAddress( contactUser.getEmail() );
                            singleRecipient.setName( contactUser.getName() );
                            boolean isContaion = isListContaionSpecItem( ccRecipientList, singleRecipient );
                            if (!isContaion) {
                                ccRecipientList.add( singleRecipient );
                                notifyRichEdit( ccRecipientRichEdit, singleRecipient, i );
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 选择去重
     * @param selectMemList
     * @param specItem
     */
    private boolean isListContaionSpecItem(ArrayList<MailRecipientModel> selectMemList, MailRecipientModel specItem) {
        for (int i = 0; i < selectMemList.size(); i++) {
            if (selectMemList.get( i ).getAddress().equals( specItem.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新RichEdit
     *
     * @param mRecipients
     * @param richEdit
     */
    private void notifyRichEdit(RichEdit richEdit, MailRecipientModel mRecipients, int subId) {
        InsertModel insertModel = new InsertModel( "； ", (System.currentTimeMillis()) + "" + subId, mRecipients.getName(), mRecipients.getAddress());
        richEdit.insertSpecialStr( false, insertModel );
    }

    /**
     * Recipient 根据insertModels 多的删掉
     */
    private void synchronousRemoveRecipients(ArrayList<MailRecipientModel> recipients, List<InsertModel> insertModels) {
        for (int i = 0; i < recipients.size(); i++) {
            String email = recipients.get( i ).getAddress();
            boolean haveEmail = false;
            for (int j = 0; j < insertModels.size(); j++) {
                if (email.equals( insertModels.get( j ).getInsertContentId() )) {
                    haveEmail = true;
                    break;
                }
            }
            if (!haveEmail) {
                recipients.remove( i );
            }
        }
    }

    /**
     * Re 根据InsertModel少的添加
     **/
    private void synchronousAddRecipients(ArrayList<MailRecipientModel> recipients, List<InsertModel> insertModels) {
        for (int m = 0; m < insertModels.size(); m++) {
            String address = insertModels.get( m ).getInsertContentId().toString();
            String name = insertModels.get( m ).getInsertContent().toString();
            LogUtils.LbcDebug( "address:"+address+"name:"+name );
            boolean haveEmail = false;
            for (int n = 0; n < recipients.size(); n++) {
                if (address.equals( recipients.get(n).getAddress())) {
                    haveEmail = true;
                    break;
                }
            }
            if (!haveEmail) {
                MailRecipientModel mailRecipientModel = new MailRecipientModel();
                mailRecipientModel.setAddress(address);
                mailRecipientModel.setName(name);
                recipients.add( mailRecipientModel );
            }
        }

    }

    /**
     * 网络通信类
     */
    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailCertificateUploadSuccess(byte[] arg0) {
             LogUtils.LbcDebug( "发送邮件成功:"+arg0.toString());
            ToastUtils.show( MailSendActivity.this,"恭喜 ，发送邮件成功了" );
             super.returnMailCertificateUploadSuccess(arg0);
        }

        @Override
        public void returnMailCertificateUploadFail(String error, int errorCode) {
            LogUtils.LbcDebug( "发送邮件失败"+error );
            Toast.makeText( MailSendActivity.this,"真糟糕，发送失败",Toast.LENGTH_SHORT );
            super.returnMailCertificateUploadFail( error, errorCode );
        }
    }


}
