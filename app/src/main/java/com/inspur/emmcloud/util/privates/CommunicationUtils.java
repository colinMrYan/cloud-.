package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.Email;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentCard;
import com.inspur.emmcloud.bean.chat.MsgContentComment;
import com.inspur.emmcloud.bean.chat.MsgContentExtendedLinks;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.Phone;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;

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

    /**
     * 获取单聊对方的uid
     *
     * @param context
     * @param title
     * @return
     */
    public static String getDirctChannelOtherUid(Context context,String title) {
        String otherUid = "";
        try {
            String[] uidArray = title.split("-");
            String myUid = ((MyApplication) context.getApplicationContext()).getUid();
            if (uidArray[0].equals(myUid)) {
                otherUid = uidArray[1];
            } else {
                otherUid = uidArray[0];
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return otherUid;
    }

    /* *
    * 获取频道名称
    * @param channel
    * @return
    */
    public static String getConversationTitle(Conversation conversation) {
        String title = conversation.getName();
        if (conversation.getType().equals(Conversation.TYPE_DIRECT)){
            title = DirectChannelUtils.getDirectChannelTitle(MyApplication.getInstance(),title);
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

    public static Message combinLocalRegularFileMessage(String cid, String localFilePath) {
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(getTracer());
        message.setType("file/regular-file");
        File file = new File(localFilePath);
        MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
        msgContentRegularFile.setCategory(CommunicationUtils.getChatFileCategory(file.getName()));
        msgContentRegularFile.setName(file.getName());
        msgContentRegularFile.setSize(FileUtils.getFileSize(localFilePath));
        msgContentRegularFile.setMedia(localFilePath);
        message.setContent(msgContentRegularFile.toString());
        return message;
    }

    public static Message combinLocalCommentTextPlainMessage(String cid, String commentedMid, String text, Map<String, String> mentionsMap) {
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(getTracer());
        message.setType("comment/text-plain");
        MsgContentComment msgContentComment = new MsgContentComment();
        msgContentComment.setText(text);
        msgContentComment.setMessage(commentedMid);
        if (mentionsMap != null && mentionsMap.size() > 0) {
            msgContentComment.setMentionsMap(mentionsMap);
        }
        message.setContent(msgContentComment.toString());
        return message;


    }

    public static Message combinLocalExtendedLinksMessage(String cid, String poster, String title, String subTitle, String url) {
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(getTracer());
        message.setType("extended/links");
        MsgContentExtendedLinks msgContentExtendedLinks = new MsgContentExtendedLinks();
        msgContentExtendedLinks.setPoster(poster);
        msgContentExtendedLinks.setTitle(title);
        msgContentExtendedLinks.setSubtitle(subTitle);
        msgContentExtendedLinks.setUrl(url);
        message.setContent(msgContentExtendedLinks.toString());
        return message;
    }

    public static Message combinLocalMediaImageMessage(String cid, String localFilePath) {
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(getTracer());
        message.setType(Message.MESSAGE_TYPE_MEDIA_IMAGE);
        File file = new File(localFilePath);
        Bitmap bitmap = BitmapFactory.decodeFile(localFilePath);
        int imgHeight = bitmap.getHeight();
        int imgWidth = bitmap.getWidth();
        long fileSize = FileUtils.getFileSize(localFilePath);
        bitmap.recycle();
        MsgContentMediaImage msgContentMediaImage = new MsgContentMediaImage();
        msgContentMediaImage.setName(file.getName());
//        msgContentMediaImage.setPreviewHeight(imgHeight);
//        msgContentMediaImage.setPreviewWidth(imgWidth);
//        msgContentMediaImage.setPreviewSize(fileSize);
//        msgContentMediaImage.setPreviewMedia(localFilePath);
//        msgContentMediaImage.setThumbnailHeight(imgHeight);
//        msgContentMediaImage.setThumbnailWidth(imgWidth);
//        msgContentMediaImage.setThumbnailSize(fileSize);
//        msgContentMediaImage.setThumbnailMedia(localFilePath);
        msgContentMediaImage.setRawHeight(imgHeight);
        msgContentMediaImage.setRawWidth(imgWidth);
        msgContentMediaImage.setRawSize(fileSize);
        msgContentMediaImage.setRawMedia(localFilePath);
        message.setContent(msgContentMediaImage.toString());
        return message;
    }


    public static Message combinLocalMediaVoiceMessage(String cid, String localFilePath, int duration, String results) {
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(getTracer());
        message.setType(Message.MESSAGE_TYPE_MEDIA_VOICE);
        MsgContentMediaVoice msgContentMediaVoice = new MsgContentMediaVoice();
        msgContentMediaVoice.setDuration(duration);
        msgContentMediaVoice.setMedia(localFilePath);
        msgContentMediaVoice.setJsonResults(results);
        message.setContent(msgContentMediaVoice.toString());
        return message;
    }


    public static Message combinLocalReplyAttachmentCardMessage(ContactUser contactUser, String cid, String fromUser) {
        Message msgRobot = new Message();
        msgRobot.setChannel(cid);
        msgRobot.setMessage("1.0");
        msgRobot.setId(System.currentTimeMillis() + "");
        msgRobot.setCreationDate(System.currentTimeMillis());
        msgRobot.setType(Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD);
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
        msgContentAttachmentCard.setAvatar(APIUri.getChannelImgUrl(MyApplication.getInstance(), contactUser.getId()));
        msgContentAttachmentCard.setFirstName(contactUser.getName());
        msgContentAttachmentCard.setLastName("");
        String contactOrgName = "";
        ContactOrg contactOrg = ContactOrgCacheUtils.getContactOrg(contactUser.getParentId());
        if (contactOrg != null) {
            contactOrgName = contactOrg.getName();
        }
        msgContentAttachmentCard.setOrganization(contactOrgName);
        msgContentAttachmentCard.setTitle("");
        msgContentAttachmentCard.setUid(contactUser.getId());
        Email email = new Email(MyApplication.getInstance().getResources().getString(R.string.work), contactUser.getEmail());
        List<Email> emailList = new ArrayList<>();
        emailList.add(email);
        msgContentAttachmentCard.setEmailList(emailList);
        Phone phone = new Phone(MyApplication.getInstance().getResources().getString(R.string.work), contactUser.getMobile());
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
                break;
            default:
                fileCategory = "Unknown";
                break;

        }
        return fileCategory;

    }


}
