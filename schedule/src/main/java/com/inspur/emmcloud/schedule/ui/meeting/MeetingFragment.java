package com.inspur.emmcloud.schedule.ui.meeting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

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
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.adapter.ScheduleMeetingListAdapter;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.bean.meeting.GetMeetingListResult;
import com.inspur.emmcloud.schedule.bean.meeting.Meeting;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/4/6.
 */

public class MeetingFragment extends BaseFragment implements MySwipeRefreshLayout.OnRefreshListener
        , MySwipeRefreshLayout.OnLoadListener, ScheduleMeetingListAdapter.OnItemClickLister {
    @BindView(R2.id.swipe_refresh_layout)
    MySwipeRefreshLayout swipeRefreshLayout;
    @BindView(R2.id.lv_view_meeting)
    ListView meetingListView;
    @BindView(R2.id.ev_search)
    ClearEditText searchEdit;
    @BindView(R2.id.rl_meeting_list_default)
    LinearLayout meetingListDefaultLayout;
    private ScheduleMeetingListAdapter scheduleMeetingListAdapter;
    private List<Meeting> meetingList = new ArrayList<>();
    private List<Meeting> uiMeetingList = new ArrayList<>();
    private ScheduleAPIService apiService;
    private int pageNum = 1;
    private int currentPageSize = 50;
    private boolean isPullUp = false;
    private boolean isHistoryMeeting = false;
    private boolean isRefresh = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            isHistoryMeeting = getArguments().getBoolean(Constant.EXTRA_IS_HISTORY_MEETING, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule_fragment_meeting, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnLoadListener(this);
        if (isHistoryMeeting) {
            swipeRefreshLayout.setCanLoadMore(true);
        }
        scheduleMeetingListAdapter = new ScheduleMeetingListAdapter(getActivity());
        meetingListView.setAdapter(scheduleMeetingListAdapter);
        scheduleMeetingListAdapter.setOnItemClickLister(this);
        apiService = new ScheduleAPIService(getActivity());
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
            case Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_SETTING_CHANGED:
                getMeetingList();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Meeting meeting = uiMeetingList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ScheduleDetailActivity.EXTRA_SCHEDULE_ENTITY, meeting);
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
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            isRefresh = true;
            long startTime = TimeUtils.getDayBeginCalendar(Calendar.getInstance()).getTimeInMillis();
            swipeRefreshLayout.setRefreshing(true);
            List<ScheduleCalendar> scheduleCalendarList = ScheduleCalendarCacheUtils.getScheduleCalendarList(BaseApplication.getInstance());
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
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            apiService.getMeetingHistoryListByPage(page);
        } else {
            swipeRefreshLayout.setCanLoadMore(false);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class WebService extends ScheduleAPIInterfaceImpl {
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
        }

        @Override
        public void returnMeetingListFail(String error, int errorCode) {

            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void returnMeetingListByMeetingRoomFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setLoading(false);
            swipeRefreshLayout.setCanLoadMore(false);
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
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
