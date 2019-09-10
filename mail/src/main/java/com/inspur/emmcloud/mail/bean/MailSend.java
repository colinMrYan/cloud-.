package com.inspur.emmcloud.mail.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/1/7.
 */

public class MailSend implements Serializable {
    private String Subject;
    private String Body;
    private List<MailAttachment> MailAttachments = new ArrayList<>();
    private MailRecipientModel From;
    private List<MailRecipientModel> ToRecipients = new ArrayList<>();
    private List<MailRecipientModel> CcRecipients = new ArrayList<>();
    private List<MailRecipientModel> BccRecipients = new ArrayList<>();
    private String OriginalMail;
    private boolean IsReply;
    private boolean IsForward;
    private boolean NeedEncrypt;
    private boolean NeedSign;


    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        Body = body;
    }

    public List<MailAttachment> getMailAttachments() {
        return MailAttachments;
    }

    public void setMailAttachments(List<MailAttachment> mailAttachments) {
        MailAttachments = mailAttachments;
    }

    public MailRecipientModel getFrom() {
        return From;
    }

    public void setFrom(MailRecipientModel from) {
        From = from;
    }

    public List<MailRecipientModel> getToRecipients() {
        return ToRecipients;
    }

    public void setToRecipients(List<MailRecipientModel> toRecipients) {
        ToRecipients = toRecipients;
    }

    public List<MailRecipientModel> getCcRecipients() {
        return CcRecipients;
    }

    public void setCcRecipients(List<MailRecipientModel> ccRecipients) {
        CcRecipients = ccRecipients;
    }

    public List<MailRecipientModel> getBccRecipients() {
        return BccRecipients;
    }

    public void setBccRecipients(List<MailRecipientModel> bccRecipients) {
        BccRecipients = bccRecipients;
    }

    public String getOriginalMail() {
        return OriginalMail;
    }

    public void setOriginalMail(String originalMail) {
        OriginalMail = originalMail;
    }

    public boolean getIsReply() {
        return IsReply;
    }

    public void setIsReply(Boolean isReply) {
        IsReply = isReply;
    }

    public Boolean getIsForward() {
        return IsForward;
    }

    public void setIsForward(Boolean isForward) {
        IsForward = isForward;
    }

    public boolean isNeedEncrypt() {
        return NeedEncrypt;
    }

    public void setNeedEncrypt(boolean needEncrypt) {
        NeedEncrypt = needEncrypt;
    }

    public boolean isNeedSign() {
        return NeedSign;
    }

    public void setNeedSign(boolean needSign) {
        NeedSign = needSign;
    }
}


