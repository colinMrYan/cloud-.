package com.inspur.emmcloud.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.appcenter.groupnews.GroupNewsActivity;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
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
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppId2AppAndOpenAppUtils;
import com.inspur.emmcloud.util.privates.WebAppUtils;
import com.inspur.imp.api.ImpActivity;

import org.json.JSONObject;

import java.io.InputStream;

/**
 * scheme统一处理类
 */

public class SchemeHandleActivity extends Activity {
    private BroadcastReceiver unlockReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this);
        if (MyApplication.getInstance().getOPenNotification()) {
            MyApplication.getInstance().setOpenNotification(false);
            if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(SchemeHandleActivity.this)) {
                registerReiceiver();
                Intent intent = new Intent(SchemeHandleActivity.this, FaceVerifyActivity.class);
                intent.putExtra("isFaceVerifyExperience", false);
                startActivity(intent);
                return;
            } else if (getIsNeedGestureCode()) {
                registerReiceiver();
                Intent intent = new Intent(this, GestureLoginActivity.class);
                intent.putExtra("gesture_code_change", "login");
                startActivity(intent);
                return;
            }
        }

        openScheme();
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
        registerReceiver(unlockReceiver, myIntentFilter);
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
                    Uri uri = getIntent().getData();
                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    if (uri == null || scheme == null || host == null) {
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
                            IntentUtils.startActivity(SchemeHandleActivity.this,
                                    ChannelActivity.class, bundle, true);
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
                        default:
                            finish();
                            break;
                    }
                }
            }, 1);

        } else {
            IntentUtils.startActivity(this, LoginActivity.class, true);
        }
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

    /**
     * 获取分享文件的Uri
     * @return
     */
    public Uri getShareFileUri() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        // 判断Intent是否是“分享”功能(Share Via)
        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                try {
                    // 获取资源路径Uri
                    Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                    LogUtils.YfcDebug("uri路径：" + uri.toString());
                    //解析Uri资源
                    ContentResolver cr = getContentResolver();
                    InputStream is = cr.openInputStream(uri);
                    // Get binary bytes for encode
//                    byte[] data = getBytesFromFile(is);
                    return uri;
                } catch (Exception e) {
//                    Log.e(this.getClass().getName(), e.toString());
                }
            } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        if (unlockReceiver != null) {
            unregisterReceiver(unlockReceiver);
            unlockReceiver = null;
        }
        super.onDestroy();
    }
}
