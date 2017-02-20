package com.inspur.emmcloud.ui.chat;

import java.text.SimpleDateFormat;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.ui.work.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.TimeUtils;

/**
 * DisplayActMeetingCancelMsg
 * 
 * @author sunqx 展示会议取消卡片 2016-08-18
 */
public class DisplayActMeetingCancelMsg {

	/**
	 * 会议取消卡片
	 * 
	 * @param context
	 * @param apiService
	 * @param convertView
	 * @param msg
	 */
	public static void displayCancelMeetingMsg(final Context context,
			 View convertView, final Msg msg) {
		JSONObject meetingObj = JSONUtils.getJSONObject(msg.getBody(),
				"reservation", new JSONObject());
		final Meeting meeting = new Meeting(meetingObj);
		SimpleDateFormat format = new SimpleDateFormat(
				"MM-dd HH:mm");
		String meetingTime = TimeUtils.getTime(Long.parseLong(meeting.getFrom()), format);
		((TextView)convertView.findViewById(R.id.time_text)).setText(meetingTime);
		((TextView)convertView.findViewById(R.id.location_text)).setText(meeting.getLocation());
		((TextView)convertView.findViewById(R.id.topic_text)).setText(meeting.getTopic());
		((TextView)convertView.findViewById(R.id.notice_text)).setText(meeting.getNotice());
		((ImageView)convertView.findViewById(R.id.flag_img)).setImageResource(R.drawable.icon_card_meeting_cancel);
		((LinearLayout)convertView.findViewById(R.id.card_layout)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Bundle bundle = new Bundle();
				bundle.putSerializable("meeting", meeting);
				IntentUtils.startActivity((Activity)context, MeetingDetailActivity.class, bundle);
			}
		});
		
//		LinearLayout bottomLayout = (LinearLayout) convertView
//				.findViewById(R.id.bottom_layout);
//		LinearLayout confirmLayout = (LinearLayout) convertView
//				.findViewById(R.id.confirm_layout);
//		LinearLayout meetingCardLayout = (LinearLayout) convertView
//				.findViewById(R.id.header_layout);
//		TextView noticeText = (TextView) convertView
//				.findViewById(R.id.meeting_notice_text);
//		TextView timeText = (TextView) convertView
//				.findViewById(R.id.meeting_time_text);
//		
//		meetingCardLayout
//				.setBackgroundResource(R.drawable.shape_meeting_round_layout);
//		bottomLayout.setVisibility(View.GONE);
//		confirmLayout.setVisibility(View.GONE);
//		noticeText.setText(meeting.getNotice());
//		noticeText.setVisibility(View.INVISIBLE);
//		MeetingCardShowTime.showMeetingTime(context, meeting, timeText);
//		((TextView) convertView
//				.findViewById(R.id.meeting_topic_text))
//				.setText(ContactCacheUtils.getUserName(context,
//						meeting.getOrganizer())
//						+ context.getString(R.string.meeting_canceled_topic)
//						+ meeting.getTopic()
//						+ context.getString(R.string.meeting_tail));
//		((TextView) convertView.findViewById(R.id.meeting_loc_text))
//				.setText(meeting.getLocation());
//		meetingCardLayout.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Bundle bundle = new Bundle();
//				bundle.putSerializable("meeting", meeting);
//				IntentUtils.startActivity((Activity)context, MeetingDetailActivity.class, bundle);
//			}
//		});
	}
}
