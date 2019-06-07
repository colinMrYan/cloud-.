package com.inspur.emmcloud.login.api;

import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.login.bean.GetDeviceCheckResult;
import com.inspur.emmcloud.login.bean.GetLoginResult;
import com.inspur.emmcloud.login.bean.GetMDMStateResult;
import com.inspur.emmcloud.login.bean.GetRegisterCheckResult;
import com.inspur.emmcloud.login.bean.GetRegisterResult;
import com.inspur.emmcloud.login.bean.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.login.bean.UploadMDMInfoResult;

/**
 * Created by chenmch on 2019/6/7.
 */

public interface LoginAPIInterface {
    void returnOauthSignInSuccess(GetLoginResult getLoginResult);

    void returnOauthSignInFail(String error, int errorCode, String headerLimitRemaining, String headerRetryAfter);

    void returnRefreshTokenFail(String error, int errorCode);

    void returnRefreshTokenSuccess(GetLoginResult getLoginResult);

    void returnLoginSMSCaptchaSuccess();

    void returnLoginSMSCaptchaFail(String error, int errorCode);

    void returnRegisterSMSSuccess(GetRegisterResult getRegisterResult);

    void returnRegisterSMSFail(String error, int errorCode);

    void returnReisterSMSCheckSuccess(GetRegisterCheckResult getRegisterResult);

    void returnReisterSMSCheckFail(String error, int errorCode);

    void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult);

    void returnMyInfoFail(String error, int errorCode);

    void returnResetPasswordSuccess();

    void returnResetPasswordFail(String error, int errorCode);

    void returnFaceLoginGSSuccess();

    void returnFaceLoginGSFail(String error, int errorCode);

    void returnModifyPasswordSuccess();

    void returnModifyPasswordFail(String error, int errorCode);

    void returnLoginDesktopCloudPlusSuccess(LoginDesktopCloudPlusBean loginDesktopCloudPlusBean);

    void returnLoginDesktopCloudPlusFail(String error, int errorCode);

    void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult);

    void returnMDMStateFail(String error, int errorCode);

    void returnUploadMDMInfoSuccess(UploadMDMInfoResult uploadMDMInfoResult);

    void returnUploadMDMInfoFail();

    void returnDeviceCheckSuccess(GetDeviceCheckResult getDeviceCheckResult);

    void returnDeviceCheckFail(String error, int errorCode);
}
