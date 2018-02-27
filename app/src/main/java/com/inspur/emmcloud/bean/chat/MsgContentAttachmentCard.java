package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

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
    private List<Email> emailList = new ArrayList<>();
    private List<Phone> phoneList = new ArrayList<>();
    public MsgContentAttachmentCard(String content){
        JSONObject object = JSONUtils.getJSONObject(content);
        firstName = JSONUtils.getString(object,"firstName","");
        lastName = JSONUtils.getString(object,"lastName","");
        avatar = JSONUtils.getString(object,"avatar","");
        organization = JSONUtils.getString(object,"organization","");
        title = JSONUtils.getString(object,"title","");
        JSONArray emailArray = JSONUtils.getJSONArray(object,"email",new JSONArray());
        for (int i=0;i<emailArray.length();i++){
            emailList.add(new Email(JSONUtils.getJSONObject(emailArray,i,new JSONObject())));
        }
        JSONArray phoneArray = JSONUtils.getJSONArray(object,"phone",new JSONArray());
        for (int i=0;i<phoneArray.length();i++){
            phoneList.add(new Phone(JSONUtils.getJSONObject(phoneArray,i,new JSONObject())));
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

    public class Email{
        private String category;
        private String address;
        public Email(JSONObject obj){
            category = JSONUtils.getString(obj,"category","");
            address = JSONUtils.getString(obj,"address","");
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public class Phone{
        private String category;
        private String number;
        public Phone(JSONObject obj){
            category = JSONUtils.getString(obj,"category","");
            number = JSONUtils.getString(obj,"number","");
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }
    }
}
