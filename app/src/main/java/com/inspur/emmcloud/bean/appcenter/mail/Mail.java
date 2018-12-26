package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenmch on 2018/12/25.
 */

public class Mail implements Serializable{
    private String id;
    private String subject;
    private String displaySender;
    private String displayTo;
    private String textBody;
    private boolean isRead;
    private boolean isEncrypted;
    private int size;
    private long  creationTimestamp;
    private boolean hasAttachments;
    public Mail(){

    }

    public Mail(JSONObject object){
        id= JSONUtils.getString(object,"id","");
        subject= JSONUtils.getString(object,"subject","");
        displaySender= JSONUtils.getString(object,"displaySender","");
        displayTo= JSONUtils.getString(object,"displayTo","");
        textBody= JSONUtils.getString(object,"textBody","");
        isRead= JSONUtils.getBoolean(object,"isRead",false);
        isEncrypted= JSONUtils.getBoolean(object,"isEncrypted",false);
        hasAttachments=JSONUtils.getBoolean(object,"hasAttachments",false);
        size= JSONUtils.getInt(object,"size",0);
        creationTimestamp = JSONUtils.getLong(object,"creationTimestamp",0L);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDisplaySender() {
        return displaySender;
    }

    public void setDisplaySender(String displaySender) {
        this.displaySender = displaySender;
    }

    public String getDisplayTo() {
        return displayTo;
    }

    public void setDisplayTo(String displayTo) {
        this.displayTo = displayTo;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }


    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
