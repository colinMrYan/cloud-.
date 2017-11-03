package com.inspur.emmcloud.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.app.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.app.groupnews.GroupNewsActivity;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.find.AnalysisActivity;
import com.inspur.emmcloud.ui.find.DocumentActivity;
import com.inspur.emmcloud.ui.find.KnowledgeActivity;
import com.inspur.emmcloud.ui.find.trip.TripInfoActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebAppUtils;
import com.inspur.imp.api.ImpActivity;

/**
 * Created by chenmch on 2017/7/10.
 */

public class SchemeHandleActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this);
        //  this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        if (((MyApplication) getApplicationContext()).isHaveLogin()) {
            openIndexActivity(this);
            //此处加延时操作，为了让打开通知时IndexActivity走onCreate()方法
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Uri uri = getIntent().getData();
                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    if(uri == null || scheme == null || host == null){
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
                            IntentUtils.startActivity(SchemeHandleActivity.this, ReactNativeAppActivity.class, true);
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

                        default:
                            break;
                    }
                }
            }, 1);

        } else {
            IntentUtils.startActivity(this, LoginActivity.class, true);
        }

    }

    /**
     * 打开web应用
     *
     * @param host
     * @param openMode
     */
    private void openWebApp(String host, final String openMode) {
        String url = "https://emm.inspur.com/api/v1/gs_sso/msg_uri?id=" + host;
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
}
