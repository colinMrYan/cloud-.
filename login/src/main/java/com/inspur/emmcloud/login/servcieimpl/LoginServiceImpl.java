package com.inspur.emmcloud.login.servcieimpl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;
import com.inspur.emmcloud.login.bean.UploadMDMInfoResult;
import com.inspur.emmcloud.login.ui.LoginActivity;
import com.inspur.emmcloud.login.util.LoginUtils;
import com.inspur.emmcloud.login.util.MDM.MDM;
import com.inspur.emmcloud.login.util.OauthUtils;

/**
 * Created by chenmch on 2019/6/3.
 */

public class LoginServiceImpl extends LoginAPIInterfaceImpl implements LoginService {
    @Override
    public void logout(Context context) {
        OauthUtils.getInstance().cancelToken();
        PreferencesUtils.putString(context, "accessToken", "");
        PreferencesUtils.putString(context, "refreshToken", "");
        PreferencesUtils.putString(context, "userRealName", "");
        PreferencesUtils.putString(context, "userID", "");
        BaseApplication.getInstance().setAccessToken("");
        BaseApplication.getInstance().setRefreshToken("");
        BaseApplication.getInstance().setUid("");
        BaseApplication.getInstance().setTanent("");
        BaseApplication.getInstance().setCurrentEnterprise(null);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void refreshToken(OauthCallBack callBack, long requestTime) {
        OauthUtils.getInstance().refreshToken(callBack, requestTime);
    }

    @Override
    public void MDMCheck(Activity context, String tanent, String userCode, String userName, String state, boolean impActivity) {
        MDM mdm = new MDM(context, tanent, userCode, userName, state);
        mdm.setImpActivity(impActivity);
        mdm.handCheckResult();
    }

    @Override
    public void autoLogin(Activity activity, Handler handler) {
        new LoginUtils(activity, handler).autoLogin();
    }

    @Override
    public void setMDMStatusNoPass() {
        new MDM().getMDMListener().MDMStatusNoPass();
    }

    @Override
    public void uploadMDMInfo() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            LoginAPIService appAPIService = new LoginAPIService(BaseApplication.getInstance());
            appAPIService.setAPIInterface(this);
            appAPIService.uploadMDMInfo();
        }
    }

    @Override
    public void returnUploadMDMInfoSuccess(UploadMDMInfoResult uploadMDMInfoResult) {
        if (uploadMDMInfoResult.getDoubleValidation() != -1) {
            PreferencesByUserAndTanentUtils.putInt(BaseApplication.getInstance(), Constant.PREF_MNM_DOUBLE_VALIADATION, uploadMDMInfoResult.getDoubleValidation());
        }

    }

}
