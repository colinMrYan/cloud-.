package com.inspur.emmcloud.util.privates.oss;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManagerUtils;

import java.util.HashMap;

/**
 * Created by oss on 2015/12/7 0007.
 * 支持普通上传，普通下载和断点上传
 */
public class OssService implements VolumeFileUploadService {

    private static final int SUCCESS = 0;
    private static final int PROGRESS = 1;
    private static final int FAIL = 2;
    private OSS oss;
    private GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult;
    private OSSAsyncTask task;
    private int progress = 0;
    private ProgressCallback progressCallback;
    private VolumeFile mockVolumeFile;
    private Handler handler;


    public OssService(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, VolumeFile mockVolumeFile) {
        this.getVolumeFileUploadTokenResult = getVolumeFileUploadTokenResult;
        this.mockVolumeFile = mockVolumeFile;
        initOss();
    }

    /**
     * 初始化osss
     */
    private void initOss() {
        OSSCredentialProvider credentialProvider = new STSGetter(getVolumeFileUploadTokenResult);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        //conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        try {
            //此语句根据阿里云api文档需要放进主线程，此处应该是一个线程
            oss = new OSSClient(MyApplication.getInstance(), getVolumeFileUploadTokenResult.getEndpoint(), credentialProvider, conf);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public int getProgress() {
        return progress;
    }

    private void handMessage()

    {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS:
                        if (progressCallback != null) {
                            String result = (String) msg.obj;
                            progressCallback.onSuccess(new VolumeFile(result));
                        }
                        VolumeFileUploadManagerUtils.getInstance().removeVolumeFileUploadService(mockVolumeFile);
                        break;
                    case FAIL:
                        if (progressCallback != null) {
                            progressCallback.onFail();
                        }
                        break;
                    case PROGRESS:
                        if (progressCallback != null) {
                            int progress = (int) msg.obj;
                            progressCallback.onLoading(progress);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }


    @Override
    public void uploadFile(String fileName, String localFile) {
        handMessage();
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(getVolumeFileUploadTokenResult.getBucket(), fileName, localFile);


        /**
         * 设置callback address
         */
        // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
        put.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", getVolumeFileUploadTokenResult.getCallbackUrl());
                //callbackBody可以自定义传入的信息
                put("callbackBody", StringUtils.utf8Encode(getVolumeFileUploadTokenResult.getCallbackBody(),getVolumeFileUploadTokenResult.getCallbackBody()));
            }
        });


        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                //Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                progress = (int) (100 * currentSize / totalSize);
                LogUtils.jasonDebug("progress=" + progress);
                Message msg = new Message();
                msg.what = PROGRESS;
                msg.obj = progress;
                if (handler != null) {
                    handler.sendMessage(msg);
                }
            }
        });

        task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                LogUtils.jasonDebug("onSuccess");
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());
                Log.d("PutObjectResult", result.getServerCallbackReturnBody());
                Message msg = new Message();
                msg.what = SUCCESS;
                msg.obj = result.getServerCallbackReturnBody();
                if (handler != null) {
                    handler.sendMessage(msg);
                }


            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                LogUtils.jasonDebug("onFailure");
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                    info = serviceException.toString();
                }
                Message msg = new Message();
                msg.what = FAIL;
                if (handler != null) {
                    handler.sendMessage(msg);
                }
            }
        });
    }

    @Override
    public void onDestory() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (handler != null) {
            handler = null;
        }
    }
}
