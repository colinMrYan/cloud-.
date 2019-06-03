package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentAttachmentCard {
    private String firstName;
    private String lastName;
    private String avatar;
    private String organization;
    private String title;
    private String uid;
    private String tmpId;
    private List<Email> emailList = new ArrayList<>();
    private List<Phone> phoneList = new ArrayList<>();

    public MsgContentAttachmentCard() {

    }

    public MsgContentAttachmentCard(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        firstName = JSONUtils.getString(object, "firstName", "");
        lastName = JSONUtils.getString(object, "lastName", "");
        avatar = JSONUtils.getString(object, "avatar", "");
        organization = JSONUtils.getString(object, "organization", "");
        title = JSONUtils.getString(object, "title", "");
        uid = JSONUtils.getString(object, "uid", "");
        tmpId = JSONUtils.getString(object, "tmpId", "");
        JSONArray emailArray = JSONUtils.getJSONArray(object, "email", new JSONArray());
        for (int i = 0; i < emailArray.length(); i++) {
            emailList.add(new Email(JSONUtils.getJSONObject(emailArray, i, new JSONObject())));
        }
        JSONArray phoneArray = JSONUtils.getJSONArray(object, "phone", new JSONArray());
        for (int i = 0; i < phoneArray.length(); i++) {
            phoneList.add(new Phone(JSONUtils.getJSONObject(phoneArray, i, new JSONObject())));
        }

    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Email> getEmailList() {
        return emailList;
    }

    public void setEmailList(List<Email> emailList) {
        this.emailList = emailList;
    }

    public List<Phone> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(List<Phone> phoneList) {
        this.phoneList = phoneList;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("firstName", firstName);
            obj.put("lastName", lastName);
            obj.put("avatar", avatar);
            obj.put("organization", organization);
            obj.put("title", title);
            obj.put("uid", uid);
            JSONArray emailArray = new JSONArray();
            for (int i = 0; i < emailList.size(); i++) {
                emailArray.put(i, emailList.get(i).toJSONObject());
            }
            if (emailArray.length() > 0) {
                obj.put("email", emailArray);
            }

            JSONArray phoneArray = new JSONArray();
            for (int i = 0; i < phoneList.size(); i++) {
                phoneArray.put(i, phoneList.get(i).toJSONObject());
            }
            if (phoneArray.length() > 0) {
                obj.put("phone", phoneArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
