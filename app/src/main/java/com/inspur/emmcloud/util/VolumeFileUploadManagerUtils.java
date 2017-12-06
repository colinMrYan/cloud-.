package com.inspur.emmcloud.util;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.bean.Volume.VolumeFileUploadInfo;
import com.inspur.emmcloud.callback.ProgressCallback;
import com.inspur.emmcloud.callback.VolumeFileUploadService;
import com.inspur.emmcloud.util.oss.OssService;
import com.inspur.emmcloud.util.oss.STSGetter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 云盘文件上传管理类
 */

public class VolumeFileUploadManagerUtils {
    private static VolumeFileUploadManagerUtils instance;
    private List<VolumeFileUploadInfo> volumeFileUploadInfoList = new ArrayList<>();
    private MyAppAPIService apiService;

    public static VolumeFileUploadManagerUtils getInstance(){
        if (instance == null){
            synchronized (VolumeFileUploadManagerUtils.class){
                if (instance == null){
                    instance = new VolumeFileUploadManagerUtils();
                }
            }
        }
        return instance;
    }

    public VolumeFileUploadManagerUtils(){
        apiService = new MyAppAPIService(MyApplication.getInstance());
        apiService.setAPIInterface(new WebService());
    }

    /**
     * 上传文件
     * @param mockVolumeFile
     * @param localFilePath
     * @param volumeFileParentPath
     */
    public void uploadFile(VolumeFile mockVolumeFile,String localFilePath,String volumeFileParentPath){
        File file = new File(localFilePath);
        VolumeFileUploadInfo volumeFileUploadInfo =new VolumeFileUploadInfo(null,mockVolumeFile,volumeFileParentPath,null,localFilePath);
        volumeFileUploadInfoList.add(volumeFileUploadInfo);
        apiService.getVolumeFileUploadToken(file.getName(),volumeFileParentPath,localFilePath,mockVolumeFile);
    }

    /**
     * 重新上传
     * @param mockVolumeFile
     */
    public void reUploadFile(VolumeFile mockVolumeFile){
        VolumeFileUploadInfo targetVolumeFileUploadInfo = null;
        for (int i = 0; i< volumeFileUploadInfoList.size(); i++){
            VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
            VolumeFile volumeFile = volumeFileUploadInfo.getVolumeFile();
            if (volumeFile == mockVolumeFile){
                targetVolumeFileUploadInfo =volumeFileUploadInfo ;
                break;
            }
        }
        if (targetVolumeFileUploadInfo != null){
            apiService.getVolumeFileUploadToken(mockVolumeFile.getName(),targetVolumeFileUploadInfo.getVolumeFileParentPath(),targetVolumeFileUploadInfo.getLocalFilePath(),mockVolumeFile);
        }

    }

    /**
     * 获取云盘此文件夹目录下正在上传的云盘文件
     * @param volumeId
     * @param dirPath
     * @return
     */
    public List<VolumeFile> getCurrentForderUploadingVolumeFile(String volumeId,String volumeFileParentPath){
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i< volumeFileUploadInfoList.size(); i++){
            VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
            VolumeFile volumeFile = volumeFileUploadInfo.getVolumeFile();
            if (volumeFileUploadInfo.getVolumeFileParentPath().equals(volumeFileParentPath) && volumeFile.getVolume().equals(volumeId)){
                volumeFileList.add(volumeFile);
            }
        }
        return volumeFileList;

    }

    /**
     * 移除上传服务
     * @param mockVolumeFile
     */
    public void removeVolumeFileUploadService(VolumeFile mockVolumeFile){
        for (int i = 0; i< volumeFileUploadInfoList.size(); i++){
            VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
            if (volumeFileUploadInfo.getVolumeFile() == mockVolumeFile){
                VolumeFileUploadService volumeFileUploadService = volumeFileUploadInfo.getVolumeFileUploadService();
                if (volumeFileUploadService != null){
                    volumeFileUploadService.onDestory();
                }
                volumeFileUploadInfoList.remove(i);
                break;
            }
        }
    }

    /**
     * 设置上传callback
     * @param volumeFile
     * @param progressCallback
     */
    public void setOssUploadProgressCallback(VolumeFile volumeFile,ProgressCallback progressCallback){
        for (int i = 0; i< volumeFileUploadInfoList.size(); i++){
            VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
            if (volumeFileUploadInfo.getVolumeFile() == volumeFile){
                VolumeFileUploadService volumeFileUploadService = volumeFileUploadInfo.getVolumeFileUploadService();
                volumeFileUploadInfo.setProgressCallback(progressCallback);
                //如果volumeFileUploadService已存在，则给volumeFileUploadService设置ProgressCallback
                if (volumeFileUploadService != null){
                    volumeFileUploadService.setProgressCallback(progressCallback);
                }
            }
        }
    }


    /**
     * 初始化一个OssService用来上传下载
     * @param context
     * @param getVolumeFileUploadTokenResult
     * @param mockVolumeFile
     * @return
     */
    public OssService initOSS(Context context, GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, VolumeFile mockVolumeFile) {
        OSSCredentialProvider credentialProvider = new STSGetter(getVolumeFileUploadTokenResult);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(context, getVolumeFileUploadTokenResult.getEndpoint(), credentialProvider, conf);
        return new OssService(oss, getVolumeFileUploadTokenResult,mockVolumeFile);

    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, String fileLocalPath,VolumeFile mockVolumeFile) {
            VolumeFileUploadService volumeFileUploadService = initOSS(MyApplication.getInstance(), getVolumeFileUploadTokenResult,mockVolumeFile);
            for (int i = 0; i< volumeFileUploadInfoList.size(); i++){
                VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
                if (volumeFileUploadInfo.getVolumeFile() == mockVolumeFile){
                    //如果ProgressCallback已经从ui传递进来，则给volumeFileUploadService设置ProgressCallback
                    ProgressCallback progressCallback = volumeFileUploadInfo.getProgressCallback();
                    if (progressCallback != null){
                        volumeFileUploadService.setProgressCallback(progressCallback);
                    }
                    volumeFileUploadInfo.setVolumeFileUploadService(volumeFileUploadService);
                    volumeFileUploadService.uploadFile(getVolumeFileUploadTokenResult.getFileName(),fileLocalPath);
                    break;
                }
            }
        }

        @Override
        public void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile,String error, int errorCode, String filePath) {
            for (int i = 0; i< volumeFileUploadInfoList.size(); i++){
                VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
                if (volumeFileUploadInfo.getVolumeFile() == mockVolumeFile){
                    //如果ProgressCallback已经从ui传递进来，则给volumeFileUploadService设置ProgressCallback
                    ProgressCallback progressCallback = volumeFileUploadInfo.getProgressCallback();
                    volumeFileUploadInfo.getVolumeFile().setStatus(VolumeFile.STATUS_UPLOADIND_FAIL);
                    if (progressCallback != null){
                        progressCallback.onFail();
                    }
                    break;
                }
            }
        }
    }
}
