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

public class VolumeAPIInterfaceInstance implements VolumeAPIInterface{
    @Override
    public void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, String fileLocalPath, VolumeFile mockVolumeFile, int transferObserverId) {
    }

    @Override
    public void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath) {
    }

    @Override
    public void returnVolumeMemAddSuccess(List<String> uidList) {
    }

    @Override
    public void returnVolumeMemAddFail(String error, int errorCode) {
    }

    @Override
    public void returnGroupMemAddSuccess(List<String> uidList) {
    }

    @Override
    public void returnGroupMemAddFail(String error, int errorCode) {
    }

    @Override
    public void returnGroupMemDelSuccess(List<String> uidList) {
    }

    @Override
    public void returnGroupMemDelFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
    }

    @Override
    public void returnVolumeListFail(String error, int errorCode) {
    }

    @Override
    public void returnCreateShareVolumeSuccess(Volume volume) {
    }

    @Override
    public void returnCreateShareVolumeFail(String error, int errorCode) {
    }

    @Override
    public void returnUpdateShareVolumeNameSuccess(Volume volume, String name) {
    }

    @Override
    public void returnUpdateShareVolumeNameFail(String error, int errorCode) {
    }

    @Override
    public void returnRemoveShareVolumeSuccess(Volume volume) {
    }

    @Override
    public void returnRemoveShareVolumeFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult) {
    }

    @Override
    public void returnVolumeFileListFail(String error, int errorCode) {
    }

    @Override
    public void returnCreateForderSuccess(VolumeFile volumeFile) {
    }

    @Override
    public void returnCreateForderFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeFileDeleteSuccess(List<VolumeFile> deleteVolumeFileList) {
    }

    @Override
    public void returnVolumeFileDeleteFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeFileRenameSuccess(VolumeFile oldVolumeFile, String fileNewName) {
    }

    @Override
    public void returnVolumeFileRenameFail(String error, int errorCode) {
    }

    @Override
    public void returnMoveFileSuccess(List<VolumeFile> movedVolumeFileList) {
    }

    @Override
    public void returnMoveFileFail(String error, int errorCode) {
    }

    @Override
    public void returnCopyFileSuccess() {
    }

    @Override
    public void returnCopyFileFail(String error, int errorCode) {
    }

    @Override
    public void returnCopyFileBetweenVolumeSuccess() {

    }

    @Override
    public void returnCopyFileBetweenVolumeFail(String error, int errorCode, VolumeFile volumeFile) {

    }

    @Override
    public void returnMoveOrCopyFileBetweenVolumeSuccess(String operation) {

    }

    @Override
    public void returnMoveOrCopyFileBetweenVolumeFail(GetReturnMoveOrCopyErrorResult errorResult, int errorCode, String srcVolumeFilePath, String operation, List<VolumeFile> volumeFileList) {

    }

    @Override
    public void returnVolumeDetailSuccess(VolumeDetail volumeDetail) {
    }

    @Override
    public void returnVolumeDetailFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeMemDelSuccess(List<String> uidList) {
    }

    @Override
    public void returnVolumeMemDelFail(String error, int errorCode) {
    }

    @Override
    public void returnUpdateGroupNameSuccess(String name) {
    }

    @Override
    public void returnUpdateGroupNameFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeGroupContainMeSuccess(GetVolumeGroupResult getVolumeGroupResult) {
    }

    @Override
    public void returnVolumeGroupContainMeFail(String error, int errorCode) {
    }

    @Override
    public void returnVolumeGroupSuccess(GetVolumeResultWithPermissionResult getVolumeResultWithPermissionResult) {

    }

    @Override
    public void returnVolumeGroupFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateVolumeGroupPermissionSuccess(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult) {

    }

    @Override
    public void returnUpdateVolumeGroupPermissionFail(String error, int errorCode) {

    }
}
