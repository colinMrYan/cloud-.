package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.baselib.util.NotificationSetUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.SwitchView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.system.AppConfig;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyAppCacheUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {

    private static final int DATA_CLEAR_SUCCESS = 0;
    @BindView(R.id.switch_view_setting_web_rotate)
    SwitchView webRotateSwitch;
    @BindView(R.id.switch_view_setting_notification)
    Switch notificationSwitch;
    private Handler handler;
    private MineAPIService apiService;
    private LoadingDialog loadingDlg;
    private SwitchView.OnStateChangedListener onStateChangedListener = new SwitchView.OnStateChangedListener() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void toggleToOn(View view) {
            switch (view.getId()) {
                case R.id.switch_view_setting_web_rotate:
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
                case R.id.switch_view_setting_web_rotate:
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


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
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
                Constant.PUSH_SWITCH_FLAG,false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setPushStatus(NotificationSetUtils.isNotificationEnabled(this) && switchFlag);
        }else{
            setPushStatus(switchFlag);
        }
    }

    private void setPushStatus(boolean openPush) {
        if(openPush){
            PushManagerUtils.getInstance().startPush();
            PushManagerUtils.getInstance().registerPushId2Emm();
        } else {
            PushManagerUtils.getInstance().stopPush();
            PushManagerUtils.getInstance().unregisterPushId2Emm();
        }
    }

    private boolean getSwitchOpen() {
        boolean isOpen = PreferencesByUserAndTanentUtils.getBoolean(SettingActivity.this,Constant.PUSH_SWITCH_FLAG,true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !NotificationSetUtils.isNotificationEnabled(this)) {
            isOpen = false;
        }
        return isOpen;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MineAPIService(this);
        setWebAutoRotateState();
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
                    showNotificationCloseDlg();

                }
            }
        });
    }

    private void showNotificationCloseDlg() {
        new CustomDialog.MessageDialogBuilder(SettingActivity.this)
                    .setMessage(R.string.notification_switch_cant_recive)
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    notificationSwitch.setChecked(true);
                    dialog.dismiss();
                    })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    notificationSwitch.setChecked(false);
                    PreferencesByUserAndTanentUtils.putBoolean(SettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
                    switchPush();
                    })
                    .show();
    }



    private void setWebAutoRotateState() {
        boolean isWebAutoRotate = Boolean.parseBoolean(AppConfigCacheUtils.getAppConfigValue(this, Constant.CONCIG_WEB_AUTO_ROTATE, "false"));
        webRotateSwitch.setOpened(isWebAutoRotate);
    }


    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                ToastUtils.show(getApplicationContext(),
                        R.string.data_clear_success);

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
            case R.id.clear_cache_layout:
                showClearCacheDlg();
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
            new CustomDialog.MessageDialogBuilder(SettingActivity.this)
                    .setMessage(getString(R.string.notification_switch_open_setting))
                    .setNegativeButton(R.string.cancel, (dialog, index) -> {
                        dialog.dismiss();
                        notificationSwitch.setChecked(false);
                    })
                    .setPositiveButton(R.string.ok, (dialog, index) -> {
                        dialog.dismiss();
                        NotificationSetUtils.openNotificationSetting(SettingActivity.this);
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
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    PushManagerUtils.getInstance().unregisterPushId2Emm();
                    dialog.dismiss();
                    MyApplication.getInstance().signout();
                })
                .show();
    }

    /**
     * 弹出清除缓存选项提示框
     */
    private void showClearCacheDlg() {
        final String[] items = new String[]{getString(R.string.settings_clean_imgae_attachment), getString(R.string.settings_clean_web), getString(R.string.settings_clean_all)};
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.cus_dialog_style);

        new CustomDialog.ListDialogBuilder(this)
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
                .setNegativeButton(getString(R.string.cancel), (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.ok), (dialog, index) -> {
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



    private void saveWebAutoRotateConfig(boolean isWebAutoRotate) {
        if (NetUtils.isNetworkConnected(this)) {
            AppAPIService apiService = new AppAPIService(this);
            apiService.saveWebAutoRotateConfig(isWebAutoRotate);
        }
    }



}
