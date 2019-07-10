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
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.bean.schedule.meeting.ReplyAttendResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeetingDetailReplyActivity extends BaseActivity {
    @BindView(R.id.lv_meeting_reply)
    ListView listView;
    private List<ReplyAttendResult> dataList = new ArrayList<>();

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_detail_reply;
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        ReplyAttendResult originData = (ReplyAttendResult) getIntent().getSerializableExtra("OriginReplyData");
        String[] contents = new String[]{"忽略", "接受", "拒绝"};
        for (int i = 0; i < 3; i++) {
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
                Intent intent = new Intent();
                intent.putExtra("AttendReplyStatus", dataList.get(position).content);
                intent.putExtra("ReplyResult", dataList.get(position));
                setResult(100, intent);
                finish();
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
}
