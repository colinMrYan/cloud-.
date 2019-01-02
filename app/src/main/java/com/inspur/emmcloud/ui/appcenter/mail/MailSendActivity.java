package com.inspur.emmcloud.ui.appcenter.mail;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.mail.MailRecipientModel;
import com.inspur.emmcloud.bean.chat.InsertModel;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.RichEdit;

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

    @ViewInject(R.id.iv_recipients)
    private ImageView recipientsImageView;
    @ViewInject(R.id.iv_cc_recipients)
    private ImageView ccRecipientsImageView;

    private ArrayList<String> memberUidList = new ArrayList<>();
    private ArrayList<MailRecipientModel> recipientList = new ArrayList<>();
    private ArrayList<MailRecipientModel> mCCRecipients = new ArrayList<>();


    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_CC_MEMBER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        init();
    }

    /**
     * 初始化
     * */
    private void init() {
        recipientRichEdit.setInputWatcher( new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String  data ;
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()>1&&' '==s.charAt(s.length()-1)){
                    recipientRichEdit.insertLastManualData(1);
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
                if(hasFocus){
                }else{
                     recipientRichEdit.insertLastManualData(-1);
                }
            }
        });

        recipientRichEdit.setInsertModelListWatcher( new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
                     synchronousRemoveRecipients( recipientList,insertModelList );
                     synchronousAddRecipients( recipientList,insertModelList );
                     LogUtils.LbcDebug( "当前："+recipientList.size() );
            }
        });

        ccRecipientRichEdit.setInputWatcher( new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()>1&&' '==s.charAt(s.length()-1)){
                    ccRecipientRichEdit.insertLastManualData(1);
                }
            }
        } );

        ccRecipientRichEdit.setInsertModelListWatcher( new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
                synchronousRemoveRecipients( mCCRecipients,insertModelList );
                synchronousAddRecipients( mCCRecipients,insertModelList );
            }
        } );
        ccRecipientRichEdit.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                }else{
                    ccRecipientRichEdit.insertLastManualData(-1);
                }
            }
        });

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send_mail:
                break;
            case R.id.iv_recipients:
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
                Intent intent1 = new Intent();
                intent1.putExtra( ContactSearchFragment.EXTRA_TYPE, 2 );
                intent1.putExtra( ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList );
                intent1.putExtra( ContactSearchFragment.EXTRA_MULTI_SELECT, true );
                intent1.putExtra( ContactSearchFragment.EXTRA_TITLE, "添加抄送人" );
                intent1.setClass( getApplicationContext(),
                        ContactSearchActivity.class );
                startActivityForResult( intent1, QEQUEST_CC_MEMBER );
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
                            singleRecipient.setmRecipientEmail( contactUser.getEmail() );
                            singleRecipient.setmRecipientName( contactUser.getName() );
                            LogUtils.LbcDebug( singleRecipient.getmRecipientEmail() );
                            boolean isContaion = isListContaionSpecItem( recipientList, singleRecipient );
                            if (!isContaion) {
                                recipientList.add( singleRecipient );
                                notifyRichEdit(recipientRichEdit, singleRecipient);
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
                            singleRecipient.setmRecipientEmail( contactUser.getEmail() );
                            singleRecipient.setmRecipientName( contactUser.getName() );
                            LogUtils.LbcDebug( singleRecipient.getmRecipientEmail() );
                            boolean isContaion = isListContaionSpecItem( mCCRecipients, singleRecipient );
                            if (!isContaion) {
                                mCCRecipients.add( singleRecipient );
                                notifyRichEdit(ccRecipientRichEdit, singleRecipient);
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
     *选择去重
     * @param selectMemList
     * @param specItem */
    private boolean isListContaionSpecItem(ArrayList<MailRecipientModel> selectMemList, MailRecipientModel specItem) {
        for (int i = 0; i < selectMemList.size(); i++) {
            if (selectMemList.get( i ).getmRecipientEmail().equals( specItem.getmRecipientEmail() )) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新RichEdit
     * @param mRecipients
     * @param richEdit*/
    private void  notifyRichEdit( RichEdit richEdit,   MailRecipientModel mRecipients){
            String name = mRecipients.getmRecipientName();
            String email = mRecipients.getmRecipientEmail();
            InsertModel  insertModel = new InsertModel(";", (System.currentTimeMillis()) + "", name, email);
            richEdit.insertSpecialStr(false, insertModel);
    }

    /**
     *Recipient 根据insertModels 多的删掉*/
    private void synchronousRemoveRecipients(ArrayList<MailRecipientModel>recipients,List<InsertModel> insertModels){
        for(int i=0;i<recipients.size();i++){
            String email = recipients.get(i).getmRecipientEmail();
            boolean haveEmail=false;
            for(int j=0;j<insertModels.size();j++){
                if(email.equals(insertModels.get( j ).getInsertContentId())){
                    haveEmail=true;
                    break;
                }
            }
            if(!haveEmail){
                recipients.remove( i );
            }
        }
    }

    /**
     *Re 根据InsertModel少的添加
     **/
    private void   synchronousAddRecipients(ArrayList<MailRecipientModel>recipients,List<InsertModel> insertModels){
        for(int m=0;m<insertModels.size();m++){
            String email = insertModels.get(m).getInsertContentId().toString();
            boolean haveEmail=false;
            for(int n=0;n<recipients.size();n++){
                if(email.equals(recipients.get(n).getmRecipientEmail())){
                    haveEmail=true;
                    break;
                }
            }
            if(!haveEmail){
                MailRecipientModel mailRecipientModel = new MailRecipientModel();
                mailRecipientModel.setmRecipientEmail( email );
                mailRecipientModel.setmRecipientName( email );
                recipients.add(mailRecipientModel);
            }
        }

    }
}
