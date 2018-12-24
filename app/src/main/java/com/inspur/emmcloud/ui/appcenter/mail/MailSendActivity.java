package com.inspur.emmcloud.ui.appcenter.mail;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.MailEditText;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView( R.layout.activity_mail_send )
public class MailSendActivity extends BaseActivity {
    @ViewInject( R.id.et_recipient)
    private MailEditText mRecipientEditText;
    @ViewInject( R.id.et_copy_to_recipient )
    private EditText mCopyToEditText;
    @ViewInject( R.id.et_sender_theme )
    private  EditText mSendThemeEditText;

    @ViewInject( R.id.iv_recipients )
    private ImageView mRecipientsImageView;
    @ViewInject( R.id.iv_copy_to_recipients)
    private  ImageView mCopy2RecipientsImageView;

    private ArrayList<String> memberUidList = new ArrayList<>();
    private ArrayList<String> uiMemberUidList = new ArrayList<>();
    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_CT_MEMBER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    /***/
    private  void init() {
        mRecipientEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    String content = mRecipientEditText.getText().toString();
                    int length = content.length();
                    content.endsWith("-");
                }
                return false;
            }
        });
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.tv_send_mail:
                break;
            case R.id.iv_recipients:
                Intent intent = new Intent();
                    intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                    intent.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList);
                    intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                    intent.putExtra(ContactSearchFragment.EXTRA_TITLE, "添加收件人");
                    intent.setClass(getApplicationContext(),
                            ContactSearchActivity.class);
                    startActivityForResult(intent, QEQUEST_ADD_MEMBER);
                break;
            case R.id.iv_copy_to_recipients:
                Intent intent1 = new Intent();
                intent1.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent1.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList);
                intent1.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent1.putExtra(ContactSearchFragment.EXTRA_TITLE, "添加抄送人");
                intent1.setClass(getApplicationContext(),
                        ContactSearchActivity.class);
                startActivityForResult(intent1, QEQUEST_CT_MEMBER);
                break;
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case QEQUEST_ADD_MEMBER:
                    String NameEmails ="";
                    ArrayList<String> addUidList = new ArrayList<>();
                    ArrayList<String> addNameList = new ArrayList<>();
                    ArrayList<String> addEmailList= new ArrayList<>();
                    List<SearchModel> addMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    if (addMemberList.size()>0){
                        LogUtils.LbcDebug("addMemberList.size()"+addMemberList.size());
                        LogUtils.LbcDebug("data:::::"+addMemberList.toString());
                        for (int i = 0; i < addMemberList.size(); i++) {
                            addUidList.add(addMemberList.get(i).getId());
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid( addUidList.get( i ) );
                            addEmailList.add( contactUser.getEmail() );
                            addNameList.add(addMemberList.get(i).getName());
                            LogUtils.LbcDebug("Email:::::"+ addEmailList.get( i )+"    Name:::"+addNameList.get( i ));
                            NameEmails = NameEmails+addNameList.get(i)+"("+addEmailList.get(i)+"),";
                        }
                        mRecipientEditText.setText(NameEmails);
                    }
                    break;
                case QEQUEST_CT_MEMBER:
                    String CTNameEmails ="";
                    ArrayList<String> ctAddUidList = new ArrayList<>();
                    ArrayList<String> ctaddNameList = new ArrayList<>();
                    ArrayList<String> ctaddEmailList= new ArrayList<>();
                    List<SearchModel> ctaddMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    if (ctaddMemberList.size()>0){
                        LogUtils.LbcDebug("addMemberList.size()"+ctaddMemberList.size());
                        LogUtils.LbcDebug("data:::::"+ctaddMemberList.toString());
                        for (int i = 0; i < ctaddMemberList.size(); i++) {
                            ctAddUidList.add(ctaddMemberList.get(i).getId());
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid( ctAddUidList.get( i ) );
                            ctaddEmailList.add(contactUser.getEmail());
                            ctaddNameList.add(ctaddMemberList.get(i).getName());
                            LogUtils.LbcDebug("Email:::::"+ ctaddEmailList.get( i )+"    Name:::"+ctaddNameList.get( i ));
                            CTNameEmails = CTNameEmails+ctaddNameList.get(i)+"("+ctaddEmailList.get(i)+"),";
                        }
                        mCopyToEditText.setText(CTNameEmails);
                    }
                    break;
                default:
                    break;
            }
        }
    }



}
