package com.inspur.emmcloud.util.privates;

import android.app.Activity;

import com.github.zafarkhaja.semver.Version;
import com.hjq.toast.ToastUtils;
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
    private static final int TYPE_OFFLINE_APP_ILLEGAL = 0; //不合法
    private static final int TYPE_OFFLINE_APP_INTEGER = 1;  //int类型
    private static final int TYPE_OFFLINE_APP_THREE_LEGAL = 2; // “1.0.0” 三段式

    public static void handleOfflineWeb(final Activity activity, final App app) {
        LoadingDialog loadingDlg = new LoadingDialog(activity);
        String versionName = app.getVersion();
        if (isVersionLegal(versionName) == TYPE_OFFLINE_APP_ILLEGAL) {
            ToastUtils.show("版本不合法");
            return;
        }
        final String userId = BaseApplication.getInstance().getUid();

        String preOfflineAppZipFilePath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + userId + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/";

        List<File> fileList = FileUtils.getSubFileList(preOfflineAppZipFilePath);

        if (fileList.size() > 0) {      //判断目录是否有离线版本
            String defaultVersionName = fileList.get(0).getName();
            //ex:第一次传入整数值  第二次传入三段式
            if (isVersionLegal(defaultVersionName) != isVersionLegal(app.getVersion())) {
                ToastUtils.show("版本不合法");
                return;
            }
            switch (isVersionLegal(defaultVersionName)) {   //分情况处理版本类型
                case TYPE_OFFLINE_APP_INTEGER:
                    int versionCode = Integer.parseInt(defaultVersionName);
                    for (int i = 1; i < fileList.size(); i++) {     //取出最新版本
                        String itemName = fileList.get(i).getName();
                        if (Integer.parseInt(itemName) > versionCode) {
                            versionCode = Integer.parseInt(itemName);
                        }
                    }
                    if (versionCode != Integer.parseInt(app.getVersion())) {
                        downLoadZip(activity, app, loadingDlg);
                    }
                    break;
                case TYPE_OFFLINE_APP_THREE_LEGAL:
                    Version version = Version.valueOf(defaultVersionName);
                    for (int i = 1; i < fileList.size(); i++) {     //取出最新版本
                        String itemName = fileList.get(i).getName();
                        if (version.compareTo(Version.valueOf(itemName)) > 0) {
                            version = Version.valueOf(itemName);
                        }
                    }
                    if (!version.getNormalVersion().equals(app.getVersion())) { //如果有版本更新  去下载
                        downLoadZip(activity, app, loadingDlg);
                    }
                    break;
            }

            UriUtils.openUrl(activity, app.getUri(), app.getAppName(), app.getUserHeader() == 1);  //先打开最新版本
        } else {
            loadingDlg.show();      //第一次显示loading  其他不显示
            downLoadZip(activity, app, loadingDlg, true);
        }
    }

    //判断版本号类型  0：非法  1：int值  2：“1.0.0” 类型
    private static int isVersionLegal(String versionName) {
        try {
            if (versionName.contains(".")) {
                Version.valueOf(versionName);   //如传入“1.0” 会导致异常
                return TYPE_OFFLINE_APP_THREE_LEGAL;
            } else {
                Integer.parseInt(versionName);
                return TYPE_OFFLINE_APP_INTEGER;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TYPE_OFFLINE_APP_ILLEGAL;
    }

    private static void downLoadZip(final Activity activity, final App app, final LoadingDialog loadingDialog) {
        downLoadZip(activity, app, loadingDialog, false);
    }

    private static void downLoadZip(final Activity activity, final App app, final LoadingDialog loadingDialog, final boolean isFirst) {

        final String userId = BaseApplication.getInstance().getUid();
        final String offlineAppZipFilePath = MyAppConfig.LOCAL_OFFLINE_APP_PATH + "/" + userId + "/" +
                BaseApplication.getInstance().getTanent() + "/" + app.getAppID() + "/" + app.getVersion();
        APIDownloadCallBack progressCallback = new APIDownloadCallBack(activity, app.getInstallUri()) {
            @Override
            public void callbackSuccess(File file) {
                loadingDialog.dismiss();
                try {
                    ZipUtils.upZipFile(file, offlineAppZipFilePath);
                    FileUtils.deleteFile(file.getAbsolutePath());

                    if (isFirst) {
                        UriUtils.openUrl(activity, app.getUri(), app.getAppName(), app.getUserHeader() == 1);
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
                loadingDialog.dismiss();
            }
        };

        //开启下载
        String saveZipPath = offlineAppZipFilePath + "app.zip";
        DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
        downLoaderUtils.startDownLoad(app.getInstallUri(), saveZipPath, progressCallback);
    }
}
