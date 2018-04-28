package com.inspur.emmcloud.api.apiservice;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by chenmch on 2018/4/28.
 */

public class WSAPIService {
    private static WSAPIService apiService = null;
    private static final String EVENT = "com.inspur.ecm.chat";

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

    public void sendChatTextPlainMsg(String content, String cid, Map<String, String> mentionsMap, String tracer) {
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
            bodyObj.put("type", "text/plain");
            bodyObj.put("text", content);
            if (mentionsMap != null && mentionsMap.size() > 0) {
                JSONObject mentionsObj = JSONUtils.map2Json(mentionsMap);
                bodyObj.put("mentions", mentionsObj);
            }
            object.put("body", bodyObj);
            WebSocketPush.getInstance().sendWSMessage(EVENT, object, Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE);
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
            LogUtils.jasonDebug("object=" + object.toString());
            WebSocketPush.getInstance().sendWSMessage(EVENT, object, Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendChatMediaImageMsg(String cid, String tracer, VolumeFile volumeFile, Message fakeMessage) {
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
            LogUtils.jasonDebug("object=" + object.toString());
            WebSocketPush.getInstance().sendWSMessage(EVENT, object, Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE);

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
            WebSocketPush.getInstance().sendWSMessage(EVENT, object, Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE);
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
            WebSocketPush.getInstance().sendWSMessage(EVENT, object, Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
