package com.inspur.emmcloud;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.inspur.emmcloud.service.AppExceptionService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.login.ModifyUserFirstPsdActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LanguageUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ResolutionUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

/**
 * 应用启动Activity
 *
 * @author Administrator
 *
 */
public class MainActivity extends Activity { // 此处不能继承BaseActivity 推送会有问题

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private Handler handler;
    private LanguageUtils languageUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this);
        setContentView(R.layout.activity_main);
		/* 解决了在sd卡中第一次安装应用，进入到主页并切换到后台再打开会重新启动应用的bug */
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
		//进行app异常上传
		startUploadExceptionService();
        ((MyApplication) getApplicationContext()).addActivity(this);
        // 检测分辨率、网络环境
        if (!ResolutionUtils.isFitResolution(MainActivity.this)) {
            showResolutionDialog();
        } else {
            initEnvironment();
        }
    }

    private void startUploadExceptionService(){
        Intent intent = new Intent();
        intent.setClass(this, AppExceptionService.class);
        startService(intent);
    }


    /** 显示分辨率不符合条件的提示框 **/
    private void showResolutionDialog() {
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
		getServerLanguage();
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        // 是否已建立简易密码
                        if (!PreferencesUtils.getBoolean(MainActivity.this,
                                "hasPassword")) {
                            IntentUtils.startActivity(MainActivity.this,
                                    ModifyUserFirstPsdActivity.class, true);
                        } else {
                            IntentUtils.startActivity(MainActivity.this,
                                    IndexActivity.class, true);
                        }
                        break;
                    case LOGIN_FAIL:
                        IntentUtils.startActivity(MainActivity.this,
                                LoginActivity.class, true);
                        break;
                    case GET_LANGUAGE_SUCCESS:
                        enterApp();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    /**
     * 获取服务端支持的语言
     */
    private void getServerLanguage() {
        // TODO Auto-generated method stub
        String accessToken = PreferencesUtils.getString(MainActivity.this,
                "accessToken", "");
        String myInfo = PreferencesUtils.getString(getApplicationContext(),
                "myInfo", "");
        String languageJson = PreferencesUtils.getString(getApplicationContext(),
                UriUtils.tanent + "appLanguageObj");
        if (!StringUtils.isBlank(accessToken) && !StringUtils.isBlank(myInfo) && StringUtils.isBlank(languageJson)) {
            languageUtils = new LanguageUtils(MainActivity.this, handler);
            languageUtils.getServerSupportLanguage();
        }else {
            enterApp();
        }
    }


    /**
     * 进入App
     */
    private void enterApp() {
        // TODO Auto-generated method stub
        Boolean isFirst = PreferencesUtils.getBoolean(
                MainActivity.this, "isFirst", true);
        if (checkIfUpgraded() || isFirst) {
            IntentUtils.startActivity(MainActivity.this,
                    GuideActivity.class, true);
        } else {
            loginApp();
        }
    }

    /**
     * 检测是否应用版本是否进行了升级
     *
     * @return
     */
    private boolean checkIfUpgraded() {
        boolean ifUpgraded = false;
        String savedVersion = PreferencesUtils.getString(MainActivity.this,
                "previousVersion", "");
        String currentVersion = AppUtils.getVersion(MainActivity.this);
        if (TextUtils.isEmpty(savedVersion)) {
            return false;
        } else {
            ifUpgraded = AppUtils
                    .isAppHasUpgraded(savedVersion, currentVersion);
        }
        return ifUpgraded;
    }

    /**
     * 登录app
     */
    private void loginApp() {
        // TODO Auto-generated method stub
        String accessToken = PreferencesUtils.getString(MainActivity.this,
                "accessToken", "");
        String myInfo = PreferencesUtils.getString(getApplicationContext(),
                "myInfo", "");
        if ((!StringUtils.isBlank(accessToken))
                && (!StringUtils.isBlank(myInfo))) {
            IntentUtils.startActivity(MainActivity.this, IndexActivity.class,
                    true);
        } else {
            IntentUtils.startActivity(MainActivity.this, LoginActivity.class,
                    true);
        }
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}
