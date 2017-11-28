package com.inspur.emmcloud.util.oss;

import android.content.Context;
import android.os.Handler;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadSTSTokenResult;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.callback.ProgressCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/11/24.
 */

public class OssUploadManager {
    private static  OssUploadManager instance;
    private List<OssUploadInfo> ossUploadInfoList = new ArrayList<>();

    public static  OssUploadManager getInstance(){
        if (instance == null){
            synchronized (OssUploadManager.class){
                if (instance == null){
                    instance = new OssUploadManager();
                }
            }
        }
        return instance;
    }

    public void removeOssService(String volumeFileId){
        OssService ossService = null;
        for (int i=0;i<ossUploadInfoList.size();i++){
            OssUploadInfo ossUploadInfo = ossUploadInfoList.get(i);
            if (ossUploadInfo.getVolumeFile().getId().equals(volumeFileId)){
                ossService = ossUploadInfo.getOssService();
                ossUploadInfoList.remove(i);
                break;
            }
        }
        if (ossService != null){
            ossService.onDestory();
        }
    }

    public List<VolumeFile> getCurrentForderUploadingVolumeFile(String volumeId,String dirPath){
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i=0;i<ossUploadInfoList.size();i++){
            OssUploadInfo ossUploadInfo = ossUploadInfoList.get(i);
            if (ossUploadInfo.getParentPath().equals(volumeId+dirPath)){
                volumeFileList.add(ossUploadInfo.getVolumeFile());
            }
        }
        return volumeFileList;

    }

    public void setOssUploadProgressCallback(VolumeFile volumeFile,ProgressCallback progressCallback){
        for (int i=0;i<ossUploadInfoList.size();i++){
            OssUploadInfo ossUploadInfo = ossUploadInfoList.get(i);
            if (ossUploadInfo.getVolumeFile() == volumeFile){
                ossUploadInfo.getOssService().setProgressCallback(progressCallback);
            }
        }
    }

    //初始化一个OssService用来上传下载
    public OssService initOSS(Context context, GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult,ProgressCallback progressCallback,String volumeFileId) {
        OSSCredentialProvider credentialProvider = new STSGetter(getVolumeFileUploadSTSTokenResult);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(context, getVolumeFileUploadSTSTokenResult.getEndpoint(), credentialProvider, conf);
        return new OssService(oss, getVolumeFileUploadSTSTokenResult, progressCallback,volumeFileId);

    }

    public synchronized void startUpload(Context context, final GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult, ProgressCallback progressCallback, VolumeFile volumeFile,final String filePath,String parentPath){
       final OssService ossService = initOSS(context.getApplicationContext(), getVolumeFileUploadSTSTokenResult,progressCallback,volumeFile.getId());
        OssUploadInfo ossUploadInfo = new OssUploadInfo(ossService,volumeFile,parentPath);
        ossUploadInfoList.add(ossUploadInfo);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ossService.asyncPutImage(getVolumeFileUploadSTSTokenResult.getFileName(), filePath);
            }
        },500);


    }
}
