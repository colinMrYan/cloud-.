package com.inspur.emmcloud.bean.system;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppConfigCacheUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by: yufuchang
 * Date: 2020/4/2
 */
public class Update2NewVersionUtils {

    //更新到新版本（云+2.0）提醒间隔时间
    private static final int notUpdateInterval = 86400000;
    private Context context;

    protected static final int SHOW_PEOGRESS_LAODING_DLG = 0;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int DOWNLOAD_FAIL = 5;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private long totalSize;
    private long downloadSize;
    private static double MBDATA = 1048576.0;
    private static double KBDATA = 1024.0;

    private static String DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/download/";

    private int progress;
    private MyDialog progressDownloadDialog;
    private TextView ratioText;
    private String downloadPercent;
    private Callback.Cancelable cancelable;
    private ProgressBar downloadProgressBar;
    private TextView percentText;
    private String upgradeUrl;
    private Handler upgradeHandler;
    public Update2NewVersionUtils(Context context){
        this.context = context;
        handMessage();
    }

    /**
     * 检查是否需要升级到新版本
     */
    public void checkNeedUpdate2NewVersion() {
        AppUpdateConfigBean appUpdateConfigBean = new AppUpdateConfigBean(AppConfigCacheUtils.getAppConfigValue(context,Constant.CONCIG_UPDATE_2_NEWVERSION,""));
        long appUpdate2NewVersionNotUpdateTime = PreferencesUtils.getLong(context,"appUpdate2NewVersionNotUpdateTime",0);
        //检查如果没装云+2.0，且有新版本的下载地址，且没有延迟提示，才提示更新到新版本
        if(!AppUtils.checkAppInstalledByApplist(context,"com.inspur.playwork.internet") && !StringUtils.isBlank(appUpdateConfigBean.getNewVersionURL()) && (System.currentTimeMillis() - appUpdate2NewVersionNotUpdateTime) > notUpdateInterval){
            showSelectUpgradeDlg(appUpdateConfigBean);
        }
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        upgradeHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case UPGRADE_FAIL:

                        break;
                    case DOWNLOAD:
                        downloadPercent = getPercent(progress);
                        String text = setFormat(downloadSize) + "/"
                                + setFormat(totalSize);
                        if (ratioText != null) {
                            ratioText.setText(text);
                        }
                        if (downloadProgressBar != null) {
                            downloadProgressBar.setProgress(progress);
                        }
                        if (percentText != null) {
                            percentText.setText(downloadPercent);
                        }
                        break;
                    case DOWNLOAD_FINISH:
                        if (progressDownloadDialog != null && progressDownloadDialog.isShowing()) {
                            progressDownloadDialog.dismiss();
                        }
//                        AppUtils.installApk(context,DOWNLOAD_PATH, "update.apk");
                        FileUtils.openFile(context, DOWNLOAD_PATH + "update.apk");
                        if (context instanceof MainActivity) {
                            ((Activity) context).finish();
                        }
                        break;

                    case DOWNLOAD_FAIL:
                        if (progressDownloadDialog != null && progressDownloadDialog.isShowing()) {
                            progressDownloadDialog.dismiss();
                        }
                        ToastUtils.show(context,
                                context.getString(R.string.update_fail));
                        break;
                    case SHOW_PEOGRESS_LAODING_DLG:
                        if (null != progressDownloadDialog) {
                            progressDownloadDialog.show();
                        }
                        break;

                    default:
                        break;
                }
            }

        };
    }

    /**
     * 获取百分率
     **/
    private String getPercent(int progress) {
        // TODO Auto-generated method stub
        return progress + "%";
    }

    /**
     * 格式化数据
     **/
    private String setFormat(long data) {
        // TODO Auto-generated method stub
        if (data < KBDATA) {
            return data + "B";
        } else if (data < MBDATA) {
            return new DecimalFormat(("####0.00")).format(data / KBDATA) + "KB";
        } else {
            return new DecimalFormat(("####0.00")).format(data / MBDATA) + "MB";
        }
    }

    private void showSelectUpgradeDlg(final AppUpdateConfigBean appUpdateConfigBean) {
        upgradeUrl = appUpdateConfigBean.getNewVersionURL();
        final MyDialog dialog = new MyDialog(context,
                R.layout.dialog_update_version);
        dialog.setCancelable(false);
        TextView okBtn = dialog.findViewById(R.id.tv_version_update_download);
        ((TextView)dialog.findViewById(R.id.tv_version_update_content)).setText(appUpdateConfigBean.getNewVersionTip());
        dialog.findViewById(R.id.tv_new_version_help).setVisibility(StringUtils.isBlank(appUpdateConfigBean.getHelpURL())?View.GONE:View.VISIBLE);
        dialog.findViewById(R.id.tv_new_version_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("uri", appUpdateConfigBean.getHelpURL());
                bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
                ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
            }
        });
        okBtn.setText(R.string.update_2_new_cloud_plus);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        ImageView cancelBt = dialog.findViewById(R.id.iv_version_update_close);
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PreferencesUtils.putLong(context, "appUpdate2NewVersionNotUpdateTime", System.currentTimeMillis());
            }
        });
        dialog.show();

    }

    private void showDownloadDialog() {
        progressDownloadDialog = new MyDialog(context, R.layout.app_dialog_down_progress_one_button);
        progressDownloadDialog.setDimAmount(0.2f);
        progressDownloadDialog.setCancelable(false);
        progressDownloadDialog.setCanceledOnTouchOutside(false);
        ((TextView) progressDownloadDialog.findViewById(R.id.tv_permission_dialog_title)).
                setText(context.getString(R.string.cloud_plus_update, AppUtils.getAppName(context)));
        progressDownloadDialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDownloadDialog.dismiss();
                if (cancelable != null) {
                    cancelable.cancel();
                }
            }
        });
        percentText = progressDownloadDialog.findViewById(R.id.tv_num_percent);
        ratioText = progressDownloadDialog.findViewById(R.id.tv_num_progress);
        downloadProgressBar = progressDownloadDialog.findViewById(R.id.progress_bar_apk_download);
        if (context != null) {
            // 下载文件
            downloadApk();
        }
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 判断SD卡是否存在，并且是否具有读写权限
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            LogUtils.YfcDebug("升级下载地址："+upgradeUrl);
            RequestParams params = new RequestParams(upgradeUrl);
            params.setSaveFilePath(DOWNLOAD_PATH + "update.apk");
            cancelable = x.http().get(params,
                    new Callback.ProgressCallback<File>() {

                        @Override
                        public void onCancelled(CancelledException arg0) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onError(Throwable arg0, boolean arg1) {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(DOWNLOAD_FAIL);
                        }

                        @Override
                        public void onFinished() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onSuccess(File arg0) {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                        }

                        @Override
                        public void onLoading(long arg0, long arg1, boolean arg2) {
                            // TODO Auto-generated method stub
                            totalSize = arg0;
                            downloadSize = arg1;
                            progress = (int) (((float) arg1 / arg0) * 100);
                            // 更新进度
                            if (progressDownloadDialog != null
                                    && progressDownloadDialog.isShowing()) {
                                upgradeHandler.sendEmptyMessage(DOWNLOAD);
                            }
                        }

                        @Override
                        public void onStarted() {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(SHOW_PEOGRESS_LAODING_DLG);
                        }

                        @Override
                        public void onWaiting() {
                            // TODO Auto-generated method stub

                        }
                    });
        } else {
            upgradeHandler.sendEmptyMessage(DOWNLOAD_FAIL);
        }

    }
}
