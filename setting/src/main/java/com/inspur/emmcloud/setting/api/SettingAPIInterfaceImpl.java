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

public class SettingAPIInterfaceImpl implements SettingAPIInterface {
    @Override
    public void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult, String filePath) {

    }

    @Override
    public void returnUploadMyHeadFail(String error, int errorCode) {

    }

    @Override
    public void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean) {

    }

    @Override
    public void returnUserProfileConfigFail(String error, int errorCode) {

    }

    @Override
    public void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult) {

    }

    @Override
    public void returnBindingDeviceListFail(String error, int errorCode) {

    }

    @Override
    public void returnUnBindDeviceSuccess() {

    }

    @Override
    public void returnUnBindDeviceFail(String error, int errorCode) {

    }

    @Override
    public void returnDeviceLogListSuccess(GetDeviceLogResult getDeviceLogResult) {

    }

    @Override
    public void returnDeviceLogListFail(String error, int errorCode) {

    }

    @Override
    public void returnFaceSettingSuccess(GetFaceSettingResult getFaceSettingResult) {

    }

    @Override
    public void returnFaceSettingFail(String error, int errorCode) {

    }

    @Override
    public void returnFaceVerifySuccess(GetFaceSettingResult getFaceSettingResult) {

    }

    @Override
    public void returnFaceVerifyFail(String error, int errorCode) {

    }

    @Override
    public void returnExperienceUpgradeFlagSuccess(GetExperienceUpgradeFlagResult getExperienceUpgradeFlagResult) {

    }

    @Override
    public void returnExperienceUpgradeFlagFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateExperienceUpgradeFlagSuccess() {

    }

    @Override
    public void returnUpdateExperienceUpgradeFlagFail(String error, int errorCode) {

    }

    @Override
    public void returnUserCardMenusSuccess(GetUserCardMenusResult getUserCardMenusResult) {

    }

    @Override
    public void returnUserCardMenusFail(String error, int errorCode) {

    }

    @Override
    public void returnCardPackageListSuccess(GetCardPackageResult getCardPackageResult) {

    }

    @Override
    public void returnCardPackageListFail(String error, int errorCode) {

    }

    @Override
    public void returnUserHeadUploadSuccess(GetUserHeadUploadResult getUserHeadUploadResult) {

    }

    @Override
    public void returnUserHeadUploadFail(String error, int errorCode) {

    }

    @Override
    public void returnModifyUserInfoSucces(SettingGetBoolenResult getBoolenResult) {

    }

    @Override
    public void returnModifyUserInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnMDMStateSuccess(GetMDMStateResult getMDMStateResult) {

    }

    @Override
    public void returnMDMStateFail(String error, int errorCode) {

    }

    @Override
    public void returnLogOffSuccess(LogOffResult logOffResult) {

    }

    @Override
    public void returnLogOffFail(String error, int errorCode) {

    }

}
