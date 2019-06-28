package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

/**
 * 用这个类把ErrorCode码转成string.xml文件里的对应提示信息
 */
public class ErrorCodeUtils {

    /**
     * 把错误代码转化为提示信息的方法
     * 处理Ecm错误码，如果Ecm增加了一种错误，
     * 需在此处添加一种错误码，
     * 并在国际化string.xml中添加相应文字资源
     *
     * @param context
     * @param errorCode
     * @return
     */
    public static String getAlertByCode(Context context, int errorCode) {
        String errorKey = "";
        switch (errorCode) {
            case 41001:
                errorKey = "meeting_user_profile_missing";
                break;
            case 41002:
                errorKey = "meeting_user_profile_broken";
                break;
            case 72001:
                errorKey = "meeting_time_earlier_than_permission";
                break;
            case 72002:
                errorKey = "meeting_time_later_than_permission";
                break;
            case 72003:
                errorKey = "meeting_time_span_over_limit";
                break;
            case 72004:
                errorKey = "meeting_duration_over_limit";
                break;
            case 72005:
                errorKey = "meeting_time_conflict";
                break;
            case 72006:
                errorKey = "meeting_invalid_booking_time";
                break;
            case 72101:
                errorKey = "meeting_room_not_exists";
                break;
            case 72102:
                errorKey = "meeting_room_locked_by_admin";
                break;
            case 72103:
                errorKey = "meeting_room_for_special_use";
                break;
            case 72201:
                errorKey = "meeting_book_history_not_exist";
                break;
            case 72202:
                errorKey = "meeting_expired_booking_history_immutable";
                break;
            case 72301:
                errorKey = "meeting_department_booking_times_over_run";
                break;
            default:
                errorKey = "net_request_failed";
                break;
        }
        //stringID：string.xml内配置的ID
        //errorKey: string.xml内配置的名字
        int stringID = context.getResources().getIdentifier(errorKey, "string",context.getPackageName());
        //string.xml内配置的具体内容
        //errorMessage:根据errorCode取到的文字
        String errorMessage = context.getResources().getString(stringID);
        return errorMessage;
    }
}
