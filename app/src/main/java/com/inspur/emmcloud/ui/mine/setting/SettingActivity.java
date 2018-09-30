package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
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
import com.inspur.emmcloud.util.privates.ClientConfigUpdateUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.PushIdManagerUtils;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
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
    private LoadingDialog loadingDlg;
    private SwitchView webAutoRotateSwitch;
    private SwitchView backgroundRunSwitch;
    private SwitchView voice2WordSwitch;

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
        webAutoRotateSwitch = (SwitchView) findViewById(R.id.web_auto_rotate_switch);
        setWebAutoRotateState();
        webAutoRotateSwitch.setOnStateChangedListener(onStateChangedListener);
        backgroundRunSwitch = (SwitchView) findViewById(R.id.background_run_switch);
        voice2WordSwitch = (SwitchView) findViewById(R.id.switch_voice_word);
        boolean isAppSetRunBackground = PreferencesUtils.getBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, false);
        backgroundRunSwitch.setOpened(isAppSetRunBackground);
        backgroundRunSwitch.setOnStateChangedListener(onStateChangedListener);
        voice2WordSwitch.setOpened(PreferencesByUserAndTanentUtils.getInt(this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH,
                DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN) == DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN);
        voice2WordSwitch.setOnStateChangedListener(onStateChangedListener);
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
                    PreferencesByUserAndTanentUtils.putInt(SettingActivity.this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH, DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN);
                    voice2WordSwitch.setOpened(true);
                    break;
                case R.id.web_auto_rotate_switch:
                    AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "true");
                    AppConfigCacheUtils.saveAppConfig(MyApplication.getInstance(), appConfig);
                    setWebAutoRotateState();
                    saveWebAutoRotateConfig(true);
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
                    PreferencesByUserAndTanentUtils.putInt(SettingActivity.this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH, DisplayMediaVoiceMsg.IS_VOICE_WORD_CLOUSE);
                    voice2WordSwitch.setOpened(false);
                    break;
                case R.id.web_auto_rotate_switch:
                    AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, "false");
                    AppConfigCacheUtils.saveAppConfig(MyApplication.getInstance(), appConfig);
                    setWebAutoRotateState();
                    saveWebAutoRotateConfig(false);
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
                        boolean isCommunicateExist = TabAndAppExistUtils.isTabExist(MyApplication.getInstance(),Constant.APP_TAB_BAR_COMMUNACATE);
                        if (NetUtils.isNetworkConnected(getApplicationContext(),false) && MyApplication.getInstance().isV1xVersionChat() && isCommunicateExist){
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
                        Intent intentLog = new Intent(SettingActivity.this,
                                IndexActivity.class);
                        intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentLog);
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

}
