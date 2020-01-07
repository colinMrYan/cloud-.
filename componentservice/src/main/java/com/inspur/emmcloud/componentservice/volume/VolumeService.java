package com.inspur.emmcloud.componentservice.volume;

import com.inspur.emmcloud.componentservice.CoreService;

public interface VolumeService extends CoreService {
    void cancelVolumeFileUploadService(VolumeFile mockVolumeFile);

    void getVolumeList(GetVolumeListListener getVolumeListListener);

    void getVolumeFileList(String volumeId, String path, String fileType, GetVolumeFileListListener getVolumeFileListListener);
}
