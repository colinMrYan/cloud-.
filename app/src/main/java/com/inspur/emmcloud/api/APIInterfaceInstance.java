package com.inspur.emmcloud.api;


import com.inspur.emmcloud.basemodule.bean.AppException;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppRedirectResult;
import com.inspur.emmcloud.bean.appcenter.GetAddAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAllAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAppGroupResult;
import com.inspur.emmcloud.bean.appcenter.GetClientIdRsult;
import com.inspur.emmcloud.bean.appcenter.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.appcenter.GetRemoveAppResult;
import com.inspur.emmcloud.bean.appcenter.GetSearchAppResult;
import com.inspur.emmcloud.bean.appcenter.GetWebAppRealUrlResult;
import com.inspur.emmcloud.bean.mine.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;
import com.inspur.emmcloud.bean.system.navibar.NaviBarModel;

import java.util.List;

public class APIInterfaceInstance implements APIInterface {
    @Override
    public void returnAllAppsSuccess(GetAllAppResult getAllAppResult) {

    }

    @Override
    public void returnAllAppsFail(String error, int errorCode) {

    }

    @Override
    public void returnAddAppSuccess(GetAddAppResult getAddAppResult) {

    }

    @Override
    public void returnAddAppFail(String error, int errorCode) {

    }

    @Override
    public void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult) {

    }

    @Override
    public void returnRemoveAppFail(String error, int errorCode) {

    }

    @Override
    public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck) {

    }

    @Override
    public void returnUpgradeFail(String error, boolean isManualCheck, int errorCode) {

    }

    @Override
    public void returnSearchAppSuccess(GetSearchAppResult getAllAppResult) {

    }

    @Override
    public void returnSearchAppFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult, String filePath) {

    }

    @Override
    public void returnUploadMyHeadFail(String error, int errorCode) {

    }

    @Override
    public void returnModifyUserInfoSucces(GetBoolenResult getBoolenResult) {

    }

    @Override
    public void returnModifyUserInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadExceptionSuccess(List<AppException> appExceptionList) {

    }

    @Override
    public void returnUploadExceptionFail(String error, int errorCode) {

    }

    @Override
    public void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion) {

    }

    @Override
    public void returnUserAppsFail(String error, int errorCode) {

    }

    @Override
    public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {

    }

    @Override
    public void returnGetClientIdResultFail(String error, int errorCode) {

    }

    @Override
    public void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult) {

    }

    @Override
    public void returnGetAppAuthCodeResultFail(String error, int errorCode) {

    }

    @Override
    public void returnVeriryApprovalPasswordSuccess(String password) {

    }

    @Override
    public void returnVeriryApprovalPasswordFail(String error, int errorCode) {

    }

    @Override
    public void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean) {

    }

    @Override
    public void returnUserProfileConfigFail(String error, int errorCode) {

    }

    @Override
    public void returnSplashPageInfoSuccess(SplashPageBean splashPageBean) {

    }

    @Override
    public void returnSplashPageInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnAppInfoSuccess(App app) {

    }

    @Override
    public void returnAppInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult) {

    }

    @Override
    public void returnAppConfigFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadPositionSuccess() {

    }

    @Override
    public void returnWebAppRealUrlSuccess(GetWebAppRealUrlResult getWebAppRealUrlResult) {

    }

    @Override
    public void returnWebAppRealUrlFail() {

    }

    @Override
    public void returnSaveConfigSuccess() {

    }

    @Override
    public void returnSaveConfigFail() {

    }

    @Override
    public void returnRecommendAppWidgetListSuccess(GetRecommendAppWidgetListResult getRecommendAppWidgetListResult) {

    }

    @Override
    public void returnRecommendAppWidgetListFail(String error, int errorCode) {

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
    public void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel) {

    }

    @Override
    public void returnBadgeCountFail(String error, int errorCode) {

    }

    @Override
    public void returnCheckCloudPluseConnectionSuccess(byte[] arg0, String url) {

    }

    @Override
    public void returnCheckCloudPluseConnectionError(String error, int responseCode, String url) {

    }


    @Override
    public void returnNaviBarModelSuccess(NaviBarModel naviBarModel) {

    }

    @Override
    public void returnNaviBarModelFail(String error, int errorCode) {

    }
}
