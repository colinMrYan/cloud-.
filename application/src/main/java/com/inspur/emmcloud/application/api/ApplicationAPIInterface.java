package com.inspur.emmcloud.application.api;

import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.GetAddAppResult;
import com.inspur.emmcloud.application.bean.GetAllAppResult;
import com.inspur.emmcloud.application.bean.GetAppGroupResult;
import com.inspur.emmcloud.application.bean.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.application.bean.GetRemoveAppResult;
import com.inspur.emmcloud.application.bean.GetSearchAppResult;
import com.inspur.emmcloud.application.bean.GetWebAppRealUrlResult;
import com.inspur.emmcloud.basemodule.application.GetClientIdRsult;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModel;

/**
 * Created by: yufuchang
 * Date: 2019/11/27
 */
public interface ApplicationAPIInterface {
    void returnAllAppsSuccess(GetAllAppResult getAllAppResult);

    void returnAllAppsFail(String error, int errorCode);

    void returnAddAppSuccess(GetAddAppResult getAddAppResult);

    void returnAddAppFail(String error, int errorCode);

    void returnRemoveAppSuccess(GetRemoveAppResult getRemoveAppResult);

    void returnRemoveAppFail(String error, int errorCode);

    void returnSearchAppSuccess(GetSearchAppResult getAllAppResult);

    void returnSearchAppFail(String error, int errorCode);

    void returnAppInfoSuccess(App app);

    void returnAppInfoFail(String error, int errorCode);

    void returnWebAppRealUrlSuccess(GetWebAppRealUrlResult getWebAppRealUrlResult);

    void returnWebAppRealUrlFail();

    void returnSaveConfigSuccess();

    void returnSaveConfigFail();

    void returnRecommendAppWidgetListSuccess(GetRecommendAppWidgetListResult getRecommendAppWidgetListResult);

    void returnRecommendAppWidgetListFail(String error, int errorCode);

    void returnVeriryApprovalPasswordSuccess(String password, String locationUrl);

    void returnVeriryApprovalPasswordFail(String error, int errorCode);

    void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel);

    void returnBadgeCountFail(String error, int errorCode);




    void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult);

    void returnGetClientIdResultFail(String error, int errorCode);



    void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion);

    void returnUserAppsFail(String error, int errorCode);


}
