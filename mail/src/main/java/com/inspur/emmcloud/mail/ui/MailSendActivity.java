package com.inspur.emmcloud.mail.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollWebView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.basemodule.widget.richedit.InsertModel;
import com.inspur.emmcloud.basemodule.widget.richedit.RichEdit;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.bean.Mail;
import com.inspur.emmcloud.mail.bean.MailCertificateDetail;
import com.inspur.emmcloud.mail.bean.MailRecipient;
import com.inspur.emmcloud.mail.bean.MailRecipientModel;
import com.inspur.emmcloud.mail.bean.MailSend;
import com.inspur.emmcloud.mail.util.MailCacheUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by libaochao on 2018/12/20.
 */
public class MailSendActivity extends BaseActivity {
    public static final String MODE_NEW = "mail_new";
    public static final String MODE_REPLY = "mail_replay";
    public static final String MODE_REPLY_ALL = "mail_replay_all";
    public static final String MODE_FORWARD = "mail_forward";
    public static final String EXTRA_MAIL_ID = "extra_mail_id";
    public static final String EXTRA_MAIL_MODE = "extra_mail_mode";
    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_CC_MEMBER = 3;
    @BindView(R.id.rich_edit_recipients)
    RichEdit recipientRichEdit;
    @BindView(R.id.rich_edit_cc_recipient)
    RichEdit ccRecipientRichEdit;
    @BindView(R.id.et_theme_send)
    EditText sendThemeEditText;
    @BindView(R.id.tv_recipients_show)
    TextView recipientsShowText;
    @BindView(R.id.tv_cc_recipient_show)
    TextView ccRecipientsShowText;
    @BindView(R.id.et_content_send)
    EditText contentSendEditText;
    @BindView(R.id.iv_fw_tip)
    ImageView fwTipImageView;
    @BindView(R.id.rl_fw_body)
    RelativeLayout fwBodyLayout;
    @BindView(R.id.rl_include_origin)
    RelativeLayout includeOriginLayout;
    @BindView(R.id.cb_fw_body)
    CheckBox fwBodyCheckBox;
    @BindView(R.id.wv_body)
    NoScrollWebView bodyWebView;
    @BindView(R.id.tv_header_title)
    TextView headerTitleText;
    @BindView(R.id.iv_recipients)
    ImageView recipientsImageView;
    @BindView(R.id.iv_cc_recipients)
    ImageView ccRecipientsImageView;
    private ArrayList<String> memberUidList = new ArrayList<>();
    private ArrayList<MailRecipientModel> recipientList = new ArrayList<>();
    private ArrayList<MailRecipientModel> ccRecipientList = new ArrayList<>();
    private MailCertificateDetail myCertificate;
    private Mail originMail;
    private LoadingDialog loadingDlg;
    private String mailMode = MODE_NEW;


    @Override
    public void onCreate() {
        setContentView(R.layout.activity_mail_send);
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_mail_send;
    }

    /**
     * 初始化
     */
    private void init() {
        String username = PreferencesUtils.getString(this, "userRealName", "");
        String content = "\n\n\n\n" + username + "\n\t" + "---发自我的云+移动端";
        contentSendEditText.setText(content);
        loadingDlg = new LoadingDialog(this);
        recipientRichEdit.setInputWatcher(new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String data;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 1 && ' ' == s.charAt(s.length() - 1)) {
                    recipientRichEdit.insertLastManualData(1);
                }
            }
        });
        recipientRichEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    recipientRichEdit.insertLastManualData(-1);
                }
            }
        });

        recipientRichEdit.setInsertModelListWatcher(new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
                synchronousRemoveRecipients(recipientList, insertModelList);
                synchronousAddRecipients(recipientList, insertModelList);
            }
        });

        ccRecipientRichEdit.setInputWatcher(new RichEdit.InputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 1 && ' ' == s.charAt(s.length() - 1)) {
                    ccRecipientRichEdit.insertLastManualData(1);
                }
            }
        });

        ccRecipientRichEdit.setInsertModelListWatcher(new RichEdit.InsertModelListWatcher() {
            @Override
            public void onDataChanged(List<InsertModel> insertModelList) {
                synchronousRemoveRecipients(ccRecipientList, insertModelList);
                synchronousAddRecipients(ccRecipientList, insertModelList);
            }
        });
        ccRecipientRichEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ccRecipientRichEdit.insertLastManualData(-1);
                }
            }
        });

        fwBodyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                bodyWebView.setVisibility(b ? View.VISIBLE : View.GONE);
            }
        });
        Bundle mailBundle = getIntent().getExtras();
        if (null != mailBundle) {
            mailMode = mailBundle.getString(EXTRA_MAIL_MODE);
            String replyMailId = mailBundle.getString(EXTRA_MAIL_ID);
            originMail = MailCacheUtils.getMail(replyMailId);
            if (null != originMail) {
                List<MailRecipient> mailRecipientList = new ArrayList<>();
                switch (mailMode) {
                    case MODE_REPLY:
                        String string = JSON.toJSONString(((Object) originMail));
                        LogUtils.LbcDebug("JSonData:" + string);

                        mailRecipientList.clear();
                        mailRecipientList.add(originMail.getFromMailRecipient());
                        insertReciversFromExtra(recipientRichEdit, mailRecipientList, recipientList);

                        sendThemeEditText.setText(originMail.getSubject().toString());
                        headerTitleText.setText("回复邮件");
                        break;
                    case MODE_REPLY_ALL:
                        headerTitleText.setText("回复邮件");
                        mailRecipientList.clear();
                        mailRecipientList.add(originMail.getFromMailRecipient());
                        insertReciversFromExtra(recipientRichEdit, mailRecipientList, recipientList);

                        mailRecipientList.clear();
                        mailRecipientList = originMail.getToMailRecipientList();
                        insertReciversFromExtra(recipientRichEdit, mailRecipientList, recipientList);
                        recipientsShowText.setText(recipientRichEdit.getText().toString());
                        recipientRichEdit.setVisibility(View.GONE);
                        recipientsShowText.setVisibility(View.VISIBLE);

                        mailRecipientList.clear();
                        mailRecipientList = originMail.getCcMailRecipientList();
                        insertReciversFromExtra(ccRecipientRichEdit, mailRecipientList, ccRecipientList);
                        ccRecipientsShowText.setText(ccRecipientRichEdit.getText().toString());
                        ccRecipientRichEdit.setVisibility(View.GONE);
                        ccRecipientsShowText.setVisibility(View.VISIBLE);

                        sendThemeEditText.setText(originMail.getSubject().toString());

                        break;
                    case MODE_FORWARD:
                        headerTitleText.setText("转发邮件");
                        sendThemeEditText.setText(originMail.getSubject().toString());
                        break;
                }

                String mailBodyText = originMail.getBodyText();
                if (!StringUtils.isBlank(mailBodyText)) {
                    WebSettings webSettings = bodyWebView.getSettings();
                    webSettings.setUseWideViewPort(true);
                    webSettings.setJavaScriptEnabled(true);
                    webSettings.setSupportZoom(true);
                    webSettings.setBuiltInZoomControls(true);
                    webSettings.setDisplayZoomControls(false);
                    webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                    webSettings.setLoadWithOverviewMode(true);
                    webSettings.setDefaultFontSize(40);
                    webSettings.setMinimumFontSize(40);
                    bodyWebView.loadDataWithBaseURL(null, mailBodyText, "text/html", "utf-8", null);
                    fwTipImageView.setVisibility((originMail != null) ? View.VISIBLE : View.GONE);
                }
            }
        }

        Object object = PreferencesByUsersUtils.getObject(this, MailCertificateInstallActivity.CERTIFICATER_KEY);
        if (null == object) {
            myCertificate = new MailCertificateDetail();
            myCertificate.setEncryptedMail(false);
            myCertificate.setSignedMail(false);
        } else {
            myCertificate = (MailCertificateDetail) object;
        }

        includeOriginLayout.setVisibility(mailMode.equals(MODE_NEW) ? View.GONE : View.VISIBLE);
    }

    /**
     * 转发、回复等插入收件人
     **/
    private void insertReciversFromExtra(RichEdit richRdit, List<MailRecipient> mailRecipients, ArrayList<MailRecipientModel> recipientsList) {
        for (int i = 0; i < mailRecipients.size(); i++) {
            MailRecipientModel reciver = new MailRecipientModel();
            reciver.setName(mailRecipients.get(i).getName());
            reciver.setAddress(mailRecipients.get(i).getAddress());
            boolean isContaion = isListContaionSpecItem(recipientsList, reciver);
            if (!isContaion) {
                recipientsList.add(reciver);
                notifyRichEdit(richRdit, reciver, i);
            }
        }
    }

    /**
     * 发送邮件 数据准备
     */
    private MailSend prepareSendData() {
        MailSend mail = new MailSend();
        SpannableString spanString = new SpannableString(contentSendEditText.getText().toString());
        String html = Html.toHtml(spanString);
        mail.setBody(html);
        mail.setToRecipients(recipientList);
        mail.setCcRecipients(ccRecipientList);
        String mailSenderAddess = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT);
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByEmail(mailSenderAddess);
        mail.setFrom(new MailRecipientModel(contactUser.getName(), contactUser.getEmail()));
        mail.setNeedEncrypt(myCertificate.isEncryptedMail());
        mail.setNeedSign(myCertificate.isSignedMail());
        mail.setOriginalMail((originMail != null && fwBodyCheckBox.isChecked()) ? originMail.getBodyText() : "");
        mail.setSubject(StringUtils.isBlank(sendThemeEditText.getText().toString()) ? "" : sendThemeEditText.getText().toString());
        mail.setIsForward(mailMode.equals(MODE_FORWARD));
        mail.setIsReply(mailMode.equals(MODE_REPLY) || mailMode.equals(MODE_REPLY_ALL));
        return mail;
    }

    /**
     * 发邮件时提醒
     */
    private void noSignOrEncryptHintDialog() {
        if (!myCertificate.isSignedMail() || !myCertificate.isEncryptedMail()) {
            String mailHint = "该邮件未" + (myCertificate.isSignedMail() ? "" : "加签") + (myCertificate.isEncryptedMail() ? "" : "加密") + "确定发送？";
            new CustomDialog.MessageDialogBuilder(MailSendActivity.this)
                    .setMessage(mailHint)
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                sendMail();   //发送邮件
                            } catch (Exception e) {
                                LogUtils.LbcDebug("Error");
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            try {
                sendMail();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送邮件
     */
    private void sendMail() throws Exception {
        MailSend mailSend = prepareSendData();
        String jsonMail = JSON.toJSONString(mailSend);
        LogUtils.jasonDebug("jsonMail=" + jsonMail);
        String key = EncryptUtils.stringToMD5(mailSend.getFrom().getAddress().toString());
        byte[] mailContent = EncryptUtils.encodeNoBase64(jsonMail, key, Constant.MAIL_ENCRYPT_IV);
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            MailApiService apiService = new MailApiService(this);
            apiService.setAPIInterface(new WebService());
            apiService.sendEncryptMail(mailContent);
        }
    }

    /**
     * 点击事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send_mail:
                if (recipientList.size() == 0) {
                    ToastUtils.show(this, "至少添加一个收件人");
                    return;
                }
                noSignOrEncryptHintDialog();
                break;
            case R.id.iv_recipients:
                recipientRichEdit.insertLastManualData(0);
                Intent intent = new Intent();
                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList);
                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, "添加收件人");
                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intent, QEQUEST_ADD_MEMBER);
                break;
            case R.id.iv_cc_recipients:
                ccRecipientsShowText.setVisibility(View.GONE);
                ccRecipientRichEdit.setVisibility(View.VISIBLE);
                ccRecipientRichEdit.insertLastManualData(0);
                Intent intent1 = new Intent();
                intent1.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent1.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList);
                intent1.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent1.putExtra(ContactSearchFragment.EXTRA_TITLE, "添加抄送人");
                intent1.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intent1, QEQUEST_CC_MEMBER);
                break;
            case R.id.iv_fw_tip:
                fwTipImageView.setVisibility(View.GONE);
                fwBodyLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_recipients_show:
                recipientsShowText.setVisibility(View.GONE);
                recipientRichEdit.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_cc_recipient_show:
                ccRecipientsShowText.setVisibility(View.GONE);
                ccRecipientRichEdit.setVisibility(View.VISIBLE);
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
                    List<SearchModel> addMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    if (addMemberList.size() > 0) {
                        for (int i = 0; i < addMemberList.size(); i++) {
                            MailRecipientModel singleRecipient = new MailRecipientModel();
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(addMemberList.get(i).getId());
                            singleRecipient.setAddress(contactUser.getEmail());
                            singleRecipient.setName(contactUser.getName());
                            boolean isContaion = isListContaionSpecItem(recipientList, singleRecipient);
                            if (!isContaion) {
                                recipientList.add(singleRecipient);
                                notifyRichEdit(recipientRichEdit, singleRecipient, i);
                            }
                        }
                    }
                    break;
                case QEQUEST_CC_MEMBER:
                    List<SearchModel> ctAddMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    if (ctAddMemberList.size() > 0) {
                        for (int i = 0; i < ctAddMemberList.size(); i++) {
                            MailRecipientModel singleRecipient = new MailRecipientModel();
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(ctAddMemberList.get(i).getId());
                            singleRecipient.setAddress(contactUser.getEmail());
                            singleRecipient.setName(contactUser.getName());
                            boolean isContaion = isListContaionSpecItem(ccRecipientList, singleRecipient);
                            if (!isContaion) {
                                ccRecipientList.add(singleRecipient);
                                notifyRichEdit(ccRecipientRichEdit, singleRecipient, i);
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
     *
     * @param selectMemList
     * @param specItem
     */
    private boolean isListContaionSpecItem(ArrayList<MailRecipientModel> selectMemList, MailRecipientModel specItem) {
        for (int i = 0; i < selectMemList.size(); i++) {
            if (selectMemList.get(i).getAddress().equals(specItem.getName())) {
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
        InsertModel insertModel = new InsertModel("； ", (System.currentTimeMillis()) + "" + subId, mRecipients.getName(), mRecipients.getAddress());
        richEdit.insertSpecialStr(false, insertModel);
    }

    /**
     * Recipient 根据insertModels 多的删掉
     */
    private void synchronousRemoveRecipients(ArrayList<MailRecipientModel> recipients, List<InsertModel> insertModels) {
        for (int i = 0; i < recipients.size(); i++) {
            String email = recipients.get(i).getAddress();
            boolean haveEmail = false;
            for (int j = 0; j < insertModels.size(); j++) {
                if (email.equals(insertModels.get(j).getInsertContentId())) {
                    haveEmail = true;
                    break;
                }
            }
            if (!haveEmail) {
                recipients.remove(i);
            }
        }
    }

    /**
     * Re 根据InsertModel少的添加
     **/
    private void synchronousAddRecipients(ArrayList<MailRecipientModel> recipients, List<InsertModel> insertModels) {
        for (int m = 0; m < insertModels.size(); m++) {
            String address = insertModels.get(m).getInsertContentId().toString();
            String name = insertModels.get(m).getInsertContent().toString();
            LogUtils.LbcDebug("address:" + address + "name:" + name);
            boolean haveEmail = false;
            for (int n = 0; n < recipients.size(); n++) {
                if (address.equals(recipients.get(n).getAddress())) {
                    haveEmail = true;
                    break;
                }
            }
            if (!haveEmail) {
                MailRecipientModel mailRecipientModel = new MailRecipientModel();
                mailRecipientModel.setAddress(address);
                mailRecipientModel.setName(name);
                recipients.add(mailRecipientModel);
            }
        }

    }

    /**
     * 网络通信类
     */
    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnSendMailSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(MailSendActivity.this, "邮件发送成功！");
            finish();
        }

        @Override
        public void returnSendMailFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(MailSendActivity.this, "邮件发送失败！");
        }
    }


}
