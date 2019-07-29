package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleMeetingListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.schedule.calendar.AccountType;
import com.inspur.emmcloud.bean.schedule.calendar.ScheduleCalendar;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.util.privates.cache.ScheduleCalendarCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/6.
 */

public class MeetingFragment extends BaseFragment implements MySwipeRefreshLayout.OnRefreshListener
        , MySwipeRefreshLayout.OnLoadListener, ScheduleMeetingListAdapter.OnItemClickLister {

    private MySwipeRefreshLayout swipeRefreshLayout;
    private ListView meetingListView;
    private ClearEditText searchEdit;
    private LinearLayout meetingListDefaultLayout;
    private ScheduleMeetingListAdapter scheduleMeetingListAdapter;
    private List<Meeting> meetingList = new ArrayList<>();
    private List<Meeting> uiMeetingList = new ArrayList<>();
    private ScheduleApiService apiService;
    private int pageNum = 1;
    private int currentPageSize = 50;
    private boolean isPullUp = false;
    private boolean isHistoryMeeting = false;
    private View rootView;
    private boolean isRefresh = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_schedule_meeting, null);
        if (getArguments() != null) {
            isHistoryMeeting = getArguments().getBoolean(Constant.EXTRA_IS_HISTORY_MEETING, false);
        }
        EventBus.getDefault().register(this);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        meetingListView = rootView.findViewById(R.id.lv_view_meeting);
        searchEdit = rootView.findViewById(R.id.ev_search);
        meetingListDefaultLayout = rootView.findViewById(R.id.rl_meeting_list_default);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnLoadListener(this);
        if (isHistoryMeeting) {
            swipeRefreshLayout.setCanLoadMore(true);
        }
        scheduleMeetingListAdapter = new ScheduleMeetingListAdapter(getActivity());
        meetingListView.setAdapter(scheduleMeetingListAdapter);
        scheduleMeetingListAdapter.setOnItemClickLister(this);
        apiService = new ScheduleApiService(getActivity());
        apiService.setAPIInterface(new WebService());
        getMeetingList();
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchMeeting(s);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater
                    .inflate(R.layout.fragment_schedule_meeting, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void searchMeeting(Editable s) {
        String searchContent = s.toString();
        uiMeetingList.clear();
        for (int i = 0; i < meetingList.size(); i++) {
            if (meetingList.get(i).getTitle().contains(searchContent)) {
                uiMeetingList.add(meetingList.get(i));
            }
        }
        scheduleMeetingListAdapter.setMeetingList(uiMeetingList);
        scheduleMeetingListAdapter.notifyDataSetChanged();
        meetingListDefaultLayout.setVisibility(uiMeetingList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED:
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED:
                getMeetingList();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Meeting meeting = uiMeetingList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ScheduleDetailActivity.EXTRA_SCHEDULE_ENTITY, meeting);
        bundle.putBoolean(Constant.EXTRA_IS_HISTORY_MEETING, isHistoryMeeting);
        IntentUtils.startActivity(getActivity(), ScheduleDetailActivity.class, bundle);
    }

    @Override
    public void onRefresh() {
        isPullUp = false;
        pageNum = 1;
        getMeetingList();
    }

    @Override
    public void onLoadMore() {
        if (uiMeetingList.size() > 0) {
            isPullUp = true;
            getMeetingList();
        } else {
            swipeRefreshLayout.setLoading(false);
        }
    }

    @Event(value = R.id.rl_meeting_search)
    public void onClick(View view) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void getMeetingList() {
        if (isHistoryMeeting) {
            getMeetingHistoryListByPage(pageNum);
        } else {
            getMeetingListByStartTime();
        }
    }

    private void getMeetingListByStartTime() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            isRefresh = true;
            long startTime = TimeUtils.getDayBeginCalendar(Calendar.getInstance()).getTimeInMillis();
            swipeRefreshLayout.setRefreshing(true);
            List<ScheduleCalendar> scheduleCalendarList = ScheduleCalendarCacheUtils.getScheduleCalendarList(BaseApplication.getInstance());
//            ScheduleCalendar appScheduleCalendar = null;
//            for (ScheduleCalendar scheduleCalendar : scheduleCalendarList) {
//                if (scheduleCalendar.getAcType().equals(AccountType.APP_MEETING.toString()) || scheduleCalendar.getAcType().equals(AccountType.APP_SCHEDULE.toString())) {
//                    appScheduleCalendar = scheduleCalendar;
//                    continue;
//                }
//                apiService.getMeetingListByTime(startTime, scheduleCalendar);
//            }
//            apiService.getMeetingListByTime(startTime, appScheduleCalendar);

            ScheduleCalendar scheduleCalendar = null;
            for (int i = 0; i < scheduleCalendarList.size(); i++) {
                if (scheduleCalendarList.get(i).getAcType().equals(AccountType.EXCHANGE.toString())) {
                    scheduleCalendar = scheduleCalendarList.get(i);
                    break;
                }
            }
            apiService.getMeetingListByTime(startTime, scheduleCalendar);

        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getMeetingHistoryListByPage(int page) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            apiService.getMeetingHistoryListByPage(page);
        } else {
            swipeRefreshLayout.setCanLoadMore(false);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult) {
            searchEdit.setText("");
            meetingList = getMeetingListResult.getMeetingList();
            if (isRefresh) {
                isRefresh = false;
                uiMeetingList.clear();
            }
            uiMeetingList.removeAll(meetingList);
            uiMeetingList.addAll(meetingList);
            scheduleMeetingListAdapter.setMeetingList(uiMeetingList);
            scheduleMeetingListAdapter.notifyDataSetChanged();
            meetingListDefaultLayout.setVisibility(uiMeetingList.size() > 0 ? View.GONE : View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setCanLoadMore(false);
            swipeRefreshLayout.setLoading(false);
        }

        @Override
        public void returnMeetingListByMeetingRoomFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setLoading(false);
            swipeRefreshLayout.setCanLoadMore(false);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnMeetingHistoryListSuccess(GetMeetingListResult getMeetingListByPage) {
            List<Meeting> meetingHistoryList = getMeetingListByPage.getMeetingList();
            currentPageSize = meetingHistoryList.size();
            swipeRefreshLayout.setLoading(false);
            swipeRefreshLayout.setRefreshing(false);
            if (!isPullUp) {
                meetingList.clear();
                uiMeetingList.clear();
                swipeRefreshLayout.setRefreshing(false);
            }
            swipeRefreshLayout.setCanLoadMore(currentPageSize > 49);
            meetingList.addAll(meetingHistoryList);
            uiMeetingList.addAll(meetingHistoryList);
            scheduleMeetingListAdapter.setMeetingList(uiMeetingList);
            scheduleMeetingListAdapter.notifyDataSetChanged();
            meetingListDefaultLayout.setVisibility(uiMeetingList.size() > 0 ? View.GONE : View.VISIBLE);
            pageNum++;
        }

        @Override
        public void returnMeetingHistoryListFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setLoading(false);
            swipeRefreshLayout.setCanLoadMore(currentPageSize > 49);
            super.returnMeetingHistoryListFail(error, errorCode);
        }
    }
}
