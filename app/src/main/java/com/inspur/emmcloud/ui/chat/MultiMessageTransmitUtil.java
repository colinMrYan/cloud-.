package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableString;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.MultiMessageItem;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.richtext.markdown.MarkDown;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.inspur.emmcloud.bean.chat.Message.MESSAGE_TYPE_FILE_REGULAR_FILE;

public class MultiMessageTransmitUtil {

    public static final String EXTRA_MULTI_MESSAGE_TYPE = "multi_message_type";
    public static final Integer TYPE_SINGLE = 0;
    public static final Integer TYPE_MULTI_ITEM_BY_ITEM = 1;
    public static final Integer TYPE_MULTI_MERGED = 2;

    public interface WrapperMultiItemListener {
        void onWrapperMultiItemFinished(JSONArray jsonArray);

        void onWrapperMultiItemFailed();
    }

    public static void transmitMultiMergedMessage(final Context context, final String cid, final Set<UIMessage> selectedMessages) {
        MultiMessageTransmitUtil.getJSONArrayFromUiMessage(cid, false, selectedMessages, new WrapperMultiItemListener() {
            @Override
            public void onWrapperMultiItemFinished(JSONArray jsonArray) {
                Message fakeMessage = CommunicationUtils.combineLocalMultiMessage(jsonArray, cid, null);
                fakeMessage.setSendStatus(Message.MESSAGE_SEND_ING);
                MessageCacheUtil.saveMessage(context, fakeMessage);
                MessageSendManager.getInstance().sendMessage(fakeMessage);
                ToastUtils.show(R.string.chat_message_send_success);
            }

            @Override
            public void onWrapperMultiItemFailed() {
                ToastUtils.show(R.string.chat_message_send_fail);

            }
        });

    }


    public static ArrayList<MultiMessageItem> getListFromJsonStr(String jsonStr) {
        ArrayList<MultiMessageItem> arrayList = new ArrayList<>();
        JSONArray messageList;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            messageList = jsonObject.optJSONArray("messageList");
            arrayList = getListFromJsonArray(messageList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public static ArrayList<MultiMessageItem> getListFromJsonArray(@NonNull JSONArray messageList) {
        ArrayList<MultiMessageItem> arrayList = new ArrayList<>();
        try {
            for (int i = 0; i < messageList.length(); i++) {
                arrayList.add(new MultiMessageItem(messageList.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Collections.sort(arrayList, new Comparator<MultiMessageItem>() {
            @Override
            public int compare(MultiMessageItem o1, MultiMessageItem o2) {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                if (o1.sendTime > o2.sendTime) {
                    return 1;
                } else if (o1.sendTime < o2.sendTime) {
                    return -1;
                }
                return 0;
            }
        });
        return arrayList;
    }


    public static void getJSONArrayFromUiMessage(final String cid, final boolean itemByItem, final Set<UIMessage> messages, final WrapperMultiItemListener wrapperMultiItemListener) {
        final JSONArray jsonArray = new JSONArray();
        Set<UIMessage> fileMessageSet = new CopyOnWriteArraySet<>();
        final int originMessageSize = messages.size();

        for (UIMessage uiMessage : messages) {
            Message message = uiMessage.getMessage();
            JSONObject messageDataJson = new JSONObject();
            try {
                messageDataJson.put("type", message.getType());
                messageDataJson.put("tmpId", message.getTmpId());
                messageDataJson.put("parent", message.getId());
                messageDataJson.put("sendUserId", message.getFromUser());
                messageDataJson.put("sendUserName", uiMessage.getSenderName());
                messageDataJson.put("sendTime", message.getCreationDate() / 1000);
                switch (message.getType()) {
                    case Message.MESSAGE_TYPE_TEXT_PLAIN:
                        String text = message.getMsgContentTextPlain().getText();
                        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(text,
                                message.getMsgContentTextPlain().getMentionsMap());
                        messageDataJson.put("text", spannableString.toString());
                        jsonArray.put(messageDataJson);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    case MESSAGE_TYPE_FILE_REGULAR_FILE:
                        fileMessageSet.add(uiMessage);
                        break;
                    case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                        spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(
                                message.getMsgContentTextMarkdown().getText(),
                                message.getMsgContentTextMarkdown().getMentionsMap());
                        messageDataJson.put("text", MarkDown.fromMarkdown(spannableString.toString()));
                        jsonArray.put(messageDataJson);
                        break;
//            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
//                transmitLinkMsg(cid, uiMessage);
//                break;
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        if (fileMessageSet.isEmpty()) {
            wrapperMultiItemListener.onWrapperMultiItemFinished(jsonArray);
            return;
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (final UIMessage fileMessage : fileMessageSet) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Message message = fileMessage.getMessage();
                    MsgContentMediaImage msgContentMediaImage = message.getMsgContentMediaImage();
                    MsgContentRegularFile msgContentAttachmentFile = message.getMsgContentAttachmentFile();
                    final String path = message.getType().equals(Message.MESSAGE_TYPE_MEDIA_IMAGE) ? msgContentMediaImage.getRawMedia() : msgContentAttachmentFile.getMedia();
                    if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
                        final ChatAPIService apiService = new ChatAPIService(BaseApplication.getInstance());
                        apiService.setAPIInterface(
                                new APIInterfaceInstance() {
                                    @Override
                                    public void returnTransmitPictureSuccess(String callbackCid, String description, Message message) {
                                        synchronized (jsonArray) {
                                            try {
                                                String path = JSONUtils.getString(description, "path", "");
                                                switch (message.getType()) {
                                                    case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                                                        JSONObject imageDataJson = new JSONObject();
                                                        JSONObject thumbnailObj = new JSONObject();
                                                        thumbnailObj.put("width", message.getMsgContentMediaImage().getThumbnailWidth());
                                                        thumbnailObj.put("height", message.getMsgContentMediaImage().getThumbnailHeight());
                                                        thumbnailObj.put("size", message.getMsgContentMediaImage().getThumbnailSize());
                                                        thumbnailObj.put("media", path);
                                                        JSONObject previewObj = new JSONObject();
                                                        previewObj.put("width", message.getMsgContentMediaImage().getPreviewWidth());
                                                        previewObj.put("height", message.getMsgContentMediaImage().getPreviewHeight());
                                                        previewObj.put("size", message.getMsgContentMediaImage().getPreviewSize());
                                                        previewObj.put("media", path);
                                                        JSONObject rawObj = new JSONObject();
                                                        rawObj.put("width", message.getMsgContentMediaImage().getRawWidth());
                                                        rawObj.put("height", message.getMsgContentMediaImage().getRawHeight());
                                                        rawObj.put("size", message.getMsgContentMediaImage().getRawSize());
                                                        rawObj.put("media", path);
                                                        imageDataJson.put("name", message.getMsgContentMediaImage().getName());
                                                        imageDataJson.put("type", message.getType());
                                                        imageDataJson.put("tmpId", message.getTmpId());
                                                        imageDataJson.put("sendUserId", message.getFromUser());
                                                        imageDataJson.put("sendUserName", fileMessage.getSenderName());
                                                        imageDataJson.put("sendTime", message.getCreationDate() / 1000);
                                                        imageDataJson.put("preview", previewObj);
                                                        imageDataJson.put("thumbnail", thumbnailObj);
                                                        imageDataJson.put("raw", rawObj);
                                                        jsonArray.put(imageDataJson);

                                                        break;
                                                    case MESSAGE_TYPE_FILE_REGULAR_FILE:
                                                        JSONObject fileDataJson = new JSONObject();
                                                        fileDataJson.put("sendUserId", message.getFromUser());
                                                        fileDataJson.put("sendUserName", fileMessage.getSenderName());
                                                        fileDataJson.put("sendTime", message.getCreationDate() / 1000);
                                                        fileDataJson.put("type", "file/regular-file");
                                                        fileDataJson.put("category", CommunicationUtils.getChatFileCategory(message.getMsgContentAttachmentFile().getName()));
                                                        fileDataJson.put("name", message.getMsgContentAttachmentFile().getName());
                                                        fileDataJson.put("size", message.getMsgContentAttachmentFile().getSize());
                                                        fileDataJson.put("media", path);
                                                        fileDataJson.put("tmpId", message.getTmpId());
                                                        jsonArray.put(fileDataJson);
                                                        break;
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            if (jsonArray.length() == originMessageSize) {
                                                wrapperMultiItemListener.onWrapperMultiItemFinished(jsonArray);
                                            }
                                        }

                                    }

                                    @Override
                                    public void returnTransmitPictureError(String error, int errorCode) {
                                        ToastUtils.show(R.string.chat_message_send_fail);
                                        wrapperMultiItemListener.onWrapperMultiItemFailed();

                                    }
                                }
                        );

                        String fileType = fileMessage.getMessage().getType().equals(Message.MESSAGE_TYPE_MEDIA_IMAGE) ? "image" : "regular-file";
                        apiService.transmitFile(path, fileMessage.getMessage().getChannel(), cid, fileType, fileMessage.getMessage());
                    }
                }
            });
        }
    }

}
