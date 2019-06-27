package com.inspur.emmcloud.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.IcsFileUtil;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.schedule.calendar.CalendarEvent;
import com.inspur.emmcloud.bean.system.ChangeTabBean;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.appcenter.groupnews.GroupNewsActivity;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.ui.appcenter.webex.WebexMyMeetingActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.find.AnalysisActivity;
import com.inspur.emmcloud.ui.find.DocumentActivity;
import com.inspur.emmcloud.ui.find.KnowledgeActivity;
import com.inspur.emmcloud.ui.find.trip.TripInfoActivity;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.ui.schedule.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.ui.schedule.task.TaskAddActivity;
import com.inspur.emmcloud.util.privates.AppId2AppAndOpenAppUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.MailLoginUtils;
import com.inspur.emmcloud.util.privates.ProfileUtils;
import com.inspur.emmcloud.util.privates.WebAppUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * scheme统一处理类
 */

public class SchemeHandleActivity extends BaseActivity {
    private boolean isFirst = true;

    @Override
    public void onCreate() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        //因为MyActivityLifecycleCallbacks需要在onActivityStarted的时候处理二次验证问题，所有openScheme
        // 需要在onStart中执行，同时要防止onStart方法多次执行
        if (isFirst) {
            if (MyApplication.getInstance().isSafeLock()) {
                if (MyApplication.getInstance().isSafeLock()) {
                    if (!EventBus.getDefault().isRegistered(this)) {
                        EventBus.getDefault().register(this);
                    }
                } else {
                    openScheme();
                }
            } else {
                openScheme();
            }
            isFirst = false;
        }

    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    @Override
    public int getStatusType() {
        return STATUS_TRANSPARENT;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        openScheme();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSafeUnLockMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_SAFE_UNLOCK)) {
            new ProfileUtils(SchemeHandleActivity.this, new CommonCallBack() {
                @Override
                public void execute() {
                    openScheme();
                }
            }).initProfile(false);

        }
    }

    /**
     * 打开具体的要么
     */
    private void openScheme() {
        if (((MyApplication) getApplicationContext()).isHaveLogin()) {
            openIndexActivity(this);
            //此处加延时操作，为了让打开通知时IndexActivity走onCreate()方法
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String action = "";
                    if (getIntent() != null) {
                        action = getIntent().getAction();
                    }
                    if (!StringUtils.isBlank(action) && (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action))) {
                        handleShareIntent();
                    } else {
                        Uri uri = getIntent().getData();
                        if (uri == null) {
                            finish();
                            return;
                        }
                        String scheme = uri.getScheme();
                        String host = uri.getHost();
                        if (scheme == null || host == null) {
                            finish();
                            return;
                        }
                        Bundle bundle = new Bundle();
                        switch (scheme) {
                            case "ecc-contact":
                            case "ecm-contact":
                                bundle.putString("uid", host);
                                if (host.startsWith("BOT")) {
                                    IntentUtils.startActivity(SchemeHandleActivity.this, RobotInfoActivity.class, bundle, true);
                                } else {
                                    IntentUtils.startActivity(SchemeHandleActivity.this, UserInfoActivity.class, bundle, true);
                                }
                                break;
                            case "ecc-component":
                                openComponentScheme(uri, host);
                                break;
                            case "ecc-app-react-native":
                                bundle.putString(scheme, uri.toString());
                                IntentUtils.startActivity(SchemeHandleActivity.this, ReactNativeAppActivity.class, bundle, true);
                                break;
                            case "gs-msg":
                                if (!NetUtils.isNetworkConnected(SchemeHandleActivity.this)) {
                                    finish();
                                    break;
                                }
                                String openMode = uri.getQueryParameter("openMode");
                                openWebApp(host, openMode);
                                break;
                            case "ecc-channel":
                                bundle.putString("cid", host);
                                bundle.putBoolean(ConversationActivity.EXTRA_NEED_GET_NEW_MESSAGE, true);
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    IntentUtils.startActivity(SchemeHandleActivity.this,
                                            ChannelV0Activity.class, bundle, true);
                                } else {
                                    IntentUtils.startActivity(SchemeHandleActivity.this,
                                            ConversationActivity.class, bundle, true);
                                }
                                break;
                            case "ecc-app":
                                AppId2AppAndOpenAppUtils appId2AppAndOpenAppUtils = new AppId2AppAndOpenAppUtils(SchemeHandleActivity.this);
                                appId2AppAndOpenAppUtils.setOnFinishActivityListener(new AppId2AppAndOpenAppUtils.OnFinishActivityListener() {
                                    @Override
                                    public void onFinishActivity() {
                                        finish();
                                    }
                                });
                                appId2AppAndOpenAppUtils.getAppInfoById(uri);
                                break;

                            case "ecc-calendar-jpush":
                                String content = getIntent().getStringExtra("content");
                                if (content != null) {
                                    JSONObject calEventObj = JSONUtils.getJSONObject(content);
                                    CalendarEvent calendarEvent = new CalendarEvent(calEventObj);
                                    Intent intent = new Intent(SchemeHandleActivity.this, CalendarAddActivity.class);
                                    intent.putExtra("calEvent", calendarEvent);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                                finish();
                                break;
                            case "ecc-app-change-tab":
                                EventBus.getDefault().post(new ChangeTabBean(Constant.APP_TAB_BAR_APPLICATION));
                                break;
                            case "emm":
                                if (host.equals("news")) {
                                    IntentUtils.startActivity(SchemeHandleActivity.this, GroupNewsActivity.class, true);
                                } else if (host.equals("volume")) {
                                    IntentUtils.startActivity(SchemeHandleActivity.this, VolumeHomePageActivity.class, true);
                                }
                                break;
                            case "inspur-ecc-native":
                                openNativeSchemeByHost(host, uri, getIntent());
                                break;
                            case "content":
                                IcsFileUtil.parseIcsFile(SchemeHandleActivity.this, uri);
                                break;
                            default:
                                finish();
                                break;
                        }
                    }
                }
            }, 1);

        } else {
            ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_MAIN).navigation();
            finish();
        }
    }

    /**
     * 处理带分享功能的Action
     */
    private void handleShareIntent() {
        String action = getIntent().getAction();
        List<String> uriList = new ArrayList<>();
        if (Intent.ACTION_SEND.equals(action)) {
            Uri uri = FileUtils.getShareFileUri(getIntent());
            if (isLinkShare()) {
                handleLinkShare(getShareLinkContent());
                return;
            } else if (uri != null) {
                uriList.add(GetPathFromUri4kitkat.getPathByUri(MyApplication.getInstance(), uri));
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            List<Uri> fileUriList = FileUtils.getShareFileUriList(getIntent());
            for (int i = 0; i < fileUriList.size(); i++) {
                uriList.add(GetPathFromUri4kitkat.getPathByUri(MyApplication.getInstance(), fileUriList.get(i)));
            }
        }
        if (uriList.size() > 0) {
            startVolumeShareActivity(uriList);
        } else {
            ToastUtils.show(SchemeHandleActivity.this, getString(R.string.share_not_support));
            finish();
        }
    }

    /**
     * 获取title和url
     *
     * @return
     */
    private HashMap<String, String> getShareLinkContent() {
        Intent intent = getIntent();
        String urlStr = "";
        String titleStr = "";
        String digest = "";
        HashMap<String, String> shareLinkMap = new HashMap<>();
        if (intent != null) {
            String text = intent.getExtras().getString(Intent.EXTRA_TEXT);
            String subject = intent.getExtras().getString(Intent.EXTRA_SUBJECT);
            if (text != null && subject != null) {
                urlStr = getShareUrl(text);
                titleStr = subject;
                digest = text.replace(getShareUrl(text), "");
            } else if (text != null && subject == null) {
                urlStr = getShareUrl(text);
                titleStr = text.replace(urlStr, "");
            } else if (text == null && subject == null) {
                return shareLinkMap;
            }
            shareLinkMap.put("title", StringUtils.isBlank(titleStr) ? getString(R.string.share_default_title) : titleStr);
            shareLinkMap.put("url", urlStr);
            shareLinkMap.put("digest", digest);
        }
        return shareLinkMap;
    }

    /**
     * 获取url
     *
     * @param text
     */
    private String getShareUrl(String text) {
//        Pattern p = Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?", Pattern.CASE_INSENSITIVE);
        Pattern p = Pattern.compile(Constant.PATTERN_URL, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(text);
        matcher.find();
        return matcher.group();
    }

    /**
     * 是一个链接分享
     *
     * @return
     */
    private boolean isLinkShare() {
        Intent intent = getIntent();
        return intent.getExtras() != null && !StringUtils.isBlank(intent.getExtras().getString(Intent.EXTRA_TEXT));
    }

    /**
     * 处理分享url
     */
    private void handleLinkShare(HashMap<String, String> shareLinkContentMap) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", shareLinkContentMap.get("url"));
            jsonObject.put("poster", "");
            jsonObject.put("digest", shareLinkContentMap.get("digest"));
            jsonObject.put("title", shareLinkContentMap.get("title"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setClass(SchemeHandleActivity.this, ShareLinkActivity.class);
        intent.putExtra(Constant.SHARE_LINK, jsonObject.toString());
        startActivity(intent);
        finish();
    }

    /**
     * @param uriList
     */
    private void startVolumeShareActivity(List<String> uriList) {
        Intent intent = new Intent();
        intent.setClass(SchemeHandleActivity.this, ShareFilesActivity.class);
        intent.putExtra(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
        startActivity(intent);
        finish();
    }


    /**
     * 打开web应用
     *
     * @param host
     * @param openMode
     */
    private void openWebApp(String host, final String openMode) {
        String url = APIUri.getGSMsgSchemeUrl(host);
        new WebAppUtils(SchemeHandleActivity.this, new WebAppUtils.OnGetWebAppRealUrlListener() {
            @Override
            public void getWebAppRealUrlSuccess(String webAppUrl) {
                boolean isUriHasTitle = (openMode != null && openMode.equals("1"));
                Bundle bundle = new Bundle();
                bundle.putString("uri", webAppUrl);
                bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, isUriHasTitle);
                ARouter.getInstance().build("/web/main").with(bundle).navigation();
                SchemeHandleActivity.this.finish();
            }

            @Override
            public void getWebAppRealUrlFail() {
                ToastUtils.show(SchemeHandleActivity.this, R.string.react_native_app_open_failed);
                finish();
            }
        }).getWebAppRealUrl(url);
    }

    /**
     * 打开主tab页
     *
     * @param context
     */
    private void openIndexActivity(Context context) {
        Intent indexIntent = new Intent(context, IndexActivity.class);
        if (!((MyApplication) context.getApplicationContext()).isIndexActivityRunning()) {
            context.startActivity(indexIntent);
        } else if (!((MyApplication) context.getApplicationContext()).getIsActive()) {
            indexIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(indexIntent);
        }
    }

    private void openNativeSchemeByHost(String host, Uri query, Intent intent) {
        SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.APP_TAB_BAR_WORK);
        switch (host) {
            case "calendar":
                if (query.getQuery() == null) {
                    simpleEventMessage.setMessageObj(Constant.ACTION_CALENDAR);
                    EventBus.getDefault().post(simpleEventMessage);
                } else if (!StringUtils.isBlank(query.getQueryParameter("id"))) {
                    openScheduleActivity(query.getQueryParameter("id"), CalendarAddActivity.class);
                }
                finish();
                break;
            case "to-do":
                if (query.getQuery() == null) {
                    simpleEventMessage.setMessageObj(Constant.ACTION_TASK);
                    EventBus.getDefault().post(simpleEventMessage);
                } else if (!StringUtils.isBlank(query.getQueryParameter("id"))) {
                    openScheduleActivity(query.getQueryParameter("id"), TaskAddActivity.class);
                }
                finish();
                break;
            case "meeting":
                if (query.getQuery() == null) {
                    simpleEventMessage.setMessageObj(Constant.ACTION_MEETING);
                    EventBus.getDefault().postSticky(simpleEventMessage);
                } else if (!StringUtils.isBlank(query.getQueryParameter("id"))) {
                    openScheduleActivity(query.getQueryParameter("id"), MeetingDetailActivity.class);
                }
                finish();
                break;
            case "webex":
                String installUri = intent.getExtras().getString("installUri", "");
                Bundle bundle = new Bundle();
                bundle.putString("installUri", installUri);
                IntentUtils.startActivity(SchemeHandleActivity.this, WebexMyMeetingActivity.class, bundle, true);
                break;
            case "mail":
                new MailLoginUtils().loginMail(this);
                break;
            default:
                finish();
                break;
        }
    }

    /**
     * 打开日程的Activity
     *
     * @param query
     * @param scheduleActivity
     */
    private void openScheduleActivity(String query, Class scheduleActivity) {
        Bundle bundle = new Bundle();
        bundle.putString(Constant.SCHEDULE_QUERY, query);
        IntentUtils.startActivity(SchemeHandleActivity.this, scheduleActivity, bundle);
    }

    private void openComponentScheme(Uri uri, String host) {
        Bundle bundle = new Bundle();
        switch (host) {
            case "stastistics":
                IntentUtils.startActivity(this, AnalysisActivity.class, bundle, true);
                break;
            case "trips":
                String path = uri.getPath();
                String tripId = path.split("/")[1];
                bundle.putString("tripId", tripId);
                IntentUtils.startActivity(this, TripInfoActivity.class, bundle, true);
                break;
            case "news.ecc":
                IntentUtils.startActivity(this, GroupNewsActivity.class, true);
                break;
            case "document":
                IntentUtils.startActivity(this, DocumentActivity.class, true);
                break;
            case "knowledge":
                IntentUtils.startActivity(this, KnowledgeActivity.class, true);
                break;
            default:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
