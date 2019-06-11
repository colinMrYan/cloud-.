package com.inspur.emmcloud.bean.appcenter.mail;

/**
 * Created by chenmch on 2019/1/1.
 */

public class GetMailDetailResult {
    private Mail mail;

    public GetMailDetailResult(byte[] response) {

    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }
}
