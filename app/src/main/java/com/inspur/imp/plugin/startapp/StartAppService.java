/**
 * StartAppService.java
 * classes : com.inspur.imp.plugin.startapp.StartAppService
 * V 1.0.0
 * Create at 2016年9月18日 上午9:57:30
 */
package com.inspur.imp.plugin.startapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.inspur.imp.plugin.ImpPlugin;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * com.inspur.imp.plugin.startapp.StartAppService create at 2016年9月18日 上午9:57:30
 */
public class StartAppService extends ImpPlugin {
    private static final int SHOW_PEOGRESS_LAODING_DLG = 0;
    private static final int DOWNLOAD = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int DOWNLOAD_FAIL = 5;
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
//                    AppUtils.installApk(getActivity(),MyAppConfig.LOCAL_DOWNLOAD_PATH,"impInstall.apk");
                    FileUtils.openFile(getActivity(), MyAppConfig.LOCAL_DOWNLOAD_PATH + "impInstall.apk");
                    break;
                case DOWNLOAD_FAIL:
                    if (downloadingDialog != null && downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    ToastUtils.show(getActivity(), getActivity().getString(R.string.download_fail));
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

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("open".equals(action)) {
            startApp(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 验证app是否已经安装
     *
     * @param packageName
     * @return
     */
    private boolean isAppInstall(String packageName) {
        PackageManager packageManager = getFragmentContext().getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    /**
     * 打开一个App
     *
     * @param paramsObject
     */
    private void startApp(JSONObject paramsObject) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        String packageName = JSONUtils.getString(paramsObject, "packageName", null);
        String targetActivity = JSONUtils.getString(paramsObject, "activityName", null);
        String action = JSONUtils.getString(paramsObject, "action", null);
        String dataUri = JSONUtils.getString(paramsObject, "dataUri", null);
        String intentUri = JSONUtils.getString(paramsObject, "intentUri", null);
        JSONObject intentParamsObj = JSONUtils.getJSONObject(paramsObject, "intentParam", null);
        String appUrl = JSONUtils.getString(paramsObject, "appUrl", "");
        String appInstallTips = JSONUtils.getString(paramsObject, "appInstallTips", "");
        JSONArray jsonArray = JSONUtils.getJSONArray(paramsObject, "category", new JSONArray());
        try {
            if (intentParamsObj != null) {
                Iterator<String> keys = intentParamsObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    bundle.putSerializable(key, (Serializable) intentParamsObj.get(key));
                }
            }
            if (!StringUtils.isBlank(packageName)) {
                if (!isAppInstall(packageName)) {
                    showInstallDialog(appUrl, appInstallTips);
                    return;
                }
                if (!StringUtils.isBlank(targetActivity)) {
                    ComponentName componet = new ComponentName(packageName, targetActivity);
                    intent.setComponent(componet);
                } else {
                    intent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
                }
            }
            if (!StringUtils.isBlank(intentUri)) {
                intent = Intent.parseUri(intentUri, 0);
            }
            if (!StringUtils.isBlank(dataUri)) {
                intent.setData(Uri.parse(dataUri));
            }
            if (!StringUtils.isBlank(action)) {
                intent.setAction(action);
            }
            //添加category
            for (int i = 0; i < jsonArray.length(); i++) {
                intent.addCategory(jsonArray.getString(i));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(bundle);
            getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show(getFragmentContext(), R.string.react_native_app_open_failed);
        }
    }

    /**
     * 弹出未安装提示，用户点击确定则下载
     *
     * @param appUrl
     * @param appInstallTips
     */
    private void showInstallDialog(final String appUrl, String appInstallTips) {
        new MyQMUIDialog.MessageDialogBuilder(getActivity())
                .setMessage(appInstallTips)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        showDownloadDialog(appUrl);
                    }
                })
                .show();
    }

    /**
     * 展示下载Dialog
     *
     * @param appUrl
     */
    private void showDownloadDialog(String appUrl) {
        downloadingDialog = new MyDialog(getActivity(), R.layout.app_dialog_update_progress);
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
            params.setSaveFilePath(MyAppConfig.LOCAL_DOWNLOAD_PATH + "impInstall.apk");
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

    @Override
    public void onDestroy() {
    }
}
