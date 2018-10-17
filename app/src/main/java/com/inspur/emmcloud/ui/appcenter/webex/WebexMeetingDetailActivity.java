package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.api.apiservice.WebexAPIService;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexTKResult;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.mine.setting.RecommendAppActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppDownloadUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_detail)
public class WebexMeetingDetailActivity extends BaseActivity {
    public static final String EXTRA_WEBEXMEETING = "WebexMeeting";
    private final String webexAppPackageName = "com.cisco.webex.meetings";
    private final int REQUEST_SELECT_CONTACT = 1;
    @ViewInject(R.id.bt_function)
    private Button functionBtn;
    @ViewInject(R.id.iv_photo)
    private CircleTextImageView photoImg;
    @ViewInject(R.id.tv_title)
    private TextView titleText;
    @ViewInject(R.id.tv_owner)
    private TextView ownerText;
    @ViewInject(R.id.tv_time)
    private TextView timeText;
    @ViewInject(R.id.tv_duration)
    private TextView durationText;
    @ViewInject(R.id.tv_meeting_id)
    private TextView meetingIdText;
    @ViewInject(R.id.tv_meeting_password)
    private TextView meetingPasswordText;
    @ViewInject(R.id.rl_host_key)
    private RelativeLayout hostKeyLayout;
    @ViewInject(R.id.tv_host_key)
    private TextView hostKeyText;
    @ViewInject(R.id.iv_delete)
    private ImageView deleteImg;

    private WebexMeeting webexMeeting;
    private boolean isOwner = false;
    private LoadingDialog loadingDialog;
    private WebexAPIService apiService;
    private String fakeMessageId;
    private String shareContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(this);
        webexMeeting = (WebexMeeting) getIntent().getSerializableExtra(EXTRA_WEBEXMEETING);
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new WebService());
        showWebexMeetingDetial();
        getWebexMeeting();
        EventBus.getDefault().register(this);
    }


    private void showWebexMeetingDetial() {
        titleText.setText(webexMeeting.getConfName());
        Calendar startCalendar = webexMeeting.getStartDateCalendar();
        String timeYDM = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(MyApplication.getInstance(), TimeUtils.FORMAT_YEAR_MONTH_DAY));
        String week = TimeUtils.getWeekDay(MyApplication.getInstance(), startCalendar);
        String timeHrMin = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(MyApplication.getInstance(), TimeUtils.FORMAT_HOUR_MINUTE));
        timeText.setText(timeYDM + " " + week + " " + timeHrMin);
        int duration = webexMeeting.getDuration();
        int hour = duration / 60;
        int min = duration % 60;
        String hourtext = (hour == 0) ? "" : hour + getString(R.string.hour);
        String mintext = (min == 0) ? "" : min + getString(R.string.min);
        durationText.setText(hourtext + mintext);
        meetingPasswordText.setText(webexMeeting.getMeetingPassword());
        meetingIdText.setText(formatMeetingID(webexMeeting.getMeetingID()));
        String photoUrl = APIUri.getWebexPhotoUrl(webexMeeting.getHostWebExID());
        ImageDisplayUtils.getInstance().displayImage(photoImg, photoUrl, R.drawable.icon_person_default);
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String myEmail = getMyInfoResult.getMail();
        isOwner = webexMeeting.getHostWebExID().equals(myEmail);
        deleteImg.setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
        ownerText.setText(isOwner ? getString(R.string.mine) : webexMeeting.getHostUserName());
        functionBtn.setText(isOwner ? getString(R.string.webex_start) : getString(R.string.join));
        hostKeyLayout.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        hostKeyText.setText(webexMeeting.getHostKey());
        boolean isMeetingEnd = isMeetingEnd();
        functionBtn.setEnabled(!isMeetingEnd);
        functionBtn.setTextColor(isMeetingEnd ? Color.parseColor("#999999") : Color.parseColor("#ffffff"));
        functionBtn.setBackground(isMeetingEnd ? ContextCompat.getDrawable(MyApplication.getInstance(), R.drawable.shape_webex_buttion_add_disable) : ContextCompat.getDrawable(MyApplication.getInstance(), R.drawable.shape_webex_buttion_add_enable));
    }

    private String formatMeetingID(String meetingID) {
        if (StringUtils.isBlank(meetingID)){
            return "";
        }
        char[] strs = meetingID.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            sb.append(strs[i]);
            if (i != 0 && (i + 1) % 3 == 0) {
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private boolean isMeetingEnd() {
        return webexMeeting.getStartDateCalendar().getTimeInMillis() + webexMeeting.getDuration() * 60000 <= System.currentTimeMillis();
    }

    /**
     * 安装提示
     */
    private void showInstallDialog() {
        new MyQMUIDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setMessage(getString(R.string.webex_install_tips))
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        String downloadUrl = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_WEBEX_DOWNLOAD_URL, "");
                        new AppDownloadUtils().showDownloadDialog(WebexMeetingDetailActivity.this, downloadUrl);
                    }
                })
                .show();
    }


    private void joinWebexMeeting() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("wbx://meeting"));
            intent.putExtra("MK", webexMeeting.getMeetingID());
            intent.putExtra("MPW", webexMeeting.getMeetingPassword());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startWebexMeeting(String tk) {
        try {
            String sessionTicket = URLEncoder.encode(tk, "UTF-8");
            String webexID = URLEncoder.encode(webexMeeting.getHostWebExID(), "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            String uri = "wbx://meeting/inspurcloud.webex.com.cn/inspurcloud?MK=" + webexMeeting.getMeetingID() + "&MPW=" + webexMeeting.getMeetingPassword() + "&MTGTK=&sitetype=TRAIN&r2sec=1&UN=" + webexID + "&TK=" + sessionTicket;
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.bt_function:
                if (AppUtils.isAppInstalled(MyApplication.getInstance(), webexAppPackageName)) {
                    if (!isMeetingEnd()) {
                        if (isOwner) {
                            getWebexTK();
                        } else {
                            joinWebexMeeting();
                        }
                    } else {
                        functionBtn.setEnabled(false);
                        functionBtn.setTextColor(Color.parseColor("#999999"));
                        functionBtn.setBackground(ContextCompat.getDrawable(MyApplication.getInstance(), R.drawable.shape_webex_buttion_add_disable));
                        ToastUtils.show(MyApplication.getInstance(), R.string.webex_meeting_ended);
                    }
                } else {
                    showInstallDialog();
                }
                break;
            case R.id.iv_delete:
                showDeleteMeetingWarningDlg();
                break;
            case R.id.iv_share:
                shareWebexMeeting();
                break;
        }
    }

    private void shareWebexMeeting() {
        shareContent = webexMeeting.getConfName()+"\n"+getString(R.string.webex_time)+timeText.getText()+"\n"
                +getString(R.string.webex_meeting_code)+webexMeeting.getMeetingID()+"\n"
                +getString(R.string.webex_meeting_password_tip)+webexMeeting.getMeetingPassword();
        UMShareAPI.get(this);
        PlatformConfig.setWeixin("wx4eb8727ea9c26495", "56a0426315f1d0985a1cc1e75e96130d");
        final CustomShareListener mShareListener = new CustomShareListener(WebexMeetingDetailActivity.this);
        new ShareAction(WebexMeetingDetailActivity.this)
                .setDisplayList(SHARE_MEDIA.EMAIL,  SHARE_MEDIA.SMS)
                .addButton("internal_share", "app_name", "ic_launcher", "ic_launcher")
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (share_media == null) {
                            if (snsPlatform.mKeyword.equals("app_name")) {
                                Intent intent = new Intent();
                                intent.putExtra("select_content", 0);
                                intent.putExtra("isMulti_select", false);
                                intent.putExtra("isContainMe", true);
                                intent.putExtra("title", getString(R.string.news_share));
                                intent.setClass(WebexMeetingDetailActivity.this,
                                        ContactSearchActivity.class);
                                startActivityForResult(intent, REQUEST_SELECT_CONTACT);
                            }
                        } else {
                            new ShareAction(WebexMeetingDetailActivity.this).withText(shareContent)
                                    .setPlatform(share_media)
                                    .setCallback(mShareListener)
                                    .share();
                        }
                    }
                })
                .open();

    }

    @Event(value = {R.id.tv_meeting_id, R.id.tv_meeting_password}, type = View.OnLongClickListener.class)
    private boolean onLongClick(View v) {
        AppUtils.copyContentToPasteBoard(MyApplication.getInstance(), (TextView) v);
        return false;
    }

    private void showDeleteMeetingWarningDlg() {
        new MyQMUIDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setMessage(getString(R.string.remove_webex_meeting_warning_info))
                .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        removeWebexMeeting();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_CONTACT) {
            SearchModel searchModel = ((List<SearchModel>) data.getSerializableExtra("selectMemList")).get(0);
            String id = searchModel.getId();
            if (searchModel.getType().equals(SearchModel.TYPE_USER)) {
                createDirectChannel(id, shareContent);
            } else if (searchModel.getType().equals(SearchModel.TYPE_GROUP)) {
                sendShareMessage(id, shareContent);
            }
        }
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid, final String content) {
        new ChatCreateUtils().createDirectChannel(WebexMeetingDetailActivity.this, uid,
                new ChatCreateUtils.OnCreateDirectChannelListener() {
                    @Override
                    public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                        sendShareMessage(getCreateSingleChannelResult.getCid(), content);
                    }

                    @Override
                    public void createDirectChannelFail() {
                        ToastUtils.show(MyApplication.getInstance(), R.string.news_share_fail);
                    }
                });
    }


    private static class CustomShareListener implements UMShareListener {

        private WeakReference<RecommendAppActivity> mActivity;

        private CustomShareListener(WebexMeetingDetailActivity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.show(mActivity.get(), R.string.news_share_success);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.show(mActivity.get(), R.string.news_share_fail);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                if (fakeMessageId != null && String.valueOf(eventMessage.getExtra()).equals(fakeMessageId)) {
                    ToastUtils.show(MyApplication.getInstance(), R.string.news_share_success);
                }
            } else {
                ToastUtils.show(MyApplication.getInstance(), R.string.news_share_fail);
            }

        }

    }

    /**
     * 聊天中分享
     *
     * @param cid
     */
    private void sendShareMessage(String cid, String content) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            if (MyApplication.getInstance().isV0VersionChat()) {
                ChatAPIService apiService = new ChatAPIService(
                        WebexMeetingDetailActivity.this);
                apiService.setAPIInterface(new WebService());
                JSONObject msgBodyObj = new JSONObject();
                try {
                    msgBodyObj.put("source", content);
                    msgBodyObj.put("mentions", new JSONArray());
                    msgBodyObj.put("urls", new JSONArray());
                    msgBodyObj.put("tmpId", AppUtils.getMyUUID(MyApplication.getInstance()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                apiService.sendMsg(cid, msgBodyObj.toString(), "txt_rich", System.currentTimeMillis() + "");
            } else {
                Message message = CommunicationUtils.combinLocalTextPlainMessage(cid, content, new HashMap<String, String>());
                fakeMessageId = message.getId();
                WSAPIService.getInstance().sendChatExtendedLinksMsg(message);
            }

        }

    }


    private void getWebexMeeting() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDialog.show();
            apiService.getWebexMeeting(webexMeeting.getMeetingID());
        }
    }

    private void getWebexTK() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDialog.show();
            apiService.getWebexTK();
        }
    }

    private void removeWebexMeeting() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDialog.show();
            apiService.removeMeeting(webexMeeting.getMeetingID());
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnWebexMeetingSuccess(WebexMeeting webexMeeting) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebexMeetingDetailActivity.this.webexMeeting = webexMeeting;
            showWebexMeetingDetial();
        }

        @Override
        public void returnWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            finish();
        }

        @Override
        public void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            String tk = getWebexTKResult.getTk();
            startWebexMeeting(tk);
        }

        @Override
        public void returnWebexTKFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnRemoveWebexMeetingSuccess() {
            LoadingDialog.dimissDlg(loadingDialog);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_WEBEXMEETING, webexMeeting);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnRemoveWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId) {
            LoadingDialog.dimissDlg(loadingDialog);
            ToastUtils.show(MyApplication.getInstance(), R.string.news_share_success);
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            ToastUtils.show(MyApplication.getInstance(), R.string.news_share_fail);
        }
    }
}
