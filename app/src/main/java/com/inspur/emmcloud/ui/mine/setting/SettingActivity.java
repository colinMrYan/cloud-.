package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.BackgroundService;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.chat.DisplayMediaVoiceMsg;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientConfigUpdateUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.PushIdManagerUtils;
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

public class SettingActivity extends BaseActivity {

    private static final int DATA_CLEAR_SUCCESS = 0;
    private Handler handler;
    private SwitchView webAutoRotateSwitch;
    private SwitchView backgroundRunSwitch;
    private SwitchView voice2WordSwitch;
    private RelativeLayout experienceUpgradeLayout;
    private SwitchView experienceUpgradeSwitch;
    private MineAPIService apiService;
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        setLanguage();
        handMessage();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MineAPIService(this);
        apiService.setAPIInterface(new WebService());
        webAutoRotateSwitch = (SwitchView) findViewById(R.id.web_auto_rotate_switch);
        setWebAutoRotateState();
        webAutoRotateSwitch.setOnStateChangedListener(onStateChangedListener);
        backgroundRunSwitch = (SwitchView) findViewById(R.id.background_run_switch);
        boolean isAppSetRunBackground = PreferencesUtils.getBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, false);
        backgroundRunSwitch.setOpened(isAppSetRunBackground);
        backgroundRunSwitch.setOnStateChangedListener(onStateChangedListener);
        experienceUpgradeLayout = (RelativeLayout) findViewById(R.id.rl_experience_upgrade);
        experienceUpgradeSwitch = (SwitchView) findViewById(R.id.sw_experience_upgrade);
        if (MyApplication.getInstance().isV1xVersionChat()) {
            voice2WordSwitch = (SwitchView) findViewById(R.id.switch_voice_word);
            findViewById(R.id.rl_voice_word).setVisibility(View.VISIBLE);
            findViewById(R.id.v_voice_word_line).setVisibility(View.VISIBLE);
            voice2WordSwitch.setOpened(AppUtils.getIsVoiceWordOpen());
            voice2WordSwitch.setOnStateChangedListener(onStateChangedListener);
        }
        if (AppUtils.isAppVersionStandard()) {
            getUserExperienceUpgradeFlag();
            experienceUpgradeLayout.setVisibility(View.VISIBLE);
            boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
            experienceUpgradeSwitch.setOpened(isExperienceUpgradeFlag);
            experienceUpgradeSwitch.setOnStateChangedListener(onStateChangedListener);
        }

    }

    private void setWebAutoRotateState() {
        boolean isWebAutoRotate = Boolean.parseBoolean(AppConfigCacheUtils.getAppConfigValue(this, Constant.CONCIG_WEB_AUTO_ROTATE, "false"));
        webAutoRotateSwitch.setOpened(isWebAutoRotate);
    }

    /**
     * 设置显示app语言
     */
    private void setLanguage() {
        // TODO Auto-generated method stub
        TextView languageText = (TextView) findViewById(R.id.msg_languagechg_result_text);
        String languageName = PreferencesUtils.getString(
                getApplicationContext(), MyApplication.getInstance().getTanent() + "language", "");
        String languageJson = PreferencesUtils
                .getString(this, MyApplication.getInstance().getTanent() + "appLanguageObj");
        if (languageJson != null && !languageName.equals("followSys")) {
            Language language = new Language(languageJson);
            languageText.setText(new Language(languageJson).getLabel());
            String iso = language.getIso();
            iso = iso.replace("-", "_");
            iso = iso.toLowerCase();
            int id = getResources().getIdentifier(iso, "drawable", getApplicationContext().getPackageName());
            //设置语言国旗标志
            ((ImageView) findViewById(R.id.msg_language_flag_img)).setImageResource(id);
        } else {
            languageText.setText(getString(R.string.follow_system));
        }
    }

    private SwitchView.OnStateChangedListener onStateChangedListener = new SwitchView.OnStateChangedListener() {

        @Override
        public void toggleToOn(View view) {
            switch (view.getId()) {
                case R.id.background_run_switch:
                    setAppRunBackground(true);
                    break;
                case R.id.switch_voice_word:
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH, DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN);
                    voice2WordSwitch.setOpened(true);
                    break;
                case R.id.web_auto_rotate_switch:
                    AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "true");
                    AppConfigCacheUtils.saveAppConfig(MyApplication.getInstance(), appConfig);
                    setWebAutoRotateState();
                    saveWebAutoRotateConfig(true);
                    break;
                case R.id.sw_experience_upgrade:
                    updateUserExperienceUpgradeFlag();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void toggleToOff(View view) {
            switch (view.getId()) {
                case R.id.background_run_switch:
                    setAppRunBackground(false);
                    break;
                case R.id.switch_voice_word:
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH, DisplayMediaVoiceMsg.IS_VOICE_WORD_CLOUSE);
                    voice2WordSwitch.setOpened(false);
                    break;
                case R.id.web_auto_rotate_switch:
                    AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "false");
                    AppConfigCacheUtils.saveAppConfig(MyApplication.getInstance(), appConfig);
                    setWebAutoRotateState();
                    saveWebAutoRotateConfig(false);
                    break;
                case R.id.sw_experience_upgrade:
                    updateUserExperienceUpgradeFlag();
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 设置app是否运行在后台
     *
     * @param isAppSetRunBackground
     */
    private void setAppRunBackground(boolean isAppSetRunBackground) {
        PreferencesUtils.putBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, isAppSetRunBackground);
        backgroundRunSwitch.setOpened(isAppSetRunBackground);
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
            case R.id.back_layout:
                finish();
                break;
            case R.id.signout_layout:
                showSignoutDlg();
                break;
            case R.id.msg_languagechg_layout:
                IntentUtils.startActivity(SettingActivity.this,
                        LanguageChangeActivity.class);
                break;
            case R.id.clear_cache_layout:
                showClearCacheDlg();
                break;
            case R.id.switch_enterprese_text:
                IntentUtils.startActivity(SettingActivity.this, SwitchEnterpriseActivity.class);
                break;
            case R.id.setting_gesture_layout:
                IntentUtils.startActivity(SettingActivity.this, SafeCenterActivity.class);
                break;
            case R.id.rl_switch_theme:
                int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
                PreferencesUtils.putInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, (currentThemeNo == 0) ? 1 : 0);
                Intent intent = new Intent(SettingActivity.this,
                        IndexActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            default:
                break;
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
                        new PushIdManagerUtils(SettingActivity.this).unregisterPushId2Emm();
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
