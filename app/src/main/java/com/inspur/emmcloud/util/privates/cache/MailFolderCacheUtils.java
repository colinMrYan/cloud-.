package com.inspur.emmcloud.util.privates.cache;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.appcenter.mail.MailFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/12/25.
 */

public class MailFolderCacheUtils {
    public static void saveMailFolderList(List<MailFolder> mailFolderList) {
        if (mailFolderList == null || mailFolderList.size() == 0) {
            return;
        }
        try {
            DbCacheUtils.getDb().delete(MailFolder.class);
            DbCacheUtils.getDb().saveOrUpdate(mailFolderList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static List<MailFolder> getMailFolderList() {
        List<MailFolder> mailFolderList = null;
        try {
            mailFolderList = DbCacheUtils.getDb().findAll(MailFolder.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (mailFolderList == null) {
            mailFolderList = new ArrayList<>();
        }
        return mailFolderList;
    }

    public static List<MailFolder> getChildMailFolderList(String parentFolderId) {
        List<MailFolder> mailFolderList = null;
        try {
            mailFolderList = DbCacheUtils.getDb().selector(MailFolder.class)
                    .where("parentFolderId", "=", parentFolderId).orderBy("sort").findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (mailFolderList == null) {
            mailFolderList = new ArrayList<>();
        }
        return mailFolderList;
    }

    public static MailFolder getMailFolderById(String forderId) {
        try {
            MailFolder mailFolder = DbCacheUtils.getDb().findById(MailFolder.class, forderId);
            return mailFolder;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
