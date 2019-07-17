package com.inspur.emmcloud.ui.schedule;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.BaseLayoutFragment;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.GetScheduleBasicDataResult;
import com.inspur.emmcloud.bean.schedule.calendar.Holiday;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.componentservice.mail.MailService;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.util.privates.cache.HolidayCacheUtils;
import com.inspur.emmcloud.widget.DragScaleView;
import com.inspur.emmcloud.widget.calendardayview.CalendarDayView;
import com.inspur.emmcloud.widget.calendardayview.Event;
import com.inspur.emmcloud.widget.calendarview.CalendarLayout;
import com.inspur.emmcloud.widget.calendarview.CalendarView;
import com.inspur.emmcloud.widget.calendarview.EmmCalendar;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenmch on 2019/7/12.
 */

public class ScheduleBaseFragment extends BaseLayoutFragment implements View.OnLongClickListener,
        CalendarView.OnCalendarSelectListener, CalendarLayout.CalendarExpandListener,
        DragScaleView.OnMoveListener, View.OnTouchListener {
    private static final String PV_COLLECTION_CAL = "calendar";
    private static final String PV_COLLECTION_MISSION = "task";
    private static final String PV_COLLECTION_MEETING = "meeting";
    protected Calendar pageStartCalendar = Calendar.getInstance();
    protected Calendar pageEndCalendar = Calendar.getInstance();
    protected Map<Integer, List<Holiday>> yearHolidayListMap = new HashMap<>();
    protected Calendar selectCalendar = Calendar.getInstance();
    protected CalendarView calendarView;
    protected CalendarLayout calendarLayout;
    protected TextView scheduleDataText;
    protected ImageView calendarViewExpandImg;
    protected CalendarDayView calendarDayView;
    protected RelativeLayout contentLayout;
    protected DragScaleView dragScaleView;
    protected ScrollView eventScrollView;
    private float contentLayoutTouchY = -1;
    private ScheduleApiService apiService;
    private Event modifyEvent;

    @Override
    protected void onCreate() {
        init();
        apiService = new ScheduleApiService(getActivity());
        apiService.setAPIInterface(new WebService());
        checkExchangeLogin();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_schedule;
    }

    protected void init() {

    }

    protected void showCalendarEvent(boolean isForceUpdate) {

    }

    /**
     * 日历返回今天的接口
     */
    public void setScheduleBackToToday() {
        if (calendarView != null) {
            calendarView.scrollToCurrent();
        }
    }

    public Calendar getSelectCalendar() {
        return selectCalendar;
    }


    @Override
    public void onCalendarOutOfRange(EmmCalendar calendar) {

    }

    @Override
    public void isExpand(boolean isExpand) {
        if (isExpand) {
            onCalendarSelect(calendarView.getSelectedCalendar(), false);
        }
        calendarViewExpandImg.setImageResource(isExpand ? R.drawable.ic_schedule_up : R.drawable.ic_schedule_down);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.calendar_day_view) {
            if (dragScaleView != null && dragScaleView.getVisibility() == View.VISIBLE) {
                removeEventAddDragScaleView();
            } else {
                showScheduleEventAddDragView(null, 0, 0);
            }

            return true;
        }
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.calendar_day_view) {
            contentLayoutTouchY = event.getY();
        }
        return false;
    }

    @Override
    public void moveTo(final boolean isNeedScroll, final int ScrollOffset, final int top, final int height) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isNeedScroll) {
                    int scrollOffset = ScrollOffset;
                    int currentScrollY = eventScrollView.getScrollY();
                    eventScrollView.scrollBy(0, scrollOffset);
                    if (ScrollOffset < 0) {//向下滚动
                        if (currentScrollY < -ScrollOffset) {
                            scrollOffset = -currentScrollY;
                        }
                    } else {
                        int maxScrollOffset = eventScrollView.getChildAt(0).getHeight() - currentScrollY - eventScrollView.getHeight();
                        if (scrollOffset > maxScrollOffset) {
                            scrollOffset = maxScrollOffset;
                        }

                    }
                    //ScrollView滚动后，DragScaleView也会随之滚动，为了让DragScaleView能够跟随手势
                    dragScaleView.updateLastY(-scrollOffset);
                }
                calendarDayView.showDragViewTime(top, height);
            }
        }, 10);
    }

    @Override
    public void onCalendarSelect(EmmCalendar calendar, boolean isClick) {
        removeEventAddDragScaleView();
        selectCalendar = Calendar.getInstance();
        selectCalendar.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(), 0, 0, 0);
        selectCalendar.set(Calendar.MILLISECOND, 0);
        setSelectCalendarTimeInfo();
        List<EmmCalendar> currentPageCalendarList = calendarView.getCurrentPageCalendars();
        if (currentPageCalendarList != null) {
            EmmCalendar startEmmCalendar = currentPageCalendarList.get(0);
            EmmCalendar endEmmCalendar = currentPageCalendarList.get(currentPageCalendarList.size() - 1);
            pageStartCalendar.set(startEmmCalendar.getYear(), startEmmCalendar.getMonth() - 1, startEmmCalendar.getDay());
            pageEndCalendar.set(endEmmCalendar.getYear(), endEmmCalendar.getMonth() - 1, endEmmCalendar.getDay());
            showCalendarEvent(false);
        }
    }


    /**
     * 显示选中时间
     */
    private void setSelectCalendarTimeInfo() {
        StringBuilder builder = new StringBuilder();
        boolean isToday = TimeUtils.isCalendarToday(selectCalendar);
        calendarDayView.setCurrentTimeLineShow(isToday);
        builder.append(TimeUtils.calendar2FormatString(MyApplication.getInstance(), selectCalendar,
                TimeUtils.getFormat(MyApplication.getInstance(),
                        Calendar.getInstance().get(Calendar.YEAR) == selectCalendar.get(Calendar.YEAR) ? TimeUtils.FORMAT_MONTH_DAY : TimeUtils.FORMAT_YEAR_MONTH_DAY)));
        builder.append(" ");
        builder.append(TimeUtils.getWeekDay(MyApplication.getInstance(), selectCalendar));
        scheduleDataText.setText(builder.toString());
    }


    /**
     * 检查Exchange邮箱登录
     */
    private void checkExchangeLogin() {
        if (PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false)) {
            Router router = Router.getInstance();
            if (router.getService(MailService.class) != null) {
                MailService service = router.getService(MailService.class);
                String exchangeAccount = service.getExchangeMailAccount();
                String exchangePassword = service.getExchangeMailPassword();
                if (!StringUtils.isBlank(exchangeAccount) && !StringUtils.isBlank(exchangePassword)) {
                    service.exchangeLogin(getActivity(), new OnExchangeLoginListener() {
                        @Override
                        public void onMailLoginSuccess() {

                        }

                        @Override
                        public void onMailLoginFail(String error, int errorCode) {
                            showExchangeLoginFailDlg();
                        }
                    });
                }
            }
        }

    }


    /**
     * 删除日历添加DragView
     */
    protected boolean removeEventAddDragScaleView() {
        if (dragScaleView != null && dragScaleView.getVisibility() == View.VISIBLE) {
            if (dragScaleView.isEventModify()) {
                updateEventTime();
            }
            contentLayout.removeView(dragScaleView);
            calendarDayView.hideDragViewTime();
            dragScaleView = null;
            return true;
        }
        this.modifyEvent = null;
        return false;
    }


    /**
     * 删除添加事件的View
     */
    protected void showScheduleEventAddDragView(Event event, int top, int dragScaleViewHeight) {
        this.modifyEvent = event;
        dragScaleView = new DragScaleView(getActivity());
        if (event == null) {
            dragScaleViewHeight = DensityUtil.dip2px(40) + 2 * dragScaleView.getOffset();
            int halfHourHeight = DensityUtil.dip2px(20);
            //保证DragScaleView显示在两个整点或两个半点之间
            top = (int) Math.round((contentLayoutTouchY - halfHourHeight) * 1.0 / halfHourHeight) * halfHourHeight;
        } else {
            dragScaleView.setContent(event.getEventTitle());
            dragScaleViewHeight += 2 * dragScaleView.getOffset();
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dragScaleViewHeight);
        params.setMargins(0, top, 0, 0);
        dragScaleView.setParentView(eventScrollView);
        contentLayout.addView(dragScaleView, params);
        calendarDayView.showDragViewTime(top, dragScaleViewHeight - 2 * dragScaleView.getOffset());
        dragScaleView.setOnMoveListener(this);
        dragScaleView.getParent().requestDisallowInterceptTouchEvent(true);
        dragScaleView.setFocusable(true);
        dragScaleView.setFocusableInTouchMode(true);
        dragScaleView.requestFocus();
        dragScaleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modifyEvent == null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(CalendarAddActivity.EXTRA_START_CALENDAR, calendarDayView.getDragViewStartTime(selectCalendar));
                    bundle.putSerializable(CalendarAddActivity.EXTRA_END_CALENDAR, calendarDayView.getDragViewEndTime(selectCalendar));
                    IntentUtils.startActivity(getActivity(), CalendarAddActivity.class, bundle);
                    removeEventAddDragScaleView();
                }

            }
        });
        dragScaleView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }


    /**
     * 弹出注销提示框
     */
    private void showExchangeLoginFailDlg() {
        new CustomDialog.MessageDialogBuilder(getActivity())
                .setMessage(R.string.schedule_exchange_login_fail)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Bundle bundle = new Bundle();
                        bundle.putString("from", "schedule_exchange_login");
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_MAIL_LOGIN).with(bundle).navigation(getActivity());
                    }
                })
                .show();
    }

    protected void getScheduleBasicData(int year) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            String version = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_BASIC_DATA_VERSION, "0");
            apiService.getScheduleBasicData(year, version);
        }
    }

    private void updateEventTime() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            Calendar startTime = calendarDayView.getDragViewStartTime(selectCalendar);
            Calendar endTime = calendarDayView.getDragViewEndTime(selectCalendar);
            if (modifyEvent.getCalendarType() == Schedule.TYPE_CALENDAR) {
                Schedule schedule = (Schedule) modifyEvent.getEventObj();
                if (!startTime.equals(schedule.getStartTimeCalendar()) || !endTime.equals(schedule.getEndTimeCalendar())) {
                    schedule.setStartTime(startTime.getTimeInMillis());
                    schedule.setEndTime(endTime.getTimeInMillis());
                    apiService.updateSchedule(schedule.toCalendarEventJSONObject().toString());
                }
            } else if (modifyEvent.getCalendarType() == Schedule.TYPE_MEETING) {
                Meeting meeting = (Meeting) modifyEvent.getEventObj();
                if (!startTime.equals(meeting.getStartTimeCalendar()) || !endTime.equals(meeting.getEndTimeCalendar())) {
                    meeting.setStartTime(startTime.getTimeInMillis());
                    meeting.setEndTime(endTime.getTimeInMillis());
                    apiService.updateSchedule(meeting.toJSONObject().toString());
                }
            }


        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnScheduleBasicDataSuccess(GetScheduleBasicDataResult getScheduleBasicDataResult) {
            PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_BASIC_DATA_VERSION, getScheduleBasicDataResult.getVersion());
            if (getScheduleBasicDataResult.getCommand().equals("FORWARD")) {
                boolean isEnableExchangePrevious = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
                PreferencesByUserAndTanentUtils.putBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, getScheduleBasicDataResult.isEnableExchange());
                //当检测到突然开启Exchange日历功能时，进行Exchange登录检查
                if (getScheduleBasicDataResult.isEnableExchange() && !isEnableExchangePrevious) {
                    checkExchangeLogin();
                }
                List<Holiday> holidayList = getScheduleBasicDataResult.getHolidayList();
                int year = getScheduleBasicDataResult.getYear();
                    yearHolidayListMap.put(year, holidayList);
                if (pageStartCalendar.get(Calendar.YEAR) == year || pageEndCalendar.get(Calendar.YEAR) == year) {
                    showCalendarEvent(true);
                }
                HolidayCacheUtils.saveHolidayList(MyApplication.getInstance(), getScheduleBasicDataResult.getYear(), holidayList);
            }
        }

        @Override
        public void returnScheduleBasicDataFail(String error, int errorCode) {
        }

        @Override
        public void returnUpdateScheduleSuccess() {
            showCalendarEvent(true);
        }

        @Override
        public void returnUpdateScheduleFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnUpdateMeetingSuccess() {
            showCalendarEvent(true);
        }

        @Override
        public void returnUpdateMeetingFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }
    }
}
