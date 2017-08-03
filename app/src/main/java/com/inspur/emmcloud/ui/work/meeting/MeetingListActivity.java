package com.inspur.emmcloud.ui.work.meeting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.GetMeetingsResult;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.Room;
import com.inspur.emmcloud.util.GroupUtils;
import com.inspur.emmcloud.util.GroupUtils.GroupBy;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableExpandableListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MeetingListActivity
 * 
 * @author 包含预定过的所有会议
 */
public class MeetingListActivity extends BaseActivity implements
		OnRefreshListener {
	private static final int WEEK_SUNDAY = 1;
	private static final int WEEK_MONDAY = 2;
	private static final int WEEK_TUESDAY = 3;
	private static final int WEEK_WENDNESDAY = 4;
	private static final int WEEK_THURSDAY = 5;
	private static final int WEEK_FRIDAY = 6;
	private static final int WEEK_SATURDAY = 7;
	private PullableExpandableListView expandListView;
	private MyAdapter adapter;
	private LoadingDialog loadingDlg;
	private WorkAPIService apiService;
	private PullToRefreshLayout pullToRefreshLayout;
	private LinearLayout relNullLayout;
	private int groupIdCancel, childIdCancel;
	private Map<String, List<Meeting>> meetingMap = new HashMap<String, List<Meeting>>();
	private List<String> meetingGroupList = new ArrayList<String>();
	private BroadcastReceiver meetingReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_meeting_list);
		initViews();
		initListener();
		getMeetings(true);
		registerMeetingReceiver();
	}

	/**
	 * 获取会议列表
	 */
	private void getMeetings(boolean isShowDlg) {
		if (NetUtils.isNetworkConnected(MeetingListActivity.this)) {
			loadingDlg.show(isShowDlg);
			apiService.getMeetings(7);
		}
	}

	/**
	 * 注册刷新任务的广播
	 */
	private void registerMeetingReceiver() {
		meetingReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.hasExtra("refreshMeeting")) {
					if (NetUtils.isNetworkConnected(MeetingListActivity.this)) {
						getMeetings(true);
					}
				}
			}
		};
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("com.inspur.meeting");
		registerReceiver(meetingReceiver, myIntentFilter);
	}

	/**
	 * 初始化Views
	 */
	private void initViews() {
		apiService = new WorkAPIService(MeetingListActivity.this);
		apiService.setAPIInterface(new WebService());

		loadingDlg = new LoadingDialog(MeetingListActivity.this);
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.meeting_expandlistview_layout);
		pullToRefreshLayout.setOnRefreshListener(MeetingListActivity.this);
		relNullLayout = (LinearLayout) findViewById(R.id.meeting_list_out_layout);
		expandListView = (PullableExpandableListView) findViewById(R.id.meeting_expandablelistview);
		expandListView.setGroupIndicator(null);
		expandListView.setVerticalScrollBarEnabled(false);
		expandListView.setHeaderDividersEnabled(false);
		expandListView.setCanpullup(false);
		expandListView.setCanpulldown(true);
		expandListView.setOnChildClickListener(new MeetingListListener());
	}

	/**
	 * 初始化监听器
	 */
	private void initListener() {
		expandListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						if (view.getTag(R.id.meeting_list_duration_text) == null
								|| view.getTag(R.id.meeting_list_date_img) == null) {
							return true;
						}
						final int groupId = (Integer) view
								.getTag(R.id.meeting_list_duration_text);
						final int childId = (Integer) view
								.getTag(R.id.meeting_list_date_img);
						String userId = ((MyApplication) getApplication())
								.getUid();
						String organizer = meetingMap
								.get(meetingGroupList.get(groupId))
								.get(childId).getOrganizer();
						boolean isAdmin = PreferencesUtils.getBoolean(
								MeetingListActivity.this, UriUtils.tanent
										+ userId + "isAdmin");
						if (!isAdmin) {
							if (!userId.equals(organizer)) {
								ToastUtils
										.show(MeetingListActivity.this,
												getString(R.string.meeting_list_no_self));
								return true;
							}
						}
						DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == -1) {
									deleteMeeting(groupId, childId);
								} else {
									dialog.dismiss();
								}
							}
						};
						EasyDialog.showDialog(MeetingListActivity.this,
								getString(R.string.prompt),
								getString(R.string.meeting_list_cirform),
								getString(R.string.ok),
								getString(R.string.cancel), listener, true);
						return true;
					}
				});
	}

	/**
	 * 删除会议
	 * 
	 * @param groupId
	 * @param childId
	 */
	private void deleteMeeting(int groupId, int childId) {
		if (NetUtils.isNetworkConnected(MeetingListActivity.this)) {
			loadingDlg.show();
			String meetingId = meetingMap.get(meetingGroupList.get(groupId))
					.get(childId).getMeetingId();
			apiService.deleteMeeting(meetingId);
			groupIdCancel = groupId;
			childIdCancel = childId;
		}
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.meeting_list_add_img:
		case R.id.meeting_list_btn:
			intent.setClass(MeetingListActivity.this,
					MeetingBookingActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.meeting_list_history_img:
			intent.setClass(MeetingListActivity.this,
					MeetingHistoryListActivity.class);
			IntentUtils.startActivity(MeetingListActivity.this,
					MeetingHistoryListActivity.class);
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		unregisterReceiver();
		sendBroadCast();
		finish();
	}

	/**
	 * 发送广播
	 */
	private void sendBroadCast() {
		Intent mIntent = new Intent("com.inspur.meeting");
		mIntent.putExtra("refreshMeeting", "refreshMeeting");
		sendBroadcast(mIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			getMeetings(true);
			setResult(RESULT_OK);
		}
	}

	/**
	 * expandableListView适配器
	 *
	 */
	public class MyAdapter extends BaseExpandableListAdapter {
		@Override
		public int getGroupCount() {
			return meetingGroupList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (meetingMap.get(meetingGroupList.get(groupPosition)) != null) {
				return meetingMap.get(meetingGroupList.get(groupPosition))
						.size();
			} else {
				return 0;
			}

		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			PullableExpandableListView expandableListView = (PullableExpandableListView) parent;
			expandableListView.expandGroup(groupPosition);
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(MeetingListActivity.this)
						.inflate(R.layout.meeting_list_group_item, null);
				holder = new ViewHolder();
				holder.dateText = (TextView) convertView
						.findViewById(R.id.date_text);
				holder.weekText = (TextView) convertView
						.findViewById(R.id.week_text);
				holder.todayText = (TextView) convertView
						.findViewById(R.id.today_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String from = meetingMap.get(meetingGroupList.get(groupPosition))
					.get(0).getFrom();
			String week = getWeekDay(from);
			Calendar calendar = TimeUtils.timeString2Calendar(from);
			String date = TimeUtils.calendar2FormatString(
					MeetingListActivity.this, calendar,
					TimeUtils.FORMAT_YEAR_MONTH_DAY);
			holder.dateText.setText(date);
			holder.weekText.setText(week);
			if (TimeUtils.isCalendarToday(TimeUtils.timeString2Calendar(from))) {
				holder.todayText.setText("(" + getString(R.string.today) + ")");
			} else {
				holder.todayText.setText("");
			}
			return convertView;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			ExpandViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(MeetingListActivity.this)
						.inflate(R.layout.meeting_list_item, null);
				holder = new ExpandViewHolder();
				holder.meetingTopicText = (TextView) convertView
						.findViewById(R.id.meeting_list_topic_text);
				holder.meetingNoticeText = (TextView) convertView
						.findViewById(R.id.meeting_list_notice_text);
				holder.meetingDuration = (TextView) convertView
						.findViewById(R.id.meeting_list_duration_text);
				holder.meetingRoom = (TextView) convertView
						.findViewById(R.id.meeting_list_room_text);
				holder.meetingCardLineView = (View) convertView
						.findViewById(R.id.meeting_card_line);
				convertView.setTag(holder);
			} else {
				holder = (ExpandViewHolder) convertView.getTag();
			}
			Meeting meeting = meetingMap.get(
					meetingGroupList.get(groupPosition)).get(childPosition);
			Room room = meeting.getRooms().get(0);
			String meetingTime = "";
			meetingTime = getTimeFormat(meeting);
			holder.meetingTopicText.setText(meeting.getTopic());
			holder.meetingDuration.setText(meetingTime);
			holder.meetingRoom.setText(room.getRoomName() + " "
					+ room.getName());
			holder.meetingNoticeText.setText(meeting.getNotice());
			convertView.setTag(R.id.meeting_list_date_img, childPosition);
			convertView.setTag(R.id.meeting_list_duration_text, groupPosition);
			return convertView;
		}

		class ViewHolder {
			TextView dateText, weekText, todayText;
		}

		class ExpandViewHolder {
			TextView meetingTopicText;
			TextView meetingDuration;
			TextView meetingRoom;
			TextView meetingNoticeText;
			View meetingCardLineView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	/**
	 * 初始化数据
	 * 
	 * @param getMeetingsResult
	 */
	private void initData(GetMeetingsResult getMeetingsResult) {
		getMeetingsResult.getMeetingsList();
		meetingMap = GroupUtils.group(getMeetingsResult.getMeetingsList(),
				new MeetingGroup());
		if (meetingMap != null && meetingMap.size() > 0) {
			meetingGroupList = new ArrayList<String>(meetingMap.keySet());
			Collections.sort(meetingGroupList, new SortClass());
		}
		if (meetingMap == null || meetingMap.size() == 0) {
			relNullLayout.setVisibility(View.VISIBLE);
			pullToRefreshLayout.setVisibility(View.GONE);
		} else {
			relNullLayout.setVisibility(View.GONE);
			pullToRefreshLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 得到格式化的时间如06:00-07:30
	 * 
	 * @param meeting
	 * @return
	 */
	public String getTimeFormat(Meeting meeting) {
		String from = meeting.getFrom();
		String to = meeting.getTo();
		String dateFromTime = "", dateToTime = "";
		dateFromTime = getFromTime(from);
		dateToTime = getToTime(to);
		String meetingTime = dateFromTime + "-" + dateToTime;
		return meetingTime;
	}

	/**
	 * 获取结束时间
	 * 
	 * @return
	 */
	private String getToTime(String to) {
		Calendar calendarTo = TimeUtils.timeString2Calendar(to);
		String dateToTime = TimeUtils.calendar2FormatString(
				MeetingListActivity.this, calendarTo,
				TimeUtils.DATE_FORMAT_HOUR_MINUTE);
		return dateToTime;
	}

	/**
	 * @param from
	 * @return
	 */
	private String getFromTime(String from) {
		Calendar calendFrom = TimeUtils.timeString2Calendar(from);
		String dateFromTime = TimeUtils.calendar2FormatString(
				MeetingListActivity.this, calendFrom,
				TimeUtils.DATE_FORMAT_HOUR_MINUTE);
		return dateFromTime;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver();
	}

	/**
	 * 注销广播
	 */
	private void unregisterReceiver() {
		if (meetingReceiver != null) {
			unregisterReceiver(meetingReceiver);
			meetingReceiver = null;
		}
	}

	/**
	 * 获取显示
	 * 
	 * @param from
	 * @return
	 */
	public String getWeekDay(String from) {
		String week = "";
		int dayOfWeek = TimeUtils.dayForWeek(from);
		switch (dayOfWeek) {
		case WEEK_MONDAY:
			week = week + getString(R.string.monday);
			break;
		case WEEK_TUESDAY:
			week = week + getString(R.string.tuesday);
			break;
		case WEEK_WENDNESDAY:
			week = week + getString(R.string.wednesday);
			break;
		case WEEK_THURSDAY:
			week = week + getString(R.string.thursday);
			break;
		case WEEK_FRIDAY:
			week = week + getString(R.string.friday);
			break;
		case WEEK_SATURDAY:
			week = week + getString(R.string.saturday);
			break;
		case WEEK_SUNDAY:
			week = week + getString(R.string.sunday);
			break;
		default:
			break;
		}
		return week;
	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		getMeetings(false);
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
	}

	class WebService extends APIInterfaceInstance {
		@Override
		public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			initData(getMeetingsResult);
			adapter = new MyAdapter();
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			expandListView.setAdapter(adapter);
		}

		@Override
		public void returnMeetingsFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
			WebServiceMiddleUtils.hand(MeetingListActivity.this, error,errorCode);
		}

		@Override
		public void returnDelMeetingSuccess() {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			ToastUtils.show(MeetingListActivity.this,
					getString(R.string.meeting_list_cancel_success));
			meetingMap.get(meetingGroupList.get(groupIdCancel)).remove(
					childIdCancel);
			if (meetingMap.get(meetingGroupList.get(groupIdCancel)).size() == 0) {
				meetingGroupList.remove(groupIdCancel);
			}
			if (meetingGroupList.size() == 0) {
				pullToRefreshLayout.setVisibility(View.GONE);
				relNullLayout.setVisibility(View.VISIBLE);
			}
			adapter.notifyDataSetChanged();
		}

		@Override
		public void returnDelMeetingFail(String error,int errorCode) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(MeetingListActivity.this, error,errorCode);
		}

	}

	/**
	 * 点击 时间监听，数据传递
	 * 
	 */
	class MeetingListListener implements OnChildClickListener {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			Meeting meeting = meetingMap.get(
					meetingGroupList.get(groupPosition)).get(childPosition);
			Bundle bundle = new Bundle();
			bundle.putSerializable("meeting", meeting);
			IntentUtils.startActivity(MeetingListActivity.this,
					MeetingDetailActivity.class, bundle);
			return false;
		}

	}

	/**
	 * 分类接口实现
	 *
	 */
	class MeetingGroup implements GroupBy<String> {
		@Override
		public String groupBy(Object obj) {
			SimpleDateFormat format = new SimpleDateFormat(
					getString(R.string.format_date_group_by));
			Meeting meeting = (Meeting) obj;
			String from = meeting.getFrom();
			Calendar calendarForm = TimeUtils.timeString2Calendar(from);
			String dateFromTime = TimeUtils.calendar2FormatString(
					MeetingListActivity.this, calendarForm, format);
			return dateFromTime;
		}

	}

	/**
	 * 排序接口
	 *
	 */
	public class SortClass implements Comparator {
		public int compare(Object arg0, Object arg1) {
			String dateA = (String) arg0;
			String dateB = (String) arg1;
			dateA = dateA.replace("-", "");
			dateB = dateB.replace("-", "");
			int fromA = Integer.parseInt(dateA);
			int fromB = Integer.parseInt(dateB);
			if (fromA > fromB) {
				return 1;
			} else if (fromA < fromB) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
