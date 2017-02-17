package com.inspur.emmcloud.util;

import android.content.Context;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Meeting;

public class MeetingCardShowTime {

	/**
	 * 显示时间
	 * @param context
	 * @param getMeetingMsg
	 * @param timeTextView
	 */
	public static void showMeetingTime(Context context,Meeting getMeetingMsg,TextView timeTextView) {
		// TODO Auto-generated method stub
		String from = getMeetingMsg.getFrom();
		// 计算距今几天
		int between = 0;
		try {
			between = TimeUtils.daysBetweenToday(from);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if(between == 0){
			timeTextView.setText(context.getString(R.string.today));
		}else if(between == 1){
			timeTextView.setText(context.getString(R.string.tomorrow));
		}else if(between == 2){
			timeTextView.setText(context.getString(R.string.after));
		}else{
			timeTextView.setText(TimeUtils.calendar2FormatString(context, TimeUtils.timeString2Calendar(from), TimeUtils.FORMAT_MONTH_DAY));
		}
	}
}
