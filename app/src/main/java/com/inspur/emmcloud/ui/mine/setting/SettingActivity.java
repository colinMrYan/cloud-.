package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.login.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.mine.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.bean.system.AppConfig;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;
import com.inspur.emmcloud.bean.system.navibar.NaviBarScheme;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.BackgroundService;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.chat.DisplayMediaVoiceMsg;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.NotificationSetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientConfigUpdateUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.PushManagerUtils;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyAppCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

@ContentView(R.layout.activity_setting)
public class SettingActivity extends BaseActivity {

    private static final int DATA_CLEAR_SUCCESS = 0;
    private Handler handler;
    @ViewInject(R.id.switch_view_setting_web_rotate)
    private SwitchView webRotateSwitch;
    @ViewInject(R.id.switch_view_setting_run_background)
    private SwitchView runBackgroundSwitch;
    @ViewInject(R.id.switch_view_setting_voice_2_word)
    private SwitchView voice2WordSwitch;
    @ViewInject(R.id.rl_setting_voice_2_word)
    private RelativeLayout voice2WordLayout;
    @ViewInject(R.id.rl_setting_experience_upgrade)
    private RelativeLayout experienceUpgradeLayout;
    @ViewInject(R.id.switch_view_setting_experience_upgrade)
    private SwitchView experienceUpgradeSwitch;
    @ViewInject(R.id.tv_setting_language_name)
    private TextView languageNameText;
    @ViewInject(R.id.iv_setting_language_flag)
    private ImageView languageFlagImg;
    private MineAPIService apiService;
    private LoadingDialog loadingDlg;
    @ViewInject(R.id.tv_setting_theme_name)
    private TextView themeNameText;
    @ViewInject(R.id.rl_setting_switch_tablayout)
    private RelativeLayout switchTabLayout;
    @ViewInject(R.id.tv_setting_tab_name)
    private TextView tabName;
    @ViewInject(R.id.switch_view_setting_notification)
    private Switch notificationSwitch;
    private SwitchView.OnStateChangedListener onStateChangedListener = new SwitchView.OnStateChangedListener() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void toggleToOn(View view) {
            switch (view.getId()) {
                case R.id.switch_view_setting_run_background:
                    setAppRunBackground(true);
                    break;
                case R.id.switch_view_setting_voice_2_word:
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH, DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN);
                    voice2WordSwitch.setOpened(true);
                    break;
                case R.id.switch_view_setting_web_rotate:
                    AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "true");
                    AppConfigCacheUtils.saveAppConfig(MyApplication.getInstance(), appConfig);
                    setWebAutoRotateState();
                    saveWebAutoRotateConfig(true);
                    break;
                case R.id.switch_view_setting_experience_upgrade:
                    updateUserExperienceUpgradeFlag();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void toggleToOff(View view) {
            switch (view.getId()) {
                case R.id.switch_view_setting_run_background:
                    setAppRunBackground(false);
                    break;
                case R.id.switch_view_setting_voice_2_word:
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH, DisplayMediaVoiceMsg.IS_VOICE_WORD_CLOUSE);
                    voice2WordSwitch.setOpened(false);
                    break;
                case R.id.switch_view_setting_web_rotate:
                    AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "false");
                    AppConfigCacheUtils.saveAppConfig(MyApplication.getInstance(), appConfig);
                    setWebAutoRotateState();
                    saveWebAutoRotateConfig(false);
                    break;
                case R.id.switch_view_setting_experience_upgrade:
                    updateUserExperienceUpgradeFlag();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initView();
        setLanguage();
        handMessage();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationSwitch.setChecked(getSwitchOpen());
        switchPush();
    }

    /**
     * 开关push并向服务器发出信号
     */
    private void switchPush() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(NotificationSetUtils.isNotificationEnabled(this) &&
                    PreferencesByUserAndTanentUtils.getBoolean(this,
                            Constant.PUSH_SWITCH_FLAG,false)){
                MyApplication.getInstance().startPush();
                PushManagerUtils.getInstance().registerPushId2Emm();
            }else{
                MyApplication.getInstance().stopPush();
                PushManagerUtils.getInstance().unregisterPushId2Emm();
            }
        }else{
            if(PreferencesByUserAndTanentUtils.getBoolean(this,
                    Constant.PUSH_SWITCH_FLAG,false)){
                MyApplication.getInstance().startPush();
                PushManagerUtils.getInstance().registerPushId2Emm();
            }else{
                MyApplication.getInstance().stopPush();
                PushManagerUtils.getInstance().unregisterPushId2Emm();
            }
        }
    }

    private boolean getSwitchOpen() {
        boolean isOpen = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(NotificationSetUtils.isNotificationEnabled(this)){
                isOpen = PreferencesByUserAndTanentUtils.getBoolean(SettingActivity.this,Constant.PUSH_SWITCH_FLAG,false);
            }else{
                isOpen = false;
            }
        }else{
            isOpen = PreferencesByUserAndTanentUtils.getBoolean(SettingActivity.this,Constant.PUSH_SWITCH_FLAG,false);
        }
        return isOpen;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MineAPIService(this);
        apiService.setAPIInterface(new WebService());
        setWebAutoRotateState();
        webRotateSwitch.setOnStateChangedListener(onStateChangedListener);
        boolean isAppSetRunBackground = PreferencesUtils.getBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, false);
        runBackgroundSwitch.setOpened(isAppSetRunBackground);
        runBackgroundSwitch.setOnStateChangedListener(onStateChangedListener);
        if (MyApplication.getInstance().isV1xVersionChat()) {
            voice2WordLayout.setVisibility(View.VISIBLE);
            voice2WordSwitch.setOpened(AppUtils.getIsVoiceWordOpen());
            voice2WordSwitch.setOnStateChangedListener(onStateChangedListener);
        }
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        showNotificationDlg();
                    }
                    notificationSwitch.setChecked(true);
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this,Constant.PUSH_SWITCH_FLAG,true);
                    switchPush();
                }else{
                    notificationSwitch.setChecked(false);
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this,Constant.PUSH_SWITCH_FLAG,false);
                    switchPush();
                }
            }
        });
        if (AppUtils.isAppVersionStandard()) {
            getUserExperienceUpgradeFlag();
            experienceUpgradeLayout.setVisibility(View.VISIBLE);
            boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
            experienceUpgradeSwitch.setOpened(isExperienceUpgradeFlag);
            experienceUpgradeSwitch.setOnStateChangedListener(onStateChangedListener);
        }
        themeNameText.setText(ThemeSwitchActivity.getThemeName());
        NaviBarModel naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(this,Constant.APP_TAB_LAYOUT_DATA,""));
        switchTabLayout.setVisibility(naviBarModel.getNaviBarPayload().getNaviBarSchemeList().size()>0?View.VISIBLE:View.GONE);
        tabName.setText(getTabLayoutName());
    }

    private String getTabLayoutName() {
        NaviBarModel naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(this,Constant.APP_TAB_LAYOUT_DATA,""));
        List<NaviBarScheme> naviBarSchemeList = naviBarModel.getNaviBarPayload().getNaviBarSchemeList();
        String currentTabLayoutName = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(),Constant.APP_TAB_LAYOUT_NAME,"");
        for (int i = 0; i < naviBarSchemeList.size(); i++) {
            if(naviBarSchemeList.get(i).getName().equals(currentTabLayoutName)){
                Configuration config = getResources().getConfiguration();
                String environmentLanguage = config.locale.getLanguage();
                String tabName = "";
                switch (environmentLanguage.toLowerCase()) {
                    case "zh-hant":
                        tabName = naviBarSchemeList.get(i).getNaviBarTitleResult().getZhHans();
                        break;
                    case "en":
                    case "en-us":
                        tabName = naviBarSchemeList.get(i).getNaviBarTitleResult().getEnUS();
                        break;
                    default:
                        tabName = naviBarSchemeList.get(i).getNaviBarTitleResult().getZhHans();
                        break;
                }
                return tabName;
            }
        }
        return "";
    }

    private void setWebAutoRotateState() {
        boolean isWebAutoRotate = Boolean.parseBoolean(AppConfigCacheUtils.getAppConfigValue(this, Constant.CONCIG_WEB_AUTO_ROTATE, "false"));
        webRotateSwitch.setOpened(isWebAutoRotate);
    }

    /**
     * 设置显示app语言
     */
    private void setLanguage() {
        // TODO Auto-generated method stub
        String languageName = PreferencesUtils.getString(MyApplication.getInstance(), MyApplication.getInstance().getTanent() + "language", "");
        String languageJson = PreferencesUtils
                .getString(this, MyApplication.getInstance().getTanent() + "appLanguageObj");
        if (languageJson != null && !languageName.equals("followSys")) {
            Language language = new Language(languageJson);
            languageNameText.setText(new Language(languageJson).getLabel());
            String iso = language.getIso();
            iso = iso.replace("-", "_");
            iso = iso.toLowerCase();
            Integer id = getResources().getIdentifier(iso, "drawable", getApplicationContext().getPackageName());
            if (id == null) {
                id = R.drawable.zh_cn;
            }
            //设置语言国旗标志
            languageFlagImg.setImageResource(id);
        } else {
            languageNameText.setText(getString(R.string.follow_system));
        }
    }

    /**
     * 设置app是否运行在后台
     *
     * @param isAppSetRunBackground
     */
    private void setAppRunBackground(boolean isAppSetRunBackground) {
        PreferencesUtils.putBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, isAppSetRunBackground);
        runBackgroundSwitch.setOpened(isAppSetRunBackground);
        Intent intent = new Intent();
        intent.setClass(SettingActivity.this, BackgroundService.class);
        if (isAppSetRunBackground) {
            startService(intent);
        } else {
            stopService(intent);
        }
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                ToastUtils.show(getApplicationContext(),
                        R.string.data_clear_success);
                // 通知消息页面重新创建群组头像
                Intent intent = new Intent("message_notify");
                intent.putExtra("command", "creat_group_icon");
                LocalBroadcastManager.getInstance(SettingActivity.this).sendBroadcast(intent);

            }

        };
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.bt_setting_signout:
                showSignoutDlg();
                break;
            case R.id.rl_setting_language:
                IntentUtils.startActivity(SettingActivity.this,
                        LanguageSwitchActivity.class);
                break;
            case R.id.clear_cache_layout:
                showClearCacheDlg();
                break;
            case R.id.rl_setting_account_safe:
                IntentUtils.startActivity(SettingActivity.this, SafeCenterActivity.class);
                break;
            case R.id.rl_setting_switch_theme:
                IntentUtils.startActivity(SettingActivity.this, ThemeSwitchActivity.class);
                break;
            case R.id.rl_setting_switch_tablayout:
                IntentUtils.startActivity(SettingActivity.this, TabLayoutSwitchActivity.class);
                break;
            default:
                break;
        }
    }

    /**
     * 弹出注销提示框
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showNotificationDlg() {
        if(!NotificationSetUtils.isNotificationEnabled(SettingActivity.this)){
            new MyQMUIDialog.MessageDialogBuilder(SettingActivity.this)
                    .setMessage("系统中的云+消息通知已关闭，前往打开？")
                    .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            notificationSwitch.setChecked(false);
                        }
                    })
                    .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            NotificationSetUtils.openNotificationSetting(SettingActivity.this);
                        }
                    })
                    .show();
        }
    }

    /**
     * 弹出注销提示框
     */
    private void showSignoutDlg() {
        new MyQMUIDialog.MessageDialogBuilder(SettingActivity.this)
                .setMessage(R.string.if_confirm_signout)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        PushManagerUtils.getInstance().unregisterPushId2Emm();
                        dialog.dismiss();
                        boolean isCommunicateExist = TabAndAppExistUtils.isTabExist(MyApplication.getInstance(), Constant.APP_TAB_BAR_COMMUNACATE);
                        if (NetUtils.isNetworkConnected(getApplicationContext(), false) && MyApplication.getInstance().isV1xVersionChat() && isCommunicateExist) {
                            loadingDlg.show();
                            WSAPIService.getInstance().sendAppStatus("REMOVED");
                        } else {
                            MyApplication.getInstance().signout();
                        }
                        stopAppService();
                    }
                })
                .show();
    }

    /**
     * 关闭服务
     */
    private void stopAppService() {
        stopService(new Intent(getApplicationContext(), CoreService.class));
        stopService(new Intent(getApplicationContext(), BackgroundService.class));
    }


    /**
     * 弹出清除缓存选项提示框
     */
    private void showClearCacheDlg() {
        // TODO Auto-generated method stub
        final String[] items = new String[]{getString(R.string.settings_clean_imgae_attachment), getString(R.string.settings_clean_web), getString(R.string.settings_clean_all)};
        new QMUIDialog.MenuDialogBuilder(SettingActivity.this)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                DataCleanManager.cleanApplicationData(
                                        SettingActivity.this, MyAppConfig.LOCAL_DOWNLOAD_PATH,
                                        MyAppConfig.LOCAL_CACHE_PATH);
                                ImageDisplayUtils.getInstance().clearAllCache();
                                handler.sendEmptyMessage(DATA_CLEAR_SUCCESS);
                                break;
                            case 1:
                                DataCleanManager.cleanWebViewCache(SettingActivity.this);
                                ToastUtils.show(getApplicationContext(),
                                        R.string.data_clear_success);
                                break;
                            case 2:
                                showClearCacheWarningDlg();
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }

    /**
     * 弹出清除全部缓存提示框
     */
    private void showClearCacheWarningDlg() {
        // TODO Auto-generated method stub
        new MyQMUIDialog.MessageDialogBuilder(SettingActivity.this)
                .setMessage(getString(R.string.my_setting_tips_quit))
                .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        DataCleanManager.cleanWebViewCache(SettingActivity.this);
                        ((MyApplication) getApplicationContext()).deleteAllDb();
                        String msgCachePath = MyAppConfig.LOCAL_DOWNLOAD_PATH;
                        String imgCachePath = MyAppConfig.LOCAL_CACHE_PATH;
                        DataCleanManager.cleanApplicationData(SettingActivity.this,
                                msgCachePath, imgCachePath);
                        MyApplication.getInstance().setIsContactReady(false);
                        //当清除所有缓存的时候清空以db形式存储数据的configVersion
                        ClientConfigUpdateUtils.getInstance().clearDbDataConfigWithClearAllCache();
                        ImageDisplayUtils.getInstance().clearAllCache();
                        MyAppCacheUtils.clearMyAppList(SettingActivity.this);
                        //清除全部缓存时是否需要清除掉小程序，如果需要，解开下面一行的注释
//					ReactNativeFlow.deleteReactNativeInstallDir(MyAppConfig.getReactInstallPath(SettingActivity.this,userId));
                        ToastUtils.show(getApplicationContext(),
                                R.string.data_clear_success);
                        //((MyApplication) getApplicationContext()).exit();
                        Intent intent = new Intent(SettingActivity.this,
                                IndexActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (handler != null) {
            handler = null;
        }
    }


    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReiceiveWebsocketRemoveCallback(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_WEBSOCKET_STATUS_REMOVE)) {
            LoadingDialog.dimissDlg(loadingDlg);
            MyApplication.getInstance().signout(true);
            stopAppService();
        }

    }


    private void saveWebAutoRotateConfig(boolean isWebAutoRotate) {
        if (NetUtils.isNetworkConnected(this)) {
            AppAPIService apiService = new AppAPIService(this);
            apiService.saveWebAutoRotateConfig(isWebAutoRotate);
        }
    }

    private void getUserExperienceUpgradeFlag() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            apiService.getUserExperienceUpgradeFlag();
        }

    }

    private void updateUserExperienceUpgradeFlag() {
        boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.updateUserExperienceUpgradeFlag(isExperienceUpgradeFlag ? 0 : 1);
        } else {
            experienceUpgradeSwitch.setOpened(isExperienceUpgradeFlag);
        }

    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnExperienceUpgradeFlagSuccess(GetExperienceUpgradeFlagResult getExperienceUpgradeFlagResult) {
            boolean isExperienceUpgradeFlag = (getExperienceUpgradeFlagResult.getStatus() == 1);
            PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, isExperienceUpgradeFlag);
            if (experienceUpgradeSwitch.isOpened() != isExperienceUpgradeFlag) {
                experienceUpgradeSwitch.setOpened(isExperienceUpgradeFlag);
            }

        }

        @Override
        public void returnExperienceUpgradeFlagFail(String error, int errorCode) {

        }

        @Override
        public void returnDeviceCheckSuccess(GetDeviceCheckResult getDeviceCheckResult) {
            super.returnDeviceCheckSuccess(getDeviceCheckResult);
        }

        @Override
        public void returnUpdateExperienceUpgradeFlagSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
            PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, !isExperienceUpgradeFlag);
            experienceUpgradeSwitch.setOpened(!isExperienceUpgradeFlag);
        }

        @Override
        public void returnUpdateExperienceUpgradeFlagFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
            experienceUpgradeSwitch.setOpened(isExperienceUpgradeFlag);
        }
    }
}
