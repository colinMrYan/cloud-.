package com.inspur.emmcloud.util.privates.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.STSSessionCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by chenmch on 2019/9/5.
 */

public class S3Service implements VolumeFileUploadService {
    private GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult;
    private VolumeFile mockVolumeFile;

    public S3Service(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, VolumeFile mockVolumeFile) {
        this.getVolumeFileUploadTokenResult = getVolumeFileUploadTokenResult;
        this.mockVolumeFile = mockVolumeFile;
        initService();
    }

    private void initService() {


    }

    @Override
    public void setProgressCallback(ProgressCallback progressCallback) {

    }

    @Override
    public void uploadFile(String fileName, String localFile) {

        LogUtils.jasonDebug("getVolumeFileUploadTokenResult.getSecurityToken()==" + getVolumeFileUploadTokenResult.getSecurityToken());
        AWSCredentials credentials = new BasicSessionCredentials(getVolumeFileUploadTokenResult.getAccessKeyId(), getVolumeFileUploadTokenResult.getAccessKeySecret(), getVolumeFileUploadTokenResult.getSecurityToken());
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000);
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        AWSCredentialsProvider awsCredentialsProvider = new STSSessionCredentialsProvider(credentials, conf);
        JSONObject configObj = new JSONObject();
        try {
            JSONObject s3TransferUtilityObj = new JSONObject();

            JSONObject defaultObj = new JSONObject();

            defaultObj.put("Bucket", getVolumeFileUploadTokenResult.getBucket());
            defaultObj.put("Region", getVolumeFileUploadTokenResult.getRegion());
            s3TransferUtilityObj.put("Default", defaultObj);
            configObj.put("S3TransferUtility", s3TransferUtilityObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogUtils.jasonDebug("configObj===" + configObj);
        AWSConfiguration awsConfiguration = new AWSConfiguration(configObj);

        AWSMobileClient.getInstance().federatedSignIn()

        AWSMobileClient.getInstance().initialize(BaseApplication.getInstance(), awsConfiguration, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                LogUtils.jasonDebug("result===" + result.getUserState());
            }

            @Override
            public void onError(Exception e) {
                LogUtils.jasonDebug("e===" + e.toString());
            }
        });
        Region region = Region.getRegion(Regions.fromName(getVolumeFileUploadTokenResult.getRegion()));
        AmazonS3Client amazonS3Client = new AmazonS3Client(awsCredentialsProvider, region, conf);
        amazonS3Client.setEndpoint(getVolumeFileUploadTokenResult.getUrl());
        TransferUtility transferUtility = TransferUtility.builder()
                .context(BaseApplication.getInstance())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(amazonS3Client)
                .build();
        TransferObserver transferObserver = transferUtility.upload(getVolumeFileUploadTokenResult.getBucket(), getVolumeFileUploadTokenResult.getFileName(), new File(localFile));
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                LogUtils.jasonDebug("state===" + state.toString());
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;

                LogUtils.jasonDebug("onProgressChanged===" + percentDone);
            }

            @Override
            public void onError(int id, Exception ex) {
                LogUtils.jasonDebug("onError===" + ex.toString());

            }
        });
    }

    @Override
    public void onDestory() {

    }

}
