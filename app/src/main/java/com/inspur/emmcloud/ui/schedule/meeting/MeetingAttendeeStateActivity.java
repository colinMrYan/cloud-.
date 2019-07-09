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
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.schedule.MeetingAttendees;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/7/5.
 */

public class MeetingAttendeeStateActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ExpandableListView.OnChildClickListener {

    private static final int REQUEST_SELECT_ATTENDEE = 1;
    private static final int REQUEST_SELECT_RECORDER = 2;
    private static final int REQUEST_SELECT_LIAISON = 3;
    private static final int REQUEST_SELECT_MEETING_ROOM = 4;
    private static final int REQUEST_SET_REMIND_EVENT = 5;

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
        meeting = (Meeting) getIntent().getSerializableExtra(MeetingDetailActivity.EXTRA_MEETING_ENTITY); //来自列表
        divideAttendeeGroup();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save111:
                break;
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    private void divideAttendeeGroup() {
        MeetingAttendees meetingInvite = new MeetingAttendees();
        MeetingAttendees meetingAcceptAttendees = new MeetingAttendees();
        MeetingAttendees meetingDenyAttendees = new MeetingAttendees();
        MeetingAttendees meetingNoActionAttendees = new MeetingAttendees();
        //  SearchModel inviter = ContactUserCacheUtils.getContactUserByUid(meeting.getOwner());
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
        Bundle bundle = new Bundle();
        String uid = meetingAttendeesList.get(i).getMeetingAttendeesList().get(i1).getId();
        bundle.putString("uid", uid);
        IntentUtils.startActivity(MeetingAttendeeStateActivity.this, UserInfoActivity.class, bundle);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            List<SearchModel> searchModelList = (List<SearchModel>) data.getExtras().getSerializable("selectMemList");
//            MeetingAttendees meetingAttendees1 = new MeetingAttendees();
//            meetingAttendees1.setType(MeetingAttendees.MEETING_ATTENDEES_INVITE);
//            MeetingAttendees meetingAttendees2 = new MeetingAttendees();
//            meetingAttendees2.setType(MeetingAttendees.MEETING_ATTENDEES_ACCEPT);
//            MeetingAttendees meetingAttendees3 = new MeetingAttendees();
//            meetingAttendees3.setType(MeetingAttendees.MEETING_ATTENDEES_DENY);
//            MeetingAttendees meetingAttendees4 = new MeetingAttendees();
//            meetingAttendees4.setType(MeetingAttendees.MEETING_ATTENDEES_NO_ACTION);
//            for (int i = 0; i < searchModelList.size(); i++) {
//                if (i >= 0 && i < 1) {
//                    meetingAttendees1.getMeetingAttendeesList().add(searchModelList.get(i));
//                } else if (i >= 1 && i < 3) {
//                    meetingAttendees2.getMeetingAttendeesList().add(searchModelList.get(i));
//                } else if (i >= 3 && i < 5) {
//                    meetingAttendees3.getMeetingAttendeesList().add(searchModelList.get(i));
//                } else if (i >= 5 && i < 7) {
//                    meetingAttendees4.getMeetingAttendeesList().add(searchModelList.get(i));
//                } else {
//
//                }
//            }
//            meetingAttendeesList.add(meetingAttendees1);
//            meetingAttendeesList.add(meetingAttendees2);
//            meetingAttendeesList.add(meetingAttendees3);
//            meetingAttendeesList.add(meetingAttendees4);
//            meetingAttendeeStateAdapter.notifyDataSetChanged();
//
//        }
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
            SearchModel searchModel = meetingAttendeesList.get(i).getMeetingAttendeesList().get(i1);
            view = LayoutInflater.from(context).inflate(R.layout.meeting_attendees_expandale_child_item, null);
            TextView attendeeNameText = view.findViewById(R.id.tv_attendee_name);
            ImageView attendeeHeadImage = view.findViewById(R.id.iv_attendee_head);
            View dividerView = view.findViewById(R.id.view_divider);
            dividerView.setVisibility(View.VISIBLE);
            attendeeNameText.setText(searchModel.getName());
            final String uid = searchModel.getId();
            String photoUrl = APIUri.getChannelImgUrl(MyApplication.getInstance(), uid);
            ImageDisplayUtils.getInstance().displayRoundedImage(attendeeHeadImage, photoUrl, R.drawable.icon_person_default, context, 15);
            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }
    }


    //更新个人信息状态
    class WebService extends APIInterfaceInstance {

    }

}
