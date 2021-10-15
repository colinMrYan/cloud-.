package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.compressor.Compressor;
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
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.DisplayMediaImageMsg;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by chenmch on 2018/4/26.
 */

public class CommunicationUtils {
    /**
     * 获取单聊对方的uid
     *
     * @param context
     * @param title
     * @return
     */
    public static String getDirectChannelOtherUid(Context context, String title) {
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

    /**
     * 获取频道名称
     *
     * @param conversation
     * @return
     */
    public static String getConversationTitle(Conversation conversation) {
        String title = conversation.getName();
        if (conversation.getType().equals(Conversation.TYPE_DIRECT)) {
            title = DirectChannelUtils.getDirectChannelTitle(MyApplication.getInstance(), title);
        } else if (conversation.getType().equals(Conversation.TYPE_CAST)) {
            title = DirectChannelUtils.getRobotInfo(MyApplication.getInstance(), title).getName();
            //智慧城建定制化需求，不合理，且服务改不了
            if ("通知公告".equals(title) &&
                    AppUtils.getManifestAppVersionFlag(MyApplication.getInstance()).equals("zhihuichengjian")) {
                title = "消息到达";
            }
        } else if (conversation.getType().equals(Conversation.TYPE_TRANSFER)) {
            title = BaseApplication.getInstance().getString(R.string.chat_file_transfer);
        }
        return title;
    }

    public static String getRecallMessageShowContent(Message message) {
        boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        String username = "";
        if (isMyMsg) {
            username = BaseApplication.getInstance().getString(R.string.you);
        } else {
            username = '"' + ContactUserCacheUtils.getUserName(message.getFromUser()) + '"';
        }
        return BaseApplication.getInstance().getString(R.string.chat_info_message_status_recall, username);
    }

    public static Message combinLocalTextPlainMessage(String text, String cid, Map<String, String> mentionsMap) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType("text/plain");
        MsgContentTextPlain msgContentTextPlain = new MsgContentTextPlain();
        msgContentTextPlain.setText(text);
        if (mentionsMap != null && mentionsMap.size() > 0) {
            msgContentTextPlain.setMentionsMap(mentionsMap);
        }
        String showContent = ChatMsgContentUtils.mentionsAndUrl2Span(msgContentTextPlain.getText(), msgContentTextPlain.getMentionsMap()).toString();
        message.setShowContent(showContent);
        message.setContent(msgContentTextPlain.toString());
        return message;
    }


    public static Message combinLocalCommentTextPlainMessage(String cid, String commentedMid, String text, Map<String, String> mentionsMap) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType(Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN);
        MsgContentComment msgContentComment = new MsgContentComment();
        msgContentComment.setText(text);
        msgContentComment.setMessage(commentedMid);
        if (mentionsMap != null && mentionsMap.size() > 0) {
            msgContentComment.setMentionsMap(mentionsMap);
        }
        message.setContent(msgContentComment.toString());
        return message;


    }

    public static Message combinLocalRegularFileMessage(String cid, String localFilePath) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType(Message.MESSAGE_TYPE_FILE_REGULAR_FILE);
        message.setLocalPath(localFilePath);
        File file = new File(localFilePath);
        MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
        msgContentRegularFile.setCategory(CommunicationUtils.getChatFileCategory(file.getName()));
        msgContentRegularFile.setName(file.getName());
        msgContentRegularFile.setSize(FileUtils.getFileSize(localFilePath));
        msgContentRegularFile.setMedia(localFilePath);
        message.setContent(msgContentRegularFile.toString());
        return message;
    }


    public static Message combinLocalMediaVoiceMessage(String cid, String localFilePath, int duration, String results) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setLocalPath(localFilePath);
        message.setType(Message.MESSAGE_TYPE_MEDIA_VOICE);
        MsgContentMediaVoice msgContentMediaVoice = new MsgContentMediaVoice();
        msgContentMediaVoice.setDuration(duration);
        msgContentMediaVoice.setMedia(localFilePath);
        msgContentMediaVoice.setJsonResults(results);
        message.setContent(msgContentMediaVoice.toString());
        return message;
    }

    public static Message combinLocalMediaImageMessage(String cid, String localFilePath, Compressor.ResolutionRatio resolutionRatio) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType(Message.MESSAGE_TYPE_MEDIA_IMAGE);
        message.setLocalPath(localFilePath);
        File file = new File(localFilePath);
        Bitmap bitmap = BitmapFactory.decodeFile(localFilePath);
        if (bitmap == null) {
            return null;
        }
        int imgHeight = bitmap.getHeight();
        int imgWidth = bitmap.getWidth();
        long fileSize = FileUtils.getFileSize(localFilePath);
        bitmap.recycle();
        int previewImgHeight = 0;
        int previewImgWidth = 0;
        long previewFileSize = 0;
        String previewImgPath = localFilePath;
        if (resolutionRatio != null) {
            previewImgHeight = resolutionRatio.getHigh();
            previewImgWidth = resolutionRatio.getWidth();
            previewFileSize = resolutionRatio.getSize();
        } else {
            previewImgHeight = imgHeight;
            previewImgWidth = imgWidth;
            previewFileSize = fileSize;
            previewImgPath = localFilePath;
        }
        //还要转回dp/2
        int thumbnailHeight = 0;
        int thumbnailWidth = 0;
        ViewGroup.LayoutParams layoutParams = DisplayMediaImageMsg.getImgViewSize(MyApplication.getInstance(), previewImgWidth, previewImgHeight);
        if (layoutParams.height != 0 && layoutParams.width != 0) {
            thumbnailHeight = (DensityUtil.px2dip(MyApplication.getInstance(), layoutParams.height) / 2);
            thumbnailWidth = (DensityUtil.px2dip(MyApplication.getInstance(), layoutParams.width) / 2);
        }

        MsgContentMediaImage msgContentMediaImage = new MsgContentMediaImage();
        msgContentMediaImage.setName(file.getName());

        msgContentMediaImage.setPreviewHeight(previewImgHeight);
        msgContentMediaImage.setPreviewWidth(previewImgWidth);
        msgContentMediaImage.setPreviewSize(previewFileSize);
        msgContentMediaImage.setPreviewMedia(previewImgPath);

        msgContentMediaImage.setThumbnailHeight(thumbnailHeight);
        msgContentMediaImage.setThumbnailWidth(thumbnailWidth);
        msgContentMediaImage.setThumbnailSize(0);
        msgContentMediaImage.setThumbnailMedia(previewImgPath);

        msgContentMediaImage.setRawHeight(imgHeight);
        msgContentMediaImage.setRawWidth(imgWidth);
        msgContentMediaImage.setRawSize(fileSize);
        msgContentMediaImage.setRawMedia(localFilePath);
        msgContentMediaImage.setTmpId(tracer);
        message.setContent(msgContentMediaImage.toString());
        return message;
    }

    public static Message combineTransmitMediaImageMessage(String cid, String filePath, MsgContentMediaImage msgContentMediaImage) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType(Message.MESSAGE_TYPE_MEDIA_IMAGE);
        message.setLocalPath("");
        MsgContentMediaImage contentMediaImage = new MsgContentMediaImage();
        contentMediaImage.setName(msgContentMediaImage.getName());
        contentMediaImage.setRawHeight(msgContentMediaImage.getRawHeight());
        contentMediaImage.setRawWidth(msgContentMediaImage.getRawWidth());
        contentMediaImage.setRawSize(msgContentMediaImage.getRawSize());
        contentMediaImage.setRawMedia(filePath);
        contentMediaImage.setPreviewHeight(msgContentMediaImage.getPreviewHeight());
        contentMediaImage.setPreviewWidth(msgContentMediaImage.getPreviewWidth());
        contentMediaImage.setPreviewSize(msgContentMediaImage.getPreviewSize());
        contentMediaImage.setPreviewMedia(filePath);
        contentMediaImage.setThumbnailHeight(msgContentMediaImage.getThumbnailHeight());
        contentMediaImage.setThumbnailWidth(msgContentMediaImage.getThumbnailWidth());
        contentMediaImage.setThumbnailSize(msgContentMediaImage.getThumbnailSize());
        contentMediaImage.setThumbnailMedia(filePath);
        contentMediaImage.setTmpId(tracer);
        message.setContent(contentMediaImage.toString());
        return message;
    }

    /***
     *转发文件拼假消息**/
    public static Message combineTransmitRegularFileMessage(String cid, String newPath, MsgContentRegularFile orgMsgContentRegularFile) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType(Message.MESSAGE_TYPE_FILE_REGULAR_FILE);
        message.setLocalPath("");

        MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
        msgContentRegularFile.setCategory(orgMsgContentRegularFile.getCategory());
        msgContentRegularFile.setName(orgMsgContentRegularFile.getName());
        msgContentRegularFile.setSize(orgMsgContentRegularFile.getSize());
        msgContentRegularFile.setMedia(newPath);
        message.setContent(msgContentRegularFile.toString());
        return message;
    }


    public static Message combinLocalExtendedLinksMessage(String cid, String poster, String title, String subTitle, String url) {
        return combinLocalExtendedLinksMessage(cid, poster, title, subTitle, url, false);
    }

    public static Message combinLocalExtendedLinksMessage(String cid, String poster, String title, String subTitle, String url, boolean isShowHeader) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType(Message.MESSAGE_TYPE_EXTENDED_LINKS);
        MsgContentExtendedLinks msgContentExtendedLinks = new MsgContentExtendedLinks();
        msgContentExtendedLinks.setPoster(poster);
        msgContentExtendedLinks.setTitle(title);
        msgContentExtendedLinks.setSubtitle(subTitle);
        msgContentExtendedLinks.setUrl(url);
        msgContentExtendedLinks.setShowHeader(isShowHeader);
        message.setContent(msgContentExtendedLinks.toString());
        return message;
    }

    public static Message combinLocalExtendedLinksMessageHaveContent(String cid, String content) {
        String tracer = getTracer();
        Message message = combinLocalMessageCommon();
        message.setChannel(cid);
        message.setId(tracer);
        message.setTmpId(tracer);
        message.setType(Message.MESSAGE_TYPE_EXTENDED_LINKS);
        message.setContent(content);
        return message;
    }


    public static Message combinLocalReplyAttachmentCardMessage(ContactUser contactUser, String cid, String fromUser) {
        String currentTime = System.currentTimeMillis() + "";
        Message msgRobot = new Message();
        msgRobot.setChannel(cid);
        msgRobot.setMessage("1.0");
        msgRobot.setId(currentTime);
        msgRobot.setTmpId(currentTime);
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
        message.setRead(Message.MESSAGE_READ);
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

    public static void setUserDescText(SearchModel searchModel, TextView textView) {
        setUserDescText(searchModel, textView, true);
    }

    /**
     * 获取用户 英文名 + 机构
     *
     * @param searchModel
     * @param textView
     */
    public static void setUserDescText(SearchModel searchModel, TextView textView, boolean isShowOrg) {
        if (searchModel.getType().equals(SearchModel.TYPE_USER)) {
            String enName = getEnglishName(searchModel);
            String orgName = isShowOrg ? getOrgName(searchModel) : null;
            if (StringUtils.isBlank(enName) && StringUtils.isBlank(orgName)) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                if (StringUtils.isBlank(enName)) {
                    textView.setText(orgName);
                } else if (StringUtils.isBlank(orgName)) {
                    textView.setText(enName);
                } else {
                    textView.setText(enName + "  |  " + orgName);
                }
            }
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    /**
     * 英文名
     */
    public static String getEnglishName(SearchModel searchModel) {
        String englishName = null;
        if (searchModel.getType().equals(SearchModel.TYPE_USER)) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(searchModel.getId());
            if (contactUser != null) {
                englishName = contactUser.getNameGlobal();
                if (!StringUtils.isBlank(englishName)) {
                    if (englishName.equals(contactUser.getName()))
                        return null;
                }

            }
        }

        return englishName;
    }

    /**
     * 获取二 三级组织
     */
    public static String getOrgName(SearchModel searchModel) {
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(searchModel.getId());
        if (contactUser == null) return null;
        String orgNameOrID = contactUser.getParentId();
        if (StringUtils.isBlank(orgNameOrID)) return null;
        String root = "root";
        List<String> orgNameList = new ArrayList<>();
        while (!root.equals(orgNameOrID)) {
            ContactOrg contactOrgTest = ContactOrgCacheUtils.getContactOrg(orgNameOrID);
            if (contactOrgTest == null) return null;
            orgNameOrID = contactOrgTest.getName();
            orgNameList.add(orgNameOrID);
            orgNameOrID = contactOrgTest.getParentId();
        }
        Collections.reverse(orgNameList);
        if (orgNameList.size() > 1) {
            if (orgNameList.size() == 2) {
                return orgNameList.get(1);
            } else {
                return orgNameList.get(1) + "-" + orgNameList.get(2);
            }
        }
        return null;
    }

    public static String getHeadUrl(Conversation conversation) {
        String icon = "";
        if (StringUtils.isBlank(conversation.getId())) {
            return icon;
        }
        if (conversation.getType().equals(Conversation.TYPE_GROUP)) {
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + conversation.getId() + "_100.png1");
            if (file.exists()) {
                icon = "file://" + file.getAbsolutePath();
            }
        } else if (conversation.getType().equals(Conversation.TYPE_TRANSFER)) {
            icon = "drawable://" + R.drawable.ic_file_transfer;
        } else {
            icon = DirectChannelUtils.getDirectChannelIcon(MyApplication.getInstance(), conversation.getName());
        }

        return icon;
    }

    public static String getHeadUrl(SearchModel searchModel) {
        String icon = null;
        String type = searchModel.getType();
        if (type.equals(SearchModel.TYPE_GROUP)) {
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + searchModel.getId() + "_100.png1");
            if (file.exists()) {
                icon = "file://" + file.getAbsolutePath();
            }
        } else if (type.equals(SearchModel.TYPE_DIRECT)) {
            Conversation conversation = ConversationCacheUtils.getConversation(BaseApplication.getInstance(), searchModel.getId());
            icon = DirectChannelUtils.getDirectChannelIcon(MyApplication.getInstance(), conversation.getName());
        } else {
            if (!searchModel.getId().equals("null")) {
                icon = APIUri.getChannelImgUrl(MyApplication.getInstance(), searchModel.getId());
            }
        }
        return icon;
    }

    public static int getDefaultHeadUrl(SearchModel searchModel) {
        Integer defaultIcon = null; // 默认显示图标
        String type = searchModel.getType();
        if (type.equals(SearchModel.TYPE_GROUP)) {
            defaultIcon = R.drawable.icon_channel_group_default;
        } else if (type.equals(SearchModel.TYPE_STRUCT)) {
            defaultIcon = R.drawable.ic_contact_sub_struct;
        } else if (searchModel.getType().equals(SearchModel.TYPE_TRANSFER)) {
            defaultIcon = R.drawable.ic_file_transfer;
        } else {
            defaultIcon = R.drawable.icon_person_default;
        }
        return defaultIcon;
    }

    public static String getName(Context context, Conversation conversation) {
        if (conversation.getType().equals(Conversation.TYPE_GROUP)) {
            return conversation.getName();
        } else if (conversation.getType().equals(Conversation.TYPE_TRANSFER)) {
            return context.getString(R.string.chat_file_transfer);
        } else {
            return DirectChannelUtils.getDirectChannelTitle(context, conversation.getName());
        }
    }

}
