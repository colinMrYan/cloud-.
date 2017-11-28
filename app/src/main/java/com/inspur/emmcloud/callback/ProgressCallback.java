package com.inspur.emmcloud.callback;

import com.inspur.emmcloud.bean.Volume.VolumeFile;

/**
 * Created by chenmch on 2017/11/24.
 */

public interface ProgressCallback {
    void onSuccess(VolumeFile volumeFile);
    void onLoading(int progress);
    void onFail();
}
