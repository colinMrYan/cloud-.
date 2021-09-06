package com.inspur.emmcloud.web.api;

import java.io.File;

public interface WebProgressCallback {
    void onSuccess(File file);

    void onLoading(int progress, long current, String speed);

    void onFail();
}
