package com.inspur.reactnative;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.FileSafeCode;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UnZipAssets;
import com.inspur.emmcloud.util.ZipUtils;

import org.xutils.common.Callback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by yufuchang on 2017/2/21.
 */

public class ReactNativeFlow {

    /**
     * 发生未知错误：与重置相同
     */
    public static final int REACT_NATIVE_UNKNOWN = 0;
    /**
     * 重置：恢复React到刚安装的版本，删掉temp，current，解压assets下的zip到current
     */
    public static final int REACT_NATIVE_RESET = 1;
    /**
     * 回滚：恢复到上一版本，把temp移动到current
     */
    public static final int REACT_NATIVE_ROLLBACK = 2;
    /**
     * 升级版本：下载zip，验证完整性（不完整重新下载），旧版本移动到temp，新版本解压到current
     */
    public static final int REACT_NATIVE_FORWORD = 3;
    /**
     * 默认：不更新
     */
    public static final int REACT_NATIVE_NO_UPDATE = 4;

    /**
     * 初始化ReactNative,为第一次加载准备资源
     */
    public static void initReactNative(Context context, String userId) {
        boolean isBundleExist = checkIsBundleExist(context, userId);
        String reactCurrentFilePath = MyAppConfig.getReactAppFilePath(context, userId,"discover");
        if (!isBundleExist) {
            try {
                UnZipAssets.unZip(context, "bundle-v0.1.0.android.zip", reactCurrentFilePath, true);
                PreferencesUtils.putString(context, "react_native_lastupdatetime", System.currentTimeMillis() + "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查bundle文件是否存在
     */
    private static boolean checkIsBundleExist(Context context, String userId) {
        String filePath = MyAppConfig.getReactAppFilePath(context, userId,"discover");
        File file = new File(filePath + "/index.android.bundle");
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 比较检查更新时间是否超过半小时
     *
     * @param lastUpdateTime
     * @return
     */
    public static boolean moreThanHalfHour(String lastUpdateTime) {
        if (StringUtils.isBlank(lastUpdateTime)) {
            return false;
        }
        long halfHour = System.currentTimeMillis() - Long.parseLong(lastUpdateTime);
        long halfHourMill = 30 * 60 * 1000;
        return halfHour > halfHourMill;
    }

    /**
     * 检查文件完整性
     *
     * @param localSHA256
     * @param zipFilePath
     * @return
     */
    public static boolean isCompleteZip(String localSHA256, String zipFilePath) {
        File file = new File(zipFilePath);
        try {
            String zipSha256 = FileSafeCode.getFileSHA256(file);
            return localSHA256.equals(zipSha256);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载zip更新包
     *
     * @param context
     */
    public static void downLoadZipFile(final Context context, final ReactNativeUpdateBean reactNativeUpdateBean, final String userId) {
        final DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
        String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + "/" + userId + "/" + reactNativeUpdateBean.getBundle().getAndroidUri();
        Callback.ProgressCallback<File> progressCallback = new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                LogUtils.YfcDebug("下载开始");
            }

            @Override
            public void onLoading(long l, long l1, boolean b) {
            }

            @Override
            public void onSuccess(File file) {

            }

            @Override
            public void onError(Throwable throwable, boolean b) {

            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {
                updateNewVersion(context, reactNativeUpdateBean, userId);
            }
        };
        downLoaderUtils.startDownLoad(APIUri.getZipUrl() + reactNativeUpdateBean.getBundle().getAndroidUri(),
                reactZipFilePath,
                progressCallback);
    }

    /**
     * 下载完成之后更新react版本
     *
     * @param context
     * @param reactNativeUpdateBean
     * @param userId
     */
    private static void updateNewVersion(Context context, ReactNativeUpdateBean reactNativeUpdateBean, String userId) {
        String reactCurrentPath = MyAppConfig.getReactAppFilePath(context, userId,"discover");
        String reactTempPath = MyAppConfig.getReactTempFilePath(context, userId);
        String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + "/" + userId + "/" +
                reactNativeUpdateBean.getBundle().getAndroidUri();
        if (ReactNativeFlow.isCompleteZip(reactNativeUpdateBean.getBundle().getAndroidHash(), reactZipFilePath)) {
            moveFolder(reactCurrentPath, reactTempPath);
            deleteOldVersionFile(reactCurrentPath);
            ZipUtils.upZipFile(reactZipFilePath, reactCurrentPath);
            FileUtils.deleteFile(reactZipFilePath);
            PreferencesUtils.putString(context, "react_native_lastupdatetime", "" + System.currentTimeMillis());
            FindFragment.hasUpdated = true;
            Intent intent = new Intent("com.inspur.react.success");
            context.sendBroadcast(intent);
        } else {
            FileUtils.deleteFile(reactZipFilePath);
            ToastUtils.show(context,context.getString(R.string.react_native_app_update_failed));
        }
    }

    /**
     * 移动目录
     *
     * @param srcDirName
     * @param destDirName
     * @return
     */
    public static boolean moveFolder(String srcDirName, String destDirName) {
        return FileUtils.copyFolder(srcDirName, destDirName);
    }


    /**
     * 判断react的状态，是更新，回退，重置还是保持
     *
     * @param state
     * @return
     */
    public static int checkReactNativeOperation(String state) {
        if (state.equals("RESET")) {
            return REACT_NATIVE_RESET;
        } else if (state.equals("ROLLBACK")) {
            return REACT_NATIVE_ROLLBACK;
        } else if (state.equals("FORWARD")) {
            return REACT_NATIVE_FORWORD;
        } else if (state.equals("STANDBY")) {
            return REACT_NATIVE_NO_UPDATE;
        }
        return REACT_NATIVE_UNKNOWN;
    }

    /**
     * 检测bundle文件是否存在
     *
     * @param bundleFilePath
     * @return
     */
    public static boolean checkBundleFileIsExist(String bundleFilePath) {
        File file = new File(bundleFilePath);
        return file.exists();
    }

    /**
     * 解压文件
     *
     * @param zipFile
     * @param folderPath
     * @return
     */
    public static boolean unZipFile(String zipFile, String folderPath) {
        return ZipUtils.upZipFile(zipFile, folderPath);
    }


    /**
     * 解压assets下文件
     *
     * @param context
     * @param assetName
     * @param outputDirectory
     * @param isReWrite
     * @return
     */
    public static boolean unZipFile(Context context, String assetName,
                                    String outputDirectory, boolean isReWrite) {
        boolean unZipSuccess = false;
        try {
            UnZipAssets.unZip(context, assetName, outputDirectory, isReWrite);
            unZipSuccess = true;
        } catch (IOException e) {
            unZipSuccess = false;
            e.printStackTrace();
        }
        return unZipSuccess;
    }

    /**
     * 检查assets目录下文件是否存在
     * @param context
     * @param assetsFileName
     * @return
     */
    public static boolean checkAssetsFileExits(Context context, String assetsFileName) {
        InputStream in = null;
        try {
            in = context.getResources().getAssets().open(assetsFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in != null;
    }


    /**
     * 删除原来版本文件
     *
     * @param deletePath
     * @return
     */
    public static boolean deleteOldVersionFile(String deletePath) {
        return FileUtils.deleteFile(deletePath);
    }

    /**
     * 检查clientID是否存在
     *
     * @param context
     * @return
     */
    public static boolean checkClientIdExist(Context context) {
        String clientId = PreferencesByUserUtils.getString(context, "react_native_clientid", "");
        if (StringUtils.isBlank(clientId)) {
            return false;
        }
        return true;
    }

    /**
     * 以UTF-8编码读取传入文件夹下bundle.json文件里的信息
     * @param reactAppFilePath
     * @return
     */
    public static StringBuilder getBundleDotJsonFromFile(String reactAppFilePath){
        return FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
    }

    /**
     * 从Scheme里获取app的module
     * scheme形式：'ecc-app-react-native: //10002'
     * @param reactNativeApp
     * @return
     */
    public static String getAppModuleFromScheme(String reactNativeApp){
       return reactNativeApp.split("//")[1];
    }

    /**
     * 删除下载的zip文件
     * @param deleteZipFilePath 删除路径如xxx/xxx/ECA.zip
     * @return
     */
    public static boolean deleteReactNativeDownloadZipFile(String deleteZipFilePath){
        return FileUtils.deleteFile(deleteZipFilePath);
    }

    /**
     * 清除ReactNative缓存
     * @param reactNativeInstallDir
     * @return
     */
    public static boolean deleteReactNativeInstallDir(String reactNativeInstallDir){
        return FileUtils.deleteFile(reactNativeInstallDir);
    }


}
