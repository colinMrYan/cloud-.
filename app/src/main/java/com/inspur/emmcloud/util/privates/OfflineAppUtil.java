package com.inspur.emmcloud.util.privates;

import android.app.Activity;

import com.github.zafarkhaja.semver.Version;
import com.hjq.toast.ToastUtils;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ZipUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.appcenter.App;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OfflineAppUtil {
    private static final int OFFLINE_APP_VERSION_TYPE_ILLEGAL = 0; //不合法
    private static final int OFFLINE_APP_VERSION_TYPE_INTEGER = 1;  //int类型
    private static final int OFFLINE_APP_VERSION_TYPE_STANDARD = 2; // “1.0.0” 三段式

    public static void handleOfflineWeb(final Activity activity, final App app) {
        final String userId = BaseApplication.getInstance().getUid();
        String preOfflineAppZipFilePath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + userId + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/";
        List<File> fileFolderList = FileUtils.getSubFileForderList(preOfflineAppZipFilePath);
        int offlineAppVersionType = getOfflineAppVersionType(app.getVersion());
        if (offlineAppVersionType == OFFLINE_APP_VERSION_TYPE_ILLEGAL) {
            ToastUtils.show(R.string.react_native_app_open_failed);
        } else {
            File file = getMaxVersionAppFile(fileFolderList, offlineAppVersionType);
            if (file == null) {
                downLoadZip(activity, app, true);
            } else {
                openOfflineApp(activity, app, file.getAbsolutePath());
                if (compareTo(app.getVersion(), file.getName(), offlineAppVersionType)) {
                    downLoadZip(activity, app, false);
                }
            }
        }
    }

    private static File getMaxVersionAppFile(List<File> fileList, int offlineAppVersionType) {
        File maxVersionAppFile = null;
        for (File file : fileList) {
            if (getOfflineAppVersionType(file.getName()) == offlineAppVersionType) {
                if (maxVersionAppFile == null) {
                    maxVersionAppFile = file;
                } else if (compareTo(file.getName(), maxVersionAppFile.getName(), offlineAppVersionType)) {
                    maxVersionAppFile = file;
                }
            }
        }
        return maxVersionAppFile;
    }

    //判断版本号类型  0：非法  1：int值  2：“1.0.0” 类型
    private static int getOfflineAppVersionType(String versionName) {
        try {
            if (versionName.contains(".")) {
                Version.valueOf(versionName);   //如传入“1.0” 会导致异常
                return OFFLINE_APP_VERSION_TYPE_STANDARD;
            } else {
                Integer.parseInt(versionName);
                return OFFLINE_APP_VERSION_TYPE_INTEGER;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return OFFLINE_APP_VERSION_TYPE_ILLEGAL;
    }

    /**
     * 比较两个版本号的大小
     *
     * @param version1
     * @param version2
     * @param offlineAppVersionType
     * @return 如果Version1>version2返回true,否则返回false
     */
    private static boolean compareTo(String version1, String version2, int offlineAppVersionType) {
        if (offlineAppVersionType == OFFLINE_APP_VERSION_TYPE_INTEGER) {
            return Integer.valueOf(version1) > Integer.valueOf(version2);
        }
        return (Version.valueOf(version1).compareTo(Version.valueOf(version2)) > 0);
    }


    private static void downLoadZip(final Activity activity, final App app, final boolean isShowLoadingDlg) {
        final LoadingDialog loadingDlg = new LoadingDialog(activity);
        loadingDlg.show(isShowLoadingDlg);
        final String userId = BaseApplication.getInstance().getUid();
        final String offlineAppZipFilePath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + userId + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/" + app.getVersion();
        APIDownloadCallBack progressCallback = new APIDownloadCallBack(activity, app.getInstallUri()) {
            @Override
            public void callbackSuccess(File file) {
                LoadingDialog.dimissDlg(loadingDlg);
                try {
                    ZipUtils.upZipFile(file, offlineAppZipFilePath);
                    FileUtils.deleteFile(file.getAbsolutePath());
                    if (isShowLoadingDlg) {
                        openOfflineApp(activity, app, offlineAppZipFilePath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                super.callbackLoading(total, current, isUploading);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                LoadingDialog.dimissDlg(loadingDlg);
            }
        };

        //开启下载
        String saveZipPath = offlineAppZipFilePath + "app.zip";
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
                if (!file.getAbsolutePath().equals(folderPath)) {
                    FileUtils.deleteFile(file.getAbsolutePath());
                }
            }
        } else {
            ToastUtils.show(R.string.react_native_app_open_failed);
        }


    }
}
