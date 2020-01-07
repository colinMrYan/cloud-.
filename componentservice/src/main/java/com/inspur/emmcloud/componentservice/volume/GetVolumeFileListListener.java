package com.inspur.emmcloud.componentservice.volume;

import java.util.List;

public interface GetVolumeFileListListener {
    void onSuccess(List<VolumeFile> volumeFileList);

    void onFail();
}
