package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ScheduleMeetingListAdapter;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.ui.schedule.ScheduleBaseFragment;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/6.
 */

@ContentView(R.layout.fragment_schedule_meeting)
public class MeetingFragment extends ScheduleBaseFragment implements SwipeRefreshLayout.OnRefreshListener
        ,ScheduleMeetingListAdapter.OnItemClickLister{
    @ViewInject(R.id.swipe_refresh_layout)
    private MySwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.recycler_view_meeting)
    private RecyclerView meetingRecyclerView;
    private ScheduleMeetingListAdapter scheduleMeetingListAdapter;
    private List<Meeting> meetingList = new ArrayList<>();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(this);
        meetingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        scheduleMeetingListAdapter= new ScheduleMeetingListAdapter(getActivity());
        meetingRecyclerView.setAdapter(scheduleMeetingListAdapter);
        scheduleMeetingListAdapter.setOnItemClickLister(this);
        initData();

    }

    private void initData(){
//        for (int i = 0;i<10;i++){
//            Meeting meeting = new Meeting();
//            meeting.setTitle("2018年财年宣贯会，总结2018年公司业绩");
//            meeting.setId(System.currentTimeMillis()+"");
//            Calendar startCalendar = Calendar.getInstance();
//            startCalendar.set(Calendar.HOUR_OF_DAY,8);
//            startCalendar.set(Calendar.MINUTE,30);
//            meeting.setStartTimeCalendar(startCalendar);
//            Calendar endCalendar = Calendar.getInstance();
//            startCalendar.set(Calendar.HOUR_OF_DAY,10);
//            startCalendar.set(Calendar.MINUTE,00);
//            meeting.setEndTimeCalendar(endCalendar);
//
//            JSONObject locationObj = new JSONObject();
//            try {
//                locationObj.put("displayName","S06栋");
//            }catch (Exception e){
//                e.printStackTrace();
//
//            }
//            Location location = new Location(locationObj);
//            meeting.setScheduleLocationObj(location);
//            meetingList.add(meeting);
//        }
        scheduleMeetingListAdapter.setMeetingList(meetingList);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Event(value = R.id.tv_meeting_search)
    public void onClick(View view) {

    }
}
