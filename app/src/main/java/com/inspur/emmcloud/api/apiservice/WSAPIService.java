package com.inspur.emmcloud.api.apiservice;

import android.text.TextUtils;
import android.view.ViewGroup;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.WSCommandBatch;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentComment;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.RelatedLink;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.bean.chat.WSCommand;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.chat.DisplayMediaImageMsg;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenmch on 2018/4/28.
 */

public class WSAPIService {
    private static WSAPIService apiService = null;
    private static final String HEADER_TAG = "headers";
    private static final String METHOD_TAG = "method";
    private static final String CHANNEL_TAG =  "/channel/";
    private static final String MESSAGE_TAG =  "/message";
    private static final String ACTION_TAG =  "action";
    private static final String ENTERPRISE_TAG =  "enterprise";
    private static final String TRACER_TAG =  "tracer";
    private static final String TMP_ID_TAG =  "tmpId";
    private static final String MEDIA_TAG =  "media";

    public WSAPIService() {
    }

    public static WSAPIService getInstance() {
        if (apiService == null) {
            synchronized (WebSocketPush.class) {
                if (apiService == null) {
                    apiService = new WSAPIService();
                }
            }
        }
        return apiService;
    }

    public void sendMessage(Message fakeMessage) {
        switch (fakeMessage.getType()) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                sendChatTextPlainMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_TEXT_WHISPER:
                sendChatTextWhisperMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_TEXT_BURN:
                sendChatTextBurnMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                sendChatCommentTextPlainMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                sendChatRegularFileMsg(fakeMessage);
                break;

            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                sendChatMediaVoiceMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                sendChatExtendedLinksMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                sendChatMediaImageMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_MEDIA_VIDEO:
                sendChatMediaVideoMsg(fakeMessage);
                break;
            case Message.MESSAGE_TYPE_COMPLEX_MESSAGE:
                sendChatMultiMsg(fakeMessage);
                break;
        }

    }

    private void sendChatTextPlainMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + fakeMessage.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            MsgContentTextPlain msgContentTextPlain = fakeMessage.getMsgContentTextPlain();
            bodyObj.put("type", Message.MESSAGE_TYPE_TEXT_PLAIN);
            bodyObj.put("text", msgContentTextPlain.getText());
            Map<String, String> mentionsMap = msgContentTextPlain.getMentionsMap();
            if (mentionsMap != null && mentionsMap.size() > 0) {
                JSONObject mentionsObj = JSONUtils.map2Json(mentionsMap);
                bodyObj.put("mentions", mentionsObj);
            }
            bodyObj.put(TMP_ID_TAG, fakeMessage.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(fakeMessage.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChatMultiMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + fakeMessage.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            MsgContentTextPlain msgContentTextPlain = fakeMessage.getMsgContentTextPlain();
            bodyObj.put("type", Message.MESSAGE_TYPE_COMPLEX_MESSAGE);
            JSONObject jsonObject = new JSONObject(fakeMessage.getContent());
            bodyObj.put("messageList", jsonObject.optJSONArray("messageList"));
            Map<String, String> mentionsMap = msgContentTextPlain.getMentionsMap();
            if (mentionsMap != null && mentionsMap.size() > 0) {
                JSONObject mentionsObj = JSONUtils.map2Json(mentionsMap);
                bodyObj.put("mentions", mentionsObj);
            }
            bodyObj.put(TMP_ID_TAG, fakeMessage.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(fakeMessage.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendChatTextWhisperMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            MsgContentTextPlain msgContentTextPlain = fakeMessage.getMsgContentTextPlain();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + fakeMessage.getChannel() + MESSAGE_TAG);
            JSONObject whisperObj = new JSONObject();
            whisperObj.put("to",JSONUtils.toJSONArray(msgContentTextPlain.getWhisperUsers()));
            actionObj.put("query", whisperObj);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", Message.MESSAGE_TYPE_TEXT_PLAIN);
            bodyObj.put("text", msgContentTextPlain.getText());
            Map<String, String> mentionsMap = msgContentTextPlain.getMentionsMap();
            if (mentionsMap != null && mentionsMap.size() > 0) {
                JSONObject mentionsObj = JSONUtils.map2Json(mentionsMap);
                bodyObj.put("mentions", mentionsObj);
            }
            bodyObj.put(TMP_ID_TAG, fakeMessage.getId());
            bodyObj.put("whispers", JSONUtils.toJSONArray(msgContentTextPlain.getWhisperUsers()));
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(fakeMessage.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChatTextBurnMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + fakeMessage.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            MsgContentTextPlain msgContentTextPlain = fakeMessage.getMsgContentTextPlain();
            bodyObj.put("type", Message.MESSAGE_TYPE_TEXT_PLAIN);
            bodyObj.put("text", msgContentTextPlain.getText());
            bodyObj.put("messageType", Message.MESSAGE_TYPE_TEXT_BURN);
            bodyObj.put(TMP_ID_TAG, fakeMessage.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(fakeMessage.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChatCommentTextPlainMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + fakeMessage.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", "comment/text-plain");
            MsgContentComment msgContentComment = fakeMessage.getMsgContentComment();
            bodyObj.put("text", msgContentComment.getText());
            bodyObj.put("message", msgContentComment.getMessage());
            Map<String, String> mentionsMap = msgContentComment.getMentionsMap();
            if (mentionsMap != null && mentionsMap.size() > 0) {
                JSONObject mentionsObj = JSONUtils.map2Json(mentionsMap);
                bodyObj.put("mentions", mentionsObj);
            }
            bodyObj.put(TMP_ID_TAG, fakeMessage.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(fakeMessage.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendChatRegularFileMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + fakeMessage.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", "file/regular-file");
            bodyObj.put("category", CommunicationUtils.getChatFileCategory(fakeMessage.getMsgContentAttachmentFile().getName()));
            bodyObj.put("name", fakeMessage.getMsgContentAttachmentFile().getName());
            bodyObj.put("size", fakeMessage.getMsgContentAttachmentFile().getSize());
            bodyObj.put(MEDIA_TAG, fakeMessage.getMsgContentAttachmentFile().getMedia());
            bodyObj.put(TMP_ID_TAG, fakeMessage.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(fakeMessage.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChatMediaVoiceMsg(Message message) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + message.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, message.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", Message.MESSAGE_TYPE_MEDIA_VOICE);
            bodyObj.put("duration", message.getMsgContentMediaVoice().getDuration());
            bodyObj.put(MEDIA_TAG, message.getMsgContentMediaVoice().getMedia());
            JSONObject subTitleObj = new JSONObject();
            String language = LanguageManager.getInstance().getCurrentAppLanguage();
            switch (language) {
                case "zh-Hans":
                    subTitleObj.put("zh-cn", message.getMsgContentMediaVoice().getResult());
                    break;
                case "en":
                    subTitleObj.put("en-us", message.getMsgContentMediaVoice().getResult());
                    break;
                default:
                    subTitleObj.put("zh-cn", message.getMsgContentMediaVoice().getResult());
                    break;
            }
            bodyObj.put("subtitles", subTitleObj);
            bodyObj.put(TMP_ID_TAG, message.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(message.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", message);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, message.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChatExtendedLinksMsg(Message message) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + message.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, message.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", "extended/links");
            MsgContentExtendedLinks msgContentExtendedLinks = message.getMsgContentExtendedLinks();
            bodyObj.put("poster", msgContentExtendedLinks.getPoster());
            bodyObj.put("title", msgContentExtendedLinks.getTitle());
            bodyObj.put("subtitle", msgContentExtendedLinks.getSubtitle());
            bodyObj.put("url", msgContentExtendedLinks.getUrl());
            String appName = msgContentExtendedLinks.getAppName();
            if (!TextUtils.isEmpty(appName)) {
                bodyObj.put("app_name", appName);
                bodyObj.put("ico", msgContentExtendedLinks.getIco());
                bodyObj.put("app_url", msgContentExtendedLinks.getAppUrl());
                bodyObj.put("isHaveAPPNavbar", msgContentExtendedLinks.isHaveAPPNavBar());
            }
            bodyObj.put(Constant.WEB_FRAGMENT_SHOW_HEADER, msgContentExtendedLinks.isShowHeader());
            bodyObj.put(TMP_ID_TAG, message.getId());
            JSONArray array = new JSONArray();
            for (RelatedLink relatedLink : msgContentExtendedLinks.getRelatedLinkList()) {
                array.put(relatedLink.toJSonObject());
            }
            bodyObj.put("relatedLinks", array);
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(message.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", message);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, message.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 视频消息
    private void sendChatMediaVideoMsg(Message message) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", CHANNEL_TAG + message.getChannel() + MESSAGE_TAG);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, message.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();

            JSONObject imgObj = new JSONObject();
//            int thumbnailHeight = 0;
//            int thumbnailWidth = 0;
//            ViewGroup.LayoutParams layoutParams = DisplayMediaImageMsg.getImgViewSize(MyApplication.getInstance(),
//                    message.getMsgContentMediaVideo().getImageWidth(), message.getMsgContentMediaVideo().getImageHeight());
//            if (layoutParams.height != 0 && layoutParams.width != 0) {
//                thumbnailWidth = (DensityUtil.px2dip(MyApplication.getInstance(), layoutParams.width) / 2);
//                thumbnailHeight = (DensityUtil.px2dip(MyApplication.getInstance(), layoutParams.height) / 2);
//            }
//            imgObj.put("width", thumbnailWidth);
//            imgObj.put("height", thumbnailHeight);
            imgObj.put("width", message.getMsgContentMediaVideo().getImageWidth());
            imgObj.put("height", message.getMsgContentMediaVideo().getImageHeight());
            imgObj.put(MEDIA_TAG, message.getMsgContentMediaVideo().getImagePath());
            bodyObj.put("thumbnail", imgObj);
            bodyObj.put("type", Message.MESSAGE_TYPE_MEDIA_VIDEO);
            bodyObj.put("duration", message.getMsgContentMediaVideo().getVideoDuration());
            bodyObj.put(MEDIA_TAG, message.getMsgContentMediaVideo().getMedia());
            bodyObj.put("size", message.getMsgContentMediaVideo().getVideoSize());
            bodyObj.put("name", message.getMsgContentMediaVideo().getName());
            bodyObj.put(TMP_ID_TAG, message.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(message.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", message);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, message.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChatMediaImageMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "post");
            actionObj.put("path", "/channel/" + fakeMessage.getChannel() + "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", "media/image");
            JSONObject thumbnailObj = new JSONObject();
            thumbnailObj.put("width", fakeMessage.getMsgContentMediaImage().getThumbnailWidth());
            thumbnailObj.put("height", fakeMessage.getMsgContentMediaImage().getThumbnailHeight());
            thumbnailObj.put("size", fakeMessage.getMsgContentMediaImage().getThumbnailSize());
            thumbnailObj.put("media", fakeMessage.getMsgContentMediaImage().getRawMedia());
            JSONObject previewObj = new JSONObject();
            previewObj.put("width", fakeMessage.getMsgContentMediaImage().getPreviewWidth());
            previewObj.put("height", fakeMessage.getMsgContentMediaImage().getPreviewHeight());
            previewObj.put("size", fakeMessage.getMsgContentMediaImage().getPreviewSize());
            previewObj.put("media", fakeMessage.getMsgContentMediaImage().getRawMedia());
            JSONObject rawObj = new JSONObject();
            rawObj.put("width", fakeMessage.getMsgContentMediaImage().getRawWidth());
            rawObj.put("height", fakeMessage.getMsgContentMediaImage().getRawHeight());
            rawObj.put("size", fakeMessage.getMsgContentMediaImage().getRawSize());
            rawObj.put("media", fakeMessage.getMsgContentMediaImage().getRawMedia());
            bodyObj.put("name", fakeMessage.getMsgContentMediaImage().getName());
            bodyObj.put("preview", previewObj);
            bodyObj.put("thumbnail", thumbnailObj);
            bodyObj.put("raw", rawObj);
            bodyObj.put("tmpId", fakeMessage.getId());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(fakeMessage.getId(), Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage);
//            eventMessage.setTimeout(MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getOfflineMessage(String lastMessageId) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "get");
            actionObj.put("path", MESSAGE_TAG);
            if (lastMessageId != null) {
                JSONObject queryObj = new JSONObject();
                queryObj.put("cursor", lastMessageId);
                actionObj.put("query", queryObj);
            }
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE);
            eventMessage.setTimeout(50);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getChannelRecentMessage() {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "get");
            actionObj.put("path", "/channel/message-with-unread-count");
            JSONObject queryObj = new JSONObject();
            queryObj.put("limit", 100);
            actionObj.put("query", queryObj);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE);
            eventMessage.setTimeout(50);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMessageById(String mid) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "get");
            actionObj.put("path", "/message/" + mid);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getMessageComment(String mid,String cid) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "get");
            actionObj.put("path", "/message/" + mid + "/comment");
            JSONObject channelObj = new JSONObject();
            channelObj.put("channelId", cid);
            actionObj.put("query", channelObj);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送音视频通话消息
     * @param channelId   云+的cid
     * @param room        声网音视频的channelId
     * @param schema      自定义
     * @param type        请求类型，音频或视频VIDEO 或 VOICE
     * @param jsonArray   邀请成员
     * @param action      意图
     */
    public void sendStartVoiceAndVideoCallMessage(String channelId, String room, String schema, String type, JSONArray jsonArray, String action) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "post");
            actionObj.put("path", "/command/server");
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            JSONObject paramObj = new JSONObject();
            paramObj.put("channelId",channelId);
            paramObj.put("room",room);
            paramObj.put("schema",schema);
            paramObj.put("type",type);
            paramObj.put("to",jsonArray);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put(ACTION_TAG, action);
            bodyObj.put("params",paramObj);
            object.put("body",bodyObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 客户端收到指令请求后，通过websocket，返回以下指定格式告知服务端已收到请求
     * @param tracer// 所收到指令请求的tracer
     */
    public void sendReceiveStartVoiceAndVideoCallMessageSuccess(String tracer){
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("status", 200);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("result","ok");
            object.put("body",bodyObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_SEND_VOICE_CALL_MESSAGE);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMessageCommentCount(String mid, String channelId) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "get");
            actionObj.put("path", "/message/" + mid + "/comment/count");
            JSONObject channelObj = new JSONObject();
            channelObj.put("channelId", channelId);
            actionObj.put("query", channelObj);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT_COUNT, "", mid);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHistoryMessage(String cid, String mid, JSONArray excludeUsers) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "get");
            actionObj.put("path", CHANNEL_TAG + cid + MESSAGE_TAG);
            JSONObject queryObj = new JSONObject();
            queryObj.put("before", mid);
            queryObj.put("limit", 20);
            queryObj.put("withStateOfOwnMessages", true);
            queryObj.put("messageStatesExcludedUsers", excludeUsers);
            actionObj.put("query", queryObj);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_HISTORY_MESSAGE, "", cid);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getChannelNewMessage(String cid, JSONArray excludeUsers) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "get");
            actionObj.put("path", CHANNEL_TAG + cid + MESSAGE_TAG);
            JSONObject queryObj = new JSONObject();
            queryObj.put("before", "");
            queryObj.put("limit", 20);
            queryObj.put("withStateOfOwnMessages", true);
            queryObj.put("messageStatesExcludedUsers", excludeUsers);
            actionObj.put("query", queryObj);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            HashMap hashMap = new HashMap();
            hashMap.put("cid", cid);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_NEW_MESSAGE, "", hashMap);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteChannelMessageUnread(String cid) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD_TAG, "delete");
            actionObj.put("path", CHANNEL_TAG + cid + "/message/state/read");
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, tracer);
            object.put(HEADER_TAG, headerObj);
            HashMap hashMap = new HashMap();
            hashMap.put("cid", cid);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_DELETE_UNREAD_MESSAGE, "", hashMap);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAppStatus(String state) {
        try {

            String clientId = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_CLIENTID, "");
            JSONObject object = new JSONObject();
            try {
                String tracer = CommunicationUtils.getTracer();
                JSONObject actionObj = new JSONObject();
                actionObj.put(METHOD_TAG, "put");
                actionObj.put("path", "/client/" + clientId + "/state");
                object.put(ACTION_TAG, actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put(TRACER_TAG, tracer);
                object.put(HEADER_TAG, headerObj);
                JSONObject bodyObject = new JSONObject();
                bodyObject.put("state", state);
                object.put("body", bodyObject);
                if (state.equals("REMOVED")) {
                    EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_WEBSOCKET_STATUS_REMOVE, "", "");
                    WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
                } else {
                    WebSocketPush.getInstance().sendContent(object);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将频道消息置为已读状态
     *
     * @param cid
     */
    public void setChannelMessgeStateRead(String cid) {
        try {

            JSONObject object = new JSONObject();
            try {
                String tracer = CommunicationUtils.getTracer();
                JSONObject actionObj = new JSONObject();
                actionObj.put(METHOD_TAG, "delete");
                actionObj.put("path", CHANNEL_TAG + cid + "/message/state/unread");
                object.put(ACTION_TAG, actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put(TRACER_TAG, tracer);
                object.put(HEADER_TAG, headerObj);
                EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_SET_CHANNEL_MESSAGE_READ, "", "");
                WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 消息撤回
     *
     * @param message
     */
    public void recallMessage(Message message) {
        try {
            JSONObject object = new JSONObject();
            try {
                String tracer = CommunicationUtils.getTracer();
                JSONObject actionObj = new JSONObject();
                actionObj.put(METHOD_TAG, "post");
                actionObj.put("path", "/command/server");
                object.put(ACTION_TAG, actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put(TRACER_TAG, tracer);
                object.put(HEADER_TAG, headerObj);
                JSONObject bodyObject = new JSONObject();
                bodyObject.put(ACTION_TAG, "server.chat.message.recall");
                JSONObject paramsObj = new JSONObject();
                paramsObj.put("messageId", message.getId());
                paramsObj.put("channelId", message.getChannel());
                bodyObject.put("params", paramsObj);
                object.put("body", bodyObject);
                EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_RECALL_MESSAGE, "", message);
                WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 告诉服务端某个命令消息处理完毕
     *
     * @param wsCommand
     */
    public void commandFinishCallback(WSCommand wsCommand) {
        JSONObject object = new JSONObject();
        try {
            JSONObject actionObj = new JSONObject();
            actionObj.put("status", 200);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, wsCommand.getTracer());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObject = new JSONObject();
            bodyObject.put("request", JSONUtils.getJSONObject(wsCommand.getRequest()));
            bodyObject.put("result", "ok");
            object.put("body", bodyObject);
            String tracer = CommunicationUtils.getTracer();
            EventMessage eventMessage = new EventMessage(tracer);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 告诉服务端批量命令消息处理完毕
     *
     * @param wsCommandBatch
     */
    public void commandBatchFinishCallback(WSCommandBatch wsCommandBatch) {
        JSONObject object = new JSONObject();
        try {
            JSONObject actionObj = new JSONObject();
            actionObj.put("status", 200);
            object.put(ACTION_TAG, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE_TAG, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER_TAG, wsCommandBatch.getTracer());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObject = new JSONObject();
            bodyObject.put("request", JSONUtils.getJSONObject(wsCommandBatch.getRequest()));
            List<WSCommand> wsCommandList = wsCommandBatch.getWsCommandList();
            JSONArray array = new JSONArray();
            for (WSCommand wsCommand : wsCommandList) {
                if (wsCommand.getAction().equals("client.chat.message.recall")) {
                    array.put(wsCommand.getTracer());
                }
            }
            bodyObject.put("finished", array);
            object.put("body", bodyObject);
            String tracer = CommunicationUtils.getTracer();
            EventMessage eventMessage = new EventMessage(tracer);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
