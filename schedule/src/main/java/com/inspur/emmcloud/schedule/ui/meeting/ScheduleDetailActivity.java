package com.inspur.emmcloud.schedule.ui.meeting;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.SysCalendarAndCloudPlusScheduleSyncUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.Location;
import com.inspur.emmcloud.schedule.bean.Participant;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.schedule.bean.meeting.ReplyAttendResult;
import com.inspur.emmcloud.schedule.ui.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.schedule.util.CalendarUtils;
import com.inspur.emmcloud.schedule.util.ScheduleCacheUtils;
import com.inspur.emmcloud.schedule.util.ScheduleGroupCreateUtils;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
    public static final String IS_FROM_MEETING_ROOM = "is_from_meeting_room";  // 是否来自会议室界面
    public static final String MEMBER_PAGE_STATE = "member_page_state";
    public static final int CHECK_STATE = 3;//查看人员
    private static final int SCHEDULE_ATTENDEE = 0;  //参与人
    private static final int SCHEDULE_RECORD_HOLDER = 1;  //记录人
    private static final int SCHEDULE_CONTACT = 2;        //联系人
    private static final int SCHEDULE_INVITE = 3;          //邀请人
    /**
     * 日程相关
     **/
    @BindView(R2.id.tv_schedule_title)
    TextView scheduleTitleText;
    @BindView(R2.id.tv_schedule_time)
    TextView scheduleTimeText;
    @BindView(R2.id.tv_schedule_remind)
    TextView scheduleRemindText;
    @BindView(R2.id.schedule_distribution_layout)
    View scheduleDistributionLayout;
    @BindView(R2.id.tv_schedule_distribution)
    TextView scheduleDistributionText;
    @BindView(R2.id.tv_schedule_create)
    TextView scheduleCreateTimeText;
    @BindView(R2.id.header_text)
    TextView headText;
    @BindView(R2.id.tv_attendee)
    TextView attendeeText;
    @BindView(R2.id.tv_location)
    TextView scheduleLocationText;
    @BindView(R2.id.rl_schedule_location)
    RelativeLayout scheduleLocationLayout;
    @BindView(R2.id.tv_schedule_record_holder)
    TextView scheduleRecordHolderText;
    @BindView(R2.id.tv_schedule_conference)
    TextView scheduleConferenceText;
    @BindView(R2.id.tv_schedule_note)
    TextView scheduleNoteText;
    @BindView(R2.id.rl_schedule_record_holder)
    RelativeLayout scheduleRecordHolderLayout;
    @BindView(R2.id.rl_schedule_conference)
    RelativeLayout scheduleConferenceLayout;
    @BindView(R2.id.rl_schedule_note)
    RelativeLayout scheduleNoteLayout;
    @BindView(R2.id.iv_schedule_detail_more)
    ImageView scheduleMoreImg;
    @BindView(R2.id.rl_schedule_attendee)
    RelativeLayout scheduleAttendLayout;
    @BindView(R2.id.tv_schedule_invite)
    TextView scheduleInviteText;
    @BindView(R2.id.rl_schedule_calendar)
    View scheduleCalendarTypeLayout;
    @BindView(R2.id.image_schedule_calendar_type)
    ImageView scheduleCalendarTypeImage;
    @BindView(R2.id.tv_schedule_calendar_type)
    TextView scheduleCalendarTypeText;  //日历类型
    @BindView(R2.id.rl_schedule_attend_status)
    RelativeLayout attendStatusLayout;
    @BindView(R2.id.tv_schedule_attend_status)
    TextView attendStatusText;
    ReplyAttendResult info = new ReplyAttendResult(); //参会答复
    @BindView(R2.id.rl_schedule_invite)
    RelativeLayout scheduleInviteLayout;
    private ScheduleAPIService scheduleApiService;
    private LoadingDialog loadingDlg;
    private boolean isHistorySchedule = false; //是否来自历史会议
    private boolean isFromMeetingRoom = false; //是否来自会议室列表
    private List<String> moreTextList = new ArrayList<>();
    private String chatGroupId; //群聊ID
    private String scheduleId;   //会议 日程Id
    private boolean isFromCalendar = false;   //是否来自日程
    private Schedule scheduleEvent = new Schedule();
    private boolean relatedPersonFlag, isScheduleAdmin, isScheduleCreater;    //会议相关人员  管理员  创建者

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleAPIService(this);
        scheduleApiService.setAPIInterface(new WebService());
        getIsMeetingAdmin();
        scheduleId = getIntent().getStringExtra(Constant.SCHEDULE_QUERY);   //解析通知字段获取id
        isFromMeetingRoom = getIntent().getBooleanExtra(IS_FROM_MEETING_ROOM, false);
        info.responseType = Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN; //默认参会状态未知
        if (!StringUtils.isBlank(scheduleId)) {
            getScheduleFromId(scheduleId);
        } else {
            scheduleEvent = (Schedule) getIntent().getSerializableExtra(EXTRA_SCHEDULE_ENTITY);
            scheduleId = scheduleEvent.getId();
            initViews();
        }
    }

    /**
     * 判断是否来自日程
     */
    private void initScheduleFrom() {
        if (scheduleEvent != null)
            isFromCalendar = !scheduleEvent.isMeeting();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_schedule_detail_latest;
    }

    @SuppressLint("StringFormatInvalid")
    private void initViews() {
        initScheduleFrom();
        isHistorySchedule = System.currentTimeMillis() > scheduleEvent.getEndTime();
        headText.setText(getString(isFromCalendar ? R.string.schedule_calendar_detail : R.string.schedule_meeting_booking_detail));
        scheduleTitleText.setText(scheduleEvent.getTitle());
        scheduleTimeText.setText(getString(R.string.schedule_meeting_detail_time, getScheduleTime()));
        scheduleRemindText.setText(getString(R.string.schedule_meeting_detail_remind, ScheduleAlertTimeActivity.getAlertTimeNameByTime(scheduleEvent.getRemindEventObj().getAdvanceTimeSpan(), scheduleEvent.getAllDay())));
        //来自会议
        scheduleInviteLayout.setVisibility(View.VISIBLE);
        scheduleAttendLayout.setVisibility(StringUtils.isBlank(getScheduleParticipant()) ? View.GONE : View.VISIBLE);   //参会人
        //仅有一个参会人  且是自己得情况
        if (scheduleEvent.getAllParticipantList().size() == 1 &&
                scheduleEvent.getAllParticipantList().get(0).getId().equals(BaseApplication.getInstance().getUid())) {
            scheduleAttendLayout.setVisibility(View.GONE);
        }
        if (!StringUtils.isBlank(scheduleEvent.getOwner())) { //邀请人
            ContactService contactService = Router.getInstance().getService(ContactService.class);
            ContactUser myInfo;
            if (contactService != null) {
                myInfo = contactService.getContactUserByUid(scheduleEvent.getOwner());
            } else {
                myInfo = new ContactUser();
            }
            String userName = myInfo.getName();
            scheduleInviteText.setText(getString(R.string.schedule_meeting_detail_inviter, userName));
        }
        scheduleInviteText.setVisibility(StringUtils.isBlank(scheduleEvent.getOwner()) ? View.GONE : View.VISIBLE);
        attendeeText.setText(getString(R.string.schedule_meeting_detail_attendee, getScheduleParticipant())); //参会人
        scheduleNoteText.setText(scheduleEvent.getNote());             //备注
        scheduleNoteLayout.setVisibility(StringUtil.isBlank(scheduleEvent.getNote()) ? View.GONE : View.VISIBLE);

        scheduleDistributionText.setVisibility(View.VISIBLE);
        scheduleDistributionText.setText(getScheduleCategory(scheduleEvent));
        if (StringUtils.isBlank(getScheduleCategory(scheduleEvent))) {
            scheduleDistributionLayout.setVisibility(View.GONE);
        }

        if (StringUtils.isBlank(scheduleEvent.getLocation())) {
            scheduleLocationLayout.setVisibility(View.GONE);
        } else {
            Location locationObj = scheduleEvent.getScheduleLocationObj();
            if (StringUtils.isBlank(locationObj.getBuilding()) && StringUtils.isBlank(locationObj.getDisplayName())) {
                scheduleLocationLayout.setVisibility(View.GONE);
            } else {
                String locationData = getString(R.string.schedule_every_detail_location) + scheduleEvent.getScheduleLocationObj().getBuilding() + " " + scheduleEvent.getScheduleLocationObj().getDisplayName();
                scheduleLocationText.setText(locationData);
            }
        }

        scheduleCreateTimeText.setText(getString(R.string.schedule_meeting_detail_create, TimeUtils.calendar2FormatString(this,
                TimeUtils.timeLong2Calendar(scheduleEvent.getCreationTime()), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE)));
        scheduleMoreImg.setVisibility((PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                false) || (scheduleEvent.getOwner().equals(BaseApplication.getInstance().getUid())) && System.currentTimeMillis() < scheduleEvent.getEndTime()) ? View.VISIBLE : View.GONE);
        attendStatusLayout.setVisibility((Calendar.getInstance().after(TimeUtils.timeLong2Calendar(scheduleEvent.getEndTime())) ||
                scheduleEvent.getOwner().equals(BaseApplication.getInstance().getUid())) ? View.GONE : View.VISIBLE);
        initScheduleType();
        initDiffStatus();
        if (isFromMeetingRoom) { //如果来自会议室列表  参会状态跟日程  更多  不显示
            scheduleCalendarTypeLayout.setVisibility(View.GONE);
            attendStatusLayout.setVisibility(View.GONE);
            scheduleMoreImg.setVisibility(View.GONE);
        }
    }

    /**
     * 从数据库获取日程数据
     */
    private void getDbCalendarFromId() {
        scheduleEvent = ScheduleCacheUtils.getDBScheduleById(this, scheduleId);
        if (scheduleEvent != null) {
            initViews();
        }
    }

    /**
     * 通过id获取网络数据
     */
    private void getScheduleFromId(String id) {
        if (NetUtils.isNetworkConnected(this)) {
            if (scheduleEvent == null || (isFromCalendar && TextUtils.isEmpty(scheduleEvent.getId()))) {  //如果日程缓存有数据则不显示loading
                loadingDlg.show();
            }
            scheduleApiService.getCalendarDataFromId(id);
        } else {
            finish();
        }
    }

    private void initDiffStatus() {

        //如果不是相关人员  隐藏
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

        isScheduleAdmin = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN, false);
        isScheduleCreater = scheduleEvent.getOwner().equals(BaseApplication.getInstance().getUid());
        if (relatedPersonFlag || isScheduleAdmin) {     //参与状态是否显示
            attendStatusLayout.setVisibility((isHistorySchedule || isScheduleCreater) ? View.GONE : View.VISIBLE);
        } else {
            attendStatusLayout.setVisibility(View.GONE);
        }

        moreTextList.clear();
        if (isFromCalendar) {  //来自日程
            if (scheduleEvent.canModify()) {
                moreTextList.add(getString(R.string.schedule_calendar_modify));
            }
            if (scheduleEvent.canDelete()) {
                moreTextList.add(getString(R.string.schedule_calendar_delete));
            }

            if (relatedPersonFlag && (scheduleEvent.canModify() || scheduleEvent.canDelete())) {
                scheduleMoreImg.setVisibility(View.VISIBLE);
            } else {
                scheduleMoreImg.setVisibility(View.GONE);
            }
            scheduleMoreImg.setVisibility(moreTextList.size() > 0 ? View.VISIBLE : View.GONE);
            return;
        }

        if (relatedPersonFlag || isScheduleAdmin) {
            scheduleMoreImg.setVisibility(View.VISIBLE);

            //管理员不显示发起群聊 (创建者跟参会人)
            if (relatedPersonFlag && WebServiceRouterManager.getInstance().isV1xVersionChat()) {
                moreTextList.add(getString(R.string.schedule_meeting_create_group_chat)); //会议群聊
                scheduleApiService.getCalendarBindChat(scheduleId);
            }

            //管理员 并且不是创建者 (管理员只能删除会议  创建者可以删除和修改会议)
            final boolean isShowChangeMeeting = isScheduleAdmin && !isScheduleCreater;

            //仅有管理员跟创建者有此逻辑
            if (isScheduleAdmin || isScheduleCreater) {
                if (isShowChangeMeeting) {   //仅是管理员
                    moreTextList.add(getString(R.string.schedule_meeting_cancel));
                } else {    //创建者 or 创建者同时管理员
                    if (!isHistorySchedule) {
                        moreTextList.add(getString(R.string.schedule_meeting_change));
                    }
                    moreTextList.add(getString(R.string.schedule_meeting_cancel));
                }
            }
            scheduleMoreImg.setVisibility(moreTextList.size() > 0 ? View.VISIBLE : View.GONE);
        } else {
            scheduleMoreImg.setVisibility(View.GONE);
        }
    }

    private void initScheduleType() {
        scheduleCalendarTypeImage.setImageResource(CalendarUtils.getCalendarIconResId(scheduleEvent));
        scheduleCalendarTypeText.setText(CalendarUtils.getCalendarName(scheduleEvent));
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
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            if (!PreferencesByUserAndTanentUtils.isKeyExist(BaseApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN)) {
                loadingDlg.show();
            }
            scheduleApiService.getIsMeetingAdmin(BaseApplication.getInstance().getUid());
        }
    }

    private String getScheduleCategory(Schedule schedule) {
        String scheduleCategory = "";
        if (!StringUtils.isBlank(schedule.getOwner()) && BaseApplication.getInstance().getUid().equals(schedule.getOwner())) {
            scheduleCategory = getString(R.string.schedule_meeting_my_create);
        } else {
            List<Participant> participantList = schedule.getAllParticipantList();
            for (int i = 0; i < participantList.size(); i++) {
                if (participantList.get(i).getId().equals(BaseApplication.getInstance().getUid())) {
                    scheduleCategory = getString(R.string.schedule_meeting_my_take_part_in);
                    break;
                }
            }
        }
        return scheduleCategory;
    }

    private String getScheduleParticipant() {
        List<Participant> participantList = deleteRepeatData(scheduleEvent.getAllParticipantList());
        if (participantList.size() == 0) {
            return "";
        }
        ContactService contactService = Router.getInstance().getService(ContactService.class);
        Participant participant = participantList.get(0);
        if (participantList.size() == 1) {
            ContactUser isHaveInfo = contactService != null ? contactService.getContactUserByUid(participant.getId()) : null;
            if (StringUtils.isBlank(participant.getId()) || isHaveInfo == null) { //id为空但是有name的情况
                return participant.getName() + participant.getEmail();
            }
            ContactUser contactUser = contactService != null ? contactService.getContactUserByUid(participantList.get(0).getId()) : null;
            if (contactUser != null) {
                return contactUser.getName();
            }
            return null;
        } else {
            ContactUser isHaveInfo = contactService != null ? contactService.getContactUserByUid(participant.getId()) : null;
            if (StringUtils.isBlank(participantList.get(0).getId()) || isHaveInfo == null) {  //id为空但是有name的情况
                return getString(R.string.schedule_meeting_detail_attendee_num,
                        participantList.get(0).getName() + participantList.get(0).getEmail(),
                        participantList.size());
            }
            String name = "";
            ContactUser contactUser = contactService != null ? contactService.getContactUserByUid(participantList.get(0).getId()) : null;
            if (contactUser != null) {
                name = contactUser.getName();
            }
            return getString(R.string.schedule_meeting_detail_attendee_num, name, participantList.size());
        }
    }

    /**
     * 获取会议起止时间
     *
     * @return
     */
    private String getScheduleTime() {
        String duringTime = "";
        long startTime = scheduleEvent.getStartTime();
        long endTime = scheduleEvent.getEndTime();

        if (TimeUtils.isSameDay(TimeUtils.timeLong2Calendar(startTime), TimeUtils.timeLong2Calendar(endTime))) {
            duringTime = TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(startTime), TimeUtils.FORMAT_MONTH_DAY) + " " +
                    TimeUtils.getWeekDay(this, TimeUtils.timeLong2Calendar(startTime)) + " " +
                    TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(startTime), TimeUtils.FORMAT_HOUR_MINUTE) +
                    " - " + TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(endTime), TimeUtils.FORMAT_HOUR_MINUTE);
            if (scheduleEvent.getAllDay()) {
                duringTime = TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(startTime), TimeUtils.FORMAT_MONTH_DAY) + " " +
                        TimeUtils.getWeekDay(this, TimeUtils.timeLong2Calendar(startTime));
            }
        } else {
            duringTime = TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(startTime), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE) +
                    " - " + TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(endTime), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
            if (scheduleEvent.getAllDay()) {
                long tempStartTime = startTime;
                long tempEndTime = endTime - 1 * 60 * 1000;
                long difference = tempEndTime - tempStartTime;
                if (difference < 24 * 3600 * 1000) {
                    duringTime = TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(tempStartTime), TimeUtils.FORMAT_MONTH_DAY) + " " +
                            TimeUtils.getWeekDay(this, TimeUtils.timeLong2Calendar(tempStartTime));
                } else {
                    duringTime = TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(tempStartTime), TimeUtils.FORMAT_MONTH_DAY) +
                            " - " + TimeUtils.calendar2FormatString(this, TimeUtils.timeLong2Calendar(tempEndTime), TimeUtils.FORMAT_MONTH_DAY);
                }
            }
        }
        return duringTime;
    }

    public void onClick(View v) {
        Bundle bundle = new Bundle();
        if (scheduleEvent != null) {
            LogUtils.LbcDebug("schedule" + JSONUtils.toJSONString(scheduleEvent));
        } else {
            LogUtils.LbcDebug("schedule == null");
        }
        bundle.putSerializable(EXTRA_SCHEDULE_ENTITY, scheduleEvent);
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();
        } else if (i == R.id.iv_schedule_detail_more) {
            showOperationDialog();
        } else if (i == R.id.rl_schedule_attendee ||
                i == R.id.rl_schedule_record_holder ||
                i == R.id.rl_schedule_conference) {
            if (!relatedPersonFlag && !isScheduleCreater && !isScheduleAdmin) {
                startMembersActivity(SCHEDULE_ATTENDEE);
            } else if (scheduleEvent != null) {
                IntentUtils.startActivity(this, MeetingAttendeeStateActivity.class, bundle);
            }
        } else if (i == R.id.rl_schedule_invite) {
            startMembersActivity(SCHEDULE_INVITE);
        } else if (i == R.id.rl_schedule_attend_status) {
            Intent replyIntent = new Intent(this, ScheduleDetailReplyActivity.class);
            replyIntent.putExtra("OriginReplyData", info);
            replyIntent.putExtra(Constant.OPEN_SCHEDULE_DETAIL, scheduleEvent);
            startActivityForResult(replyIntent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 100) {
            info = (ReplyAttendResult) data.getSerializableExtra("ReplyResult");
            attendStatusText.setText(info.content);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED, null));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startMembersActivity(int type) {
        List<String> uidList = new ArrayList<>();
        Bundle bundle = new Bundle();
        switch (type) {
            case SCHEDULE_ATTENDEE:
                uidList = getUidList(deleteRepeatData(scheduleEvent.getAllParticipantList()));
                bundle.putString("title", getString(R.string.schedule_meeting_add_attendee_title));
                break;
            case SCHEDULE_RECORD_HOLDER:
                uidList = getUidList(scheduleEvent.getRecorderParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_record_holder_title));
                break;
            case SCHEDULE_CONTACT:
                uidList = getUidList(scheduleEvent.getRoleParticipantList());
                bundle.putString("title", getString(R.string.schedule_meeting_add_conference_title));
                break;
            case SCHEDULE_INVITE:
                uidList.add(scheduleEvent.getOwner());
                bundle.putString("title", getString(R.string.schedule_meeting_detail_invite));
                break;
            default:
                break;
        }
        bundle.putStringArrayList("uidList", (ArrayList<String>) uidList);
        bundle.putInt(MEMBER_PAGE_STATE, CHECK_STATE);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_MEMBER).with(bundle).navigation(ScheduleDetailActivity.this);
    }

    //list去重
    private List<Participant> deleteRepeatData(List<Participant> list) {
        if (list == null) return null;
        //不把邀请人加到参会人里
        Iterator<Participant> iterator = list.iterator();
        while (iterator.hasNext()) {
            Participant item = iterator.next();
            ContactService contactService = Router.getInstance().getService(ContactService.class);
            ContactUser user = contactService != null ? contactService.getContactUserByUid(item.getId()) : null;
            if (user == null) {
                iterator.remove();
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
                if (tag.equals(getString(isFromCalendar ? R.string.schedule_calendar_modify : R.string.schedule_meeting_change))) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ScheduleAddActivity.EXTRA_SCHEDULE_CALENDAR_EVENT, scheduleEvent);
                    IntentUtils.startActivity(ScheduleDetailActivity.this, ScheduleAddActivity.class, bundle, true);
                } else if (tag.equals(getString(isFromCalendar ? R.string.schedule_calendar_delete : R.string.schedule_meeting_cancel))) {
                    showConfirmClearDialog(scheduleEvent);
                } else if (tag.equals(getString(R.string.schedule_meeting_create_group_chat))) {
                    new ScheduleGroupCreateUtils().startGroupChat(ScheduleDetailActivity.this, scheduleEvent, chatGroupId, null);
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
                .setMessage(getString(isFromCalendar ? R.string.schedule_calendar_cancel_the_schedule : R.string.schedule_meeting_cancel_the_meeting))
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

    class WebService extends ScheduleAPIInterfaceImpl {

        @Override
        public void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin) {
            LoadingDialog.dimissDlg(loadingDlg);
            PreferencesByUserAndTanentUtils.putBoolean(BaseApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                    getIsAdmin.isAdmin());
        }

        @Override
        public void returnIsMeetingAdminFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
        }

        @Override
        public void returnDeleteScheduleSuccess(String scheduleId) {
            LoadingDialog.dimissDlg(loadingDlg);
            SysCalendarAndCloudPlusScheduleSyncUtils.deleteCalendarEvent(ScheduleDetailActivity.this, scheduleId);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_CHANGED, null));
            finish();
        }

        @Override
        public void returnDeleteScheduleFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (!isFromCalendar) {
                WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
            }
            super.returnDeleteScheduleFail(error, errorCode);
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
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
            LoadingDialog.dimissDlg(loadingDlg);
            finish();
        }
    }
}
