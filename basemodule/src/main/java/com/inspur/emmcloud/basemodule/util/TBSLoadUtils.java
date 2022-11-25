package com.inspur.emmcloud.basemodule.util;

import static com.tencent.liteav.base.ContextUtils.getApplicationContext;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsListener;

import java.util.HashMap;
import java.util.List;

public class TBSLoadUtils {
    private static final String TAG = "TBSLoadUtils";
    public interface TbsDownloadListener {
        void updateProgress(int progress);

        void updateInstallState();
    }

    public static void initTxTbx(final Context context, final TbsDownloadListener downloadListener) {
        // 禁用X5浏览器的话停止下载
        if (!PreferencesUtils.getBoolean(context, Constant.PREF_TBS_USE_X5, true)) {
            Log.d("ByWebView","!USE_X5");
            return;
        }
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        /* 设置允许移动网络下进行内核下载。默认不下载，会导致部分一直用移动网络的用户无法使用x5内核 */
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.setCoreMinVersion(QbSdk.CORE_VER_ENABLE_202112);
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
            }

            @Override
            public void onInstallFinish(int i) {
                if (i == 200) {
                    PreferencesUtils.putBoolean(context, Constant.PREF_TBS_INSTALL, true);
                    if (downloadListener != null){
                        downloadListener.updateInstallState();
                    }
                    Log.d("ByWebView", "x5内核安装成功");
                    Activity currentActivity =  ((BaseApplication) context).getActivityLifecycleCallbacks().getCurrentActivity();
                    if (currentActivity instanceof BaseFragmentActivity) {
                        new CustomDialog.MessageDialogBuilder(currentActivity)
                                .setMessage(currentActivity.getString(R.string.toast_restart_app))
                                .setPositiveButton(currentActivity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    } else {
                        ToastUtils.show(R.string.toast_restart_app);
                    }
                }
            }

            @Override
            public void onDownloadProgress(int i) {
                if (downloadListener != null){
                    downloadListener.updateProgress(i);
                }
            }
        });
        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
            }

            /**
             * 预初始化结束
             * 由于X5内核体积较大，需要依赖网络动态下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
             * @param isX5 是否使用X5内核
             */
            @Override
            public void onViewInitFinished(boolean isX5) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                if (isX5) {
                    Log.d("ByWebView", "x5内核加载成功");
                } else {
                    // 首次进入安装成功，但是默认使用webkit
                    Log.e("ByWebView", "x5内核加载失败，自动切换到系统内核");
                    if (!PreferencesUtils.getBoolean(context, Constant.PREF_TBS_INSTALL) && NetUtils.isNetworkConnected(context) && !TbsDownloader.isDownloading()){
                        QbSdk.reset(context);
                        //手动开始下载，此时需要先判定网络是否符合要求
                        TbsDownloader.startDownload(context);
                    }
                }
            }
        });
    }
}
