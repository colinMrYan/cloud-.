package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.util.common.LogUtils;
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
        meeting = (Meeting) getIntent().getSerializableExtra("");
        if(meeting == null){
            meeting = new Meeting();
        }
        initViews();
    }

    private void initViews() {
        meetingTitleText.setText(meeting.getTitle());
        meetingTimeText.setText(getMeetingTime());
        meetingRemindText.setText(TimeUtils.getLeftTimeFromMeetingBegin(meeting.getRemindEventObj().getAdvanceTimeSpan()));
//        meetingDistributionText.setText(meeting.getOwner());
        meetingCreateTimeText.setText(getString(R.string.meeting_detail_create,TimeUtils.calendar2FormatString(this,
                TimeUtils.timeLong2Calendar(meeting.getCreationTime()), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE)));
        attendeeText.setText(getString(R.string.meeting_detail_attendee_num,meeting.getCommonParticipantList().get(0).getName(),meeting.getCommonParticipantList().size()));
        meetingRecordHolderText.setText(getRecordHolder());
        meetingConferenceText.setText(getConference());
        meetingNoteText.setText(meeting.getNote());
    }

    /**
     * 获取联络人
     * @return
     */
    private String getConference() {
        String recorder = "";
        List<Participant> participantList =  meeting.getCommonParticipantList();
        for(Participant participant : participantList){
            if(participant.getRole().equals(ROLE_RECORDER)){
                recorder = participant.getName();
                break;
            }
        }
        return recorder;
    }

    /**
     * 获取记录人
     * @return
     */
    private String getRecordHolder() {
        String conference = "";
        List<Participant> participantList =  meeting.getCommonParticipantList();
        for(Participant participant : participantList){
            if(participant.getRole().equals(ROLE_CONTACT)){
                conference = participant.getName();
                break;
            }
        }
        return conference;
    }

    private String getMeetingTime() {
        return  (meeting.getStartTime() + meeting.getEndTime() + "");
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
                LogUtils.YfcDebug("参会人");
                break;
            case R.id.rl_meeting_record_holder:
                LogUtils.YfcDebug("会议记录");
                break;
            case R.id.rl_meeting_conference:
                LogUtils.YfcDebug("会议联络");
                break;
            case R.id.rl_meeting_sign:
                LogUtils.YfcDebug("会议签到");
                break;
            case R.id.rl_meeting_summary:
                LogUtils.YfcDebug("会议纪要");
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
