package com.inspur.emmcloud.setting.util;

import android.content.Context;

import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;

public class FingerPrintUtils {

    private static FingerPrintUtils fingerPrintUtils;

    public static FingerPrintUtils getFingerPrintInstance() {
        if (fingerPrintUtils == null) {
            synchronized (FingerPrintUtils.class) {
                if (fingerPrintUtils == null) {
                    fingerPrintUtils = new FingerPrintUtils();
                }
            }
        }
        return fingerPrintUtils;
    }


    /**
     * 判断系统指纹是否可用
     *
     * @param context
     * @return
     */
    public boolean isFingerPrintAvaiable(Context context) {
        FingerprintIdentify cloudFingerprintIdentify = new FingerprintIdentify(context);
        boolean isHardwareEnable = getIsHardwareEnable(cloudFingerprintIdentify);
        boolean isFingerprintEnable = getIsFingerprintEnable(cloudFingerprintIdentify);
        return isHardwareEnable && isFingerprintEnable;
    }

    /**
     * 硬件是否可用
     *
     * @return
     */
    private boolean getIsHardwareEnable(FingerprintIdentify cloudFingerprintIdentify) {
        return cloudFingerprintIdentify == null ? false : cloudFingerprintIdentify.isHardwareEnable();
    }

    /**
     * 判断是否设置了指纹
     *
     * @param cloudFingerprintIdentify
     * @return
     */
    private boolean getIsFingerprintEnable(FingerprintIdentify cloudFingerprintIdentify) {
        return cloudFingerprintIdentify.isFingerprintEnable();
    }
}
