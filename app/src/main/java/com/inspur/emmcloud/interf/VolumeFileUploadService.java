package com.inspur.emmcloud.interf;

/**
 * Created by chenmch on 2017/12/5.
 */

public interface VolumeFileUploadService {
    void onDestory();

    void setProgressCallback(ProgressCallback progressCallback);

    void uploadFile(String fileName, String localFile);
}
