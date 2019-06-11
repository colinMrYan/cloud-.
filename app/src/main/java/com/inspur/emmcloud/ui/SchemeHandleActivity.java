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
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.schedule.calendar.CalendarEvent;
import com.inspur.emmcloud.bean.system.ChangeTabBean;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.util.privates.AppId2AppAndOpenAppUtils;
import com.inspur.emmcloud.util.privates.ProfileUtils;
import com.inspur.emmcloud.util.privates.WebAppUtils;
import com.inspur.imp.api.ImpActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

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
//                            case "ecc-component":
//                                openComponentScheme(uri, host);
//                                break;
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
                            default:
                                finish();
                                break;
                        }
                    }
                }
            }, 1);

        } else {
            ARouter.getInstance().build("/login/main").navigation();
            finish();
        }
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



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
