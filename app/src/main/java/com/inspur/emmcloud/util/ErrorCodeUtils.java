package com.inspur.emmcloud.util;

import android.content.Context;

/**
 * 用这个类把ErrorCode码转成string.xml文件里的对应提示信息
 */
public class ErrorCodeUtils {

	/**
	 * 把错误代码转化为提示信息的方法
	 * @param context
	 * @param errorCode
	 * @return
	 */
	public static String getAlertByCode(Context context,int errorCode){
		String errorKey = "";
		switch (errorCode) {
		case 41001:
			errorKey = "user_profile_missing";
			break;
		case 41002:
			errorKey = "user_profile_broken";
			break;
		case 72001:
			errorKey = "time_earlier_than_permission";
			break;
		case 72002:
			errorKey = "time_later_than_permission";
			break;
		case 72003:
			errorKey = "time_span_over_limit";
			break;
		case 72004:
			errorKey = "duration_over_limit";
			break;
		case 72005:
			errorKey = "time_conflict";
			break;
		case 72006:
			errorKey = "invalid_booking_time";
			break;
		case 72101:
			errorKey = "room_not_exists";
			break;
		case 72102:
			errorKey = "room_locked_by_admin";
			break;
		case 72103:
			errorKey = "room_for_special_use";
			break;
		case 72201:
			errorKey = "book_history_not_exist";
			break;
		case 72202:
			errorKey = "expired_booking_history_immutable";
			break;
		case 72301:
			errorKey = "department_booking_times_over_run";
			break;
		default:
			errorKey = "net_request_failed";
			break;
		}
		//stringID：string.xml内配置的ID  
		//errorKey: string.xml内配置的名字 
		int stringID = context.getResources().getIdentifier(errorKey,"string", "com.inspur.emmcloud");  
		//string.xml内配置的具体内容  
		//errorMessage:根据errorCode取到的文字
		String errorMessage = context.getResources().getString(stringID);  
		return errorMessage;
	}
}
