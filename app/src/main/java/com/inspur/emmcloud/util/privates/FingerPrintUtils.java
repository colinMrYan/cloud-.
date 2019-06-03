package com.inspur.emmcloud.util.privates;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.widget.Toast;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yufuchang on 2017/9/1.
 */

public class FingerPrintUtils {
    private CancellationSignal mCancellationSignal;
    private FingerprintManagerCompat mFingerprintManager;
    private Context context;

    public FingerPrintUtils(Activity activity) {
        this.context = activity;
//        setFingerPrintListener();
    }

    public void setFingerPrintListener() {

        mFingerprintManager.authenticate(null, 0, mCancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
            /**
             * 指纹识别成功
             * @param result
             */
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                Toast.makeText(context, "指纹识别成功", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post("success");
            }

            /**
             * 指纹识别失败调用
             */
            @Override
            public void onAuthenticationFailed() {
                LogUtils.YfcDebug("指纹识别失败");
                Toast.makeText(context, "指纹识别失败", Toast.LENGTH_SHORT).show();
            }

            /**
             * @param helpMsgId
             * @param helpString
             */
            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                LogUtils.YfcDebug("指纹识别帮助");
                Toast.makeText(context, helpString, Toast.LENGTH_SHORT).show();
            }

            /**
             * 多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
             * @param errMsgId  最多的错误次数
             * @param errString 错误的信息反馈
             */
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                LogUtils.YfcDebug("指纹识别错误次数" + errMsgId);
            }
        }, null);
    }

    /**
     * 判断是否满足设置指纹的条件
     *
     * @return true 满足 false 不满足
     */
    public boolean isSatisfactionFingerprint() {
        mCancellationSignal = new CancellationSignal();
        mFingerprintManager = FingerprintManagerCompat.from(context);
        boolean isHasFingerPrintPermission = checkFingerPrintPermission();
        boolean isHasFingerPrintHardware = checkFingerPrintHardware();
        boolean isOpenUnlockCode = checkHasUnlockCode();
        boolean isHasFingerprint = checkHasFingerPrint();
        return (isHasFingerPrintPermission && isHasFingerPrintHardware && isOpenUnlockCode && isHasFingerprint);
    }

    /**
     * 检查是否录入指纹
     *
     * @return
     */
    private boolean checkHasFingerPrint() {
        //是否有指纹录入
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            Toast.makeText(context, "您还未录入指纹", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * 检查是否设置手势密码
     *
     * @return
     */
    private boolean checkHasUnlockCode() {
        String gestureCode = PreferencesByUserAndTanentUtils.getString(context, "gesture_code");
        return !StringUtils.isBlank(gestureCode);
    }

    /**
     * 检查硬件是否支持
     *
     * @return
     */
    private boolean checkFingerPrintHardware() {
        //硬件是否支持指纹识别
        if (!mFingerprintManager.isHardwareDetected()) {
            Toast.makeText(context, "您手机不支持指纹识别功能", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * 检查权限
     *
     * @return
     */
    private boolean checkFingerPrintPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "请开启指纹识别权限", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * 停止指纹识别
     */
    public void stopsFingerPrintListener() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }
}
