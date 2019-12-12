package com.inspur.emmcloud.interf;

import java.io.File;

public interface ChatProgressCallback {
    void onSuccess(File file);

    void onLoading(int progress, long current, String speed);

    void onFail();
}
