package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFileUploadInfo;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;
import com.inspur.emmcloud.util.privates.oss.OssService;

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

    public VolumeFileUploadManagerUtils() {
        apiService = new MyAppAPIService(MyApplication.getInstance());
        apiService.setAPIInterface(new WebService());
    }

    public static VolumeFileUploadManagerUtils getInstance() {
        if (instance == null) {
            synchronized (VolumeFileUploadManagerUtils.class) {
                if (instance == null) {
                    instance = new VolumeFileUploadManagerUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 上传文件
     *
     * @param mockVolumeFile
     * @param localFilePath
     * @param volumeFileParentPath
     */
    public void uploadFile(VolumeFile mockVolumeFile, String localFilePath, String volumeFileParentPath) {
        File file = new File(localFilePath);
        VolumeFileUploadInfo volumeFileUploadInfo = new VolumeFileUploadInfo(null, mockVolumeFile, volumeFileParentPath, null, localFilePath);
        volumeFileUploadInfoList.add(volumeFileUploadInfo);
        apiService.getVolumeFileUploadToken(file.getName(), volumeFileUploadInfo, mockVolumeFile);
    }

    /**
     * 重新上传
     *
     * @param mockVolumeFile
     */
    public void reUploadFile(VolumeFile mockVolumeFile) {
        VolumeFileUploadInfo targetVolumeFileUploadInfo = null;
        for (int i = 0; i < volumeFileUploadInfoList.size(); i++) {
            VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
            VolumeFile volumeFile = volumeFileUploadInfo.getVolumeFile();
            if (volumeFile == mockVolumeFile) {
                targetVolumeFileUploadInfo = volumeFileUploadInfo;
                //上传文件
                apiService.getVolumeFileUploadToken(mockVolumeFile.getName(), targetVolumeFileUploadInfo, mockVolumeFile);
                break;
            }
        }
    }

    /**
     * 获取云盘此文件夹目录下正在上传的云盘文件
     *
     * @param volumeId
     * @param volumeFileParentPath
     * @return
     */
    public List<VolumeFile> getCurrentForderUploadingVolumeFile(String volumeId, String volumeFileParentPath) {
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i < volumeFileUploadInfoList.size(); i++) {
            VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
            VolumeFile volumeFile = volumeFileUploadInfo.getVolumeFile();
            if (volumeFileUploadInfo.getVolumeFileParentPath().equals(volumeFileParentPath) && volumeFile.getVolume().equals(volumeId)) {
                volumeFileList.add(volumeFile);
            }
        }
        return volumeFileList;

    }

    /**
     * 移除上传服务
     *
     * @param mockVolumeFile
     */
    public void removeVolumeFileUploadService(VolumeFile mockVolumeFile) {
        if (mockVolumeFile != null) {
            for (int i = 0; i < volumeFileUploadInfoList.size(); i++) {
                VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
                if (volumeFileUploadInfo.getVolumeFile() == mockVolumeFile) {
                    VolumeFileUploadService volumeFileUploadService = volumeFileUploadInfo.getVolumeFileUploadService();
                    if (volumeFileUploadService != null) {
                        volumeFileUploadService.onDestory();
                    }
                    volumeFileUploadInfoList.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * 设置上传callback
     *
     * @param volumeFile
     * @param progressCallback
     */
    public void setOssUploadProgressCallback(VolumeFile volumeFile, ProgressCallback progressCallback) {
        for (int i = 0; i < volumeFileUploadInfoList.size(); i++) {
            VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
            if (volumeFileUploadInfo.getVolumeFile() == volumeFile) {
                VolumeFileUploadService volumeFileUploadService = volumeFileUploadInfo.getVolumeFileUploadService();
                volumeFileUploadInfo.setProgressCallback(progressCallback);
                //如果volumeFileUploadService已存在，则给volumeFileUploadService设置ProgressCallback
                if (volumeFileUploadService != null) {
                    volumeFileUploadService.setProgressCallback(progressCallback);
                }
            }
        }
    }

    /**
     * 根据不同的storage选择不同的存储服务
     *
     * @param getVolumeFileUploadTokenResult
     * @param mockVolumeFile
     * @return
     */
    private VolumeFileUploadService getVolumeFileUploadService(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, VolumeFile mockVolumeFile) {
        VolumeFileUploadService volumeFileUploadService = null;
        switch (getVolumeFileUploadTokenResult.getStorage()) {
            case "ali_oss":  //阿里云
                try {
                    volumeFileUploadService = new OssService(getVolumeFileUploadTokenResult, mockVolumeFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "aws_s3":
                break;
            default:
                break;
        }
        return volumeFileUploadService;
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, String fileLocalPath, VolumeFile mockVolumeFile) {
            LogUtils.jasonDebug("getXPath()==" + getVolumeFileUploadTokenResult.getXPath());
            VolumeFileUploadService volumeFileUploadService = getVolumeFileUploadService(getVolumeFileUploadTokenResult, mockVolumeFile);
            for (int i = 0; i < volumeFileUploadInfoList.size(); i++) {
                VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
                if (volumeFileUploadInfo.getVolumeFile() == mockVolumeFile) {
                    ProgressCallback progressCallback = volumeFileUploadInfo.getProgressCallback();
                    if (volumeFileUploadService != null) {
                        if (progressCallback != null) {
                            volumeFileUploadService.setProgressCallback(progressCallback);   //如果ProgressCallback已经从ui传递进来，则给volumeFileUploadService设置ProgressCallback
                        }
                        volumeFileUploadInfo.setVolumeFileUploadService(volumeFileUploadService);
                        volumeFileUploadInfo.setGetVolumeFileUploadTokenResult(getVolumeFileUploadTokenResult);
                        volumeFileUploadService.uploadFile(getVolumeFileUploadTokenResult.getFileName(), fileLocalPath);
                    } else {  //如果没有获取相应的上传服务 返回上传失败
                        volumeFileUploadInfo.getVolumeFile().setStatus(VolumeFile.STATUS_UPLOADIND_FAIL);
                        if (progressCallback != null) {
                            progressCallback.onFail();
                        }
                    }
                    break;
                }
            }


        }

        @Override
        public void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath) {
            for (int i = 0; i < volumeFileUploadInfoList.size(); i++) {
                VolumeFileUploadInfo volumeFileUploadInfo = volumeFileUploadInfoList.get(i);
                if (volumeFileUploadInfo.getVolumeFile() == mockVolumeFile) {
                    //如果ProgressCallback已经从ui传递进来，则给volumeFileUploadService设置ProgressCallback
                    ProgressCallback progressCallback = volumeFileUploadInfo.getProgressCallback();
                    volumeFileUploadInfo.getVolumeFile().setStatus(VolumeFile.STATUS_UPLOADIND_FAIL);
                    if (progressCallback != null) {
                        progressCallback.onFail();
                    }
                    break;
                }
            }
        }
    }
}
