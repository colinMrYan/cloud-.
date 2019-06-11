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

public interface APIInterface {

    void returnAllAppsSuccess(GetAllAppResult getAllAppResult);

    void returnAllAppsFail(String error, int errorCode);


    void returnAddAppSuccess(GetAddAppResult getAddAppResult);

    void returnAddAppFail(String error, int errorCode);

    void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult);

    void returnRemoveAppFail(String error, int errorCode);

    void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck);

    void returnUpgradeFail(String error, boolean isManualCheck, int errorCode);

    void returnSearchAppSuccess(GetSearchAppResult getAllAppResult);

    void returnSearchAppFail(String error, int errorCode);





    void returnUploadMyHeadSuccess(GetUploadMyHeadResult getUploadMyInfoResult, String filePath);

    void returnUploadMyHeadFail(String error, int errorCode);

    void returnModifyUserInfoSucces(GetBoolenResult getBoolenResult);

    void returnModifyUserInfoFail(String error, int errorCode);

    void returnUploadExceptionSuccess(final List<AppException> appExceptionList);

    void returnUploadExceptionFail(String error, int errorCode);


    void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion);

    void returnUserAppsFail(String error, int errorCode);

    void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult);

    void returnGetClientIdResultFail(String error, int errorCode);

    void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult);

    void returnGetAppAuthCodeResultFail(String error, int errorCode);


    void returnVeriryApprovalPasswordSuccess(String password);

    void returnVeriryApprovalPasswordFail(String error, int errorCode);


    void returnUserProfileConfigSuccess(UserProfileInfoBean userProfileInfoBean);

    void returnUserProfileConfigFail(String error, int errorCode);




    void returnSplashPageInfoSuccess(SplashPageBean splashPageBean);

    void returnSplashPageInfoFail(String error, int errorCode);




    void returnAppInfoSuccess(App app);

    void returnAppInfoFail(String error, int errorCode);

    void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult);

    void returnAppConfigFail(String error, int errorCode);

    void returnUploadPositionSuccess();

    void returnWebAppRealUrlSuccess(GetWebAppRealUrlResult getWebAppRealUrlResult);

    void returnWebAppRealUrlFail();

    void returnSaveConfigSuccess();

    void returnSaveConfigFail();


    void returnRecommendAppWidgetListSuccess(GetRecommendAppWidgetListResult getRecommendAppWidgetListResult);

    void returnRecommendAppWidgetListFail(String error, int errorCode);


    void returnExperienceUpgradeFlagSuccess(GetExperienceUpgradeFlagResult getExperienceUpgradeFlagResult);

    void returnExperienceUpgradeFlagFail(String error, int errorCode);

    void returnUpdateExperienceUpgradeFlagSuccess();

    void returnUpdateExperienceUpgradeFlagFail(String error, int errorCode);

    void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel);

    void returnBadgeCountFail(String error, int errorCode);


    void returnCheckCloudPluseConnectionSuccess(byte[] arg0, String url);

    void returnCheckCloudPluseConnectionError(String error, int responseCode, String url);



    void returnNaviBarModelSuccess(NaviBarModel naviBarModel);

    void returnNaviBarModelFail(String error, int errorCode);


}
