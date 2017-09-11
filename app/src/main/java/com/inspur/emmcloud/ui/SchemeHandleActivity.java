package com.inspur.emmcloud.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.MyApplication;
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
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.api.ImpActivity;

/**
 * Created by chenmch on 2017/7/10.
 */

public class SchemeHandleActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(((MyApplication)getApplicationContext()).isHaveLogin()){
            openIndexActivity(this);
            //此处加延时操作，为了让打开通知时IndexActivity走onCreate()方法
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Uri uri = getIntent().getData();
                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    Bundle bundle = new Bundle();
                    switch (scheme) {
                        case "ecc-contact":
                        case "ecm-contact":
                            bundle.putString("uid",host);
                            if(host.startsWith("BOT")){
                                IntentUtils.startActivity(SchemeHandleActivity.this, RobotInfoActivity.class,bundle);
                            }else{
                                IntentUtils.startActivity(SchemeHandleActivity.this, UserInfoActivity.class,bundle);
                            }
                            break;
                        case "ecc-component":
                            openComponentScheme(uri,host);
                            break;
                        case "ecc-app-react-native":
                            IntentUtils.startActivity(SchemeHandleActivity.this,ReactNativeAppActivity.class);
                            break;

                        case "gs-msg":
                            String url = "https://emm.inspur.com/ssohandler/gs_msg/" + host;
                            String openMode = uri.getQueryParameter("openMode");
                            boolean isUriHasTitle = (openMode != null && openMode.equals("1"));
                            bundle.putString("uri", url);
                            if (isUriHasTitle) {
                                bundle.putString("appName", "");
                            }
                            IntentUtils.startActivity(SchemeHandleActivity.this,ImpActivity.class,bundle);
                            break;
                        case "ecc-channel":
                            bundle.putString("cid", host);
                            bundle.putBoolean("get_new_msg",true);
                            IntentUtils.startActivity(SchemeHandleActivity.this,
                                    ChannelActivity.class, bundle);
                            break;

                        default:
                            break;
                    }
                }
            },1);

        }else {
           IntentUtils.startActivity(this, LoginActivity.class);
        }
        finish();
    }

    /**
     * 打开主tab页
     *
     * @param context
     */
    private void openIndexActivity(Context context) {
        Intent indexIntent = new Intent(context, IndexActivity.class);
        if (!((MyApplication) context.getApplicationContext()).isIndexActivityRunning()) {
            LogUtils.jasonDebug("start----index----");
            context.startActivity(indexIntent);
        } else if (!((MyApplication) context.getApplicationContext()).getIsActive()) {
            LogUtils.jasonDebug("start----index2222222----");
            indexIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(indexIntent);
        }
    }

    private void openComponentScheme(Uri uri,String host){
        Bundle bundle = new Bundle();
        switch (host) {
            case "stastistics":
                IntentUtils.startActivity(this, AnalysisActivity.class,bundle);
                break;
            case "trips":
                String path = uri.getPath();
                String tripId = path.split("/")[1];
                bundle.putString("tripId",tripId);
                IntentUtils.startActivity(this,TripInfoActivity.class,bundle);
                break;
            case "news.ecc":
                IntentUtils.startActivity(this,GroupNewsActivity.class);
                break;

            case "document":
                IntentUtils.startActivity(this,DocumentActivity.class);
                break;

            case "knowledge":
                IntentUtils.startActivity(this,KnowledgeActivity.class);
                break;
            default:
                break;
        }
    }
}
