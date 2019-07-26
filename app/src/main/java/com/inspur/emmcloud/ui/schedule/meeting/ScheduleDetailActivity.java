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
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.ReplyAttendResult;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.privates.CalendarUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ScheduleCacheUtils;

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
public class ScheduleDetailActivity extends BaseActivity {

    public static final String EXTRA_SCHEDULE_ENTITY = "extra_schedule_entity";
    private static final int MEETING_ATTENDEE = 0;
    private static final int MEETING_RECORD_HOLDER = 1;
    private static final int MEETING_CONTACT = 2;
    private static final int MEETING_INVITE = 3;
    private static final String TYPE_DEFAULT = "default";
    private static final String TYPE_MEETING = "meeting";
    private static final String TYPE_EXCHANGE = "exchange";
    private static final String TYPE_WEBEX = "webex";
    /**
     * 日程相关
     **/
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
    @BindView(R.id.header_text)
    TextView headText;
    @BindView(R.id.tv_attendee)
    TextView attendeeText;
    @BindView(R.id.tv_location)
    TextView meetingLocationText;
    @BindView(R.id.rl_meeting_location)
    RelativeLayout meetingLocationLayout;
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
    @BindView(R.id.rl_meeting_attendee)
    RelativeLayout meetingAttendLayout;
    @BindView(R.id.tv_meeting_invite)
    TextView meetingInviteText;
    @BindView(R.id.image_meeting_calendar_type)
    ImageView meetingCalendarTypeImage;
    @BindView(R.id.tv_meeting_calendar_type)
    TextView meetingCalendarTypeText;  //日历类型
    @BindView(R.id.rl_meeting_attend_status)
    RelativeLayout attendStatusLayout;
    @BindView(R.id.tv_meeting_attend_status)
    TextView attendStatusText;
    ReplyAttendResult info = new ReplyAttendResult(); //参会答复
    @BindView(R.id.rl_meeting_invite)
    RelativeLayout meetingInviteLayout;
    private ScheduleApiService scheduleApiService;
    private LoadingDialog loadingDlg;
    private boolean isHistoryMeeting = false; //是否来自历史会议
    private List<String> moreTextList = new ArrayList<>();
    private String chatGroupId; //群聊ID
    private String meetingId, calendarId;   //会议id  日程Id
    private boolean isFromCalendar = false;   //是否来自日程
    private Schedule scheduleEvent = new Schedule();


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleApiService(this);
        scheduleApiService.setAPIInterface(new WebService());
        meetingId = getIntent().getStringExtra(Constant.SCHEDULE_QUERY); //来自通知
        scheduleEvent = (Schedule) getIntent().getSerializableExtra(EXTRA_SCHEDULE_ENTITY); //来自列表
        isHistoryMeeting = getIntent().getBooleanExtra(Constant.EXTRA_IS_HISTORY_MEETING, false);
        info.responseType = Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN; //默认参会状态未知
        isFromCalendar = getIntent().getBooleanExtra(Constant.EXTRA_IS_FROM_CALENDAR, false);
        headText.setText(getString(isFromCalendar ? R.string.schedule_calendar_detail : R.string.schedule_meeting_booking_detail));
        if (!isFromCalendar) {      //来自会议
            getIsMeetingAdmin();
            if (!TextUtils.isEmpty(meetingId)) {    //id不为空是从网络获取数据  来自通知
                getMeetingFromId(meetingId);
            } else {                                //id为空是走之前逻辑
                initViews();
            }
            if (StringUtils.isBlank(meetingId)) {
                meetingId = scheduleEvent.getId();
            }
            scheduleApiService.getCalendarBindChat(meetingId);
        } else { //来自日程
            calendarId = getIntent().getStringExtra(Constant.SCHEDULE_QUERY);   //解析通知字段获取id
            if (!TextUtils.isEmpty(calendarId)) {        //来自通知
                getDbCalendarFromId();
                getNetCalendarFromId();
            } else if (getIntent().hasExtra(EXTRA_SCHEDULE_ENTITY)) {  //通知没有，列表页跳转过来
                scheduleEvent = (Schedule) getIntent().getSerializableExtra(EXTRA_SCHEDULE_ENTITY);
                initViews();
            }
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_schedule_detail_latest;
    }

    @SuppressLint("StringFormatInvalid")
    private void initViews() {
        isFromCalendar = getIntent().getBooleanExtra(Constant.EXTRA_IS_FROM_CALENDAR, false);
        meetingTitleText.setText(scheduleEvent.getTitle());
        meetingTimeText.setText(getString(R.string.meeting_detail_time, getMeetingTime()));
        meetingRemindText.setText(getString(R.string.meeting_detail_remind, ScheduleAlertTimeActivity.getAlertTimeNameByTime(scheduleEvent.getRemindEventObj().getAdvanceTimeSpan(), scheduleEvent.getAllDay())));
//        meetingDistributionText.setText(meeting.getOwner());
        if (isFromCalendar) {  //来自日历
            meetingInviteLayout.setVisibility(View.GONE);   //邀请人
            meetingNoteLayout.setVisibility(View.GONE);     //备注
            meetingAttendLayout.setVisibility(View.GONE);   //参会人
        } else {                //来自会议
            meetingInviteLayout.setVisibility(View.VISIBLE);
            meetingAttendLayout.setVisibility(StringUtils.isBlank(getMeetingParticipant()) ? View.GONE : View.VISIBLE);   //参会人
            if (!StringUtils.isBlank(scheduleEvent.getOwner())) { //邀请人
                String userName = ContactUserCacheUtils.getUserName(scheduleEvent.getOwner());
                meetingInviteText.setText(getString(R.string.meeting_detail_inviter, userName));
            }
            meetingInviteText.setVisibility(StringUtils.isBlank(scheduleEvent.getOwner()) ? View.GONE : View.VISIBLE);
            attendeeText.setText(getString(R.string.meeting_detail_attendee, getMeetingParticipant())); //参会人
            meetingNoteText.setText(scheduleEvent.getNote());             //备注
            meetingNoteLayout.setVisibility(StringUtil.isBlank(scheduleEvent.getNote()) ? View.GONE : View.VISIBLE);
        }

        meetingDistributionText.setVisibility(View.VISIBLE);
        meetingDistributionText.setText(getMeetingCategory(scheduleEvent));
        if (StringUtils.isBlank(getMeetingCategory(scheduleEvent))) {
            meetingDistributionLayout.setVisibility(View.GONE);
        }

        if (StringUtils.isBlank(scheduleEvent.getLocation())) {
            meetingLocationLayout.setVisibility(View.GONE);
        } else {
            String locationData = getString(R.string.meeting_detail_location) + scheduleEvent.getScheduleLocationObj().getBuilding() + " " + scheduleEvent.getScheduleLocationObj().getDisplayName();
            meetingLocationText.setText(locationData);
        }

        meetingCreateTimeText.setText(getString(R.string.meeting_detail_create, TimeUtils.calendar2FormatString(this,
                TimeUtils.timeLong2Calendar(scheduleEvent.getCreationTime()), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE)));


        meetingMoreImg.setVisibility((PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                false) || (scheduleEvent.getOwner().equals(MyApplication.getInstance().getUid())) && System.currentTimeMillis() < scheduleEvent.getEndTime()) ? View.VISIBLE : View.GONE);
        initScheduleType();
        initDiffStatus();
    }

    /**
     * 从数据库获取日程数据
     */
    private void getDbCalendarFromId() {
        scheduleEvent = ScheduleCacheUtils.getDBScheduleById(this, calendarId);
        if (scheduleEvent != null) {
            initViews();
        }
    }

    /**
     * 从网络获取日程数据
     */
    private void getNetCalendarFromId() {
        if (NetUtils.isNetworkConnected(this)) {
            if (!TextUtils.isEmpty(calendarId)) {
                if (scheduleEvent == null || TextUtils.isEmpty(scheduleEvent.getId())) { //如果缓存有数据则不显示loading
                    loadingDlg.show();
                }
                scheduleApiService.getCalendarDataFromId(calendarId);
            }
        } else {
            ToastUtils.show(this, "");
        }
    }

    private void initDiffStatus() {
        if (isFromCalendar) {  //来自日程
            if (scheduleEvent.canModify()) {
                moreTextList.add(getString(R.string.schedule_calendar_modify));
            }
            if (scheduleEvent.canDelete()) {
                moreTextList.add(getString(R.string.schedule_calendar_delete));
            }

            if (!scheduleEvent.canModify() && !scheduleEvent.canDelete()) {
                meetingMoreImg.setVisibility(View.GONE);
            } else {
                meetingMoreImg.setVisibility(View.VISIBLE);
            }
            return;
        }
        //如果不是相关人员  隐藏
        boolean relatedPersonFlag = false;
        List<Participant> list = scheduleEvent.getAllParticipantList();
        Participant mParticipant = null;
        for (Participant item : list) {
            if (BaseApplication.getInstance().getUid().equals(item.getId())) {
                info.responseType = item.getResponseType();
                mParticipant = item;
                relatedPersonFlag = true;
            }
        }

        initAttendStatus(mParticipant);

        if (BaseApplication.getInstance().getUid().equals(scheduleEvent.getOwner())) {
            relatedPersonFlag = true;
        }
        boolean isMeetingAdmin = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN, false);
        boolean isMeetingCreater = scheduleEvent.getOwner().equals(MyApplication.getInstance().getUid());
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
        meetingCalendarTypeImage.setImageResource(CalendarUtils.getCalendarIconResId(scheduleEvent));
        meetingCalendarTypeText.setText(CalendarUtils.getCalendarName(scheduleEvent));
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
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            if (!PreferencesByUserAndTanentUtils.isKeyExist(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN)) {
                loadingDlg.show();
            }
            scheduleApiService.getIsMeetingAdmin(MyApplication.getInstance().getUid());
        }
    }

    private String getMeetingCategory(Schedule schedule) {
        String meetingCategory = "";
        if (!StringUtils.isBlank(schedule.getOwner()) && MyApplication.getInstance().getUid().equals(schedule.getOwner())) {
            meetingCategory = getString(R.string.schedule_meeting_my_create);
        } else {
            List<Participant> participantList = schedule.getAllParticipantList();
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
            if (scheduleEvent == null) {
                loadingDlg.show();
            }
            scheduleApiService.getMeetingDataFromId(id);
        } else {
            ToastUtils.show(this, "");
        }
    }

    private String getMeetingParticipant() {
        List<Participant> participantList = deleteRepeatData(scheduleEvent.getAllParticipantList());
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
        long startTime = scheduleEvent.getStartTime();
        long endTime = scheduleEvent.getEndTime();
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
        if (scheduleEvent != null) {
            LogUtils.LbcDebug("meeting" + JSONUtils.toJSONString(scheduleEvent));
        } else {
            LogUtils.LbcDebug("meeting == null");
        }
        bundle.putSerializable(EXTRA_SCHEDULE_ENTITY, scheduleEvent);
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_meeting_detail_more:
                showOperationDialog();
                break;
            case R.id.rl_meeting_attendee:
            case R.id.rl_meeting_record_holder:
            case R.id.rl_meeting_conference:
                if (scheduleEvent != null)
                    IntentUtils.startActivity(this, MeetingAttendeeStateActivity.class, bundle);
                break;
            case R.id.rl_meeting_invite:
                startMembersActivity(MEETING_INVITE);
                break;
            case R.id.rl_meeting_attend_status:     //参会答复
                Intent replyIntent = new Intent(this, ScheduleDetailReplyActivity.class);
                replyIntent.putExtra("OriginReplyData", info);
                replyIntent.putExtra(Constant.SCHEDULE_DETAIL, scheduleEvent);
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
                uidList = getUidList(scheduleEvent.getCommonParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_attendee_title));
                break;
            case MEETING_RECORD_HOLDER:
                uidList = getUidList(scheduleEvent.getRecorderParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_record_holder_title));
                break;
            case MEETING_CONTACT:
                uidList = getUidList(scheduleEvent.getRoleParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_conference_title));
                break;
            case MEETING_INVITE:
                uidList.add(scheduleEvent.getOwner());
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
        //不把邀请人加到参会人里
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
                    bundle.putSerializable(ScheduleAddActivity.EXTRA_SCHEDULE_CALENDAR_EVENT, scheduleEvent);
                    bundle.putBoolean(ScheduleAddActivity.EXTRA_EVENT_TYPE_FROM_MEETING, !scheduleEvent.getType().equals("default"));
                    IntentUtils.startActivity(ScheduleDetailActivity.this, ScheduleAddActivity.class, bundle, true);
                } else if (tag.equals(getString(R.string.schedule_meeting_cancel))) {
                    showConfirmClearDialog(scheduleEvent);
                } else if (tag.equals(getString(R.string.message_create_group))) {
                    new ChatCreateUtils().startGroupChat(ScheduleDetailActivity.this, scheduleEvent, chatGroupId, null);
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
    private void showConfirmClearDialog(final Schedule schedule) {
        new CustomDialog.MessageDialogBuilder(ScheduleDetailActivity.this)
                .setMessage(getString(isFromCalendar ? R.string.calendar_cancel_the_schedule : R.string.meeting_cancel_the_meeting))
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
                        delSchedule();
                        finish();
                    }
                })
                .show();
    }

    /**
     * 刪除日程
     **/
    private void delSchedule() {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            scheduleApiService.deleteSchedule(scheduleEvent);
        }
    }

    /**
     * 删除会议
     */
//    private void deleteMeeting(Schedule meeting) {
//        if (NetUtils.isNetworkConnected(this)) {
//            loadingDlg.show();
//            scheduleApiService.deleteMeeting(meeting);
//        }
//    }

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
        public void returnDeleteScheduleSuccess(String scheduleId) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (isFromCalendar) {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED, null));
            } else {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_MEETING_DATA_CHANGED, null));
            }
            finish();
        }

        @Override
        public void returnDeleteScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (!isFromCalendar) {
                WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            }
            super.returnDeleteScheduleFail(error, errorCode);
        }

        @Override
        public void returnMeetingDataFromIdSuccess(Meeting meetingData) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (meetingData != null) {
                scheduleEvent = meetingData;
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

        @Override
        public void returnScheduleDataFromIdSuccess(Schedule schedule) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (schedule != null) {
                scheduleEvent = schedule;
                initViews();
            }
        }

        @Override
        public void returnScheduleDataFromIdFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            finish();
        }
    }
}
