package com.inspur.emmcloud.componentservice.volume;

public interface GetVolumeListListener {
    void onSuccess(Volume volume);

    void onFail();
}
