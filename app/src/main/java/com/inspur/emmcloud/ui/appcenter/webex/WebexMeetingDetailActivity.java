package com.inspur.emmcloud.ui.appcenter.webex;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.Calendar;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_detail)
public class WebexMeetingDetailActivity extends BaseActivity {

    public static final String EXTRA_WEBEXMEETING = "WebexMeeting";
    @ViewInject(R.id.bt_function)
    private QMUIRoundButton functionBtn;
    @ViewInject(R.id.iv_photo)
    private ImageView photoImg;
    @ViewInject(R.id.tv_title)
    private TextView titleText;
    @ViewInject(R.id.tv_owner)
    private TextView ownerText;
    @ViewInject(R.id.tv_time)
    private TextView timeText;
    @ViewInject(R.id.tv_duration)
    private TextView durationText;
    @ViewInject(R.id.tv_meeting_id)
    private TextView meetingIdText;
    @ViewInject(R.id.tv_meeting_password)
    private TextView meetingPasswordText;
    private WebexMeeting webexMeeting;
    private boolean isOwner = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webexMeeting = (WebexMeeting) getIntent().getSerializableExtra(EXTRA_WEBEXMEETING);
        titleText.setText(webexMeeting.getConfName());
        Calendar startCalendar = webexMeeting.getStartDateCalendar();
        String timeYDM = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(MyApplication.getInstance(), TimeUtils.FORMAT_YEAR_MONTH_DAY));
        String week = TimeUtils.getWeekDay(MyApplication.getInstance(), startCalendar);
        timeText.setText(timeYDM + week);
        int duration = webexMeeting.getDuration();
        int hour = duration / 60;
        int min = duration % 60;
        String hourtext = (hour == 0)?"":hour + "小时";
        String mintext = (min == 0)?"":min + "分钟";
        durationText.setText("(" +hourtext+mintext+ ")");
        meetingPasswordText.setText(webexMeeting.getMeetingPassword());
        meetingIdText.setText(webexMeeting.getMeetingID());
        String photoUrl = "https://emm.inspur.com/img/userhead/"+webexMeeting.getHostWebExID();
        ImageDisplayUtils.getInstance().displayImage(photoImg,photoUrl,R.drawable.icon_person_default);
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String myEmail = getMyInfoResult.getMail();
        isOwner = webexMeeting.getHostWebExID().equals(myEmail);
        ownerText.setText(isOwner?getString(R.string.mine):webexMeeting.getHostWebExID());

        functionBtn.setText(isOwner?"开始":"加入");
        boolean isMeetingEnd = webexMeeting.getStartDateCalendar().getTimeInMillis()+duration*6000<=System.currentTimeMillis();
        functionBtn.setEnabled(!isMeetingEnd);
        functionBtn.set
        functionBtn.setBackgroundColor(isMeetingEnd? Color.parseColor("#D7D7D7"):Color.parseColor("#0F7BCA"));
    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.rl_back:
                finish();
                break;
            case R.id.bt_function:
                break;
        }
    }
}
