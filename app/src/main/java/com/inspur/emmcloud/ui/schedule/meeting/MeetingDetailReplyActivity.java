package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.bean.schedule.Participant;
import com.inspur.emmcloud.bean.schedule.meeting.ReplyAttendResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeetingDetailReplyActivity extends BaseActivity {
    @BindView(R.id.lv_meeting_reply)
    ListView listView;
    private String meetingId;
    private List<ReplyAttendResult> dataList = new ArrayList<>();
    private List<String> responseTypeList = new ArrayList<>();
    private LoadingDialog loadingDlg;
    private ScheduleApiService scheduleApiService;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_detail_reply;
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleApiService(this);
        scheduleApiService.setAPIInterface(new WebService());

        initData();
        initView();
    }

    private void initData() {
        ReplyAttendResult originData = (ReplyAttendResult) getIntent().getSerializableExtra("OriginReplyData");
        meetingId = getIntent().getStringExtra("meetingId");
        if (originData.responseType.equals(Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN)) {
            ReplyAttendResult info = new ReplyAttendResult();
            info.content = getString(R.string.schedule_meeting_attend_unknown);
            info.responseType = Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN;
            info.isSelect = true;
            responseTypeList.add(info.responseType);
            dataList.add(info);
        }

        String[] contents = new String[]{getString(R.string.schedule_meeting_attend_accept),
                getString(R.string.schedule_meeting_attend_ignore), getString(R.string.schedule_meeting_attend_reject)};
        String[] responseTypes = new String[]{Participant.CALENDAR_RESPONSE_TYPE_ACCEPT,
                Participant.CALENDAR_RESPONSE_TYPE_TENTATIVE, Participant.CALENDAR_RESPONSE_TYPE_DECLINE};
        for (int i = 0; i < contents.length; i++) {
            ReplyAttendResult info = new ReplyAttendResult();
            info.content = contents[i];
            info.responseType = responseTypes[i];
            if (originData.responseType.equals(responseTypes[i])) {
                info.isSelect = true;
            } else {
                info.isSelect = false;
            }
            responseTypeList.add(info.responseType);
            dataList.add(info);
        }
    }

    private void initView() {

        final MeetingReplyAdapter adapter = new MeetingReplyAdapter(this, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!dataList.get(position).isSelect) {  //已选中的点击无反应  未知无反应
                    loadingDlg.show();
                    scheduleApiService.setMeetingAttendStatus(meetingId, responseTypeList.get(position));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    class MeetingReplyAdapter extends BaseAdapter {
        List<ReplyAttendResult> list = new ArrayList<>();
        private Context context;    //方便以后改动

        public MeetingReplyAdapter(Context context, List<ReplyAttendResult> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.meeting_reply_item, null);
            }
            ReplyAttendResult info = dataList.get(position);
            ((TextView) convertView.findViewById(R.id.tv_meeting_reply)).setText(info.content);
            convertView.findViewById(R.id.iv_meeting_reply_selected).setVisibility(info.isSelect ? View.VISIBLE : View.INVISIBLE);
            convertView.findViewById(R.id.item_meeting_reply_space)
                    .setVisibility((dataList.size() == 4 && position == 0) ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnAttendMeetingStatusSuccess(String result, String responseType) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            //得到被选中的选项信息
            ReplyAttendResult item = new ReplyAttendResult();
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i).responseType.equals(responseType)) {
                    item = dataList.get(i);
                    item.isSelect = true;
                } else {
                    item.isSelect = false;
                }
            }

            Intent intent = new Intent();
            intent.putExtra("AttendReplyStatus", item.content);
            intent.putExtra("ReplyResult", item);
            setResult(100, intent);
            finish();
        }

        @Override
        public void returnAttendMeetingStatusFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
        }
    }
}
