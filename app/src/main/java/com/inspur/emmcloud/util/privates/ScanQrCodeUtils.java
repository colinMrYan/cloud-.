package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.barcode.ScanResultActivity;

/**
 * Created by yufuchang on 2017/7/21.
 * 封装扫描处理模块，用来处理扫一扫功能
 */

public class ScanQrCodeUtils {
    private static ScanQrCodeUtils scanQrCodeUtils;
    private Context context;
    private LoadingDialog loadingDialog;

    private ScanQrCodeUtils(Context context) {
        this.context = context;
        loadingDialog = new LoadingDialog(context);
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
     * @param msg
     */
    public void handleActionWithMsg(String msg) {
        msg = msg.trim();
        if (isMatchCloudPlusProtrol(msg)) {
            Uri uri = Uri.parse(msg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } else if (msg.startsWith("http")) {
            Intent intent = new Intent();
            intent.setClass(context, ImpActivity.class);
            intent.putExtra("uri", msg);
            intent.putExtra("appName", "    ");
            context.startActivity(intent);
        } else {
            showUnKnownMsg(msg);
        }
    }

    /**
     * 判断是否符合云+的协议栈
     *
     * @param msg
     * @return
     */
    private boolean isMatchCloudPlusProtrol(String msg) {
        return msg.startsWith("ecm-contact") || msg.startsWith("ecc-component") ||
                msg.startsWith("ecc-app-react-native") || msg.startsWith("gs-msg")
                || msg.startsWith("ecc-channel") || msg.startsWith("ecc-app");
    }

    /**
     * 展示扫描到的信息
     *
     * @param msg
     */
    private void showUnKnownMsg(String msg) {
        Intent intent = new Intent();
        intent.putExtra("result", msg);
        intent.setClass(context, ScanResultActivity.class);
        context.startActivity(intent);
    }
}
