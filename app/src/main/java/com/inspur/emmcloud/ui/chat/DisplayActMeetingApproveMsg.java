package com.inspur.emmcloud.ui.chat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.JSONObjectUtils;
import com.inspur.emmcloud.util.MeetingCardShowTime;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

/**
 * DisplayActMeetingApproveMsg
 * 
 * @author sunqx 展示申请会议室审批卡片 2016-08-19
 */
public class DisplayActMeetingApproveMsg {
	/**
	 * 申请会议室审批卡片
	 * 
	 * @param context
	 * @param apiService
	 * @param convertView
	 * @param msg
	 */
	public static void displayMeetingApproveMsg(final Context context,
			final ChatAPIService apiService, View convertView, final Msg msg
			) {
		final LinearLayout meetingCardBottom = (LinearLayout) convertView
				.findViewById(R.id.bottom_layout);
		final LinearLayout meetingConfirmLayout = (LinearLayout) convertView
				.findViewById(R.id.confirm_layout);
		LinearLayout meetingCardLayout = (LinearLayout) convertView
				.findViewById(R.id.header_layout);
		TextView textView = (TextView) convertView
				.findViewById(R.id.meeting_notice_text);
		TextView timeTextView = (TextView) convertView
				.findViewById(R.id.meeting_time_text);
		TextView locTextView = (TextView) convertView
				.findViewById(R.id.meeting_loc_text);
		TextView topicTextView = (TextView) convertView
				.findViewById(R.id.meeting_topic_text);
		TextView acceptTextView = (TextView) convertView
				.findViewById(R.id.meeting_accepted_text);
		TextView refuseTextView = (TextView) convertView
				.findViewById(R.id.meeting_refused_text);
		final TextView confirmTextView = (TextView) convertView
				.findViewById(R.id.meeting_confirm_text);
		String uid = PreferencesUtils.getString(context, "userID");
		String msgUid = msg.getUid();
		JSONObjectUtils jsonObjectAnalysisUtils = new JSONObjectUtils(msg.getPrivates());
		String privateId = jsonObjectAnalysisUtils.getString("_id");
		
		JSONObject jsonObject;
		Meeting meeting = null;
		try {
			jsonObject = new JSONObject(msg.getBody());
			meeting = new Meeting(jsonObject.getJSONObject("reservation"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		GetMeettingMsg getMeettingMsg = new GetMeettingMsg(msg.getBody());
		String meetingId = meeting.getMeetingId();
		final String privateIds = privateId;
		final String meetingIds = meetingId;
		if (uid.equals(msgUid)) {
			meetingCardLayout
					.setBackgroundResource(R.drawable.shape_meeting_round_layout);
			meetingCardBottom.setVisibility(View.GONE);
			meetingConfirmLayout.setVisibility(View.GONE);
			textView.setText(meeting.getNotice());
			timeTextView.setText(TimeUtils
					.getDisplayTime(context,Long.parseLong(meeting.getFrom())));
			topicTextView.setText(meeting.getTopic());
			locTextView.setText(meeting.getLocation());
		} else {
			final String organizer = meeting.getOrganizer();
			String state = jsonObjectAnalysisUtils.getString("state");
			if (state.equals("ACCEPT")) {
				confirmTextView.setText(context
						.getString(R.string.meeting_has_confirmed));
				meetingCardBottom.setVisibility(View.GONE);
				meetingConfirmLayout.setVisibility(View.VISIBLE);
			} else if (state.equals("REJECT")) {
				confirmTextView.setText(context
						.getString(R.string.meeting_has_refused));
				meetingCardBottom.setVisibility(View.GONE);
				meetingConfirmLayout.setVisibility(View.VISIBLE);
			}
			textView.setText(meeting.getNotice());
			MeetingCardShowTime.showMeetingTime(context,meeting,timeTextView);
//			timeTextView.setText(TimeUtils
//					.getMeettingDisplayTime(getMeettingMsg.getFrom()));
			topicTextView.setText(meeting.getTopic());
			locTextView.setText(meeting.getLocation());
			final String userid = PreferencesUtils.getString(context, "userID");
			acceptTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (userid.equals(organizer)) {
						ToastUtils.show(context,context.getString(R.string.meeting_organizer));
						return;
					}
					meetingCardBottom.setVisibility(View.GONE);
					meetingConfirmLayout.setVisibility(View.VISIBLE);
					confirmTextView.setText(context
							.getString(R.string.meeting_has_confirmed));
					sendMeetingReply(context, meetingIds, privateIds, apiService, "ACCEPT");
					changeMeetingCardState(context, privateIds, "ACCEPT", msg);
				}
			});
			refuseTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (userid.equals(organizer)) {
						ToastUtils.show(context, context.getString(R.string.meeting_organizer));
						return;
					}
					confirmTextView.setText(context
							.getString(R.string.meeting_has_refused));
					meetingCardBottom.setVisibility(View.GONE);
					meetingConfirmLayout.setVisibility(View.VISIBLE);
					sendMeetingReply(context, meetingIds, privateIds, apiService, "REJECT");
					changeMeetingCardState(context, privateIds, "REJECT", msg);
				}
			});

			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == -1) {
						// 确定逻辑
						confirmTextView.setText(context
								.getString(R.string.meeting_has_refused));
						sendMeetingReply(context, meetingIds, privateIds, apiService, "REJECT");
						changeMeetingCardState(context, privateIds, "REJECT",
								msg);
					} else {
						// 取消
						sendMeetingReply(context, meetingIds, privateIds, apiService, "UNTREATED");
						meetingCardBottom.setVisibility(View.VISIBLE);
						meetingConfirmLayout.setVisibility(View.GONE);
						changeMeetingCardState(context, privateIds,
								"UNTREATED", msg);
					}
				}
			};
			meetingConfirmLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EasyDialog.showDialog(context, context
							.getString(R.string.prompt), context
							.getString(R.string.meeting_state_modification),
							context.getString(R.string.meeting_refused),
							context.getString(R.string.meeting_cancel_confirm),
							listener, true);
				}
			});
		}
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
