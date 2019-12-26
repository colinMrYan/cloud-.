package com.inspur.emmcloud.schedule.ui.meeting;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.Participant;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.meeting.ReplyAttendResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScheduleDetailReplyActivity extends BaseActivity {
    @BindView(R2.id.lv_meeting_reply)
    ListView listView;
    private Schedule schedule;
    private List<ReplyAttendResult> dataList = new ArrayList<>();
    private List<String> responseTypeList = new ArrayList<>();
    private LoadingDialog loadingDlg;
    private ScheduleAPIService scheduleApiService;

    @Override
    public int getLayoutResId() {
        return R.layout.schedule_meeting_detail_reply_activity;
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        scheduleApiService = new ScheduleAPIService(this);
        scheduleApiService.setAPIInterface(new WebService());

        initData();
        initView();
    }

    private void initData() {
        ReplyAttendResult originData = (ReplyAttendResult) getIntent().getSerializableExtra("OriginReplyData");
        schedule = (Schedule) getIntent().getSerializableExtra(Constant.OPEN_SCHEDULE_DETAIL);
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
                    scheduleApiService.setMeetingAttendStatus(schedule, responseTypeList.get(position));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ibt_back) {
            finish();
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
                convertView = View.inflate(context, R.layout.schedule_meeting_reply_item, null);
            }
            ReplyAttendResult info = dataList.get(position);
            ((TextView) convertView.findViewById(R.id.tv_meeting_reply)).setText(info.content);
            convertView.findViewById(R.id.iv_meeting_reply_selected).setVisibility(info.isSelect ? View.VISIBLE : View.INVISIBLE);
            convertView.findViewById(R.id.item_meeting_reply_space)
                    .setVisibility((dataList.size() == 4 && position == 0) ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }

    class WebService extends ScheduleAPIInterfaceImpl {
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
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
        }
    }
}
