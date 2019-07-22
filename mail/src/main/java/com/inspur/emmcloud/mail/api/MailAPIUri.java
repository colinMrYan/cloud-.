package com.inspur.emmcloud.mail.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by libaochao on 2019/7/22.
 */

public class MailAPIUri {
    public static String getMailBaseUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/ews/v1.0";
    }

    public static String getMailFolderUrl() {
        return getMailBaseUrl() + "/Folder";
    }


    public static String getMailListUrl() {
        return getMailBaseUrl() + "/Mail/List";
    }

    public static String getMailDetailUrl(boolean isEncrypted) {
        return getMailBaseUrl() + (isEncrypted ? "/Mail/EncryptedDetail" : "/Mail/Detail");
    }

    public static String getLoginMailUrl() {
        return getMailBaseUrl() + "/UserProfile/MailBind";
    }

    public static String getMailAttachmentUrl() {
        return getMailBaseUrl() + "/Mail/SafeAttachment?";
    }

    /**
     * 获取上传Certificate的接口
     *
     * @return
     */
    public static String getCertificateUrl() {
        return getMailBaseUrl() + "/UserProfile/CheckData";
    }

    /**
     * 获取上传邮件Url
     *
     * @return
     */
    public static String getUploadMailUrl() {
        return getMailBaseUrl() + "/Mail/SafeSend";
    }

    /**
     * 获取删除邮件Url
     *
     * @return
     */
    public static String getRemoveMailUrl() {
        return getMailBaseUrl() + "/Mail/Remove";
    }
}
