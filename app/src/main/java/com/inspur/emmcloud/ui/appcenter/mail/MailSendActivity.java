package com.inspur.emmcloud.ui.appcenter.mail;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.mail.MailRecipientModel;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.FlowLayout;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView( R.layout.activity_mail_send )
public class MailSendActivity extends BaseActivity {
    @ViewInject(R.id.fl_recipient)
    private FlowLayout mRecipientFlowLayout;
    @ViewInject(R.id.et_copy_to_recipient)
    private EditText mCopyToEditText;
    @ViewInject(R.id.et_sender_theme)
    private EditText mSendThemeEditText;

    @ViewInject(R.id.iv_recipients)
    private ImageView mRecipientsImageView;
    @ViewInject(R.id.iv_copy_to_recipients)
    private ImageView mCopy2RecipientsImageView;

    private EditText searchEdit;


    private ArrayList<String> memberUidList = new ArrayList<>();
    private ArrayList<MailRecipientModel> mRecipients = new ArrayList<>();
    private ArrayList<MailRecipientModel> mCTRecipients = new ArrayList<>();
    private String searchText;
    private MyTextWatcher myTextWatcher;


    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_CT_MEMBER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        init();
    }

    /***/
    private void init() {
        myTextWatcher  = new MyTextWatcher();
         flowAddEdit();
        if(mRecipients.size()>0){
            notifyFlowLayoutDataChange(mRecipients);
        }
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
            case R.id.iv_copy_to_recipients:
                Intent intent1 = new Intent();
                intent1.putExtra( ContactSearchFragment.EXTRA_TYPE, 2 );
                intent1.putExtra( ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList );
                intent1.putExtra( ContactSearchFragment.EXTRA_MULTI_SELECT, true );
                intent1.putExtra( ContactSearchFragment.EXTRA_TITLE, "添加抄送人" );
                intent1.setClass( getApplicationContext(),
                        ContactSearchActivity.class );
                startActivityForResult( intent1, QEQUEST_ADD_MEMBER );
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
                           final  MailRecipientModel singleRecipient = new MailRecipientModel();
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(addMemberList.get(i).getId());
                            LogUtils.LbcDebug( contactUser.getEmail() );
                            singleRecipient.setmRecipientEmail( contactUser.getEmail() );
                            singleRecipient.setmRecipientName( contactUser.getName() );
                            LogUtils.LbcDebug( singleRecipient.getmRecipientEmail() );
                            if(!mRecipients.contains(singleRecipient)){
                                LogUtils.LbcDebug( "不包含" );
                                mRecipients.add(singleRecipient);
                            }else{
                                LogUtils.LbcDebug( "包含不操作" );
                            }
                        }
                        notifyFlowLayoutDataChange(mRecipients);
                    }
                    break;
                case QEQUEST_CT_MEMBER:
                    String CTNameEmails = "";
                    ArrayList<String> ctAddUidList = new ArrayList<>();
                    ArrayList<String> ctaddNameList = new ArrayList<>();
                    ArrayList<String> ctaddEmailList = new ArrayList<>();
                    List<SearchModel> ctaddMemberList = (List<SearchModel>) data
                            .getSerializableExtra( "selectMemList" );
                    if (ctaddMemberList.size() > 0) {
                        LogUtils.LbcDebug( "addMemberList.size()" + ctaddMemberList.size() );
                        LogUtils.LbcDebug( "data:::::" + ctaddMemberList.toString() );
                        for (int i = 0; i < ctaddMemberList.size(); i++) {
                            ctAddUidList.add( ctaddMemberList.get( i ).getId() );
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid( ctAddUidList.get( i ) );
                            ctaddEmailList.add( contactUser.getEmail() );
                            ctaddNameList.add( ctaddMemberList.get( i ).getName() );
                            LogUtils.LbcDebug( "Email:::::" + ctaddEmailList.get( i ) + "    Name:::" + ctaddNameList.get( i ) );
                            CTNameEmails = CTNameEmails + ctaddNameList.get( i ) + "(" + ctaddEmailList.get( i ) + "),";
                        }
                        mCopyToEditText.setText( CTNameEmails );
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 刷新FlowLayout
     */
    private void notifyFlowLayoutDataChange(ArrayList<MailRecipientModel> selectMemList) {
        mRecipientFlowLayout.removeAllViews();
        for (int i = 0; i < selectMemList.size(); i++) {
            final MailRecipientModel searchModel = selectMemList.get(i);
            TextView searchResultText = new TextView(this);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = DensityUtil.dip2px( MyApplication.getInstance(), 5);
            params.topMargin = DensityUtil.dip2px(MyApplication.getInstance(), 2);
            params.bottomMargin = params.topMargin;
            searchResultText.setLayoutParams(params);
            int piddingTop = DensityUtil.dip2px(this.getApplicationContext(), 1);
            int piddingLeft = DensityUtil.dip2px(this.getApplicationContext(), 5);
            searchResultText.setPadding(piddingLeft, piddingTop, piddingLeft, piddingTop);
            searchResultText.setGravity( Gravity.CENTER);
            searchResultText.setTextSize( TypedValue.COMPLEX_UNIT_SP, 16);
            searchResultText.setTextColor(searchModel.getmEmailFormat()?Color.parseColor("#0F7BCA"):Color.parseColor("#f96666"));
            searchResultText.setText(selectMemList.get(i).getmRecipientName());
            searchResultText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    LogUtils.LbcDebug( "点击事件,删除对象重绘" );
                    delectRecipient(searchModel);
                }
            });
            mRecipientFlowLayout.addView(searchResultText);
        }
       flowAddEdit();
    }

    private void delectRecipient(MailRecipientModel model){
        if(mRecipients.contains(model)){
            mRecipients.remove(model);
            notifyFlowLayoutDataChange(mRecipients);
        }
    }

    private void flowAddEdit() {
        if (searchEdit == null) {
            searchEdit = new EditText( this );
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
                    this.getApplicationContext(), ViewGroup.LayoutParams.WRAP_CONTENT ) );
            params.topMargin = DensityUtil.dip2px( this.getApplicationContext(), 2 );
            params.bottomMargin = params.topMargin;
            int piddingTop = DensityUtil.dip2px( MyApplication.getInstance(), 1 );
            int piddingLeft = DensityUtil.dip2px( MyApplication.getInstance(), 10 );
            searchEdit.setPadding( piddingLeft, piddingTop, piddingLeft, piddingTop );
            searchEdit.setLayoutParams( params );
            searchEdit.setSingleLine( true );
            searchEdit.setTextSize( TypedValue.COMPLEX_UNIT_SP, 16 );
            searchEdit.setBackground( null );
            searchEdit.setHint( "请输入邮箱名称");
            searchEdit.addTextChangedListener( myTextWatcher );
        }else{
            searchEdit.setText("");
        }
        if (searchEdit.getParent() == null) {
            mRecipientFlowLayout.addView( searchEdit );
        }
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
        searchText = searchEdit.getText().toString().trim();
         if(!(StringUtils.isBlank(searchText))&&','==searchText.charAt(searchText.length()-1)){
             String email = searchText.substring( 0,searchText.length()-1 );
             MailRecipientModel mailRecipientModel = new MailRecipientModel();
             mailRecipientModel.setmRecipientName(email);
             mailRecipientModel.setmRecipientEmail(email);
            if(!mRecipients.contains(mailRecipientModel)){
                mRecipients.add( mailRecipientModel );
                notifyFlowLayoutDataChange(mRecipients);
            }
         }
        }
    }
}
