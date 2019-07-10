package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.schedule.Location;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.ReplyAttendResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2019/4/16.
 */
public class MeetingDetailActivity extends BaseActivity {

    public static final String EXTRA_MEETING_ENTITY = "extra_meeting_entity";
    private static final int MEETING_ATTENDEE = 0;
    private static final int MEETING_RECORD_HOLDER = 1;
    private static final int MEETING_CONTACT = 2;
    private static final int MEETING_INVITE = 3;
    @BindView(R.id.tv_meeting_title)
    TextView meetingTitleText;
    @BindView(R.id.tv_meeting_time)
    TextView meetingTimeText;
    @BindView(R.id.tv_meeting_remind)
    TextView meetingRemindText;
    @BindView(R.id.tv_meeting_distribution)
    TextView meetingDistributionText;
    @BindView(R.id.tv_meeting_create)
    TextView meetingCreateTimeText;
    @BindView(R.id.tv_attendee)
    TextView attendeeText;
    @BindView(R.id.tv_location)
    TextView meetingLocationText;
    @BindView(R.id.tv_meeting_record_holder)
    TextView meetingRecordHolderText;
    @BindView(R.id.tv_meeting_conference)
    TextView meetingConferenceText;
    @BindView(R.id.tv_meeting_note)
    TextView meetingNoteText;
    @BindView(R.id.rl_meeting_record_holder)
    RelativeLayout meetingRecordHolderLayout;
    @BindView(R.id.rl_meeting_conference)
    RelativeLayout meetingConferenceLayout;
    @BindView(R.id.rl_meeting_note)
    RelativeLayout meetingNoteLayout;
    @BindView(R.id.iv_meeting_detail_more)
    ImageView meetingMoreImg;
    @BindView(R.id.tv_meeting_invite)
    TextView meetingInviteText;
    @BindView(R.id.tv_meeting_attend_status)
    TextView attendStatusText;
    ReplyAttendResult info = new ReplyAttendResult(); //参会答复
    private Meeting meeting;
    private ScheduleApiService scheduleApiService;
    private LoadingDialog loadingDlg;
    private String meetingId;   //会议id

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleApiService(this);
        scheduleApiService.setAPIInterface(new WebService());
        meetingId = getIntent().getStringExtra(Constant.SCHEDULE_QUERY); //来自通知
        meeting = (Meeting) getIntent().getSerializableExtra(EXTRA_MEETING_ENTITY); //来自列表
        info.position = 1;
        getIsMeetingAdmin();
        if (!TextUtils.isEmpty(meetingId)) {    //id不为空是从网络获取数据  来自通知
            getMeetingFromId(meetingId);
        } else {                                //id为空是走之前逻辑
            initViews();
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_detail_tmp;
    }

    private void initViews() {
        meetingTitleText.setText(meeting.getTitle());
        meetingTimeText.setText(getString(R.string.meeting_detail_time, getMeetingTime()));
        meetingRemindText.setText(getString(R.string.meeting_detail_remind, ScheduleAlertTimeActivity.getAlertTimeNameByTime(meeting.getRemindEventObj().getAdvanceTimeSpan(), meeting.getAllDay())));
//        meetingDistributionText.setText(meeting.getOwner());
        meetingInviteText.setText("邀请人：" + meeting.getOwner());
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
        meetingMoreImg.setVisibility((PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                false) || (meeting.getOwner().equals(MyApplication.getInstance().getUid())) && System.currentTimeMillis() < meeting.getEndTime()) ? View.VISIBLE : View.GONE);
    }

    /**
     * 判断当前用户是否会议室管理员
     */
    private void getIsMeetingAdmin() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            if (!PreferencesByUserAndTanentUtils.isKeyExist(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN)) {
                loadingDlg.show();
            }
            scheduleApiService.getIsMeetingAdmin(MyApplication.getInstance().getUid());
        }
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
        Bundle bundle = new Bundle();
        if (meeting != null) {
            LogUtils.LbcDebug("meeting" + JSONUtils.toJSONString(meeting));
        } else {
            LogUtils.LbcDebug("meeting == null");
        }
        bundle.putSerializable(MeetingDetailActivity.EXTRA_MEETING_ENTITY, meeting);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_meeting_detail_more:
                showOperationDialog();
                break;
            case R.id.rl_meeting_attendee:
                if (meeting != null)
                    IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAttendeeStateActivity.class, bundle);
                break;
            case R.id.rl_meeting_record_holder:
                IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAttendeeStateActivity.class, bundle);
                break;
            case R.id.rl_meeting_conference:
                if (meeting != null)
                    IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAttendeeStateActivity.class, bundle);
                break;
            case R.id.rl_meeting_invite:    //  TODO
                startMembersActivity(MEETING_INVITE);
                break;
            case R.id.rl_meeting_attend_status:     //参会答复
                Intent replyIntent = new Intent(this, MeetingDetailReplyActivity.class);
                replyIntent.putExtra("OriginReplyData", info);
                startActivityForResult(replyIntent, 0);
                break;
            case R.id.tv_meeting_create_group_chat: //发起群聊  TODO
                Intent contactIntent = new Intent();
                contactIntent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                contactIntent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                contactIntent.putExtra(ContactSearchFragment.EXTRA_TITLE,
                        getString(R.string.message_create_group));
                contactIntent.setClass(this, ContactSearchActivity.class);
                startActivity(contactIntent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 100) {
            info = (ReplyAttendResult) data.getSerializableExtra("ReplyResult");
            attendStatusText.setText(info.content);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startMembersActivity(int type) {
        List<String> uidList = new ArrayList<>();
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
            case MEETING_INVITE:
                uidList.add(meeting.getOwner());
                break;
            default:
                break;
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

    private void showOperationDialog() {
        final boolean isShowChangeMeeting = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                false) && !meeting.getOwner().equals(MyApplication.getInstance().getUid());
        ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener onSheetItemClickListener = new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                switch (position) {
                    case 0:
                        if (isShowChangeMeeting) {
                            deleteMeeting(meeting);
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(EXTRA_MEETING_ENTITY, meeting);
                            IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAddActivity.class, bundle, true);
                        }
                        break;
                    case 1:
                        deleteMeeting(meeting);
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        };
        if (isShowChangeMeeting) {
            new ActionSheetDialog.ActionListSheetBuilder(MeetingDetailActivity.this)
                    //    .addItem(getString(R.string.meeting_detail_show_qrcode))
                    .addItem(getString(R.string.schedule_meeting_cancel))
                    .setOnSheetItemClickListener(onSheetItemClickListener)
                    .build()
                    .show();
        } else {
            new ActionSheetDialog.ActionListSheetBuilder(MeetingDetailActivity.this)
                    //    .addItem(getString(R.string.meeting_detail_show_qrcode))
                    .addItem(getString(R.string.schedule_meeting_change))
                    .addItem(getString(R.string.schedule_meeting_cancel))
                    .setOnSheetItemClickListener(onSheetItemClickListener)
                    .build()
                    .show();
        }
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
        public void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin) {
            LoadingDialog.dimissDlg(loadingDlg);
            PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                    getIsAdmin.isAdmin());
        }

        @Override
        public void returnIsMeetingAdminFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
        }

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
