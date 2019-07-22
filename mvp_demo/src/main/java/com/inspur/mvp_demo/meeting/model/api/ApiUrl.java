package com.inspur.mvp_demo.meeting.model.api;

public class ApiUrl {
    public static String getMeetingHistoryUrl(int pageNum) {
        return "https://ecm.inspuronline.com/inspur_esg/schedule-ext/api/schedule/v6.0/meeting/GetHistory/" + 1;
    }
}
