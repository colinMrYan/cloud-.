package com.inspur.emmcloud.util.privates;

import android.os.Handler;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFileUpload;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;
import com.inspur.emmcloud.ui.appcenter.volume.observe.LoadObservable;
import com.inspur.emmcloud.util.privates.cache.VolumeFileUploadCacheUtils;
import com.inspur.emmcloud.util.privates.oss.OssService;
import com.inspur.emmcloud.util.privates.s3.S3Service;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 云盘文件上传管理类
 */

public class VolumeFileUploadManager extends APIInterfaceInstance {
    private static VolumeFileUploadManager instance;
    private List<VolumeFileUpload> volumeFileUploadList = new ArrayList<>();
    private MyAppAPIService apiService;

    public VolumeFileUploadManager() {
        apiService = new MyAppAPIService(MyApplication.getInstance());
        apiService.setAPIInterface(this);

        volumeFileUploadList = VolumeFileUploadCacheUtils.getVolumeFileUploadList();
        boolean isNeedUpdateVolumeFileUploadStatus = false;
        for (VolumeFileUpload volumeFileUpload : volumeFileUploadList) {
            if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                volumeFileUpload.setStatus(VolumeFile.STATUS_PAUSE);
                isNeedUpdateVolumeFileUploadStatus = true;
            }

            if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_SUCCESS) ||
                    volumeFileUpload.getStatus().equals(VolumeFile.STATUS_FAIL)) {
                isNeedUpdateVolumeFileUploadStatus = true;
            }
        }
        if (isNeedUpdateVolumeFileUploadStatus) {
            VolumeFileUploadCacheUtils.saveVolumeFileUploadList(volumeFileUploadList);
        }
    }

    public static VolumeFileUploadManager getInstance() {
        if (instance == null) {
            synchronized (VolumeFileUploadManager.class) {
                if (instance == null) {
                    instance = new VolumeFileUploadManager();
                }
            }
        }
        return instance;
    }

    private void refreshCache() {
//        volumeFileUploadList.clear();
//        volumeFileUploadList = VolumeFileUploadCacheUtils.getVolumeFileUploadList();
//        boolean isNeedUpdateVolumeFileUploadStatus = false;
//        for (VolumeFileUpload volumeFileUpload : volumeFileUploadList) {
//            if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_LOADING)) {
//                isNeedUpdateVolumeFileUploadStatus = true;
//            }
//
//            if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_SUCCESS) ||
//                    volumeFileUpload.getStatus().equals(VolumeFile.STATUS_FAIL)) {
//                isNeedUpdateVolumeFileUploadStatus = true;
//            }
//        }
//        if (isNeedUpdateVolumeFileUploadStatus) {
//            VolumeFileUploadCacheUtils.saveVolumeFileUploadList(volumeFileUploadList);
//        }
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
        VolumeFileUpload volumeFileUpload = new VolumeFileUpload(mockVolumeFile, localFilePath, volumeFileParentPath);
        if (FileUtils.isFileExist(volumeFileUpload.getLocalFilePath()) && NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            volumeFileUpload.setStatus(VolumeFile.STATUS_LOADING);
            apiService.getVolumeFileUploadToken(file.getName(), volumeFileUpload, mockVolumeFile);
        } else {
            volumeFileUpload.setStatus(VolumeFile.STATUS_FAIL);
        }
        volumeFileUploadList.add(0, volumeFileUpload);
        VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
    }

    /**
     * 重新上传
     *
     * @param mockVolumeFile
     */
    public void reUploadFile(VolumeFile mockVolumeFile) {
        Log.d("zhang", "reUploadFile: ");
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                volumeFileUpload.setVolumeFileUploadService(null);
                if (FileUtils.isFileExist(volumeFileUpload.getLocalFilePath())) {
                    volumeFileUpload.setStatus(VolumeFile.STATUS_LOADING);
                    //上传文件
                    apiService.getVolumeFileUploadToken(mockVolumeFile.getName(), volumeFileUpload, mockVolumeFile);
                } else {
                    volumeFileUpload.setStatus(VolumeFile.STATUS_FAIL);
                }
                VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
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
    public List<VolumeFile> getCurrentFolderUploadVolumeFile(String volumeId, String volumeFileParentPath) {
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getVolumeId().equals(volumeId) && volumeFileUpload.getVolumeFileParentPath().equals(volumeFileParentPath)) {
                VolumeFile mockVolumeFile = VolumeFile.getMockVolumeFile(volumeFileUpload);
                volumeFileList.add(mockVolumeFile);
            }
        }
        return volumeFileList;
    }

    /**
     * 获取云盘此文件夹目录下正在上传的云盘文件
     *
     * @return
     */
    public List<VolumeFile> getAllUploadVolumeFile() {
        refreshCache();
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            VolumeFile mockVolumeFile = VolumeFile.getMockVolumeFile(volumeFileUpload);
            volumeFileList.add(mockVolumeFile);

        }
        return volumeFileList;
    }

    public synchronized List<VolumeFile> getFinishUploadList() {
        refreshCache();
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_NORMAL) || volumeFileUpload.getStatus().equals(VolumeFile.STATUS_SUCCESS)) {
                VolumeFile mockVolumeFile = VolumeFile.getMockVolumeFile(volumeFileUpload);
                volumeFileList.add(mockVolumeFile);
            }
        }
        return volumeFileList;
    }

    public synchronized List<VolumeFile> getUnFinishUploadList() {
        refreshCache();
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_LOADING) || volumeFileUpload.getStatus().equals(VolumeFile.STATUS_PAUSE) ||
                    volumeFileUpload.getStatus().equals(VolumeFile.STATUS_FAIL)) {
                VolumeFile mockVolumeFile = VolumeFile.getMockVolumeFile(volumeFileUpload);
                volumeFileList.add(mockVolumeFile);
            }

        }
        return volumeFileList;
    }

    /**
     * 通过volumeFile 获取对应的上传item
     */
    public VolumeFileUpload getVolumeFileUpload(VolumeFile volumeFile) {
        for (VolumeFileUpload volumeFileUpload : volumeFileUploadList) {
            if (volumeFileUpload.getId().equals(volumeFile.getId())) {
                return volumeFileUpload;
            }
        }
        return null;
    }

    public void deleteUploadInfo(VolumeFile volumeFile) {
        VolumeFileUpload item = getVolumeFileUpload(volumeFile);
        if (item != null) {
            volumeFileUploadList.remove(item);
            VolumeFileUploadCacheUtils.deleteVolumeFileUpload(item);
        }
    }

    /**
     * 移除上传服务
     *
     * @param mockVolumeFile
     */
    public void cancelVolumeFileUploadService(VolumeFile mockVolumeFile) {
        if (mockVolumeFile != null) {
            for (int i = 0; i < volumeFileUploadList.size(); i++) {
                VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
                if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                    VolumeFileUploadService volumeFileUploadService = volumeFileUpload.getVolumeFileUploadService();
                    if (volumeFileUploadService != null) {
                        volumeFileUploadService.onDestroy();
                    }
//                    volumeFileUploadList.remove(i);
//                    VolumeFileUploadCacheUtils.deleteVolumeFileUpload(volumeFileUpload);
                    break;
                }
            }
        }
    }

    public void pauseVolumeFileUploadService(VolumeFile mockVolumeFile) {
        if (mockVolumeFile != null) {
            for (int i = 0; i < volumeFileUploadList.size(); i++) {
                VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
                if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                    Log.d("zhang", "pauseVolumeFileUploadService: ");
                    volumeFileUpload.setStatus(VolumeFile.STATUS_PAUSE);
                    VolumeFileUploadService volumeFileUploadService = volumeFileUpload.getVolumeFileUploadService();
                    if (volumeFileUploadService != null) {
                        Log.d("zhang", "pauseVolumeFileUploadService: 1111");
                        volumeFileUploadService.onPause();
                        volumeFileUpload.setVolumeFileUploadService(null);
                    }
                    VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
                    break;
                }
            }
        }
    }

    /**
     * 设置上传callback
     *
     * @param volumeFile
     * @param businessProgressCallback
     */
    public void setBusinessProgressCallback(final VolumeFile volumeFile, final ProgressCallback businessProgressCallback) {
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            final VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(volumeFile.getId())) {
                if (businessProgressCallback != null) {
                    if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                        businessProgressCallback.onLoading(volumeFileUpload.getProgress(), 0, "");
                    } else if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_FAIL)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                businessProgressCallback.onFail();
                            }
                        });

                    }
                }
                volumeFileUpload.setBusinessProgressCallback(businessProgressCallback);
                break;
            }
        }
    }

    /**
     * 根据不同的storage选择不同的存储服务
     *
     * @param volumeFileUpload
     * @param mockVolumeFile
     * @return
     */
    private VolumeFileUploadService getVolumeFileUploadService(VolumeFileUpload volumeFileUpload, VolumeFile mockVolumeFile) {
        VolumeFileUploadService volumeFileUploadService = null;
        switch (volumeFileUpload.getGetVolumeFileUploadTokenResult().getStorage()) {
            case "ali_oss":  //阿里云
                try {
                    volumeFileUploadService = new OssService(volumeFileUpload.getGetVolumeFileUploadTokenResult(), mockVolumeFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "aws_s3":
                volumeFileUploadService = new S3Service(volumeFileUpload);
                break;
            default:
                break;
        }
        return volumeFileUploadService;
    }

    @Override
    public void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, String fileLocalPath, VolumeFile mockVolumeFile, int transferObserverId) {
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                if (!volumeFileUpload.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                    return;
                }
                ProgressCallback progressCallback = volumeFileUpload.getProgressCallback();
                if (progressCallback == null) {
                    progressCallback = new MyProgressCallback(volumeFileUpload);
                    volumeFileUpload.setProgressCallback(progressCallback);
                }
                if (volumeFileUpload.getVolumeFileUploadService() == null) {
                    volumeFileUpload.setGetVolumeFileUploadTokenResult(getVolumeFileUploadTokenResult);
                    VolumeFileUploadService volumeFileUploadService = getVolumeFileUploadService(volumeFileUpload, mockVolumeFile);
                    if (volumeFileUploadService != null) {
                        volumeFileUploadService.setProgressCallback(progressCallback);
                        volumeFileUpload.setTransferObserverId(transferObserverId);
                        volumeFileUpload.setVolumeFileUploadService(volumeFileUploadService);
                        volumeFileUploadService.uploadFile(getVolumeFileUploadTokenResult.getFileName(), fileLocalPath);
                    } else {  //如果没有获取相应的上传服务 返回上传失败
                        progressCallback.onFail();
                    }
                } else {
                    volumeFileUpload.getVolumeFileUploadService().setProgressCallback(progressCallback);
                }
                break;

            }
        }
    }

    @Override
    public void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath) {
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                //如果ProgressCallback已经从ui传递进来，则给volumeFileUploadService设置ProgressCallback
                ProgressCallback progressCallback = volumeFileUpload.getProgressCallback();
                progressCallback.onFail();
                break;
            }
        }
    }

    /**
     * 重置上传
     */
    public void resetVolumeFileStatus(VolumeFile volumeFile) {
        VolumeFileUpload volumeFileUpload = getVolumeFileUpload(volumeFile);
        if (volumeFileUpload != null) {
            Log.d("zhang", "resetVolumeFileStatus: ");
            volumeFileUploadList.remove(volumeFileUpload);
            VolumeFileUploadCacheUtils.deleteVolumeFileUpload(volumeFileUpload);
        }
    }

    private class MyProgressCallback implements ProgressCallback {
        private VolumeFileUpload volumeFileUpload;

        public MyProgressCallback(VolumeFileUpload volumeFileUpload) {
            this.volumeFileUpload = volumeFileUpload;
        }

        @Override
        public void onSuccess(VolumeFile volumeFile) {
            Log.d("zhang", "onSuccess: 上传成功 + " + volumeFile.getName());
//            ToastUtils.show(BaseApplication.getInstance(), R.string.clouddriver_upload_success);
//            volumeFileUploadList.remove(volumeFileUpload);
            Log.d("zhang", "onSuccess: volumeFileUploadList.size = " + volumeFileUploadList.size());
            for (int i = 0; i < volumeFileUploadList.size(); i++) {
                VolumeFileUpload item = volumeFileUploadList.get(i);
                if (item.getId().equals(volumeFileUpload.getId())) {
                    volumeFileUploadList.remove(item);
                    VolumeFileUploadCacheUtils.deleteVolumeFileUpload(volumeFileUpload);
                    //恢复成真正的ID
                    volumeFileUpload.setId(volumeFile.getId());
                    volumeFileUpload.setStatus(VolumeFile.STATUS_SUCCESS);
                    volumeFileUpload.setLastUpdate(System.currentTimeMillis());
                    VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
                    volumeFileUploadList.add(volumeFileUpload);
                }
            }

//            VolumeFileUploadCacheUtils.deleteVolumeFileUpload(volumeFileUpload);
            if (volumeFileUpload.getBusinessProgressCallback() != null) {
                volumeFileUpload.getBusinessProgressCallback().onSuccess(volumeFile);
            }

            SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_VOLUME_FILE_UPLOAD_SUCCESS, volumeFile);
            simpleEventMessage.setExtraObj(volumeFileUpload);
            EventBus.getDefault().post(simpleEventMessage);
            LoadObservable.getInstance().notifyDateChange();
        }

        @Override
        public void onLoading(int progress, long current, String speed) {
            volumeFileUpload.setProgress(progress);
            volumeFileUpload.setStatus(VolumeFile.STATUS_LOADING);
            VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
            if (volumeFileUpload.getBusinessProgressCallback() != null) {
                volumeFileUpload.getBusinessProgressCallback().onLoading(progress, current, speed);
            }
        }

        @Override
        public void onFail() {
            Log.d("zhang", "callbackError: 上传失败" + volumeFileUpload.getUploadPath());
            volumeFileUpload.setStatus(VolumeFile.STATUS_FAIL);
            VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
            if (volumeFileUpload.getBusinessProgressCallback() != null) {
                volumeFileUpload.setStatus(VolumeFile.STATUS_FAIL);
                VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
                volumeFileUpload.getBusinessProgressCallback().onFail();
            }
        }
    }
}
