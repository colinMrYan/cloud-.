package com.inspur.emmcloud.basemodule.api;

import com.inspur.emmcloud.basemodule.bean.ApiRequestRecord;
import com.inspur.emmcloud.basemodule.bean.AppException;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.bean.GetLanguageResult;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.basemodule.bean.PVCollectModel;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModel;

import java.util.List;

/**
 * Created by chenmch on 2019/6/5.
 */

public class BaseModuleAPIInterfaceInstance implements BaseModuleAPIInterface {
    @Override
    public void returnUploadCollectSuccess(List<PVCollectModel> collectModelList) {

    }

    @Override
    public void returnUploadCollectFail(String error, int errorCode) {

    }

    @Override
    public void returnLanguageSuccess(GetLanguageResult getLanguageResult, String languageConfigVersion) {
    }

    @Override
    public void returnLanguageFail(String error, int errorCode) {
    }

    @Override
    public void returnAllConfigVersionSuccess(GetAllConfigVersionResult getAllConfigVersionResult) {

    }

    @Override
    public void returnAllConfigVersionFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult) {

    }

    @Override
    public void returnUploadPushInfoResultFail(String error, int errorCode) {

    }

    @Override
    public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {

    }

    @Override
    public void returnMyInfoFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadExceptionSuccess() {

    }

    @Override
    public void returnUploadExceptionSuccess(List<AppException> appExceptionList) {

    }

    @Override
    public void returnUploadExceptionFail(String error, int errorCode) {

    }

    @Override
    public void returnUploadApiRequestRecordSuccess(List<ApiRequestRecord> apiRequestRecordList) {

    }

    @Override
    public void returnUploadApiRequestRecordFail() {

    }

    @Override
    public void returnCheckCloudPluseConnectionSuccess(byte[] arg0, String url) {

    }

    @Override
    public void returnCheckCloudPluseConnectionError(String error, int responseCode, String url) {

    }

    @Override
    public void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel) {

    }

    @Override
    public void returnBadgeCountFail(String error, int errorCode) {

    }
}
