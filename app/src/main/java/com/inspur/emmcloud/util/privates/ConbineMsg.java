package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Email;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentCard;
import com.inspur.emmcloud.bean.chat.Phone;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.imp.util.compressor.Compressor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConbineMsg {

    public static Msg conbineResImgMsg(String filePath) {
        int imgHeight = 0;
        int imgWidth = 0;
        Bitmap bitmapImg = BitmapFactory.decodeFile(filePath);
        imgHeight = bitmapImg.getHeight();
        imgWidth = bitmapImg.getWidth();
        bitmapImg.recycle();
        if (imgHeight > MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE || imgWidth > MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE) {
            String fileName = System.currentTimeMillis() + ".jpg";
            try {
                new Compressor(MyApplication.getInstance()).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_CACHE_PATH).compressToFile(new File(filePath), fileName);
                filePath = MyAppConfig.LOCAL_CACHE_PATH + fileName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JSONObject jsonObject = new JSONObject();
        //暂时为要显示的消息设定一个假的id
        String fakeMessageId = System.currentTimeMillis() + "";
        String uploadFilePath = filePath;
        File uploadFile = new File(uploadFilePath);
        bitmapImg = BitmapFactory.decodeFile(filePath);
        imgHeight = bitmapImg.getHeight();
        imgWidth = bitmapImg.getWidth();
        try {
            jsonObject.put("tmpId", AppUtils.getMyUUID(MyApplication.getInstance()));
            jsonObject.put("key", uploadFilePath);
            jsonObject.put("name",
                    uploadFile.getName());
            jsonObject.put("size", uploadFile.length());
            jsonObject.put("type", "Photos");
            jsonObject.put("height", imgHeight);
            jsonObject.put("width", imgWidth);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        bitmapImg.recycle();
        return conbineCommonMsg(jsonObject.toString(),
                "res_image", fakeMessageId);
    }

    public static Msg conbineResFileMsg(String filePath) {
        File tempFile = new File(filePath);
        String fileMime = FileUtils.getMimeType(tempFile);
        String fileName = tempFile.getName();
        if (StringUtils.isBlank(FileUtils.getSuffix(tempFile))) {
            ToastUtils.show(MyApplication.getInstance(),
                    MyApplication.getInstance().getString(R.string.not_support_upload));
            return null;
        }
        String fakeMessageId = System.currentTimeMillis() + "";


        // 组织文件卡片数据
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("key", filePath);
            jsonObject.put("name", fileName);
            jsonObject.put("size", tempFile.length());
            jsonObject.put("type", fileMime);
            jsonObject.put("tmpId", AppUtils.getMyUUID(MyApplication.getInstance()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return conbineCommonMsg(jsonObject.toString(),
                "res_file", fakeMessageId);
    }


    /**
     * 组织消息添加到频道消息列表
     *
     * @param body
     * @return
     */
    public static Msg conbineCommonMsg(String body, String type, String fakeMessageId) {
        String userID = MyApplication.getInstance().getUid();
        Msg msgSend = new Msg();
        String userName = PreferencesUtils.getString(MyApplication.getInstance(),
                "userRealName");
        msgSend.setMid(fakeMessageId);
        msgSend.setTitle(userName);
        msgSend.setType(type);
        msgSend.setUid(userID);
        msgSend.setAvatar("");
        msgSend.setTime(System.currentTimeMillis());
        msgSend.setBody(body);
        return msgSend;
    }


    public static Msg conbineRobotMsg(Context context, String body, String robotUid, String type, String fakeMessageId) {
        Msg msgSend = new Msg();
        String title = RobotCacheUtils.getRobotById(context, robotUid).getName();
        msgSend.setMid(fakeMessageId);
        msgSend.setTitle(title);
        msgSend.setType(type);
        msgSend.setUid(robotUid);
        msgSend.setAvatar("");
        msgSend.setTime(System.currentTimeMillis());
        msgSend.setBody(body);
        return msgSend;
    }

    public static Message conbineReplyAttachmentCardMsg(ContactUser contactUser, String cid, String from, String fakeMessageId) {
        Message msgRobot = new Message();
        msgRobot.setChannel(cid);
        msgRobot.setMessage("1.0");
        msgRobot.setId(fakeMessageId);
        msgRobot.setCreationDate(System.currentTimeMillis());
        msgRobot.setType("attachment/card");
        JSONObject fromObj = new JSONObject();
        try {
            fromObj.put("user", from);
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
        msgContentAttachmentCard.setOrganization("");
        msgContentAttachmentCard.setTitle("");
        msgContentAttachmentCard.setUid(contactUser.getId());
        Email email = new Email("工作", contactUser.getEmail());
        List<Email> emailList = new ArrayList<>();
        emailList.add(email);
        msgContentAttachmentCard.setEmailList(emailList);
        Phone phone = new Phone("工作", contactUser.getMobile());
        List<Phone> phoneList = new ArrayList<>();
        phoneList.add(phone);
        msgContentAttachmentCard.setPhoneList(phoneList);
        msgRobot.setContent(msgContentAttachmentCard.toString());
        return msgRobot;
    }
}
