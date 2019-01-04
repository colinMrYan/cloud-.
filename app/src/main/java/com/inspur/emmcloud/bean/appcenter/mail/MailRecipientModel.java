package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.util.common.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by libaochao on 2018/12/25.
 */

public class MailRecipientModel {

    private  String mRecipientName;  //收件人名称
    private  String mRecipientEmail; //收件人邮箱
    private  boolean mEmailFormat;   //邮箱格式

    public MailRecipientModel(){
        mEmailFormat=false;
        mRecipientName="";
        mRecipientEmail="";
    }

    public  MailRecipientModel(String mRecipientName,String mRecipientEmail ){
        this.mRecipientEmail=mRecipientEmail;
        this.mRecipientName=mRecipientName;
        mEmailFormat = EmailCheckoutFormat(mRecipientEmail);
    }

    public boolean getmEmailFormat(){
        return  mEmailFormat;
    }

    public String getmRecipientName() {
        return mRecipientName;
    }

    public void setmRecipientName(String mRecipientName) {
        this.mRecipientName = mRecipientName;
    }

    public String getmRecipientEmail() {
        return mRecipientEmail;
    }

    public void setmRecipientEmail(String mRecipientEmail) {
        mEmailFormat=EmailCheckoutFormat(mRecipientEmail);
        this.mRecipientEmail = mRecipientEmail;
    }

    //校验邮箱格式
    private boolean EmailCheckoutFormat(String s){
        // 判断邮箱
        if(!StringUtils.isBlank(s)){
            return Pattern.compile("\\w+@{1}\\w+\\.{1}\\w+").matcher(s).matches();
        }
        return false;
    }

}
