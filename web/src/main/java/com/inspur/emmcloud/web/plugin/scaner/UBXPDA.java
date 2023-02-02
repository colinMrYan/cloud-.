package com.inspur.emmcloud.web.plugin.scaner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

public class UBXPDA implements IPDA {
    private final static String UBX_SCANER_ACTION = "android.intent.ACTION_DECODE_DATA";
    private final static Integer MIN_TIME_CALL_JS = 150;
    private static long lastCallJsTime;
    private final Activity mActivity;
    private final PDAService mScanerService;

    public UBXPDA(@NonNull Activity activity, @NonNull PDAService scanerService) {
        mActivity = activity;
        mScanerService = scanerService;
    }

    @Override
    public void init() {
        mScanerService.callJsResult("success");
    }

    @Override
    public void registerScanHeaderReceiver() {
        registerScanResultBroadcastReceiver();
    }

    @Override
    public void unregisterScanHeaderReceiver() {
        unregisterScanResultBroadcastReceiver();
        mScanerService.callJsResult("success");
    }

    @Override
    public void destroy() {
        unregisterScanResultBroadcastReceiver();
    }

    private BroadcastReceiver mScanResultBroadcastReceiver;

    private void registerScanResultBroadcastReceiver() {
        if (mScanResultBroadcastReceiver == null) {
            mScanResultBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (!UBX_SCANER_ACTION.equals(intent.getAction())) {
                        return;
                    }
                    long currentClickTime = System.currentTimeMillis();
                    if ((currentClickTime - lastCallJsTime) >= MIN_TIME_CALL_JS) {
                        String code = intent.getStringExtra("barcode_string");
                        mScanerService.callJsResult(code);
                        lastCallJsTime = currentClickTime;
                    }
                }
            };
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UBX_SCANER_ACTION);
        mActivity.registerReceiver(mScanResultBroadcastReceiver, intentFilter);
    }

    private void unregisterScanResultBroadcastReceiver() {
        if (mScanResultBroadcastReceiver != null) {
            mActivity.unregisterReceiver(mScanResultBroadcastReceiver);
            mScanResultBroadcastReceiver = null;
        }
    }
}
