package com.inspur.emmcloud.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Window;
import android.view.WindowManager;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.system.ChangeTabBean;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.appcenter.groupnews.GroupNewsActivity;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.ui.appcenter.webex.WebexMyMeetingActivity;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.find.AnalysisActivity;
import com.inspur.emmcloud.ui.find.DocumentActivity;
import com.inspur.emmcloud.ui.find.KnowledgeActivity;
import com.inspur.emmcloud.ui.find.trip.TripInfoActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppId2AppAndOpenAppUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.ProfileUtils;
import com.inspur.emmcloud.util.privates.WebAppUtils;
import com.inspur.imp.api.ImpActivity;

import org.greenrobot.eventbus.EventBus;
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

public class SchemeHandleActivity extends Activity {
    private BroadcastReceiver unlockReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        new ProfileUtils(SchemeHandleActivity.this, new CommonCallBack() {
            @Override
            public void execute() {
                if (MyApplication.getInstance().getOPenNotification()) {
                    MyApplication.getInstance().setOpenNotification(false);
                    if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(SchemeHandleActivity.this)) {
                        registerReiceiver();
                        MyApplication.getInstance().setIsActive(true);
                        faceVerify();
                        return;
                    } else if (getIsNeedGestureCode()) {
                        registerReiceiver();
                        MyApplication.getInstance().setIsActive(true);
                        gestureVerify();
                        return;
                    }
                }
                openScheme();
            }
        }).initProfile();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        new ProfileUtils(SchemeHandleActivity.this, new CommonCallBack() {
            @Override
            public void execute() {
                if (MyApplication.getInstance().getOPenNotification()) {
                    MyApplication.getInstance().setOpenNotification(false);
                    if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(SchemeHandleActivity.this)) {
                        MyApplication.getInstance().setIsActive(true);
                        faceVerify();
                        return;
                    } else if (getIsNeedGestureCode()) {
                        MyApplication.getInstance().setIsActive(true);
                        gestureVerify();
                        return;
                    }
                }
                openScheme();
            }
        }).initProfile();
    }

    private void faceVerify() {
        Intent intent = new Intent(SchemeHandleActivity.this, FaceVerifyActivity.class);
        intent.putExtra("isFaceVerifyExperience", false);
        startActivity(intent);
    }

    private void gestureVerify() {
        Intent intent = new Intent(this, GestureLoginActivity.class);
        intent.putExtra("gesture_code_change", "login");
        startActivity(intent);
    }


    /**
     * 注册安全解锁监听广播
     */
    private void registerReiceiver() {
        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                openScheme();
            }
        };

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constant.ACTION_SAFE_UNLOCK);
        LocalBroadcastManager.getInstance(this).registerReceiver(unlockReceiver, myIntentFilter);
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
                                bundle.putBoolean("get_new_msg", true);
                                if (MyApplication.getInstance().isV0VersionChat()) {
                                    IntentUtils.startActivity(SchemeHandleActivity.this,
                                            ChannelV0Activity.class, bundle, true);
                                } else {
                                    IntentUtils.startActivity(SchemeHandleActivity.this,
                                            ChannelActivity.class, bundle, true);
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
                                    Intent intent = new Intent(SchemeHandleActivity.this, CalEventAddActivity.class);
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
                                if (host.equals("news")){
                                    IntentUtils.startActivity(SchemeHandleActivity.this, GroupNewsActivity.class,true);
                                }else if(host.equals("volume")){
                                    IntentUtils.startActivity(SchemeHandleActivity.this, VolumeHomePageActivity.class,true);
                                }
                                break;
                            case "inspur-ecc-native":
                                openNativeSchemeByHost(host,getIntent());
                                break;
                            default:
                                finish();
                                break;
                        }
                    }
                }
            }, 1);

        } else {
            IntentUtils.startActivity(this, LoginActivity.class, true);
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
            if(isLinkShare()){
                handleLinkShare(getShareLinkContent());
                return;
            }else if(uri != null){
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
        }else{
            ToastUtils.show(SchemeHandleActivity.this,getString(R.string.share_not_support));
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
        if(intent != null){
            String text = intent.getExtras().getString(Intent.EXTRA_TEXT);
            String subject = intent.getExtras().getString(Intent.EXTRA_SUBJECT);
            if (text != null && subject != null) {
                urlStr = getShareUrl(text);
                titleStr = subject;
                digest = text.replace(getShareUrl(text),"");
            } else if (text != null && subject == null) {
                urlStr = getShareUrl(text);
                titleStr = text.replace(urlStr, "");
            } else if (text == null && subject == null) {
                return shareLinkMap;
            }
            shareLinkMap.put("title", StringUtils.isBlank(titleStr)?getString(R.string.share_default_title):titleStr);
            shareLinkMap.put("url", urlStr);
            shareLinkMap.put("digest",digest);
        }
        return shareLinkMap;
    }

    /**
     * 获取url
     *
     * @param text
     */
    private  String getShareUrl(String text) {
//        Pattern p = Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?", Pattern.CASE_INSENSITIVE);
        Pattern p = Pattern.compile(Constant.PATTERN_URL, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(text);
        matcher.find();
        return matcher.group();
    }

    /**
     * 是一个链接分享
     * @return
     */
    private boolean isLinkShare() {
        Intent intent = getIntent();
        return intent.getExtras() != null && !StringUtils.isBlank(intent.getExtras().getString(Intent.EXTRA_TEXT));
    }

    /**
     * 处理分享url
     */
    private void handleLinkShare(HashMap<String,String> shareLinkContentMap) {
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
     * 是否应该显示显示手势解锁
     *
     * @return
     */
    private boolean getIsNeedGestureCode() {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(this);
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
                if (isUriHasTitle) {
                    bundle.putString("appName", "");
                }
                IntentUtils.startActivity(SchemeHandleActivity.this, ImpActivity.class, bundle, true);
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

    private void openNativeSchemeByHost(String host,Intent intent){
        switch (host){
            case "calendar":
                IntentUtils.startActivity(SchemeHandleActivity.this, CalActivity.class,true);
                break;
            case "to-do":
                IntentUtils.startActivity(SchemeHandleActivity.this, MessionListActivity.class,true);
                break;
            case "meeting":
                IntentUtils.startActivity(SchemeHandleActivity.this, MeetingListActivity.class,true);
                break;
            case "webex":
                String installUri = intent.getExtras().getString("installUri","");
                Bundle bundle = new Bundle();
                bundle.putString("installUri",installUri);
                IntentUtils.startActivity(SchemeHandleActivity.this, WebexMyMeetingActivity.class,bundle,true);
                break;
        }
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
        if (unlockReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(unlockReceiver);
            unlockReceiver = null;
        }
        super.onDestroy();
    }
}
