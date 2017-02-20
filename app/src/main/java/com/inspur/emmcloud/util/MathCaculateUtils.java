package com.inspur.emmcloud.util;

import java.util.Calendar;

public class MathCaculateUtils {

	/**
	 * 上取整时间
	 * @param calendar
	 * @param calendarOther
	 * @return
	 */
	public static int getCeil(Calendar calendar,Calendar calendarOther){
		return (int)Math.ceil((calendar.getTimeInMillis() - calendarOther.getTimeInMillis())/(60*60)/1000.0);
	}
}
