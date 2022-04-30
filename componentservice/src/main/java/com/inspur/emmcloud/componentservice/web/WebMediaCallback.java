package com.inspur.emmcloud.componentservice.web;

import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;

import org.json.JSONException;

public interface WebMediaCallback extends ProgressCallback {
    @Override
    void onSuccess(VolumeFile volumeFile);

    @Override
    void onLoading(int progress, long current, String speed);

    @Override
    void onFail();

    void onRecordEnd(String resourceLocalPath) throws JSONException;
}
