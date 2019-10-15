package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.ResultCallback;
import com.inspur.emmcloud.util.privates.audioformat.AudioMp3ToPcm;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by chenmch on 2019/10/15.
 */

public class MessageSendManager {
    private static MessageSendManager mInstance;
    //    private OnMessageSendStatusListener onMessageSendStatusListener;
    private List<Message> messageListInSendRetry = new ArrayList<>();
    private ScheduledExecutorService scheduledExecutorService;

    private MessageSendManager() {
    }

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

    /**
     * 每次应用启动时将所有发送中的消息置为等待重发状态中
     */
    public void initMessageStatus() {
        List<Message> messageListInSendingStatus = MessageCacheUtil.getMessageListBySendStatus(Message.MESSAGE_SEND_ING);
        for (Message message : messageListInSendingStatus) {
            if (isMessageSendTimeout(message)) {
                message.setSendStatus(Message.MESSAGE_SEND_FAIL);
                message.setWaitingSendRetry(false);
            } else {
                message.setWaitingSendRetry(true);
            }
        }
        MessageCacheUtil.saveMessageList(BaseApplication.getInstance(), messageListInSendingStatus);
    }


    public void resendMessageAfterWSOnline() {
        messageListInSendRetry = MessageCacheUtil.getMessageListBySendStatus(Message.MESSAGE_SEND_ING, true);
        for (Message message : messageListInSendRetry) {
            message.setWaitingSendRetry(false);
        }
        MessageCacheUtil.saveMessageList(BaseApplication.getInstance(), messageListInSendRetry);
        Iterator<Message> it = messageListInSendRetry.iterator();
        while (it.hasNext()) {
            Message message = it.next();
            it.remove();
            sendMessage(message);
        }
    }


//    public void setOnMessageSendStatusListener(OnMessageSendStatusListener onMessageSendStatusListener){
//        this.onMessageSendStatusListener = onMessageSendStatusListener;
//    }


    public void sendMessage(final Message message) {
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
                CommonCallBack commonCallBack = new CommonCallBack() {
                    @Override
                    public void execute() {
                        if (message.getMsgContentMediaVoice().getMedia().equals(message.getLocalPath())) {
                            sendMessageWithFile(message);
                        } else {
                            WSAPIService.getInstance().sendMessage(message);
                        }
                    }
                };
                convertMediaVoiceFile(message, commonCallBack);

                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                WSAPIService.getInstance().sendMessage(message);
                break;
            default:
                break;
        }
    }

    public void addMessageInSendRetry(Message message) {
        if (isMessageSendTimeout(message)) {
            setMessageSendFail(message);
        } else {
            message.setWaitingSendRetry(true);
            MessageCacheUtil.saveMessage(BaseApplication.getInstance(), message);
            messageListInSendRetry.add(message);
        }
    }

    public void removeMessageInSendRetry(Message message) {
        if (messageListInSendRetry.size() > 0 && message.getFromUser().equals(BaseApplication.getInstance().getUid())) {
            Iterator<Message> it = messageListInSendRetry.iterator();
            while (it.hasNext()) {
                Message fakeMessage = it.next();
                if (message.getTmpId().equals(fakeMessage.getId())) {
                    it.remove();
                    break;
                }

            }

        }
    }

    private void setMessageSendFail(Message message) {
        message.setSendStatus(Message.MESSAGE_SEND_FAIL);
        EventMessage eventMessage = new EventMessage(message.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "");
        eventMessage.setStatus(-1);
        EventBus.getDefault().post(eventMessage);
    }

    private void convertMediaVoiceFile(final Message message, final CommonCallBack commonCallBack) {
        if (AppUtils.getIsVoiceWordOpen() && StringUtils.isBlank(message.getMsgContentMediaVoice().getResult())) {
            String localMp3Path = message.getLocalPath();
            String localWavPath = localMp3Path.replace(".mp3", ".wav");
            if (FileUtils.isFileExist(localWavPath)) {
                voiceToWord(localWavPath, message, commonCallBack);
            } else {
                final String dstPcmPath = localMp3Path.replace(".mp3", ".pcm");
                new AudioMp3ToPcm().startMp3ToPCM(localMp3Path, dstPcmPath, new ResultCallback() {
                    @Override
                    public void onSuccess() {
                        voiceToWord(dstPcmPath, message, commonCallBack);
                    }

                    @Override
                    public void onFail() {
                        if (commonCallBack != null) {
                            commonCallBack.execute();
                        }
                    }
                });
            }

        } else {
            if (commonCallBack != null) {
                commonCallBack.execute();
            }

        }
    }

    private void voiceToWord(String filePath, final Message message, final CommonCallBack commonCallBack) {
        Voice2StringMessageUtils voice2StringMessageUtils = new Voice2StringMessageUtils(BaseApplication.getInstance());
        if (FileUtils.getFileExtension(filePath).equals("pcm")) {
            voice2StringMessageUtils.setAudioSimpleRate(8000);
        }
        voice2StringMessageUtils.setOnVoiceResultCallback(new OnVoiceResultCallback() {
            @Override
            public void onVoiceStart() {
            }

            @Override
            public void onVoiceResultSuccess(VoiceResult voiceResult, boolean isLast) {
                MsgContentMediaVoice originMsgContentMediaVoice = message.getMsgContentMediaVoice();
                if (!voiceResult.getResults().equals(originMsgContentMediaVoice.getResult())) {
                    MsgContentMediaVoice msgContentMediaVoice = new MsgContentMediaVoice();
                    msgContentMediaVoice.setDuration(originMsgContentMediaVoice.getDuration());
                    msgContentMediaVoice.setMedia(originMsgContentMediaVoice.getMedia());
                    msgContentMediaVoice.setJsonResults(voiceResult.getResults());
                    message.setContent(msgContentMediaVoice.toString());
                    MessageCacheUtil.saveMessage(MyApplication.getInstance(), message);
                    if (commonCallBack != null) {
                        commonCallBack.execute();
                    }

                }
            }

            @Override
            public void onVoiceFinish() {
            }

            @Override
            public void onVoiceLevelChange(int volume) {

            }

            @Override
            public void onVoiceResultError(VoiceResult errorResult) {
                if (commonCallBack != null) {
                    commonCallBack.execute();
                }

            }
        });
        voice2StringMessageUtils.startVoiceListeningByVoiceFile(message.getMsgContentMediaVoice().getDuration(), filePath);
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
                        WSAPIService.getInstance().sendMessage(fakeMessage);
                        break;
                }
            }

            @Override
            public void onLoading(int progress, String uploadSpeed) {
                //此处不进行loading进度，因为消息的发送进度不等于资源的发送进度
            }

            @Override
            public void onFail() {
                //当网络失败或者发送时间已经超过10分钟时
                if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false) || isMessageSendTimeout(fakeMessage)) {
                    setMessageSendFail(fakeMessage);
                } else {
                    fakeMessage.setWaitingSendRetry(true);
                    MessageCacheUtil.saveMessage(BaseApplication.getInstance(), fakeMessage);
                }
            }
        };
        ChatFileUploadManagerUtils.getInstance().uploadResFile(fakeMessage, progressCallback);
    }

    private boolean isMessageSendTimeout(Message fakeMessage) {
        LogUtils.jasonDebug("" + (System.currentTimeMillis() - fakeMessage.getCreationDate() - (MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE * 1000)));
        return System.currentTimeMillis() - fakeMessage.getCreationDate() - (MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE * 1000) >= 0;
    }

    private class CheckMessageSendTimeoutRunnable implements Runnable {
        @Override
        public void run() {
            if (messageListInSendRetry.size() > 0) {
                for (Message message : messageListInSendRetry) {
                    if (isMessageSendTimeout(message)) {
                        message.setWaitingSendRetry(false);
                    }
                }
            }
        }
    }


//    public interface OnMessageSendStatusListener{
//        void onMessageSendFail(Message message);
//        void onMessageSendFail(List<Message>  messageList);
//    }
}
