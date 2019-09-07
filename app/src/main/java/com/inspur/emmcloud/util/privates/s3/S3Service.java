package com.inspur.emmcloud.util.privates.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by chenmch on 2019/9/5.
 */

public class S3Service extends APIInterfaceInstance implements VolumeFileUploadService {
    private GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult;
    private VolumeFile mockVolumeFile;
    private ProgressCallback progressCallback;

    public S3Service(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, VolumeFile mockVolumeFile) {
        this.getVolumeFileUploadTokenResult = getVolumeFileUploadTokenResult;
        this.mockVolumeFile = mockVolumeFile;
        initService();
    }

    private void initService() {


    }

    @Override
    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    @Override
    public void uploadFile(String fileName, final String localFilePath) {

        ClientConfiguration mConf = new ClientConfiguration();
        mConf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        mConf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        mConf.setMaxConnections(5);
        mConf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次

        BasicSessionCredentials awsCredentials = new BasicSessionCredentials(getVolumeFileUploadTokenResult.getAccessKeyId(), getVolumeFileUploadTokenResult.getAccessKeySecret(), getVolumeFileUploadTokenResult.getSecurityToken());
        Region region = Region.getRegion(getVolumeFileUploadTokenResult.getRegion());
        MyAmazonS3Client sS3Client = new MyAmazonS3Client(awsCredentials, region, mConf);
        sS3Client.setEndpoint(getVolumeFileUploadTokenResult.getUrl());
        TransferUtility mTransferUtility = TransferUtility.builder().context(BaseApplication.getInstance())
                .s3Client(sS3Client)
                .build();


        final TransferObserver transferObserver = mTransferUtility.upload(getVolumeFileUploadTokenResult.getBucket(), getVolumeFileUploadTokenResult.getFileName(), new File(localFilePath));
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    if (progressCallback != null) {
                        LogUtils.jasonDebug("transferObserver=" + transferObserver.getAbsoluteFilePath());
                        callback(localFilePath);
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (progressCallback != null) {
                    int progress = (int) ((float) bytesCurrent / (float) bytesTotal) * 100;
                    progressCallback.onLoading(progress);
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                callbackFail();

            }
        });
    }

    private void callbackFail() {
        if (progressCallback != null) {
            progressCallback.onFail();
        }
    }

    private void callbackSuccess(VolumeFile volumeFile) {
        if (progressCallback != null) {
            progressCallback.onSuccess(volumeFile);
        }
    }

    @Override
    public void onDestroy() {

    }

    //{"x:region":"oss-cn-shanghai",
    // "x:volume":"45e78609-4f31-4822-b892-6de659c09448",
    // "x:path":"/日志.txt|1567762012634",
    // "x:format":".txt",
    // "bucket":"ecm-cloud-drive",
    // "size":22003,
    // "mimeType":"text/plain",
    // "object":"4acd2349-f4e6-4b02-bfee-04af31ae5b81"}

    private void callback(String localFilePath) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            MyAppAPIService apiService = new MyAppAPIService(BaseApplication.getInstance());
            apiService.setAPIInterface(this);
            File file = new File(localFilePath);
            JSONObject obj = new JSONObject();
            try {
                obj.put("object", getVolumeFileUploadTokenResult.getFileName());
                obj.put("size", FileUtils.getFileSize(localFilePath));
                obj.put("mimeType", FileUtils.getMimeType(file));
                obj.put("bucket", getVolumeFileUploadTokenResult.getBucket());
                obj.put("x:format", FileUtils.getExtensionNameWithPoint(file.getName()));
                obj.put("location", getVolumeFileUploadTokenResult.getEndpoint() + "/" + getVolumeFileUploadTokenResult.getBucket() + "/" + getVolumeFileUploadTokenResult.getFileName());
                String callbackBody = getVolumeFileUploadTokenResult.getCallbackBody();
                String[] bodyparms = callbackBody.split("&");
                for (String parm : bodyparms) {
                    if (parm.startsWith("x:region=") || parm.startsWith("x:volume=") || parm.startsWith("x:path=") || parm.startsWith("x:storage")) {
                        String key = parm.split("=")[0];
                        String value = parm.substring(key.length() + 1, parm.length());
                        obj.put(key, value);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            apiService.callbackAfterFileUpload(getVolumeFileUploadTokenResult.getCallbackUrl(), obj);
        } else {
            callbackFail();
        }
    }

    @Override
    public void returnCallbackAfterFileUploadSuccess(VolumeFile volumeFile) {
        callbackSuccess(volumeFile);
    }

    @Override
    public void returnCallbackAfterFileUploadFail(String error, int errorCode) {
        callbackFail();
    }
}
