package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.load.oss.OssService;
import com.inspur.emmcloud.basemodule.util.load.s3.S3Service;
import com.inspur.emmcloud.bean.ChatFileUploadInfo;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.componentservice.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.volume.VolumeFileUploadService;
import com.inspur.emmcloud.widget.filemanager.FileUtil;
import com.inspur.emmcloud.widget.filemanager.bean.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/8/19.
 */

public class ChatFileUploadManagerUtils extends APIInterfaceInstance {
    private static ChatFileUploadManagerUtils instance;
    private List<ChatFileUploadInfo> chatFileUploadInfoList = new ArrayList<>();
    private ChatAPIService apiService;

    public ChatFileUploadManagerUtils() {
        apiService = new ChatAPIService(MyApplication.getInstance());
        apiService.setAPIInterface(this);
    }

    public static ChatFileUploadManagerUtils getInstance() {
        if (instance == null) {
            synchronized (ChatFileUploadManagerUtils.class) {
                if (instance == null) {
                    instance = new ChatFileUploadManagerUtils();
                }
            }
        }
        return instance;
    }


    /**
     * 判断此消息的资源文件是否正在上传中
     *
     * @param message
     * @return
     */
    public boolean isMessageResourceUploading(Message message) {
        for (ChatFileUploadInfo chatFileUploadInfo : chatFileUploadInfoList) {
            if (chatFileUploadInfo.getMessage().getId().equals(message.getId())) {
                return true;
            }
        }
        return false;
    }

    public void cancelVolumeFileUploadService(Message message) {
        for (ChatFileUploadInfo chatFileUploadInfo : chatFileUploadInfoList) {
            if (chatFileUploadInfo.getMessage().getId().equals(message.getId())) {
                chatFileUploadInfo.setCallback(null);
                if (chatFileUploadInfo.getVolumeFileUploadService() != null) {
                    chatFileUploadInfo.getVolumeFileUploadService().onDestroy();
                }
            }
        }
    }


    private void callbackSuccess(ChatFileUploadInfo chatFileUploadInfo, VolumeFile volumeFile) {
        if (chatFileUploadInfo.getCallback() != null) {
            chatFileUploadInfo.getCallback().onSuccess(volumeFile);
        }
        chatFileUploadInfoList.remove(chatFileUploadInfo);
    }

    private void callbackFail(ChatFileUploadInfo chatFileUploadInfo) {
        if (chatFileUploadInfo.getCallback() != null) {
            chatFileUploadInfo.getCallback().onFail();
        }
        chatFileUploadInfoList.remove(chatFileUploadInfo);
    }

    private String getFilePath(Message message) {
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
        return filePath;
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
            case "ali_oss":
                try {
                    volumeFileUploadService = new OssService(getVolumeFileUploadTokenResult, mockVolumeFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "aws_s3":
                volumeFileUploadService = new S3Service(getVolumeFileUploadTokenResult);
                break;
            default:
                break;
        }
        return volumeFileUploadService;
    }

    /**
     * 真正开始上传文件
     *
     * @param getVolumeFileUploadTokenResult
     * @param chatFileUploadInfo
     */
    private void startUpload(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, final ChatFileUploadInfo chatFileUploadInfo) {
        VolumeFileUploadService volumeFileUploadService = getVolumeFileUploadService(getVolumeFileUploadTokenResult, null);
        chatFileUploadInfo.setVolumeFileUploadService(volumeFileUploadService);
        volumeFileUploadService.setProgressCallback(new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                callbackSuccess(chatFileUploadInfo, volumeFile);
            }

            @Override
            public void onLoading(int progress, long current, String speed) {
            }

            @Override
            public void onFail() {
                callbackFail(chatFileUploadInfo);

            }
        });
        // 视频上传时特殊处理，原有文件上传逻辑不变
        String localFile; // 源文件路径
        if (Message.MESSAGE_TYPE_MEDIA_VIDEO.equals(chatFileUploadInfo.getMessage().getType())) {
            // 视频文件上传逻辑：图片，视频
            String fileName = getVolumeFileUploadTokenResult.getFileName();
            if (fileName.endsWith(".png") || fileName.endsWith(".gif")
                    || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.equals(".dng")) {
                localFile = chatFileUploadInfo.getMessage().getMsgContentMediaVideo().getImagePath();
            } else {
                localFile = chatFileUploadInfo.getMessage().getMsgContentMediaVideo().getMedia();
            }
        } else {
            // 原有文件上传逻辑
            localFile = getFilePath(chatFileUploadInfo.getMessage());
        }
        volumeFileUploadService.uploadFile(getVolumeFileUploadTokenResult.getFileName(), localFile);
    }

    /**
     * 上传文件分为两步
     * 1.获取文件上传token
     * 2.选择服务端给定的上传服务进行上传
     *
     * @param message
     * @param callback
     */
    public void uploadResFile(Message message, ProgressCallback callback) {
        if (!NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            if (callback != null) {
                callback.onFail();
            }
            return;
        }
        ChatFileUploadInfo chatFileUploadInfo = new ChatFileUploadInfo(message, callback);
        chatFileUploadInfoList.add(chatFileUploadInfo);
        String filePath = getFilePath(message);
        if (filePath != null) {
            File file = new File(filePath);
            apiService.getFileUploadToken(file.getName(), chatFileUploadInfo);
        } else {
            callbackFail(chatFileUploadInfo);
        }
    }

    /**
     * 视频分为图片上传和视频上传
     * 每个文件上传分为两步
     * 1.获取文件上传token
     * 2.选择服务端给定的上传服务进行上传
     *
     * @param message
     * @param callback
     */
    public void uploadVideoImageFile(Message message, ProgressCallback callback) {
        if (!NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            if (callback != null) {
                callback.onFail();
            }
            return;
        }
        ChatFileUploadInfo chatFileUploadInfo = new ChatFileUploadInfo(message, callback);
        // 保
        chatFileUploadInfoList.add(chatFileUploadInfo);
        String imagePath = message.getMsgContentMediaVideo().getImagePath();
        if (imagePath != null) {
            File file = new File(imagePath);
            apiService.getFileUploadToken(file.getName(), chatFileUploadInfo);
        } else {
            callbackFail(chatFileUploadInfo);
        }
    }

    /**
     * 视频分为图片上传和视频上传
     * 每个文件上传分为两步
     * 1.获取文件上传token
     * 2.选择服务端给定的上传服务进行上传
     *
     * @param message
     * @param callback
     */
    public void uploadVideoFile(Message message, ProgressCallback callback) {
        if (!NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            if (callback != null) {
                callback.onFail();
            }
            return;
        }
        ChatFileUploadInfo chatFileUploadInfo = new ChatFileUploadInfo(message, callback);
        chatFileUploadInfoList.add(chatFileUploadInfo);
        String videoPath = message.getMsgContentMediaVideo().getMedia();
        if (videoPath != null) {
            apiService.getFileUploadToken(message.getMsgContentMediaVideo().getName(), chatFileUploadInfo);
        } else {
            callbackFail(chatFileUploadInfo);
        }
    }


    @Override
    public void returnChatFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, ChatFileUploadInfo chatFileUploadInfo) {
        startUpload(getVolumeFileUploadTokenResult, chatFileUploadInfo);
    }

    @Override
    public void returnChatFileUploadTokenFail(String error, int errorCode, ChatFileUploadInfo chatFileUploadInfo) {
        callbackFail(chatFileUploadInfo);
    }
}
