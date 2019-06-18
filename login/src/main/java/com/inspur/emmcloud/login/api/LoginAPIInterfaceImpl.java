package com.inspur.emmcloud.login.api;

import com.inspur.emmcloud.login.bean.GetDeviceCheckResult;
import com.inspur.emmcloud.login.bean.GetLoginResult;
import com.inspur.emmcloud.login.bean.GetMDMStateResult;
import com.inspur.emmcloud.login.bean.GetRegisterCheckResult;
import com.inspur.emmcloud.login.bean.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.login.bean.UploadMDMInfoResult;

/**
 * Created by chenmch on 2019/6/7.
 */

public class LoginAPIInterfaceImpl implements LoginAPIInterface {
    @Override
    public void returnOauthSignInSuccess(GetLoginResult getLoginResult) {

    }

    @Override
    public void returnOauthSignInFail(String error, int errorCode, String headerLimitRemaining, String headerRetryAfter) {

    }

    @Override
    public void returnRefreshTokenFail(String error, int errorCode) {

    }

    @Override
    public void returnLoginSMSCaptchaSuccess() {

    }

    @Override
    public void returnLoginSMSCaptchaFail(String error, int errorCode) {

    }

    @Override
    public void returnReisterSMSCheckSuccess(GetRegisterCheckResult getRegisterResult) {

    }

    @Override
    public void returnReisterSMSCheckFail(String error, int errorCode) {

    }

    @Override
    public void returnResetPasswordSuccess() {

    }

    @Override
    public void returnResetPasswordFail(String error, int errorCode) {

    }

    @Override
    public void returnFaceLoginGSSuccess() {

    }

    @Override
    public void returnFaceLoginGSFail(String error, int errorCode) {

    }

    @Override
    public void returnRefreshTokenSuccess(GetLoginResult getLoginResult) {

    }

    @Override
    public void returnModifyPasswordSuccess() {

    }

    @Override
    public void returnModifyPasswordFail(String error, int errorCode) {

    }
    @Override
    public void returnLoginDesktopCloudPlusSuccess(LoginDesktopCloudPlusBean loginDesktopCloudPlusBean) {

    }

    @Override
    public void returnLoginDesktopCloudPlusFail(String error, int errorCode) {

    }

    @Override
    public void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult) {

    }

    @Override
    public void returnMDMStateFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadMDMInfoSuccess(UploadMDMInfoResult uploadMDMInfoResult) {

    }

    @Override
    public void returnUploadMDMInfoFail() {

    }

    @Override
    public void returnDeviceCheckSuccess(GetDeviceCheckResult getDeviceCheckResult) {

    }

    @Override
    public void returnDeviceCheckFail(String error, int errorCode) {

    }
}
