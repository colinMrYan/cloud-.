package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleMeetingListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.schedule.ScheduleBaseFragment;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/6.
 */

@ContentView(R.layout.fragment_schedule_meeting)
public class MeetingFragment extends ScheduleBaseFragment implements MySwipeRefreshLayout.OnRefreshListener
        , MySwipeRefreshLayout.OnLoadListener, ScheduleMeetingListAdapter.OnItemClickLister {

    private static String EXTRA_IS_HISTORY_MEETING = "is_history_meeting";

    @ViewInject(R.id.swipe_refresh_layout)
    private MySwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.lv_view_meeting)
    private ListView meetingListView;
    @ViewInject(R.id.ev_search)
    private ClearEditText searchEdit;
    @ViewInject(R.id.rl_meeting_list_default)
    private RelativeLayout meetingListDefaultLayout;
    private ScheduleMeetingListAdapter scheduleMeetingListAdapter;
    private List<Meeting> meetingList = new ArrayList<>();
    private List<Meeting> uiMeetingList = new ArrayList<>();
    private ScheduleApiService apiService;
    private int pageNum = 1;
    private int currentPageSize = 50;
    private boolean isPullUp = false;
    private boolean isHistoryMeeting = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isHistoryMeeting = getArguments().getBoolean(EXTRA_IS_HISTORY_MEETING, false);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnLoadListener(this);
        if (isHistoryMeeting){
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
        meetingListDefaultLayout.setVisibility(uiMeetingList.size()>0?View.GONE:View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED:
                getMeetingList();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Meeting meeting = uiMeetingList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MeetingDetailActivity.EXTRA_MEETING_ENTITY, meeting);
        IntentUtils.startActivity(getActivity(), MeetingDetailActivity.class, bundle);
    }

    @Override
    public void onRefresh() {
        isPullUp = false;
        pageNum = 1;
        getMeetingList();
    }

    @Override
    public void onLoadMore() {
        isPullUp = true;
        getMeetingList();
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
        if (isHistoryMeeting){
            getMeetingHistoryListByPage(pageNum);
        }else{
            getMeetingListByStartTime();
        }
    }

    private void getMeetingListByStartTime() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(),false)) {
            long startTime = TimeUtils.getDayBeginCalendar(Calendar.getInstance()).getTimeInMillis();
            swipeRefreshLayout.setRefreshing(true);
            apiService.getMeetingListByTime(startTime);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getMeetingHistoryListByPage(int page) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(),false)) {
            apiService.getMeetingHistoryListByPage(page);
        } else {
            swipeRefreshLayout.setCanLoadMore(false);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult) {
            searchEdit.setText("");
            meetingList = getMeetingListResult.getMeetingList();
            uiMeetingList.clear();
            uiMeetingList.addAll(meetingList);
            scheduleMeetingListAdapter.setMeetingList(uiMeetingList);
            scheduleMeetingListAdapter.notifyDataSetChanged();
            meetingListDefaultLayout.setVisibility(uiMeetingList.size()>0?View.GONE:View.VISIBLE);
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
            meetingListDefaultLayout.setVisibility(uiMeetingList.size()>0?View.GONE:View.VISIBLE);
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
