package com.inspur.reactnative;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.FileSafeCode;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.UnZipAssets;
import com.inspur.emmcloud.util.ZipUtils;

import org.xutils.common.Callback;

import java.io.File;
import java.io.IOException;

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
    public static final int REACT_NATIVE_REVERT = 2;
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
    public static void initReactNative(Context context,String userId) {
        boolean isBundleExist = checkIsBundleExist(context,userId);
        String filePath = context.getFilesDir().getPath();
        if(!isBundleExist){
            try {
                UnZipAssets.unZip(context,"bundle-v0.1.0.android.zip",filePath+"/current"+userId,true);
                LogUtils.YfcDebug("解压bundle到Current文件夹下");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查bundle文件是否存在
     */
    private static boolean checkIsBundleExist(Context context,String userId) {
        String filePath = context.getFilesDir().getPath();
        File file = new File(filePath+"current"+userId+"/index.android.bundle");
        if(file.exists()){
            return  true;
        }
        return false;
    }

    /**
     * 比较更新时间是否超过半小时
     * @param lastUpdateTime
     * @return
     */
    public static boolean moreThanHalfHour(String lastUpdateTime){
        //为什么延迟半小时
        return true;
    }

    /**
     * 检查文件完整性
     * @param localMd5
     * @param zipFilePath
     * @return
     */
    public static  boolean isCompleteZip(String localMd5,String zipFilePath){
//        File file = new File(zipFilePath);
//        LogUtils.YfcDebug("验证sha256本地值："+localMd5+"文件值："+ FileSafeCode.getFileSHA256(file));
        File file = new File(zipFilePath);
        try {
            String zipSha256 = FileSafeCode.getFileSHA256(file);
            return localMd5.equals(zipSha256);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载zip更新包
     * @param context
     */
    public static void downLoadZipFile(final Context context, final ReactNativeUpdateBean reactNativeUpdateBean, final String userId){
        final DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
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
                LogUtils.YfcDebug("下载中l："+l);
                LogUtils.YfcDebug("下载中l1"+l1);
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
                String filePath = context.getFilesDir().getPath();
                if(ReactNativeFlow.isCompleteZip(reactNativeUpdateBean.getBundle().getAndroidHash(),
                        MyAppConfig.LOCAL_DOWNLOAD_PATH+"/"+userId+"/"+reactNativeUpdateBean.getBundle().getAndroidUri())){
                    LogUtils.YfcDebug("下载的是一个完整的包");
                    moveFolder(filePath+"/current"+userId,filePath+"/temp"+userId);
                    deleteZipFile(filePath+"/current"+userId);
                    ZipUtils.upZipFile(MyAppConfig.LOCAL_DOWNLOAD_PATH+"/"+userId+"/"+
                            reactNativeUpdateBean.getBundle().getAndroidUri(),
                            filePath+"/current"+userId);
                    FileUtils.deleteFile(MyAppConfig.LOCAL_DOWNLOAD_PATH+"/"+userId+"/"+
                            reactNativeUpdateBean.getBundle().getAndroidUri());
                    Intent intent = new Intent("com.inspur.react.success");
                    context.sendBroadcast(intent);
                }
                else{
                    //如何处理？重新下载不行，progressCallback不能再当参数传入
                }

            }
        };

        downLoaderUtils.startDownLoad(APIUri.getZipUrl()+reactNativeUpdateBean.getBundle().getAndroidUri(),
                MyAppConfig.LOCAL_DOWNLOAD_PATH+"/"+userId+"/"+reactNativeUpdateBean.getBundle().getAndroidUri(),
                progressCallback);
        LogUtils.YfcDebug("下载地址："+APIUri.getZipUrl()+reactNativeUpdateBean.getBundle().getAndroidUri());
//        downLoaderUtils.startDownLoad("http://10.24.13.151:8080/default.zip","/sdcard/IMP-Cloud/cache/cloud/default.zip",progressCallback);
    }

    /**
     * 移动目录
     * @param srcDirName
     * @param destDirName
     * @return
     */
    public static boolean moveFolder(String srcDirName, String destDirName){
        return FileUtils.copyFolder(srcDirName,destDirName);
    }

    /**
     * 判断状态
     * @return
     */
    public static int checkReactNativeOperation (String state){
        if(state.equals("RESET")){
            return REACT_NATIVE_RESET;
        }else if(state.equals("ROLLBACK")){
            return REACT_NATIVE_REVERT;
        }else if(state.equals("FORWARD")){
            return REACT_NATIVE_FORWORD;
        }else if(state.equals("STANDBY")){
            return REACT_NATIVE_NO_UPDATE;
        }
        return  REACT_NATIVE_UNKNOWN;
    }

    /**
     * 检测bundle文件是否存在
     * @param bundleFilePath
     * @return
     */
    public static boolean checkBundleFileIsExist(String bundleFilePath){
        File file = new File(bundleFilePath);
        return  file.exists();
    }

    /**
     * 解压文件
     * @param zipFile
     * @param folderPath
     * @return
     */
    public static boolean unZipFile(String zipFile, String folderPath){
        return ZipUtils.upZipFile(zipFile,folderPath);
    }


    /**
     * 解压assets下文件
     * @param context
     * @param assetName
     * @param outputDirectory
     * @param isReWrite
     * @return
     */
    public static boolean unZipFile(Context context, String assetName,
                                    String outputDirectory, boolean isReWrite){
        boolean unZipSuccess = false;
        try {
            UnZipAssets.unZip(context,assetName,outputDirectory,isReWrite);
            unZipSuccess = true;
        } catch (IOException e) {
            unZipSuccess = false;
            e.printStackTrace();
        }
        return unZipSuccess;
    }


    /**
     * 删除zip文件  临时需求，以后可能不再使用
     * @param deletePath
     * @return
     */
    public static boolean deleteZipFile(String deletePath){
        return FileUtils.deleteFile(deletePath);
    }


}
