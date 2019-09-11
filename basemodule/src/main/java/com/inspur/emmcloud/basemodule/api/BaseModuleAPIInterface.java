package com.inspur.emmcloud.basemodule.api;

import com.inspur.emmcloud.basemodule.bean.AppException;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.bean.GetLanguageResult;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.basemodule.bean.PVCollectModel;

import java.util.List;

/**
 * Created by chenmch on 2019/6/5.
 */

public interface BaseModuleAPIInterface {
    void returnUploadCollectSuccess(List<PVCollectModel> collectModelList);

    void returnUploadCollectFail(String error, int errorCode);

    void returnLanguageSuccess(GetLanguageResult getLanguageResult, String languageConfigVersion);

    void returnLanguageFail(String error, int errorCode);

    void returnAllConfigVersionSuccess(GetAllConfigVersionResult getAllConfigVersionResult);

    void returnAllConfigVersionFail(String error, int errorCode);

    void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult);

    void returnUploadPushInfoResultFail(String error, int errorCode);

    void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult);

    void returnMyInfoFail(String error, int errorCode);

    void returnUploadExceptionSuccess();

    void returnUploadExceptionSuccess(final List<AppException> appExceptionList);

    void returnUploadExceptionFail(String error, int errorCode);

}
