package com.inspur.emmcloud.volume.serviceimpl;

import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.volume.VolumeService;
import com.inspur.emmcloud.volume.util.VolumeFileUploadManager;

public class VolumeServiceImpl implements VolumeService {

    @Override
    public void cancelVolumeFileUploadService(VolumeFile mockVolumeFile) {
        VolumeFileUploadManager.getInstance().cancelVolumeFileUploadService(mockVolumeFile);
    }
}
