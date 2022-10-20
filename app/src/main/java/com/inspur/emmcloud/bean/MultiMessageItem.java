package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVideo;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MultiMessageItem {

    public Long sendTime;
    public String sendUserId;
    public String sendUserName;
    public String text;
    public String tmpId;
    public String type;
//    public String name;
    public String content;
    public String parent;
    //    public String category;
//    public long size;
//    public String media;
//    public JSONObject preview;
//    public JSONObject thumbnail;
//    public JSONObject raw;

    public MultiMessageItem(JSONObject jsonObject) {
        sendTime = jsonObject.optLong("sendTime");
        text = jsonObject.optString("text");
        sendUserId = jsonObject.optString("sendUserId");
        sendUserName = jsonObject.optString("sendUserName");
        parent = jsonObject.optString("parent");
        tmpId = jsonObject.optString("tmpId");
        type = jsonObject.optString("type");
        content = jsonObject.toString();
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
                    CommunicationUtils.wrapperImageSendMessageJSONWithoutTmpId(messageJson, new MsgContentMediaImage(content));
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    CommunicationUtils.wrapperFileSendMessageJSONWithoutTmpId(messageJson, new MsgContentRegularFile(content));
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                    CommunicationUtils.wrapperLinkedSendMessageJSONWithoutTmpId(messageJson, new MsgContentExtendedLinks(content));
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VIDEO:
                    CommunicationUtils.wrapperVideoSendMessageJSONWithoutTmpId(messageJson, new MsgContentMediaVideo(content));
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
        message.setContent(content);
        return message;
    }
}
