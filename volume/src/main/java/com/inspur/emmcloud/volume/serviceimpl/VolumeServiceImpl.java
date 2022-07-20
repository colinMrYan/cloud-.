package com.inspur.emmcloud.volume.serviceimpl;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.componentservice.volume.GetVolumeFileListListener;
import com.inspur.emmcloud.componentservice.volume.GetVolumeListListener;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.volume.VolumeService;
import com.inspur.emmcloud.volume.api.VolumeAPIInterfaceInstance;
import com.inspur.emmcloud.volume.api.VolumeAPIService;
import com.inspur.emmcloud.volume.bean.GetVolumeFileListResult;
import com.inspur.emmcloud.volume.bean.GetVolumeListResult;
import com.inspur.emmcloud.volume.util.VolumeFileUploadManager;

import java.util.ArrayList;
import java.util.List;

public class VolumeServiceImpl implements VolumeService {
    GetVolumeListListener getVolumeListListener;
    GetVolumeFileListListener getVolumeFileListListener;
    String fileFilterType;
    private VolumeAPIService apiService;

    public VolumeServiceImpl() {
        apiService = new VolumeAPIService(BaseApplication.getInstance());
        apiService.setAPIInterface(new WebService());
    }

    @Override
    public void cancelVolumeFileUploadService(VolumeFile mockVolumeFile) {
        VolumeFileUploadManager.getInstance().cancelVolumeFileUploadService(mockVolumeFile);
    }

    @Override
    public void getVolumeList(GetVolumeListListener getVolumeListListener) {
        this.getVolumeListListener = getVolumeListListener;
        apiService.getVolumeList();
    }

    @Override
    public void getVolumeFileList(String volumeId, String path, String fileType, GetVolumeFileListListener getVolumeFileListListener) {
        this.fileFilterType = fileType;
        this.getVolumeFileListListener = getVolumeFileListListener;
        apiService.getVolumeFileList(volumeId, path);
    }

    @Override
    public void uploadFile(VolumeFile file, String localPath, ProgressCallback callback) {
        VolumeFileUploadManager.getInstance().uploadFile(file, localPath,"/", callback);
    }

    private class WebService extends VolumeAPIInterfaceInstance {
        @Override
        public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
            super.returnVolumeListSuccess(getVolumeListResult);
            if (getVolumeListListener != null) {
                getVolumeListListener.onSuccess(getVolumeListResult.getMyVolume());
            }
        }

        @Override
        public void returnVolumeListFail(String error, int errorCode) {
            super.returnVolumeListFail(error, errorCode);
            if (getVolumeListListener != null) {
                getVolumeListListener.onFail();
            }
        }

        @Override
        public void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult) {
            super.returnVolumeFileListSuccess(getVolumeFileListResult);
            if (getVolumeFileListListener != null) {
                List<VolumeFile> volumeFileList = new ArrayList<>();
                if (StringUtils.isBlank(fileFilterType)) {
                    volumeFileList = getVolumeFileListResult.getVolumeFileList();
                } else if (fileFilterType.equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                    volumeFileList = getVolumeFileListResult.getVolumeFileDirectoryList();
                } else {
                    volumeFileList = getVolumeFileListResult.getVolumeFileFilterList(fileFilterType);
                }
                getVolumeFileListListener.onSuccess(volumeFileList);
            }
        }

        @Override
        public void returnVolumeFileListFail(String error, int errorCode) {
            super.returnVolumeFileListFail(error, errorCode);
            if (getVolumeFileListListener != null) {
                getVolumeFileListListener.onFail();
            }
        }
    }
}
