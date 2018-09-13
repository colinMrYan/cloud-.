package com.inspur.emmcloud.api.apiservice;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentComment;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.RelatedLink;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by chenmch on 2018/4/28.
 */

public class WSAPIService {
    private static WSAPIService apiService = null;

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

    public WSAPIService() {
    }

    public void sendChatTextPlainMsg(Message fakeMessage) {
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
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage.getId());
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendChatCommentTextPlainMsg(Message fakeMessage) {
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
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage.getId());
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendChatRegularFileMsg(String cid, String tracer, VolumeFile volumeFile) {
        try {
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "post");
            actionObj.put("path", "/channel/" + cid + "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("type", "file/regular-file");
            bodyObj.put("category", CommunicationUtils.getChatFileCategory(volumeFile.getName()));
            bodyObj.put("name", volumeFile.getName());
            bodyObj.put("size", volumeFile.getSize());
            bodyObj.put("media", volumeFile.getPath());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", tracer);
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, tracer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendChatMediaVoiceMsg(Message message, VolumeFile volumeFile) {
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
            bodyObj.put("media", volumeFile.getPath());
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", message.getId());
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, message.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendChatExtendedLinksMsg(Message message) {
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
            JSONArray array = new JSONArray();
            for (RelatedLink relatedLink : msgContentExtendedLinks.getRelatedLinkList()) {
                array.put(relatedLink.toJSonObject());
            }
            bodyObj.put("relatedLinks", array);
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", message.getId());
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, message.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendChatMediaImageMsg(VolumeFile volumeFile, Message fakeMessage) {
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
            bodyObj.put("name", volumeFile.getName());
            JSONObject thumbnailObj = new JSONObject();
            thumbnailObj.put("width", fakeMessage.getMsgContentMediaImage().getThumbnailWidth());
            thumbnailObj.put("height", fakeMessage.getMsgContentMediaImage().getThumbnailHeight());
            thumbnailObj.put("size", fakeMessage.getMsgContentMediaImage().getThumbnailSize());
            thumbnailObj.put("media", volumeFile.getPath());
            JSONObject previewObj = new JSONObject();
            previewObj.put("width", fakeMessage.getMsgContentMediaImage().getPreviewWidth());
            previewObj.put("height", fakeMessage.getMsgContentMediaImage().getPreviewHeight());
            previewObj.put("size", fakeMessage.getMsgContentMediaImage().getPreviewSize());
            previewObj.put("media", volumeFile.getPath());
            JSONObject rawObj = new JSONObject();
            rawObj.put("width", fakeMessage.getMsgContentMediaImage().getRawWidth());
            rawObj.put("height", fakeMessage.getMsgContentMediaImage().getRawHeight());
            rawObj.put("size", fakeMessage.getMsgContentMediaImage().getRawSize());
            rawObj.put("media", volumeFile.getPath());
            bodyObj.put("name", volumeFile.getName());
            bodyObj.put("preview", previewObj);
            bodyObj.put("thumbnail", thumbnailObj);
            bodyObj.put("raw", rawObj);
            object.put("body", bodyObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, "", fakeMessage.getId());
            WebSocketPush.getInstance().sendEventMessage(eventMessage, object, fakeMessage.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getOfflineMessage() {
        try {
            String tracer = CommunicationUtils.getTracer();
            JSONObject object = new JSONObject();
            JSONObject actionObj = new JSONObject();
            actionObj.put("method", "get");
            actionObj.put("path", "/message");
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE);
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
            queryObj.put("limit", 15);
            actionObj.put("query", queryObj);
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE);
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
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID);
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
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT);
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
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT_COUNT, "", mid);
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
            queryObj.put("limit", 15);
            actionObj.put("query", queryObj);
            object.put("action", actionObj);
            JSONObject headerObj = new JSONObject();
            headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
            headerObj.put("tracer", tracer);
            object.put("headers", headerObj);
            EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_GET_HISTORY_MESSAGE, "", "");
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
                    EventMessage eventMessage = new EventMessage(Constant.EVENTBUS_TAG_WEBSOCKET_STATUS_REMOVE, "", "");
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
     * @param cid
     */
    public void setChannelMessgeStateRead(String cid) {
        try {

            JSONObject object = new JSONObject();
            try {
                String tracer = CommunicationUtils.getTracer();
                JSONObject actionObj = new JSONObject();
                actionObj.put("method", "post");
                actionObj.put("path", "/channel/" + cid + "/message/state/read");
                object.put("action", actionObj);
                JSONObject headerObj = new JSONObject();
                headerObj.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
                headerObj.put("tracer", tracer);
                object.put("headers", headerObj);
                WebSocketPush.getInstance().sendContent(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
