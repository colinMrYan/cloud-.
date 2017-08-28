package com.inspur.emmcloud.callback;

import org.xutils.common.Callback;

import java.io.File;

/**
 * Created by chenmch on 2017/8/19.
 */

public class FileDowmloadProgressCallback implements Callback.ProgressCallback<File> {
    @Override
    public void onWaiting() {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onLoading(long l, long l1, boolean b) {

    }

    @Override
    public void onSuccess(File file) {

    }

    @Override
    public void onError(Throwable throwable, boolean b) {

    }

    @Override
    public void onCancelled(CancelledException e) {

    }

    @Override
    public void onFinished() {

    }
}
