package com.inspur.emmcloud.ui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2019/3/29.
 */
@ContentView(R.layout.activity_schedule_alert_time)
public class ScheduleAlertTimeActivity extends BaseActivity {
    @ViewInject(R.id.lv_alert_time)
    ListView alertTimeListView;
    @ViewInject(R.id.iv_no_alert_select)
    ImageView noAlertSelectImage;
    public static String EXTRA_SCHEDULE_ALERT_TIME = "schedule_alert_time";

    String alertTime = "";
    private Adapter adapter;
    private int selectPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String[] alertTimeArray = {
                getString(R.string.calendar_when_event_occurs),
                getString(R.string.calendar_ten_minite_ago),
                getString(R.string.calendar_twenty_minite_ago),
                getString(R.string.calendar_thirty_minite_ago),
                getString(R.string.calendar_one_hour_ago),
                getString(R.string.calendar_one_day_ago)};
        alertTime = getIntent().getExtras().containsKey(EXTRA_SCHEDULE_ALERT_TIME) ?
                getIntent().getExtras().getString(EXTRA_SCHEDULE_ALERT_TIME) : getString(R.string.calendar_no_alert);
        if (!alertTime.equals(getString(R.string.calendar_no_alert))) {
            noAlertSelectImage.setVisibility(View.GONE);
            for (int i = 0; i < alertTimeArray.length; i++) {
                if (alertTimeArray[i].equals(alertTime)) {
                    selectPosition = i;
                    break;
                }
            }
        } else {
            noAlertSelectImage.setVisibility(View.VISIBLE);
        }
        adapter = new Adapter(alertTimeArray);
        alertTimeListView.setAdapter(adapter);
        alertTimeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                noAlertSelectImage.setVisibility(View.GONE);
                selectPosition = position;
                adapter.notifyDataSetChanged();
                alertTime = alertTimeArray[position];
            }
        });
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_no_alert:
                noAlertSelectImage.setVisibility(View.VISIBLE);
                selectPosition = -1;
                alertTime = getString(R.string.calendar_no_alert);
                break;
            case R.id.tv_save:
                returnData();
                break;
            default:
                break;
        }
    }

    /**
     * 返回数据
     */
    public void returnData() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SCHEDULE_ALERT_TIME, alertTime);
        setResult(RESULT_OK, intent);
        finish();
    }

    private class Adapter extends BaseAdapter {
        private String[] alertTimeArray;

        public Adapter(String[] alertTimeArray) {
            this.alertTimeArray = alertTimeArray;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return alertTimeArray.length;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.schedule_alert_time, null);
            TextView timeText = convertView
                    .findViewById(R.id.tv_alert_time);
            ImageView selectImg = convertView
                    .findViewById(R.id.iv_alert_time_select);
            timeText.setText(alertTimeArray[position]);
            if (selectPosition == position) {
                selectImg.setVisibility(View.VISIBLE);
            } else {
                selectImg.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}
