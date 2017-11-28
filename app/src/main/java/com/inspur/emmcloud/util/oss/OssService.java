package com.inspur.emmcloud.util.oss;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadSTSTokenResult;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.callback.ProgressCallback;
import com.inspur.emmcloud.util.LogUtils;

import java.io.File;
import java.util.HashMap;

/**
 * Created by oss on 2015/12/7 0007.
 * 支持普通上传，普通下载和断点上传
 */
public class OssService {

    private static final int SUCCESS = 0;
    private static final int PROGRESS = 1;
    private static final int FAIL = 2;
    private OSS oss;
    private GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult;
    private OSSAsyncTask task;
    private int progress = 0;
    private ProgressCallback progressCallback;
    private String volumeFileId = "";
    private Handler handler;


    public OssService(OSS oss, GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult, ProgressCallback progressCallback, String volumeFileId) {
        this.oss = oss;
        this.getVolumeFileUploadSTSTokenResult = getVolumeFileUploadSTSTokenResult;
        this.progressCallback = progressCallback;
        this.volumeFileId = volumeFileId;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
        if (progressCallback != null) {
            progressCallback.onLoading(progress);
        }
    }

    public int getProgress() {
        return progress;
    }

    public void callUpload() {
        if (task != null) {
            task.cancel();
        }
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
                        OssUploadManager.getInstance().removeOssService(volumeFileId);
                        onDestory();
                        break;
                    case FAIL:
                        if (progressCallback != null) {
                            progressCallback.onFail();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void asyncPutImage(String object, String localFile) {
        //如果未设置isBeginUpload 暂时不开始
        if (progressCallback == null) {
            return;
        }
        if (object.equals("")) {
            Log.w("AsyncPutImage", "ObjectNull");
            return;
        }
        handMessage();
        LogUtils.jasonDebug("localFile="+localFile);
        File file = new File(localFile);
        if (!file.exists()) {
            Log.w("AsyncPutImage", "FileNotExist");
            Log.w("LocalFile", localFile);
            return;
        }
        // 构造上传请求
        PutObjectRequest put = new PutObjectRequest(getVolumeFileUploadSTSTokenResult.getBucket(), object, localFile);


        /**
         * 设置callback address
         */
        // 传入对应的上传回调参数，这里默认使用OSS提供的公共测试回调服务器地址
        put.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", getVolumeFileUploadSTSTokenResult.getCallbackUrl());
                //callbackBody可以自定义传入的信息
                put("callbackBody", getVolumeFileUploadSTSTokenResult.getCallbackBody());
            }
        });


        // 异步上传时可以设置进度回调
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                //Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                progress = (int) (100 * currentSize / totalSize);
                LogUtils.jasonDebug("progress="+progress);
                if (progressCallback != null) {
                    progressCallback.onLoading(progress);
                }
            }
        });

        task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");

                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());
                Log.d("PutObjectResult", result.getServerCallbackReturnBody());
                Message msg = new Message();
                msg.what = SUCCESS;
                msg.obj = result.getServerCallbackReturnBody();
                if (handler != null){
                    handler.sendMessage(msg);
                }


            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
                if (handler != null){
                    handler.sendMessage(msg);
                }
//                UIDisplayer.uploadFail(info);
//                UIDisplayer.displayInfo(info);
            }
        });
    }
    public void onDestory(){
        if (task != null){
            task.cancel();
            task = null;
        }
        if(handler != null){
            handler = null;
        }
    }

}
