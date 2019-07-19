package com.inspur.emmcloud.webex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.webex.R;
import com.inspur.emmcloud.webex.api.WebexAPIUri;
import com.inspur.emmcloud.webex.bean.WebexMeeting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenmch on 2018/10/11.
 */

public class WebexMeetingAdapter extends BaseExpandableListAdapter {
    private Context context;
    private Map<String, List<WebexMeeting>> webexMeetingMap = new HashMap<>();
    private List<String> webexMeetingGroupList = new ArrayList<>();
    private String myEmail;
    private OnFunctionBtnClickListener onFunctionBtnClickListener;

    public WebexMeetingAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<String> webexMeetingGroupList, Map<String, List<WebexMeeting>> webexMeetingMap) {
        this.webexMeetingGroupList = webexMeetingGroupList;
        this.webexMeetingMap = webexMeetingMap;
        String myInfo = PreferencesUtils.getString(context, "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        myEmail = getMyInfoResult.getMail();
    }

    @Override
    public int getGroupCount() {
        return webexMeetingGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<WebexMeeting> webexMeetingList = webexMeetingMap.get(webexMeetingGroupList.get(groupPosition));
        return webexMeetingList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
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
        List<WebexMeeting> webexMeetingList = webexMeetingMap.get(webexMeetingGroupList.get(groupPosition));
        Calendar calendar = webexMeetingList.get(0).getStartDateCalendar();
        ExpandableListView expandableListView = (ExpandableListView) parent;
        expandableListView.expandGroup(groupPosition);
        convertView = LayoutInflater.from(context).inflate(R.layout.webex_item_view_meeting_group, null);
        TextView todayText = (TextView) convertView.findViewById(R.id.tv_today);
        TextView dateText = (TextView) convertView.findViewById(R.id.tv_date);
        String timeDate = TimeUtils.calendar2FormatString(context, calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
        String timeWeek = TimeUtils.getWeekDay(context, calendar);
        todayText.setVisibility(TimeUtils.isCalendarToday(calendar) ? View.VISIBLE : View.INVISIBLE);
        dateText.setText(timeDate + " " + timeWeek);
        return convertView;
    }

    public void setFounctionBtnClickListener(OnFunctionBtnClickListener onFunctionBtnClickListener) {
        this.onFunctionBtnClickListener = onFunctionBtnClickListener;
    }

    @Override
    public View getChildView(final int groupPosition,
                             final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        ExpandViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.webex_item_view_meeting_child, null);
            holder = new ExpandViewHolder();
            holder.photoImg = (CircleTextImageView) convertView.findViewById(R.id.iv_photo);
            holder.timeText = (TextView) convertView
                    .findViewById(R.id.tv_time);
            holder.titleText = (TextView) convertView
                    .findViewById(R.id.tv_name_tips);
            holder.ownerText = (TextView) convertView
                    .findViewById(R.id.tv_owner);
            holder.line = convertView.findViewById(R.id.v_line);
            holder.functionBtn = (Button) convertView.findViewById(R.id.bt_function);
            convertView.setTag(holder);
        } else {
            holder = (ExpandViewHolder) convertView.getTag();
        }
        List<WebexMeeting> webexMeetingList = webexMeetingMap.get(webexMeetingGroupList.get(groupPosition));
        WebexMeeting webexMeeting = webexMeetingList.get(childPosition);
        Calendar startCalendar = webexMeeting.getStartDateCalendar();
        String startDateString = TimeUtils.getTime(context, startCalendar.getTimeInMillis(), TimeUtils.FORMAT_HOUR_MINUTE);
        String endDateString = TimeUtils.getTime(context, startCalendar.getTimeInMillis() + 60000 * webexMeeting.getDuration(), TimeUtils.FORMAT_HOUR_MINUTE);
        holder.timeText.setText(startDateString + " - " + endDateString);
        holder.titleText.setText(webexMeeting.getConfName());
        String email = webexMeeting.getHostWebExID();
        boolean isOwner = email.equals(myEmail);
        holder.ownerText.setText(isOwner ? context.getString(R.string.mine) : webexMeeting.getHostUserName());
        if (TimeUtils.isCalendarToday(startCalendar) && !isMeetingEnd(webexMeeting) && (isOwner || webexMeeting.isInProgress())) {
            holder.functionBtn.setText(email.equals(myEmail) ? context.getString(R.string.webex_start) : context.getString(R.string.join));
            holder.functionBtn.setVisibility(View.VISIBLE);
            holder.functionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onFunctionBtnClickListener != null) {
                        onFunctionBtnClickListener.onFunctionClick((Button) v, groupPosition, childPosition);
                    }
                }
            });
        } else {
            holder.functionBtn.setVisibility(View.GONE);
        }
        holder.line.setVisibility(isLastChild ? View.INVISIBLE : View.VISIBLE);
        String photoUrl = WebexAPIUri.getWebexPhotoUrl(webexMeeting.getHostWebExID());
        ImageDisplayUtils.getInstance().displayImage(holder.photoImg, photoUrl, R.drawable.icon_person_default);
        return convertView;
    }

    private boolean isMeetingEnd(WebexMeeting webexMeeting) {
        return webexMeeting.getStartDateCalendar().getTimeInMillis() + webexMeeting.getDuration() * 60000 <= System.currentTimeMillis();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface OnFunctionBtnClickListener {
        void onFunctionClick(Button button, int groupPosition, int childPosition);
    }

    class ExpandViewHolder {
        CircleTextImageView photoImg;
        TextView timeText;
        TextView titleText;
        TextView ownerText;
        View line;
        Button functionBtn;
    }

    ;

}

