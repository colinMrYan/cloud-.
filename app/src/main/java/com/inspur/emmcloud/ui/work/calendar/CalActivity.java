package com.inspur.emmcloud.ui.work.calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.CalEventGroup;
import com.inspur.emmcloud.bean.schedule.calendar.CalendarEvent;
import com.inspur.emmcloud.bean.work.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class CalActivity extends BaseActivity implements MySwipeRefreshLayout.OnRefreshListener, MySwipeRefreshLayout.OnLoadListener {

    protected static final int UPDATE_CAL_EVENT = 1;
    private static final int FILTER_CAL_EVENT = 2;
    private static final int ADD_CAL_EVENT = 3;
    private ExpandableListView expandableListView;

    private LoadingDialog loadingDlg;
    private WorkAPIService apiService;
    private List<MyCalendar> calendarList = new ArrayList<>();
    private List<CalendarEvent> allCalEventList = new ArrayList<>();
    private String calEventDisplayType;
    private List<CalEventGroup> calEventGroupList = new ArrayList<>();
    private MySwipeRefreshLayout swipeRefreshLayout;
    private CalendarEvent deleteCalEvent;
    private int page = 0;
    private List<String> calendarIdList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);
        loadingDlg = new LoadingDialog(this);
        apiService = new WorkAPIService(this);
        apiService.setAPIInterface(new WebServcie());
        swipeRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnLoadListener(this);
        expandableListView = (ExpandableListView) findViewById(R.id.expandable_list);
        expandableListView.setGroupIndicator(null);
        expandableListView.setVerticalScrollBarEnabled(false);
        expandableListView.setHeaderDividersEnabled(false);
        getCalendarEvent(true);

    }

    /**
     * 获取所有的日历
     */
    private void getCalendarEvent(boolean isShowDlg) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            page = 0;
            calendarList = MyCalendarCacheUtils
                    .getAllMyCalendarList(CalActivity.this);
            if (calendarList.size() > 0) {

                loadingDlg.show(isShowDlg);

                for (int i = 0; i < calendarList.size(); i++) {
                    calendarIdList.add(calendarList.get(i).getId());
                }
                getCalEvents(true);
            } else {
                if (!isShowDlg) {
                    swipeRefreshLayout.setLoading(false);
                }
                initDisplayData();
            }
        } else if (!isShowDlg) {
            swipeRefreshLayout.setLoading(false);
        }

    }

    /**
     * 获取所有日历的所有event
     *
     * @param isRefresh
     */
    private void getCalEvents(boolean isRefresh) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            Calendar afterCalendar = Calendar.getInstance();
            Calendar beforeCalendar = Calendar.getInstance();
            beforeCalendar.add(Calendar.YEAR, 1);
            afterCalendar.set(afterCalendar.get(Calendar.YEAR),
                    afterCalendar.get(Calendar.MONTH),
                    afterCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            afterCalendar = TimeUtils.localCalendar2UTCCalendar(afterCalendar);
            beforeCalendar = TimeUtils
                    .localCalendar2UTCCalendar(beforeCalendar);
            apiService.getAllCalEvents(calendarIdList, afterCalendar,
                    beforeCalendar, 15, page, isRefresh);
        } else {
            swipeRefreshLayout.setLoading(false);
        }
    }

    /**
     * 初始化和处理展示数据
     */
    private void initDisplayData() {
        // TODO Auto-generated method stub
        calEventGroupList.clear();
        calEventDisplayType = PreferencesUtils.getString(
                getApplicationContext(), "celEventDisplayType", "monthly");
        for (int i = 0; i < allCalEventList.size(); i++) {
            CalendarEvent calEvent = allCalEventList.get(i);
            Calendar startDate = calEvent.getStartDate();
            boolean isHide = MyCalendarOperationCacheUtils.getIsHide(
                    getApplicationContext(), calEvent.getCalendar().getId());
            if (calEvent.getState() != null
                    && !calEvent.getState().equals("REMOVED") && !isHide && startDate != null) {
                if (startDate != null) {
//                    EventAlertUtils.setCalEventNotification(
//                            getApplicationContext(), calEvent);
                    int month = startDate.get(Calendar.MONTH) + 1;
                    int key = -1;
                    if (calEventDisplayType.equals("monthly")) {
                        key = startDate.get(Calendar.YEAR) * 100 + month;
                    } else {
                        // 此处year*10 是为了区分不同年份同样季度的排序
                        key = startDate.get(Calendar.YEAR) * 10 + month / 4 + 1;
                    }
                    boolean isContainKey = false;
                    for (int j = 0; j < calEventGroupList.size(); j++) {
                        CalEventGroup calEventGroup = calEventGroupList.get(j);
                        if (calEventGroup.getKey() == key) {
                            isContainKey = true;
                            List<CalendarEvent> calEventList = calEventGroup
                                    .getCalEventList();
                            calEventList.add(calEvent);
                            calEventGroup.setCalEventList(calEventList);
                            break;
                        }
                    }
                    if (!isContainKey) {
                        List<CalendarEvent> calEventList = new ArrayList<CalendarEvent>();
                        calEventList.add(calEvent);
                        CalEventGroup calEventGroup = new CalEventGroup(key,
                                calEventList);
                        calEventGroupList.add(calEventGroup);
                    }
                }
            }
        }
        if (calEventGroupList != null && calEventGroupList.size() > 1) {
            Collections.sort(calEventGroupList, new CalEventGroup());
        }
        for (int i = 0; i < calEventGroupList.size(); i++) {
            List<CalendarEvent> calEventList = calEventGroupList.get(i).getCalEventList();
            if (calEventList.size() > 0) {
                Collections.sort(calEventList, new CalendarEvent());
            }
        }
        expandableListView.setAdapter(new CalAdapter());
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.cal_config_img:
                intent.putExtra("calendarList", (Serializable) calendarList);
                intent.setClass(CalActivity.this, CalFilterActivity.class);
                startActivityForResult(intent, FILTER_CAL_EVENT);
                break;
            case R.id.add_event_img:
                intent.setClass(CalActivity.this, CalEventAddActivity.class);
                startActivityForResult(intent, ADD_CAL_EVENT);
                break;

            default:
                break;
        }
    }

    /**
     * 弹出频道操作选择框
     *
     * @param calEvent
     */
    private void showOperationDlg(final CalendarEvent calEvent) {
        // TODO Auto-generated method stub

        final String[] items = new String[]{getString(R.string.delete), getString(R.string.cancel)};
        new QMUIDialog.MenuDialogBuilder(this)
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            deleteCalEvent(calEvent);
                        }
                    }
                })
                .show();
    }

    /**
     * 删除事件
     *
     * @param calEvent
     */
    private void deleteCalEvent(CalendarEvent calEvent) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            deleteCalEvent = calEvent;
            loadingDlg.show();
            apiService.deleteCalEvent(calEvent.getId());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case UPDATE_CAL_EVENT:
                    CalendarEvent calEvent = (CalendarEvent) data.getExtras()
                            .getSerializable("calEvent");
                    int index = allCalEventList.indexOf(calEvent);
                    LogUtils.debug("jason", "index=" + index);
                    if (index != -1) {
                        allCalEventList.remove(index);
                        allCalEventList.add(index, calEvent);
                    }
                    initDisplayData();
                    break;
                case FILTER_CAL_EVENT:
                    calendarList = (List<MyCalendar>) data.getExtras()
                            .getSerializable("calendarList");
                    initDisplayData();
                    break;
                case ADD_CAL_EVENT:
                    CalendarEvent addCalEvent = (CalendarEvent) data.getExtras()
                            .getSerializable("addCalendarEvent");
                    allCalEventList.add(addCalEvent);
                    initDisplayData();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 发送CalEvent变化通知
     */
    public void sendBoradcastReceiver() {
        Intent mIntent = new Intent(Constant.ACTION_CALENDAR);
        mIntent.putExtra("refreshCalendar", "");
        // 发送广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
    }

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
        getCalendarEvent(false);
    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub
        if (calendarIdList != null && calendarIdList.size() > 0) {
            page = page + 1;
            getCalEvents(false);
        } else {
            swipeRefreshLayout.setLoading(false);
        }
    }

    class CalAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            // TODO Auto-generated method stub
            return calEventGroupList.size();
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            // TODO Auto-generated method stub
            return calEventGroupList.get(groupPosition).getCalEventList()
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            ExpandableListView expandableListView = (ExpandableListView) parent;
            expandableListView.expandGroup(groupPosition);
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(CalActivity.this).inflate(
                        R.layout.cal_expand_list, null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView
                        .findViewById(R.id.cal_textView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String title = "";
            if (calEventDisplayType.equals("monthly")) {
                Calendar startDate = calEventGroupList.get(groupPosition)
                        .getCalEventList().get(0).getStartDate();
                title = TimeUtils.calendar2FormatString(
                        getApplicationContext(), startDate,
                        TimeUtils.FORMAT_YEAR_MONTH);
            } else {
                int key = calEventGroupList.get(groupPosition).getKey();
                title = key / 10 + " " + "Q" + key % 10;
            }
            holder.textView.setText(title);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            ExpandViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(CalActivity.this).inflate(
                        R.layout.cal_list_item, null);
                holder = new ExpandViewHolder();
                holder.calEventTitleText = (TextView) convertView
                        .findViewById(R.id.cal_title_text);
                holder.calEventStartDateText = (TextView) convertView
                        .findViewById(R.id.cal_time_text);
                holder.eventCountdownText = (CircleTextImageView) convertView
                        .findViewById(R.id.event_countdown_text);
                convertView.setTag(holder);
            } else {
                holder = (ExpandViewHolder) convertView.getTag();
            }

            List<CalendarEvent> CalEventList = calEventGroupList.get(
                    groupPosition).getCalEventList();
            final CalendarEvent calendarEvent = CalEventList.get(childPosition);
            Calendar localStartDate = calendarEvent.getStartDate();
            holder.eventCountdownText.setText(TimeUtils
                    .getCountdown(getApplicationContext(), localStartDate));
            int color = CalendarColorUtils.getColor(CalActivity.this,
                    calendarEvent.getCalendar().getColor());
            holder.eventCountdownText.setFillColor(color);
            holder.calEventTitleText.setText(calendarEvent.getTitle());

            String localDisplayTime = TimeUtils.calendar2FormatString(
                    getApplicationContext(), localStartDate,
                    TimeUtils.FORMAT_MONTH_DAY);
            holder.calEventStartDateText.setText(localDisplayTime);

            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),
                            CalEventAddActivity.class);
                    intent.putExtra("calEvent", calendarEvent);
                    startActivityForResult(intent, UPDATE_CAL_EVENT);
                }
            });

            convertView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // TODO Auto-generated method stub
                    if (!calendarEvent.getCalendar().getCommunity()) {
                        showOperationDlg(calendarEvent);
                    }
                    return false;
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView textView;
            ImageView imageView;
        }

        class ExpandViewHolder {
            TextView calEventTitleText;
            // ImageView imageView;
            TextView calEventStartDateText;
            // TextView calCountDownText;
            CircleTextImageView eventCountdownText;

        }

    }

    public class WebServcie extends APIInterfaceInstance {

        @Override
        public void returnCalEventsSuccess(
                GetCalendarEventsResult getCalendarEventsResult,
                boolean isRefresh) {
            // TODO Auto-generated method stub
            List<CalendarEvent> resultCalEventList = getCalendarEventsResult
                    .getCalEventList();
            swipeRefreshLayout.setCanLoadMore(resultCalEventList.size() == 15);
            if (isRefresh) {
                allCalEventList = resultCalEventList;
            } else {
                allCalEventList.addAll(resultCalEventList);

            }
            initDisplayData();
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            swipeRefreshLayout.setLoading(false);
        }

        @Override
        public void returnCalEventsFail(String error, boolean isRefresh, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(CalActivity.this, error, errorCode);
            swipeRefreshLayout.setLoading(false);
        }

        @Override
        public void returnDeleteCalEventSuccess() {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            for (int i = 0; i < allCalEventList.size(); i++) {
                CalendarEvent calendarEvent = allCalEventList.get(i);
                if (calendarEvent.getId().equals(deleteCalEvent.getId())) {
                    calendarEvent.setState("REMOVED");
                }
            }

            initDisplayData();
            sendBoradcastReceiver();
        }

        @Override
        public void returnDeleteCalEventFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(CalActivity.this, error, errorCode);
        }

    }
}
