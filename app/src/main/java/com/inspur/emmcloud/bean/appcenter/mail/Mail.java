package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/12/25.
 */

@Table(name = "Mail", onCreated = "CREATE INDEX MailIndex ON Mail(id)")
public class Mail implements Serializable {
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "subject")
    private String subject;
    @Column(name = "displaySender")
    private String displaySender;
    @Column(name = "displayTo")
    private String displayTo;
    @Column(name = "isRead")
    private boolean isRead;
    @Column(name = "isEncrypted")
    private boolean isEncrypted;
    @Column(name = "size")
    private int size;
    @Column(name = "creationTimestamp")
    private long creationTimestamp;
    @Column(name = "hasAttachments")
    private boolean hasAttachments;
    @Column(name = "body")
    private String body;
    @Column(name = "from")
    private String from;
    @Column(name = "ccRecipients")
    private String ccRecipients;
    @Column(name = "bccRecipients")
    private String bccRecipients;
    @Column(name = "toRecipients")
    private String toRecipients;
    @Column(name = "isComplete")
    private boolean isComplete = false;//是否是内容完整的Mail，在列表获取的Mail不完整
    @Column(name = "folderId")
    private String folderId;
    @Column(name = "attachments")
    private String attachments;

    private MailRecipient fromMailRecipient;
    private List<MailRecipient> ccMailRecipientList = new ArrayList<>();
    private List<MailRecipient> bccMailRecipientList = new ArrayList<>();
    private List<MailRecipient> toMailRecipientList = new ArrayList<>();
    private List<MailAttachment> mailAttachmentList = new ArrayList<>();
    private int bodyType = -1;
    private String bodyText = "";

    public Mail() {

    }

    public Mail(String response) {
        this(JSONUtils.getJSONObject(response), "");
    }

    public Mail(JSONObject object, String folderId) {
        id = JSONUtils.getString(object, "id", "");
        subject = JSONUtils.getString(object, "subject", "");
        displaySender = JSONUtils.getString(object, "displaySender", "");
        displayTo = JSONUtils.getString(object, "displayTo", "");
        isRead = JSONUtils.getBoolean(object, "isRead", false);
        isEncrypted = JSONUtils.getBoolean(object, "isEncrypted", false);
        hasAttachments = JSONUtils.getBoolean(object, "hasAttachments", false);
        size = JSONUtils.getInt(object, "size", 0);
        creationTimestamp = JSONUtils.getLong(object, "creationTimestamp", 0L);
        body = JSONUtils.getString(object, "body", "");
        if (!body.equals("")) {
            bodyType = JSONUtils.getInt(body, "bodyType", -1);
            bodyText = JSONUtils.getString(body, "text", "");
        }
        isComplete = !body.equals("");
        from = JSONUtils.getString(object, "from", "");
        JSONObject fromObj = JSONUtils.getJSONObject(from);
        fromMailRecipient = new MailRecipient(fromObj);
        ccRecipients = JSONUtils.getString(object, "ccRecipients", "");
        JSONArray ccRecipientArray = JSONUtils.getJSONArray(ccRecipients, new JSONArray());
        for (int i = 0; i < ccRecipientArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(ccRecipientArray, i, new JSONObject());
            ccMailRecipientList.add(new MailRecipient(obj));
        }
        bccRecipients = JSONUtils.getString(object, "bccRecipients", "");
        JSONArray bccRecipientArray = JSONUtils.getJSONArray(bccRecipients, new JSONArray());
        for (int i = 0; i < bccRecipientArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(bccRecipientArray, i, new JSONObject());
            bccMailRecipientList.add(new MailRecipient(obj));
        }
        toRecipients = JSONUtils.getString(object, "toRecipients", "");
        JSONArray toRecipientArray = JSONUtils.getJSONArray(toRecipients, new JSONArray());
        for (int i = 0; i < toRecipientArray.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(toRecipientArray, i, new JSONObject());
            toMailRecipientList.add(new MailRecipient(obj));
        }
        this.folderId = folderId;
        if (hasAttachments) {
            attachments = JSONUtils.getString(object, "attachments", "");
            JSONArray attachmentArray = JSONUtils.getJSONArray(attachments, new JSONArray());
            for (int i = 0; i < attachmentArray.length(); i++) {
                JSONObject obj = JSONUtils.getJSONObject(attachmentArray, i, new JSONObject());
                mailAttachmentList.add(new MailAttachment(obj));
            }
        }
    }

    public MailRecipient getFromMailRecipient() {
        if (fromMailRecipient == null) {
            JSONObject fromObj = JSONUtils.getJSONObject(from);
            fromMailRecipient = new MailRecipient(fromObj);
        }
        return fromMailRecipient;
    }

    public void setFromMailRecipient(MailRecipient fromMailRecipient) {
        this.fromMailRecipient = fromMailRecipient;
    }

    public List<MailRecipient> getCcMailRecipientList() {
        if (ccMailRecipientList.size() == 0) {
            JSONArray ccRecipientArray = JSONUtils.getJSONArray(ccRecipients, new JSONArray());
            for (int i = 0; i < ccRecipientArray.length(); i++) {
                JSONObject obj = JSONUtils.getJSONObject(ccRecipientArray, i, new JSONObject());
                ccMailRecipientList.add(new MailRecipient(obj));
            }
        }
        return ccMailRecipientList;
    }

    public void setCcMailRecipientList(List<MailRecipient> ccMailRecipientList) {
        this.ccMailRecipientList = ccMailRecipientList;
    }

    public List<MailRecipient> getBccMailRecipientList() {
        if (bccMailRecipientList.size() == 0) {
            JSONArray bccRecipientArray = JSONUtils.getJSONArray(bccRecipients, new JSONArray());
            for (int i = 0; i < bccRecipientArray.length(); i++) {
                JSONObject obj = JSONUtils.getJSONObject(bccRecipientArray, i, new JSONObject());
                bccMailRecipientList.add(new MailRecipient(obj));
            }
        }
        return bccMailRecipientList;
    }

    public void setBccMailRecipientList(List<MailRecipient> bccMailRecipientList) {
        this.bccMailRecipientList = bccMailRecipientList;
    }

    public List<MailRecipient> getToMailRecipientList() {
        if (toMailRecipientList.size() == 0) {
            JSONArray toRecipientArray = JSONUtils.getJSONArray(toRecipients, new JSONArray());
            for (int i = 0; i < toRecipientArray.length(); i++) {
                JSONObject obj = JSONUtils.getJSONObject(toRecipientArray, i, new JSONObject());
                toMailRecipientList.add(new MailRecipient(obj));
            }
        }
        return toMailRecipientList;
    }

    public void setToMailRecipientList(List<MailRecipient> toMailRecipientList) {
        this.toMailRecipientList = toMailRecipientList;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public List<MailAttachment> getMailAttachmentList() {
        if (isHasAttachments() && mailAttachmentList.size() == 0) {
            JSONArray attachmentArray = JSONUtils.getJSONArray(attachments, new JSONArray());
            for (int i = 0; i < attachmentArray.length(); i++) {
                JSONObject obj = JSONUtils.getJSONObject(attachmentArray, i, new JSONObject());
                mailAttachmentList.add(new MailAttachment(obj));
            }
        }
        return mailAttachmentList;
    }

    public void setMailAttachmentList(List<MailAttachment> mailAttachmentList) {
        this.mailAttachmentList = mailAttachmentList;
    }

    public List<MailAttachment> getReallyMailAttachmentList() {
        List<MailAttachment> mailAttachmentList = getMailAttachmentList();
        List<MailAttachment> reallyMailAttachmentList = new ArrayList<>();
        for (MailAttachment mailAttachment : mailAttachmentList) {
            if (mailAttachment.isAttachment()) {
                reallyMailAttachmentList.add(mailAttachment);
            }
        }
        return reallyMailAttachmentList;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getCcRecipients() {
        return ccRecipients;
    }

    public void setCcRecipients(String ccRecipients) {
        this.ccRecipients = ccRecipients;
    }

    public String getBccRecipients() {
        return bccRecipients;
    }

    public void setBccRecipients(String bccRecipients) {
        this.bccRecipients = bccRecipients;
    }

    public String getToRecipients() {
        return toRecipients;
    }

    public void setToRecipients(String toRecipients) {
        this.toRecipients = toRecipients;
    }

    public int getBodyType() {
        if (bodyType == -1 && !body.equals("")) {
            bodyType = JSONUtils.getInt(body, "bodyType", -1);
        }
        return bodyType;
    }

    public void setBodyType(int bodyType) {
        this.bodyType = bodyType;
    }

    public String getBodyText() {
        if (bodyText.equals("") && !body.equals("")) {
            bodyText = JSONUtils.getString(body, "text", "");
        }
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public boolean equals(Object other) {

        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Mail))
            return false;

        final Mail otherMail = (Mail) other;
        return getId().equals(otherMail.getId());
    }
}
