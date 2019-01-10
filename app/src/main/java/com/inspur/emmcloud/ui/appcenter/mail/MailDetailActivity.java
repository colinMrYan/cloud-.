package com.inspur.emmcloud.ui.appcenter.mail;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MailAttachmentListAdapter;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.bean.appcenter.mail.Mail;
import com.inspur.emmcloud.bean.appcenter.mail.MailAttachment;
import com.inspur.emmcloud.bean.appcenter.mail.MailRecipient;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MailCacheUtils;
import com.inspur.emmcloud.widget.FlowLayout;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.NoScrollWebView;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.imp.plugin.file.FileUtil;
import com.qmuiteam.qmui.widget.QMUIObservableScrollView;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.List;

/**
 * Created by chenmch on 2018/12/24.
 */

@ContentView(R.layout.activity_mail_details)
public class MailDetailActivity extends BaseActivity {
    public static final String EXTRA_MAIL = "extra_mail";
    public static final String EXTRA_MAIL_ID = "extra_mail_id";
    @ViewInject(R.id.tv_mail_sender)
    private TextView senderText;
    @ViewInject(R.id.tv_mail_receiver_collapse)
    private TextView receiverCollapseText;
    @ViewInject(R.id.fl_mail_receiver_expand)
    private FlowLayout receiverFlowLayout;
    @ViewInject(R.id.tv_mail_receiver_expand)
    private TextView receiverExpandText;
    @ViewInject(R.id.rl_receiver_collapse)
    private RelativeLayout receiverCollapseLayout;
    @ViewInject(R.id.rl_mail_cc)
    private RelativeLayout ccLayout;
    @ViewInject(R.id.fl_mail_cc_expand)
    private FlowLayout ccFlowLayout;
    @ViewInject(R.id.tv_mail_cc_expand)
    private TextView ccExpandText;
    @ViewInject(R.id.tv_mail_cc_collapse)
    private TextView ccCollapseText;
    @ViewInject(R.id.rl_cc_collapse)
    private RelativeLayout ccCollapseLayout;
    @ViewInject( R.id.sv_slide_data )
    QMUIObservableScrollView scrollView;

    @ViewInject(R.id.iv_flag_encrypt)
    private ImageView encryptImg;
    @ViewInject(R.id.iv_flag_sign)
    private ImageView signImg;
    @ViewInject(R.id.tv_topic)
    private TextView topicText;
    @ViewInject(R.id.tv_mail_send_time)
    private TextView sendTimeText;
    @ViewInject(R.id.lv_attachment)
    private ScrollViewWithListView attachmentListView;
    @ViewInject(R.id.wv_content)
    private NoScrollWebView contentWebView;
    @ViewInject( R.id.rl_send_about )
    private RelativeLayout sendAboutLayout;
    private MailAttachmentListAdapter mailAttachmentListAdapter;

    private Mail mail;
    private MailApiService apiService;
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDlg = new LoadingDialog(this);
        Mail simpleMail = (Mail) getIntent().getSerializableExtra(EXTRA_MAIL);
        mail = MailCacheUtils.getMail(simpleMail.getId());
        if (!mail.isComplete()) {
            getMailDetail();
        }
        initView();
    }

    private void initView() {
        encryptImg.setImageResource(mail.isEncrypted() ? R.drawable.ic_mail_flag_encrypt_yes : R.drawable.ic_mail_flag_encrypt_no);
        encryptImg.setVisibility( mail.isEncrypted()?View.VISIBLE:View.INVISIBLE );
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
        final List<MailAttachment> mailAttachmentList = mail.getMailAttachmentList();
        mailAttachmentListAdapter = new MailAttachmentListAdapter(this,mail);
        attachmentListView.setAdapter(mailAttachmentListAdapter);
        attachmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MailAttachment mailAttachment = mailAttachmentList.get(position);
                String attachmentFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT +mail.getId()+"/"+ mailAttachment.getName();
                if (FileUtils.isFileExist(attachmentFilePath)){
                    FileUtils.openFile(MailDetailActivity.this, attachmentFilePath);
                }else {
                    downloadAttachment(mailAttachment);
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
            contentWebView.loadDataWithBaseURL(null,mailBodyText, "text/html", "utf-8",null);
            contentWebView.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress == 100){
                        view.loadUrl("javascript: var meta = document.createElement('meta'); meta.setAttribute('name', 'viewport'); meta.setAttribute('content', 'width=device-width'); document.getElementsByTagName('head')[0].appendChild(meta);");
                    }
                }
            });
        }

        scrollView.addOnScrollChangedListener( new QMUIObservableScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(QMUIObservableScrollView qmuiObservableScrollView, int i, int i1, int i2, int i3) {
                int oldt = i3;
                int t = i1;
                if (oldt > t && oldt - t > 20) {
                    sendAboutLayout.setVisibility( View.VISIBLE );
                } else if (oldt < t && t - oldt > 20) {
                    sendAboutLayout.setVisibility( View.GONE );
                }
            }
        } );

    }

    private void downloadAttachment(final MailAttachment mailAttachment){
        final String attachmentFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT +mail.getId()+"/"+(mail.isEncrypted()?System.currentTimeMillis()+".tmp": mailAttachment.getName());
        String source = APIUri.getMailAttachmentUrl();
        APIDownloadCallBack callBack = new APIDownloadCallBack(getApplicationContext(), source) {
            @Override
            public void callbackStart() {
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress = (int) (current * 100.0 / total);
                String totleSize = FileUtil.formetFileSize(total);
                String currentSize = FileUtil.formetFileSize(current);
                LogUtils.jasonDebug("progress="+progress);

            }

            @Override
            public void callbackSuccess(File file) {
                if (mail.isEncrypted()){
                    byte[] encryptBytes = FileUtils.file2Bytes(file.getPath());
                    byte[] decryptBytes = decryptBytes(encryptBytes);
                    FileUtils.writeFile(MyAppConfig.LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT +mail.getId()+"/"+mailAttachment.getName(),new String(decryptBytes));
                    FileUtils.deleteFile(file.getAbsolutePath());
                }
                if (mailAttachmentListAdapter != null){
                    mailAttachmentListAdapter.notifyDataSetChanged();
                }
                ToastUtils.show(getApplicationContext(), R.string.download_success);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };

        RequestParams params = MyApplication.getInstance().getHttpRequestParams(source);
        params.addQueryStringParameter("itemId",mailAttachment.getId());
        params.setAutoResume(true);// 断点下载
        params.setSaveFilePath(attachmentFilePath);
        params.setCancelFast(true);
        params.setMethod(HttpMethod.GET);
       Callback.Cancelable cancelable =  x.http().get(params, callBack);
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
                receiverText.setTextColor(Color.parseColor("#FF4A90E2"));
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
                ccText.setTextColor(Color.parseColor("#FF4A90E2"));
                ccText.setText(recipient.getName());
                ccFlowLayout.addView(ccText);
            }
        }

    }

    private TextView getFlowChildText() {
        int height = DensityUtil.dip2px(MyApplication.getInstance(), 21);
        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, height);
        params.rightMargin = DensityUtil.dip2px(MyApplication.getInstance(), 10);
        TextView textView = new TextView(this);
        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        return textView;
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_forward:
                intentMailSendActivity(MailSendActivity.MODEL_FORWARD);
                break;
            case R.id.rl_reply_all:
                intentMailSendActivity(MailSendActivity.MODEL_REPLY_ALL);
                break;
            case R.id.rl_reply:
                intentMailSendActivity(MailSendActivity.MODEL_REPLY);
                break;
            case R.id.bt_mail_delete:

                break;
            case R.id.bt_mail_tab:
                break;
            case R.id.tv_mail_receiver_expand:
                if (receiverFlowLayout.getVisibility() == View.GONE) {
                    receiverFlowLayout.setVisibility(View.VISIBLE);
                    receiverCollapseLayout.setVisibility(View.GONE);
                    receiverExpandText.setText("收起");
                } else {
                    receiverFlowLayout.setVisibility(View.GONE);
                    receiverCollapseLayout.setVisibility(View.VISIBLE);
                    receiverExpandText.setText("展开");
                }
                break;
            case R.id.tv_mail_cc_expand:
                if (ccFlowLayout.getVisibility() == View.GONE) {
                    ccFlowLayout.setVisibility(View.VISIBLE);
                    ccCollapseLayout.setVisibility(View.GONE);
                    ccExpandText.setText("收起");
                } else {
                    ccFlowLayout.setVisibility(View.GONE);
                    ccCollapseLayout.setVisibility(View.VISIBLE);
                    ccExpandText.setText("展开");
                }
                break;
            case R.id.tv_install_cert:
                break;
        }
    }

    private void delectCurrentMail(){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService = new MailApiService(this);
            apiService.setAPIInterface(new WebService());
            apiService.getMailDetail(mail.getId(),mail.isEncrypted());
        }
    }

    private void intentMailSendActivity(String extraMailModel){
        Bundle bundle = new Bundle();
        bundle.putString(MailSendActivity.EXTRA_MAIL_ID,mail.getId());
        bundle.putString(MailSendActivity.EXTRA_MAIL_MODEL,extraMailModel);
        IntentUtils.startActivity(this,MailSendActivity.class,bundle);
    }

    private void getMailDetail() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService = new MailApiService(this);
            apiService.setAPIInterface(new WebService());
            apiService.getMailDetail(mail.getId(),mail.isEncrypted());
        }
    }

    private byte[] decryptBytes(byte[] encryptBytes){
        String account = ContactUserCacheUtils.getUserMail(MyApplication.getInstance().getUid());
        String key = EncryptUtils.stringToMD5(account);

        try{
            encryptBytes = EncryptUtils.decode(encryptBytes,key, Constant.MAIL_ENCRYPT_IV);
        }catch (Exception e){
            e.printStackTrace();
        }
        return encryptBytes;
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailDetailSuccess(byte[] response) {
            if (mail.isEncrypted()){
                response = decryptBytes(response);
            }
            LoadingDialog.dimissDlg(loadingDlg);
            if (response != null){
                LogUtils.jasonDebug("response="+new String(response));
                mail = new Mail(new String(response));
                mail.setComplete(true);
                mail.setFolderId(MailDetailActivity.this.mail.getFolderId());
                MailCacheUtils.saveMail(mail);
                MailDetailActivity.this.mail = mail;
                initView();
            }
        }

        @Override
        public void returnMailDetailFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MailDetailActivity.this, error, errorCode);
        }
    }
}
