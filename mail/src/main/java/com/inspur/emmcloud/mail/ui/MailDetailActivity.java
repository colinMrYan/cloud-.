package com.inspur.emmcloud.mail.ui;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.util.ZipUtils;
import com.inspur.emmcloud.baselib.widget.FlowLayout;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollWebView;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.R2;
import com.inspur.emmcloud.mail.adapter.MailAttachmentListAdapter;
import com.inspur.emmcloud.mail.api.MailAPIInterfaceImpl;
import com.inspur.emmcloud.mail.api.MailAPIService;
import com.inspur.emmcloud.mail.api.MailAPIUri;
import com.inspur.emmcloud.mail.bean.Mail;
import com.inspur.emmcloud.mail.bean.MailAttachment;
import com.inspur.emmcloud.mail.bean.MailRecipient;
import com.inspur.emmcloud.mail.util.MailCacheUtils;
import com.inspur.emmcloud.mail.widget.ObservableScrollView;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/12/24.
 */

public class MailDetailActivity extends BaseActivity {
    public static final String EXTRA_MAIL = "extra_mail";
    public static final String EXTRA_MAIL_ID = "extra_mail_id";
    @BindView(R2.id.sv_slide_data)
    ObservableScrollView scrollView;
    @BindView(R2.id.tv_mail_sender)
    TextView senderText;
    @BindView(R2.id.tv_mail_receiver_collapse)
    TextView receiverCollapseText;
    @BindView(R2.id.fl_mail_receiver_expand)
    FlowLayout receiverFlowLayout;
    @BindView(R2.id.tv_mail_receiver_expand)
    TextView receiverExpandText;
    @BindView(R2.id.rl_receiver_collapse)
    RelativeLayout receiverCollapseLayout;
    @BindView(R2.id.rl_mail_cc)
    RelativeLayout ccLayout;
    @BindView(R2.id.fl_mail_cc_expand)
    FlowLayout ccFlowLayout;
    @BindView(R2.id.tv_mail_cc_expand)
    TextView ccExpandText;
    @BindView(R2.id.tv_mail_cc_collapse)
    TextView ccCollapseText;
    @BindView(R2.id.rl_cc_collapse)
    RelativeLayout ccCollapseLayout;
    @BindView(R2.id.iv_flag_encrypt)
    ImageView encryptImg;
    @BindView(R2.id.iv_flag_sign)
    ImageView signImg;
    @BindView(R2.id.tv_topic)
    TextView topicText;
    @BindView(R2.id.tv_mail_send_time)
    TextView sendTimeText;
    @BindView(R2.id.lv_attachment)
    ScrollViewWithListView mailAttachmentListView;
    @BindView(R2.id.wv_content)
    NoScrollWebView contentWebView;
    @BindView(R2.id.rl_mail_operation)
    RelativeLayout mailOperationLayout;
    @BindView(R2.id.progress_bar_load)
    ProgressBar loadProgressBar;
    private MailAttachmentListAdapter mailAttachmentListAdapter;
    //Res.getDrawableID("web_qrcode_zx_code_line")
    private Mail mail;
    private MailAPIService apiService;
    private LoadingDialog loadingDlg;
    private ContactUser contactUser;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        Mail simpleMail = (Mail) getIntent().getSerializableExtra(EXTRA_MAIL);
        mail = MailCacheUtils.getMail(simpleMail.getId());
        if (!mail.isComplete()) {
            getMailDetail();
        }
        ContactService mailService = Router.getInstance().getService(ContactService.class);
        if (mailService != null) {
            contactUser = mailService.getContactUserByUid(BaseApplication.getInstance().getUid());
        }
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.mail_details_activity;
    }

    private void initView() {
        findViewById(R.id.ibt_mail_delete).setBackgroundResource(R.drawable.ic_mail_delete_white);
        signImg.setImageResource(R.drawable.ic_mail_flag_sign_yes);
        setMailContentVisible(false);
        encryptImg.setImageResource(mail.isEncrypted() ? R.drawable.ic_mail_flag_encrypt_yes : R.drawable.ic_mail_flag_encrypt_no);
        encryptImg.setVisibility(mail.isEncrypted() ? View.VISIBLE : View.INVISIBLE);
        topicText.setText(mail.getSubject());
        sendTimeText.setText(TimeUtils.getTime(this, mail.getCreationTimestamp(), TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
        senderText.setText(mail.getDisplaySender());
        receiverCollapseText.setText(mail.getDisplayTo());
        initReceiverExpandTextStatus();
        initReceiverFlowLayout();
        if (mail.getCcMailRecipientList().size() > 0) {
            StringBuilder ccMailRecipientBuilder = new StringBuilder();
            for (MailRecipient mailRecipient : mail.getCcMailRecipientList()) {
                ccMailRecipientBuilder.append(mailRecipient.getName()).append("; ");
            }
            ccCollapseText.setText(ccMailRecipientBuilder);
            ccLayout.setVisibility(View.VISIBLE);
            initCcExpandTextStatus();
            initCcFlowLayout();
        }
        if (mail.isComplete()) {
            initMailContentView();
        }
    }


    private void initMailContentView() {
        setMailContentVisible(true);
        final List<MailAttachment> reallyMailAttachmentList = mail.getReallyMailAttachmentList();
        mailAttachmentListAdapter = new MailAttachmentListAdapter(this, reallyMailAttachmentList);
        mailAttachmentListView.setAdapter(mailAttachmentListAdapter);
        mailAttachmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MailAttachment mailAttachment = reallyMailAttachmentList.get(position);
                String attachmentFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT + mail.getId() + "/" + mailAttachment.getName();
                if (FileUtils.isFileExist(attachmentFilePath)) {
                    FileUtils.openFile(MailDetailActivity.this, attachmentFilePath);
                }

            }
        });
        String mailBodyText = mail.getBodyText();
        if (!StringUtils.isBlank(mailBodyText)) {
            WebSettings webSettings = contentWebView.getSettings();
            webSettings.setUseWideViewPort(true);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webSettings.setLoadWithOverviewMode(true);
            contentWebView.loadDataWithBaseURL(null, mailBodyText, "text/html", "utf-8", null);
            contentWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress == 100) {
                        view.loadUrl("javascript: var meta = document.createElement('meta'); meta.setAttribute('name', 'viewport'); meta.setAttribute('content', 'width=device-width'); document.getElementsByTagName('head')[0].appendChild(meta);");
                    }
                }
            });
            contentWebView.setWebViewClient(new WebViewClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        WebResourceResponse webResourceResponse = getWebResourceResponse(url);
                        if (webResourceResponse != null) {
                            return webResourceResponse;
                        }

                    }
                    return super.shouldInterceptRequest(view, url);
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    WebResourceResponse webResourceResponse = getWebResourceResponse(request.getUrl().toString());
                    if (webResourceResponse != null) {
                        return webResourceResponse;
                    }
                    return super.shouldInterceptRequest(view, request);

                }
            });
        }


        scrollView.addOnScrollChangedListener(new ObservableScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(ObservableScrollView observableScrollView, int i, int i1, int i2, int i3) {
                int oldt = i3;
                int t = i1;
                if (oldt > t && oldt - t > 20) {
                    mailOperationLayout.setVisibility(View.VISIBLE);
                } else if (oldt < t && t - oldt > 20) {
                    mailOperationLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private WebResourceResponse getWebResourceResponse(String url) {
        WebResourceResponse webResourceResponse = null;
        for (MailAttachment mailAttachment : mail.getMailAttachmentList()) {
            if (!mailAttachment.isAttachment()) {
                if (url.contains(mailAttachment.getContentId())) {
                    String attachmentFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT + mail.getId() + "/" + mailAttachment.getName();
                    if (FileUtils.isFileExist(attachmentFilePath)) {
                        try {
                            File attachmentFile = new File(attachmentFilePath);
                            FileInputStream fileInputStream = new FileInputStream(attachmentFile);
                            String mimeType = FileUtils.getMimeType(attachmentFile);
                            webResourceResponse = new WebResourceResponse(mimeType, "UTF-8", fileInputStream);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    break;
                }
            }
        }
        return webResourceResponse;
    }

    /**
     * 判断是否所有的附件下载完成
     *
     * @param mailAttachmentList
     * @return
     */
    private boolean checkMailAttachmentsDownload(List<MailAttachment> mailAttachmentList) {
        boolean isMailAttachmentsDownload = true;
        for (MailAttachment mailAttachment : mailAttachmentList) {
            String attachmentFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT + mail.getId() + "/" + mailAttachment.getName();
            if (!FileUtils.isFileExist(attachmentFilePath)) {
                isMailAttachmentsDownload = false;
                break;
            }
        }
        return isMailAttachmentsDownload;
    }

    /**
     * 设置邮件内容是否可见
     *
     * @param isVisible
     */
    private void setMailContentVisible(boolean isVisible) {
        mailAttachmentListView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        contentWebView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        loadProgressBar.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void downloadMailAttachments(final List<MailAttachment> mailAttachmentList) {
        for (final MailAttachment mailAttachment : mailAttachmentList) {
            final String mailAttachmentFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT + mail.getId() + "/" + (mail.isEncrypted() ? System.currentTimeMillis() + ".tmp" : mailAttachment.getName());
            if (FileUtils.isFileExist(mailAttachmentFilePath)) {
                return;
            }
            String source = MailAPIUri.getMailAttachmentUrl();
            APIDownloadCallBack callBack = new APIDownloadCallBack(getApplicationContext(), source) {
                @Override
                public void callbackStart() {
                }

                @Override
                public void callbackLoading(long total, long current, boolean isUploading) {
//                    int progress = (int) (current * 100.0 / total);
//                    String totleSize = FileUtil.formetFileSize(total);
//                    String currentSize = FileUtil.formetFileSize(current);

                }

                @Override
                public void callbackSuccess(File file) {
                    if (mail.isEncrypted()) {
                        byte[] encryptBytes = FileUtils.file2Bytes(file.getPath());
                        byte[] decryptBytes = decryptBytes(encryptBytes);
                        //如果是加密邮件中真正的附件，需要解密后再解压缩
                        if (mailAttachment.isAttachment()) {
                            decryptBytes = ZipUtils.unGzipcompress(decryptBytes);
                        }
                        FileUtils.writeFile(MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT + mail.getId() + "/" + mailAttachment.getName(), new ByteArrayInputStream(decryptBytes));
                        FileUtils.deleteFile(file.getAbsolutePath());
                    }
                    boolean isMailAttachmentsDownload = checkMailAttachmentsDownload(mailAttachmentList);
                    if (isMailAttachmentsDownload) {
                        mail.setComplete(true);
                        MailCacheUtils.saveMail(mail);
                        initView();
                    }
                }

                @Override
                public void callbackError(Throwable arg0, boolean arg1) {
                }

                @Override
                public void callbackCanceled(CancelledException e) {

                }
            };

            RequestParams params = (BaseApplication.getInstance()).getHttpRequestParams(source);
            params.addQueryStringParameter("itemId", mailAttachment.getId());
            params.setAutoResume(true);// 断点下载
            params.setSaveFilePath(mailAttachmentFilePath);
            params.setCancelFast(true);
            params.setMethod(HttpMethod.GET);
            Callback.Cancelable cancelable = x.http().get(params, callBack);
        }


    }

    private void initReceiverExpandTextStatus() {
        if (mail.getToMailRecipientList().size() > 1) {
            receiverExpandText.setVisibility(View.VISIBLE);
        } else {
            receiverExpandText.setVisibility(View.INVISIBLE);
        }
    }

    private void initCcExpandTextStatus() {
        if (mail.getCcMailRecipientList().size() > 1) {
            ccExpandText.setVisibility(View.VISIBLE);
        } else {
            ccExpandText.setVisibility(View.INVISIBLE);
        }
    }

    private void initReceiverFlowLayout() {
        receiverFlowLayout.removeAllViews();
        List<MailRecipient> toRecipientList = mail.getToMailRecipientList();
        if (toRecipientList.size() > 1) {
            TextView receiverTipsText = getFlowChildText();
            receiverTipsText.setTextColor(Color.parseColor("#666666"));
            receiverTipsText.setText("收件人:");
            receiverFlowLayout.addView(receiverTipsText);
            for (MailRecipient recipient : toRecipientList) {
                TextView receiverText = getFlowChildText();
                receiverText.setTextColor(Color.parseColor("#666666"));
                receiverText.setText(recipient.getName());
                receiverFlowLayout.addView(receiverText);
            }
        }

    }

    private void initCcFlowLayout() {
        ccFlowLayout.removeAllViews();
        List<MailRecipient> ccRecipientList = mail.getCcMailRecipientList();
        if (ccRecipientList.size() > 1) {
            TextView ccTipsText = getFlowChildText();
            ccTipsText.setTextColor(Color.parseColor("#666666"));
            ccTipsText.setText("抄送人:");
            ccFlowLayout.addView(ccTipsText);
            for (MailRecipient recipient : ccRecipientList) {
                TextView ccText = getFlowChildText();
                ccText.setTextColor(Color.parseColor("#666666"));
                ccText.setText(recipient.getName());
                ccFlowLayout.addView(ccText);
            }
        }

    }

    private TextView getFlowChildText() {
        int height = DensityUtil.dip2px(BaseApplication.getInstance(), 21);
        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, height);
        params.rightMargin = DensityUtil.dip2px(BaseApplication.getInstance(), 10);
        TextView textView = new TextView(this);
        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        return textView;
    }


    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.rl_forward) {
            intentMailSendActivity(MailSendActivity.MODE_FORWARD);

        } else if (i == R.id.rl_reply_all) {
            intentMailSendActivity(MailSendActivity.MODE_REPLY_ALL);

        } else if (i == R.id.rl_reply) {
            intentMailSendActivity(MailSendActivity.MODE_REPLY);

        } else if (i == R.id.ibt_mail_delete) {
            removeMail(mail);

        } else if (i == R.id.tv_mail_receiver_expand) {
            if (receiverFlowLayout.getVisibility() == View.GONE) {
                receiverFlowLayout.setVisibility(View.VISIBLE);
                receiverCollapseLayout.setVisibility(View.GONE);
                receiverExpandText.setText("收起");
            } else {
                receiverFlowLayout.setVisibility(View.GONE);
                receiverCollapseLayout.setVisibility(View.VISIBLE);
                receiverExpandText.setText("展开");
            }

        } else if (i == R.id.tv_mail_cc_expand) {
            if (ccFlowLayout.getVisibility() == View.GONE) {
                ccFlowLayout.setVisibility(View.VISIBLE);
                ccCollapseLayout.setVisibility(View.GONE);
                ccExpandText.setText("收起");
            } else {
                ccFlowLayout.setVisibility(View.GONE);
                ccCollapseLayout.setVisibility(View.VISIBLE);
                ccExpandText.setText("展开");
            }

        } else if (i == R.id.tv_install_cert) {
        }
    }


    private byte[] decryptBytes(byte[] encryptBytes) {
        String account = contactUser != null ? contactUser.getEmail() : "";
        String key = EncryptUtils.stringToMD5(account);
        try {
            encryptBytes = EncryptUtils.decode(encryptBytes, key, Constant.MAIL_ENCRYPT_IV);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptBytes;
    }

    private void intentMailSendActivity(String extraMailModel) {
        Bundle bundle = new Bundle();
        bundle.putString(MailSendActivity.EXTRA_MAIL_ID, mail.getId());
        bundle.putString(MailSendActivity.EXTRA_MAIL_MODE, extraMailModel);
        IntentUtils.startActivity(this, MailSendActivity.class, bundle);
    }

    private void removeMail(Mail mail) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDlg.show();
            JSONObject object = new JSONObject();
            object.put("Email", contactUser != null ? contactUser.getEmail() : "");
            object.put("DeleteMode", 2);
            JSONArray array = new JSONArray();
            array.add(mail.getId());
            object.put("ItemIds", array);
            apiService = new MailAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.removeMail(object.toJSONString());
        }
    }

    private void getMailDetail() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            apiService = new MailAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.getMailDetail(mail.getId(), mail.isEncrypted());
        }
    }


    private class WebService extends MailAPIInterfaceImpl {

        @Override
        public void returnMailDetailSuccess(byte[] response) {
            if (mail.isEncrypted()) {
                response = decryptBytes(response);
            }
            if (response != null) {
                mail = new Mail(new String(response));
                mail.setFolderId(MailDetailActivity.this.mail.getFolderId());
                MailDetailActivity.this.mail = mail;
                List<MailAttachment> mailAttachmentList = mail.getMailAttachmentList();
                boolean isMailAttachmentsDownload = checkMailAttachmentsDownload(mailAttachmentList);
                if (isMailAttachmentsDownload) {
                    mail.setComplete(true);
                    MailCacheUtils.saveMail(mail);
                    initView();
                } else {
                    downloadMailAttachments(mailAttachmentList);
                }
            }
        }

        @Override
        public void returnMailDetailFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(MailDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnRemoveMailSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(MailDetailActivity.this, "邮件删除成功");
            finish();
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_MAIL_REMOVE, mail));
        }

        @Override
        public void returnRemoveMailFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MailDetailActivity.this, error, errorCode);
        }
    }
}
