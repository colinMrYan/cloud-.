package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.AppConfig;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.BackgroundService;
import com.inspur.emmcloud.service.CoreService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.AppConfigCacheUtils;
import com.inspur.emmcloud.util.DataCleanManager;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.MyAppCacheUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

public class SettingActivity extends BaseActivity {

    private static final int DATA_CLEAR_SUCCESS = 0;
    private Handler handler;
    private LoadingDialog loadingDlg;
    private SwitchView webAutoRotateSwitch;
    private SwitchView backgroundRunSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        setLanguage();
        handMessage();
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        webAutoRotateSwitch = (SwitchView) findViewById(R.id.web_auto_rotate_switch);
        setWebAutoRotateState();
        webAutoRotateSwitch.setOnStateChangedListener(onStateChangedListener);
        backgroundRunSwitch = (SwitchView) findViewById(R.id.background_run_switch);
        boolean isAppSetRunBackground = PreferencesUtils.getBoolean(getApplicationContext(), Constant.PREF_APP_RUN_BACKGROUND, false);
        backgroundRunSwitch.setOpened(isAppSetRunBackground);
        backgroundRunSwitch.setOnStateChangedListener(onStateChangedListener);
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
                getApplicationContext(), UriUtils.tanent + "language", "");
        String languageJson = PreferencesUtils
                .getString(this, UriUtils.tanent + "appLanguageObj");
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
            // TODO Auto-generated method stub
            if (view.getId() != R.id.background_run_switch) {
                saveWebAutoRotateConfig(true);
            } else {
                setAppRunBackground(true);
            }

        }

        @Override
        public void toggleToOff(View view) {
            // TODO Auto-generated method stub
            if (view.getId() != R.id.background_run_switch) {
                saveWebAutoRotateConfig(false);
            } else {
                setAppRunBackground(false);
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
                sendBroadcast(intent);

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
            case R.id.about_layout:
                IntentUtils.startActivity(SettingActivity.this, AboutActivity.class);
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
        new QMUIDialog.MessageDialogBuilder(SettingActivity.this)
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
                        dialog.dismiss();
                        ((MyApplication) getApplication()).signout();
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
        new QMUIDialog.MessageDialogBuilder(SettingActivity.this)
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
                        String msgCachePath = Environment
                                .getExternalStorageDirectory()
                                + "/IMP-Cloud/download/";
                        String imgCachePath = MyAppConfig.LOCAL_CACHE_PATH;
                        DataCleanManager.cleanApplicationData(SettingActivity.this,
                                msgCachePath, imgCachePath);
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
        if (handler != null) {
            handler = null;
        }
    }


    private void saveWebAutoRotateConfig(boolean isWebAutoRotate) {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            AppAPIService apiService = new AppAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.saveWebAutoRotateConfig(isWebAutoRotate);
        } else {
            setWebAutoRotateState();
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnSaveWebAutoRotateConfigSuccess(boolean isWebAutoRotate) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, isWebAutoRotate + "");
            AppConfigCacheUtils.saveAppConfig(SettingActivity.this, appConfig);
            setWebAutoRotateState();
        }

        @Override
        public void returnSaveWebAutoRotateConfigFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            setWebAutoRotateState();
            WebServiceMiddleUtils.hand(SettingActivity.this, error, errorCode);
        }
    }

}
