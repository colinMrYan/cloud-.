package com.inspur.emmcloud.login.util;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.widget.SwitchCompat;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.MaxHeightListView;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.Enterprise;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;
import com.inspur.emmcloud.login.bean.GetLoginResult;
import com.inspur.emmcloud.login.ui.LoginBySmsActivity;
import com.inspur.emmcloud.login.ui.adapter.LoginSelectEnterpriseAdapter;
import com.inspur.emmcloud.login.util.MDM.MDM;
import com.inspur.emmcloud.login.util.MDM.MDMListener;
import com.tencent.mmkv.MMKV;

import java.util.List;

/**
 * 登录公共类
 *
 * @author Administrator
 */
public class LoginUtils extends LoginAPIInterfaceImpl implements LanguageManager.GetServerLanguageListener {
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private LoginAPIService apiServices;
    private Activity activity;
    private Handler handler;
    private boolean isSMSLogin = false;
    private int mode = LoginBySmsActivity.MODE_LOGIN; // 默认登录模式
    private Handler loginUtilsHandler;
    private GetLoginResult getLoginResult;
    private LoadingDialog loadingDlg;
    private boolean isLogin = false;  //标记是登录界面(包括手机验证码登录界面)调用

    public LoginUtils(Activity activity, Handler handler) {
        this.handler = handler;
        this.activity = activity;
        loadingDlg = new LoadingDialog(activity);
        apiServices = new LoginAPIService(activity);
        apiServices.setAPIInterface(LoginUtils.this);
        handMessage();
    }

    public void autoLogin() {
//        String accessToken = PreferencesUtils.getString(BaseApplication.getInstance(), "accessToken", "");
        MMKV kv = MMKV.mmkvWithID("InterProcessKV", MMKV.MULTI_PROCESS_MODE);
        String accessToken = kv.decodeString("accessToken", "");
        String myInfo = PreferencesUtils.getString(BaseApplication.getInstance(), "myInfo", "");
        String languageJson = LanguageManager.getInstance().getCurrentLanguageJson();
        boolean isMDMStatusPass = PreferencesUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_MDM_STATUS_PASS, true);
        if (StringUtils.isBlank(accessToken)) {
            if (handler != null) {
                handler.sendEmptyMessage(LOGIN_FAIL);
            }
        } else if (StringUtils.isBlank(myInfo)) {
            new LoginUtils(activity, handler).getMyInfo();
        } else if (!isMDMStatusPass) {
            LanguageManager.getInstance().setLanguageLocal();
            new LoginUtils(activity, handler).startMDM();
            if (StringUtils.isBlank(languageJson)) {
                new LoginUtils(activity, handler).getServerSupportLanguage();
            }
        } else {
            if (handler != null) {
                handler.sendEmptyMessage(LOGIN_SUCCESS);
            }
        }
    }

    /**
     *
     */
    private void handMessage() {
        // TODO Auto-generated method stub
        loginUtilsHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        LoadingDialog.dimissDlg(loadingDlg);
                        if (handler != null) {
                            handler.sendEmptyMessage(LOGIN_SUCCESS);
                        }
                        break;
                    case LOGIN_FAIL:
                        LoadingDialog.dimissDlg(loadingDlg);
                        if (handler != null) {
                            handler.sendEmptyMessage(LOGIN_FAIL);
                        }
                        break;
                    default:
                        break;
                }
            }

        };
    }

    // 开始进行设备管理检查
    public void startMDM() {
        String userName = PreferencesUtils.getString(activity, "userRealName",
                "");
        String tanentId = BaseApplication.getInstance().getCurrentEnterprise().getId();
        String userCode = BaseApplication.getInstance().getUid();
        final MDM mdm = new MDM(activity, tanentId, userCode, userName);
        mdm.addOnMDMListener(new MDMListener() {
            @Override
            public void MDMStatusPass(int doubleValidation) {
                PreferencesByUserAndTanentUtils.putInt(BaseApplication.getInstance(), Constant.PREF_MNM_DOUBLE_VALIADATION, doubleValidation);
                PreferencesUtils.putBoolean(activity, Constant.PREF_MDM_STATUS_PASS, true);
                saveLoginInfo();
                loginUtilsHandler.sendEmptyMessage(LOGIN_SUCCESS);
                mdm.destroyOnMDMListener();
            }


            @Override
            public void MDMStatusNoPass() {
                clearLoginInfo();
                //当设备因为设备管理无法进入时，把记住选择企业选项情况，让用户重新选择企业进入
                PreferencesByUsersUtils.putString(activity, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
                loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
                mdm.destroyOnMDMListener();
            }

            @Override
            public void dimissExternalLoadingDlg() {
                if (loadingDlg != null && loadingDlg.isShowing()) {
                    loadingDlg.dismiss();
                }
            }
        });
        mdm.deviceCheck();
    }

    // 登录
    public void login(String userName, String password) {
        isLogin = true;
        if (NetUtils.isNetworkConnected(activity)) {
            loadingDlg.show();
            clearLoginInfo();
            apiServices.OauthSignIn(userName, password);
        } else {
            loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
        }

    }

    // 登录 存在bug，无法区分短信登录还是找回密码
    public void login(String userName, String password, boolean isSMSLogin) {
        this.isSMSLogin = isSMSLogin;
        login(userName, password);
    }

    /**
     * @param userName   用户名/手机号
     * @param password   密码/验证码
     * @param isSMSLogin 是否短信登录
     * @param mode       登录/找回密码 修复之前bug. mode = MODE_LOGIN登录 mode = MODE_FORGET_PASSWORD忘记密码
     */
    public void login(String userName, String password, boolean isSMSLogin, int mode) {
        this.isSMSLogin = isSMSLogin;
        this.mode = mode;
        login(userName, password);
    }

    /**
     * 获取基本信息
     */
    public void getMyInfo() {
        if (NetUtils.isNetworkConnected(activity)) {
            BaseModuleApiService baseModuleApiService = new BaseModuleApiService(activity);
            baseModuleApiService.setAPIInterface(new BaseModuleWebService());
            baseModuleApiService.getMyInfo();
        } else {
            clearLoginInfo();
            loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
        }

    }

    /**
     * 获取语音
     */
    public void getServerSupportLanguage() {
        LanguageManager.getInstance().getServerSupportLanguage(this);
    }

    @Override
    public void complete() {
    }

    /**
     * 清空登录信息
     */
    private void clearLoginInfo() {
        PreferencesUtils.putString(activity, "myInfo", "");
//        PreferencesUtils.putString(activity, "accessToken", "");
//        PreferencesUtils.putString(activity, "refreshToken", "");
//        PreferencesProvider.save(activity, "accessToken", "");
//        PreferencesProvider.save(activity, "refreshToken", "");
        // MMKV 替换 SharedPreferences
        MMKV kv = MMKV.mmkvWithID("InterProcessKV", MMKV.MULTI_PROCESS_MODE);
        kv.encode("accessToken", "");
        kv.encode("refreshToken", "");
        PreferencesUtils.putInt(activity, "keepAlive", 0);
        PreferencesUtils.putString(activity, "tokenType", "");
        PreferencesUtils.putInt(activity, "expiresIn", 0);
        BaseApplication.getInstance().setAccessToken("");
        BaseApplication.getInstance().setRefreshToken("");
    }

    private void saveLoginInfo() {
        if (getLoginResult != null) {
            String accessToken = getLoginResult.getAccessToken();
            String refreshToken = getLoginResult.getRefreshToken();
            int keepAlive = getLoginResult.getKeepAlive();
            String tokenType = getLoginResult.getTokenType();
            int expiresIn = getLoginResult.getExpiresIn();
            ((BaseApplication) activity.getApplicationContext())
                    .setAccessToken(accessToken);
            ((BaseApplication) activity.getApplicationContext())
                    .setRefreshToken(refreshToken);
//            PreferencesUtils.putString(activity, "accessToken", accessToken);
//            PreferencesUtils.putString(activity, "refreshToken", refreshToken);
//            PreferencesProvider.save(activity, "accessToken", accessToken);
//            PreferencesProvider.save(activity, "refreshToken", refreshToken);
            // MMKV 替换 SharedPreferences
            MMKV kv = MMKV.mmkvWithID("InterProcessKV", MMKV.MULTI_PROCESS_MODE);
            kv.encode("accessToken", accessToken);
            kv.encode("refreshToken", refreshToken);
            PreferencesUtils.putInt(activity, "keepAlive", keepAlive);
            PreferencesUtils.putString(activity, "tokenType", tokenType);
            PreferencesUtils.putInt(activity, "expiresIn", expiresIn);
            PreferencesUtils.putLong(activity, "token_get_time", System.currentTimeMillis());
            // 郑总token刷新失败分析日志
            if ("11487".equals(BaseApplication.getInstance().getUid())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    String processName = BaseApplication.getProcessName();
                    PVCollectModelCacheUtils.saveCollectModel("savePreferenceLoginInfo success", "---at---" + accessToken + "---rt---" + refreshToken + "---processName---" + processName);
                }
            }

        }
    }

    /**
     * 判断存储的企业id是否是有效的
     *
     * @param enterpriseList
     * @param selectLoginEnterpriseId
     */
    private boolean isEnterpriseIdValid(List<Enterprise> enterpriseList, String selectLoginEnterpriseId) {
        boolean isValid = false;
        for (int i = 0; i < enterpriseList.size(); i++) {
            if (enterpriseList.get(i).getId().equals(selectLoginEnterpriseId)) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

    /**
     * 显示选择租户Dialog
     *
     * @param enterpriseList
     */
    private void showSelectEnterpriseDlg(final List<Enterprise> enterpriseList) {
        final MyDialog myDialog = new MyDialog(activity, R.layout.login_dialog_select_tanent);
        final SwitchCompat switchView = myDialog.findViewById(R.id.auto_select_switch);
        switchView.setChecked(true);
        MaxHeightListView enterpriseListView = myDialog.findViewById(R.id.enterprise_list);
        enterpriseListView.setMaxHeight(DensityUtil.dip2px(activity, 180));
        enterpriseListView.setAdapter(new LoginSelectEnterpriseAdapter(activity, enterpriseList));
        enterpriseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isRemember = switchView.isChecked();
                String enterpriseId = enterpriseList.get(position).getId();
                PreferencesByUsersUtils.putString(activity, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, isRemember ? enterpriseId : "");
                PreferencesByUsersUtils.putString(activity, Constant.PREF_CURRENT_ENTERPRISE_ID, enterpriseId);
                myDialog.dismiss();
                BaseApplication.getInstance().initTanent();
                loadingDlg.show();
                startMDM();
                getServerSupportLanguage();
            }
        });
        myDialog.setCancelable(false);
        myDialog.show();

    }

    @Override
    public void returnOauthSignInSuccess(GetLoginResult getLoginResult) {
        // TODO Auto-generated method stub
        this.getLoginResult = getLoginResult;
        String accessToken = getLoginResult.getAccessToken();
        String refreshToken = getLoginResult.getRefreshToken();
        BaseApplication.getInstance().setAccessToken(accessToken);
        BaseApplication.getInstance().setRefreshToken(refreshToken);
        if (mode == LoginBySmsActivity.MODE_FORGET_PASSWORD) {
            handler.sendEmptyMessage(LOGIN_SUCCESS);
        } else {
            getMyInfo();
        }
    }

    @Override
    public void returnOauthSignInFail(String error, int errorCode, String headerLimitRemaining, String headerRetryAfter) {
        // TODO Auto-generated method stub
        try {
            if (errorCode == 400) {
                String code = JSONUtils.getString(error, "code", "");
                if (code.equals("1002") && !StringUtils.isBlank(headerLimitRemaining)) {
                    int limitRemaining = Integer.parseInt(headerLimitRemaining);
                    ToastUtils.show(activity, activity.getString(isSMSLogin ?
                            R.string.login_fail_sms_limit_remaining : R.string.login_fail_limit_remaining, limitRemaining));
                } else if (code.equals("1009") && !StringUtils.isBlank(headerRetryAfter)) {
                    int retryAfter = Integer.parseInt(headerRetryAfter);
                    if (retryAfter == 0) {
                        retryAfter++;
                    }
                    if (retryAfter > 59) {
                        retryAfter = new Double(Math.ceil(retryAfter * 1.0 / 60)).intValue();
                        ToastUtils.show(activity, activity.getString(R.string.login_fail_account_lock_by_min, retryAfter));
                    } else {
                        ToastUtils.show(activity, activity.getString(R.string.login_fail_account_lock_by_second, retryAfter));
                    }
                } else {
                    ToastUtils.show(activity, R.string.login_invaliad_account_or_pwd);
                }
            } else {
                WebServiceMiddleUtils.hand(activity, error, errorCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            WebServiceMiddleUtils.hand(activity, error, errorCode);
        }
        loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
    }

    private class BaseModuleWebService extends BaseModuleAPIInterfaceInstance {
        @Override
        public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
            // TODO Auto-generated method stub
            String myInfo = getMyInfoResult.getResponse();
            String name = getMyInfoResult.getName();
            PreferencesUtils.putBoolean(activity, Constant.PREF_MDM_STATUS_PASS, false);
            PreferencesUtils.putString(activity, "userRealName", name);
            PreferencesUtils.putString(activity, "userID", getMyInfoResult.getID());
            PreferencesUtils.putString(activity, "myInfo", myInfo);
            // 广水项目隐藏首次登录设置密码的操作
            if (getMyInfoResult.getDefaultEnterprise() != null && "919455".equals(getMyInfoResult.getDefaultEnterprise().getId())) {
                PreferencesUtils.putBoolean(activity, Constant.PREF_LOGIN_HAVE_SET_PASSWORD,
                        true);
            } else {
                PreferencesUtils.putBoolean(activity, Constant.PREF_LOGIN_HAVE_SET_PASSWORD,
                        getMyInfoResult.getHasPassord());
            }

            ((BaseApplication) activity.getApplicationContext())
                    .setUid(getMyInfoResult.getID());
            List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
            Enterprise defaultEnterprise = getMyInfoResult.getDefaultEnterprise();
            if (enterpriseList.size() == 0 && defaultEnterprise == null) {
                ToastUtils.show(activity, R.string.login_user_not_bound_enterprise);
                clearLoginInfo();
                loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
            } else {
                if (isLogin && enterpriseList.size() > 1) {
                    String selectLoginEnterpriseId = PreferencesByUsersUtils.getString(activity, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
                    //当用户没有指定登录的企业时或已指定登录企业但是此企业不存在时则弹出选择登录企业的页面
                    if (StringUtils.isBlank(selectLoginEnterpriseId) || !isEnterpriseIdValid(enterpriseList, selectLoginEnterpriseId)) {
                        LoadingDialog.dimissDlg(loadingDlg);
                        showSelectEnterpriseDlg(enterpriseList);
                        return;
                    }
                }
                ((BaseApplication) activity.getApplicationContext()).initTanent();
                startMDM();
                getServerSupportLanguage();
            }
        }


        @Override
        public void returnMyInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            clearLoginInfo();
            loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
            WebServiceMiddleUtils.hand(activity, error, errorCode);
        }
    }

}
