package com.inspur.emmcloud.ui.schedule.meeting;

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

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.schedule.MeetingAttendees;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/7/5.
 */

public class MeetingAttendeeStateActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ExpandableListView.OnChildClickListener {

    @BindView(R.id.expandable_list_view)
    ExpandableListView expandableListView;
    @BindView(R.id.swipe_refresh_layout)
    MySwipeRefreshLayout swipeRefreshLayout;
    MeetingAttendeeStateAdapter meetingAttendeeStateAdapter;
    Meeting meeting;
    private List<MeetingAttendees> meetingAttendeesList = new ArrayList<>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        meeting = (Meeting) getIntent().getSerializableExtra(MeetingDetailActivity.EXTRA_MEETING_ENTITY); //来自列表
        initData();
        initView();

    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_attendee_state;
    }

    private void initView() {
        expandableListView.setGroupIndicator(null);
        expandableListView.setVerticalScrollBarEnabled(false);
        expandableListView.setHeaderDividersEnabled(false);
        expandableListView.setOnChildClickListener(this);
        meetingAttendeeStateAdapter = new MeetingAttendeeStateAdapter(this);
        expandableListView.setAdapter(meetingAttendeeStateAdapter);
    }

    private void initData() {
        divideAttendeeGroup();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    private void divideAttendeeGroup() {
        meetingAttendeesList.clear();
        MeetingAttendees meetingInvite = new MeetingAttendees(MeetingAttendees.MEETING_ATTENDEES_INVITE);
        MeetingAttendees meetingAcceptAttendees = new MeetingAttendees(Participant.CALENDAR_RESPONSE_TYPE_ACCEPT);
        MeetingAttendees meetingDenyAttendees = new MeetingAttendees(Participant.CALENDAR_RESPONSE_TYPE_DECLINE);
        MeetingAttendees meetingNoActionAttendees = new MeetingAttendees(Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN);

        //考虑Owner ID无法查询的情况
        Participant participant = new Participant();
        if (!StringUtils.isBlank(meeting.getOwner())) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(meeting.getOwner());
            if (contactUser != null) {
                participant.setId(contactUser.getId());
                participant.setName(contactUser.getName());
                participant.setEmail(contactUser.getEmail());
            } else {
                participant.setId("");
                participant.setName(meeting.getOwner());
                participant.setEmail(meeting.getOwner());
            }
            meetingInvite.getMeetingAttendeesList().add(participant);
        }
        //其他人员放在无响应里去重+转化
        List<Participant> participantList = meeting.getAllParticipantList();
        //费时算法
        for (int i = 0; i < participantList.size(); i++) {
            Participant currentParticipant = participantList.get(i);
            for (int j = i + 1; j < participantList.size(); j++) {
                if (currentParticipant.getId().equals(participantList.get(j).getId())) {
                    participantList.remove(j);
                }
            }
        }
        //清除Owner, 并组装
        for (int m = 0; m < participantList.size(); m++) {
            if (participantList.get(m).getId().equals(meeting.getOwner())) {
                participantList.remove(m);
                m = m - 1;
            } else {
                if (participantList.get(m).getResponseType().equals(Participant.CALENDAR_RESPONSE_TYPE_ACCEPT)) {
                    meetingAcceptAttendees.getMeetingAttendeesList().add(participantList.get(m));
                } else if (participantList.get(m).getResponseType().equals(Participant.CALENDAR_RESPONSE_TYPE_DECLINE)) {
                    meetingDenyAttendees.getMeetingAttendeesList().add(participantList.get(m));
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
        if (meetingNoActionAttendees.getMeetingAttendeesList().size() > 0) {
            meetingAttendeesList.add(meetingNoActionAttendees);
        }
    }

    @Override
    public void onRefresh() {
        meetingAttendeeStateAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        String uid = meetingAttendeesList.get(i).getMeetingAttendeesList().get(i1).getId();
        if (!StringUtils.isBlank(uid)) {
            Bundle bundle = new Bundle();
            bundle.putString("uid", uid);
            IntentUtils.startActivity(MeetingAttendeeStateActivity.this, UserInfoActivity.class, bundle);
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
            View dividerView = view.findViewById(R.id.view_divider);
            dividerView.setVisibility(View.VISIBLE);
            attendeeNameText.setText(participant.getName());
            final String uid = participant.getId();
            String photoUrl = APIUri.getChannelImgUrl(MyApplication.getInstance(), uid);
            ImageDisplayUtils.getInstance().displayRoundedImage(attendeeHeadImage, photoUrl, R.drawable.icon_person_default, context, 15);
            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
    }


    //更新个人信息状态
    class WebService extends APIInterfaceInstance {

    }

}
