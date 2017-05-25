package com.inspur.emmcloud;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.inspur.emmcloud.bean.SplashPageBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.AppExceptionService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.login.ModifyUserFirstPsdActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LanguageUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ResolutionUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UpgradeUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

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
    private long activityShowTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityShowTime = System.currentTimeMillis();
    }

    /**
     * 初始化
     */
    private void init() {
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
        showLastSplash();
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

//                        enterApp();
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
        long betweenTime = System.currentTimeMillis() - activityShowTime;
        long leftTime = 2500 - betweenTime;
        LogUtils.YfcDebug("剩余时间：" + leftTime);
        if(checkIfShowSplashPage()){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startApp();
                }
            }, leftTime);
        }else {
            startApp();
        }

    }

    /**
     * 开启应用
     */
    private void startApp() {
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
     * 检查是否有可以展示的图片
     * @return
     */
    private boolean checkIfShowSplashPage() {
        return false;
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

    /**
     * 展示最新splash   需要添加是否已过期的逻辑
     */
    private void showLastSplash() {
        String splashInfo = PreferencesByUserUtils.getString(MainActivity.this, "splash_page_info");
        if (!StringUtils.isBlank(splashInfo)) {
            SplashPageBean splashPageBeanLoacal = new SplashPageBean(splashInfo);
            String screenType = AppUtils.getScreenType(MainActivity.this);
            SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean = splashPageBeanLoacal.getPayload()
                    .getResource().getDefaultX();
            String name = "";
            if (screenType.equals("2k")) {
                name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                        ((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getXxxhdpi());
            } else if (screenType.equals("xxxhdpi")) {
                name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                        ((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getXxhdpi());
            } else if (screenType.equals("xxhdpi")) {
                name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                        ((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getXhdpi());
            } else {
                name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                        ((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getHdpi());
            }
            long nowTime = System.currentTimeMillis();
            boolean shouldShow = ((nowTime > splashPageBeanLoacal.getPayload().getEffectiveDate())
                    && (nowTime < splashPageBeanLoacal.getPayload().getExpireDate()));
            if (shouldShow && !StringUtils.isBlank(name)) {
                ImageLoader.getInstance().displayImage("file://" + name, (GifImageView) findViewById(R.id.splash_img_top));
            } else {
                ((GifImageView) findViewById(R.id.splash_img_top)).setVisibility(View.GONE);
            }
        }
    }
}
