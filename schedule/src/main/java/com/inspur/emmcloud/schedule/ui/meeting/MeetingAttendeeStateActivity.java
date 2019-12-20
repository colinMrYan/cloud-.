package com.inspur.emmcloud.schedule.ui.meeting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.MeetingAttendees;
import com.inspur.emmcloud.schedule.bean.Participant;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/7/5.
 */

public class MeetingAttendeeStateActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ExpandableListView.OnChildClickListener {

    @BindView(R2.id.expandable_list_view)
    ExpandableListView expandableListView;
    @BindView(R2.id.swipe_refresh_layout)
    MySwipeRefreshLayout swipeRefreshLayout;
    MeetingAttendeeStateAdapter meetingAttendeeStateAdapter;
    Schedule schedule;
    private List<MeetingAttendees> meetingAttendeesList = new ArrayList<>();
    private List<Participant> recordParticipants = new ArrayList<>();
    private List<Participant> contactParticipants = new ArrayList<>();
    private ScheduleAPIService apiService;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        schedule = (Schedule) getIntent().getSerializableExtra(ScheduleDetailActivity.EXTRA_SCHEDULE_ENTITY); //来自列表
        init();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_attendee_state;
    }

    private void getMeetingData() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            if (!(schedule.getType().equals(Schedule.CALENDAR_TYPE_EXCHANGE) && schedule.getScheduleCalendar().equals(AccountType.APP_SCHEDULE.toString()))) {
                apiService.getMeetingDataFromId(schedule.getId(), ScheduleCalendarCacheUtils.getScheduleCalendar(this, schedule.getScheduleCalendar()));
                return;
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void init() {
        swipeRefreshLayout.setOnRefreshListener(this);
        expandableListView.setGroupIndicator(null);
        expandableListView.setVerticalScrollBarEnabled(false);
        expandableListView.setHeaderDividersEnabled(false);
        expandableListView.setOnChildClickListener(this);
        meetingAttendeeStateAdapter = new MeetingAttendeeStateAdapter(this);
        expandableListView.setAdapter(meetingAttendeeStateAdapter);
        recordParticipants = schedule.getRecorderParticipantList();
        contactParticipants = schedule.getRoleParticipantList();
        divideAttendeeGroup();
        apiService = new ScheduleAPIService(this);
        apiService.setAPIInterface(new WebService());
        getMeetingData();
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();
        }
    }

    //数据分组
    private void divideAttendeeGroup() {
        meetingAttendeesList.clear();
        MeetingAttendees meetingInvite = new MeetingAttendees(MeetingAttendees.MEETING_ATTENDEES_INVITE);
        MeetingAttendees meetingAcceptAttendees = new MeetingAttendees(Participant.CALENDAR_RESPONSE_TYPE_ACCEPT);
        MeetingAttendees meetingDenyAttendees = new MeetingAttendees(Participant.CALENDAR_RESPONSE_TYPE_DECLINE);
        MeetingAttendees meetingNoActionAttendees = new MeetingAttendees(Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN);
        MeetingAttendees meetingTentativeAttendees = new MeetingAttendees(Participant.CALENDAR_RESPONSE_TYPE_TENTATIVE);

        //考虑Owner ID无法查询的情况
        Participant participant = new Participant();
        if (!StringUtils.isBlank(schedule.getOwner())) {

            ContactService contactService = Router.getInstance().getService(ContactService.class);
            ContactUser contactUser;
            if (contactService != null) {
                contactUser = contactService.getContactUserByUid(schedule.getOwner());
            } else {
                contactUser = null;
            }
            if (contactUser != null) {
                participant.setId(contactUser.getId());
                participant.setName(contactUser.getName());
                participant.setEmail(contactUser.getEmail());
                participant.setRole(Participant.TYPE_INVITE);
            } else {
                participant.setId("");
                participant.setName(schedule.getOwner());
                participant.setEmail(schedule.getOwner());
                participant.setRole(Participant.TYPE_INVITE);
            }
            meetingInvite.getMeetingAttendeesList().add(participant);
        }
        //其他人员放在无响应里去重+转化
        List<Participant> participantList = schedule.getAllParticipantList();
        for (int i = 0; i < participantList.size(); i++) {
            Participant currentParticipant = participantList.get(i);
            for (int j = i + 1; j < participantList.size(); j++) {
                if (currentParticipant.getId().equals(participantList.get(j).getId())) {
                    participantList.remove(j);
                    j--;
                }
            }
        }
        //清除Owner, 并组装
        for (int m = 0; m < participantList.size(); m++) {
            if (participantList.get(m).getId().equals(schedule.getOwner())) {
                participantList.remove(m);
                m = m - 1;
            } else {
                if (participantList.get(m).getResponseType().equals(Participant.CALENDAR_RESPONSE_TYPE_ACCEPT)) {
                    meetingAcceptAttendees.getMeetingAttendeesList().add(participantList.get(m));
                } else if (participantList.get(m).getResponseType().equals(Participant.CALENDAR_RESPONSE_TYPE_DECLINE)) {
                    meetingDenyAttendees.getMeetingAttendeesList().add(participantList.get(m));
                } else if (participantList.get(m).getResponseType().equals(Participant.CALENDAR_RESPONSE_TYPE_TENTATIVE)) {
                    meetingTentativeAttendees.getMeetingAttendeesList().add(participantList.get(m));
                } else {
                    meetingNoActionAttendees.getMeetingAttendeesList().add(participantList.get(m));
                }
            }
        }
        //根据当前的个数将相应的组添加到Adapter用的数组内
        if (meetingInvite.getMeetingAttendeesList().size() > 0) {
            meetingAttendeesList.add(meetingInvite);
        }
        if (meetingAcceptAttendees.getMeetingAttendeesList().size() > 0) {
            meetingAttendeesList.add(meetingAcceptAttendees);
        }
        if (meetingDenyAttendees.getMeetingAttendeesList().size() > 0) {
            meetingAttendeesList.add(meetingDenyAttendees);
        }
        if (meetingTentativeAttendees.getMeetingAttendeesList().size() > 0) {
            meetingAttendeesList.add(meetingTentativeAttendees);
        }
        if (meetingNoActionAttendees.getMeetingAttendeesList().size() > 0) {
            meetingAttendeesList.add(meetingNoActionAttendees);
        }
        meetingAttendeeStateAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        getMeetingData();
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        String uid = meetingAttendeesList.get(i).getMeetingAttendeesList().get(i1).getId();
        if (!StringUtils.isBlank(uid)) {
            Bundle bundle = new Bundle();
            bundle.putString("uid", uid);
            ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_USERINFO).with(bundle).navigation(MeetingAttendeeStateActivity.this);
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class MeetingAttendeeStateAdapter extends BaseExpandableListAdapter {

        private Context context;

        public MeetingAttendeeStateAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getGroupCount() {
            return meetingAttendeesList.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return meetingAttendeesList.get(i).getMeetingAttendeesList().size();
        }

        @Override
        public Object getGroup(int i) {
            return meetingAttendeesList.get(i);
        }

        @Override
        public Object getChild(int i, int i1) {
            return meetingAttendeesList.get(i).getMeetingAttendeesList().get(i1);
        }

        @Override
        public long getGroupId(int i) {
            return 0;
        }

        @Override
        public long getChildId(int i, int i1) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            ExpandableListView expandableListView = (ExpandableListView) viewGroup;
            expandableListView.expandGroup(i);
            TextView textView = new TextView(context);
            int paddingLeft = DensityUtil.dip2px(context, 16);
            int paddingTop = paddingLeft / 2;
            textView.setPadding(paddingLeft, paddingTop, 0, paddingTop);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            textView.setTextColor(Color.parseColor("#999999"));
            textView.setText(meetingAttendeesList.get(i).getName());
            return textView;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            Participant participant = meetingAttendeesList.get(i).getMeetingAttendeesList().get(i1);
            view = LayoutInflater.from(context).inflate(R.layout.meeting_attendees_expandale_child_item, null);
            TextView attendeeNameText = view.findViewById(R.id.tv_attendee_name);
            ImageView attendeeHeadImage = view.findViewById(R.id.iv_attendee_head);
            TextView attendeeType = view.findViewById(R.id.tv_attendee_type);
            View dividerView = view.findViewById(R.id.view_divider);
            dividerView.setVisibility(View.VISIBLE);
            attendeeNameText.setText(participant.getName());
            final String uid = participant.getId();
            String photoUrl = BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), uid);
            ImageDisplayUtils.getInstance().displayRoundedImage(attendeeHeadImage, photoUrl, R.drawable.icon_person_default, context, 15);
            for (int num = 0; num < recordParticipants.size(); num++) {
                if (participant.getId().equals(recordParticipants.get(num).getId()) && participant.getName().equals(recordParticipants.get(num).getName())) {
                    attendeeType.setText(R.string.schedule_meeting_detail_record_title);
                    attendeeType.setVisibility(View.VISIBLE);
                }
            }
            for (int num = 0; num < contactParticipants.size(); num++) {
                if (participant.getId().equals(contactParticipants.get(num).getId()) && participant.getName().equals(contactParticipants.get(num).getName())) {
                    attendeeType.setText(attendeeType.getText() + " " + BaseApplication.getInstance().getString(R.string.schedule_meeting_detail_conference_title));
                    attendeeType.setVisibility(View.VISIBLE);
                }
            }
            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
    }


    //更新个人信息状态
    class WebService extends ScheduleAPIInterfaceImpl {
        @Override
        public void returnMeetingDataFromIdSuccess(Schedule newMeeting) {
            swipeRefreshLayout.setRefreshing(false);
            String scheduleCalendarStr = schedule.getScheduleCalendar();
            schedule = newMeeting;
            schedule.setScheduleCalendar(scheduleCalendarStr);
            divideAttendeeGroup();
        }

        @Override
        public void returnMeetingDataFromIdFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

}
