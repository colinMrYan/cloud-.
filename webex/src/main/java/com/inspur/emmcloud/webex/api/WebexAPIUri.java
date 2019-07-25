package com.inspur.emmcloud.webex.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by chenmch on 2019/7/19.
 */

public class WebexAPIUri {

    /**
     * EMM服务
     *
     * @return
     */
    public static String getWebexBaseUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm();
    }

    /**
     * 获取webex会议列表
     *
     * @return
     */
    public static String getWebexMeetingListUrl() {
        return getWebexBaseUrl() + "api/mam/v6.0/webex";
    }

    /**
     * 预定会议
     *
     * @return
     */
    public static String getScheduleWebexMeetingUrl() {
        return getWebexBaseUrl() + "api/mam/v6.0/webex/v2";
    }

    /**
     * 获取webex头像地址
     *
     * @param email
     * @return
     */
    public static String getWebexPhotoUrl(String email) {
        return getWebexBaseUrl() + "img/userhead/" + email;
    }

    /**
     * 获取webex会议
     *
     * @return
     */
    public static String getWebexMeetingUrl(String meetingID) {
        return getWebexBaseUrl() + "api/mam/v6.0/webex/SessionInfo/" + meetingID;
    }

    /**
     * 删除webex会议
     *
     * @return
     */
    public static String getRemoveWebexMeetingUrl(String meetingID) {
        return getWebexBaseUrl() + "api/mam/v6.0/webex/remove/" + meetingID;
    }

    /**
     * 获取webex会议TK
     *
     * @return
     */
    public static String getWebexTK() {
        return getWebexBaseUrl() + "api/mam/v6.0/webex/gettk";
    }
}
