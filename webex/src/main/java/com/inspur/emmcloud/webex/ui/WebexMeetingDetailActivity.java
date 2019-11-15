package com.inspur.emmcloud.webex.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.popmenu.DropPopMenu;
import com.inspur.emmcloud.baselib.widget.popmenu.MenuItem;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.webex.R;
import com.inspur.emmcloud.webex.R2;
import com.inspur.emmcloud.webex.api.WebexAPIInterfaceImpl;
import com.inspur.emmcloud.webex.api.WebexAPIService;
import com.inspur.emmcloud.webex.api.WebexAPIUri;
import com.inspur.emmcloud.webex.bean.GetWebexTKResult;
import com.inspur.emmcloud.webex.bean.WebexMeeting;
import com.inspur.emmcloud.webex.util.WebexAppDownloadUtils;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
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
    @BindView(R2.id.bt_function)
    Button functionBtn;
    @BindView(R2.id.iv_photo)
    CircleTextImageView photoImg;
    @BindView(R2.id.tv_name_tips)
    TextView titleText;
    @BindView(R2.id.tv_owner)
    TextView ownerText;
    @BindView(R2.id.tv_time)
    TextView timeText;
    @BindView(R2.id.tv_duration)
    TextView durationText;
    @BindView(R2.id.tv_meeting_id)
    TextView meetingIdText;
    @BindView(R2.id.tv_meeting_password)
    TextView meetingPasswordText;
    @BindView(R2.id.rl_host_key)
    RelativeLayout hostKeyLayout;
    @BindView(R2.id.tv_host_key)
    TextView hostKeyText;

    private WebexMeeting webexMeeting;
    private boolean isOwner = false;
    private LoadingDialog loadingDialog;
    private WebexAPIService apiService;
    private String shareContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDialog = new LoadingDialog(this);
        webexMeeting = (WebexMeeting) getIntent().getSerializableExtra(EXTRA_WEBEXMEETING);
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new WebService());
        showWebexMeetingDetial();
        getWebexMeeting();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.webex_activity_detail;
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
        String photoUrl = WebexAPIUri.getWebexPhotoUrl(webexMeeting.getHostWebExID());
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
        functionBtn.setBackground(isFunctionBtnEnable ? ContextCompat.getDrawable(BaseApplication.getInstance(), R.drawable.shape_webex_buttion_add_enable) : ContextCompat.getDrawable(BaseApplication.getInstance(), R.drawable.shape_webex_buttion_add_disable));
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
        new CustomDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setMessage(getString(R.string.webex_install_tips))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String downloadUrl = PreferencesUtils.getString(BaseApplication.getInstance(), Constant.PREF_WEBEX_DOWNLOAD_URL, "");
                        new WebexAppDownloadUtils().showDownloadDialog(WebexMeetingDetailActivity.this, downloadUrl);
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
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.bt_function) {
            if (AppUtils.isAppInstalled(BaseApplication.getInstance(), webexAppPackageName)) {
                if (!isMeetingEnd()) {
                    if (isOwner) {
                        getWebexTK();
                    } else {
                        joinWebexMeeting();
                    }
                } else {
                    functionBtn.setEnabled(false);
                    functionBtn.setTextColor(Color.parseColor("#999999"));
                    functionBtn.setBackground(ContextCompat.getDrawable(BaseApplication.getInstance(), R.drawable.shape_webex_buttion_add_disable));
                    ToastUtils.show(WebexMeetingDetailActivity.this, R.string.webex_meeting_ended);
                }
            } else {
                showInstallDialog();
            }

        } else if (i == R.id.option_img) {
            showOptionMenuPop(v);

        }
    }

    private void showOptionMenuPop(View view) {
        List<MenuItem> menuItemList = new ArrayList<>();
        menuItemList.add(new MenuItem(R.drawable.webex_meeting_attendees, 1, getString(R.string.webex_participant)));
        menuItemList.add(new MenuItem(R.drawable.webex_meeting_share, 2, getString(R.string.share)));
        menuItemList.add(new MenuItem(R.drawable.webex_meeting_delete, 3, getString(R.string.delete)));
        DropPopMenu dropPopMenu = new DropPopMenu(WebexMeetingDetailActivity.this);
        dropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 1:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(WebexAttendeesActivity.EXTRA_ATTENDEES_LIST, (Serializable) webexMeeting.getWebexAttendeesList());
                        IntentUtils.startActivity(WebexMeetingDetailActivity.this, WebexAttendeesActivity.class, bundle);
                        break;
                    case 2:
                        shareWebexMeeting();
                        break;
                    case 3:
                        showDeleteMeetingWarningDlg();
                        break;
                    default:
                        break;
                }
            }
        });
        dropPopMenu.setMenuList(menuItemList);
        dropPopMenu.show(view);
    }

    private void shareWebexMeeting() {
        shareContent = webexMeeting.getConfName() + "\n" + getString(R.string.webex_time) + timeText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_code) + meetingIdText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_password_tip) + webexMeeting.getMeetingPassword();
        UMConfigure.init(this, "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(false);
        PlatformConfig.setWeixin(Constant.WECHAT_APPID, "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
        final CustomShareListener mShareListener = new CustomShareListener(WebexMeetingDetailActivity.this);
        ShareAction shareAction = new ShareAction(WebexMeetingDetailActivity.this)
                .setDisplayList(SHARE_MEDIA.EMAIL, SHARE_MEDIA.SMS)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (share_media == null) {
                            if (snsPlatform.mKeyword.equals("app_name")) {
                                Router router = Router.getInstance();
                                if (router.getService(CommunicationService.class) != null) {
                                    CommunicationService service = router.getService(CommunicationService.class);
                                    service.shareTxtPlainToConversation(shareContent, new ShareToConversationListener() {
                                        @Override
                                        public void shareSuccess(String cid) {
                                            ToastUtils.show(R.string.baselib_share_success);
                                        }

                                        @Override
                                        public void shareFail() {
                                            ToastUtils.show(R.string.baselib_share_fail);
                                        }

                                        @Override
                                        public void shareCancel() {

                                        }
                                    });
                                }
                            }
                        } else {
                            new ShareAction(WebexMeetingDetailActivity.this).withText(shareContent)
                                    .setPlatform(share_media)
                                    .setCallback(mShareListener)
                                    .share();
                        }
                    }
                });
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            shareAction = shareAction.addButton(getString(R.string.baselib_internal_share), "app_name", "ic_launcher_share", "ic_launcher_share");
        }
        shareAction.open();
    }

    @OnLongClick(R2.id.ll_meeting_content)
    public boolean onLongClick() {
        String content = webexMeeting.getConfName() + "\n" + getString(R.string.webex_time) + timeText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_code) + meetingIdText.getText().toString() + "\n"
                + getString(R.string.webex_meeting_password_tip) + webexMeeting.getMeetingPassword();
        AppUtils.copyContentToPasteBoard(BaseApplication.getInstance(), content);
        return false;
    }

    private void showDeleteMeetingWarningDlg() {
        new CustomDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setMessage(getString(R.string.webex_remove_meeting_warning_info))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
    }


    private void getWebexMeeting() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDialog.show();
            apiService.getWebexMeeting(webexMeeting.getMeetingID());
        }
    }

    private void getWebexTK() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDialog.show();
            apiService.getWebexTK();
        }
    }

    private void removeWebexMeeting() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDialog.show();
            apiService.removeMeeting(webexMeeting.getMeetingID());
        }
    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<WebexMeetingDetailActivity> mActivity;

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
            ToastUtils.show(mActivity.get(), R.string.baselib_share_fail);
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }

    private class WebService extends WebexAPIInterfaceImpl {
        @Override
        public void returnWebexMeetingSuccess(WebexMeeting webexMeeting) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebexMeetingDetailActivity.this.webexMeeting = webexMeeting;
            showWebexMeetingDetial();
        }

        @Override
        public void returnWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
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
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnRemoveWebexMeetingSuccess() {
            LoadingDialog.dimissDlg(loadingDialog);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_WEBEXMEETING, webexMeeting);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public void returnRemoveWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
        }

    }
}
