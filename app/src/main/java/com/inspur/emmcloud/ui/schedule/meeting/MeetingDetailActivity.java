package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.ui.schedule.ScheduleAlertTimeActivity;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * Created by yufuchang on 2019/4/16.
 */
@ContentView(R.layout.activity_meeting_detail_new)
public class MeetingDetailActivity extends BaseActivity{

    private static final String ROLE_RECORDER = "recorder";
    private static final String ROLE_CONTACT = "contact";
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
    @ViewInject(R.id.tv_meeting_record_holder)
    private TextView meetingRecordHolderText;
    @ViewInject(R.id.tv_meeting_conference)
    private TextView meetingConferenceText;
    @ViewInject(R.id.tv_meeting_note)
    private TextView meetingNoteText;
    private Meeting meeting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        meeting = (Meeting) getIntent().getSerializableExtra(EXTRA_MEETING_ENTITY);
        initViews();
    }

    private void initViews() {
        meetingTitleText.setText(meeting.getTitle());
        meetingTimeText.setText(getString(R.string.meeting_detail_time,getMeetingTime()));
        meetingRemindText.setText(getString(R.string.meeting_detail_remind,ScheduleAlertTimeActivity.getAlertTimeNameByTime(meeting.getRemindEventObj().getAdvanceTimeSpan(),meeting.getAllDay())));
//        meetingDistributionText.setText(meeting.getOwner());
        meetingCreateTimeText.setText(getString(R.string.meeting_detail_create,TimeUtils.calendar2FormatString(this,
                TimeUtils.timeLong2Calendar(meeting.getCreationTime()), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE)));
        attendeeText.setText(getString(R.string.meeting_detail_attendee,getAttendee()));
        meetingRecordHolderText.setText(getRecordHolder());
        meetingConferenceText.setText(getContact());
        meetingNoteText.setText(meeting.getNote());
    }

    /**
     * 获取参会人员
     * @return
     */
    private String getAttendee() {
        String attendee = "";
        List<Participant> participantList =  meeting.getCommonParticipantList();
        if(participantList.size() == 0){
            attendee = "";
        }else if(participantList.size() == 1){
            attendee = participantList.get(0).getName();
        }else{
            attendee = getString(R.string.meeting_detail_attendee_num,participantList.get(0).getName(),participantList.size());
        }
        return attendee;
    }

    /**
     * 获取联络人
     * @return
     */
    private String getContact() {
        List<Participant> participantList = meeting.getRoleParticipantList();
        return getString(R.string.meeting_detail_conference,
                getString(R.string.meeting_detail_attendee_num,
                        participantList.get(0).getName(),
                        participantList.size()));
    }

    /**
     * 获取记录人
     * @return
     */
    private String getRecordHolder() {
        List<Participant> participantList = meeting.getRecorderParticipantList();
        return getString(R.string.meeting_detail_record_holder,
                getString(R.string.meeting_detail_attendee_num,
                        participantList.get(0).getName(),
                        participantList.size()));
    }

    /**
     * 获取会议起止时间
     * @return
     */
    private String getMeetingTime() {
        String duringTime = "";
        long startTime = meeting.getStartTime();
        long endTime = meeting.getEndTime();
        if(TimeUtils.isSameDay(TimeUtils.timeLong2Calendar(startTime),TimeUtils.timeLong2Calendar(endTime))){
            duringTime = TimeUtils.calendar2FormatString(this,TimeUtils.timeLong2Calendar(startTime),TimeUtils.FORMAT_MONTH_DAY) + " " +
                    TimeUtils.getWeekDay(this,TimeUtils.timeLong2Calendar(startTime)) + " " +
                    TimeUtils.calendar2FormatString(this,TimeUtils.timeLong2Calendar(startTime),TimeUtils.FORMAT_HOUR_MINUTE) +
                    " - "+TimeUtils.calendar2FormatString(this,TimeUtils.timeLong2Calendar(endTime),TimeUtils.FORMAT_HOUR_MINUTE);
        }else{
            //先按同一天算
            duringTime = TimeUtils.calendar2FormatString(this,TimeUtils.timeLong2Calendar(startTime),TimeUtils.FORMAT_MONTH_DAY) +
                    TimeUtils.getWeekDay(this,TimeUtils.timeLong2Calendar(startTime)) +
                    TimeUtils.calendar2FormatString(this,TimeUtils.timeLong2Calendar(startTime),TimeUtils.FORMAT_HOUR_MINUTE) +
                    " - "+TimeUtils.calendar2FormatString(this,TimeUtils.timeLong2Calendar(endTime),TimeUtils.FORMAT_HOUR_MINUTE);
        }
        return  duringTime;
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_meeting_detail_more:
                showDialog();
                break;
            case R.id.rl_meeting_attendee:
                break;
            case R.id.rl_meeting_record_holder:
                break;
            case R.id.rl_meeting_conference:
                break;
            case R.id.rl_meeting_sign:
                break;
            case R.id.rl_meeting_summary:
                break;
        }
    }

    private void showDialog() {
        new ActionSheetDialog.ActionListSheetBuilder(MeetingDetailActivity.this)
                .addItem(getString(R.string.meeting_detail_add_participant))
                .addItem(getString(R.string.meeting_detail_show_qrcode))
                .addItem(getString(R.string.meeting_detail_change_meeting))
                .addItem(getString(R.string.meeting_cancel))
                .addItem(getString(R.string.delete))
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:

                                break;
                            case 1:

                                break;
                            case 2:

                                break;
                            case 3:

                                break;
                            case 4:

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
}
