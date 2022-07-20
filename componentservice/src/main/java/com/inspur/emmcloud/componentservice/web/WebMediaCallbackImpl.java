package com.inspur.emmcloud.componentservice.web;

import com.inspur.emmcloud.componentservice.volume.VolumeFile;

import org.json.JSONException;

public abstract class WebMediaCallbackImpl implements WebMediaCallback{

    public void onSuccess(String webPath) {

    }

    @Override
    public void onSuccess(VolumeFile volumeFile) {

    }

    @Override
    public void onLoading(int progress, long current, String speed) {

    }

    @Override
    public void onFail() {

    }

    @Override
    public void onRecordEnd(String resourceLocalPath) throws JSONException {

    }
}
