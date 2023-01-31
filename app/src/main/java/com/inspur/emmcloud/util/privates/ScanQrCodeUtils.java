package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.basemodule.config.Constant;

/**
 * Created by yufuchang on 2017/7/21.
 * 封装扫描处理模块，用来处理扫一扫功能
 */

public class ScanQrCodeUtils {
    private static ScanQrCodeUtils scanQrCodeUtils;
    private Context context;

    private ScanQrCodeUtils(Context context) {
        this.context = context;
    }

    public static ScanQrCodeUtils getScanQrCodeUtilsInstance(Context context) {
        if (scanQrCodeUtils == null) {
            synchronized (ScanQrCodeUtils.class) {
                if (scanQrCodeUtils == null) {
                    scanQrCodeUtils = new ScanQrCodeUtils(context);
                }
            }
        }
        return scanQrCodeUtils;
    }

    /**
     * 处理扫描到的信息
     *
     * @param result
     */
    public void handleActionWithMsg(String result) {
        result = result.trim();
        if (isMatchCloudPlusProtocol(result)) {
            Uri uri = Uri.parse(result);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } else if (result.startsWith("http")) {
            Bundle bundle = new Bundle();
            bundle.putString("uri", result);
            bundle.putString("appName", "    ");
            ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
        } else {
            showUnKnownMsg(result);
        }
    }

    /**
     * 判断是否符合云+的协议栈
     *
     * @param result
     * @return
     */
    private boolean isMatchCloudPlusProtocol(String result) {
        return result.startsWith("ecm-contact") || result.startsWith("ecc-component") ||
                result.startsWith("ecc-app-react-native") || result.startsWith("gs-msg")
                || result.startsWith("ecc-channel") || result.startsWith("ecc-app");
    }

    /**
     * 展示扫描到的信息
     *
     * @param result
     */
    private void showUnKnownMsg(String result) {
        Bundle bundle = new Bundle();
        bundle.putString("result", result);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_SCANRESULT).with(bundle).navigation();
    }
}
