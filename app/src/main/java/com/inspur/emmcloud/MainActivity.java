package com.inspur.emmcloud;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.inspur.emmcloud.bean.system.SplashDefaultBean;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.AppExceptionService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResolutionUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.systool.permission.PermissionMangerUtils;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.util.common.systool.permission.Permissions;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.LoginUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.SplashPageUtils;
import com.inspur.emmcloud.util.privates.UpgradeUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;


/**
 * 应用启动Activity
 *
 * @author Administrator
 */
public class MainActivity extends BaseActivity { // 此处不能继承BaseActivity 推送会有问题

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private static final long SPLASH_PAGE_TIME = 2500;
    private Handler handler;
    private long activitySplashShowTime = 0;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //解决了在sd卡中第一次安装应用，进入到主页并切换到后台再打开会重新启动应用的bug
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        //当Android版本在4.4以下时不全屏显示，否则在进入IndexActivity时状态栏过度不美观
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        }
        setContentView(R.layout.activity_main);
        getStoragePermission();
    }

    private void getStoragePermission() {
        String[] necessaryPermissionArray = StringUtils.concatAll(Permissions.STORAGE,Permissions.CALL_PHONE_PERMISSION);
        new PermissionMangerUtils(this, necessaryPermissionArray, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                init();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(MainActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(MainActivity.this,permissions));
                MyApplication.getInstance().exit();
            }
        }).start();
    }


    /**
     * 初始化
     */
    private void init() {
        String appFirstLoadAlis = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
        if (appFirstLoadAlis == null) {
            appFirstLoadAlis = AppUtils.getManifestAppVersionFlag(this);
            PreferencesUtils.putString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS, appFirstLoadAlis);
        }
        if (!appFirstLoadAlis.equals("Standard")) {
            PackageManager pm = getApplicationContext().getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName(
                            MainActivity.this, getPackageName() + ".Standard"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(
                            MainActivity.this, getPackageName() + "." + appFirstLoadAlis),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
        activitySplashShowTime = System.currentTimeMillis();
        //进行app异常上传
        startUploadExceptionService();
        // 检测分辨率、网络环境
        if (!ResolutionUtils.isFitResolution(MainActivity.this)) {
            showResolutionValiadDlg();
        } else {
            initEnvironment();
        }
        showLastSplash();
    }

    /**
     * 启动异常上传服务
     */
    private void startUploadExceptionService() {
        Intent intent = new Intent();
        intent.setClass(this, AppExceptionService.class);
        startService(intent);
    }

    /**
     * 显示分辨率不符合条件的提示框
     **/
    private void showResolutionValiadDlg() {
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
        UpgradeUtils upgradeUtils = new UpgradeUtils(MainActivity.this,
                handler, false);
        upgradeUtils.checkUpdate(false);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.splash_skip_layout:
            case R.id.splash_skip_btn:
                if (timer != null) {
                    timer.cancel();
                    startApp();
                }
                break;
        }
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case UPGRADE_FAIL:
                    case NO_NEED_UPGRADE:
                    case DONOT_UPGRADE:
                        getLoginInfo();
                        break;
                    case LOGIN_SUCCESS:
                    case LOGIN_FAIL:
                    case GET_LANGUAGE_SUCCESS:
                        setSplashShow();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    /**
     * 显示跳过按钮
     */
    public void showSkipButton() {
        (findViewById(R.id.splash_skip_btn)).setVisibility(View.VISIBLE);
        (findViewById(R.id.splash_skip_layout)).setVisibility(View.VISIBLE);
    }

    /**
     * 获取登录需要的一些信息
     */
    private void getLoginInfo() {
        // TODO Auto-generated method stub
        String accessToken = PreferencesUtils.getString(MainActivity.this,
                "accessToken", "");
        String myInfo = PreferencesUtils.getString(getApplicationContext(),
                "myInfo", "");
        String languageJson = PreferencesUtils.getString(getApplicationContext(),
                MyApplication.getInstance().getTanent() + "appLanguageObj");
        boolean isMDMStatusPass = PreferencesUtils.getBoolean(getApplicationContext(), "isMDMStatusPass", true);
        if (!StringUtils.isBlank(accessToken) && (StringUtils.isBlank(myInfo))) {
            new LoginUtils(MainActivity.this, handler).getMyInfo();
        } else if (!StringUtils.isBlank(accessToken) && !StringUtils.isBlank(myInfo) && StringUtils.isBlank(languageJson)) {
            new LoginUtils(MainActivity.this, handler).getServerSupportLanguage();
        } else if (!StringUtils.isBlank(accessToken) && !StringUtils.isBlank(myInfo) && !StringUtils.isBlank(languageJson) && !isMDMStatusPass) {
            MyApplication.getInstance().setAppLanguageAndFontScale();
            new LoginUtils(MainActivity.this, handler).startMDM();
        } else {
            setSplashShow();
        }
    }


    /**
     * 展示闪屏页
     */
    private void setSplashShow() {
        // TODO Auto-generated method stub
        long betweenTime = System.currentTimeMillis() - activitySplashShowTime;
        long leftTime = SPLASH_PAGE_TIME - betweenTime;
        TimerTask task = new TimerTask() {
            public void run() {
                if (timer != null) {
                    timer.cancel();
                }
                //线程转为主线程
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        startApp();
                    }
                });
            }
        };
        if (new SplashPageUtils(MainActivity.this).checkIfShowSplashPage() && (leftTime > 0)) {
            showSkipButton();
            timer = new Timer();
            timer.schedule(task, leftTime);
        } else {
            startApp();
        }
    }

    /**
     * 开启应用
     */
    private void startApp() {
        Boolean isFirst = PreferencesUtils.getBoolean(
                MainActivity.this, "isFirst", true);
        if (AppUtils.isAppHasUpgraded(getApplicationContext()) || isFirst) {
            IntentUtils.startActivity(MainActivity.this,
                    GuideActivity.class, true);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String accessToken = PreferencesUtils.getString(MainActivity.this,
                            "accessToken", "");
                    IntentUtils.startActivity(MainActivity.this, (!StringUtils.isBlank(accessToken)) ?
                            IndexActivity.class : LoginActivity.class, true);
                }
            }, 50);

        }
    }


    /**
     * 展示最新splash   需要添加是否已过期的逻辑
     */
    private void showLastSplash() {
        String splashInfo = PreferencesByUserAndTanentUtils.getString(MainActivity.this, "splash_page_info");
        if (!StringUtils.isBlank(splashInfo)) {
            SplashPageBean splashPageBeanLoacal = new SplashPageBean(splashInfo);
            SplashDefaultBean defaultBean = splashPageBeanLoacal.getPayload()
                    .getResource().getDefaultX();
            String splashPagePath = getSplashPagePath(defaultBean);
            long nowTime = System.currentTimeMillis();
            boolean shouldShow = ((nowTime > splashPageBeanLoacal.getPayload().getEffectiveDate())
                    && (nowTime < splashPageBeanLoacal.getPayload().getExpireDate()));
            if (shouldShow && !StringUtils.isBlank(splashPagePath)) {
                ImageLoader.getInstance().displayImage("file://" + splashPagePath, (GifImageView) findViewById(R.id.splash_img_top));
            } else {
                findViewById(R.id.splash_img_top).setVisibility(View.GONE);
            }
        }
    }

    /**
     * 闪屏文件路径
     *
     * @param defaultBean
     * @return
     */
    private String getSplashPagePath(SplashDefaultBean defaultBean) {
        String screenType = AppUtils.getScreenType(MainActivity.this);
        String fileName = "";
        switch (screenType) {
            case "2k":
                fileName = defaultBean.getXxxhdpi();
                break;
            case "xxhdpi":
                fileName = defaultBean.getXxhdpi();
                break;
            case "xhdpi":
                fileName = defaultBean.getXhdpi();
                break;
            default:
                fileName = defaultBean.getHdpi();
                break;
        }
        String filePath = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
                MyApplication.getInstance().getUid(), "splash/" + fileName);
        return filePath;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
        if (timer != null) {
            timer = null;
        }
    }
}
