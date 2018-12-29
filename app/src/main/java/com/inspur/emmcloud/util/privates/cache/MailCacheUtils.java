package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.bean.appcenter.mail.Mail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/12/27.
 */

public class MailCacheUtils {
    public static void saveMail(Mail mail){
        try {
            DbCacheUtils.getDb().saveOrUpdate(mail);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void saveMailList(List<Mail> mailList){
        if (mailList == null || mailList.size()==0){
            return;
        }
        try {
            DbCacheUtils.getDb().saveOrUpdate(mailList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static Mail getMail(String mailId){
        try {
           return DbCacheUtils.getDb().findById(Mail.class,mailId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static List<Mail> getMailListByMailIdList( List<String> mailIdList){
        List<Mail> mailList = null;
        try {
            mailList =  DbCacheUtils.getDb().selector(Mail.class). where("id", "in", mailIdList).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mailList == null){
            mailList = new ArrayList<>();
        }
        return mailList;
    }

    public static List<Mail> getMailListInFolder( String folderId,int limit){
        List<Mail> mailList = null;
        try {
            mailList =  DbCacheUtils.getDb().selector(Mail.class). where("folderId", "=", folderId).orderBy("creationTimestamp", true).limit(limit).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mailList == null){
            mailList = new ArrayList<>();
        }
        return mailList;
    }
}
