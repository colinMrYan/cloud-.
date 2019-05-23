package com.inspur.emmcloud;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.bean.system.SplashDefaultBean;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.AppExceptionService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResolutionUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.systool.emmpermission.Permissions;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.LoginUtils;
import com.inspur.emmcloud.util.privates.NotificationUpgradeUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.SplashPageUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.imp.api.Res;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
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
    @BindView(R.id.ibt_skip)
    ImageButton skipImageBtn;
    @BindView(R.id.iv_splash_logo)
    ImageView splashLogoImg;
    @BindView(R.id.iv_splash_ad)
    GifImageView splashAdImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //解决了在sd卡中第一次安装应用，进入到主页并切换到后台再打开会重新启动应用的bug
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        initAppAlias();
        checkNecessaryPermission();
//        IntentUtils.startActivity(this, MeetingOfficeAddActivity.class,true);
    }

    private void initAppAlias(){
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

    private void checkNecessaryPermission() {
        final String[] necessaryPermissionArray = StringUtils.concatAll(Permissions.STORAGE, new String[]{Permissions.READ_PHONE_STATE});
        if (!PermissionRequestManagerUtils.getInstance().isHasPermission(this, necessaryPermissionArray)) {
            final MyDialog permissionDialog = new MyDialog(this, R.layout.dialog_permisson_tip);
            permissionDialog.setDimAmount(0.2f);
            permissionDialog.setCancelable(false);
            permissionDialog.setCanceledOnTouchOutside(false);
            permissionDialog.findViewById(R.id.ll_permission_storage).setVisibility(!PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.STORAGE) ? View.VISIBLE : View.GONE);
            permissionDialog.findViewById(R.id.ll_permission_phone).setVisibility(!PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.READ_PHONE_STATE) ? View.VISIBLE : View.GONE);
            if (!PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.STORAGE)
                    && !PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.READ_PHONE_STATE)) {
                LinearLayout layout = permissionDialog.findViewById(R.id.ll_permission_storage);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                params.setMargins(DensityUtil.dip2px(this, 60.0f), 0, 0, 0);
                layout.setLayoutParams(params);
            }
            ((TextView) permissionDialog.findViewById(R.id.tv_permission_dialog_title)).setText(getString(R.string.permission_open_cloud_plus, AppUtils.getAppName(MainActivity.this)));
            ((TextView) permissionDialog.findViewById(R.id.tv_permission_dialog_summary)).setText(getString(R.string.permission_necessary_permission, AppUtils.getAppName(MainActivity.this)));
            permissionDialog.findViewById(R.id.tv_next_step).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    permissionDialog.dismiss();
                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(MainActivity.this, necessaryPermissionArray, new PermissionRequestCallback() {
                        @Override
                        public void onPermissionRequestSuccess(List<String> permissions) {
                            init();
                        }

                        @Override
                        public void onPermissionRequestFail(List<String> permissions) {
                            ToastUtils.show(MainActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(MainActivity.this, permissions));
                            MyApplication.getInstance().exit();
                        }
                    });
                }
            });
            permissionDialog.show();
        } else {
            init();
        }
    }

    /**
     * 初始化
     */
    private void init() {
        String appFirstLoadAlis = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
        int splashLogoResId = Res.getDrawableID("ic_splash_logo_"+appFirstLoadAlis);
        ImageDisplayUtils.getInstance().displayImage(splashLogoImg,"drawable://"+splashLogoResId,R.drawable.ic_splash_logo);
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
                    MainActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        if (handler != null) {
            handler = null;
        }
        if (timer != null) {
            timer = null;
        }
    }
}
