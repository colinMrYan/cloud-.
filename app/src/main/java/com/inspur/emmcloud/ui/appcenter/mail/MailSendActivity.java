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
    @ViewInject(R.id.re_recipient)
    private RichEdit mRecipientRichEdit;
    @ViewInject(R.id.re_cc_recipient)
    private RichEdit mCCRecipientRichEdit;
//    @ViewInject(R.id.fl_recipient)
//    private FlowLayout mRecipientFlowLayout;
//    @ViewInject(R.id.fl_copy_to_recipient)
//    private FlowLayout mCopyToFlowLayout;
    @ViewInject(R.id.et_sender_theme)
    private EditText mSendThemeEditText;

    @ViewInject(R.id.iv_recipients)
    private ImageView mRecipientsImageView;
    @ViewInject(R.id.iv_cc_recipients)
    private ImageView mCCRecipientsImageView;

//    private EditText searchEdit;
//    private EditText ctSearchEdit;
//
      private ArrayList<String> memberUidList = new ArrayList<>();
      private ArrayList<MailRecipientModel> mRecipients = new ArrayList<>();
      private ArrayList<MailRecipientModel> mCCRecipients = new ArrayList<>();
//    private String searchText;
//    private MyTextWatcher myTextWatcher;
//    private MyCTTextWatcher myCTTextWatcher;

    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_CC_MEMBER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        init();
    }

    /***/
    private void init() {
        mRecipientRichEdit.setInputWatcher( new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String  data ;

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        } );
        mRecipientRichEdit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //  InsertModel data =  mRecipientRichEdit.insertHandData();
//               if(data==null){
//                   LogUtils.LbcDebug( "数据为空" );
//               } else{
//                   LogUtils.LbcDebug( "数据不为空" );
//               }
            }
        } );

        mRecipientRichEdit.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    LogUtils.LbcDebug( "dfocuse true" );
                }else{
                     mRecipientRichEdit.insertHandData();
                    LogUtils.LbcDebug( "focuse false" );
                }
            }
        } );

        mRecipientRichEdit.setInsertModelListWatcher( new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
               List<MailRecipientModel> delctRecipients = new ArrayList<>() ;
                for(int i=0;i<mRecipients.size();i++){
                   String email = mRecipients.get(i).getmRecipientEmail();
                   boolean haveEmail=false;
                   for(int j=0;j<insertModelList.size();j++){
                       if(email.equals(insertModelList.get( j ).getInsertContentId())){
                           haveEmail=true;
                       }
                   }
                   if(!haveEmail){
                       mRecipients.remove( i );
                   }
                }
            }
        } );

        mCCRecipientRichEdit.setInputWatcher( new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        } );

        mCCRecipientRichEdit.setInsertModelListWatcher( new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
                List<MailRecipientModel> delctRecipients = new ArrayList<>() ;
                for(int i=0;i<mCCRecipients.size();i++){
                    String email = mCCRecipients.get(i).getmRecipientEmail();
                    boolean haveEmail=false;
                    for(int j=0;j<insertModelList.size();j++){
                        if(email.equals(insertModelList.get( j ).getInsertContentId())){
                            haveEmail=true;
                        }
                    }
                    if(!haveEmail){
                        mCCRecipients.remove( i );
                    }
                }
            }
        } );

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
                            boolean isContaion = isListContaionSpecItem( mRecipients, singleRecipient );
                            if (!isContaion) {
                                mRecipients.add( singleRecipient );
                                notifyRichEdit(mRecipientRichEdit, singleRecipient);
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
                                notifyRichEdit(mCCRecipientRichEdit, singleRecipient);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isListContaionSpecItem(ArrayList<MailRecipientModel> selectMemList, MailRecipientModel specItem) {
        for (int i = 0; i < selectMemList.size(); i++) {
            if (selectMemList.get( i ).getmRecipientEmail().equals( specItem.getmRecipientEmail() )) {
                return true;
            }
        }
        return false;
    }

    private void  notifyRichEdit( RichEdit richEdit,   MailRecipientModel mRecipients){
            String name = mRecipients.getmRecipientName();
            String email = mRecipients.getmRecipientEmail();
            InsertModel  insertModel = new InsertModel(";", (System.currentTimeMillis()) + "", name, email);
            richEdit.insertSpecialStr(false, insertModel);
    }




}
