package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenmch on 2018/12/26.
 */

public class MailRecipient implements Serializable {
    private String name;
    private String address;
    private String routingType;
    private String mailboxType;
    private String id;

    public MailRecipient() {

    }

    public MailRecipient(JSONObject object) {
        name = JSONUtils.getString(object, "name", "");
        address = JSONUtils.getString(object, "address", "");
        routingType = JSONUtils.getString(object, "routingType", "");
        mailboxType = JSONUtils.getString(object, "mailboxType", "");
        id = JSONUtils.getString(object, "id", "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRoutingType() {
        return routingType;
    }

    public void setRoutingType(String routingType) {
        this.routingType = routingType;
    }

    public String getMailboxType() {
        return mailboxType;
    }

    public void setMailboxType(String mailboxType) {
        this.mailboxType = mailboxType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
