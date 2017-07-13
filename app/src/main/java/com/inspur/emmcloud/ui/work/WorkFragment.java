package com.inspur.emmcloud.ui.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.CalendarEvent;
import com.inspur.emmcloud.bean.FestivalDate;
import com.inspur.emmcloud.bean.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.GetMeetingsResult;
import com.inspur.emmcloud.bean.GetMyCalendarResult;
import com.inspur.emmcloud.bean.GetTaskListResult;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.MyCalendar;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.ui.work.WorkFragment.MyAdapter.ExpandViewHolder;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingBookingActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingDetailActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionDetailActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.CalEventNotificationUtils;
import com.inspur.emmcloud.util.CalendarUtil;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.FestivalCacheUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.WorkColorUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableExpandableListView;
import com.lidroid.xutils.exception.DbException;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * 工作页面
 * 
 * @author Administrator
 *
 */
public class WorkFragment extends Fragment implements OnRefreshListener {

	private static final int MEETING = 0;
	private static final int CAL = 1;
	private static final int TASK = 2;
	private static final int MEETING_MAX_SIZE = 3;//如果两天的会议不足时，补足三条
	private View rootView;
	private WorkAPIService apiService;
	private LoadingDialog loadingDialog;
	private PullableExpandableListView expandListView;
	private List<String> workGroup = new ArrayList<String>();;
	private MyAdapter adapter;
	private GetMeetingsResult getMeetingResult;
	private PullToRefreshLayout pullToRefreshLayout;
	private List<List<Meeting>> meetingList = new ArrayList<List<Meeting>>();;
	private ArrayList<TaskResult> taskList = new ArrayList<TaskResult>();
	private List<CalendarEvent> calEventList = new ArrayList<CalendarEvent>();
	private BroadcastReceiver calEventReceiver;
	private BroadcastReceiver meetingAndTaskReceiver;
	private List<String> calendarIdList = new ArrayList<String>();
	private String orderBy = "PRIORITY";
	private String orderType = "ASC";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_work, container,
					false);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		return rootView;
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
				getActivity().LAYOUT_INFLATER_SERVICE);
		rootView = inflater.inflate(R.layout.fragment_work, null);
		initData();
		initViews();
		getMeetings();
		getCalendarEvent();
		getTasks();
		headDate();
		registerCalEventReceiver();
		registerMeetingAndTaskReceiver();
		getOrder();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
	}
	
	/**
	 * 获取缓存的排序规则
	 */
	private void getOrder() {
		orderBy = PreferencesUtils.getString(getActivity(),
				"order_by","PRIORITY");
		orderType = PreferencesUtils.getString(getActivity(),
				"order_type","ASC");
	}
	

	/**
	 * 注册关于CalendarEvent广播，便于更新数据
	 */
	private void registerCalEventReceiver() {
		calEventReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.hasExtra("addCalEvent")
						|| intent.hasExtra("editCalEvent")
						|| intent.hasExtra("editCalendar")) {
					getCalendarEvent();
				} else if (intent.hasExtra("deleteCalEvent")) {
					CalendarEvent deleteCalEvent = (CalendarEvent) intent
							.getSerializableExtra("deleteCalEvent");
					// 修复迭代问题
					Iterator<CalendarEvent> sListIterator = calEventList
							.iterator();
					while (sListIterator.hasNext()) {
						CalendarEvent cal = sListIterator.next();
						if (cal.getId().equals(deleteCalEvent.getId())) {
							sListIterator.remove();
						}
					}
					adapter.notifyDataSetChanged();
				}

			}

		};
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("editcalendar_event");
		getActivity().registerReceiver(calEventReceiver, myIntentFilter);
	}

	/**
	 * 注册刷新任务和会议的广播
	 */
	private void registerMeetingAndTaskReceiver() {
		meetingAndTaskReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.hasExtra("refreshTask")) {
					if (NetUtils.isNetworkConnected(getActivity())) {
						getOrder();
						apiService.getRecentTasks(orderBy, orderType);
					}
				} else if (intent.hasExtra("refreshMeeting")) {
					if (NetUtils.isNetworkConnected(getActivity())) {
						apiService.getMeetings(7);
					}
				}
			}
		};
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("com.inspur.meeting");
		myIntentFilter.addAction("com.inspur.task");
		getActivity().registerReceiver(meetingAndTaskReceiver, myIntentFilter);
	}

	/**
	 * 获取日历中Event
	 */
	private void getCalendarEvent() {
		if (NetUtils.isNetworkConnected(getActivity())) {
			apiService.getMyCalendar(0, 100);
		}
	}

	/**
	 * 获取今明两天所有日历的所有event
	 */
	private void getTwoDaysCalEvents() {
		if (NetUtils.isNetworkConnected(getActivity())) {
			Calendar afterCalendar = Calendar.getInstance();
			Calendar beforeCalendar = Calendar.getInstance();
			beforeCalendar.set(beforeCalendar.get(Calendar.YEAR),
					beforeCalendar.get(Calendar.MONTH),
					beforeCalendar.get(Calendar.DAY_OF_MONTH) + 2, 0, 0, 0);
			afterCalendar.set(afterCalendar.get(Calendar.YEAR),
					afterCalendar.get(Calendar.MONTH),
					afterCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			afterCalendar = TimeUtils.localCalendar2UTCCalendar(afterCalendar);
			beforeCalendar = TimeUtils.localCalendar2UTCCalendar(beforeCalendar);
			apiService.getAllCalEvents(calendarIdList, afterCalendar,
					beforeCalendar, 5, 0, true);
		}
	}

	/**
	 * 获取三条Event
	 */
	private void getCalEventsFor3() {
		if (NetUtils.isNetworkConnected(getActivity())) {
			Calendar afterCalendar = Calendar.getInstance();
			Calendar beforeCalendar = Calendar.getInstance();
			beforeCalendar.set(beforeCalendar.get(Calendar.YEAR) + 1,
					beforeCalendar.get(Calendar.MONTH),
					beforeCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			afterCalendar.set(afterCalendar.get(Calendar.YEAR),
					afterCalendar.get(Calendar.MONTH),
					afterCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			afterCalendar = TimeUtils.localCalendar2UTCCalendar(afterCalendar);
			beforeCalendar = TimeUtils.localCalendar2UTCCalendar(beforeCalendar);
			apiService.getAllCalEvents(calendarIdList, afterCalendar,
					beforeCalendar, 3, 0, false);
		}

	}

	/**
	 * 刷新会议
	 */
	private void getMeetings() {
		if (NetUtils.isNetworkConnected(getActivity())) {
			apiService.getMeetings(7);
		}				
	}

	/**
	 * 头部时间处理节假日部分
	 */
	private void headDate() {
		FestivalDate festivalDate = initFestivalDate();
		Calendar calendar = Calendar.getInstance();
//		calendar.set(2017, 0, 27);
		calendar.setTimeInMillis(festivalDate.getFestivalTime());
		int betweenQM = 0;
		betweenQM = TimeUtils.getCountdownNum(calendar);
		calendar.setTimeInMillis(System.currentTimeMillis());
		((TextView) (rootView.findViewById(R.id.work_date_text)))
				.setText(TimeUtils.calendar2FormatString(getActivity(), calendar, TimeUtils.FORMAT_MONTH_DAY));
		String appLanguageObj = PreferencesUtils.getString(
				getActivity(), UriUtils.tanent+"appLanguageObj","");
		Language language = new Language(appLanguageObj);
		if (language.getIso().equals("zh-CN")
				|| language.equals("zh-TW")
				|| language.equals("followSys")) {
			((TextView) (rootView.findViewById(R.id.work_chinesedate_text)))
					.setText(CalendarUtil.getChineseToday()
							+ TimeUtils.getWeekDay(getContext(), calendar));
		} else if (language.getIso().equals("en-US")) {
			((TextView) (rootView.findViewById(R.id.work_chinesedate_text)))
					.setText(TimeUtils.calendar2FormatString(getActivity(),
							calendar, TimeUtils.FORMAT_MONTH_DAY)
							+ "  "
							+ TimeUtils.getWeekDay(getContext(), calendar));
		}

		String festivalDateTips = FestivalCacheUtils.getFestivalTips(getActivity(), festivalDate.getFestivalKey());
		((TextView) (rootView.findViewById(R.id.work_festvaldate_text)))
				.setText(festivalDateTips +"  "+ betweenQM
						+" "+ getString(R.string.work_day));
		if (betweenQM < 0) {
			((TextView) (rootView.findViewById(R.id.work_festvaldate_text)))
					.setText(festivalDateTips + 0
							+ getString(R.string.work_day));
		}
	}

	/**
	 * 刷新任务
	 */
	private void getTasks() {
		if (NetUtils.isNetworkConnected(getActivity())) {
			loadingDialog.show();
			getOrder();
			apiService.getRecentTasks(orderBy, orderType);
		}
	}

	/**
	 * 初始化views
	 */
	private void initViews() {
		ImageView img = (ImageView) rootView.findViewById(R.id.header_bg_img);
		String imageUri = "drawable://" + R.drawable.work_navi_back;
		new ImageDisplayUtils(getContext(), R.drawable.bg_corner).display(img,
				imageUri);
		expandListView = (PullableExpandableListView) rootView
				.findViewById(R.id.expandableListView);
		pullToRefreshLayout = (PullToRefreshLayout) rootView
				.findViewById(R.id.refresh_view);
		pullToRefreshLayout.setOnRefreshListener(WorkFragment.this);
		apiService = new WorkAPIService(getActivity());
		apiService.setAPIInterface(new WebService());
		loadingDialog = new LoadingDialog(getActivity());
		expandListView.setGroupIndicator(null);
		expandListView.setVerticalScrollBarEnabled(false);
		expandListView.setHeaderDividersEnabled(false);
		expandListView.setCanpullup(false);
		expandListView.setCanpulldown(true);
		expandListView.setOnGroupClickListener(new WorkGroupListener());
		expandListView.setOnChildClickListener(new WorkChildClickListener());
		adapter = new MyAdapter();
		expandListView.setAdapter(adapter);
	}

	/**
	 * expandableListView适配器
	 *
	 */
	public class MyAdapter extends BaseExpandableListAdapter {
		public MyAdapter() {
		}

		@Override
		public int getGroupCount() {
			return 3;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (groupPosition == 0) {
				if (meetingList != null && meetingList.size() > 0
						&& meetingList.get(groupPosition).size() > 0) {
					return meetingList.get(0).size();
				} else {
					return 1;
				}
			} else if (groupPosition == 2) {
				if (taskList.size() == 0) {
					return 1;
				}
				if (taskList.size() < 5) {
					return taskList.size();
				} else {
					return 5;
				}
			} else {
				if (calEventList.size() == 0) { // 暂无日程
					return 1;
				}
				return calEventList.size();
			}

		}

		@Override
		public Object getGroup(int groupPosition) {
			return 0;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return 0;
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

		/**
		 * 显示：group
		 */
		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			PullableExpandableListView expandableListView = (PullableExpandableListView) parent;
			expandableListView.expandGroup(groupPosition);
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.expand_list, null);
				holder = new ViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.work_right_img);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(workGroup.get(groupPosition));
			holder.imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					if (groupPosition == 0) {
						intent.setClass(getActivity(),
								MeetingListActivity.class);
						startActivityForResult(intent, 0);
					} else if (groupPosition == 1) {
						intent.setClass(getActivity(), CalActivity.class);
						startActivity(intent);
					} else if (groupPosition == 2) {
						intent.setClass(getActivity(),
								MessionListActivity.class);
						startActivity(intent);
					}
				}
			});
			return convertView;
		}

		/**
		 * 显示：child
		 */
		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			ExpandViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.list_item, null);
				holder = new ExpandViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.textView);
				holder.textViewdate = (TextView) convertView
						.findViewById(R.id.work_icontext_text);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.work_icon_img);
				holder.relativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.work_expand_layout);
				holder.meetingTime = (TextView) convertView
						.findViewById(R.id.work_meetingtime_text);
				holder.iconLayout = (RelativeLayout) convertView
						.findViewById(R.id.work_icon_layout);
				holder.addImg = (ImageView) convertView
						.findViewById(R.id.add_img);
				convertView.setTag(holder);
			} else {
				holder = (ExpandViewHolder) convertView.getTag();
			}
			if (groupPosition == MEETING) {
				if (getMeetingResult == null
						|| getMeetingResult.getMeetingsList().size() == 0) {
					holder.meetingTime.setVisibility(View.INVISIBLE);
					holder.iconLayout.setVisibility(View.INVISIBLE);
					holder.addImg.setVisibility(View.VISIBLE);
					holder.textView
							.setText(getString(R.string.meeting_add_meeting));
					holder.textView.setTextColor(Color.parseColor("#4a90e2"));
				} else {
					holder.textView.setTextColor(Color.parseColor("#333333"));
					holder.addImg.setVisibility(View.INVISIBLE);
					holder.iconLayout.setVisibility(View.VISIBLE);
					holder.meetingTime.setVisibility(View.VISIBLE);
					String meetingFrom = getMeetingResult.getMeetingsList()
							.get(childPosition).getFrom();
					if (!StringUtils.isBlank(meetingFrom)) {
						initMeetingItems(holder,
								groupPosition, childPosition);
					}
					Calendar cal = TimeUtils
							.timeString2Calendar(getMeetingResult
									.getMeetingsList().get(childPosition)
									.getFrom());
					holder.textViewdate.setTextSize(12);
					if (cal != null) {
						holder.textViewdate.setText(TimeUtils.getCountdown(
								getActivity(), cal));
					}
				}
			} else if (groupPosition == CAL) {
				String calEventTimeSection = "";
				holder.textViewdate.setTextSize(12);
				if (calEventList.size() == 0) {
					holder.textView
							.setText(getString(R.string.work_nocal_text));
					holder.iconLayout.setVisibility(View.INVISIBLE);
				} else {
					CalendarEvent calEvent = calEventList.get(childPosition);
					holder.textView.setText(calEvent.getTitle());
					calEventTimeSection = TimeUtils
							.getCalEventTimeSelection(getActivity(),calEvent);
					holder.iconLayout.setVisibility(View.VISIBLE);
					WorkColorUtils.showDayOfWeek( holder.imageView,
							 TimeUtils
									.getCountdownNum(calEvent
											.getLocalStartDate()));
					holder.textViewdate.setText(TimeUtils.getCountdown(getActivity(),calEvent
							.getLocalStartDate()));
				}
				holder.meetingTime.setText(calEventTimeSection);
			} else if (groupPosition == TASK) {
				int taskCountdown = 0;
				if (taskList.size() == 0) {
					holder.textView
							.setText(getString(R.string.work_notask_text));
					holder.iconLayout.setVisibility(View.INVISIBLE);
				} else if (taskList.size() > 0) {
					holder.textView.setText(taskList.get(childPosition)
							.getTitle());
					holder.iconLayout.setVisibility(View.VISIBLE);
						try {
							taskCountdown = TimeUtils.daysBetweenToday(taskList.get(
									childPosition).getCreationDate());
						} catch (ParseException e) {
							e.printStackTrace();
						}
					WorkColorUtils.showDayOfWeek(holder.imageView,
							Math.abs(taskCountdown));
					Calendar calendar = taskList.get(childPosition).getDueDate();
					holder.textViewdate.setTextSize(10);
					if (calendar != null) {
						if (!StringUtils.isEmpty(TimeUtils
								.getCountdown(getActivity(),calendar))) {
							holder.textViewdate.setText(TimeUtils
									.getCountdown(getActivity(),calendar));
						}
						holder.meetingTime.setText(calendar.get(Calendar.YEAR)
								+ "-" + (calendar.get(Calendar.MONTH) + 1)
								+ "-" + calendar.get(Calendar.DAY_OF_MONTH));
					} else {
						holder.textViewdate
								.setText(TimeUtils.getTime(taskList.get(childPosition).getCreationDate()));
						holder.meetingTime
								.setText("");
					}

				}

			}
			return convertView;
		}

		class ViewHolder {
			TextView textView;
			ImageView imageView;
		}

		class ExpandViewHolder {
			TextView textView;
			ImageView imageView;
			TextView textViewdate;
			TextView meetingTime;
			RelativeLayout relativeLayout;
			View lineview;
			RelativeLayout iconLayout;
			ImageView addImg;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == getActivity().RESULT_OK) {
			if (NetUtils.isNetworkConnected(getActivity())) {
				loadingDialog.show();
				apiService.getMeetings(7);
				getOrder();
				apiService.getRecentTasks(orderBy, orderType);
			}

		}
	}

	/**
	 * 会议条目内容
	 * @param holder
	 * @param groupPosition
	 * @param childPosition
	 */
	public void initMeetingItems(
			ExpandViewHolder holder, final int groupPosition,
			final int childPosition) {
		if (groupPosition == 0) {
			holder.textView.setText(getMeetingResult.getMeetingsList()
					.get(childPosition).getTopic());
			String timeFrom = getMeetingResult.getMeetingsList()
					.get(childPosition).getFrom();
			if (!StringUtils.isBlank(timeFrom)) {
				Calendar calendar = TimeUtils.timeString2Calendar(timeFrom);
				holder.textViewdate.setText(TimeUtils.getCountdown(
						getActivity(), calendar));
				WorkColorUtils.showDayOfWeek(holder.imageView,
						TimeUtils.getCountdownNum(calendar));
			}else {
				holder.textViewdate.setText(getString(R.string.time_null));
			}
			if (getMeetingResult.getMeetingsList().size() != 0) {
				String from = getMeetingResult.getMeetingsList()
						.get(childPosition).getFrom();
				String meetingFromTime = TimeUtils.calendar2FormatString(
						getActivity(), TimeUtils.timeString2Calendar(from),
						TimeUtils.FORMAT_HOUR_MINUTE);
				String to = getMeetingResult.getMeetingsList()
						.get(childPosition).getTo();
				String meetingToTime = TimeUtils.calendar2FormatString(
						getActivity(), TimeUtils.timeString2Calendar(to),
						TimeUtils.FORMAT_HOUR_MINUTE);
				String meetingTime = meetingFromTime + " - " + meetingToTime;
				holder.meetingTime.setText(meetingTime);
			}
		}
		
	}

	/**
	 * 初始化工作组数据
	 */
	private void initData(){
		
		addWorkItem(getString(R.string.work_meeting_text));
		addWorkItem(getString(R.string.work_calendar_text));
		addWorkItem(getString(R.string.work_task_text));
	}
	
	/**
	 * 初始化节日
	 * @return
	 */
	private FestivalDate initFestivalDate(){
		FestivalDate festivalDate = null;
		try {
			if (!DbCacheUtils.getDb(getActivity()).tableIsExist(FestivalDate.class)) {
				FestivalCacheUtils.saveFestivalList(getActivity());
			} 
			festivalDate = FestivalCacheUtils.getFestival(getActivity());
		} catch (DbException e) {
			e.printStackTrace();
		}
		return festivalDate;
	}

	/**
	 * 添加工作条目
	 * @param workItemName
	 */
	private void addWorkItem(String workItemName) {
		workGroup.add(workItemName);
	}

	/**
	 * 初始化会议信息
	 * 
	 * @param getMeetingsResult
	 */
	private void initDataSize(GetMeetingsResult getMeetingsResult) {
		meetingList = new ArrayList<List<Meeting>>();
		// 四个会议的list，分别是今，明，后，三天
		List<Meeting> meetingToday, meetingTomorrow, meetingAfter, meetingOther;
		meetingToday = new ArrayList<Meeting>();
		meetingTomorrow = new ArrayList<Meeting>();
		meetingAfter = new ArrayList<Meeting>();
		meetingOther = new ArrayList<Meeting>();
		for (int i = 0; i < getMeetingsResult.getMeetingsList().size(); i++) {
			String from = getMeetingsResult.getMeetingsList().get(i)
					.getFrom();
			int between = -1;
			between = TimeUtils.getCountdownNum(TimeUtils.timeString2Calendar(from));
			switch (between) {
			case 0:
				meetingToday.add(getMeetingsResult.getMeetingsList().get(i));
				break;
			case 1:
				meetingTomorrow.add(getMeetingsResult.getMeetingsList().get(
						i));
				break;
			// case 2:
			// meettingAfter.add(getMeettingsResult.getMeettingsList().get(i));
			// break;
			default:
				meetingOther.add(getMeetingsResult.getMeetingsList().get(i));
				break;
			}
		}
		meetingToday.addAll(meetingTomorrow);
		if(meetingToday.size()>=MEETING_MAX_SIZE){
			meetingList.add(meetingToday);
		}else {
			ArrayList<Meeting>  meetingAdd = new ArrayList<Meeting>();
			int count = MEETING_MAX_SIZE - meetingToday.size();
			if(meetingOther.size()>=count){
				for (int i = 0; i < count; i++) {
					meetingAdd.add(meetingOther.get(i));
				}
			}else {
				meetingAdd.addAll(meetingOther);
			}
			meetingToday.addAll(meetingAdd);
			meetingList.add(meetingToday);
		}
		adapter.notifyDataSetChanged();
	}

	class WebService extends APIInterfaceInstance {
		@Override
		public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			WorkFragment.this.getMeetingResult = getMeetingsResult;
			initDataSize(getMeetingsResult);
		}

		@Override
		public void returnMeetingsFail(String error,int errorCode) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
			WebServiceMiddleUtils.hand(getActivity(), error,errorCode);
		}

		@Override
		public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			taskList = getTaskListResult.getTaskList();
			if (taskList.size() > 0 && (adapter != null)) {
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void returnRecentTasksFail(String error,int errorCode) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			WebServiceMiddleUtils.hand(getActivity(), error,errorCode);
		}

		@Override
		public void returnMyCalendarSuccess(
				GetMyCalendarResult getMyCalendarResult) {
			List<MyCalendar> calendarList = getMyCalendarResult
					.getCalendarList();
			MyCalendarCacheUtils
					.saveMyCalendarList(getActivity(), calendarList);
			if (calendarList != null && calendarList.size() > 0) {
				for (int i = 0; i < calendarList.size(); i++) {
					MyCalendar myCalendar = calendarList.get(i);
					if (!myCalendar.getState().equals("REMOVED")
							&& !MyCalendarOperationCacheUtils.getIsHide(
									getActivity(), myCalendar.getId())) {
						calendarIdList.add(calendarList.get(i).getId());
					}
				}
				getTwoDaysCalEvents();
			}
		}

		@Override
		public void returnMyCalendarFail(String error,int errorCode) {
			WebServiceMiddleUtils.hand(getActivity(), error,errorCode);
		}

		@Override
		public void returnCalEventsSuccess(
				GetCalendarEventsResult getCalendarEventsResult,
				boolean isRefresh) {
			calEventList = new ArrayList<CalendarEvent>();
			calEventList = getCalendarEventsResult.getCalEventList();
			for (int i = 0; i < calEventList.size(); i++) {
				CalendarEvent calEvent = calEventList.get(i);
				CalEventNotificationUtils.setCalEventNotification(
						getActivity().getApplicationContext(), calEvent);
			}
			if (isRefresh && (calEventList.size() < 3)) { // 获取今明两天的日历不足3条
				getCalEventsFor3();
			} else {
				if (loadingDialog != null && loadingDialog.isShowing()) {
					loadingDialog.dismiss();
				}
				adapter.notifyDataSetChanged();
			}

		}

		@Override
		public void returnCalEventsFail(String error, boolean isRefresh,int errorCode) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			WebServiceMiddleUtils.hand(getActivity(), error,errorCode);
		}

	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		if (NetUtils.isNetworkConnected(getActivity())) {
			apiService.getMeetings(7);
			getOrder();
			apiService.getMyCalendar(0, 30);
			apiService.getRecentTasks(orderBy, orderType);
		} else {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
		}
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
	}

	class WorkGroupListener implements OnGroupClickListener {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			Intent intent = new Intent();
			if (groupPosition == 0) {
				intent.setClass(getActivity(), MeetingListActivity.class);
				startActivityForResult(intent, 0);
				recordUserClickWorkFunction("meeting");
			} else if (groupPosition == 1) {
				intent.setClass(getActivity(), CalActivity.class);
				startActivity(intent);
				recordUserClickWorkFunction("calendar");
			} else if (groupPosition == 2) {
				intent.setClass(getActivity(), MessionListActivity.class);
				startActivityForResult(intent, 0);
				recordUserClickWorkFunction("todo");
			}
			return false;
		}

	}

	/**
	 * 记录用户点击
	 * @param functionId
	 */
	private void recordUserClickWorkFunction(String functionId){
		PVCollectModel pvCollectModel = new PVCollectModel(functionId,"work");
		PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
	}

	class WorkChildClickListener implements OnChildClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			Intent intent = new Intent();
			if (groupPosition == 1) {
				if (calEventList.size() != 0) {
					intent.putExtra("calEvent",
							(Serializable) calEventList.get(childPosition));
					intent.setClass(getActivity(), CalEventAddActivity.class);
					startActivity(intent);
					recordUserClickWorkFunction("calendar");
				}
			} else if (groupPosition == 2) {
				if (taskList.size() != 0) {
					intent.putExtra("task",
							(Serializable) taskList.get(childPosition));
					intent.setClass(getActivity(), MessionDetailActivity.class);
					startActivity(intent);
					recordUserClickWorkFunction("todo");
				}
			} else if (groupPosition == 0) {
				if (getMeetingResult != null &&getMeetingResult.getMeetingsList().size() > 0) {
						Meeting meeting = getMeetingResult.getMeetingsList().get(childPosition);
						Bundle bundle = new Bundle();
						bundle.putSerializable("meeting", meeting);
						IntentUtils.startActivity(getActivity(),
								MeetingDetailActivity.class, bundle);
				}else {
					IntentUtils.startActivity(getActivity(),
							MeetingBookingActivity.class);
				}
				recordUserClickWorkFunction("meeting");
			}
			return false;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (calEventReceiver != null) {
			getActivity().unregisterReceiver(calEventReceiver);
			calEventReceiver = null;
		}
		if (meetingAndTaskReceiver != null) {
			getActivity().unregisterReceiver(meetingAndTaskReceiver);
			meetingAndTaskReceiver = null;
		}
	}

}
