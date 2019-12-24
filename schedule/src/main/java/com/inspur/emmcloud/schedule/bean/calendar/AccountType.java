package com.inspur.emmcloud.schedule.bean.calendar;

/**
 * Created by chenmch on 2019/7/25.
 */

public enum AccountType {
    APP_SCHEDULE, APP_MEETING, EXCHANGE;

    public static AccountType getAccountType(String value) {
        try {
            return AccountType.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
            return AccountType.APP_SCHEDULE;
        }
    }
}