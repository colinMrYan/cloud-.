package com.inspur.emmcloud.componentservice.volume;

import com.inspur.emmcloud.componentservice.CoreService;

public interface VolumeService extends CoreService {
    void cancelVolumeFileUploadService(VolumeFile mockVolumeFile);
}
