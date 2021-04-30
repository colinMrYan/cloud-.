package com.inspur.emmcloud.setting.api;

import com.inspur.emmcloud.setting.bean.GetBindingDeviceResult;
import com.inspur.emmcloud.setting.bean.GetCardPackageResult;
import com.inspur.emmcloud.setting.bean.GetDeviceLogResult;
import com.inspur.emmcloud.setting.bean.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.setting.bean.GetFaceSettingResult;
import com.inspur.emmcloud.setting.bean.GetMDMStateResult;
import com.inspur.emmcloud.setting.bean.GetUploadMyHeadResult;
import com.inspur.emmcloud.setting.bean.GetUserCardMenusResult;
import com.inspur.emmcloud.setting.bean.GetUserHeadUploadResult;
import com.inspur.emmcloud.setting.bean.LogOffResult;
import com.inspur.emmcloud.setting.bean.SettingGetBoolenResult;
import com.inspur.emmcloud.setting.bean.UserProfileInfoBean;

/**
 * Created by libaochao on 2019/12/25.
 */

public interface SettingAPIInterface {
    void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult, String filePath);

    void returnUploadMyHeadFail(String error, int errorCode);

    void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean);

    void returnUserProfileConfigFail(String error, int errorCode);

    void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult);

    void returnBindingDeviceListFail(String error, int errorCode);

    void returnUnBindDeviceSuccess();

    void returnUnBindDeviceFail(String error, int errorCode);

    void returnDeviceLogListSuccess(GetDeviceLogResult getDeviceLogResult);

    void returnDeviceLogListFail(String error, int errorCode);

    void returnFaceSettingSuccess(GetFaceSettingResult getFaceSettingResult);

    void returnFaceSettingFail(String error, int errorCode);

    void returnFaceVerifySuccess(GetFaceSettingResult getFaceSettingResult);

    void returnFaceVerifyFail(String error, int errorCode);

    void returnExperienceUpgradeFlagSuccess(GetExperienceUpgradeFlagResult getExperienceUpgradeFlagResult);

    void returnExperienceUpgradeFlagFail(String error, int errorCode);

    void returnUpdateExperienceUpgradeFlagSuccess();

    void returnUpdateExperienceUpgradeFlagFail(String error, int errorCode);

    void returnUserCardMenusSuccess(GetUserCardMenusResult getUserCardMenusResult);

    void returnUserCardMenusFail(String error, int errorCode);

    void returnCardPackageListSuccess(GetCardPackageResult getCardPackageResult);

    void returnCardPackageListFail(String error, int errorCode);

    void returnUserHeadUploadSuccess(GetUserHeadUploadResult getUserHeadUploadResult);

    void returnUserHeadUploadFail(String error, int errorCode);

    void returnModifyUserInfoSucces(SettingGetBoolenResult getBoolenResult);

    void returnModifyUserInfoFail(String error, int errorCode);

    void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult);

    void returnMDMStateFail(String error, int errorCode);

    void returnLogOffSuccess(LogOffResult logOffResult);

    void returnLogOffFail(String error, int errorCode);
}
