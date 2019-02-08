package com.inspur.emmcloud.ui.work.meeting;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetMeetingsResult;
import com.inspur.emmcloud.bean.work.Meeting;
import com.inspur.emmcloud.bean.work.Room;
import com.inspur.emmcloud.util.common.GroupUtils;
import com.inspur.emmcloud.util.common.GroupUtils.GroupBy;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CustomEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 会议历史记录搜索 com.inspur.emmcloud.ui.work.meeting.MeetingHistorySearchActivity
 * create at 2016年10月9日 上午9:47:05
 */
public class MeetingHistorySearchActivity extends BaseActivity implements
        MySwipeRefreshLayout.OnLoadListener, OnEditorActionListener {

    private final int LIMIT = 15;
    private ExpandableListView expandListView;
    private Map<String, List<Meeting>> MeetingGroupByDayMap = new ArrayMap<String, List<Meeting>>();
    private List<String> meetingDayList = new ArrayList<String>();
    private MyAdapter adapter;
    private LoadingDialog loadingDlg;
    private WorkAPIService apiService;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout noMeetingLayout;
    private List<Meeting> allMeetingList = new ArrayList<Meeting>();
    private int page = 0;
    private CustomEditText searchEdit;
    private String keyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_history_search);
        initViews();
    }


    /**
     * 初始化Views
     */
    private void initViews() {
        apiService = new WorkAPIService(MeetingHistorySearchActivity.this);
        apiService.setAPIInterface(new WebService());

        loadingDlg = new LoadingDialog(MeetingHistorySearchActivity.this);
        swipeRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnLoadListener(this);
        swipeRefreshLayout.setEnabled(false);
        searchEdit = (CustomEditText) findViewById(R.id.search_edit);
        searchEdit.setOnEditorActionListener(this);
        searchEdit.addTextChangedListener(watcher);
        noMeetingLayout = (RelativeLayout) findViewById(R.id.meeting_history_search_null_layout);
        expandListView = (ExpandableListView) findViewById(R.id.expandable_list);
        expandListView.setGroupIndicator(null);
        expandListView.setVerticalScrollBarEnabled(false);
        expandListView.setHeaderDividersEnabled(false);
        expandListView.setOnChildClickListener(new MeetingListListener());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.widget.TextView.OnEditorActionListener#onEditorAction(android
     * .widget.TextView, int, android.view.KeyEvent)
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO Auto-generated method stub
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String searchContent = searchEdit.getText().toString();
            if (StringUtils.isBlank(searchContent)){
                ToastUtils.show(getApplicationContext(),R.string.app_input_search_key);
                return true;
            }
            if (!searchContent.equals(keyword)) {
                keyword = searchContent;
                page = 0;
                getSearchMeetings(true, false);
            }
        }
        return false;
    }

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            String content = s.toString();
            if (StringUtils.isBlank(content)) {
                keyword = "";
                MeetingGroupByDayMap.clear();
                meetingDayList.clear();
                noMeetingLayout.setVisibility(View.VISIBLE);
                page = 0;
                notifyDataChanged();
            }
        }
    };

    /**
     * 更新Adapter数据
     */
    public void notifyDataChanged() {
        if (adapter == null) {
            adapter = new MyAdapter();
            expandListView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * expandableListView适配器
     */
    public class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return meetingDayList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return MeetingGroupByDayMap.get(meetingDayList.get(groupPosition))
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return MeetingGroupByDayMap.get(meetingDayList.get(groupPosition));
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return MeetingGroupByDayMap.get(meetingDayList.get(groupPosition))
                    .get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            ExpandableListView expandableListView = (ExpandableListView) parent;
            expandableListView.expandGroup(groupPosition);
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(
                        MeetingHistorySearchActivity.this).inflate(
                        R.layout.meeting_list_group_item, null);
                holder = new ViewHolder();
                holder.dateText = (TextView) convertView
                        .findViewById(R.id.date_text);
                holder.weekText = (TextView) convertView
                        .findViewById(R.id.week_text);
                holder.todayText = (TextView) convertView
                        .findViewById(R.id.today_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String from = MeetingGroupByDayMap
                    .get(meetingDayList.get(groupPosition)).get(0).getFrom();
            Calendar fromCalendar = TimeUtils.timeString2Calendar(from);
            String week = TimeUtils.getWeekDay(getApplicationContext(),
                    fromCalendar);
            String date = TimeUtils.calendar2FormatString(
                    MeetingHistorySearchActivity.this, fromCalendar,
                    TimeUtils.FORMAT_YEAR_MONTH_DAY);
            holder.dateText.setText(date);
            holder.weekText.setText(week);
            if (TimeUtils.isCalendarToday(fromCalendar)) {
                holder.todayText.setText("(" + getString(R.string.today) + ")");
            } else {
                holder.todayText.setText("");
            }
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition,
                                 final int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            ExpandViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(
                        MeetingHistorySearchActivity.this).inflate(
                        R.layout.meeting_list_child_item, null);
                holder = new ExpandViewHolder();
                holder.meetingTopictext = (TextView) convertView
                        .findViewById(R.id.meeting_topic_text);
                holder.meetingNoticeText = (TextView) convertView
                        .findViewById(R.id.meeting_notice_text);
                holder.meetingTimeText = (TextView) convertView
                        .findViewById(R.id.meeting_time_text);
                holder.meetingRoom = (TextView) convertView
                        .findViewById(R.id.meeting_location_text);
                holder.meetingCardLineView = convertView
                        .findViewById(R.id.meeting_card_line);
                convertView.setTag(holder);
            } else {
                holder = (ExpandViewHolder) convertView.getTag();
            }
            Meeting meeting = MeetingGroupByDayMap.get(
                    meetingDayList.get(groupPosition)).get(childPosition);
            Room room = meeting.getRooms().get(0);
            String meetingTime = getTimeDuration(meeting);
            holder.meetingTopictext.setText(meeting.getTopic());
            holder.meetingTimeText.setText(meetingTime);
            holder.meetingRoom.setText(room.getRoomName() + " "
                    + room.getName());
            holder.meetingNoticeText.setText(meeting.getNotice());
            if (childPosition == MeetingGroupByDayMap.get(
                    meetingDayList.get(groupPosition)).size() - 1) {
                holder.meetingCardLineView.setVisibility(View.GONE);
            } else {
                holder.meetingCardLineView.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        class ViewHolder {
            TextView dateText;
            TextView weekText;
            TextView todayText;
        }

        class ExpandViewHolder {
            TextView meetingTopictext;
            TextView meetingTimeText;
            TextView meetingRoom;
            TextView meetingNoticeText;
            View meetingCardLineView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

    }

    /**
     * 初始化数据
     */
    private void initAndDisplayData() {
        MeetingGroupByDayMap = new ArrayMap<String, List<Meeting>>();
        meetingDayList = new ArrayList<String>();
        if (allMeetingList.size() == 0) {
            noMeetingLayout.setVisibility(View.VISIBLE);
        } else {
            noMeetingLayout.setVisibility(View.GONE);
            MeetingGroupByDayMap = GroupUtils.group(allMeetingList,
                    new MeetingGroupByDay());
            meetingDayList = new ArrayList<String>(
                    MeetingGroupByDayMap.keySet());
            Collections.sort(meetingDayList, new SortClass());

        }
        if (adapter == null) {
            adapter = new MyAdapter();
            expandListView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * 点击 时间监听，数据传递
     *
     * @author sunqx
     */
    class MeetingListListener implements OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                                    int groupPosition, int childPosition, long id) {
            Meeting meeting = MeetingGroupByDayMap.get(
                    meetingDayList.get(groupPosition)).get(childPosition);

            Bundle bundle = new Bundle();
            bundle.putSerializable("meeting", meeting);
            IntentUtils.startActivity(MeetingHistorySearchActivity.this,
                    MeetingDetailActivity.class, bundle);
            return false;
        }

    }

    /**
     * 得到格式化的时间如06:00-07:30
     *
     * @param meeting
     * @return
     */
    public String getTimeDuration(Meeting meeting) {
        String from = meeting.getFrom();
        String to = meeting.getTo();
        String dateFromTime = "", dateToTime = "";
        dateFromTime = getFromTime(from);
        dateToTime = getToTime(to);
        String meetingTime = dateFromTime + "-" + dateToTime;
        return meetingTime;
    }

    /**
     * 获取结束时间
     *
     * @return
     */
    private String getToTime(String to) {
        // TODO Auto-generated method stub
        Calendar calendarTo = TimeUtils.timeString2Calendar(to);
        String dateToTime = TimeUtils.calendar2FormatString(
                MeetingHistorySearchActivity.this, calendarTo,
                TimeUtils.DATE_FORMAT_HOUR_MINUTE);
        return dateToTime;
    }

    /**
     * @param from
     * @return
     */
    private String getFromTime(String from) {
        // TODO Auto-generated method stub
        Calendar calendFrom = TimeUtils.timeString2Calendar(from);
        String dateFromTime = TimeUtils.calendar2FormatString(
                MeetingHistorySearchActivity.this, calendFrom,
                TimeUtils.DATE_FORMAT_HOUR_MINUTE);
        return dateFromTime;
    }

    class MeetingGroupByDay implements GroupBy<String> {

        @Override
        public String groupBy(Object obj) {
            SimpleDateFormat format = new SimpleDateFormat(
                    getString(R.string.format_date_group_by));
            Meeting meeting = (Meeting) obj;
            String from = meeting.getFrom();
            Calendar calendarForm = TimeUtils.timeString2Calendar(from);
            String dateFromTime = TimeUtils.calendar2FormatString(
                    MeetingHistorySearchActivity.this, calendarForm, format);
            return dateFromTime;
        }

    }

    /**
     * 排序接口
     */
    public class SortClass implements Comparator {
        public int compare(Object arg0, Object arg1) {
            String dateA = (String) arg0;
            String dateB = (String) arg1;
            dateA = dateA.replace("-", "");
            dateB = dateB.replace("-", "");
            int fromA = Integer.parseInt(dateA);
            int fromB = Integer.parseInt(dateB);
            if (fromA > fromB) {
                return -1;
            } else if (fromA < fromB) {
                return 1;
            } else {
                return 0;
            }
        }
    }


    @Override
    public void onLoadMore() {
        getSearchMeetings(false, true);
    }

    /**
     * 获取会议列表
     *
     * @param isShowDlg
     * @param isLoadMore
     */
    private void getSearchMeetings(boolean isShowDlg, boolean isLoadMore) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show(isShowDlg);
            int requestPage = isLoadMore ? page + 1 : 0;
            apiService.getHistoryMeetingList(keyword, requestPage, LIMIT, isLoadMore);
        } else {
            swipeRefreshLayout.setLoading(false);
        }
    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnMeetingsSuccess(
                GetMeetingsResult getMeetingsResult, int page) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            MeetingHistorySearchActivity.this.page = page;
            swipeRefreshLayout.setLoading(false);
            List<Meeting> meetingList = getMeetingsResult.getMeetingsList();
            swipeRefreshLayout.setCanLoadMore(meetingList.size() == LIMIT);
            if (page != 0) {
                allMeetingList.addAll(allMeetingList.size(), meetingList);
            } else {
                allMeetingList = meetingList;
            }
            initAndDisplayData();
        }

        @Override
        public void returnMeetingsFail(String error, int errorCode) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            swipeRefreshLayout.setLoading(false);
            WebServiceMiddleUtils.hand(MeetingHistorySearchActivity.this,
                    error, errorCode);
        }

    }

}
