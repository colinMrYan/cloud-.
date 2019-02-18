package com.inspur.emmcloud.ui.work;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
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
        View.OnClickListener {

//    private TextView mTextMonthDay;
//    private TextView mTextYear;
//    private TextView mTextLunar;
//    private TextView mTextCurrentDay;
    private CalendarView mCalendarView;
    private RelativeLayout mRelativeTool;
    private int mYear;
    private CalendarLayout mCalendarLayout;
    private GroupRecyclerView mRecyclerView;
    private View rootView;


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
        mRelativeTool = rootView.findViewById(R.id.rl_tool);
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
        mCalendarView.setOnCalendarSelectListener(this);
        mCalendarView.setOnYearChangeListener(this);
//        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        mYear = mCalendarView.getCurYear();
//        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
//        mTextLunar.setText("今日");
//        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));
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

    @Override
    public void onClick(View v) {

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
}
