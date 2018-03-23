package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.LoginSelectEnterpriseAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.login.GetLoginResult;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.MDM.MDM;
import com.inspur.emmcloud.util.privates.MDM.MDMListener;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MaxHightListView;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import java.util.List;

/**
 * 登录公共类
 *
 * @author Administrator
 */
public class LoginUtils extends APIInterfaceInstance {
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_LANGUAGE_SUCCESS = 3;
    private  LoginAPIService apiServices;
    private Activity activity;
    private Handler handler;
    private boolean isSMSLogin = false;
    private Handler loginUtilsHandler;
    private LanguageUtils languageUtils;
    private GetLoginResult getLoginResult;
    private LoadingDialog loadingDialog;
    private boolean isLogin = false;  //标记是登录界面(包括手机验证码登录界面)调用

    public LoginUtils(Activity activity, Handler handler) {
        this.handler = handler;
        this.activity = activity;
        loadingDialog = new LoadingDialog(activity);
        apiServices = new LoginAPIService(activity);
        apiServices.setAPIInterface(LoginUtils.this);
        handMessage();
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
                    case GET_LANGUAGE_SUCCESS:
                        // handler.sendEmptyMessage(LOGIN_SUCCESS);
                        startMDM();
                        break;
                    case LOGIN_SUCCESS:
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                        handler.sendEmptyMessage(LOGIN_SUCCESS);
                        break;
                    case LOGIN_FAIL:
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                        handler.sendEmptyMessage(LOGIN_FAIL);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    // 开始进行设备管理检查
    public void startMDM() {
        // TODO Auto-generated method stub
        String userName = PreferencesUtils.getString(activity, "userRealName",
                "");
        String tanentId = ((MyApplication) activity.getApplicationContext()).getCurrentEnterprise().getId();
        String userCode = ((MyApplication) activity.getApplicationContext())
                .getUid();
        final MDM mdm = new MDM(activity, tanentId, userCode, userName);
        mdm.addOnMDMListener(new MDMListener() {

            @Override
            public void MDMStatusPass() {
                // TODO Auto-generated method stub
                PreferencesUtils.putBoolean(activity, "isMDMStatusPass", true);
                saveLoginInfo();
                loginUtilsHandler.sendEmptyMessage(LOGIN_SUCCESS);
                mdm.destroyOnMDMListener();
            }

            @Override
            public void MDMStatusNoPass() {
                // TODO Auto-generated method stub
                clearLoginInfo();
                //当设备因为设备管理无法进入时，把记住选择企业选项情况，让用户重新选择企业进入
                PreferencesByUsersUtils.putString(activity, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID,"");
                loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
                mdm.destroyOnMDMListener();
            }

            @Override
            public void dimissExternalLoadingDlg() {
                // TODO Auto-generated method stub
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        });
        mdm.deviceCheck();
    }

    // 登录
    public void login(String userName, String password) {
        isLogin = true;
        if (NetUtils.isNetworkConnected(activity)) {
            loadingDialog.show();
            clearLoginInfo();
            apiServices.OauthSignin(userName, password);
        } else {
            loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
        }

    }

    // 登录
    public void login(String userName, String password, boolean isSMSLogin) {
        this.isSMSLogin = isSMSLogin;
        login(userName, password);
    }

    /**
     * 获取基本信息
     */
    public void getMyInfo() {
        if (NetUtils.isNetworkConnected(activity)) {
            apiServices.getMyInfo();
        } else {
            clearLoginInfo();
            loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
        }

    }

    /**
     * 获取语音
     */
    public void getServerSupportLanguage() {
        if (NetUtils.isNetworkConnected(activity, false)) {
            languageUtils = new LanguageUtils(activity, loginUtilsHandler);
            languageUtils.getServerSupportLanguage();
        } else {
            loginUtilsHandler.sendEmptyMessage(GET_LANGUAGE_SUCCESS);
        }

    }

    /**
     * 清空登录信息
     */
    private void clearLoginInfo() {
        PreferencesUtils.putString(activity, "accessToken", "");
        PreferencesUtils.putString(activity, "refreshToken", "");
        PreferencesUtils.putInt(activity, "keepAlive", 0);
        PreferencesUtils.putString(activity, "tokenType", "");
        PreferencesUtils.putInt(activity, "expiresIn", 0);
    }

    private void saveLoginInfo() {
        if (getLoginResult != null) {
            String accessToken = getLoginResult.getAccessToken();
            String refreshToken = getLoginResult.getRefreshToken();
            int keepAlive = getLoginResult.getKeepAlive();
            String tokenType = getLoginResult.getTokenType();
            int expiresIn = getLoginResult.getExpiresIn();
            ((MyApplication) activity.getApplicationContext())
                    .setAccessToken(accessToken);
            ((MyApplication) activity.getApplicationContext())
                    .setRefreshToken(refreshToken);
            PreferencesUtils.putString(activity, "accessToken", accessToken);
            PreferencesUtils.putString(activity, "refreshToken", refreshToken);
            PreferencesUtils.putInt(activity, "keepAlive", keepAlive);
            PreferencesUtils.putString(activity, "tokenType", tokenType);
            PreferencesUtils.putInt(activity, "expiresIn", expiresIn);
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
     * @param defaultEnterprise
     */
    private void showSelectEnterpriseDlg(final List<Enterprise> enterpriseList, Enterprise defaultEnterprise) {
        final MyDialog myDialog = new MyDialog(activity, R.layout.dialog_login_select_tanent);
        final SwitchView switchView = (SwitchView) myDialog.findViewById(R.id.auto_select_switch);
        switchView.setOpened(true);
        MaxHightListView enterpriseListView = (MaxHightListView) myDialog.findViewById(R.id.enterprise_list);
        enterpriseListView.setMaxHeight(DensityUtil.dip2px(activity, 180));
        enterpriseListView.setAdapter(new LoginSelectEnterpriseAdapter(activity, enterpriseList, defaultEnterprise));
        enterpriseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isRemember = switchView.isOpened();
                String enterpriseId = enterpriseList.get(position).getId();
                PreferencesByUsersUtils.putString(activity, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, isRemember ? enterpriseId : "");
                PreferencesByUsersUtils.putString(activity, Constant.PREF_CURRENT_ENTERPRISE_ID, enterpriseId);
                myDialog.dismiss();
                MyApplication.getInstance().initTanent();
                loadingDialog.show();
                getServerSupportLanguage();
            }
        });
        myDialog.setCancelable(false);
        myDialog.show();

    }

    @Override
    public void returnOauthSigninSuccess(GetLoginResult getLoginResult) {
        // TODO Auto-generated method stub
        this.getLoginResult = getLoginResult;
        String accessToken = getLoginResult.getAccessToken();
        String refreshToken = getLoginResult.getRefreshToken();
        ((MyApplication) activity.getApplicationContext())
                .setAccessToken(accessToken);
        ((MyApplication) activity.getApplicationContext())
                .setRefreshToken(refreshToken);
        getMyInfo();
    }

    @Override
    public void returnOauthSigninFail(String error, int errorCode) {
        // TODO Auto-generated method stub
        if (errorCode == 400) {
            ToastUtils.show(activity, isSMSLogin ? R.string.code_verification_failure : R.string.invaliad_account_or_pwd);
        } else {
            WebServiceMiddleUtils.hand(activity, error, errorCode);
        }
        loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
    }

    @Override
    public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
        // TODO Auto-generated method stub
        String myInfo = getMyInfoResult.getResponse();
        String name = getMyInfoResult.getName();
        PreferencesUtils.putBoolean(activity, "isMDMStatusPass", false);
        PreferencesUtils.putString(activity, "userRealName", name);
        PreferencesUtils.putString(activity, "userID", getMyInfoResult.getID());
        PreferencesUtils.putString(activity, "myInfo", myInfo);
        PreferencesUtils.putBoolean(activity, "hasPassword",
                getMyInfoResult.getHasPassord());
        ((MyApplication) activity.getApplicationContext())
                .setUid(getMyInfoResult.getID());
        List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
        Enterprise defaultEnterprise = getMyInfoResult.getDefaultEnterprise();
        if (isLogin && enterpriseList.size() > 1) {
            String selectLoginEnterpriseId = PreferencesByUsersUtils.getString(activity, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
            //当用户没有指定登录的企业时或已指定登录企业但是此企业不存在时则弹出选择登录企业的页面
            if (StringUtils.isBlank(selectLoginEnterpriseId) || !isEnterpriseIdValid(enterpriseList, selectLoginEnterpriseId)) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                showSelectEnterpriseDlg(enterpriseList, defaultEnterprise);
                return;
            }
        }
        ((MyApplication) activity.getApplicationContext()).initTanent();
        getServerSupportLanguage();
    }


    @Override
    public void returnMyInfoFail(String error, int errorCode) {
        // TODO Auto-generated method stub
        clearLoginInfo();
        loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
        WebServiceMiddleUtils.hand(activity, error, errorCode);
    }

}
