package com.inspur.reactnative;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ZipUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.appcenter.ReactNativeUpdateBean;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.FileSafeCode;

import java.io.File;

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
        String reactCurrentFilePath = MyAppConfig.getReactAppFilePath(context, userId, "discover");
        if (!isBundleExist) {
            ZipUtils.unZip(context, "bundle-inspur_esg-v0.3.1-beta7-.android.zip", reactCurrentFilePath, true);
//                PreferencesUtils.putString(context, "react_native_lastupdatetime", System.currentTimeMillis() + "");//隔半小时检查逻辑
        }
    }

    /**
     * 检查bundle文件是否存在
     */
    private static boolean checkIsBundleExist(Context context, String userId) {
        String filePath = MyAppConfig.getReactAppFilePath(context, userId, "discover");
        File file = new File(filePath + "/index.android.bundle");
        return file.exists();
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
        final String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + "/" + userId + "/" + reactNativeUpdateBean.getBundle().getAndroidUri();
        APIDownloadCallBack progressCallback = new APIDownloadCallBack(context, APIUri.getZipUrl()) {
            @Override
            public void callbackStart() {

            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {

            }

            @Override
            public void callbackSuccess(File file) {
                updateNewVersion(context, reactNativeUpdateBean, userId);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {

            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };
        String source = APIUri.getZipUrl() + reactNativeUpdateBean.getBundle().getAndroidUri();
        new DownLoaderUtils().startDownLoad(source, reactZipFilePath, progressCallback);
    }

    /**
     * 下载完成之后更新react版本
     *
     * @param context
     * @param reactNativeUpdateBean
     * @param userId
     */
    private static void updateNewVersion(Context context, ReactNativeUpdateBean reactNativeUpdateBean, String userId) {
        String reactCurrentPath = MyAppConfig.getReactAppFilePath(context, userId, "discover");
        String reactTempPath = MyAppConfig.getReactTempFilePath(context, userId);
        String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + userId + "/" +
                reactNativeUpdateBean.getBundle().getAndroidUri();
        //出现文件问题和hash值验证问题时打开这里调试
//        LogUtils.YfcDebug("reactZipFilepath："+reactZipFilePath);
//        LogUtils.YfcDebug("网络返回的hash："+reactNativeUpdateBean.getBundle().getAndroidHash());
//        LogUtils.YfcDebug("文件目录下的hash："+FileSafeCode.getFileSHA256(new File(reactZipFilePath)));
        if (ReactNativeFlow.isCompleteZip(reactNativeUpdateBean.getBundle().getAndroidHash(), reactZipFilePath)) {
            FileUtils.copyFolder(reactCurrentPath, reactTempPath);
            FileUtils.deleteFile(reactCurrentPath);
            ZipUtils.upZipFile(reactZipFilePath, reactCurrentPath);
            FileUtils.deleteFile(reactZipFilePath);
//            PreferencesUtils.putString(context, "react_native_lastupdatetime", "" + System.currentTimeMillis());//隔半小时检查更新逻辑相关
            FindFragment.hasUpdated = true;
            Intent intent = new Intent("com.inspur.react.success");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } else {
            AppExceptionCacheUtils.saveAppException(context, 3, reactZipFilePath, "discover download not compelete error", 0);
        }
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
     * 以UTF-8编码读取传入文件夹下bundle.json文件里的信息
     *
     * @param reactAppFilePath
     * @return
     */
    public static StringBuilder getBundleDotJsonFromFile(String reactAppFilePath) {
        return FileUtils.readFile(reactAppFilePath + "/bundle.json", "UTF-8");
    }

    /**
     * 从Scheme里获取app的module
     * scheme形式：'ecc-app-react-native: //10002'
     *
     * @param reactNativeApp
     * @return
     */
    public static String getAppModuleFromScheme(String reactNativeApp) {
        return reactNativeApp.split("//")[1];
    }


}
