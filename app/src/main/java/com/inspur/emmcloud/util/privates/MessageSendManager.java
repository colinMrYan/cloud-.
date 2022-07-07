package com.inspur.emmcloud.util.privates;

import android.os.Handler;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.componentservice.app.CommonCallBack;
import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.interf.ResultCallback;
import com.inspur.emmcloud.util.privates.audioformat.AudioMp3ToPcm;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenmch on 2019/10/15.
 */

public class MessageSendManager {
    private static MessageSendManager mInstance;
    private final int interval = 5000;
    //    private OnMessageSendStatusListener onMessageSendStatusListener;
    private List<Message> messageListInSendRetry = new ArrayList<>();
    private List<Message> recallSendingMessageList = new ArrayList<>();
    private Handler handler;
    private Runnable runnable;
    //检查消息是否发送超时的Runnable是否正在执行
    private boolean isCheckMessageSendTimeoutRunning = false;

    private MessageSendManager() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Iterator<Message> it = messageListInSendRetry.iterator();
                while (it.hasNext()) {
                    Message message = it.next();
                    if (isMessageSendTimeout(message)) {
                        setMessageSendFail(message);
                        it.remove();
                    }
                }
                if (messageListInSendRetry.size() > 0) {
                    isCheckMessageSendTimeoutRunning = true;
                    handler.postDelayed(runnable, interval);
                } else {
                    isCheckMessageSendTimeoutRunning = false;
                    handler.removeCallbacks(runnable);
                }


            }
        };
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

    public void recallSendingMessage(Message message) {
        if (messageListInSendRetry.contains(message)) {
            messageListInSendRetry.remove(message);
        } else if (ChatFileUploadManagerUtils.getInstance().isMessageResourceUploading(message)) {
            ChatFileUploadManagerUtils.getInstance().cancelVolumeFileUploadService(message);
        } else {
            recallSendingMessageList.add(message);
        }

    }

    public void startCheckMessageSendTimeout() {
        if (messageListInSendRetry.size() > 0 && !isCheckMessageSendTimeoutRunning) {
            handler.postDelayed(runnable, interval);
            isCheckMessageSendTimeoutRunning = true;
        }
    }


    public void stopCheckMessageSendTimeout() {
        if (messageListInSendRetry.size() == 0) {
            handler.removeCallbacks(runnable);
            isCheckMessageSendTimeoutRunning = false;
        }
    }


    /**
     * 每次应用启动时将所有发送中的消息置为等待重发状态中
     */
    public void initMessageStatus() {
        messageListInSendRetry.clear();
        stopCheckMessageSendTimeout();
        List<Message> messageListInSendingStatus = MessageCacheUtil.getMessageListBySendStatus(Message.MESSAGE_SEND_ING);
        for (Message message : messageListInSendingStatus) {
            //聊天文件正在上传中不修改任何状态
            if (!ChatFileUploadManagerUtils.getInstance().isMessageResourceUploading(message)) {
                if (isMessageSendTimeout(message)) {
                    message.setSendStatus(Message.MESSAGE_SEND_FAIL);
                    message.setWaitingSendRetry(false);
                } else {
                    message.setWaitingSendRetry(true);
                }
            }

        }
        MessageCacheUtil.saveMessageList(BaseApplication.getInstance(), messageListInSendingStatus);
    }


    /**
     * 当WS上线之后重发所有的待发送中地方消息
     * 注意：必须在WS离线消息获取成功之后
     */
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
                        if (!recallSendingMessageList.contains(message)) {
                            if (message.getMsgContentMediaVoice().getMedia().equals(message.getLocalPath())) {
                                sendMessageWithFile(message);
                            } else {
                                WSAPIService.getInstance().sendMessage(message);
                            }
                        } else {
                            recallSendingMessageList.remove(message);
                        }

                    }
                };
                convertMediaVoiceFile(message, commonCallBack);

                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_WHISPER:
            case Message.MESSAGE_TYPE_TEXT_BURN:
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
            case Message.MESSAGE_TYPE_COMPLEX_MESSAGE:
                WSAPIService.getInstance().sendMessage(message);
                break;
            default:
                break;
        }
    }

//    private void sendMessage(Message message){
//        WSAPIService.getInstance().sendMessage(message);
//    }

    public void addMessageInSendRetry(Message message) {
        if (!recallSendingMessageList.contains(message)) {
            if (isMessageSendTimeout(message)) {
                setMessageSendFail(message);
            } else {
                message.setWaitingSendRetry(true);
                MessageCacheUtil.saveMessage(BaseApplication.getInstance(), message);
                messageListInSendRetry.add(message);
                startCheckMessageSendTimeout();
            }
        } else {
            recallSendingMessageList.remove(message);

        }
    }

    public void onMessageSendSuccess(Message message) {
        if (message.getFromUser().equals(BaseApplication.getInstance().getUid())) {
            Message fakeMessage = new Message();
            fakeMessage.setId(message.getTmpId());
            if (messageListInSendRetry.size() > 0) {
                messageListInSendRetry.remove(fakeMessage);
                stopCheckMessageSendTimeout();
            }
            if (recallSendingMessageList.size() > 0) {
                recallSendingMessageList.remove(fakeMessage);
            }

        }

    }

    private void setMessageSendFail(Message message) {
        message.setWaitingSendRetry(false);
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
            public void onLoading(int progress, long current, String speed) {
                //此处不进行loading进度，因为消息的发送进度不等于资源的发送进度
            }

            @Override
            public void onFail() {
                if (!recallSendingMessageList.contains(fakeMessage)) {
                    //当网络失败或者发送时间已经超过10分钟时
                    if (!NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
                        addMessageInSendRetry(fakeMessage);
                    } else {
                        setMessageSendFail(fakeMessage);
                    }
                } else {
                    recallSendingMessageList.remove(fakeMessage);
                }

            }
        };

        if (!recallSendingMessageList.contains(fakeMessage)) {
            ChatFileUploadManagerUtils.getInstance().uploadResFile(fakeMessage, progressCallback);
        } else {
            recallSendingMessageList.remove(fakeMessage);
        }
    }

    private boolean isMessageSendTimeout(Message fakeMessage) {
        return System.currentTimeMillis() - fakeMessage.getCreationDate() - (MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE * 1000) >= 0;
    }


    public void onDestroy() {
        messageListInSendRetry.clear();
        recallSendingMessageList.clear();
        stopCheckMessageSendTimeout();
    }
}
