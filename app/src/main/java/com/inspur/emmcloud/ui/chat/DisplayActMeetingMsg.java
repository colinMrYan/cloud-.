package com.inspur.emmcloud.ui.chat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.ui.work.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONObjectUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.MeetingCardShowTime;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

/**
 * DisplayActMeetingMsg
 * 
 * @author sunqx 展示会议邀请卡片 2016-08-19
 */
public class DisplayActMeetingMsg {
	/**
	 * 会议邀请卡片
	 * 
	 * @param context
	 * @param apiService
	 * @param convertView
	 * @param msg
	 */
	public static void displayMeetingInviteMsg(final Context context,
			final ChatAPIService apiService, View convertView, final Msg msg
			) {
		final LinearLayout bottomLayout = (LinearLayout) convertView
				.findViewById(R.id.bottom_layout);
		final LinearLayout confirmLayout = (LinearLayout) convertView
				.findViewById(R.id.confirm_layout);
		final LinearLayout meetingCardLayout = (LinearLayout) convertView
				.findViewById(R.id.header_layout);
		TextView noticeText = (TextView) convertView
				.findViewById(R.id.meeting_notice_text);
		TextView timeText = (TextView) convertView
				.findViewById(R.id.meeting_time_text);
		TextView locText = (TextView) convertView
				.findViewById(R.id.meeting_loc_text);
		TextView topicText = (TextView) convertView
				.findViewById(R.id.meeting_topic_text);
		TextView acceptText= (TextView) convertView
				.findViewById(R.id.meeting_accepted_text);
		TextView refuseText = (TextView) convertView
				.findViewById(R.id.meeting_refused_text);
		final TextView confirmText = (TextView) convertView
				.findViewById(R.id.meeting_confirm_text);
		String uid = PreferencesUtils.getString(context, "userID");
		String msgUid = msg.getUid();
		String meetingId = "";
		String privateId = getPrivateId(msg);
		JSONObject jsonObject = JSONUtils.getJSONObject(msg.getBody(), "reservation", new JSONObject());
		final Meeting meeting = new Meeting(jsonObject);
		
//		final GetMeettingMsg getMeettingMsg = new GetMeettingMsg(msg.getBody());
		meetingId = meeting.getMeetingId();
		if (uid.equals(msgUid)) {
			meetingCardLayout
					.setBackgroundResource(R.drawable.shape_meeting_round_layout);
			bottomLayout.setVisibility(View.GONE);
			confirmLayout.setVisibility(View.GONE);
			noticeText.setText(meeting.getNotice());
			MeetingCardShowTime.showMeetingTime(context,meeting,timeText);
			
			topicText.setText(meeting.getTopic());
			locText.setText(meeting.getLocation());
		} else {
			final String organizer = meeting.getOrganizer();
			String state = "";
			state = getState(msg);
			if (state.equals("ACCEPT")) {
				confirmText.setText(context
						.getString(R.string.meeting_has_confirmed));
				bottomLayout.setVisibility(View.GONE);
				confirmLayout.setVisibility(View.VISIBLE);
//				showConfirmView(meettingCardBottom,meettingCardLayout);
			} else if (state.equals("REJECT")) {
				confirmText.setText(context
						.getString(R.string.meeting_has_refused));
				bottomLayout.setVisibility(View.GONE);
				confirmLayout.setVisibility(View.VISIBLE);
//				showConfirmView(meettingCardBottom,meettingCardLayout);
			}
			noticeText.setText(meeting.getNotice());
			timeText.setText(TimeUtils
					.getDisplayTime(context,Long.parseLong(meeting.getFrom())));
			topicText.setText(meeting.getTopic());
			locText.setText(meeting.getLocation());
			final Meeting meetingShow = meeting;
			meetingCardLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putSerializable("meeting", meeting);
					IntentUtils.startActivity((Activity)context, MeetingDetailActivity.class, bundle);
				}
			});
			final String userId = PreferencesUtils.getString(context, "userID");
			final String meetingIds = meetingId;
			final String privateIds = privateId;
			acceptText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					showOrganizer(context,userId,organizer);
					bottomLayout.setVisibility(View.GONE);
					confirmLayout.setVisibility(View.VISIBLE);
//					showConfirmView(meettingCardBottom,meettingCardLayout);
					confirmText.setText(context
							.getString(R.string.meeting_has_confirmed));
					sendMeetingReply(context,meetingIds,privateIds,apiService,"ACCEPT");
					changeMeetingCardState(context, privateIds, "ACCEPT", msg);
				}
			});
			refuseText.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showOrganizer(context, userId, organizer);
					confirmText.setText(context
							.getString(R.string.meeting_has_refused));
//					showConfirmView(meettingCardBottom,meettingCardLayout);
					bottomLayout.setVisibility(View.GONE);
					confirmLayout.setVisibility(View.VISIBLE);
					sendMeetingReply(context, meetingIds, privateIds, apiService, "REJECT");
					changeMeetingCardState(context, privateIds, "REJECT", msg);
				}
			});

			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == -1) {
						// 确定逻辑
						confirmText.setText(context
								.getString(R.string.meeting_has_refused));
						String state = getState(msg);
						if(state.equals("ACCEPT")){
							sendMeetingReply(context, meetingIds, privateIds, apiService, "REJECT");
							changeMeetingCardState(context, privateIds, "REJECT",
									msg);
						}else {
							sendMeetingReply(context, meetingIds, privateIds, apiService, "ACCEPT");
							changeMeetingCardState(context, privateIds, "ACCEPT",
									msg);
						}
						
					} else {
						// 取消
						sendMeetingReply(context, meetingIds, privateIds, apiService, "UNTREATED");
						bottomLayout.setVisibility(View.VISIBLE);
						confirmLayout.setVisibility(View.GONE);
						changeMeetingCardState(context, privateIds,
								"UNTREATED", msg);
					}
				}
			};
			confirmLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					LogUtils.debug("yfcLog", "会议状态"+msg.getPrivates());
					String state = getState(msg);
					if(state.equals("REJECT")){
						showChangeCardState(context,listener,context.getString(R.string.meeting_cancel_reject),context.getString(R.string.confirm_checkin));
					}else if(state.equals("ACCEPT")){
						showChangeCardState(context, listener,context.getString(R.string.meeting_cancel_confirm), context.getString(R.string.meeting_refused));
					}
					
				}
			});
		}

	}



	protected static void showChangeCardState(Context context,DialogInterface.OnClickListener listener,String stringCancel, String stringConfirm) {
		EasyDialog.showDialog(context, context
				.getString(R.string.prompt), context
				.getString(R.string.meeting_state_modification),
				stringCancel,
				stringConfirm,
				listener, true);
	}



	/**
	 * 显示确认view
	 * @param meetingCardBottom
	 * @param meetingConfirmLayout
	 */
	private static void showConfirmView(LinearLayout meetingCardBottom,LinearLayout meetingConfirmLayout) {
		LogUtils.debug("yfcLog", "确认按钮被点击,用这个方法的时候只能隐藏底部view不能显示确定view");
		meetingConfirmLayout.setVisibility(View.VISIBLE);
		meetingCardBottom.setVisibility(View.GONE);
	}

	/**
	 * 发送会议状态
	 * @param context
	 * @param meetingIds
	 * @param privateIds
	 * @param apiService
	 * @param state
	 */
	protected static void sendMeetingReply(Context context,String meetingIds,String privateIds,ChatAPIService apiService,String state) {
		if (NetUtils.isNetworkConnected(context)) {
			apiService.sendMeetingReply(meetingIds, privateIds,
					state);
		}
	}

	/**
	 * 会议组织者提示
	 * @param context
	 * @param userId
	 * @param organizer
	 */
	protected static void showOrganizer(Context context,String userId,String organizer) {
		if (userId.equals(organizer)) {
			ToastUtils.show(context, context.getString(R.string.meeting_organizer));
			return;
		}
	}

	/**
	 * 获取会议状态
	 * @param msg
	 * @return
	 */
	private static String getState(Msg msg) {
		JSONObjectUtils jsonObjectUtils = new JSONObjectUtils(msg.getPrivates());
		String state = jsonObjectUtils.getString("state");
		return state;
	}

	/**
	 * 获取任务privateId
	 * @param msg
	 * @return
	 */
	private static String getPrivateId(Msg msg) {
		JSONObjectUtils jsonObjectUtils = new JSONObjectUtils(msg.getPrivates());
		String privateId = jsonObjectUtils.getString("state");
		return privateId;
	}

	/**
	 * 修改消息状态的方法
	 * 
	 * @param context
	 * @param privateid
	 * @param state
	 * @param msg
	 */
	private static void changeMeetingCardState(Context context,
			String privateid, String state, Msg msg) {
		JSONObject privatesJsonObject = new JSONObject();
		try {
			privatesJsonObject.put("_id", privateid);
			privatesJsonObject.put("state", state);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msg.setPrivates(privatesJsonObject.toString());
		MsgCacheUtil.saveMsg(context, msg);
	}
}
