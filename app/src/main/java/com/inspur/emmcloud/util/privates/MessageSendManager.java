package com.inspur.emmcloud.util.privates;

import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

/**
 * Created by chenmch on 2019/10/15.
 */

public class MessageSendManager {
    private static MessageSendManager mInstance;

    public static MessageSendManager getInstance() {
        if (mInstance == null) {
            synchronized (MessageSendManager.class) {
                if (mInstance == null) {
                    mInstance = new MessageSendManager();
                }
            }
        }
        return mInstance;
    }


    public void sendMessage(Message message) {
        String messageType = message.getType();
        switch (messageType) {
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                if (message.getMsgContentAttachmentFile().getMedia().equals(message.getLocalPath())) {
                    sendMessageWithFile(message);
                } else {
                    WSAPIService.getInstance().sendMessage(message);
                }
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                if (message.getMsgContentMediaImage().getRawMedia().equals(message.getLocalPath())) {
                    sendMessageWithFile(message);
                } else {
                    WSAPIService.getInstance().sendMessage(message);
                }
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                if (AppUtils.getIsVoiceWordOpen() && StringUtils.isBlank(message.getMsgContentMediaVoice().getResult())) {

                } else {

                }
                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                WSAPIService.getInstance().sendMessage(message);
                break;
            default:
                break;
        }


        /**
         * 将消息显示状态置为发送成功
         *
         * @param index
         */

    private void setMessageSendSuccess(int index, Message message) {
        UIMessage uiMessage = adapter.getItemData(index);
        uiMessage.setMessage(message);
        uiMessage.setId(message.getId());
        uiMessage.setSendStatus(1);
        int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        if (index - firstItemPosition >= 0) {
            View view = msgListView.getChildAt(index - firstItemPosition);
            if (view != null) {
                view.findViewById(R.id.rl_send_status).setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 发送带有附件类型的消息
     *
     * @param fakeMessage
     */
    private void sendMessageWithFile(final Message fakeMessage) {
        ProgressCallback progressCallback = new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                switch (fakeMessage.getType()) {
                    case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                        MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
                        msgContentRegularFile.setName(volumeFile.getName());
                        msgContentRegularFile.setSize(volumeFile.getSize());
                        msgContentRegularFile.setMedia(volumeFile.getPath());
                        msgContentRegularFile.setTmpId(fakeMessage.getTmpId());
                        fakeMessage.setContent(msgContentRegularFile.toString());
                        MessageCacheUtil.saveMessage(BaseApplication.getInstance(), fakeMessage);
                        WSAPIService.getInstance().sendMessage(fakeMessage);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                        MsgContentMediaImage msgContentMediaImage = new MsgContentMediaImage();
                        msgContentMediaImage.setRawWidth(fakeMessage.getMsgContentMediaImage().getRawWidth());
                        msgContentMediaImage.setRawHeight(fakeMessage.getMsgContentMediaImage().getRawHeight());
                        msgContentMediaImage.setRawSize(volumeFile.getSize());
                        msgContentMediaImage.setRawMedia(volumeFile.getPath());
                        msgContentMediaImage.setPreviewHeight(fakeMessage.getMsgContentMediaImage().getPreviewHeight());
                        msgContentMediaImage.setPreviewWidth(fakeMessage.getMsgContentMediaImage().getPreviewWidth());
                        msgContentMediaImage.setPreviewSize(fakeMessage.getMsgContentMediaImage().getPreviewSize());
                        msgContentMediaImage.setPreviewMedia(volumeFile.getPath());
                        msgContentMediaImage.setThumbnailHeight(fakeMessage.getMsgContentMediaImage().getThumbnailHeight());
                        msgContentMediaImage.setThumbnailWidth(fakeMessage.getMsgContentMediaImage().getThumbnailWidth());
                        msgContentMediaImage.setThumbnailMedia(volumeFile.getPath());
                        msgContentMediaImage.setName(volumeFile.getName());
                        msgContentMediaImage.setTmpId(fakeMessage.getTmpId());
                        fakeMessage.setContent(msgContentMediaImage.toString());
                        MessageCacheUtil.saveMessage(BaseApplication.getInstance(), fakeMessage);
                        WSAPIService.getInstance().sendMessage(fakeMessage);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_VOICE:
                        MsgContentMediaVoice msgContentMediaVoice = new MsgContentMediaVoice();
                        msgContentMediaVoice.setMedia(volumeFile.getPath());
                        msgContentMediaVoice.setDuration(fakeMessage.getMsgContentMediaVoice().getDuration());
                        msgContentMediaVoice.setJsonResults(fakeMessage.getMsgContentMediaVoice().getResult());
                        msgContentMediaVoice.setTmpId(fakeMessage.getTmpId());
                        fakeMessage.setContent(msgContentMediaVoice.toString());
                        MessageCacheUtil.saveMessage(BaseApplication.getInstance(), fakeMessage);
                        WSAPIService.getInstance().sendChatMediaVoiceMsg(fakeMessage);
                        break;
                }
            }

            @Override
            public void onLoading(int progress, String uploadSpeed) {
                //此处不进行loading进度，因为消息的发送进度不等于资源的发送进度
            }

            @Override
            public void onFail() {
                setMessageSendFailStatus(fakeMessage.getId());
            }
        };
        ChatFileUploadManagerUtils.getInstance().uploadResFile(fakeMessage, progressCallback);
    }
}
