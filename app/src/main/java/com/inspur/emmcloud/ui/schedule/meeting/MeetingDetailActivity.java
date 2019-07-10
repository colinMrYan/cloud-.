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
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.schedule.Location;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.ReplyAttendResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    @BindView(R.id.rl_meeting_attend_status)
    RelativeLayout attendStatusLayout;
    @BindView(R.id.tv_meeting_attend_status)
    TextView attendStatusText;

    private Meeting meeting;
    private ScheduleApiService scheduleApiService;
    private LoadingDialog loadingDlg;
    private String meetingId;   //会议id
    ReplyAttendResult info = new ReplyAttendResult(); //参会答复
    private boolean isHistoryMeeting = false; //是否来自历史会议
    private List<String> moreTextList = new ArrayList<>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleApiService(this);
        scheduleApiService.setAPIInterface(new WebService());
        meetingId = getIntent().getStringExtra(Constant.SCHEDULE_QUERY); //来自通知
        meeting = (Meeting) getIntent().getSerializableExtra(EXTRA_MEETING_ENTITY); //来自列表
        isHistoryMeeting = getIntent().getBooleanExtra(Constant.EXTRA_IS_HISTORY_MEETING, false);
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
        String userName = ContactUserCacheUtils.getUserName(meeting.getOwner());
        meetingInviteText.setText(getString(R.string.meeting_detail_inviter, userName));
        meetingInviteText.setVisibility(StringUtils.isBlank(userName) ? View.GONE : View.VISIBLE);
        String locationData = getString(R.string.meeting_detail_location) + new Location(meeting.getLocation()).getBuilding() + " " + new Location(meeting.getLocation()).getDisplayName();
        meetingLocationText.setText(locationData);
        meetingDistributionText.setVisibility(View.VISIBLE);
        meetingDistributionText.setText(getMeetingCategory(meeting));
        if (StringUtils.isBlank(getMeetingCategory(meeting))) {
            meetingDistributionLayout.setVisibility(View.GONE);
        }
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
        setMoreImageStatus();
    }

    private void setMoreImageStatus() {
        //如果不是相关人员  隐藏
        boolean relatedPersonFlag = false;
        List<Participant> list = meeting.getAllParticipantList();
        for (Participant item : list) {
            if (BaseApplication.getInstance().getUid().equals(item.getId())) {
                relatedPersonFlag = true;
            }
        }

        if (BaseApplication.getInstance().getUid().equals(meeting.getOwner())) {
            relatedPersonFlag = true;
        }

        if (relatedPersonFlag) {
            meetingMoreImg.setVisibility(View.VISIBLE);
            attendStatusLayout.setVisibility(isHistoryMeeting ? View.GONE : View.VISIBLE);

            final boolean isShowChangeMeeting = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                    false) && !meeting.getOwner().equals(MyApplication.getInstance().getUid());
            if (isHistoryMeeting) {
                moreTextList.add(getString(R.string.message_create_group)); //发起群聊
            } else {
                if (isShowChangeMeeting) {
                    moreTextList.add(getString(R.string.message_create_group)); //发起群聊
                    moreTextList.add(getString(R.string.schedule_meeting_cancel));
                } else {
                    moreTextList.add(getString(R.string.message_create_group)); //发起群聊
                    moreTextList.add(getString(R.string.schedule_meeting_change));
                    moreTextList.add(getString(R.string.schedule_meeting_cancel));
                }
            }

        } else {
            meetingMoreImg.setVisibility(View.GONE);
            attendStatusLayout.setVisibility(View.GONE);
        }
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
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_meeting_detail_more:
                showOperationDialog();
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
            case R.id.rl_meeting_invite:    //邀请人
                startMembersActivity(MEETING_INVITE);
                break;
            case R.id.rl_meeting_attend_status:     //参会答复
                Intent replyIntent = new Intent(this, MeetingDetailReplyActivity.class);
                replyIntent.putExtra("OriginReplyData", info);
                startActivityForResult(replyIntent, 0);
                break;
            case R.id.tv_meeting_create_group_chat: //发起群聊
                startGroupChat();
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
            case MEETING_INVITE:
                uidList = new ArrayList<>();
                uidList.add(meeting.getOwner());
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

    //list去重
    private List<Participant> deleteRepeatData(List<Participant> list) {
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

    //发起群聊
    private void startGroupChat() {
        List<Participant> totalList = deleteRepeatData(meeting.getAllParticipantList());
        JSONArray peopleArray = new JSONArray();
        for (Participant participant : totalList) {
            JSONObject json = new JSONObject();
            try {
                if (!participant.getId().equals(BaseApplication.getInstance().getUid())) {
                    json.put("pid", participant.getId());
                    json.put("name", participant.getName());
                    peopleArray.put(json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        new ConversationCreateUtils().createGroupConversation(this, peopleArray, new ConversationCreateUtils.OnCreateGroupConversationListener() {
            @Override
            public void createGroupConversationSuccess(Conversation conversation) {
                if (TabAndAppExistUtils.isTabExist(MeetingDetailActivity.this, Constant.APP_TAB_BAR_COMMUNACATE)) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                    IntentUtils.startActivity(MeetingDetailActivity.this, ConversationActivity.class, bundle);
                    //创建群聊成功后  通知消息界面刷新界面数据（群组头像等）
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CHAT_CHANGE, conversation));
                }
            }

            @Override
            public void createGroupConversationFail() {
            }
        });
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
                String tag = (String) itemView.getTag();
                if (tag.equals(getString(R.string.schedule_meeting_change))) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EXTRA_MEETING_ENTITY, meeting);
                    IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAddActivity.class, bundle, true);
                } else if (tag.equals(getString(R.string.schedule_meeting_cancel))) {
                    deleteMeeting(meeting);
                } else if (tag.equals(getString(R.string.message_create_group))) {
                    startGroupChat();
                }
//                switch (position) {
//                    case 0:
//                        if (isShowChangeMeeting) {
//                            deleteMeeting(meeting);
//                        } else {
//                            Bundle bundle = new Bundle();
//                            bundle.putSerializable(EXTRA_MEETING_ENTITY, meeting);
//                            IntentUtils.startActivity(MeetingDetailActivity.this, MeetingAddActivity.class, bundle, true);
//                        }
//                        break;
//                    case 1:
//                        deleteMeeting(meeting);
//                        break;
//                    default:
//                        break;
//                }
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

//        if (isShowChangeMeeting) {
//
//            new ActionSheetDialog.ActionListSheetBuilder(MeetingDetailActivity.this)
//                    //    .addItem(getString(R.string.meeting_detail_show_qrcode))
//                    .addItem(getString(R.string.schedule_meeting_cancel))
//                    .setOnSheetItemClickListener(onSheetItemClickListener)
//                    .build()
//                    .show();
//        } else {
//            new ActionSheetDialog.ActionListSheetBuilder(MeetingDetailActivity.this)
//                    //    .addItem(getString(R.string.meeting_detail_show_qrcode))
//                    .addItem(getString(R.string.schedule_meeting_change))
//                    .addItem(getString(R.string.schedule_meeting_cancel))
//                    .setOnSheetItemClickListener(onSheetItemClickListener)
//                    .build()
//                    .show();
//        }
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
