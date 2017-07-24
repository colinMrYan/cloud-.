package com.inspur.emmcloud.ui.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.CalendarEvent;
import com.inspur.emmcloud.bean.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.GetMeetingsResult;
import com.inspur.emmcloud.bean.GetMyCalendarResult;
import com.inspur.emmcloud.bean.GetTaskListResult;
import com.inspur.emmcloud.bean.Meeting;
import com.inspur.emmcloud.bean.MyCalendar;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.CalEventNotificationUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.WorkColorUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 工作页面
 *
 * @author Administrator
 */
public class WorkFragment extends Fragment implements OnRefreshListener {

    private static final int TYPE_CALENDAR = 0;
    private static final int TYPE_APPROVAL = 1;
    private static final int TYPE_MEETING = 2;
    private static final int TYPE_TASK = 3;
    private View rootView;
    private WorkAPIService apiService;
    private LoadingDialog loadingDialog;
    private PullableListView listView;
    private BaseAdapter adapter;
    private PullToRefreshLayout pullToRefreshLayout;
    private List<Meeting> meetingList = new ArrayList<>();
    private ArrayList<TaskResult> taskList = new ArrayList<>();
    private List<CalendarEvent> calEventList = new ArrayList<>();
    private BroadcastReceiver calEventReceiver;
    private BroadcastReceiver meetingAndTaskReceiver;
    private List<String> calendarIdList = new ArrayList<>();
    private ChildAdapter calendarChildAdapter,meetingChildAdapter,taskChildAdapter;

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
        initViews();
        getData();
        registerCalEventReceiver();
        registerMeetingAndTaskReceiver();
    }

    /**
     * 初始化views
     */
    private void initViews() {
        listView = (PullableListView) rootView
                .findViewById(R.id.list);
        listView.setCanPullDown(false);
        pullToRefreshLayout = (PullToRefreshLayout) rootView
                .findViewById(R.id.refresh_view);
        pullToRefreshLayout.setOnRefreshListener(WorkFragment.this);
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        loadingDialog = new LoadingDialog(getActivity());
        adapter = new Adapter();
        listView.setAdapter(adapter);
    }

    /**
     * 获取数据
     */
    private void getData() {
        getMeetings();
        getCalendarEvent();
        getTasks();
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
                    calendarChildAdapter.notifyDataSetChanged();
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
                    getTasks();
                } else if (intent.hasExtra("refreshMeeting")) {
                    getMeetings();
                }
            }
        };
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("com.inspur.meeting");
        myIntentFilter.addAction("com.inspur.task");
        getActivity().registerReceiver(meetingAndTaskReceiver, myIntentFilter);
    }

    static class ViewHolder {
        ImageView groupIconImg;
        TextView groupTitleText;
        RelativeLayout groupHeaderlayout;
        ScrollViewWithListView GroupListView;
    }
    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (position<getCount()-1){
                if (convertView == null){
                    holder = new ViewHolder();
                    convertView = LayoutInflater.from(getActivity()).inflate(R.layout.work_card_group_item_view_vertical, null);
                    holder.groupIconImg = (ImageView) convertView.findViewById(R.id.group_icon_img);
                    holder.groupTitleText = (TextView) convertView.findViewById(R.id.group_title_text);
                    holder.groupHeaderlayout = (RelativeLayout) convertView.findViewById(R.id.group_header_layout);
                    holder.GroupListView = (ScrollViewWithListView) convertView.findViewById(R.id.list);
                    convertView.setTag(holder);
                }else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.groupHeaderlayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        if (position == 0) {
                            intent.setClass(getActivity(), CalActivity.class);
                            startActivityForResult(intent, 0);
                        } else if (position == 1) {
                            intent.setClass(getActivity(), MeetingListActivity.class);
                            startActivity(intent);
                        } else if (position == 2) {
                            intent.setClass(getActivity(), MessionListActivity.class);
                            startActivityForResult(intent, 0);
                        }
                    }
                });
                if (position == 0) {
                    holder.groupIconImg.setImageResource(R.drawable.ic_work_calendar);
                    holder.groupTitleText.setText(R.string.work_calendar_text);
                    calendarChildAdapter = new ChildAdapter(TYPE_CALENDAR);
                    holder.GroupListView.setAdapter(calendarChildAdapter);
                } else if (position == 1) {
                    holder.groupIconImg.setImageResource(R.drawable.ic_work_meeting);
                    holder.groupTitleText.setText(R.string.work_meeting_text);
                    meetingChildAdapter = new ChildAdapter(TYPE_MEETING);
                    holder.GroupListView.setAdapter(meetingChildAdapter);

                } else {
                    holder.groupIconImg.setImageResource(R.drawable.ic_work_task);
                    holder.groupTitleText.setText(R.string.work_task_text);
                    taskChildAdapter = new ChildAdapter(TYPE_TASK);
                    holder.GroupListView.setAdapter(taskChildAdapter);
                }
                return convertView;
            }else {
                View view = new View(getActivity());
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getActivity(),50)));
                return view;
            }
        }
    }

    private class ChildAdapter extends BaseAdapter {
        private int type;

        public ChildAdapter(int type) {
            this.type = type;
        }

        @Override
        public int getCount() {
            if (type == TYPE_CALENDAR) {
                return calEventList.size();
            }
            if (type == TYPE_MEETING) {
                return meetingList.size();
            }
            LogUtils.jasonDebug("taskList.size()="+taskList.size());
            return taskList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.work_card_child_item_view_vertical, null);
            TextView countDownText = (TextView) convertView.findViewById(R.id.count_down_text);
            TextView dateText = (TextView) convertView.findViewById(R.id.date_text);
            String countDown = "";
            String content = "";
            String date = "";
            switch (type) {
                case TYPE_MEETING:
                    Meeting meeting =  meetingList.get(position);
                    content =meeting.getTopic();
                    countDown = TimeUtils.getCountdown(getActivity(),meeting.getFrom());
                    WorkColorUtils.showDayOfWeek( countDownText,
                            TimeUtils
                                    .getCountdownNum(meeting.getFrom()));
                    break;
                case TYPE_TASK:
                    LogUtils.jasonDebug("task-------------------------");
                    TaskResult task = taskList.get(position);
                    content = task.getTitle();
                    ViewGroup.LayoutParams param = countDownText.getLayoutParams();
                    param.height = DensityUtil.dip2px(getActivity(),8);
                    param.width = param.height;
                    countDownText.setLayoutParams(param);
                    WorkColorUtils.showDayOfWeek( countDownText,
                            TimeUtils
                                    .getCountdownNum(task.getCreationDate()));
                    break;
                case TYPE_CALENDAR:
                    CalendarEvent calendarEvent = calEventList.get(position);
                    content = calendarEvent.getTitle();
                    countDown = TimeUtils.getCountdown(getActivity(),calendarEvent.getLocalStartDate());
                    WorkColorUtils.showDayOfWeek( countDownText,
                            TimeUtils
                                    .getCountdownNum(calendarEvent.getLocalStartDate()));
                    break;
                default:
                    break;
            }
            ((TextView) convertView.findViewById(R.id.content_text)).setText(content);
            countDownText.setText(countDown);
            return convertView;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (NetUtils.isNetworkConnected(getActivity())) {
                getMeetings();
                getTasks();
            }

        }
    }


    /**
     * 获取日历中Event
     */
    private void getCalendarEvent() {
        if (NetUtils.isNetworkConnected(getActivity())) {
            apiService.getMyCalendar(0, 30);
        }
    }


    /**
     * 获取任务
     */
    private void getTasks() {
        if (NetUtils.isNetworkConnected(getActivity())) {
            String orderBy = PreferencesUtils.getString(getActivity(),
                    "order_by", "PRIORITY");
            String orderType = PreferencesUtils.getString(getActivity(),
                    "order_type", "ASC");
            apiService.getRecentTasks(orderBy, orderType);
        }
    }


    /**
     * 获取会议
     */
    private void getMeetings() {
        if (NetUtils.isNetworkConnected(getActivity())) {
            apiService.getMeetings(7);
        }
    }

    /**
     * 获取三条Event
     */
    private void getCalEventsFor3() {
        if (NetUtils.isNetworkConnected(getActivity()) && calendarIdList.size() > 0) {
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
     * 获取今明两天所有日历的所有event
     */
    private void getCalEventsForTwoDays() {
        if (NetUtils.isNetworkConnected(getActivity()) && calendarIdList.size() > 0) {
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


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingsSuccess(GetMeetingsResult getMeetingsResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            WorkFragment.this.meetingList = getMeetingsResult.getMeetingsList();
            Collections.sort(WorkFragment.this.meetingList, new Meeting());
            meetingChildAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnMeetingsFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            taskList = getTaskListResult.getTaskList();
                taskChildAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnMyCalendarSuccess(
                GetMyCalendarResult getMyCalendarResult) {
            List<MyCalendar> calendarList = getMyCalendarResult
                    .getCalendarList();
            MyCalendarCacheUtils
                    .saveMyCalendarList(getActivity(), calendarList);
            for (int i = 0; i < calendarList.size(); i++) {
                MyCalendar myCalendar = calendarList.get(i);
                if (!myCalendar.getState().equals("REMOVED")
                        && !MyCalendarOperationCacheUtils.getIsHide(
                        getActivity(), myCalendar.getId())) {
                    calendarIdList.add(calendarList.get(i).getId());
                }
                getCalEventsForTwoDays();
            }
        }

        @Override
        public void returnMyCalendarFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnCalEventsSuccess(
                GetCalendarEventsResult getCalendarEventsResult,
                boolean isRefresh) {
            calEventList = getCalendarEventsResult.getCalEventList();
            CalEventNotificationUtils.setCalEventNotification(getActivity().getApplicationContext(), calEventList);
            if (isRefresh && (calEventList.size() < 3)) { // 获取今明两天的日历不足3条
                getCalEventsFor3();
            } else {
                calendarChildAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void returnCalEventsFail(String error, boolean isRefresh, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            getData();
        } else {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
        }
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
    }


//	/**
//	 * 初始化节日
//	 * @return
//	 */
//	private FestivalDate initFestivalDate(){
//		FestivalDate festivalDate = null;
//		try {
//			if (!DbCacheUtils.getDb(getActivity()).tableIsExist(FestivalDate.class)) {
//				FestivalCacheUtils.saveFestivalList(getActivity());
//			}
//			festivalDate = FestivalCacheUtils.getFestival(getActivity());
//		} catch (DbException e) {
//			e.printStackTrace();
//		}
//		return festivalDate;
//	}

//	/**
//	 * 记录用户点击
//	 * @param functionId
//	 */
//	private void recordUserClickWorkFunction(String functionId){
//		PVCollectModel pvCollectModel = new PVCollectModel(functionId,"work");
//		PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
//	}

//	class WorkChildClickListener implements OnChildClickListener {
//		@Override
//		public boolean onChildClick(ExpandableListView parent, View v,
//				int groupPosition, int childPosition, long id) {
//			Intent intent = new Intent();
//			if (groupPosition == 1) {
//				if (calEventList.size() != 0) {
//					intent.putExtra("calEvent",
//							(Serializable) calEventList.get(childPosition));
//					intent.setClass(getActivity(), CalEventAddActivity.class);
//					startActivity(intent);
//					recordUserClickWorkFunction("calendar");
//				}
//			} else if (groupPosition == 2) {
//				if (taskList.size() != 0) {
//					intent.putExtra("task",
//							(Serializable) taskList.get(childPosition));
//					intent.setClass(getActivity(), MessionDetailActivity.class);
//					startActivity(intent);
//					recordUserClickWorkFunction("todo");
//				}
//			} else if (groupPosition == 0) {
//				if (getMeetingResult != null &&getMeetingResult.getMeetingsList().size() > 0) {
//						Meeting meeting = getMeetingResult.getMeetingsList().get(childPosition);
//						Bundle bundle = new Bundle();
//						bundle.putSerializable("meeting", meeting);
//						IntentUtils.startActivity(getActivity(),
//								MeetingDetailActivity.class, bundle);
//				}else {
//					IntentUtils.startActivity(getActivity(),
//							MeetingBookingActivity.class);
//				}
//				recordUserClickWorkFunction("meeting");
//			}
//			return false;
//		}
//	}


    @Override
    public void onPause() {
        super.onPause();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
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
