package com.inspur.emmcloud.interf;

import com.inspur.emmcloud.componentservice.download.ProgressCallback;

/**
 * Created by chenmch on 2017/12/5.
 */

public interface VolumeFileUploadService {
    void onDestroy();

    void setProgressCallback(ProgressCallback progressCallback);

    void uploadFile(String fileName, String localFile);

    void onPause();
}
