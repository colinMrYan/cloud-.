package com.inspur.emmcloud.componentservice.download;

import com.inspur.emmcloud.componentservice.volume.VolumeFile;

/**
 * Created by chenmch on 2017/11/24.
 */

public interface ProgressCallback {
    void onSuccess(VolumeFile volumeFile);

    void onLoading(int progress, long current, String speed);

    void onFail();
}
