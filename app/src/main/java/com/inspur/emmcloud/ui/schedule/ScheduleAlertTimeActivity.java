package com.inspur.emmcloud.ui.schedule;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.RemindEvent;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/3/29.
 */
public class ScheduleAlertTimeActivity extends BaseActivity {
    public static String EXTRA_SCHEDULE_ALERT_TIME = "schedule_alert_time";
    public static String EXTRA_SCHEDULE_IS_ALL_DAY = "schedule_is_all_day";
    public static String EXTRA_IS_TASK = "schedule_is_task";
    static int[] alertTimeIntArray = {-1, 0, 600, 1200, 1800, 3600, 86400};
    static int[] alertTimeAllDayIntArray = {-1, -32400, 54000, 140400, 572400};
    @BindView(R.id.lv_alert_time)
    ListView alertTimeListView;
    @BindView(R.id.iv_no_alert_select)
    ImageView noAlertSelectImage;
    int alertTime = -1;
    String[] alertTimeArray = {
            MyApplication.getInstance().getString(R.string.calendar_when_event_occurs),
            MyApplication.getInstance().getString(R.string.calendar_ten_minite_ago),
            MyApplication.getInstance().getString(R.string.calendar_twenty_minite_ago),
            MyApplication.getInstance().getString(R.string.calendar_thirty_minite_ago),
            MyApplication.getInstance().getString(R.string.calendar_one_hour_ago),
            MyApplication.getInstance().getString(R.string.calendar_one_day_ago)};
    String[] allDayAlertTimeArray = {
            MyApplication.getInstance().getString(R.string.schedule_alert_time_occur),
            MyApplication.getInstance().getString(R.string.schedule_alert_time_before_one_day),
            MyApplication.getInstance().getString(R.string.schedule_alert_time_before_two_day),
            MyApplication.getInstance().getString(R.string.schedule_alert_time_before_a_week)};
    private Adapter adapter;
    private int selectPosition = -1;
    private String[] alertTimeString = {};
    private int[] alertTimeInt = {};
    private boolean isAllDay = false;

    /**
     * 根据提前多长时间Int值及 是否 allday 获取相应的名称
     */
    public static String getAlertTimeNameByTime(int alertTime, boolean isAllDay) {
        String[] alertTimeArray = {
                MyApplication.getInstance().getString(R.string.calendar_when_event_occurs),
                MyApplication.getInstance().getString(R.string.calendar_ten_minite_ago),
                MyApplication.getInstance().getString(R.string.calendar_twenty_minite_ago),
                MyApplication.getInstance().getString(R.string.calendar_thirty_minite_ago),
                MyApplication.getInstance().getString(R.string.calendar_one_hour_ago),
                MyApplication.getInstance().getString(R.string.calendar_one_day_ago)};
        String[] allDayAlertTimeArray = {
                MyApplication.getInstance().getString(R.string.schedule_alert_time_occur),
                MyApplication.getInstance().getString(R.string.schedule_alert_time_before_one_day),
                MyApplication.getInstance().getString(R.string.schedule_alert_time_before_two_day),
                MyApplication.getInstance().getString(R.string.schedule_alert_time_before_a_week)};
        String[] returnAlertTimeString = isAllDay ? allDayAlertTimeArray : alertTimeArray;
        int[] returnAlertTimeInt = isAllDay ? alertTimeAllDayIntArray : alertTimeIntArray;
        if (alertTime == -1) {
            return MyApplication.getInstance().getString(R.string.calendar_no_alert);
        }
        for (int i = 0; i < returnAlertTimeInt.length; i++) {
            if (alertTime == returnAlertTimeInt[i]) {
                return returnAlertTimeString[i - 1];
            }
        }
        return "";
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        alertTime = getIntent().getExtras().containsKey(EXTRA_SCHEDULE_ALERT_TIME) ?
                getIntent().getExtras().getInt(EXTRA_SCHEDULE_ALERT_TIME) : -1;
        //获取Allday值
        isAllDay = getIntent().getExtras().containsKey(EXTRA_SCHEDULE_IS_ALL_DAY) ?
                getIntent().getExtras().getBoolean(EXTRA_SCHEDULE_IS_ALL_DAY) : false;
        alertTimeArray[0] = getIntent().getExtras().containsKey(EXTRA_IS_TASK) ?
                getApplication().getString(R.string.calendar_when_event_finished) : alertTimeArray[0];
        alertTimeString = isAllDay ? allDayAlertTimeArray : alertTimeArray;
        alertTimeInt = isAllDay ? alertTimeAllDayIntArray : alertTimeIntArray;
        if (alertTime != -1) {
            noAlertSelectImage.setVisibility(View.GONE);
            for (int i = 0; i < alertTimeInt.length; i++) {
                if (alertTime == alertTimeInt[i]) {
                    selectPosition = i - 1;
                    break;
                }
            }
        } else {
            noAlertSelectImage.setVisibility(View.VISIBLE);
        }
        adapter = new Adapter(alertTimeString);
        alertTimeListView.setAdapter(adapter);
        alertTimeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                noAlertSelectImage.setVisibility(View.GONE);
                selectPosition = position;
                adapter.notifyDataSetChanged();
                alertTime = alertTimeInt[position + 1];
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_schedule_alert_time;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_no_alert:
                noAlertSelectImage.setVisibility(View.VISIBLE);
                selectPosition = -1;
                alertTime = -1;
                adapter.notifyDataSetChanged();
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
        String name = "";
        if (selectPosition == -1) {
            name = getString(R.string.calendar_no_alert);
        } else {
            name = alertTimeString[selectPosition];
        }
        RemindEvent remindEvent = new RemindEvent("", alertTimeInt[selectPosition + 1], name);
        intent.putExtra(EXTRA_SCHEDULE_ALERT_TIME, remindEvent);
        setResult(RESULT_OK, intent);
        finish();
    }

    /***/
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
