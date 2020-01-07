package com.inspur.emmcloud.volume.api;

import com.inspur.emmcloud.componentservice.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.componentservice.volume.Volume;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.volume.bean.GetReturnMoveOrCopyErrorResult;
import com.inspur.emmcloud.volume.bean.GetVolumeFileListResult;
import com.inspur.emmcloud.volume.bean.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.volume.bean.GetVolumeGroupResult;
import com.inspur.emmcloud.volume.bean.GetVolumeListResult;
import com.inspur.emmcloud.volume.bean.GetVolumeResultWithPermissionResult;
import com.inspur.emmcloud.volume.bean.VolumeDetail;

import java.util.List;

public interface VolumeAPIInterface {

    void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult,
                                            String fileLocalPath, VolumeFile mockVolumeFile, int transferObserverId);
    void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath);

    void returnVolumeMemAddSuccess(List<String> uidList);
    void returnVolumeMemAddFail(String error, int errorCode);

    void returnGroupMemAddSuccess(List<String> uidList);
    void returnGroupMemAddFail(String error, int errorCode);

    void returnGroupMemDelSuccess(List<String> uidList);
    void returnGroupMemDelFail(String error, int errorCode);

    void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult);
    void returnVolumeListFail(String error, int errorCode);

    void returnCreateShareVolumeSuccess(Volume volume);
    void returnCreateShareVolumeFail(String error, int errorCode);

    void returnUpdateShareVolumeNameSuccess(Volume volume, String name);
    void returnUpdateShareVolumeNameFail(String error, int errorCode);

    void returnRemoveShareVolumeSuccess(Volume volume);
    void returnRemoveShareVolumeFail(String error, int errorCode);

    void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult);
    void returnVolumeFileListFail(String error, int errorCode);

    void returnCreateForderSuccess(VolumeFile volumeFile);
    void returnCreateForderFail(String error, int errorCode);

    void returnVolumeFileDeleteSuccess(List<VolumeFile> deleteVolumeFileList);
    void returnVolumeFileDeleteFail(String error, int errorCode);

    void returnVolumeFileRenameSuccess(VolumeFile oldVolumeFile, String fileNewName);
    void returnVolumeFileRenameFail(String error, int errorCode);

    void returnMoveFileSuccess(List<VolumeFile> movedVolumeFileList);
    void returnMoveFileFail(String error, int errorCode);

    void returnCopyFileSuccess();
    void returnCopyFileFail(String error, int errorCode);

    void returnCopyFileBetweenVolumeSuccess();
    void returnCopyFileBetweenVolumeFail(String error, int errorCode, VolumeFile volumeFile);

    void returnMoveOrCopyFileBetweenVolumeSuccess(String operation);
    void returnMoveOrCopyFileBetweenVolumeFail(GetReturnMoveOrCopyErrorResult errorResult, int errorCode, String srcVolumeFilePath, String operation, List<VolumeFile> volumeFileList);

    void returnVolumeDetailSuccess(VolumeDetail volumeDetail);
    void returnVolumeDetailFail(String error, int errorCode);

    void returnVolumeMemDelSuccess(List<String> uidList);
    void returnVolumeMemDelFail(String error, int errorCode);

    void returnUpdateGroupNameSuccess(String name);
    void returnUpdateGroupNameFail(String error, int errorCode);

    void returnVolumeGroupContainMeSuccess(GetVolumeGroupResult getVolumeGroupResult);
    void returnVolumeGroupContainMeFail(String error, int errorCode);

    void returnVolumeGroupSuccess(GetVolumeResultWithPermissionResult getVolumeResultWithPermissionResult);
    void returnVolumeGroupFail(String error, int errorCode);

    void returnUpdateVolumeGroupPermissionSuccess(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult);
    void returnUpdateVolumeGroupPermissionFail(String error, int errorCode);
}
