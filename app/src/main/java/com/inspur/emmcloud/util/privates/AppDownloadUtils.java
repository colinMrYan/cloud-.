package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * Created by chenmch on 2018/10/15.
 */

public class AppDownloadUtils {
    private static final int SHOW_PEOGRESS_LAODING_DLG = 0;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int DOWNLOAD_FAIL = 5;
    private Activity activity;
    private long totalSize;
    private long downloadSize;
    private int progressSize;
    private MyDialog downloadingDialog;
    private TextView progressTv;
    Handler downloadWeBexHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD:
                    if (progressTv != null) {
                        progressTv.setText(progressSize + "%," + "  " + AppUtils.getKBOrMBFormatString(downloadSize) + "/"
                                + AppUtils.getKBOrMBFormatString(totalSize));
                    }
                    break;
                case DOWNLOAD_FINISH:
                    if (downloadingDialog != null && downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
//                    AppUtils.installApk(MyApplication.getInstance(), MyAppConfig.LOCAL_DOWNLOAD_PATH, "webex.apk");
                    FileUtils.openFile(MyApplication.getInstance(), MyAppConfig.LOCAL_DOWNLOAD_PATH + "webex.apk");
                    break;
                case DOWNLOAD_FAIL:
                    if (downloadingDialog != null && downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    ToastUtils.show(MyApplication.getInstance(), activity.getString(R.string.download_fail));
                    break;
                case SHOW_PEOGRESS_LAODING_DLG:
                    if (downloadingDialog != null && !downloadingDialog.isShowing()) {
                        downloadingDialog.show();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private Callback.Cancelable cancelableDownloadRequest;

    /**
     * 展示下载Dialog
     *
     * @param appUrl
     */
    public void showDownloadDialog(Activity activity, String appUrl) {
        this.activity = activity;
        downloadingDialog = new MyDialog(activity, R.layout.app_dialog_update_progress);
        downloadingDialog.setCancelable(false);
        progressTv = (TextView) downloadingDialog.findViewById(R.id.ratio_text);
        Button cancelBtn = (Button) downloadingDialog.findViewById(R.id.cancel_bt);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancelableDownloadRequest != null) {
                    cancelableDownloadRequest.cancel();
                }
                if (downloadingDialog != null && downloadingDialog.isShowing()) {
                    downloadingDialog.dismiss();
                }
            }
        });
        // 下载apk文件
        downloadWeBexApk(appUrl);
    }

    /**
     * 下载安装包
     */
    private void downloadWeBexApk(String appUrl) {
        // 判断SD卡是否存在，并且是否具有读写权限
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            RequestParams params = new RequestParams(appUrl);
            params.setSaveFilePath(MyAppConfig.LOCAL_DOWNLOAD_PATH + "webex.apk");
            cancelableDownloadRequest = x.http().get(params,
                    new Callback.ProgressCallback<File>() {

                        @Override
                        public void onCancelled(CancelledException arg0) {
                        }

                        @Override
                        public void onError(Throwable arg0, boolean arg1) {
                            sendCallBackMessage(DOWNLOAD_FAIL);
                        }

                        @Override
                        public void onFinished() {
                        }

                        @Override
                        public void onSuccess(File arg0) {
                            sendCallBackMessage(DOWNLOAD_FINISH);
                        }

                        @Override
                        public void onLoading(long arg0, long arg1, boolean arg2) {
                            totalSize = arg0;
                            downloadSize = arg1;
                            progressSize = (int) (((float) arg1 / arg0) * 100);
                            // 更新进度
                            if (downloadingDialog != null && downloadingDialog.isShowing()) {
                                sendCallBackMessage(DOWNLOAD);
                            }
                        }

                        @Override
                        public void onStarted() {
                            sendCallBackMessage(SHOW_PEOGRESS_LAODING_DLG);
                        }

                        @Override
                        public void onWaiting() {
                        }
                    });
        } else {
            sendCallBackMessage(DOWNLOAD_FAIL);
        }
    }

    /**
     * 发送回调消息给主线程
     *
     * @param downloadState
     */
    private void sendCallBackMessage(int downloadState) {
        if (downloadWeBexHandler != null) {
            downloadWeBexHandler.sendEmptyMessage(downloadState);
        }
    }
}
