package com.inspur.emmcloud;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.SplashPageBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.AppExceptionService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.login.ModifyUserFirstPsdActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LanguageUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ResolutionUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UpgradeUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.reactnative.ReactNativeFlow;

import org.xutils.common.Callback;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;

/**
 * 应用启动Activity
 *
 * @author Administrator
 */
public class MainActivity extends Activity { // 此处不能继承BaseActivity 推送会有问题

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private Handler handler;
    private LanguageUtils languageUtils;
    private ReactNativeAPIService reactApiService;
    private AppAPIService appAPIService;
    private GifImageView splashImageTop;
    private String oldSplashVersionName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this);
        setContentView(R.layout.activity_main);
        init();
        //updateSplashPage();
        downloadSplashPage("https://ss0.bdstatic.com/94oJfD_bAAcT8t7mm9GUKT-xh_/timg?" +
                        "image&quality=100&size=b4000_4000&sec=1495175375&di=ffcdfea98e5e825a242e119d2cc444fa&src=" +
                "http://i.dimg.cc/5e/a5/0c/a2/47/9c/ac/0e/a4/56/cb/d7/03/09/e5/bc.jpg",
                "bc.jpg");
    }

    /**
     * 初始化
     */
    private void init() {
        splashImageTop = (GifImageView) findViewById(R.id.splash_img_top);
                /* 解决了在sd卡中第一次安装应用，进入到主页并切换到后台再打开会重新启动应用的bug */
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        //进行app异常上传
        startUploadExceptionService();
        ((MyApplication) getApplicationContext()).addActivity(this);
        // 检测分辨率、网络环境
        if (!ResolutionUtils.isFitResolution(MainActivity.this)) {
            showResolutionDialog();
        } else {
            initEnvironment();
        }
    }

    /**
     * 检查闪屏页更新
     */
    private void updateSplashPage() {
        reactApiService = new ReactNativeAPIService(MainActivity.this);
        reactApiService.setAPIInterface(new WebService());
        appAPIService = new AppAPIService(MainActivity.this);
        appAPIService.setAPIInterface(new WebService());
        showLastSplash();
        //这里并不是实时更新所以不加dialog
        if (NetUtils.isNetworkConnected(MainActivity.this)) {
            String splashInfo = PreferencesByUserUtils.getString(MainActivity.this, "splash_page_info");
            SplashPageBean splashPageBean = JSON.parseObject(splashInfo, SplashPageBean.class);
            oldSplashVersionName = splashPageBean.getName();
            String clientId = PreferencesUtils.getString(MainActivity.this, UriUtils.tanent + ((MyApplication) getApplication()).getUid() + "react_native_clientid", "");
            if (!StringUtils.isBlank(clientId)) {
                appAPIService.getSplashPageInfo(clientId, splashPageBean.getVersionCode());
            } else {
                //没有clientId首先将获取ClientId然后再检查更新
                getSplashClientId();
            }

        }
    }

    /**
     * 获取clientid,这里没有转圈的dialog
     */
    private void getSplashClientId() {
        if (NetUtils.isNetworkConnected(MainActivity.this)) {
            reactApiService.getClientId(AppUtils.getMyUUID(MainActivity.this), AppUtils.GetChangShang());
        }
    }

    /**
     * 开启异常上传服务
     */
    private void startUploadExceptionService() {
        Intent intent = new Intent();
        intent.setClass(this, AppExceptionService.class);
        startService(intent);
    }


    /**
     * 显示分辨率不符合条件的提示框
     **/
    private void showResolutionDialog() {
        // TODO Auto-generated method stub
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                finish();
            }
        };
        EasyDialog.showDialog(this, getString(R.string.prompt),
                getString(R.string.resolution_valiad), getString(R.string.ok),
                listener, false);
    }

    /**
     * 初始化应用环境
     */
    private void initEnvironment() {
        // TODO Auto-generated method stub
        Boolean isFirst = PreferencesUtils.getBoolean(MainActivity.this,
                "isFirst", true);
        // 当第一次进入应用，系统没有自动创建快捷方式时进行创建
        if (isFirst && !AppUtils.isHasShortCut(MainActivity.this)) {
            ((MyApplication) getApplicationContext())
                    .addShortCut(MainActivity.this);
        }
        handMessage();
        UpgradeUtils upgradeUtils = new UpgradeUtils(MainActivity.this, handler);
        upgradeUtils.checkUpdate(false);
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        // 是否已建立简易密码
                        if (!PreferencesUtils.getBoolean(MainActivity.this,
                                "hasPassword")) {
                            IntentUtils.startActivity(MainActivity.this,
                                    ModifyUserFirstPsdActivity.class, true);
                        } else {
                            IntentUtils.startActivity(MainActivity.this,
                                    IndexActivity.class, true);
                        }
                        break;
                    case LOGIN_FAIL:
                        IntentUtils.startActivity(MainActivity.this,
                                LoginActivity.class, true);
                        break;
                    case UPGRADE_FAIL:
                    case NO_NEED_UPGRADE:
                    case DONOT_UPGRADE:
                        getServerLanguage();
                        break;
                    case GET_LANGUAGE_SUCCESS:
                        enterApp();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    /**
     * 获取服务端支持的语言
     */
    private void getServerLanguage() {
        // TODO Auto-generated method stub
        String accessToken = PreferencesUtils.getString(MainActivity.this,
                "accessToken", "");
        String myInfo = PreferencesUtils.getString(getApplicationContext(),
                "myInfo", "");
        String languageJson = PreferencesUtils.getString(getApplicationContext(),
                UriUtils.tanent + "appLanguageObj");
        if (!StringUtils.isBlank(accessToken) && !StringUtils.isBlank(myInfo) && StringUtils.isBlank(languageJson)) {
            languageUtils = new LanguageUtils(MainActivity.this, handler);
            languageUtils.getServerSupportLanguage();
        } else {
            enterApp();
        }
    }


    /**
     * 进入App
     */
    private void enterApp() {
        // TODO Auto-generated method stub
        Boolean isFirst = PreferencesUtils.getBoolean(
                MainActivity.this, "isFirst", true);
        if (checkIfUpgraded() || isFirst) {
            IntentUtils.startActivity(MainActivity.this,
                    GuideActivity.class, true);
        } else {
            loginApp();
        }
    }

    /**
     * 检测是否应用版本是否进行了升级
     *
     * @return
     */
    private boolean checkIfUpgraded() {
        boolean ifUpgraded = false;
        String savedVersion = PreferencesUtils.getString(MainActivity.this,
                "previousVersion", "");
        String currentVersion = AppUtils.getVersion(MainActivity.this);
        if (TextUtils.isEmpty(savedVersion)) {
            return false;
        } else {
            ifUpgraded = AppUtils
                    .isAppHasUpgraded(savedVersion, currentVersion);
        }
        return ifUpgraded;
    }

    /**
     * 登录app
     */
    private void loginApp() {
        // TODO Auto-generated method stub
        String accessToken = PreferencesUtils.getString(MainActivity.this,
                "accessToken", "");
        String myInfo = PreferencesUtils.getString(getApplicationContext(),
                "myInfo", "");
        if ((!StringUtils.isBlank(accessToken))
                && (!StringUtils.isBlank(myInfo))) {
            IntentUtils.startActivity(MainActivity.this, IndexActivity.class,
                    true);
        } else {
            IntentUtils.startActivity(MainActivity.this, LoginActivity.class,
                    true);
        }
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    //这里发生失败都不提示，下次继续检查更新
    class WebService extends APIInterfaceInstance {

        @Override
        public void returnSplashPageInfoSuccess(SplashPageBean splashPageBean) {
            updateSplashPageWithOrder(splashPageBean);
            super.returnSplashPageInfoSuccess(splashPageBean);
        }

        @Override
        public void returnSplashPageInfoFail(String error, int errorCode) {
            super.returnSplashPageInfoFail(error, errorCode);
            LogUtils.YfcDebug("获取闪屏页失败：" + error + "错误代码：" + errorCode);
        }

        @Override
        public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {
            super.returnGetClientIdResultSuccess(getClientIdRsult);
            PreferencesUtils.putString(MainActivity.this, UriUtils.tanent +
                            ((MyApplication) getApplication()).getUid() + "react_native_clientid",
                    getClientIdRsult.getClientId());
            updateSplashPage();
        }

        @Override
        public void returnGetClientIdResultFail(String error) {
            super.returnGetClientIdResultFail(error);
        }
    }

    /**
     * 根据命令更新闪屏页
     *
     * @param splashPageBean
     */
    private void updateSplashPageWithOrder(SplashPageBean splashPageBean) {
        String command = splashPageBean.getCommand();
        if (command.equals("FORWARD")) {
            downloadSplashPage(splashPageBean.getUrl(), splashPageBean.getCommand());
        } else if (command.equals("ROLLBACK")) {
            String userId = ((MyApplication)getApplication()).getUid();
            ReactNativeFlow.moveFolder(MyAppConfig.getSplashPageImageLastVersionPath(MainActivity.this,userId),
                    MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                            userId, "splash"));
        } else if (command.equals("STANDBY")) {
            showLastSplash();
        } else {
            showLastSplash();
            LogUtils.YfcDebug("当做STANDBY");
        }
    }

    /**
     * 展示最新splash   需要添加是否已过期的逻辑
     */
    private void showLastSplash() {
        String splashInfo = PreferencesByUserUtils.getString(MainActivity.this, "splash_page_info");
        if(!StringUtils.isBlank(splashInfo)){
            SplashPageBean splashPageBeanLocal = JSON.parseObject(splashInfo, SplashPageBean.class);
            String name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                    ((MyApplication) getApplication()).getUid(), "splash/" + splashPageBeanLocal.getName());
            if(!StringUtils.isBlank(name)){
                splashImageTop.setImageBitmap(BitmapFactory.decodeFile(name));
            }else{
                splashImageTop.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 下载闪屏页
     *
     * @param url
     */
    private void downloadSplashPage(String url, String fileName) {
        DownLoaderUtils downloaderUtils = new DownLoaderUtils();
        LogUtils.YfcDebug("下载到的路径：" + MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                ((MyApplication) getApplication()).getUid(), "splash/" + fileName));
        downloaderUtils.startDownLoad(url, MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                ((MyApplication) getApplication()).getUid(), "splash/" + fileName), new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long l, long l1, boolean b) {

            }

            @Override
            public void onSuccess(File file) {
                String userId = ((MyApplication)getApplication()).getUid();
                ReactNativeFlow.moveFolder(MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                        userId, "splash"),MyAppConfig.getSplashPageImageLastVersionPath(MainActivity.this,userId)
                        );
                ReactNativeFlow.deleteOldVersionFile(MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                        userId, "splash/"+oldSplashVersionName));
//                try {
//                    LogUtils.YfcDebug("文件后缀名："+FileUtils.getSuffix(file));
//                    if (FileUtils.getSuffix(file).toLowerCase().equals("gif")) {
//                        GifDrawable gifFromPath = new GifDrawable(file);
//                        splashImageTop.setImageDrawable(gifFromPath);
//                    }else{
//                        InputStream input = new FileInputStream(file);
//                        Bitmap bitmap = BitmapFactory.decodeStream(input);
//                        splashImageTop.setImageBitmap(bitmap);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                LogUtils.YfcDebug("下载成功");
            }

            @Override
            public void onError(Throwable throwable, boolean b) {

            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

}
