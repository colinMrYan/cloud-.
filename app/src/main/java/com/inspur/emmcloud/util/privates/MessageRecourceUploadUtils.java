package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;
import com.inspur.emmcloud.util.privates.oss.OssService;

import java.io.File;


public class MessageRecourceUploadUtils {
    private Context context;
    private ChatAPIService apiService;
    private String cid;
    private ProgressCallback callback;
    private Message message;
    private File file;

    public MessageRecourceUploadUtils(Context context, String cid) {
        this.context = context;
        apiService = new ChatAPIService(context);
        apiService.setAPIInterface(new WebService());
        this.cid = cid;

    }


    public void uploadResFile(Message message) {
        // TODO Auto-generated method stub
        if (!NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            callbackFail();
            return;
        }
        this.message = message;
        String filePath = null;
        switch (message.getType()) {
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                filePath = message.getMsgContentAttachmentFile().getMedia();
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                filePath = message.getMsgContentMediaImage().getRawMedia();
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                filePath = message.getMsgContentMediaVoice().getMedia();
                break;
        }
        if (filePath != null) {
            file = new File(filePath);
            apiService.getFileUploadToken(file.getName(), cid, message.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE));
        }
    }

    public void setProgressCallback(ProgressCallback callback) {
        this.callback = callback;
    }

    private void callbackFail() {
        if (callback != null) {
            callback.onFail();
        }
    }

    private void callbackSuccess(VolumeFile volumeFile) {
        if (callback != null) {
            callback.onSuccess(volumeFile);
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
            default:
                break;
        }
        return volumeFileUploadService;
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult) {
            VolumeFileUploadService volumeFileUploadService = getVolumeFileUploadService(getVolumeFileUploadTokenResult, null);
            volumeFileUploadService.setProgressCallback(new ProgressCallback() {
                @Override
                public void onSuccess(VolumeFile volumeFile) {
                    callbackSuccess(volumeFile);
                }

                @Override
                public void onLoading(int progress) {
                }

                @Override
                public void onFail() {
                    callbackFail();

                }
            });

            volumeFileUploadService.uploadFile(getVolumeFileUploadTokenResult.getFileName(), file.getAbsolutePath());
        }

        @Override
        public void returnChatFileUploadTokenFail(String error, int errorCode) {
            callbackFail();
        }
    }
}
