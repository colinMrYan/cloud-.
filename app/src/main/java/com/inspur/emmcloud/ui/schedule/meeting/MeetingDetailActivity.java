package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by yufuchang on 2019/4/16.
 */
@ContentView(R.layout.activity_meeting_detail_new)
public class MeetingDetailActivity extends BaseActivity{

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
//        meetingTitleText.setText(meeting.getTitle());
//        meetingTimeText.setText(meeting.getStartTime() + meeting.getEndTime() + "");
//        meetingRemindText.setText(meeting.getRemindEvent());
//        meetingDistributionText.setText(meeting.getOwner());
//        meetingCreateTimeText.setText(meeting.getCreationTime() + "");
//        attendeeText.setText(meeting.getParticipants() );
//        meetingRecordHolderText.setText(meeting.getParticipants() );
//        meetingConferenceText.setText(meeting.getParticipants());
//        meetingNoteText.setText(meeting.getNote());
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_meeting_detail_more:
                showDialog();
//                finish();
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
//                .addItem(getString(R.string.take_photo))
//                .addItem(getString(R.string.clouddriver_select_photo))
//                .addItem(getString(R.string.clouddriver_select_file))
//                .addItem(getString(R.string.clouddriver_select_file))
//                .addItem(getString(R.string.clouddriver_select_file))
                .addItem("添加参会人")
                .addItem("展示签到二维码")
                .addItem("修改会议")
                .addItem("取消会议")
                .addItem("删除")
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
