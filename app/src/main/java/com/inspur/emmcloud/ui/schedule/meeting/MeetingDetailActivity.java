package com.inspur.emmcloud.ui.schedule.meeting;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.schedule.Location;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.ReplyAttendResult;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    private static final String TYPE_DEFAULT = "default";
    private static final String TYPE_MEETING = "meeting";
    private static final String TYPE_EXCHANGE = "exchange";
    private static final String TYPE_WEBEX = "webex";
    @BindView(R.id.tv_meeting_title)
    TextView meetingTitleText;
    @BindView(R.id.tv_meeting_time)
    TextView meetingTimeText;
    @BindView(R.id.tv_meeting_remind)
    TextView meetingRemindText;
    @BindView(R.id.meeting_distribution_layout)
    View meetingDistributionLayout;
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
    @BindView(R.id.tv_meeting_calendar_type)
    TextView meetingCalendarTypeText;  //日历类型
    @BindView(R.id.rl_meeting_attend_status)
    RelativeLayout attendStatusLayout;
    @BindView(R.id.tv_meeting_attend_status)
    TextView attendStatusText;
    @BindView(R.id.header_text)
    TextView headerTextView;

    ReplyAttendResult info = new ReplyAttendResult(); //参会答复
    private Meeting meeting;
    private Schedule schedule;
    private ScheduleApiService scheduleApiService;
    private LoadingDialog loadingDlg;
    private String meetingId;   //会议id
    private boolean isHistoryMeeting = false; //是否来自历史会议
    private List<String> moreTextList = new ArrayList<>();
    private String chatGroupId; //群聊ID
    private String eventType;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleApiService(this);
        scheduleApiService.setAPIInterface(new WebService());
        meetingId = getIntent().getStringExtra(Constant.SCHEDULE_QUERY); //来自通知
        meeting = (Meeting) getIntent().getSerializableExtra(EXTRA_MEETING_ENTITY); //来自列表的会议
        schedule = (Schedule) getIntent().getSerializableExtra(EXTRA_MEETING_ENTITY); //来自列表的日程
        isHistoryMeeting = getIntent().getBooleanExtra(Constant.EXTRA_IS_HISTORY_MEETING, false);
        info.responseType = Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN; //默认参会状态未知
        eventType = meeting != null || !StringUtils.isBlank(meetingId) ? Schedule.TYPE_MEETING : Schedule.TYPE_CALENDAR;//判断当前的事件类型(会议或日程)
        getIsMeetingAdmin();
        if (!TextUtils.isEmpty(meetingId)) {    //id不为空是从网络获取数据  来自通知
            getMeetingFromId(meetingId);
        } else {                                //id为空是走之前逻辑
            initViews();
        }
        if (StringUtils.isBlank(meetingId)) {
            meetingId = meeting.getId();
        }
        scheduleApiService.getCalendarBindChat(meetingId);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_detail_latest;
    }

    @SuppressLint("StringFormatInvalid")
    private void initViews() {
        // headerTextView.setText();
        meetingTitleText.setText(meeting.getTitle());
        meetingTimeText.setText(getString(R.string.meeting_detail_time, getMeetingTime()));
        meetingRemindText.setText(getString(R.string.meeting_detail_remind, ScheduleAlertTimeActivity.getAlertTimeNameByTime(meeting.getRemindEventObj().getAdvanceTimeSpan(), meeting.getAllDay())));
//        meetingDistributionText.setText(meeting.getOwner());
        if (!StringUtils.isBlank(meeting.getOwner())) {
            String userName = ContactUserCacheUtils.getUserName(meeting.getOwner());
            meetingInviteText.setText(getString(R.string.meeting_detail_inviter, userName));
        }
        meetingInviteText.setVisibility(StringUtils.isBlank(meeting.getOwner()) ? View.GONE : View.VISIBLE);
        String locationData = getString(R.string.meeting_detail_location) + new Location(meeting.getLocation()).getBuilding() + " " + new Location(meeting.getLocation()).getDisplayName();
        meetingLocationText.setText(locationData);
        meetingDistributionText.setVisibility(View.VISIBLE);
        meetingDistributionText.setText(getMeetingCategory(meeting));
        if (StringUtils.isBlank(getMeetingCategory(meeting))) {
            meetingDistributionLayout.setVisibility(View.GONE);
        }
        meetingCreateTimeText.setText(getString(R.string.meeting_detail_create, TimeUtils.calendar2FormatString(this,
                TimeUtils.timeLong2Calendar(meeting.getCreationTime()), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE)));
        attendeeText.setText(getString(R.string.meeting_detail_attendee, getMeetingParticipant()));
        meetingNoteText.setText(meeting.getNote());
        meetingNoteLayout.setVisibility(StringUtil.isBlank(meeting.getNote()) ? View.GONE : View.VISIBLE);
        meetingMoreImg.setVisibility((PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                false) || (meeting.getOwner().equals(MyApplication.getInstance().getUid())) && System.currentTimeMillis() < meeting.getEndTime()) ? View.VISIBLE : View.GONE);
        initScheduleType();
        initDiffStatus();
    }

    private void initDiffStatus() {
        //如果不是相关人员  隐藏
        boolean relatedPersonFlag = false;
        List<Participant> list = meeting.getAllParticipantList();
        Participant mParticipant = null;
        for (Participant item : list) {
            if (BaseApplication.getInstance().getUid().equals(item.getId())) {
                info.responseType = item.getResponseType();
                mParticipant = item;
                relatedPersonFlag = true;
            }
        }

        initAttendStatus(mParticipant);

        if (BaseApplication.getInstance().getUid().equals(meeting.getOwner())) {
            relatedPersonFlag = true;
        }
        boolean isMeetingAdmin = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN, false);
        boolean isMeetingCreater = meeting.getOwner().equals(MyApplication.getInstance().getUid());
        if (relatedPersonFlag || isMeetingAdmin) {
            meetingMoreImg.setVisibility(View.VISIBLE);
            attendStatusLayout.setVisibility((isHistoryMeeting || isMeetingCreater) ? View.GONE : View.VISIBLE);

            //管理员不显示发起群聊 (创建者跟参会人)
            if (relatedPersonFlag && WebServiceRouterManager.getInstance().isV1xVersionChat()) {
                moreTextList.add(getString(R.string.message_create_group)); //发起群聊
            }

            //管理员 并且不是创建者 (管理员只能删除会议  创建者可以删除和修改会议)
            final boolean isShowChangeMeeting = isMeetingAdmin && !isMeetingCreater;

            //仅有管理员跟创建者有此逻辑
            if (isMeetingAdmin || isMeetingCreater) {
                if (isShowChangeMeeting) {   //仅是管理员
                    moreTextList.add(getString(R.string.schedule_meeting_cancel));
                } else {    //创建者 or 创建者同时管理员
                    if (!isHistoryMeeting) {
                        moreTextList.add(getString(R.string.schedule_meeting_change));
                    }
                    moreTextList.add(getString(R.string.schedule_meeting_cancel));
                }
            }
        } else {
            meetingMoreImg.setVisibility(View.GONE);
            attendStatusLayout.setVisibility(View.GONE);
        }
    }

    private void initScheduleType() {
        if (meeting == null) return;
        switch (meeting.getType()) {
            case TYPE_MEETING:
                meetingCalendarTypeText.setText(getString(R.string.meeting));
                break;
            case TYPE_EXCHANGE:
                meetingCalendarTypeText.setText(TYPE_EXCHANGE);
                break;
            case TYPE_WEBEX:
                meetingCalendarTypeText.setText(TYPE_WEBEX);
                break;
            default:
                meetingCalendarTypeText.setText(getString(R.string.calendar));
                break;
        }
    }

    private void initAttendStatus(Participant participant) {
        if (participant == null) return;
        switch (participant.getResponseType()) {
            case Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN:
                attendStatusText.setText(getString(R.string.schedule_meeting_attend_status_default));
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_ACCEPT:
                attendStatusText.setText(getString(R.string.schedule_meeting_attend_accept));
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_TENTATIVE:
                attendStatusText.setText(getString(R.string.schedule_meeting_attend_ignore));
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_DECLINE:
                attendStatusText.setText(getString(R.string.schedule_meeting_attend_reject));
                break;
        }
    }

    /**
     * 判断当前用户是否会议室管理员
     */
    private void getIsMeetingAdmin() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && (eventType.equals(Schedule.TYPE_MEETING))) {
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

    private String getMeetingParticipant() {
        List<Participant> participantList = deleteRepeatData(meeting.getAllParticipantList());
        if (participantList.size() == 0) {
            return "";
        }
        Participant participant = participantList.get(0);
        if (participantList.size() == 1) {
            if (StringUtils.isBlank(participant.getId()) ||
                    ContactUserCacheUtils.getContactUserByUid(participant.getId()) == null) { //id为空但是有name的情况
                return participant.getName() + participant.getEmail();
            }

            return ContactUserCacheUtils.getContactUserByUid(participantList.get(0).getId()).getName();
        } else {
            if (StringUtils.isBlank(participantList.get(0).getId()) ||
                    ContactUserCacheUtils.getContactUserByUid(participant.getId()) == null) {  //id为空但是有name的情况
                return getString(R.string.meeting_detail_attendee_num,
                        participantList.get(0).getName() + participantList.get(0).getEmail(),
                        participantList.size());
            }
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
                if (meeting != null)
                    IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAttendeeStateActivity.class, bundle);
                break;
            case R.id.rl_meeting_conference:
                if (meeting != null)
                    IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAttendeeStateActivity.class, bundle);
                break;
            case R.id.rl_meeting_invite:
                startMembersActivity(MEETING_INVITE);
                break;
            case R.id.rl_meeting_attend_status:     //参会答复
                Intent replyIntent = new Intent(this, MeetingDetailReplyActivity.class);
                replyIntent.putExtra("OriginReplyData", info);
                replyIntent.putExtra("meetingId", meetingId);
                startActivityForResult(replyIntent, 0);
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
        Bundle bundle = new Bundle();
        switch (type) {
            case MEETING_ATTENDEE:
                uidList = getUidList(meeting.getCommonParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_attendee_title));
                break;
            case MEETING_RECORD_HOLDER:
                uidList = getUidList(meeting.getRecorderParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_record_holder_title));
                break;
            case MEETING_CONTACT:
                uidList = getUidList(meeting.getRoleParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_conference_title));
                break;
            case MEETING_INVITE:
                uidList.add(meeting.getOwner());
                bundle.putString("title", getString(R.string.meeting_detail_invite));
                break;
            default:
                break;
        }
        bundle.putStringArrayList("uidList", (ArrayList<String>) uidList);
        bundle.putInt(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.CHECK_STATE);
        IntentUtils.startActivity(this, MembersActivity.class, bundle);
    }

    //list去重
    private List<Participant> deleteRepeatData(List<Participant> list) {
        //把创建人加入到群聊
        if (!StringUtils.isBlank(meeting.getOwner())) {
            Participant ownerParticipant = new Participant();
            ownerParticipant.setId(meeting.getOwner());
            String ownerName = ContactUserCacheUtils.getUserName(meeting.getOwner());
            ownerParticipant.setName(ownerName);
            list.add(ownerParticipant);
        }

        for (Participant item : list) {
            ContactUser user = ContactUserCacheUtils.getContactUserByUid(item.getId());
            if (user == null) {
                list.remove(item);
            }
        }
        Set<Participant> set = new TreeSet<>(new Comparator<Participant>() {
            @Override
            public int compare(Participant o1, Participant o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        set.addAll(list);
        List<Participant> result = new ArrayList<>(set);
        Collections.reverse(result);

        return result;
    }

    private List<String> getUidList(List<Participant> commonParticipantList) {
        List<String> uidList = new ArrayList<>();
        for (Participant participant : commonParticipantList) {
            uidList.add(participant.getId());
        }
        return uidList;
    }

    private void showOperationDialog() {
        ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener onSheetItemClickListener = new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                String tag = (String) itemView.getTag();
                if (tag.equals(getString(R.string.schedule_meeting_change))) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EXTRA_MEETING_ENTITY, meeting);
                    IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAddActivity.class, bundle, true);
                } else if (tag.equals(getString(R.string.schedule_meeting_cancel))) {
                    showConfirmClearDialog(meeting);
                } else if (tag.equals(getString(R.string.message_create_group))) {
//                    startGroupChat();
                    new ChatCreateUtils().startGroupChat(MeetingDetailActivity.this, meeting, chatGroupId, new ChatCreateUtils.ICreateGroupChatListener() {
                        @Override
                        public void createSuccess() {
//                            ToastUtils.show("发起群聊成功");
                        }

                        @Override
                        public void createFail() {
//                            ToastUtils.show("发起群聊失败");
                        }
                    });
                }
                dialog.dismiss();
            }
        };

        ActionSheetDialog.ActionListSheetBuilder builder = new ActionSheetDialog.ActionListSheetBuilder(this);
        for (int i = 0; i < moreTextList.size(); i++) {
            builder.addItem(moreTextList.get(i));
        }
        builder.setOnSheetItemClickListener(onSheetItemClickListener)
                .build()
                .show();
    }

    /**
     * 确认清除
     */
    private void showConfirmClearDialog(final Meeting meeting) {
        new CustomDialog.MessageDialogBuilder(MeetingDetailActivity.this)
                .setMessage(getString(R.string.meeting_cancel_the_meeting))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteMeeting(meeting);
                        finish();
                    }
                })
                .show();
    }


    /**
     * 删除会议
     */
    private void deleteMeeting(Meeting meeting) {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            scheduleApiService.deleteMeeting(meeting, false);
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

        @Override
        public void returnGetCalendarChatBindSuccess(String calendar, String cid) {
            chatGroupId = cid;
        }

        //获取群聊cid
        @Override
        public void returnSetCalendarChatBindSuccess(String calendarId, String chatId) {
        }
    }
}
