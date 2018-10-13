package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WebexAPIService;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexTKResult;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.net.URLEncoder;
import java.util.Calendar;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_detail)
public class WebexMeetingDetailActivity extends BaseActivity {

    public static final String EXTRA_WEBEXMEETING = "WebexMeeting";
    @ViewInject(R.id.bt_function)
    private Button functionBtn;
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
    @ViewInject(R.id.rl_host_key)
    private RelativeLayout hostKeyLayout;
    @ViewInject(R.id.tv_host_key)
    private TextView hostKeyText;
    @ViewInject(R.id.iv_delete)
    private ImageView deleteImg;

    private WebexMeeting webexMeeting;
    private boolean isOwner = false;
    private LoadingDialog loadingDialog;
    private WebexAPIService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(this);
        webexMeeting = (WebexMeeting) getIntent().getSerializableExtra(EXTRA_WEBEXMEETING);
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new WebService());
        getWebexMeeting();
    }

    private void showWebexMeetingDetial(){
        titleText.setText(webexMeeting.getConfName());
        Calendar startCalendar = webexMeeting.getStartDateCalendar();
        String timeYDM = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(MyApplication.getInstance(), TimeUtils.FORMAT_YEAR_MONTH_DAY));
        String week = TimeUtils.getWeekDay(MyApplication.getInstance(), startCalendar);
        String timeHrMin = TimeUtils.Calendar2TimeString(startCalendar, TimeUtils.getFormat(MyApplication.getInstance(), TimeUtils.FORMAT_HOUR_MINUTE));
        timeText.setText(timeYDM + " "+week+" "+timeHrMin);
        int duration = webexMeeting.getDuration();
        int hour = duration / 60;
        int min = duration % 60;
        String hourtext = (hour == 0)?"":hour + getString(R.string.hour);
        String mintext = (min == 0)?"":min + getString(R.string.min);
        durationText.setText("(" +hourtext+mintext+ ")");
        meetingPasswordText.setText(webexMeeting.getMeetingPassword());
        meetingIdText.setText(webexMeeting.getMeetingID());
        String photoUrl = APIUri.getWebexPhotoUrl(webexMeeting.getHostWebExID());
        ImageDisplayUtils.getInstance().displayImage(photoImg,photoUrl,R.drawable.icon_person_default);
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String myEmail = getMyInfoResult.getMail();
        isOwner = webexMeeting.getHostWebExID().equals(myEmail);
        deleteImg.setVisibility(isOwner?View.VISIBLE:View.INVISIBLE);
        ownerText.setText(isOwner?getString(R.string.mine):webexMeeting.getHostUserName());
        functionBtn.setText(isOwner?getString(R.string.webex_start):getString(R.string.join));
        hostKeyLayout.setVisibility(isOwner?View.VISIBLE:View.GONE);
        hostKeyText.setText(webexMeeting.getHostKey());
        boolean isMeetingEnd = isMeetingEnd();
        functionBtn.setEnabled(!isMeetingEnd);
        functionBtn.setTextColor(isMeetingEnd? Color.parseColor("#999999"):Color.parseColor("#ffffff"));
        functionBtn.setBackgroundColor(isMeetingEnd? Color.parseColor("#D7D7D7"):Color.parseColor("#0F7BCA"));
        functionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMeetingEnd()){
                    if (isOwner){
                        getWebexTK();
                    }else {
                        joinWebexMeeting();
                    }
                }else {
                    functionBtn.setEnabled(false);
                    functionBtn.setTextColor(Color.parseColor("#999999"));
                    functionBtn.setBackgroundColor( Color.parseColor("#D7D7D7"));
                    ToastUtils.show(MyApplication.getInstance(),R.string.webex_meeting_ended);
                }

            }
        });

    }

    private boolean isMeetingEnd(){
        return webexMeeting.getStartDateCalendar().getTimeInMillis()+webexMeeting.getDuration()*60000<=System.currentTimeMillis();
    }

    private void joinWebexMeeting(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("wbx://meeting"));
        intent.putExtra("MK",webexMeeting.getMeetingID());
        intent.putExtra("MPW","123123");
        startActivity(intent);
    }


    public void startWebexMeeting(String tk){
        try {
            String sessionTicket = URLEncoder.encode(tk,"UTF-8");
            String webexID = URLEncoder.encode(webexMeeting.getHostWebExID(),"UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            String uri = "wbx://meeting/inspurcloud.webex.com.cn/inspurcloud?MK="+webexMeeting.getMeetingID()+"&MPW="+webexMeeting.getMeetingPassword()+"&MTGTK=&sitetype=TRAIN&r2sec=1&UN="+webexID+"&TK="+sessionTicket;
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void onClick(View v){
        switch (v.getId()){
            case R.id.rl_back:
                finish();
                break;
            case R.id.bt_function:
                break;
            case R.id.iv_delete:
                showDeleteMeetingWarningDlg();

                break;
        }
    }

    private void showDeleteMeetingWarningDlg(){
        new MyQMUIDialog.MessageDialogBuilder(WebexMeetingDetailActivity.this)
                .setTitle(getString(R.string.remove_webex_meeting_warning_title))
                .setMessage(getString(R.string.remove_webex_meeting_warning_info))
                .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        removeWebexMeeting();
                    }
                })
                .show();
    }

    private void getWebexMeeting(){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
            loadingDialog.show();
            apiService.getWebexMeeting(webexMeeting.getMeetingID());
        }
    }

    private void getWebexTK(){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
            loadingDialog.show();
            apiService.getWebexTK();
        }
    }

    private void removeWebexMeeting(){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
            loadingDialog.show();
            apiService.removeMeeting(webexMeeting.getMeetingID());
        }
    }

    private class  WebService extends APIInterfaceInstance{
        @Override
        public void returnWebexMeetingSuccess(WebexMeeting webexMeeting) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebexMeetingDetailActivity.this.webexMeeting = webexMeeting;
            showWebexMeetingDetial();
        }

        @Override
        public void returnWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            finish();
        }

        @Override
        public void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            String tk = getWebexTKResult.getTk();
            startWebexMeeting(tk);
        }

        @Override
        public void returnWebexTKFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(),error,errorCode);
        }

        @Override
        public void returnRemoveWebexMeetingSuccess() {
            LoadingDialog.dimissDlg(loadingDialog);
            Intent intent=new Intent();
            intent.putExtra(EXTRA_WEBEXMEETING,webexMeeting);
            setResult(RESULT_OK,intent);
            finish();
        }

        @Override
        public void returnRemoveWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(),error,errorCode);
        }
    }
}
