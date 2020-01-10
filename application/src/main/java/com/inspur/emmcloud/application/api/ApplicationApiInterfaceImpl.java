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
public class ApplicationApiInterfaceImpl implements ApplicationAPIInterface {
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
    public void returnSearchAppSuccess(GetSearchAppResult getAllAppResult) {

    }

    @Override
    public void returnSearchAppFail(String error, int errorCode) {

    }

    @Override
    public void returnAppInfoSuccess(App app) {

    }

    @Override
    public void returnAppInfoFail(String error, int errorCode) {

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
    public void returnVeriryApprovalPasswordSuccess(String password, String locationUrl) {

    }

    @Override
    public void returnVeriryApprovalPasswordFail(String error, int errorCode) {

    }

    @Override
    public void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel) {

    }

    @Override
    public void returnBadgeCountFail(String error, int errorCode) {

    }



    @Override
    public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {

    }

    @Override
    public void returnGetClientIdResultFail(String error, int errorCode) {

    }

    @Override
    public void returnUserAppsSuccess(GetAppGroupResult getAppGroupResult, String clientConfigMyAppVersion) {

    }

    @Override
    public void returnUserAppsFail(String error, int errorCode) {

    }

}
