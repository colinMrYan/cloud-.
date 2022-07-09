package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;

import org.json.JSONException;
import org.json.JSONObject;

public class MultiMessageItem {

    public Long sendTime;
    public String sendUserId;
    public String sendUserName;
    public String text;
    public String tmpId;
    public String type;
    public String name;
    public String category;
    public String parent;
    public long size;
    public String media;
    public JSONObject preview;
    public JSONObject thumbnail;
    public JSONObject raw;

    public MultiMessageItem(JSONObject jsonObject) {
        sendTime = jsonObject.optLong("sendTime");
        text = jsonObject.optString("text");
        sendUserId = jsonObject.optString("sendUserId");
        sendUserName = jsonObject.optString("sendUserName");
        parent = jsonObject.optString("parent");
        tmpId = jsonObject.optString("tmpId");
        type = jsonObject.optString("type");
        category = jsonObject.optString("category");
        type = jsonObject.optString("type");
        media = jsonObject.optString("media");
        preview = jsonObject.optJSONObject("preview");
        thumbnail = jsonObject.optJSONObject("thumbnail");
        raw = jsonObject.optJSONObject("raw");
        name = jsonObject.optString("name");
        size = jsonObject.optLong("size");
    }

    public JSONObject transferItemByItemMessageData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("parent", parent);
            JSONObject messageJson = new JSONObject();
            messageJson.put("type", type);
            messageJson.put("tmpId", tmpId);
            messageJson.put("sendTime", sendTime);

            switch (type) {
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    messageJson.put("text", text);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    messageJson.put("name", name);
                    messageJson.put("preview", preview);
                    messageJson.put("thumbnail", thumbnail);
                    messageJson.put("raw", raw);
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    messageJson.put("category", category);
                    messageJson.put("name", name);
                    messageJson.put("size", size);
                    messageJson.put("media", media);
                    break;
            }
            jsonObject.put("messageBody", messageJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public Message transferMessage(String cid) {
        Message message = new Message();
        message.setCreationDate(sendTime);
        message.setTmpId(tmpId);
        message.setId(System.currentTimeMillis() + "");
        message.setFrom(sendUserId);
        message.setType(type);
        message.setChannel(cid);
        switch (type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                message.setContent(text);
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
                msgContentRegularFile.setMedia(media);
                msgContentRegularFile.setSize(size);
                msgContentRegularFile.setName(name);
                message.setContent(JSONUtils.toJSONString(msgContentRegularFile));
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                MsgContentMediaImage msgContentMediaImage = new MsgContentMediaImage();
                msgContentMediaImage.setName(name);
                msgContentMediaImage.setPreviewHeight(preview.optInt("height"));
                msgContentMediaImage.setPreviewWidth(preview.optInt("width"));
                msgContentMediaImage.setPreviewSize(preview.optInt("size"));
                msgContentMediaImage.setPreviewMedia(preview.optString("media"));
                msgContentMediaImage.setThumbnailHeight(thumbnail.optInt("height"));
                msgContentMediaImage.setThumbnailWidth(thumbnail.optInt("width"));
                msgContentMediaImage.setThumbnailSize(thumbnail.optInt("size"));
                msgContentMediaImage.setThumbnailMedia(thumbnail.optString("media"));
                msgContentMediaImage.setRawHeight(raw.optInt("height"));
                msgContentMediaImage.setRawWidth(raw.optInt("width"));
                msgContentMediaImage.setRawSize(raw.optInt("size"));
                msgContentMediaImage.setRawMedia(raw.optString("media"));
                msgContentMediaImage.setTmpId(tmpId);
                message.setContent(msgContentMediaImage.toString());
                break;
        }
        return message;
    }


    public Message transferItemByItemMessage(String cid) {
        Message message = new Message();
        message.setCreationDate(System.currentTimeMillis());
        message.setTmpId(tmpId);
        message.setId(tmpId);
        message.setFrom(BaseApplication.getInstance().getUid());
        message.setType(type);
        message.setChannel(cid);
        switch (type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                message.setContent(text);
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
                msgContentRegularFile.setMedia(media);
                msgContentRegularFile.setSize(size);
                msgContentRegularFile.setName(name);
                msgContentRegularFile.setTmpId(tmpId);
                msgContentRegularFile.setCategory(category);
                message.setContent(JSONUtils.toJSONString(msgContentRegularFile));
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                MsgContentMediaImage msgContentMediaImage = new MsgContentMediaImage();
                msgContentMediaImage.setName(name);
                msgContentMediaImage.setPreviewHeight(preview.optInt("height"));
                msgContentMediaImage.setPreviewWidth(preview.optInt("width"));
                msgContentMediaImage.setPreviewSize(preview.optInt("size"));
                msgContentMediaImage.setPreviewMedia(preview.optString("media"));
                msgContentMediaImage.setThumbnailHeight(thumbnail.optInt("height"));
                msgContentMediaImage.setThumbnailWidth(thumbnail.optInt("width"));
                msgContentMediaImage.setThumbnailSize(thumbnail.optInt("size"));
                msgContentMediaImage.setThumbnailMedia(thumbnail.optString("media"));
                msgContentMediaImage.setRawHeight(raw.optInt("height"));
                msgContentMediaImage.setRawWidth(raw.optInt("width"));
                msgContentMediaImage.setRawSize(raw.optInt("size"));
                msgContentMediaImage.setRawMedia(raw.optString("media"));
                msgContentMediaImage.setTmpId(tmpId);
                message.setContent(msgContentMediaImage.toString());
                break;
        }
        return message;
    }
}
