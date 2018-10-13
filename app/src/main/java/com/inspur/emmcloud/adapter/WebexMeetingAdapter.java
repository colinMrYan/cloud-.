package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;

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

    public WebexMeetingAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<String> webexMeetingGroupList,Map<String, List<WebexMeeting>> webexMeetingMap){
        this.webexMeetingGroupList =webexMeetingGroupList;
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
        List<WebexMeeting> webexMeetingList =  webexMeetingMap.get(webexMeetingGroupList.get(groupPosition));
        Calendar calendar = webexMeetingList.get(0).getStartDateCalendar();
        ExpandableListView expandableListView = (ExpandableListView) parent;
        expandableListView.expandGroup(groupPosition);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_view_webex_meeting_group, null);
        TextView weekText = (TextView) convertView.findViewById(R.id.tv_week);
        TextView dateText = (TextView) convertView.findViewById(R.id.tv_date);
        int textColor = -1;
        if (TimeUtils.isCalendarToday(calendar)){
            textColor = ContextCompat.getColor(context,R.color.header_bg);
            weekText.setText(R.string.today);
        }else {
            textColor = Color.parseColor("#333333");
            weekText.setText(TimeUtils.getWeekDay(context,calendar));
        }
        weekText.setTextColor(textColor);
        dateText.setTextColor(textColor);
        dateText.setText(TimeUtils.calendar2FormatString(MyApplication.getInstance(),calendar,TimeUtils.FORMAT_YEAR_MONTH_DAY));
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition,
                             final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        ExpandViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_view_webex_meeting_child, null);
            holder = new ExpandViewHolder();
            holder.photoImg = (ImageView)convertView.findViewById(R.id.iv_photo);
            holder.timeText = (TextView) convertView
                    .findViewById(R.id.tv_time);
            holder.titleText = (TextView) convertView
                    .findViewById(R.id.tv_title);
            holder.ownerText = (TextView) convertView
                    .findViewById(R.id.tv_owner);
            holder.line = convertView.findViewById(R.id.v_line);
            convertView.setTag(holder);
        } else {
            holder = (ExpandViewHolder) convertView.getTag();
        }
        List<WebexMeeting> webexMeetingList = webexMeetingMap.get(webexMeetingGroupList.get(groupPosition));
        WebexMeeting webexMeeting = webexMeetingList.get(childPosition);
        Calendar startCalendar = webexMeeting.getStartDateCalendar();
        String startDateString = TimeUtils.getTime(context,startCalendar.getTimeInMillis(),TimeUtils.FORMAT_HOUR_MINUTE);
        String endDateString = TimeUtils.getTime(context,startCalendar.getTimeInMillis()+60000*webexMeeting.getDuration(),TimeUtils.FORMAT_HOUR_MINUTE);
        holder.timeText.setText(startDateString+" - "+endDateString);
        holder.titleText.setText(webexMeeting.getConfName());
        String email = webexMeeting.getHostWebExID();
        if (email.equals(myEmail)){
            holder.ownerText.setText(R.string.mine);
        }else {
            holder.ownerText.setText(webexMeeting.getHostUserName());
        }
        holder.line.setVisibility(isLastChild?View.INVISIBLE:View.VISIBLE);
        String photoUrl = APIUri.getWebexPhotoUrl(webexMeeting.getHostWebExID());
        ImageDisplayUtils.getInstance().displayImage(holder.photoImg,photoUrl,R.drawable.icon_person_default);
        return convertView;
    }

    class ExpandViewHolder {
        ImageView photoImg;
        TextView timeText;
        TextView titleText;
        TextView ownerText;
        View line;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

