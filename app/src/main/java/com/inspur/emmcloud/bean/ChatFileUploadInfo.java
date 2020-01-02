package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;

/**
 * Created by chenmch on 2019/8/20.
 */

public class ChatFileUploadInfo {
    private Message message;
    private ProgressCallback callback;
    private VolumeFileUploadService volumeFileUploadService;

    public ChatFileUploadInfo(Message message, ProgressCallback callback) {
        this.message = message;
        this.callback = callback;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public VolumeFileUploadService getVolumeFileUploadService() {
        return volumeFileUploadService;
    }

    public void setVolumeFileUploadService(VolumeFileUploadService volumeFileUploadService) {
        this.volumeFileUploadService = volumeFileUploadService;
    }

    public ProgressCallback getCallback() {
        return callback;
    }

    public void setCallback(ProgressCallback callback) {
        this.callback = callback;
    }

    public boolean equals(Object other) {

        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof ChatFileUploadInfo))
            return false;

        final ChatFileUploadInfo otherChatFileUploadInfo = (ChatFileUploadInfo) other;
        return getMessage().getId().equals(otherChatFileUploadInfo.getMessage().getId());
    }
}
