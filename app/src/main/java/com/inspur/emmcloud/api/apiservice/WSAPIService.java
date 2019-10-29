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
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.RelatedLink;
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
        }

    }

    private void sendChatTextPlainMsg(Message fakeMessage) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "post");
            actionObj.put("path", "/channel/" + fakeMessage.getChannel() + "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", fakeMessage.getId());
            object.put("headers", headerObj);
            JSONObject bodyObj = new JSONObject();
            MsgContentTextPlain msgContentTextPlain = fakeMessage.getMsgContentTextPlain();
            bodyObj.put("type", Message.MESSAGE_TYPE_TEXT_PLAIN);
            bodyObj.put("text", msgContentTextPlain.getText());
            Map<String, String> mentionsMap = msgContentTextPlain.getMentionsMap();
            if (mentionsMap != null && mentionsMap.size() > 0) {
                JSONObject mentionsObj = JSONUtils.map2Json(mentionsMap);
                bodyObj.put("mentions", mentionsObj);
            }
            bodyObj.put("tmpId", fakeMessage.getId());
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
            actionObj.put("method", "post");
            actionObj.put("path", "/channel/" + fakeMessage.getChannel() + "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", fakeMessage.getId());
            object.put("headers", headerObj);
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
            bodyObj.put("tmpId", fakeMessage.getId());
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
            actionObj.put("method", "post");
            actionObj.put("path", "/channel/" + fakeMessage.getChannel() + "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", fakeMessage.getId());
            object.put("headers", headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", "file/regular-file");
            bodyObj.put("category", CommunicationUtils.getChatFileCategory(fakeMessage.getMsgContentAttachmentFile().getName()));
            bodyObj.put("name", fakeMessage.getMsgContentAttachmentFile().getName());
            bodyObj.put("size", fakeMessage.getMsgContentAttachmentFile().getSize());
            bodyObj.put("media", fakeMessage.getMsgContentAttachmentFile().getMedia());
            bodyObj.put("tmpId", fakeMessage.getId());
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
            actionObj.put("method", "post");
            actionObj.put("path", "/channel/" + message.getChannel() + "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", message.getId());
            object.put("headers", headerObj);
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
            bodyObj.put("tmpId", message.getId());
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
            actionObj.put("method", "post");
            actionObj.put("path", "/channel/" + message.getChannel() + "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", message.getId());
            object.put("headers", headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", "extended/links");
            MsgContentExtendedLinks msgContentExtendedLinks = message.getMsgContentExtendedLinks();
            bodyObj.put("poster", msgContentExtendedLinks.getPoster());
            bodyObj.put("title", msgContentExtendedLinks.getTitle());
            bodyObj.put("subtitle", msgContentExtendedLinks.getSubtitle());
            bodyObj.put("url", msgContentExtendedLinks.getUrl());
            bodyObj.put("tmpId", message.getId());
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
            object.put("headers", headerObj);
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
            actionObj.put("method", "get");
            actionObj.put("path", "/message");
            if (lastMessageId != null) {
                JSONObject queryObj = new JSONObject();
                queryObj.put("cursor", lastMessageId);
                actionObj.put("query", queryObj);
            }
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
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
            actionObj.put("method", "get");
            actionObj.put("path", "/channel/message-with-unread-count");
            JSONObject queryObj = new JSONObject();
            queryObj.put("limit", 100);
            actionObj.put("query", queryObj);
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
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
            actionObj.put("method", "get");
            actionObj.put("path", "/message/" + mid);
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getMessageComment(String mid) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "get");
            actionObj.put("path", "/message/" + mid + "/comment");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
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
            actionObj.put("method", "post");
            actionObj.put("path", "/command/server");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            JSONObject paramObj = new JSONObject();
            paramObj.put("channelId",channelId);
            paramObj.put("room",room);
            paramObj.put("schema",schema);
            paramObj.put("type",type);
            paramObj.put("to",jsonArray);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("action", action);
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
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("result","ok");
            object.put("body",bodyObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMessageCommentCount(String mid) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "get");
            actionObj.put("path", "/message/" + mid + "/comment/count");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT_COUNT, "", mid);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHistoryMessage(String cid, String mid) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "get");
            actionObj.put("path", "/channel/" + cid + "/message");
            JSONObject queryObj = new JSONObject();
            queryObj.put("before", mid);
            queryObj.put("limit", 20);
            actionObj.put("query", queryObj);
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_HISTORY_MESSAGE, "", cid);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getChannelNewMessage(String cid, boolean isNeedRefreshConversationList) {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "get");
            actionObj.put("path", "/channel/" + cid + "/message");
            JSONObject queryObj = new JSONObject();
            queryObj.put("before", "");
            queryObj.put("limit", 15);
            actionObj.put("query", queryObj);
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            HashMap hashMap = new HashMap();
            hashMap.put("cid", cid);
            hashMap.put("isNeedRefreshConversationList", isNeedRefreshConversationList);
            EventMessage eventMessage = new EventMessage(tracer, Constant.EVENTBUS_TAG_GET_NEW_MESSAGE, "", hashMap);
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
                actionObj.put("method", "put");
                actionObj.put("path", "/client/" + clientId + "/state");
                object.put("action", actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put("tracer", tracer);
                object.put("headers", headerObj);
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
                actionObj.put("method", "delete");
                actionObj.put("path", "/channel/" + cid + "/message/state/unread");
                object.put("action", actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put("tracer", tracer);
                object.put("headers", headerObj);
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
                actionObj.put("method", "post");
                actionObj.put("path", "/command/server");
                object.put("action", actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put("tracer", tracer);
                object.put("headers", headerObj);
                JSONObject bodyObject = new JSONObject();
                bodyObject.put("action", "server.chat.message.recall");
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
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", wsCommand.getTracer());
            object.put("headers", headerObj);
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
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", wsCommandBatch.getTracer());
            object.put("headers", headerObj);
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
