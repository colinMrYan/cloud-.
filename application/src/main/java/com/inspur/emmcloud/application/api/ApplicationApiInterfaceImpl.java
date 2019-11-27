package com.inspur.emmcloud.application.api;

import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.GetAddAppResult;
import com.inspur.emmcloud.application.bean.GetAllAppResult;
import com.inspur.emmcloud.application.bean.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.application.bean.GetRemoveAppResult;
import com.inspur.emmcloud.application.bean.GetSearchAppResult;
import com.inspur.emmcloud.application.bean.GetWebAppRealUrlResult;

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
}
