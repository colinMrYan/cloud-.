package com.inspur.emmcloud.volume.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.volume.VolumeService;
import com.inspur.emmcloud.volume.serviceimpl.VolumeServiceImpl;

public class VolumeAppLike implements IApplicationLike {
    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        router.addService(VolumeService.class, new VolumeServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(VolumeService.class);
    }
}
