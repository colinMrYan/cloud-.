package com.inspur.emmcloud.mail.bean;

import com.inspur.emmcloud.baselib.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by libaochao on 2018/12/25.
 */

public class MailRecipientModel {

    private String address; //收件人邮箱
    private boolean format;   //邮箱格式
    private String name;  //收件人名称

    public MailRecipientModel() {
        format = false;
        name = "";
        address = "";
    }

    public MailRecipientModel(String mRecipientName, String mRecipientEmail) {
        this.address = mRecipientEmail;
        this.name = mRecipientName;
        format = EmailCheckoutFormat(mRecipientEmail);
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
        format = EmailCheckoutFormat(address);
        this.address = address;
    }

    public boolean isFormat() {
        return format;
    }

    public void setFormat(boolean format) {
        this.format = format;
    }


    //校验邮箱格式
    private boolean EmailCheckoutFormat(String s) {
        // 判断邮箱
        if (!StringUtils.isBlank(s)) {
            return Pattern.compile("\\w+@{1}\\w+\\.{1}\\w+").matcher(s).matches();
        }
        return false;
    }

}
