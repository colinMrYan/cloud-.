package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.NotificationSetUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.Language;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.bean.mine.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.bean.system.AppConfig;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;
import com.inspur.emmcloud.bean.system.navibar.NaviBarScheme;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.chat.DisplayMediaVoiceMsg;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyAppCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {

    private static final int DATA_CLEAR_SUCCESS = 0;
    @BindView(R.id.switch_view_setting_web_rotate)
    SwitchCompat webRotateSwitch;
    @BindView(R.id.switch_view_setting_voice_2_word)
    SwitchCompat voice2WordSwitch;
    @BindView(R.id.rl_setting_voice_2_word)
    RelativeLayout voice2WordLayout;
    @BindView(R.id.rl_setting_experience_upgrade)
    RelativeLayout experienceUpgradeLayout;
    @BindView(R.id.switch_view_setting_experience_upgrade)
    SwitchCompat experienceUpgradeSwitch;
    @BindView(R.id.tv_setting_language_name)
    TextView languageNameText;
    @BindView(R.id.iv_setting_language_flag)
    ImageView languageFlagImg;
    @BindView(R.id.tv_setting_theme_name)
    TextView themeNameText;
    @BindView(R.id.rl_setting_switch_tablayout)
    RelativeLayout switchTabLayout;
    @BindView(R.id.tv_setting_tab_name)
    TextView tabName;
    @BindView(R.id.switch_view_setting_notification)
    SwitchCompat notificationSwitch;
    private Handler handler;
    private MineAPIService apiService;
    private LoadingDialog loadingDlg;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.switch_view_setting_web_rotate:
                    AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, String.valueOf(b));
                    AppConfigCacheUtils.saveAppConfig(MyApplication.getInstance(), appConfig);
                    saveWebAutoRotateConfig(b);
                    break;
                case R.id.switch_view_setting_voice_2_word:
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH,
                            b ? DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN : DisplayMediaVoiceMsg.IS_VOICE_WORD_CLOUSE);
                    break;
                case R.id.switch_view_setting_experience_upgrade:
                    boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
                    if (isExperienceUpgradeFlag != b) {
                        updateUserExperienceUpgradeFlag();
                    }
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
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
        setLanguage();
        handMessage();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationSwitch.setChecked(getSwitchOpen());
    }

    /**
     * 开关push并向服务器发出信号
     */
    private void switchPush() {
        boolean switchFlag = PreferencesByUserAndTanentUtils.getBoolean(this,
                Constant.PUSH_SWITCH_FLAG, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setPushStatus(NotificationSetUtils.isNotificationEnabled(this) && switchFlag);
        } else {
            setPushStatus(switchFlag);
        }
    }

    private void setPushStatus(boolean openPush) {
        if (openPush) {
            PushManagerUtils.getInstance().startPush();
            PushManagerUtils.getInstance().registerPushId2Emm();
        } else {
            PushManagerUtils.getInstance().stopPush();
            PushManagerUtils.getInstance().unregisterPushId2Emm();
        }
    }

    private boolean getSwitchOpen() {
        boolean isOpen = PreferencesByUserAndTanentUtils.getBoolean(SettingActivity.this, Constant.PUSH_SWITCH_FLAG, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !NotificationSetUtils.isNotificationEnabled(this)) {
            isOpen = false;
        }
        return isOpen;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MineAPIService(this);
        apiService.setAPIInterface(new WebService());
        setWebAutoRotateState();
        webRotateSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            voice2WordLayout.setVisibility(View.VISIBLE);
            voice2WordSwitch.setChecked(AppUtils.getIsVoiceWordOpen());
            voice2WordSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        }
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        showNotificationDlg();
                    }
                    notificationSwitch.setChecked(true);
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PUSH_SWITCH_FLAG, true);
                    switchPush();
                } else {
                    showNotificationCloseDlg();

                }
            }
        });
        if (AppUtils.isAppVersionStandard()) {
            getUserExperienceUpgradeFlag();
            experienceUpgradeLayout.setVisibility(View.VISIBLE);
            boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
            PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, isExperienceUpgradeFlag);
            experienceUpgradeSwitch.setChecked(isExperienceUpgradeFlag);
            experienceUpgradeSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        }
        themeNameText.setText(ThemeSwitchActivity.getThemeName());
        NaviBarModel naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(this, Constant.APP_TAB_LAYOUT_DATA, ""));
        switchTabLayout.setVisibility(naviBarModel.getNaviBarPayload().getNaviBarSchemeList().size() > 1 ? View.VISIBLE : View.GONE);
        tabName.setText(getTabLayoutName());
    }

    private void showNotificationCloseDlg() {
        new CustomDialog.MessageDialogBuilder(SettingActivity.this)
                .setMessage(R.string.notification_switch_cant_recive)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notificationSwitch.setChecked(true);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        notificationSwitch.setChecked(false);
                        PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
                        switchPush();
                    }
                })
                .show();
    }

    private String getTabLayoutName() {
        NaviBarModel naviBarModel = new NaviBarModel(PreferencesByUserAndTanentUtils.getString(this, Constant.APP_TAB_LAYOUT_DATA, ""));
        List<NaviBarScheme> naviBarSchemeList = naviBarModel.getNaviBarPayload().getNaviBarSchemeList();
        String currentTabLayoutName = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.APP_TAB_LAYOUT_NAME, "");
        if (StringUtils.isBlank(currentTabLayoutName)) {
            currentTabLayoutName = naviBarModel.getNaviBarPayload().getDefaultScheme();
        }
        String tabName = "";
        for (int i = 0; i < naviBarSchemeList.size(); i++) {
            NaviBarScheme naviBarScheme = naviBarSchemeList.get(i);
            if (naviBarScheme.getName().equals(currentTabLayoutName)) {
                return getTabNameByLangudge(naviBarScheme);
            }
        }
        if (StringUtils.isBlank(tabName)) {
            String defaultScheme = naviBarModel.getNaviBarPayload().getDefaultScheme();
            for (int i = 0; i < naviBarSchemeList.size(); i++) {
                NaviBarScheme naviBarScheme = naviBarSchemeList.get(i);
                if (naviBarSchemeList.get(i).getName().equals(defaultScheme)) {
                    return getTabNameByLangudge(naviBarScheme);
                }
            }
        }
        return "";
    }

    private String getTabNameByLangudge(NaviBarScheme naviBarScheme) {
        String tempTabName = "";
        Configuration config = getResources().getConfiguration();
        String environmentLanguage = config.locale.getLanguage();
        switch (environmentLanguage.toLowerCase()) {
            case "zh-hant":
                tempTabName = naviBarScheme.getNaviBarTitleResult().getZhHans();
                break;
            case "en":
            case "en-us":
                tempTabName = naviBarScheme.getNaviBarTitleResult().getEnUS();
                break;
            default:
                tempTabName = naviBarScheme.getNaviBarTitleResult().getZhHans();
                break;
        }
        return tempTabName;
    }

    private void setWebAutoRotateState() {
        boolean isWebAutoRotate = Boolean.parseBoolean(AppConfigCacheUtils.getAppConfigValue(this, Constant.CONCIG_WEB_AUTO_ROTATE, "false"));
        webRotateSwitch.setChecked(isWebAutoRotate);
    }

    /**
     * 设置显示app语言
     */
    private void setLanguage() {
        // TODO Auto-generated method stub
        String languageName = LanguageManager.getInstance().getCurrentLanguageName();
        String languageJson = LanguageManager.getInstance().getCurrentLanguageJson();
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
            languageFlagImg.setImageResource(R.drawable.ic_mine_language_follow_system);
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

    int REQUEST_CODE_CAMERA = 10002;
    Uri fileUri = null;

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
            case R.id.rl_setting_self_start: //TODO zyj
//                UriUtils.openUrl(this, "http://www.baidu.com");
//                ARouter.getInstance().build("/meeting/history").navigation();
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.CAMERA,
                            new PermissionRequestCallback() {
                                @Override
                                public void onPermissionRequestSuccess(List<String> permissions) {
                                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                    try {
                                        fileUri = FileProvider.getUriForFile(SettingActivity.this,
                                                getApplicationContext().getPackageName() + ".provider", createMediaFile());//这是正确的写法

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3);
                                    startActivityForResult(intent, 1);
                                }

                                @Override
                                public void onPermissionRequestFail(List<String> permissions) {

                                }
                            });
                }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
                Log.d("zhang", "onActivityResult: url = " + data.getData());
                Intent intent = new Intent(this, PlayVideoActivity.class);
                intent.setData(data.getData());
                startActivity(intent);
//                vv_play.setVideoURI(fileUri);
//                vv_play.requestFocus();
            }
        }
    }

    private File createMediaFile() throws IOException {
        if (AppUtils.isHasSDCard(this)) {
            if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
                // 选择自己的文件夹
                String path = Environment.getExternalStorageDirectory().getPath() + "/myvideo/";
                // Constants.video_url 是一个常量，代表存放视频的文件夹
                File mediaStorageDir = new File(path);
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.e("TAG", "文件夹创建失败");
                        return null;
                    }
                }

                // 文件根据当前的毫秒数给自己命名
                String timeStamp = String.valueOf(System.currentTimeMillis());
                timeStamp = timeStamp.substring(7);
                String imageFileName = "V" + timeStamp;
                String suffix = ".mp4";
                File mediaFile = new File(mediaStorageDir + File.separator + imageFileName + suffix);
                return mediaFile;
            }
        }
        return null;
    }

    /**
     * 弹出注销提示框
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showNotificationDlg() {
        if (!NotificationSetUtils.isNotificationEnabled(SettingActivity.this)) {
            new CustomDialog.MessageDialogBuilder(SettingActivity.this)
                    .setMessage(getString(R.string.notification_switch_open_setting))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            notificationSwitch.setChecked(false);
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
        new CustomDialog.MessageDialogBuilder(SettingActivity.this)
                .setMessage(R.string.if_confirm_signout)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PushManagerUtils.getInstance().unregisterPushId2Emm();
                        dialog.dismiss();
                        boolean isCommunicateExist = TabAndAppExistUtils.isTabExist(MyApplication.getInstance(), Constant.APP_TAB_BAR_COMMUNACATE);
                        if (NetUtils.isNetworkConnected(getApplicationContext(), false) && WebServiceRouterManager.getInstance().isV1xVersionChat() && isCommunicateExist) {
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
    }


    /**
     * 弹出清除缓存选项提示框
     */
    private void showClearCacheDlg() {
        final String[] items = new String[]{getString(R.string.settings_clean_imgae_attachment), getString(R.string.settings_clean_web), getString(R.string.settings_clean_all)};
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.cus_dialog_style);

        new CustomDialog.ListDialogBuilder(ctw)
                .setItems(items, new DialogInterface.OnClickListener() {
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
                }).show();
    }

    /**
     * 弹出清除全部缓存提示框
     */
    private void showClearCacheWarningDlg() {
        // TODO Auto-generated method stub
        new CustomDialog.MessageDialogBuilder(SettingActivity.this)
                .setMessage(getString(R.string.my_setting_tips_quit))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
            EventBus.getDefault().unregister(this);
            LoadingDialog.dimissDlg(loadingDlg);
            MyApplication.getInstance().signout();
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
            experienceUpgradeSwitch.setChecked(isExperienceUpgradeFlag);
        }

    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnExperienceUpgradeFlagSuccess(GetExperienceUpgradeFlagResult getExperienceUpgradeFlagResult) {
            boolean isExperienceUpgradeFlag = (getExperienceUpgradeFlagResult.getStatus() == 1);
            PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, isExperienceUpgradeFlag);
            if (experienceUpgradeSwitch.isChecked() != isExperienceUpgradeFlag) {
                experienceUpgradeSwitch.setChecked(isExperienceUpgradeFlag);
            }

        }

        @Override
        public void returnExperienceUpgradeFlagFail(String error, int errorCode) {

        }


        @Override
        public void returnUpdateExperienceUpgradeFlagSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
            PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, !isExperienceUpgradeFlag);
            experienceUpgradeSwitch.setChecked(!isExperienceUpgradeFlag);
        }

        @Override
        public void returnUpdateExperienceUpgradeFlagFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            boolean isExperienceUpgradeFlag = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_EXPERIENCE_UPGRATE, false);
            experienceUpgradeSwitch.setChecked(isExperienceUpgradeFlag);
        }
    }
}
