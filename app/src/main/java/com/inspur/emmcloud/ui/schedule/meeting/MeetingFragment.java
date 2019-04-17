package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
public class MeetingFragment extends ScheduleBaseFragment implements SwipeRefreshLayout.OnRefreshListener
        , ScheduleMeetingListAdapter.OnItemClickLister {
    @ViewInject(R.id.swipe_refresh_layout)
    private MySwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.recycler_view_meeting)
    private RecyclerView meetingRecyclerView;
    private ScheduleMeetingListAdapter scheduleMeetingListAdapter;
    private List<Meeting> meetingList = new ArrayList<>();
    private ScheduleApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(this);
        meetingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scheduleMeetingListAdapter = new ScheduleMeetingListAdapter(getActivity());
        meetingRecyclerView.setAdapter(scheduleMeetingListAdapter);
        scheduleMeetingListAdapter.setOnItemClickLister(this);
        apiService = new ScheduleApiService(getActivity());
        apiService.setAPIInterface(new WebService());
        getMeetingListByStartTime();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED:
                getMeetingListByStartTime();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Meeting meeting = meetingList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MeetingDetailActivity.EXTRA_MEETING_ENTITY, meeting);
        IntentUtils.startActivity(getActivity(), MeetingDetailActivity.class, bundle);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Event(value = R.id.tv_meeting_search)
    public void onClick(View view) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    private void getMeetingListByStartTime() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            long startTime = TimeUtils.getDayBeginCalendar(Calendar.getInstance()).getTimeInMillis();
            swipeRefreshLayout.setRefreshing(true);
            apiService.getMeetingListByTime(startTime);
        }
    }

    private class WebService extends APIInterfaceInstance {


        @Override
        public void returnMeetingListSuccess(GetMeetingListResult getMeetingListResult) {
            meetingList = getMeetingListResult.getMeetingList();
            scheduleMeetingListAdapter.setMeetingList(meetingList);
            scheduleMeetingListAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void returnMeetingListByMeetingRoomFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

    }
}
