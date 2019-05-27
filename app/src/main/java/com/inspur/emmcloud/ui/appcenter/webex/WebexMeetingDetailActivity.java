package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexTKResult;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.mine.setting.RecommendAppActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppDownloadUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.WebServiceRouterManager;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.umeng.commonsdk.UMConfigure;
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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

/**
 * Created by chenmch on 2018/10/11.
 */
public class WebexMeetingDetailActivity extends BaseActivity {
    public static final String EXTRA_WEBEXMEETING = "WebexMeeting";
    private final String webexAppPackageName = "com.cisco.webex.meetings";
    private final int REQUEST_SELECT_CONTACT = 1;
    @BindView(R.id.bt_function)
    Button functionBtn;
    @BindView(R.id.iv_photo)
    CircleTextImageView photoImg;
    @BindView(R.id.tv_name_tips)
    TextView titleText;
    @BindView(R.id.tv_owner)
    TextView ownerText;
    @BindView(R.id.tv_time)
    TextView timeText;
    @BindView(R.id.tv_duration)
    TextView durationText;
    @BindView(R.id.tv_meeting_id)
    TextView meetingIdText;
    @BindView(R.id.tv_meeting_password)
    TextView meetingPasswordText;
    @BindView(R.id.rl_host_key)
    RelativeLayout hostKeyLayout;
    @BindView(R.id.tv_host_key)
    TextView hostKeyText;

    private WebexMeeting webexMeeting;
    private boolean isOwner = false;
    private LoadingDialog loadingDialog;
    private WebexAPIService apiService;
    private String fakeMessageId;
    private String shareContent;
    private PopupWindow optionMenuPop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        loadingDialog = new LoadingDialog(this);
        webexMeeting = (WebexMeeting) getIntent().getSerializableExtra(EXTRA_WEBEXMEETING);
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new WebService());
        showWebexMeetingDetial();
        getWebexMeeting();
        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_webex_detail;
    }

    private void showWebexMeetingDetial() {
        titleText.setText(webexMeeting.getConfName());
        Calendar startCalendar = webexMeeting.getStartDateCalendar();
        String timeYDM = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(this, TimeUtils.FORMAT_YEAR_MONTH_DAY));
        String week = TimeUtils.getWeekDay(this, startCalendar);
        String timeHrMin = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(this, TimeUtils.FORMAT_HOUR_MINUTE));
        timeText.setText(timeYDM + " " + week + " " + timeHrMin);
        int duration = webexMeeting.getDuration();
        int hour = duration / 60;
        int min = duration % 60;
        String timeHour = "";
        if (hour == 1) {
            timeHour = hour + getString(R.string.hour) + " ";
        } else if (hour > 1) {
            timeHour = hour + getString(R.string.hours) + " ";
        }
        String timeMin = (min == 0) ? "" : min + getString(R.string.mins);
        durationText.setText(timeHour + timeMin);
        meetingPasswordText.setText(webexMeeting.getMeetingPassword());
        meetingIdText.setText(formatMeetingID(webexMeeting.getMeetingID()));
        String photoUrl = APIUri.getWebexPhotoUrl(webexMeeting.getHostWebExID());
        ImageDisplayUtils.getInstance().displayImage(photoImg, photoUrl, R.drawable.icon_person_default);
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String myEmail = getMyInfoResult.getMail();
        isOwner = webexMeeting.getHostWebExID().equals(myEmail);

        ownerText.setText(isOwner ? getString(R.string.mine) : webexMeeting.getHostUserName());
        functionBtn.setText(isOwner ? getString(R.string.webex_start) : getString(R.string.join));
        hostKeyLayout.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        hostKeyText.setText(webexMeeting.getHostKey());
        boolean isFunctionBtnEnable = !isMeetingEnd() && (isOwner || webexMeeting.isInProgress());
        functionBtn.setEnabled(isFunctionBtnEnable);
        functionBtn.setTextColor(isFunctionBtnEnable ? Color.parseColor("#ffffff") : Color.parseColor("#999999"));
        functionBtn.setBackground(isFunctionBtnEnable ? ContextCompat.getDrawable(MyApplication.getInstance(), R.drawable.shape_webex_buttion_add_enable) : ContextCompat.getDrawable(MyApplication.getInstance(), R.drawable.shape_webex_buttion_add_disable));
    }

    private String formatMeetingID(String meetingID) {
        if (StringUtils.isBlank(meetingID)) {
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
            case R.id.ibt_back:
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
                        ToastUtils.show(WebexMeetingDetailActivity.this, R.string.webex_meeting_ended);
                    }
                } else {
                    showInstallDialog();
                }
                break;
            case R.id.rl_delete:
                optionMenuPop.dismiss();
                showDeleteMeetingWarningDlg();
                break;
            case R.id.rl_share:
                optionMenuPop.dismiss();
                shareWebexMeeting();
                break;
            case R.id.option_img:
                showOptionMenuPop(v);
                break;
            case R.id.rl_attendees:
                optionMenuPop.dismiss();
                Bundle bundle = new Bundle();
                bundle.putSerializable(WebexAttendeesActivity.EXTRA_ATTENDEES_LIST, (Serializable) webexMeeting.getWebexAttendeesList());
                IntentUtils.startActivity(this, WebexAttendeesActivity.class, bundle);
                break;
        }
    }

    private void showOptionMenuPop(View view) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_webex_meeting_detail_option, null);
        optionMenuPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        contentView.findViewById(R.id.rl_delete).setVisibility(isOwner ? View.VISIBLE : View.INVISIBLE);
        optionMenuPop.setTouchable(true);
        optionMenuPop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        optionMenuPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(WebexMeetingDetailActivity.this, 1.0f);
            }
        });
        optionMenuPop.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        AppUtils.setWindowBackgroundAlpha(this, 0.8f);
        // 设置好参数之后再show
        optionMenuPop.showAsDropDown(view);
    }

    private void shareWebexMeeting() {
        shareContent = webexMeeting.getConfName() + "\n" + getString(R.string.webex_time) + timeText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_code) + meetingIdText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_password_tip) + webexMeeting.getMeetingPassword();
        UMConfigure.init(this, "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(false);
        PlatformConfig.setWeixin("wx4eb8727ea9c26495", "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
        final CustomShareListener mShareListener = new CustomShareListener(WebexMeetingDetailActivity.this);
        new ShareAction(WebexMeetingDetailActivity.this)
                .setDisplayList(SHARE_MEDIA.EMAIL, SHARE_MEDIA.SMS)
                .addButton(getString(R.string.webex_internal_share), "app_name", "ic_launcher", "ic_launcher")
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (share_media == null) {
                            if (snsPlatform.mKeyword.equals("app_name")) {
                                Intent intent = new Intent();
                                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
                                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
                                ArrayList<String> uidList = new ArrayList<>();
                                uidList.add(MyApplication.getInstance().getUid());
                                intent.putStringArrayListExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
                                intent.putExtra(ContactSearchFragment.EXTRA_CONTAIN_ME, false);
                                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.news_share));
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

    @OnLongClick(R.id.ll_meeting_content)
    public boolean onLongClick() {
        String content = webexMeeting.getConfName() + "\n" + getString(R.string.webex_time) + timeText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_code) + meetingIdText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_password_tip) + webexMeeting.getMeetingPassword();
        AppUtils.copyContentToPasteBoard(MyApplication.getInstance(), content);
        return false;
    }

    private void showDeleteMeetingWarningDlg() {
        new MyQMUIDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setMessage(getString(R.string.webex_remove_meeting_warning_info))
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
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(WebexMeetingDetailActivity.this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            sendShareMessage(conversation.getId(), content);
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(WebexMeetingDetailActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            sendShareMessage(getCreateSingleChannelResult.getCid(), content);
                        }

                        @Override
                        public void createDirectChannelFail() {
                            ToastUtils.show(WebexMeetingDetailActivity.this, R.string.news_share_fail);
                        }
                    });
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
            if (fakeMessageId != null && String.valueOf(eventMessage.getId()).equals(fakeMessageId)) {
                if (eventMessage.getStatus() == 200) {
                    ToastUtils.show(WebexMeetingDetailActivity.this, R.string.news_share_success);
                } else {
                    ToastUtils.show(WebexMeetingDetailActivity.this, R.string.news_share_fail);
                }
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
            if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
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
                Message message = CommunicationUtils.combinLocalTextPlainMessage(content, cid, new HashMap<String, String>());
                fakeMessageId = message.getId();
                WSAPIService.getInstance().sendChatTextPlainMsg(message);
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
            // ToastUtils.show(mActivity.get(), R.string.news_share_success);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.show(mActivity.get(), R.string.news_share_fail);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

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
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
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
            ToastUtils.show(WebexMeetingDetailActivity.this, R.string.news_share_success);
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            ToastUtils.show(WebexMeetingDetailActivity.this, R.string.news_share_fail);
        }
    }
}
