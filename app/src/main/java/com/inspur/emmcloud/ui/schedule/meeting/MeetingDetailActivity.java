package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.Location;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.helper.StringUtil;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/4/16.
 */
@ContentView(R.layout.activity_meeting_detail_new)
public class MeetingDetailActivity extends BaseActivity {

    private static final int MEETING_ATTENDEE = 0;
    private static final int MEETING_RECORD_HOLDER = 1;
    private static final int MEETING_CONTACT = 2;
    public static final String EXTRA_MEETING_ENTITY = "extra_meeting_entity";

    @ViewInject(R.id.tv_meeting_title)
    private TextView meetingTitleText;
    @ViewInject(R.id.tv_meeting_time)
    private TextView meetingTimeText;
    @ViewInject(R.id.tv_meeting_remind)
    private TextView meetingRemindText;
    @ViewInject(R.id.tv_meeting_distribution)
    private TextView meetingDistributionText;
    @ViewInject(R.id.tv_meeting_create)
    private TextView meetingCreateTimeText;
    @ViewInject(R.id.tv_attendee)
    private TextView attendeeText;
    @ViewInject(R.id.tv_location)
    private TextView meetingLocationText;
    @ViewInject(R.id.tv_meeting_record_holder)
    private TextView meetingRecordHolderText;
    @ViewInject(R.id.tv_meeting_conference)
    private TextView meetingConferenceText;
    @ViewInject(R.id.tv_meeting_note)
    private TextView meetingNoteText;
    @ViewInject(R.id.rl_meeting_record_holder)
    private RelativeLayout meetingRecordHolderLayout;
    @ViewInject(R.id.rl_meeting_conference)
    private RelativeLayout meetingConferenceLayout;
    @ViewInject(R.id.rl_meeting_note)
    private RelativeLayout meetingNoteLayout;
    @ViewInject(R.id.iv_meeting_detail_more)
    private ImageView meetingMoreImg;

    private Meeting meeting;
    private ScheduleApiService scheduleApiService;
    private LoadingDialog loadingDlg;
    private String meetingId;   //会议id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleApiService(this);
        scheduleApiService.setAPIInterface(new WebService());
        meetingId = getIntent().getStringExtra(Constant.SCHEDULE_QUERY); //来自通知
        meeting = (Meeting) getIntent().getSerializableExtra(EXTRA_MEETING_ENTITY); //来自列表
        if (!TextUtils.isEmpty(meetingId)) {    //id不为空是从网络获取数据  来自通知
            getMeetingFromId(meetingId);
        } else {                                //id为空是走之前逻辑
            initViews();
        }
    }

    private void initViews() {
        meetingTitleText.setText(meeting.getTitle());
        meetingTimeText.setText(getString(R.string.meeting_detail_time, getMeetingTime()));
        meetingRemindText.setText(getString(R.string.meeting_detail_remind, ScheduleAlertTimeActivity.getAlertTimeNameByTime(meeting.getRemindEventObj().getAdvanceTimeSpan(), meeting.getAllDay())));
//        meetingDistributionText.setText(meeting.getOwner());
        String locationData = getString(R.string.meeting_detail_location) + new Location(meeting.getLocation()).getBuilding() + " " + new Location(meeting.getLocation()).getDisplayName();
        meetingLocationText.setText(locationData);
        meetingDistributionText.setVisibility(View.VISIBLE);
        meetingDistributionText.setText(getMeetingCategory(meeting));
        meetingCreateTimeText.setText(getString(R.string.meeting_detail_create, TimeUtils.calendar2FormatString(this,
                TimeUtils.timeLong2Calendar(meeting.getCreationTime()), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE)));
        attendeeText.setText(getString(R.string.meeting_detail_attendee, getMeetingParticipant(MEETING_ATTENDEE)));
        meetingRecordHolderText.setText(getString(R.string.meeting_detail_record_holder, getMeetingParticipant(MEETING_RECORD_HOLDER)));
        meetingConferenceText.setText(getString(R.string.meeting_detail_conference, getMeetingParticipant(MEETING_CONTACT)));
        meetingNoteText.setText(meeting.getNote());
        meetingRecordHolderLayout.setVisibility(meeting.getRecorderParticipantList().size() > 0 ? View.VISIBLE : View.GONE);
        meetingConferenceLayout.setVisibility(meeting.getRoleParticipantList().size() > 0 ? View.VISIBLE : View.GONE);
        meetingNoteLayout.setVisibility(StringUtil.isBlank(meeting.getNote()) ? View.GONE : View.VISIBLE);
        meetingMoreImg.setVisibility((meeting.getOwner().equals(MyApplication.getInstance().getUid()) && System.currentTimeMillis() < meeting.getEndTime()) ? View.VISIBLE : View.GONE);
    }

    private String getMeetingCategory(Meeting meeting) {
        String meetingCategory = "";
        if (meeting.getOwner().equals(MyApplication.getInstance().getUid())) {
            meetingCategory = getString(R.string.schedule_meeting_my_create);
        } else {
            List<Participant> participantList = meeting.getAllParticipantList();
            for (int i = 0; i < participantList.size(); i++) {
                if (participantList.get(i).getId().equals(MyApplication.getInstance().getUid())) {
                    meetingCategory = getString(R.string.schedule_meeting_my_take_part_in);
                    break;
                }
            }
        }
        return meetingCategory;
    }


    /**
     * 通过id获取会议数据
     */
    private void getMeetingFromId(String id) {
        if (NetUtils.isNetworkConnected(this)) {
            if (meeting == null) {
                loadingDlg.show();
            }
            scheduleApiService.getMeetingDataFromId(id);
        } else {
            ToastUtils.show(this, "");
        }
    }


    private String getMeetingParticipant(int type) {
        List<Participant> participantList = null;
        switch (type) {
            case MEETING_ATTENDEE:
                participantList = meeting.getCommonParticipantList();
                break;
            case MEETING_RECORD_HOLDER:
                participantList = meeting.getRecorderParticipantList();
                break;
            case MEETING_CONTACT:
                participantList = meeting.getRoleParticipantList();
                break;
        }
        if (participantList.size() == 0) {
            return "";
        } else if (participantList.size() == 1) {
            return ContactUserCacheUtils.getContactUserByUid(participantList.get(0).getId()).getName();
        } else {
            return getString(R.string.meeting_detail_attendee_num,
                    ContactUserCacheUtils.getContactUserByUid(participantList.get(0).getId()).getName(),
                    participantList.size());
        }
    }


    /**
     * 获取会议起止时间
     *
     * @return
     */
    private String getMeetingTime() {
        String duringTime = "";
        long startTime = meeting.getStartTime();
        long endTime = meeting.getEndTime();
        if (TimeUtils.isSameDay(TimeUtils.timeLong2Calendar(startTime), TimeUtils.timeLong2Calendar(endTime))) {
            duringTime = TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(startTime), TimeUtils.FORMAT_MONTH_DAY) + " " +
                    TimeUtils.getWeekDay(this, TimeUtils.timeLong2Calendar(startTime)) + " " +
                    TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(startTime), TimeUtils.FORMAT_HOUR_MINUTE) +
                    " - " + TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(endTime), TimeUtils.FORMAT_HOUR_MINUTE);
        } else {
            duringTime = TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(startTime), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE) +
                    " - " + TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(endTime), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
        }
        return duringTime;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_meeting_detail_more:
                showDialog();
                break;
            case R.id.rl_meeting_attendee:
                startMembersActivity(MEETING_ATTENDEE);
                break;
            case R.id.rl_meeting_record_holder:
                startMembersActivity(MEETING_RECORD_HOLDER);
                break;
            case R.id.rl_meeting_conference:
                startMembersActivity(MEETING_CONTACT);
                break;
            case R.id.rl_meeting_sign:
                break;
            case R.id.rl_meeting_summary:
                break;
        }
    }

    private void startMembersActivity(int type) {
        List<String> uidList;
        switch (type) {
            case MEETING_ATTENDEE:
                uidList = getUidList(meeting.getCommonParticipantList());
                break;
            case MEETING_RECORD_HOLDER:
                uidList = getUidList(meeting.getRecorderParticipantList());
                break;
            case MEETING_CONTACT:
                uidList = getUidList(meeting.getRoleParticipantList());
                break;
            default:
                uidList = new ArrayList<>();
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("uidList", (ArrayList<String>) uidList);
        bundle.putString("title", getString(R.string.meeting_memebers));
        bundle.putInt(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.CHECK_STATE);
        IntentUtils.startActivity(this, MembersActivity.class, bundle);
    }

    private List<String> getUidList(List<Participant> commonParticipantList) {
        List<String> uidList = new ArrayList<>();
        for (Participant participant : commonParticipantList) {
            uidList.add(participant.getId());
        }
        return uidList;
    }

    private void showDialog() {
        new ActionSheetDialog.ActionListSheetBuilder(MeetingDetailActivity.this)
                //    .addItem(getString(R.string.meeting_detail_show_qrcode))
                .addItem(getString(R.string.schedule_meeting_change))
                .addItem(getString(R.string.schedule_meeting_cancel))
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(EXTRA_MEETING_ENTITY, meeting);
                                IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAddActivity.class, bundle, true);
                                break;
                            case 1:
                                deleteMeeting(meeting);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    /**
     * 删除会议
     */
    private void deleteMeeting(Meeting meeting) {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            scheduleApiService.deleteMeeting(meeting);
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnDelMeetingSuccess(Meeting meeting) {
            LoadingDialog.dimissDlg(loadingDlg);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED, null));
            finish();
        }

        @Override
        public void returnDelMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnMeetingDataFromIdSuccess(Meeting meetingData) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (meetingData != null) {
                meeting = meetingData;
                initViews();
            }
        }

        @Override
        public void returnMeetingDataFromIdFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            LoadingDialog.dimissDlg(loadingDlg);
            finish();
        }
    }
}
