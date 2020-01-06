package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.content.DialogInterface;

import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;

public class NetworkMobileTipUtil {

    /**
     * 移动网络 且文件大于50M提示框
     */
    public static void checkEnvironment(Context context, int tipResId, long totalDownloadSize, Callback callback) {
        if (!NetUtils.isNetworkConnected(BaseApplication.getInstance()) || !AppUtils.isHasSDCard(BaseApplication.getInstance())) {
            callback.cancel();
        }
        if (totalDownloadSize >= MyAppConfig.NETWORK_MOBILE_MAX_SIZE_ALERT && NetUtils.isNetworkTypeMobile(BaseApplication.getInstance())) {
            showNetworkMobileAlert(context, tipResId, callback);
        } else {
            callback.onNext();
        }
    }

    private static void showNetworkMobileAlert(Context context, int resId, final Callback callback) {
        if (context != null) {
            showNetworkMobileAlert(context, context.getString(resId), callback);
        } else {
            callback.cancel();
        }
    }

    private static void showNetworkMobileAlert(Context context, String tipStr, final Callback callback) {
        if (context == null) {
            callback.cancel();
            return;
        }
        new CustomDialog.MessageDialogBuilder(context)
                .setMessage(tipStr)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.cancel();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onNext();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public interface Callback {
        void cancel();

        void onNext();
    }
}
