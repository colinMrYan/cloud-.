package com.inspur.emmcloud.util.privates;

import android.app.Activity;

import com.hjq.toast.ToastUtils;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ZipUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.App;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OfflineAppUtil {

    private static final String APP_DIR_TEMP = "app_zip_temp";

    public static void handleOfflineWeb(final Activity activity, final App app) {
        String preOfflineAppZipFilePath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + BaseApplication.getInstance().getUid() + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/";
        List<File> fileFolderList = FileUtils.getSubFileForderList(preOfflineAppZipFilePath);
        File file = getAppDirFile(fileFolderList);
        if (file == null || !file.getName().equals(app.getVersion())) {
            downLoadZip(activity, app, true);
        } else {
            openOfflineApp(activity, app, file.getAbsolutePath());
        }
    }

    private static File getAppDirFile(List<File> fileFolderList) {
        for (File file : fileFolderList) {
            if (file.isDirectory() && file.getName().equals(APP_DIR_TEMP)) ;
            {
                return file;
            }
        }
        return null;
    }


    private static void downLoadZip(final Activity activity, final App app, final boolean isShowLoadingDlg) {
        if (NetUtils.isNetworkConnected(activity)) {
            return;
        }
        final LoadingDialog loadingDlg = new LoadingDialog(activity);
        loadingDlg.show(isShowLoadingDlg);
        final String userId = BaseApplication.getInstance().getUid();
        final String offlineAppZipFileDirTempPath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + userId + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/" + APP_DIR_TEMP + "/";
        final String offlineAppZipFileDirPath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + BaseApplication.getInstance().getUid() + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/" + app.getVersion() + "/";
        final File offlineAppZipFileTempDir = new File(offlineAppZipFileDirTempPath);
        if (offlineAppZipFileTempDir.exists()) {
            FileUtils.deleteFile(offlineAppZipFileDirTempPath);
        }
        offlineAppZipFileTempDir.mkdirs();
        APIDownloadCallBack progressCallback = new APIDownloadCallBack(activity, app.getInstallUri()) {
            @Override
            public void callbackSuccess(File file) {
                LoadingDialog.dimissDlg(loadingDlg);
                try {
                    ZipUtils.upZipFile(file, offlineAppZipFileDirTempPath);
                    FileUtils.deleteFile(file.getAbsolutePath());

                    if (FileUtils.isFileExist(offlineAppZipFileDirPath)) {
                        FileUtils.deleteFile(offlineAppZipFileDirPath);
                    }
                    offlineAppZipFileTempDir.renameTo(new File(offlineAppZipFileDirPath));
                    if (isShowLoadingDlg) {
                        openOfflineApp(activity, app, offlineAppZipFileDirPath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.show(R.string.react_native_app_open_failed);
                }
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                super.callbackLoading(total, current, isUploading);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                LoadingDialog.dimissDlg(loadingDlg);
                ToastUtils.show(R.string.react_native_app_update_failed);
            }
        };

        //开启下载
        String saveZipPath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + BaseApplication.getInstance().getUid() + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/app.zip";
        DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
        downLoaderUtils.startDownLoad(app.getInstallUri(), saveZipPath, progressCallback);
    }


    private static void openOfflineApp(Activity activity, App app, String folderPath) {
        File indexFile = new File(folderPath + "/" + app.getUri());
        if (indexFile.exists()) {
            UriUtils.openUrl(activity, "file:" + indexFile.getAbsolutePath(), app.getAppName(), app.getUserHeader() == 1);
            File[] files = new File(MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + BaseApplication.getInstance().getUid() + "/" +
                    BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/").listFiles();
            for (File file : files) {
                if (!file.getAbsolutePath().equals(new File(folderPath).getAbsolutePath())) {
                    FileUtils.deleteFile(file.getAbsolutePath());
                }
            }
        } else {
            ToastUtils.show(R.string.react_native_app_open_failed);
        }


    }
}
