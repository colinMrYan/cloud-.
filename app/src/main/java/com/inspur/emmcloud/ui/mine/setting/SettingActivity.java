package com.inspur.emmcloud.ui.mine.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.GetMDMStateResult;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.CalEventNotificationUtils;
import com.inspur.emmcloud.util.DataCleanManager;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import static com.inspur.emmcloud.R.id.device_manager_layout;

public class SettingActivity extends BaseActivity {

    private LoadingDialog loadingDlg;
    private static final int DATA_CLEAR_SUCCESS = 0;
    private Handler handler;
    private TextView languageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ((MyApplication) getApplicationContext()).addActivity(this);
        loadingDlg = new LoadingDialog(this);
        languageText = (TextView) findViewById(R.id.msg_languagechg_result_text);
        setLanguage();
        handMessage();
        getMDMState();
    }

    /**
     * 设置显示app语言
     */
    private void setLanguage() {
        // TODO Auto-generated method stub
        String languageName = PreferencesUtils.getString(
                getApplicationContext(), UriUtils.tanent + "language", "");
        String languageJson = PreferencesUtils
                .getString(this, UriUtils.tanent + "appLanguageObj");
        if (languageJson != null && !languageName.equals("followSys")) {
            Language language = new Language(languageJson);
            languageText.setText(new Language(languageJson).getLabel());
            setLanguageFlagImg(language);
        } else {
            languageText.setText(getString(R.string.follow_system));
        }
    }

    /**
     * 语言设置国旗
     *
     * @param language
     */
    private void setLanguageFlagImg(Language language) {
        // TODO Auto-generated method stub
        String iso = language.getIso();
        iso = iso.replace("-", "_");
        iso = iso.toLowerCase();
        int id = getResources().getIdentifier(iso, "drawable", getApplicationContext().getPackageName());
        ((ImageView) findViewById(R.id.msg_language_flag_img)).setImageResource(id);
    }

    private void getMDMState() {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            MineAPIService apiService = new MineAPIService(this);
            apiService.setAPIInterface(new Webservice());
            apiService.getMDMState();
        } else {
            setMDMLayoutState(null);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
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

        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.signout_layout:
//			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//					if (which == -1) {
//						signout();
//					}
//				}
//			};
//			EasyDialog.showDialog(SettingActivity.this,
//					getString(R.string.prompt),
//					getString(R.string.if_confirm_signout),
//					getString(R.string.ok), getString(R.string.cancel),
//					dialogClickListener, true);


//			intent.setClass(SettingActivity.this, GestureLoginActivity.class);
//			startActivity(intent);
                intent.setClass(SettingActivity.this, CreateGestureActivity.class);
                startActivity(intent);


                break;
            case device_manager_layout:
                intent.setClass(SettingActivity.this,
                        DeviceManagerActivity.class);
                startActivity(intent);
                break;
            case R.id.msg_notify_layout:
                // ToastUtils.show(getApplicationContext(),
                // R.string.function_not_implemented);
                break;
            case R.id.about_layout:
                intent.setClass(SettingActivity.this, AboutActivity.class);
                startActivity(intent);

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
                Intent intentGesture = new Intent();
                intentGesture.setClass(this,SwitchGestureActivity.class);
                startActivity(intentGesture);
                break;
            default:
                break;
        }
    }

    /**
     * 设置设备管理layout显示状态
     *
     * @param mdmState
     */
    private void setMDMLayoutState(Integer mdmState) {
        if (mdmState == null) {
            mdmState = PreferencesByUserAndTanentUtils.getInt(getApplicationContext(), "mdm_state", 1);
        }
        (findViewById(R.id.device_manager_layout)).setVisibility((mdmState == 1) ? View.VISIBLE : View.GONE);
    }

    /**
     * 弹出清除缓存选项提示框
     */
    private void showClearCacheDlg() {
        // TODO Auto-generated method stub
        float radio = 0.850f;
        final MyDialog clearCacheDlg = new MyDialog(SettingActivity.this,
                R.layout.dialog_four_item, R.style.userhead_dialog_bg, radio);
        TextView clearImgAndFileText = (TextView) clearCacheDlg
                .findViewById(R.id.text1);
        // String imgAndFileSize = DataCleanManager
        // .getAllCacheSize(SettingActivity.this);
        clearImgAndFileText
                .setText(getString(R.string.settings_clean_imgae_attachment));
        TextView clearWebCacheText = (TextView) clearCacheDlg
                .findViewById(R.id.text2);
        clearWebCacheText.setText(getString(R.string.settings_clean_web));
        TextView clearAllCacheText = (TextView) clearCacheDlg
                .findViewById(R.id.text3);
        clearAllCacheText.setText(getString(R.string.settings_clean_all));
        TextView cancelText = (TextView) clearCacheDlg.findViewById(R.id.text4);
        cancelText.setText(getString(R.string.button_cancel));
        clearImgAndFileText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        String msgCachePath = Environment
                                .getExternalStorageDirectory()
                                + "/IMP-Cloud/download/";
                        String imgCachePath = MyAppConfig.LOCAL_CACHE_PATH;
                        DataCleanManager.cleanApplicationData(
                                SettingActivity.this, msgCachePath,
                                imgCachePath);
                        ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(
                                getApplicationContext(),
                                R.drawable.icon_photo_default);
                        imageDisplayUtils.clearCache();
                        handler.sendEmptyMessage(DATA_CLEAR_SUCCESS);
                    }
                }).start();
                clearCacheDlg.dismiss();
            }
        });
        clearWebCacheText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                clearCacheDlg.dismiss();
                DataCleanManager.cleanWebViewCache(SettingActivity.this);
                ToastUtils.show(getApplicationContext(),
                        R.string.data_clear_success);
            }
        });
        clearAllCacheText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                clearCacheDlg.dismiss();
                showClearCacheWarningDlg();
            }

        });
        cancelText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                clearCacheDlg.dismiss();
            }
        });
        clearCacheDlg.show();
    }

    /**
     * 弹出清除全部缓存提示框
     */
    private void showClearCacheWarningDlg() {
        // TODO Auto-generated method stub
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (which == -1) {
                    DataCleanManager.cleanWebViewCache(SettingActivity.this);
                    ((MyApplication) getApplicationContext()).deleteAllDb();
                    String msgCachePath = Environment
                            .getExternalStorageDirectory()
                            + "/IMP-Cloud/download/";
                    String imgCachePath = MyAppConfig.LOCAL_CACHE_PATH;
                    DataCleanManager.cleanApplicationData(SettingActivity.this,
                            msgCachePath, imgCachePath);
                    ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(
                            getApplicationContext(),
                            R.drawable.icon_photo_default);
                    imageDisplayUtils.clearCache();
                    //清除全部缓存时是否需要清除掉小程序，如果需要，解开下面一行的注释
//					ReactNativeFlow.deleteReactNativeInstallDir(MyAppConfig.getReactInstallPath(SettingActivity.this,userId));
                    ToastUtils.show(getApplicationContext(),
                            R.string.data_clear_success);
                    ((MyApplication) getApplicationContext()).exit();
                    Intent intentLog = new Intent(SettingActivity.this,
                            IndexActivity.class);
                    intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentLog);
                }
            }
        };
        EasyDialog.showDialog(SettingActivity.this, getString(R.string.prompt),
                getString(R.string.my_setting_tips_quit),
                getString(R.string.ok), getString(R.string.cancel),
                dialogClickListener, true);
    }

    private void signout() {
        // TODO Auto-generated method stub
        if (((MyApplication) getApplicationContext()).getWebSocketPush() != null) {
            ((MyApplication) getApplicationContext()).getWebSocketPush()
                    .webSocketSignout();
        }
        //清除日历提醒极光推送本地通知
        CalEventNotificationUtils.cancelAllCalEventNotification(SettingActivity.this);
        ((MyApplication) getApplicationContext()).stopPush();
        ((MyApplication) getApplicationContext()).clearNotification();
        ((MyApplication) getApplicationContext()).removeAllCookie();
        ((MyApplication) getApplicationContext()).clearUserPhotoMap();
        PreferencesUtils.putString(SettingActivity.this, "tokenType", "");
        PreferencesUtils.putString(SettingActivity.this, "accessToken", "");
        ((MyApplication) getApplicationContext()).setAccessToken("");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }

    private class Webservice extends APIInterfaceInstance {

        @Override
        public void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            int mdmState = getMDMStateResult.getMdmState();
            PreferencesByUserAndTanentUtils.putInt(getApplicationContext(), "mdm_state", mdmState);
            setMDMLayoutState(mdmState);

        }

        @Override
        public void returnMDMStateFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            setMDMLayoutState(null);
            WebServiceMiddleUtils.hand(SettingActivity.this, error, errorCode);
        }


    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
    }

}
