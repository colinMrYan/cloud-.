package com.inspur.emmcloud.basemodule.api;

import com.inspur.emmcloud.basemodule.bean.ApiRequestRecord;
import com.inspur.emmcloud.basemodule.bean.AppException;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.bean.GetLanguageResult;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.basemodule.bean.PVCollectModel;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModel;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;

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

    void returnUploadApiRequestRecordSuccess(List<ApiRequestRecord> apiRequestRecordList);

    void returnUploadApiRequestRecordFail();

    void returnCheckCloudPluseConnectionSuccess(byte[] arg0, String url);

    void returnCheckCloudPluseConnectionError(String error, int responseCode, String url);

    void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel);

    void returnBadgeCountFail(String error, int errorCode);

    void returnCallbackAfterFileUploadSuccess(VolumeFile volumeFile);

    void returnCallbackAfterFileUploadFail(String error, int errorCode);

}
