package com.inspur.emmcloud.web.plugin.barcode.alidecoder;

import android.content.Context;
import android.content.Intent;

/**
 * Created by xingcheng on 2018/8/17.
 */

public class ScanHelper {

    private ScanCallback scanCallback;

    private ScanHelper() {
    }

    public static ScanHelper getInstance() {
        return Holder.instance;
    }

    public void scan(Context context, ScanCallback scanCallback) {
        if (context == null) {
            return;
        }
        this.scanCallback = scanCallback;
        context.startActivity(new Intent(context, ALiScanActivity.class));
    }

    void notifyScanResult(boolean isProcessed, Intent resultData) {
        if (scanCallback != null) {
            scanCallback.onScanResult(isProcessed, resultData);
            scanCallback = null;
        }
    }

    public interface ScanCallback {
        void onScanResult(boolean isProcessed, Intent result);
    }

    private static class Holder {
        private static ScanHelper instance = new ScanHelper();
    }
}
