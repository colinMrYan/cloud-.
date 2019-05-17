package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by libaochao on 2019/5/9.
 */

public class NotificationUpgradeUtils extends APIInterfaceInstance {
    protected static final int SHOW_PEOGRESS_LAODING_DLG = 0;
    private static final int notUpdateInterval = 86400000;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int DOWNLOAD_FAIL = 5;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private static final String TAG = "UpgradeUtils";
    private static double MBDATA = 1048576.0;
    private static double KBDATA = 1024.0;
    private static String DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/download/";
    private Boolean cancelUpdate = false;
    private GetUpgradeResult getUpgradeResult;
    private Context context;
    private Handler upgradeHandler;
    private Handler handler;
    private int upgradeCode;
    private String upgradeUrl;
    private long totalSize;
    private long downloadSize;
    /**
     * 记录进度条数量*
     */
    private int progress;
    private MyDialog progressDownloadDialog;
    private TextView ratioText;
    private String upgradeMsg;
    private String downloadPercent;
    private LoadingDialog loadingDlg;
    private Cancelable cancelable;
    private boolean isManualCheck;
    private ProgressBar downloadProgressBar;
    private TextView percentText;

    private upGradeNotificationUtils notificationUtils;

    //isManualCheck 是否在关于中手动检查更新
    public NotificationUpgradeUtils(Context context, Handler handler, boolean isManualCheck) {
        this.context = context;
        this.handler = handler;
        this.isManualCheck = isManualCheck;
        loadingDlg = new LoadingDialog(context);
        handMessage();
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        upgradeHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case UPGRADE_FAIL:
                        if (handler != null) {
                            handler.sendEmptyMessage(UPGRADE_FAIL);
                        }
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
                        if (context != null) {
                            if (upgradeCode == 2) {
                                showForceUpgradeDlg();
                            }
                        }
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

    public void checkUpdate(boolean isShowLoadingDlg) {
        if (NetUtils.isNetworkConnected(context, isShowLoadingDlg)) {
            loadingDlg.show(isShowLoadingDlg);
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(this);
            apiService.checkUpgrade(isManualCheck);
        } else if (handler != null) {
            handler.sendEmptyMessage(UPGRADE_FAIL);
        }
    }

    private void handleUpgrade() {
        // TODO Auto-generated method stub
        upgradeCode = getUpgradeResult.getUpgradeCode();
        upgradeMsg = getUpgradeResult.getUpgradeMsg();
        String changeLog = getUpgradeResult.getChangeLog();
        if (!StringUtils.isBlank(changeLog)) {
            upgradeMsg = changeLog;
        }
        upgradeUrl = getUpgradeResult.getUpgradeUrl();
        switch (upgradeCode) {
            case 0: // 无须升级
                if (handler != null) {
                    handler.sendEmptyMessage(NO_NEED_UPGRADE);
                }
                break;
            case 1: // 可选升级
                long appNotUpdateTime = PreferencesUtils.getLong(context, "appNotUpdateTime");
                if (isManualCheck || System.currentTimeMillis() - appNotUpdateTime > notUpdateInterval) {
                    showSelectUpgradeDlg();
                } else if (handler != null) {
                    handler.sendEmptyMessage(NO_NEED_UPGRADE);
                }

                break;
            case 2: // 必须升级
                showForceUpgradeDlg();
                break;

            default:
                if (handler != null) {
                    handler.sendEmptyMessage(NO_NEED_UPGRADE);
                }
                break;
        }
    }

    private void showSelectUpgradeDlg() {
        final MyDialog dialog = new MyDialog(context,
                R.layout.dialog_two_buttons);
        dialog.setCancelable(false);
        Button okBtn = dialog.findViewById(R.id.ok_btn);
        okBtn.setText(context.getString(R.string.upgrade));
        TextView appUpdateContentText = dialog.findViewById(R.id.text);
        appUpdateContentText.setMovementMethod(ScrollingMovementMethod.getInstance());
        appUpdateContentText.setText(upgradeMsg);
        TextView appUpdateTitle = dialog.findViewById(R.id.app_update_title);
        TextView appUpdateVersion = dialog.findViewById(R.id.app_update_version);
        appUpdateTitle.setText(context.getString(R.string.app_update_remind));
        appUpdateVersion.setText(context.getString(R.string.app_last_version) + "(" + getUpgradeResult.getLatestVersion() + ")");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (context != null) {
                    if (null == notificationUtils) {
                        notificationUtils = new upGradeNotificationUtils(context, 10000);
                    }
                    // 下载文件
                    downloadApk();
                    if (handler != null) {
                        handler.sendEmptyMessage(NO_NEED_UPGRADE);
                    }
                }
            }
        });
        Button cancelBt = dialog.findViewById(R.id.cancel_btn);
        cancelBt.setText(context.getString(R.string.not_upgrade));
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PreferencesUtils.putLong(context, "appNotUpdateTime", System.currentTimeMillis());
                if (handler != null) {
                    handler.sendEmptyMessage(DONOT_UPGRADE);
                }
            }
        });
        if (context != null) {
            dialog.show();
        }

    }

    private void showForceUpgradeDlg() {
        // TODO Auto-generated method stub
        final MyDialog dialog = new MyDialog(context,
                R.layout.dialog_two_buttons);
        dialog.setCancelable(false);
        Button okBtn = dialog.findViewById(R.id.ok_btn);
        okBtn.setText(context.getString(R.string.upgrade));
        TextView appUpdateContentText = dialog.findViewById(R.id.text);
        appUpdateContentText.setMovementMethod(ScrollingMovementMethod.getInstance());
        appUpdateContentText.setText(upgradeMsg);
        TextView appUpdateTitle = dialog.findViewById(R.id.app_update_title);
        appUpdateTitle.setText(context.getString(R.string.app_update_remind));
        TextView appUpdateVersion = dialog.findViewById(R.id.app_update_version);
        appUpdateVersion.setText(context.getString(R.string.app_last_version) + "(" + getUpgradeResult.getLatestVersion() + ")");
        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        Button cancelBt = dialog.findViewById(R.id.cancel_btn);
        cancelBt.setText(context.getString(R.string.exit));
        cancelBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((MyApplication) context.getApplicationContext()).exit();
            }
        });
        if (context != null) {
            dialog.show();
        }
    }

    private void showDownloadDialog() {
        cancelUpdate = false;
        progressDownloadDialog = new MyDialog(context, R.layout.dialog_down_progress_one_button);
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
                // 设置取消状态
                cancelUpdate = true;
                if (upgradeCode == 2) { // 强制升级时
                    ((MyApplication) context.getApplicationContext()).exit();
                } else if (handler != null) {
                    handler.sendEmptyMessage(DONOT_UPGRADE);
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
                            if (null != notificationUtils) {
                                notificationUtils.updateNotification(context.getResources().getString(R.string.app_update_error), false);
                            }
                        }

                        @Override
                        public void onFinished() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onSuccess(File arg0) {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            if (null != notificationUtils) {
                                notificationUtils.deleteNotification();
                            }
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
                            if (null != notificationUtils) {
                                String data = context.getResources().getString(R.string.app_update_loaded) +
                                        FileUtils.formatFileSize(downloadSize) + "/" + FileUtils.formatFileSize(totalSize);
                                notificationUtils.updateNotification(data, true);
                            }
                        }

                        @Override
                        public void onStarted() {
                            // TODO Auto-generated method stub
                            upgradeHandler.sendEmptyMessage(SHOW_PEOGRESS_LAODING_DLG);
                            if (null != notificationUtils) {
                                notificationUtils.initNotification();
                                ToastUtils.show(context,
                                        context.getString(R.string.app_update_prepare));
                            }
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

    @Override
    public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult, boolean isManualCheck) {
        // TODO Auto-generated method stub
        if (loadingDlg != null && loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
        this.getUpgradeResult = getUpgradeResult;
        handleUpgrade();
    }

    @Override
    public void returnUpgradeFail(String error, boolean isManualCheck, int errorCode) {
        // TODO Auto-generated method stub
        if (loadingDlg != null && loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
        upgradeHandler.sendEmptyMessage(UPGRADE_FAIL);
        //在未登陆之前，为防止还不知道升级服务所在的地址导致检测升级失败，不再弹出提示
//        WebServiceMiddleUtils.hand(context, error, errorCode);
//		WebServiceMiddleUtils.hand(context, error, upgradeHandler,
//				UPGRADE_FAIL);
    }
}
