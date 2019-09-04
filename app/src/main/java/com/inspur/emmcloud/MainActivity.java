package com.inspur.emmcloud;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.EasyDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.bean.system.SplashDefaultBean;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.privates.NotificationUpgradeUtils;
import com.inspur.emmcloud.util.privates.SplashPageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.droidsonroids.gif.GifImageView;


/**
 * 应用启动Activity
 *
 * @author Administrator
 */
public class MainActivity extends BaseActivity {

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private static final long SPLASH_PAGE_TIME = 2500;
    @BindView(R.id.ibt_skip)
    ImageButton skipImageBtn;
    @BindView(R.id.iv_splash_logo)
    ImageView splashLogoImg;
    @BindView(R.id.iv_splash_ad)
    GifImageView splashAdImg;
    private Handler handler;
    private long activitySplashShowTime = 0;
    private Timer timer;

    @Override
    public void onCreate() {
        initAppAlias();
        ButterKnife.bind(this);
        // 解决了在sd卡中第一次安装应用，进入到主页并切换到后台再打开会重新启动应用的bug
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    protected int getStatusType() {
        return STATUS_WHITE;
    }

    private void initAppAlias() {
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
    }

    /**
     * 初始化
     */
    private void init() {
        String appFirstLoadAlis = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
        int splashLogoResId = Res.getDrawableID("ic_splash_logo_" + appFirstLoadAlis);
        ImageDisplayUtils.getInstance().displayImage(splashLogoImg, "drawable://" + splashLogoResId, R.drawable.ic_splash_logo);
        activitySplashShowTime = System.currentTimeMillis();

        // 检测分辨率、网络环境
        if (!ResolutionUtils.isFitResolution(MainActivity.this)) {
            showResolutionValiadDlg();
        } else {
            initEnvironment();
        }
        showLastSplash();
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
        handMessage();
        NotificationUpgradeUtils upgradeUtils = new NotificationUpgradeUtils(MainActivity.this,
                handler, false);
        upgradeUtils.checkUpdate(false);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_skip:
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
                        autoLogin();
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
     * 获取登录需要的一些信息
     */
    private void autoLogin() {
        // TODO Auto-generated method stub
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.autoLogin(MainActivity.this, handler);
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
                        skipImageBtn.setVisibility(View.INVISIBLE);
                        startApp();
                    }
                });
            }
        };
        if (new SplashPageUtils(MainActivity.this).checkIfShowSplashPage() && (leftTime > 0)) {
            skipImageBtn.setVisibility(View.VISIBLE);
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
        //当弹出锁屏界面时不进行跳转，需要解锁完成之后在进行页面跳转，防止出现跳转界面遮盖处锁屏界面
        if (!MyApplication.getInstance().isSafeLock()) {
            Boolean isFirst = PreferencesUtils.getBoolean(
                    MainActivity.this, "isFirst", true);
            if (AppUtils.isAppHasUpgraded(getApplicationContext()) || isFirst) {
                IntentUtils.startActivity(MainActivity.this,
                        GuideActivity.class, true);
            } else {
                String accessToken = PreferencesUtils.getString(MainActivity.this,
                        "accessToken", "");
                MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if (StringUtils.isBlank(accessToken)) {
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_LOGIN_MAIN).navigation();
                } else {
                    IntentUtils.startActivity(MainActivity.this, IndexActivity.class, true);
                }

            }
        } else {
            EventBus.getDefault().register(this);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_SAFE_UNLOCK)) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    startApp();
                }
            });
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
                ImageLoader.getInstance().displayImage("file://" + splashPagePath, splashAdImg);
            } else {
                splashAdImg.setVisibility(View.GONE);
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
        EventBus.getDefault().unregister(this);
        if (handler != null) {
            handler = null;
        }
        if (timer != null) {
            timer = null;
        }
    }
}
