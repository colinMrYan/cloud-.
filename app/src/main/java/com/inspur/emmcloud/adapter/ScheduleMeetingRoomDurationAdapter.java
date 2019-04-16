package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.inspur.emmcloud.bean.work.MeetingSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/15.
 */

public class ScheduleMeetingRoomDurationAdapter extends BaseAdapter {
    private Context context;
    private List<MeetingSchedule> meetingScheduleList = new ArrayList<>();

    public ScheduleMeetingRoomDurationAdapter(Context context, List<MeetingSchedule> meetingScheduleList) {
        this.meetingScheduleList = meetingScheduleList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return meetingScheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
//        // TODO Auto-generated method stub
//        LayoutInflater vi = LayoutInflater.from(context);
//        final MeetingSchedule meetingSchedule = meetingScheduleList
//                .get(position);
//        SimpleDateFormat format = TimeUtils.getFormat(context, TimeUtils.FORMAT_HOUR_MINUTE);
//        long beginTimeLong = meetingSchedule.getFrom();
//        long endTimeLong = meetingSchedule.getTo();
//        String beginTimeString = TimeUtils.getTime(beginTimeLong, format);
//        String endTimeString = TimeUtils.getTime(endTimeLong, format);
//        String timeSegment = beginTimeString + "-" + endTimeString;
//        if (meetingSchedule.getMeeting() == null) {
//            convertView = vi.inflate(
//                    R.layout.meeting_no_schedule_item_view, null);
//            ((TextView) convertView.findViewById(R.id.time_text))
//                    .setText(timeSegment + " " + getString(R.string.meeting_free));
//            convertView.findViewById(R.id.meeting_layout)
//                    .setOnClickListener(new View.OnClickListener() {
//
//                        @Override
//                        public void onClick(View v) {
//                            // TODO Auto-generated method stub
//                        }
//                    });
//        } else {
//            final Meeting meeting = meetingSchedule.getMeeting();
//            convertView = vi.inflate(R.layout.meeting_schedule_item_view,
//                    null);
//            ((TextView) convertView.findViewById(R.id.meeting_time_text))
//                    .setText(timeSegment);
//            String organizer = ContactUserCacheUtils.getUserName(meeting.getOrganizer());
//            ((TextView) convertView
//                    .findViewById(R.id.meeting_order_name_text))
//                    .setText(organizer);
//            ((TextView) convertView.findViewById(R.id.meeting_title_text))
//                    .setText(meeting.getTopic());
//
//            convertView.findViewById(R.id.meeting_layout)
//                    .setOnClickListener(new View.OnClickListener() {
//
//                        @Override
//                        public void onClick(View v) {
//                            // TODO Auto-generated method stub
//                            showMeetingInfo(meeting);
//                        }
//                    });
//
//            convertView.findViewById(R.id.meeting_layout)
//                    .setOnLongClickListener(new OnLongClickListener() {
//
//                        @Override
//                        public boolean onLongClick(View v) {
//                            if (meetingSchedule.getMeeting().getOrganizer().equals(MyApplication.getInstance().getUid())) {
//                                boolean isMeetingAdmin = PreferencesByUserAndTanentUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN, false);
//                                if (isMeetingAdmin) {
//                                    showDeleteMeetingDlg(meetingSchedule);
//                                }
//                            }
//                            return true;
//                        }
//
//                    });
//        }
        return convertView;
    }
}
