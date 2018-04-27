package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.Email;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentCard;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.Phone;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.common.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by chenmch on 2018/4/26.
 */

public class CommunicationUtils {
    /* *
    * 获取频道名称
    * @param channel
    * @return
    */
    public static String getChannelDisplayTitle(Channel channel) {

        String title;
        if (channel.getType().equals("DIRECT")) {
            title = DirectChannelUtils.getDirectChannelTitle(MyApplication.getInstance(),
                    channel.getTitle());
        } else if (channel.getType().equals("SERVICE")) {
            title = DirectChannelUtils.getRobotInfo(MyApplication.getInstance(), channel.getTitle()).getName();
        } else {
            title = channel.getTitle();
        }
        return title;
    }


    public static Message combinLocalTextPlainMessage(String text, String cid, Map<String, String> mentionsMap) {
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(getTracer());
        message.setType("text/plain");
        MsgContentTextPlain msgContentTextPlain = new MsgContentTextPlain();
        msgContentTextPlain.setText(text);
        if (mentionsMap != null && mentionsMap.size() > 0) {
            msgContentTextPlain.setMentionsMap(mentionsMap);
        }
        message.setContent(msgContentTextPlain.toString());
        return message;
    }

    public static Message combinLocalRegularFileMessage(String cid,String localFilePath) {
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(getTracer());
        message.setType("file/regular-file");
        File file = new File(localFilePath);
        MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
        msgContentRegularFile.setCategory(CommunicationUtils.getChatFileCategory(file.getName()));
        msgContentRegularFile.setName(file.getName());
        msgContentRegularFile.setSize(FileUtils.getFileSize(localFilePath));
        msgContentRegularFile.setMedia("");
        message.setContent(msgContentRegularFile.toString());
        return message;
    }

    public static Message combinLocalReplyAttachmentCardMessage(Contact contact, String cid, String fromUser) {
        Message msgRobot = new Message();
        msgRobot.setChannel(cid);
        msgRobot.setMessage("1.0");
        msgRobot.setId(System.currentTimeMillis() + "");
        msgRobot.setCreationDate(System.currentTimeMillis());
        msgRobot.setType("attachment/card");
        JSONObject fromObj = new JSONObject();
        try {
            fromObj.put("user", fromUser);
            fromObj.put("enterprise", MyApplication.getInstance().getTanent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        msgRobot.setFrom(fromObj.toString());
        msgRobot.setTo("");
        msgRobot.setState("");
        MsgContentAttachmentCard msgContentAttachmentCard = new MsgContentAttachmentCard();
        msgContentAttachmentCard.setAvatar(APIUri.getChannelImgUrl(MyApplication.getInstance(), contact.getInspurID()));
        msgContentAttachmentCard.setFirstName(contact.getRealName());
        msgContentAttachmentCard.setLastName("");
        msgContentAttachmentCard.setOrganization(contact.getOrgName());
        msgContentAttachmentCard.setTitle("");
        msgContentAttachmentCard.setUid(contact.getInspurID());
        Email email = new Email(MyApplication.getInstance().getResources().getString(R.string.work), contact.getEmail());
        List<Email> emailList = new ArrayList<>();
        emailList.add(email);
        msgContentAttachmentCard.setEmailList(emailList);
        Phone phone = new Phone(MyApplication.getInstance().getResources().getString(R.string.work), contact.getMobile());
        List<Phone> phoneList = new ArrayList<>();
        phoneList.add(phone);
        msgContentAttachmentCard.setPhoneList(phoneList);
        msgRobot.setContent(msgContentAttachmentCard.toString());
        return msgRobot;
    }

    /**
     * 组装本地消息通用部分
     *
     * @return
     */
    private static Message combinLocalMessageCommon() {
        Message message = new Message();
        message.setMessage("1.0");
        JSONObject fromObj = new JSONObject();
        try {
            fromObj.put("user", MyApplication.getInstance().getUid());
            fromObj.put("enterprise", MyApplication.getInstance().getTanent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        message.setFrom(fromObj.toString());
        message.setTo("");
        message.setState("");
        message.setCreationDate(System.currentTimeMillis());
        return message;
    }

    public static String getTracer() {
        return "a" + UUID.randomUUID();
    }

    public static String getChatFileCategory(String fileName) {
        String fileCategory;
        String fileExtension = FileUtils.getFileExtension(fileName);
        fileExtension = fileExtension.toLowerCase();
        switch (fileExtension) {
            case "txt":
            case "log":
                fileCategory = "Plain Text";
                break;
            case "jpg":
            case "gif":
            case "png":
            case "jpeg":
            case "bmp":
                fileCategory = "Photos";
                break;
            case "pdf":
                fileCategory = "PDF";
                break;
            case "doc":
            case "docx":
                fileCategory = "Microsoft Word";
                break;
            case "xls":
            case "xlsx":
                fileCategory = "Microsoft Excel";
                break;
            case "ppt":
            case "pptx":
                fileCategory = "Microsoft PPT";
                ;
                break;
            default:
                fileCategory = "Unknown";
                break;

        }
        return fileCategory;

    }


}
