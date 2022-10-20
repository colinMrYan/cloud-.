package com.inspur.emmcloud.api.apiservice;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.WSCommandBatch;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentComment;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.WSCommand;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenmch on 2018/4/28.
 */

public class WSAPIService {
    private static WSAPIService apiService = null;
    private static final String HEADER_TAG = "headers";
    private static final String METHOD = "method";
    private static final String CHANNEL =  "/channel/";
    private static final String MESSAGE =  "/message";
    private static final String ACTION =  "action";
    private static final String ENTERPRISE =  "enterprise";
    private static final String TRACER =  "tracer";
    private static final String TMP_ID =  "tmpId";

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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + fakeMessage.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, fakeMessage.getId());
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
            bodyObj.put(TMP_ID, fakeMessage.getId());
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + fakeMessage.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, fakeMessage.getId());
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
            bodyObj.put(TMP_ID, fakeMessage.getId());
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + fakeMessage.getChannel() + MESSAGE);
            JSONObject whisperObj = new JSONObject();
            whisperObj.put("to", JSONUtils.toJSONArray(msgContentTextPlain.getWhisperUsers()));
            actionObj.put("query", whisperObj);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", Message.MESSAGE_TYPE_TEXT_PLAIN);
            bodyObj.put("text", msgContentTextPlain.getText());
            Map<String, String> mentionsMap = msgContentTextPlain.getMentionsMap();
            if (mentionsMap != null && mentionsMap.size() > 0) {
                JSONObject mentionsObj = JSONUtils.map2Json(mentionsMap);
                bodyObj.put("mentions", mentionsObj);
            }
            bodyObj.put(TMP_ID, fakeMessage.getId());
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + fakeMessage.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            MsgContentTextPlain msgContentTextPlain = fakeMessage.getMsgContentTextPlain();
            bodyObj.put("type", Message.MESSAGE_TYPE_TEXT_PLAIN);
            bodyObj.put("text", msgContentTextPlain.getText());
            bodyObj.put("messageType", Message.MESSAGE_TYPE_TEXT_BURN);
            bodyObj.put(TMP_ID, fakeMessage.getId());
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + fakeMessage.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, fakeMessage.getId());
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
            bodyObj.put(TMP_ID, fakeMessage.getId());
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + fakeMessage.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put(TMP_ID, fakeMessage.getId());
            object.put("body", CommunicationUtils.wrapperFileSendMessageJSONWithoutTmpId(bodyObj, fakeMessage.getMsgContentAttachmentFile()));
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + message.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, message.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", Message.MESSAGE_TYPE_MEDIA_VOICE);
            bodyObj.put("duration", message.getMsgContentMediaVoice().getDuration());
            bodyObj.put("media", message.getMsgContentMediaVoice().getMedia());
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
            bodyObj.put(TMP_ID, message.getId());
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + message.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, message.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put(TMP_ID, message.getId());
            object.put("body", CommunicationUtils.wrapperLinkedSendMessageJSONWithoutTmpId(bodyObj, message.getMsgContentExtendedLinks()));
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + message.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, message.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put(TMP_ID, message.getId());
            object.put("body", CommunicationUtils.wrapperVideoSendMessageJSONWithoutTmpId(bodyObj, message.getMsgContentMediaVideo()));
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
            actionObj.put(METHOD, "post");
            actionObj.put("path", CHANNEL + fakeMessage.getChannel() + MESSAGE);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, fakeMessage.getId());
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put(TMP_ID, fakeMessage.getId());
            object.put("body", CommunicationUtils.wrapperImageSendMessageJSONWithoutTmpId(bodyObj, fakeMessage.getMsgContentMediaImage()));
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
            actionObj.put(METHOD, "get");
            actionObj.put("path", MESSAGE);
            if (lastMessageId != null) {
                JSONObject queryObj = new JSONObject();
                queryObj.put("cursor", lastMessageId);
                actionObj.put("query", queryObj);
            }
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
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
            actionObj.put(METHOD, "get");
            actionObj.put("path", "/channel/message-with-unread-count");
            JSONObject queryObj = new JSONObject();
            queryObj.put("limit", 100);
            actionObj.put("query", queryObj);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
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
            actionObj.put(METHOD, "get");
            actionObj.put("path", "/message/" + mid);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getMessageComment(String mid, String cid) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD, "get");
            actionObj.put("path", "/message/" + mid + "/comment");
            JSONObject channelObj = new JSONObject();
            channelObj.put("channelId", cid);
            actionObj.put("query", channelObj);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
            object.put(HEADER_TAG, headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送音视频通话消息
     *
     * @param channelId 云+的cid
     * @param room      声网音视频的channelId
     * @param schema    自定义
     * @param type      请求类型，音频或视频VIDEO 或 VOICE
     * @param jsonArray 邀请成员
     * @param action    意图
     */
    public void sendStartVoiceAndVideoCallMessage(String channelId, String room, String schema, String type, JSONArray jsonArray, String action) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put(METHOD, "post");
            actionObj.put("path", "/command/server");
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
            object.put(HEADER_TAG, headerObj);
            JSONObject paramObj = new JSONObject();
            paramObj.put("channelId", channelId);
            paramObj.put("room", room);
            paramObj.put("schema", schema);
            paramObj.put("type", type);
            paramObj.put("to", jsonArray);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put(ACTION, action);
            bodyObj.put("params", paramObj);
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 客户端收到指令请求后，通过websocket，返回以下指定格式告知服务端已收到请求
     *
     * @param tracer// 所收到指令请求的tracer
     */
    public void sendReceiveStartVoiceAndVideoCallMessageSuccess(String tracer) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("status", 200);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
            object.put(HEADER_TAG, headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("result", "ok");
            object.put("body", bodyObj);
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
            actionObj.put(METHOD, "get");
            actionObj.put("path", "/message/" + mid + "/comment/count");
            JSONObject channelObj = new JSONObject();
            channelObj.put("channelId", channelId);
            actionObj.put("query", channelObj);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
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
            actionObj.put(METHOD, "get");
            actionObj.put("path", CHANNEL + cid + MESSAGE);
            JSONObject queryObj = new JSONObject();
            queryObj.put("before", mid);
            queryObj.put("limit", 20);
            queryObj.put("withStateOfOwnMessages", true);
            queryObj.put("messageStatesExcludedUsers", excludeUsers);
            actionObj.put("query", queryObj);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
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
            actionObj.put(METHOD, "get");
            actionObj.put("path", CHANNEL + cid + MESSAGE);
            JSONObject queryObj = new JSONObject();
            queryObj.put("before", "");
            queryObj.put("limit", 20);
            queryObj.put("withStateOfOwnMessages", true);
            queryObj.put("messageStatesExcludedUsers", excludeUsers);
            actionObj.put("query", queryObj);
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
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
            actionObj.put(METHOD, "delete");
            actionObj.put("path", CHANNEL + cid + "/message/state/read");
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, tracer);
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
                actionObj.put(METHOD, "put");
                actionObj.put("path", "/client/" + clientId + "/state");
                object.put(ACTION, actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put(TRACER, tracer);
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
                actionObj.put(METHOD, "delete");
                actionObj.put("path", CHANNEL + cid + "/message/state/unread");
                object.put(ACTION, actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put(TRACER, tracer);
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
                actionObj.put(METHOD, "post");
                actionObj.put("path", "/command/server");
                object.put(ACTION, actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put(TRACER, tracer);
                object.put(HEADER_TAG, headerObj);
                JSONObject bodyObject = new JSONObject();
                bodyObject.put(ACTION, "server.chat.message.recall");
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
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, wsCommand.getTracer());
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
            object.put(ACTION, actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put(ENTERPRISE, MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put(TRACER, wsCommandBatch.getTracer());
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
