package com.inspur.emmcloud.ui.work.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CalFilterActivity extends BaseActivity {

    private ScrollViewWithListView calendarListView;
    private List<MyCalendar> calendarList = new ArrayList<MyCalendar>();
    private CalendarAdapter calendarAdapter;
    private LoadingDialog loadingDlg;
    private WorkAPIService apiService;
    private int operationPosition;
    private ImageView quarterSelectImg;
    private ImageView monthlySelectImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal_filter);
        calendarListView = (ScrollViewWithListView) findViewById(R.id.calendar_list);
        loadingDlg = new LoadingDialog(this);
        apiService = new WorkAPIService(CalFilterActivity.this);
        apiService.setAPIInterface(new WebServcie());
        quarterSelectImg = (ImageView) findViewById(R.id.quarter_select_img);
        monthlySelectImg = (ImageView) findViewById(R.id.month_select_img);
        String calEventDisplayType = PreferencesUtils.getString(
                getApplicationContext(), "celEventDisplayType", "monthly");
        if (calEventDisplayType.equals("monthly")) {
            monthlySelectImg.setVisibility(View.VISIBLE);
        } else {
            quarterSelectImg.setVisibility(View.VISIBLE);
        }
        calendarList = (List<MyCalendar>) getIntent().getExtras()
                .getSerializable("calendarList");
        calendarAdapter = new CalendarAdapter();
        calendarListView.setAdapter(calendarAdapter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                returnCalList();
                finish();
                break;

            case R.id.quarter_select_layout:
                if (quarterSelectImg.getVisibility() != View.VISIBLE) {
                    quarterSelectImg.setVisibility(View.VISIBLE);
                    monthlySelectImg.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            "celEventDisplayType", "quarter");
                }
                break;

            case R.id.monthly_select_layout:
                if (monthlySelectImg.getVisibility() != View.VISIBLE) {
                    monthlySelectImg.setVisibility(View.VISIBLE);
                    quarterSelectImg.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            "celEventDisplayType", "monthly");
                }
                break;
            default:
                break;
        }
    }

    private void returnCalList() {
        // TODO Auto-generated method stub
        sendBoradcastReceiver();
        Intent intent = new Intent();
        intent.putExtra("calendarList", (Serializable) calendarList);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        returnCalList();
        super.onBackPressed();
    }

    /**
     * 发送Calendar变化通知
     */
    public void sendBoradcastReceiver() {
        Intent mIntent = new Intent(Constant.ACTION_CALENDAR);
        mIntent.putExtra("refreshCalendar", "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
    }

    private class CalendarAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return calendarList.size();
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
            // TODO Auto-generated method stub
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.my_calendar_item_view, null);
            TextView textView = (TextView) convertView
                    .findViewById(R.id.calendar_name_text);
            final SwitchView statusSwitch = (SwitchView) convertView
                    .findViewById(R.id.calendar_status_switch);
            final MyCalendar calendar = calendarList.get(position);
            textView.setText(calendar.getName());
            View colorView = convertView
                    .findViewById(R.id.calendar_color_view);
            boolean isHide = MyCalendarOperationCacheUtils.getIsHide(getApplicationContext(), calendar.getId());
            if (isHide) {
                statusSwitch.toggleSwitch(false);
            } else {
                statusSwitch.toggleSwitch(true);
            }
            statusSwitch
                    .setOnStateChangedListener(new OnStateChangedListener() {

                        @Override
                        public void toggleToOn(View view) {
                            // TODO Auto-generated method stub
                            statusSwitch.toggleSwitch(true);
                            MyCalendarOperationCacheUtils.saveMyCalendarOperation(getApplicationContext(), calendar.getId(), false);
                        }

                        @Override
                        public void toggleToOff(View view) {
                            // TODO Auto-generated method stub
                            statusSwitch.toggleSwitch(false);
                            MyCalendarOperationCacheUtils.saveMyCalendarOperation(getApplicationContext(), calendar.getId(), true);
                        }

                    });
            int color = CalendarColorUtils.getColor(CalFilterActivity.this,
                    calendar.getColor());
            colorView.setBackgroundColor(color);
            return convertView;
        }

    }

    public class WebServcie extends APIInterfaceInstance {


        @Override
        public void returnDelelteCalendarByIdSuccess() {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            calendarList.remove(operationPosition);
            calendarAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnDelelteCalendarByIdFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(CalFilterActivity.this, error, errorCode);
        }

    }
}
