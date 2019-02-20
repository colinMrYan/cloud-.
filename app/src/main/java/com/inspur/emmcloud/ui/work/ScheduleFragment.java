package com.inspur.emmcloud.ui.work;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.CalendarUtil;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.calendarview.Calendar;
import com.inspur.emmcloud.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.widget.calendarview.CalendarView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yufuchang on 2019/2/18.
 */

public class ScheduleFragment extends Fragment implements
        CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener,
        CalendarLayout.CalendarExpandListener{
    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    private CalendarView calendarView;
    private CalendarLayout calendarLayout;
    private View rootView;
    private PopupWindow popupWindow;
    private TextView scheduleDataText;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = getLayoutInflater().inflate(R.layout.fragment_schedule, null);
        initView();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_schedule, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void initView() {
        calendarView =  rootView.findViewById(R.id.calendar_view_schedule);
        calendarLayout =  rootView.findViewById(R.id.calendar_layout_schedule);
        calendarLayout.setExpandListener(this);
        calendarView.setOnCalendarSelectListener(this);
        calendarView.setOnYearChangeListener(this);
        scheduleDataText = rootView.findViewById(R.id.tv_schedule_date);
        setCalendarTime(System.currentTimeMillis());
        rootView.findViewById(R.id.iv_schedule_function_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.iv_schedule_function_list:
                        showPopupWindow(v);
                        break;
                }
            }
        });
        rootView.findViewById(R.id.iv_schedule_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarLayout.switchStatus();
            }
        });
    }

    private void initData() {
        int year = calendarView.getCurYear();
        int month = calendarView.getCurMonth();
        Map<String, Calendar> map = new HashMap<>();
        map.put(getSchemeCalendar(year, month, 3, 0xFF40db25, "假").toString(),
                getSchemeCalendar(year, month, 3, 0xFF40db25, "假"));
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map);
    }

    private Calendar getSchemeCalendar(int year, int month, int day, int color, String text) {
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        calendar.setScheme(text);
        calendar.addScheme(new Calendar.Scheme());
        calendar.addScheme(0xFF008800, "假");
        calendar.addScheme(0xFF008800, "节");
        return calendar;
    }

    /**
     * 通讯录和创建群组，扫一扫合并
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.pop_schedule_window_view, null);

        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(getActivity(), 1.0f);
            }
        });
        contentView.findViewById(R.id.rl_schedule_calendar).setOnClickListener(onViewClickListener);
        contentView.findViewById(R.id.rl_schedule_mission).setOnClickListener(onViewClickListener);
        contentView.findViewById(R.id.rl_schedule_meeting).setOnClickListener(onViewClickListener);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        AppUtils.setWindowBackgroundAlpha(getActivity(), 0.8f);
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);
    }

    private View.OnClickListener onViewClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            popupWindow.dismiss();
            switch (v.getId()){
                case R.id.rl_schedule_calendar:
                    recordUserClickWorkFunction(PV_COLLECTION_CAL);
                    IntentUtils.startActivity(getActivity(), CalActivity.class);
                    break;
                case R.id.rl_schedule_meeting:
                    recordUserClickWorkFunction(PV_COLLECTION_MEETING);
                    IntentUtils.startActivity(getActivity(), MeetingListActivity.class);
                    break;
                case R.id.rl_schedule_mission:
                    recordUserClickWorkFunction(PV_COLLECTION_MISSION);
                    IntentUtils.startActivity(getActivity(), MessionListActivity.class);
                    break;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    /**
     * 记录用户点击
     *
     * @param functionId
     */
    private void recordUserClickWorkFunction(String functionId) {
        PVCollectModel pvCollectModel = new PVCollectModel(functionId, "work");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(), pvCollectModel);
    }

    @Override
    public void onYearChange(int year) {

    }

    @Override
    public void onCalendarOutOfRange(Calendar calendar) {

    }

    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        setCalendarTime(calendar.getTimeInMillis());
    }

    private void setCalendarTime(long timeInMillis) {
        java.util.Calendar calendar1 = TimeUtils.
                timeLong2Calendar(timeInMillis);
        String time = TimeUtils.calendar2FormatString(getActivity(),calendar1,TimeUtils.FORMAT_YEAR_MONTH_DAY_BY_DASH)+"·"+
                CalendarUtil.getWeekDay(calendar1);
        boolean isToday = TimeUtils.isCalendarToday(calendar1);
        if(isToday){
            time = getString(R.string.today)+"·"+time;
        }
        scheduleDataText.setText(time);
    }

    @Override
    public void isExpand(boolean isExpand) {
        ((ImageView)rootView.findViewById(R.id.iv_schedule_arrow)).setImageResource(isExpand?R.drawable.ic_schedule_up:R.drawable.ic_schedule_down);
    }
}
