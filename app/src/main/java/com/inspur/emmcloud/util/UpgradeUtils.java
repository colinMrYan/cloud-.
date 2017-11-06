package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.GetUpgradeResult;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.DecimalFormat;

public class UpgradeUtils extends APIInterfaceInstance {

    protected static final int SHOW_PEOGRESS_LAODING_DLG = 0;
    private static final int notUpdateInterval = 86400000;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int DOWNLOAD_FAIL = 5;
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private static double MBDATA = 1048576.0;
    private static double KBDATA = 1024.0;
    private Boolean cancelUpdate = false;
    private static String DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/download/";
    private static final String TAG = "UpgradeUtils";
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
    private MyDialog mDownloadDialog;
    private TextView ratioText;
    private String upgradeMsg;
    private String downloadPercent;
    private LoadingDialog loadingDlg;
    private Cancelable cancelable;
    private boolean isManualCheck;

    //isManualCheck 是否在关于中手动检查更新
    public UpgradeUtils(Context context, Handler handler, boolean isManualCheck) {
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
                        String text = downloadPercent + "," + "  "
                                + setFormat(downloadSize) + "/"
                                + setFormat(totalSize);
                        ratioText.setText(text);
                        break;

                    case DOWNLOAD_FINISH:
                        if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
                            mDownloadDialog.dismiss();
                        }
                        installApk();
                        if (context instanceof MainActivity) {
                            ((Activity) context).finish();
                        }
                        break;

                    case DOWNLOAD_FAIL:
                        if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
                            mDownloadDialog.dismiss();
                        }
                        ToastUtils.show(context,
                                context.getString(R.string.update_fail));
                        if (context != null) {
                            if (upgradeCode == 2) {
                                showForceUpgradeDlg();
                            } else {
                                showSelectUpgradeDlg();
                            }
                        }
                        break;
                    case SHOW_PEOGRESS_LAODING_DLG:
                        mDownloadDialog.show();
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
            apiService.setAPIInterface(UpgradeUtils.this);
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
                handler.sendEmptyMessage(NO_NEED_UPGRADE);
                break;
            case 1: // 可选升级
                long appNotUpdateTime = PreferencesUtils.getLong(context, "appNotUpdateTime");
                if (isManualCheck || System.currentTimeMillis() - appNotUpdateTime > notUpdateInterval) {
                    showSelectUpgradeDlg();
                } else {
                    handler.sendEmptyMessage(NO_NEED_UPGRADE);
                }

                break;
            case 2: // 必须升级
                showForceUpgradeDlg();
                break;

            default:
                break;
        }
    }

    private void showSelectUpgradeDlg() {
        // TODO Auto-generated method stub
        final MyDialog dialog = new MyDialog(context,
                R.layout.dialog_two_buttons);
        dialog.setCancelable(false);
        Button okBt = (Button) dialog.findViewById(R.id.ok_btn);
        okBt.setText(context.getString(R.string.upgrade));
        TextView text = (TextView) dialog.findViewById(R.id.text);
        text.setText(upgradeMsg);
        TextView appUpdateTitle = (TextView) dialog.findViewById(R.id.app_update_title);
        TextView appUpdateVersion = (TextView) dialog.findViewById(R.id.app_update_version);
        appUpdateTitle.setText(context.getString(R.string.app_update_remind));
        appUpdateVersion.setText(context.getString(R.string.app_last_version) + "(" + getUpgradeResult.getLatestVersion() + ")");
        okBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                showDownloadDialog();
            }

        });
        Button cancelBt = (Button) dialog.findViewById(R.id.cancel_btn);
        cancelBt.setText(context.getString(R.string.not_upgrade));
        cancelBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
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
        Button okBt = (Button) dialog.findViewById(R.id.ok_btn);
        okBt.setText(context.getString(R.string.upgrade));
        TextView text = (TextView) dialog.findViewById(R.id.text);
        text.setText(upgradeMsg);
        TextView appUpdateTitle = (TextView) dialog.findViewById(R.id.app_update_title);
        appUpdateTitle.setText(context.getString(R.string.app_update_remind));
        TextView appUpdateVersion = (TextView) dialog.findViewById(R.id.app_update_version);
        appUpdateVersion.setText(context.getString(R.string.app_last_version) + "(" + getUpgradeResult.getLatestVersion() + ")");
        okBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        Button cancelBt = (Button) dialog.findViewById(R.id.cancel_btn);
        cancelBt.setText(context.getString(R.string.exit));
        cancelBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                ((MyApplication) context.getApplicationContext()).exit();
            }
        });
        if (context != null) {
            dialog.show();
        }
    }

    private void showDownloadDialog() {
        // TODO Auto-generated method stub
        cancelUpdate = false;
        mDownloadDialog = new MyDialog(context,
                R.layout.dialog_app_update_progress);
        mDownloadDialog.setCancelable(false);
        ratioText = (TextView) mDownloadDialog.findViewById(R.id.ratio_text);
        Button cancelBt = (Button) mDownloadDialog.findViewById(R.id.cancel_bt);
        cancelBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDownloadDialog.dismiss();
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
                            if (mDownloadDialog != null
                                    && mDownloadDialog.isShowing()) {
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

    /**
     * 安装APK文件
     */
    public void installApk() {
        File apkfile = new File(DOWNLOAD_PATH, "update.apk");
        if (!apkfile.exists()) {
            ToastUtils.show(context, R.string.update_fail);
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        // 更新后启动
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        context.startActivity(i);
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
        WebServiceMiddleUtils.hand(context, error, errorCode);
//		WebServiceMiddleUtils.hand(context, error, upgradeHandler,
//				UPGRADE_FAIL);
    }

}
