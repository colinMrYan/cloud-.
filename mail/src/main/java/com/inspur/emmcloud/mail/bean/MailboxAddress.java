package com.inspur.emmcloud.mail.bean;

import com.inspur.emmcloud.baselib.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by libaochao on 2019/1/7.
 */

public class MailboxAddress {
    private String name;
    private String address;
    private boolean emailFormat;   //邮箱格式

    public MailboxAddress(String Name, String Address) {
        this.name = Name;
        this.address = Address;
        this.emailFormat = false;
        emailFormat = emailCheckoutFormat(Address);
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
        emailFormat = emailCheckoutFormat(address);
        this.address = address;
    }

    public boolean isEmailFormat() {
        return emailFormat;
    }

    public void setEmailFormat(boolean emailFormat) {
        this.emailFormat = emailFormat;
    }


    //校验邮箱格式
    private boolean emailCheckoutFormat(String s) {
        // 判断邮箱
        if (!StringUtils.isBlank(s)) {
            return Pattern.compile("\\w+@{1}\\w+\\.{1}\\w+").matcher(s).matches();
        }
        return false;
    }
}
