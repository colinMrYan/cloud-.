package com.inspur.emmcloud.ui.work;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.ui.work.calendar.CalActivity;
import com.inspur.emmcloud.ui.work.meeting.MeetingListActivity;
import com.inspur.emmcloud.ui.work.task.MessionListActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.calendarview.Calendar;
import com.inspur.emmcloud.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.widget.calendarview.CalendarView;
import com.inspur.emmcloud.widget.calendarview.group.GroupRecyclerView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yufuchang on 2019/2/18.
 */

public class ScheduleFragment extends Fragment implements
        CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener,
        CalendarLayout.CalendarExpandListener{
    private CalendarView mCalendarView;
    private int mYear;
    private CalendarLayout mCalendarLayout;
    private GroupRecyclerView mRecyclerView;
    private View rootView;
    private PopupWindow popupWindow;


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
//        setStatusBarDarkMode();
//        mTextMonthDay =  rootView.findViewById(R.id.tv_month_day);
//        mTextYear =  rootView.findViewById(R.id.tv_year);
//        mTextLunar =  rootView.findViewById(R.id.tv_lunar);
        mCalendarView =  rootView.findViewById(R.id.calendarView);
//        mTextCurrentDay = rootView.findViewById(R.id.tv_current_day);
//        mTextMonthDay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mCalendarLayout.isExpand()) {
//                    mCalendarLayout.expand();
//                    return;
//                }
//                mCalendarView.showYearSelectLayout(mYear);
//                mTextLunar.setVisibility(View.GONE);
//                mTextYear.setVisibility(View.GONE);
//                mTextMonthDay.setText(String.valueOf(mYear));
//            }
//        });
//        rootView.findViewById(R.id.fl_current).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCalendarView.scrollToCurrent();
//            }
//        });
        mCalendarLayout =  rootView.findViewById(R.id.calendarLayout);
        mCalendarLayout.setExpandListener(this);
        mCalendarView.setOnCalendarSelectListener(this);
        mCalendarView.setOnYearChangeListener(this);
//        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        mYear = mCalendarView.getCurYear();
//        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
//        mTextLunar.setText("今日");
//        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));
        rootView.findViewById(R.id.schedule_function_list_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.schedule_function_list_img:
                        showPopupWindow(v);
                        break;
                }
            }
        });
    }

    private void initData() {
        int year = mCalendarView.getCurYear();
        int month = mCalendarView.getCurMonth();

        Map<String, Calendar> map = new HashMap<>();
        map.put(getSchemeCalendar(year, month, 3, 0xFF40db25, "假").toString(),
                getSchemeCalendar(year, month, 3, 0xFF40db25, "假"));
//        map.put(getSchemeCalendar(year, month, 6, 0xFFe69138, "事").toString(),
//                getSchemeCalendar(year, month, 6, 0xFFe69138, "事"));
//        map.put(getSchemeCalendar(year, month, 9, 0xFFdf1356, "议").toString(),
//                getSchemeCalendar(year, month, 9, 0xFFdf1356, "议"));
//        map.put(getSchemeCalendar(year, month, 13, 0xFFedc56d, "记").toString(),
//                getSchemeCalendar(year, month, 13, 0xFFedc56d, "记"));
//        map.put(getSchemeCalendar(year, month, 14, 0xFFedc56d, "记").toString(),
//                getSchemeCalendar(year, month, 14, 0xFFedc56d, "记"));
//        map.put(getSchemeCalendar(year, month, 15, 0xFFaacc44, "假").toString(),
//                getSchemeCalendar(year, month, 15, 0xFFaacc44, "假"));
//        map.put(getSchemeCalendar(year, month, 18, 0xFFbc13f0, "记").toString(),
//                getSchemeCalendar(year, month, 18, 0xFFbc13f0, "记"));
//        map.put(getSchemeCalendar(year, month, 25, 0xFF13acf0, "假").toString(),
//                getSchemeCalendar(year, month, 25, 0xFF13acf0, "假"));
//        map.put(getSchemeCalendar(year, month, 27, 0xFF13acf0, "多").toString(),
//                getSchemeCalendar(year, month, 27, 0xFF13acf0, "多"));
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        mCalendarView.setSchemeDate(map);


//        mRecyclerView = (GroupRecyclerView) rootView.findViewById(R.id.recyclerView);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mRecyclerView.addItemDecoration(new GroupItemDecoration<String, Article>());
//        mRecyclerView.setAdapter(new ArticleAdapter(getActivity()));
//        mRecyclerView.notifyDataSetChanged();
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
                    recordUserClickWorkFunction("calendar");
                    IntentUtils.startActivity(getActivity(), CalActivity.class);
                    break;
                case R.id.rl_schedule_meeting:
                    recordUserClickWorkFunction("meeting");
                    IntentUtils.startActivity(getActivity(), MeetingListActivity.class);
                    break;
                case R.id.rl_schedule_mission:
                    recordUserClickWorkFunction("task");
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
    }

    @Override
    public void isExpand(boolean isExpand) {
    }
}
