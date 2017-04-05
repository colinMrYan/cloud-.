package com.inspur.emmcloud.ui.work.meeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MyViewPagerAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.GetMeetingListResult;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.MeetingSchedule;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MeetingsRoomDetailActivity extends BaseActivity {

	private static final int MEETING_DETAIL = 0;
	private final String dayStartTime = "08:00";
	private final String dayEndTime = "18:00";
	private WorkAPIService apiService;
	private String shortname, bid, roomName;
	private LoadingDialog loadingDlg;
	private TextView meetingFlourText, meetingLocationText, meetingMemberText;
	// private String[] equipments;
	private ArrayList<String> equipmentList = new ArrayList<String>();
	private ViewPager viewPager;
	private LayoutInflater inflater;
	private int viewPagerIndex = 0;
	private int displayDayCount = 2;
	private List<List<MeetingSchedule>> allDaysMeetingScheduleList;
	private RelativeLayout beforeDayLayout, afterDayLayout;
	private TextView thisDayText;
	private List<View> viewList = new ArrayList<View>();
	private Meeting deleteMeeting;//
	private List<Meeting> allMeetingList = new ArrayList<Meeting>();
	private String uid;
	private Calendar currentCalendar = Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_meeting_room_detail);
		initView();
		getMeetingList(true);
	}

	private void initView() {
		// TODO Auto-generated method stub
		apiService = new WorkAPIService(getApplicationContext());
		apiService.setAPIInterface(new WebService());
		loadingDlg = new LoadingDialog(this);
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		roomName = getIntent().getStringExtra("roomName");
		int meetingMember = getIntent().getIntExtra("meetingMember", 0);
		shortname = getIntent().getStringExtra("shortName");
		bid = getIntent().getStringExtra("bid");
		// equipments = getIntent().getStringArrayExtra("equips");
		meetingFlourText = (TextView) findViewById(R.id.meeting_room_detail_flour_text);
		meetingLocationText = (TextView) findViewById(R.id.meeting_room_detail_location_text);
		meetingMemberText = (TextView) findViewById(R.id.meeting_room_detail_topic_text);
		meetingFlourText.setText(roomName);
		meetingLocationText.setText(shortname);
		meetingMemberText.setText(meetingMember + "");
		initAndDisplayEquips();
		beforeDayLayout = (RelativeLayout) findViewById(R.id.meeting_room_detail_before_day_layout);
		thisDayText = (TextView) findViewById(R.id.meeting_room_detail_this_day_text);
		afterDayLayout = (RelativeLayout) findViewById(R.id.meeting_room_detail_after_day_layout);
		String maxAhead = getIntent().getStringExtra("maxAhead");
		if (!StringUtils.isBlank(maxAhead)) {
			displayDayCount = Integer.parseInt(maxAhead);
		}
	}

	/**
	 * 从服务器获取时间
	 * @param date
     */
	private void setCurrentCalendar(String date) {
		// TODO Auto-generated method stub
		try {
			String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
			SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
			Date currentDate = format.parse(date);
			currentCalendar.setTime(currentDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * viewPager select
	 */
	private void setSelect() {
		if (viewPagerIndex == 0) {
			beforeDayLayout.setVisibility(View.GONE);
		} else {
			beforeDayLayout.setVisibility(View.VISIBLE);
		}
		if (viewPagerIndex == displayDayCount - 1) {
			afterDayLayout.setVisibility(View.GONE);
		} else {
			afterDayLayout.setVisibility(View.VISIBLE);
		}
		thisDayText.setText(TimeUtils.getFormatStringFromTargetTime(
				getApplicationContext(), currentCalendar, viewPagerIndex));
	}

	/**
	 * 初始化listview的显示信息
	 */
	private void initListView() {
		// TODO Auto-generated method stub
		viewList.clear();
		for (int i = 0; i < displayDayCount; i++) {
			View allDayMeetingView = inflater.inflate(
					R.layout.all_day_meeting_fragment, null);
			viewList.add(allDayMeetingView);
			ListView meetingListView = (ListView) allDayMeetingView
					.findViewById(R.id.meeting_list);
			List<MeetingSchedule> meetingScheduleList = allDaysMeetingScheduleList
					.get(i);
			meetingListView.setAdapter(new MeetingAdapter(meetingScheduleList));

		}
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(new MyViewPagerAdapter(getApplicationContext(),
				viewList));
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				viewPagerIndex = arg0;
				setSelect();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
		viewPager.setCurrentItem(viewPagerIndex);
		setSelect();
	}

	/**
	 * 初始化和显示会议室设备信息
	 */
	private void initAndDisplayEquips() {
		// TODO Auto-generated method stub
		equipmentList = getIntent().getStringArrayListExtra("equips");
		if (equipmentList != null) {
			// equipments = equips.split(":");
			for (int i = 0; i < equipmentList.size(); i++) {
				if (!StringUtils.isEmpty(equipmentList.get(i))
						&& equipmentList.get(i).equals("WIFI")) {
					((ImageView) findViewById(R.id.meeting_room_detail_wifi_icon))
							.setVisibility(View.VISIBLE);
				} else if (!StringUtils.isEmpty(equipmentList.get(i))
						&& equipmentList.get(i).equals("PROJECTOR")) {
					((ImageView) findViewById(R.id.meeting_room_detail_projector_icon))
							.setVisibility(View.VISIBLE);
				} else if (!StringUtils.isEmpty(equipmentList.get(i))
						&& equipmentList.get(i).equals("CONFERENCE_PHONE")) {
					((ImageView) findViewById(R.id.meeting_room_detail_phone_icon))
							.setVisibility(View.VISIBLE);
				} else if (!StringUtils.isEmpty(equipmentList.get(i))
						&& equipmentList.get(i).equals("WHITE_BOARD")) {
				}
			}
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;

		case R.id.meeting_room_detail_before_day_layout:
			viewPagerIndex--;
			viewPager.setCurrentItem(viewPagerIndex);
			setSelect();
			break;

		case R.id.meeting_room_detail_after_day_layout:
			viewPagerIndex++;
			viewPager.setCurrentItem(viewPagerIndex);
			setSelect();
			break;
		default:
			break;
		}

	}

	public class MeetingAdapter extends BaseAdapter {
		private List<MeetingSchedule> meetingScheduleList = new ArrayList<MeetingSchedule>();

		public MeetingAdapter(List<MeetingSchedule> meetingScheduleList) {
			this.meetingScheduleList = meetingScheduleList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return meetingScheduleList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			final MeetingSchedule meetingSchedule = meetingScheduleList
					.get(position);
			SimpleDateFormat format = TimeUtils.getFormat(
					getApplicationContext(), TimeUtils.FORMAT_HOUR_MINUTE);
			long beginTimeLong = meetingSchedule.getFrom();
			long endTimeLong = meetingSchedule.getTo();
			String beginTimeString = TimeUtils.getTime(beginTimeLong, format);
			String endTimeString = TimeUtils.getTime(endTimeLong, format);
			String timeSegment = beginTimeString + "-" + endTimeString;
			if (meetingSchedule.getMeeting() == null) {
				convertView = vi.inflate(
						R.layout.meeting_no_schedule_item_view, null);
				((TextView) convertView.findViewById(R.id.time_text))
						.setText(timeSegment + " " + getString(R.string.free));
				((RelativeLayout) convertView.findViewById(R.id.meeting_layout))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Intent intent = new Intent();
								intent.putExtra("flour", shortname);
								intent.putExtra("room", roomName);
								intent.putExtra("roomid", bid);
								intent.putExtra("beginTime",
										meetingSchedule.getFrom());
								intent.putExtra("endTime",
										meetingSchedule.getTo());
								intent.putExtra("maxAhead", getIntent()
										.getStringExtra("maxAhead"));
								intent.putExtra("maxDuration", getIntent()
										.getStringExtra("maxDuration"));
								setResult(RESULT_OK, intent);
								finish();
							}
						});
			} else {
				final Meeting meeting = meetingSchedule.getMeeting();
				convertView = vi.inflate(R.layout.meeting_schedule_item_view,
						null);
				((TextView) convertView.findViewById(R.id.meeting_time_text))
						.setText(timeSegment);
				String organizer = ContactCacheUtils.getUserName(
						getApplicationContext(), meeting.getOrganizer());
				((TextView) convertView
						.findViewById(R.id.meeting_order_name_text))
						.setText(organizer);
				((TextView) convertView.findViewById(R.id.meeting_title_text))
						.setText(meeting.getTopic());

				((LinearLayout) convertView.findViewById(R.id.meeting_layout))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								// Bundle bundle = new Bundle();
								// bundle.putSerializable("meeting", meeting);
								// startActivityForResult(intent, requestCode);
								// IntentUtils.startActivity(
								// MeettingsRoomDetailActivity.this,
								// MeettingDetailActivity.class, bundle);
								startMeetingDetailAcitivity(meeting);
							}
						});

				((LinearLayout) convertView.findViewById(R.id.meeting_layout))
						.setOnLongClickListener(new OnLongClickListener() {

							@Override
							public boolean onLongClick(View v) {
								uid = ((MyApplication) getApplicationContext())
										.getUid();
								boolean isAdmin = PreferencesUtils.getBoolean(
										getApplicationContext(),
										UriUtils.tanent + uid + "isAdmin",
										false);
								if (isAdmin
										|| meetingSchedule.getMeeting()
												.getOrganizer().equals(uid)) {
									showDeleteMeetingDlg(meetingSchedule);
								}

								return true;
							}

						});
			}
			return convertView;
		}

	}

	/**
	 * 启动详情
	 * 
	 * @param meeting
	 */
	protected void startMeetingDetailAcitivity(Meeting meeting) {
		Intent intent = new Intent();
		intent.putExtra("meeting", meeting);
		intent.setClass(MeetingsRoomDetailActivity.this,
				MeetingDetailActivity.class);
		startActivityForResult(intent, MEETING_DETAIL);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null && resultCode == RESULT_OK) {
			if (data.hasExtra("delete")) {
				Meeting meeting = (Meeting) data
						.getSerializableExtra("delete");
				deleteMeeting(meeting.getMeetingId());
			} else if (data.hasExtra("update")) {
				Meeting meeting = (Meeting) data
						.getSerializableExtra("update");
				updateMeeting(meeting);
			}
		}
	}

	/**
	 * 更改会议刷新列表
	 * 
	 * @param meeting
	 */
	private void updateMeeting(Meeting meeting) {
		if (meeting == null) {
			return;
		}
		Iterator<Meeting> sListIterator = allMeetingList.iterator();
		while (sListIterator.hasNext()) {
			Meeting meetingNext = sListIterator.next();
			if (meeting.equals(meetingNext)) {
				int index = allMeetingList.indexOf(meetingNext);
				allMeetingList.add(index, meeting);
				allMeetingList.remove(index + 1);
			}
		}
		initData();
	}

	/**
	 * 删除会议
	 * @param meetingId
     */
	private void deleteMeeting(String meetingId) {
		if (StringUtils.isBlank(meetingId)) {
			return;
		}
		Iterator<Meeting> sListIterator = allMeetingList.iterator();
		while (sListIterator.hasNext()) {
			Meeting meeting = sListIterator.next();
			if (!StringUtils.isBlank(meetingId)
					&& meetingId.equals(meeting.getMeetingId())) {
				sListIterator.remove();
			}
		}
		initData();
	}

	/**
	 * 弹出取消会议提示框
	 *
	 * @param meetingSchedule
	 */
	private void showDeleteMeetingDlg(final MeetingSchedule meetingSchedule) {
		// TODO Auto-generated method stub
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == -1) {
					deleteMeeting(meetingSchedule);
				} else {
					dialog.dismiss();
				}
			}
		};

		EasyDialog.showDialog(MeetingsRoomDetailActivity.this,
				getString(R.string.prompt),
				getString(R.string.meeting_list_cirform),
				getString(R.string.ok), getString(R.string.cancel), listener,
				true);
	}

	/**
	 * 长按删除会议的接口
	 */
	private void deleteMeeting(MeetingSchedule meetingSchedule) {
		if (NetUtils.isNetworkConnected(MeetingsRoomDetailActivity.this)) {
			loadingDlg.show();
			deleteMeeting = meetingSchedule.getMeeting();
			String meetingId = deleteMeeting.getMeetingId();
			apiService.deleteMeeting(meetingId);
		}
	}

	/**
	 * 初始化从网络获取来的数据
	 */
	private void initData() {
		List<List<Meeting>> group = new ArrayList<List<Meeting>>();
		for (int i = 0; i < displayDayCount; i++) {
			List<Meeting> dayMeetingList = new ArrayList<Meeting>();
			group.add(dayMeetingList);
		}
		for (int i = 0; i < allMeetingList.size(); i++) {
			String from = allMeetingList.get(i).getFrom();
			Calendar meetingBeginCalendar = TimeUtils.timeString2Calendar(from);
			int between = -1;
			try {
				between = TimeUtils.daysBetween(
						currentCalendar.getTimeInMillis() + "",
						meetingBeginCalendar.getTimeInMillis() + "");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			if (between >= 0 && between < displayDayCount) {
				group.get(between).add(allMeetingList.get(i));
			}

		}

		initMeetingSchedule(group);
		initListView();
	}

	/**
	 * 构造今天和明天会议显示数据
	 * 
	 */
	private void initMeetingSchedule(List<List<Meeting>> group) {
		// TODO Auto-generated method stub
		allDaysMeetingScheduleList = new ArrayList<List<MeetingSchedule>>();
		for (int i = 0; i < group.size(); i++) {
			LogUtils.debug("jason", "i=" + i);
			List<Meeting> dayMeetingList = group.get(i);
			List<MeetingSchedule> dayMeetingScheduleList = new ArrayList<MeetingSchedule>();
			HashSet<Long> set = new HashSet<Long>();
			long dayStartTimeLong = TimeUtils.getTimeLongFromTargetTime(
					currentCalendar, i, dayStartTime);
			long dayEndTimeLong = TimeUtils.getTimeLongFromTargetTime(
					currentCalendar, i, dayEndTime);

			boolean isMeetingBeforeDayStartTime = false;
			boolean isMeetingAfterDayEndTime = false;
			// 添加所有会议的开头和结尾时间点
			for (int j = 0; j < dayMeetingList.size(); j++) {
				long meetingFromLong = Long.parseLong(dayMeetingList.get(j)
						.getFrom());
				long meetingToLong = Long.parseLong(dayMeetingList.get(j)
						.getTo());
				if (meetingFromLong <= dayStartTimeLong) {
					isMeetingBeforeDayStartTime = true;
				}
				if (meetingToLong >= dayEndTimeLong) {
					isMeetingAfterDayEndTime = true;
				}
				set.add(meetingFromLong);
				set.add(meetingToLong);
			}

			if (!isMeetingBeforeDayStartTime) {
				set.add(dayStartTimeLong);
			}
			if (!isMeetingAfterDayEndTime) {
				set.add(dayEndTimeLong);
			}

			List<Long> listWithoutDup = new ArrayList<Long>();
			listWithoutDup.addAll(set);
			Collections.sort(listWithoutDup);// 排序
			// 将所有时间点整理成时间片段
			for (int j = 1; j < listWithoutDup.size(); j++) {
				MeetingSchedule meetingSchedule = new MeetingSchedule();
				meetingSchedule.setFrom(listWithoutDup.get(j - 1));
				meetingSchedule.setTo(listWithoutDup.get(j));
				dayMeetingScheduleList.add(meetingSchedule);
			}

			// 给meetingSchedule的meeting 成员变量赋值
			for (int j = 0; j < dayMeetingScheduleList.size(); j++) {
				MeetingSchedule meetingSchedule = dayMeetingScheduleList.get(j);
				for (int k = 0; k < dayMeetingList.size(); k++) {
					Meeting meeting = dayMeetingList.get(k);
					if ((meetingSchedule.getFrom() + "").equals(meeting
							.getFrom())) {
						meetingSchedule.setMeeting(meeting);
						;
					}
				}
			}
			allDaysMeetingScheduleList.add(dayMeetingScheduleList);
		}

	}

	/**
	 * 获取此会议室所有的会议
	 */
	private void getMeetingList(boolean isShowDlg) {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			loadingDlg.show(isShowDlg);
			apiService.getRoomMeetingList(bid);
		}
	}

	class WebService extends APIInterfaceInstance {

		@Override
		public void returnMeetingListSuccess(
				GetMeetingListResult getMeetingListResult, String date) {
			// TODO Auto-generated method stub
			super.returnMeetingListSuccess(getMeetingListResult);
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}

			setCurrentCalendar(date);
			allMeetingList = getMeetingListResult.getMeetingsList();
			initData();

		}

		@Override
		public void returnMeetingListFail(String error) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MeetingsRoomDetailActivity.this, error);
		}

		@Override
		public void returnDelMeetingSuccess() {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			ToastUtils.show(MeetingsRoomDetailActivity.this,
					getString(R.string.meeting_list_cancel_success));
			allMeetingList.remove(deleteMeeting);
			initData();
		}

		@Override
		public void returnDelMeetingFail(String error) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MeetingsRoomDetailActivity.this, error);
		}

	}

}
