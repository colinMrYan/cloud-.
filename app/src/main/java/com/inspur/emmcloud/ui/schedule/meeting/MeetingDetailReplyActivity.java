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

        init();
    }

    private void init() {
        ReplyAttendResult originData = (ReplyAttendResult) getIntent().getSerializableExtra("OriginReplyData");
        meetingId = getIntent().getStringExtra("meetingId");
        String[] contents = new String[]{getString(R.string.schedule_meeting_attend_unknown), getString(R.string.schedule_meeting_attend_ignore),
                getString(R.string.schedule_meeting_attend_accept), getString(R.string.schedule_meeting_attend_reject)};
        for (int i = 0; i < contents.length; i++) {
            ReplyAttendResult info = new ReplyAttendResult();
            info.position = i;
            info.content = contents[i];
            info.isSelect = false;
            if (i == originData.position) {
                info.isSelect = true;
            }
            dataList.add(info);
        }
        final MeetingReplyAdapter adapter = new MeetingReplyAdapter(this, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < dataList.size(); i++) {
                    dataList.get(i).isSelect = false;
                }
                dataList.get(position).isSelect = true;
                adapter.notifyDataSetChanged();

                scheduleApiService.setMeetingAttendStatus(meetingId, position);
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

            return convertView;
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnAttendMeetingStatusSuccess(String result, int type) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            Intent intent = new Intent();
            intent.putExtra("AttendReplyStatus", dataList.get(type).content);
            intent.putExtra("ReplyResult", dataList.get(type));
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
