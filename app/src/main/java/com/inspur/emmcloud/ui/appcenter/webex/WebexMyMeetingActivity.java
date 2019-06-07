package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.WebexMeetingAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WebexAPIService;
import com.inspur.emmcloud.baselib.util.GroupUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexMeetingListResult;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexTKResult;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.util.privates.AppDownloadUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/10/11.
 */
public class WebexMyMeetingActivity extends BaseActivity {
    private static final int REQUEST_SCHEDULE_WEBEX_MEETING = 1;
    private static final int REQUEST_REMOVE_WEBEX_MEETING = 1;
    private final String webexAppPackageName = "com.cisco.webex.meetings";
    @BindView(R.id.srl)
    MySwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.elv_meeting)
    ExpandableListView expandListView;
    @BindView(R.id.ll_no_meeting)
    LinearLayout noMeetingLayout;
    @BindView(R.id.rl_mask)
    RelativeLayout maskLayout;
    @BindView(R.id.tv_no_meeting)
    TextView noMeetingText;
    private WebexMeetingAdapter adapter;
    private WebexAPIService apiService;
    private List<WebexMeeting> webexMeetingList = new ArrayList<>();
    private Map<String, List<WebexMeeting>> webexMeetingMap = new HashMap<>();
    private List<String> webexMeetingGroupList = new ArrayList<>();
    private LoadingDialog loadingDlg;
    private WebexMeeting webexMeetingOpen;
    private Timer timer;
    private Handler handler;
    private TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
        getWxMeetingList(true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_webex_my_meeting;
    }

    private void initData() {
        webexMeetingMap = GroupUtils.group(webexMeetingList, new WebexMeetingGroup());
        if (webexMeetingMap == null) {
            webexMeetingMap = new HashMap<>();
        }
        if (webexMeetingMap.size() > 0) {
            webexMeetingGroupList = new ArrayList<>(webexMeetingMap.keySet());
            Collections.sort(webexMeetingGroupList, new SortClass());
        } else {
            webexMeetingGroupList.clear();
        }
        adapter.setData(webexMeetingGroupList, webexMeetingMap);
        adapter.notifyDataSetChanged();

    }

    private void initView() {
        boolean isFirstEnter = PreferencesUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_WEBEX_FIRST_ENTER, true);
        maskLayout.setVisibility(isFirstEnter ? View.VISIBLE : View.GONE);
        String installUri = getIntent().getStringExtra("installUri");
        if (StringUtils.isBlank(installUri)) {
            installUri = "https://m.webex.com/downloads/android/touchscreen/mc.apk";
        }
        PreferencesUtils.putString(MyApplication.getInstance(), Constant.PREF_WEBEX_DOWNLOAD_URL, installUri);
        loadingDlg = new LoadingDialog(this);
        setNoMeetingTipsText();
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg_blue), getResources().getColor(R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getWxMeetingList(false);
            }
        });
        expandListView.setGroupIndicator(null);
        expandListView.setVerticalScrollBarEnabled(false);
        expandListView.setHeaderDividersEnabled(false);
        adapter = new WebexMeetingAdapter(this);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (webexMeetingList.size() > 0) {
                    noMeetingLayout.setVisibility(View.GONE);
                } else {
                    noMeetingLayout.setVisibility(View.VISIBLE);
                    setNoMeetingTipsText();
                }
            }
        });
        expandListView.setAdapter(adapter);
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                WebexMeeting webexMeeting = webexMeetingMap.get(webexMeetingGroupList.get(groupPosition)).get(childPosition);
                Intent intent = new Intent(WebexMyMeetingActivity.this, WebexMeetingDetailActivity.class);
                intent.putExtra(WebexMeetingDetailActivity.EXTRA_WEBEXMEETING, webexMeeting);
                startActivityForResult(intent, REQUEST_REMOVE_WEBEX_MEETING);
                return false;
            }
        });
        adapter.setFounctionBtnClickListener(new WebexMeetingAdapter.OnFunctionBtnClickListener() {
            @Override
            public void onFunctionClick(Button functionBtn, int groupPosition, int childPosition) {
                if (AppUtils.isAppInstalled(MyApplication.getInstance(), webexAppPackageName)) {
                    WebexMeeting webexMeeting = webexMeetingMap.get(webexMeetingGroupList.get(groupPosition)).get(childPosition);
                    webexMeetingOpen = webexMeeting;
                    boolean isMeetingEnd = webexMeeting.getStartDateCalendar().getTimeInMillis() + webexMeeting.getDuration() * 60000 <= System.currentTimeMillis();
                    if (!isMeetingEnd) {
                        getWebexMeeting();

                    } else {
                        functionBtn.setEnabled(false);
                        functionBtn.setTextColor(Color.parseColor("#999999"));
                        functionBtn.setBackground(ContextCompat.getDrawable(MyApplication.getInstance(), R.drawable.shape_webex_buttion_add_disable));
                        ToastUtils.show(WebexMyMeetingActivity.this, R.string.webex_meeting_ended);
                    }
                } else {
                    showInstallDialog();
                }
            }
        });
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new WebService());
        handMessage();
    }

    private void setNoMeetingTipsText() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        String date = TimeUtils.Calendar2TimeString(calendar, TimeUtils.getFormat(WebexMyMeetingActivity.this, TimeUtils.FORMAT_MONTH_DAY));
        noMeetingText.setText(Html.fromHtml(getString(R.string.webex_no_meeting_tips, date)));
    }

    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                adapter.notifyDataSetChanged();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUserGuideMask();
    }


    private void showUserGuideMask() {
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (timer == null) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    if (handler != null) {
                        handler.sendEmptyMessage(1);
                    }
                }
            };
        }
        timer.schedule(task, 10000, 10000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        timer = null;
    }

    private boolean isOwner(WebexMeeting webexMeeting) {
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        String myEmail = getMyInfoResult.getMail();
        return webexMeeting.getHostWebExID().equals(myEmail);

    }

    private void joinWebexMeeting(WebexMeeting webexMeeting) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("wbx://meeting"));
            intent.putExtra("MK", webexMeeting.getMeetingID());
            intent.putExtra("MPW", webexMeeting.getMeetingPassword());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startWebexMeeting(String tk) {
        try {
            String sessionTicket = URLEncoder.encode(tk, "UTF-8");
            String webexID = URLEncoder.encode(webexMeetingOpen.getHostWebExID(), "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            String uri = "wbx://meeting/inspurcloud.webex.com.cn/inspurcloud?MK=" + webexMeetingOpen.getMeetingID() + "&MPW=" + webexMeetingOpen.getMeetingPassword() + "&MTGTK=&sitetype=TRAIN&r2sec=1&UN=" + webexID + "&TK=" + sessionTicket;
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 安装提示
     */
    private void showInstallDialog() {
        new CustomDialog.MessageDialogBuilder(WebexMyMeetingActivity.this)
                .setMessage(getString(R.string.webex_install_tips))
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    String downloadUrl = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_WEBEX_DOWNLOAD_URL, "");
                    new AppDownloadUtils().showDownloadDialog(WebexMyMeetingActivity.this, downloadUrl);
                })
                .show();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_add_meeting:
                Intent intent = new Intent(this, WebexScheduleMeetingActivity.class);
                startActivityForResult(intent, REQUEST_SCHEDULE_WEBEX_MEETING);
                break;
            case R.id.iv_schedule_ok:
                maskLayout.setVisibility(View.GONE);
                PreferencesUtils.putBoolean(MyApplication.getInstance(), Constant.PREF_WEBEX_FIRST_ENTER, false);
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SCHEDULE_WEBEX_MEETING) {
                getWxMeetingList(false);
            } else if (requestCode == REQUEST_REMOVE_WEBEX_MEETING) {
                WebexMeeting webexMeeting = (WebexMeeting) data.getSerializableExtra(WebexMeetingDetailActivity.EXTRA_WEBEXMEETING);
                webexMeetingList.remove(webexMeeting);
                initData();
            }
        }
    }

    public void getWxMeetingList(boolean isShowRefresh) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            if (isShowRefresh) {
                swipeRefreshLayout.setRefreshing(true);
            }
            apiService.getWebexMeetingList();
        }
    }

    private void getWebexMeeting() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.getWebexMeeting(webexMeetingOpen.getMeetingID());
        }
    }

    private void getWebexTK() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            apiService.getWebexTK();
        } else {
            LoadingDialog.dimissDlg(loadingDlg);
        }
    }

    /**
     * 分类接口实现
     */
    class WebexMeetingGroup implements GroupUtils.GroupBy<String> {
        @Override
        public String groupBy(Object obj) {
            WebexMeeting webexMeeting = (WebexMeeting) obj;
            SimpleDateFormat format = new SimpleDateFormat(
                    getString(R.string.format_date_group_by));
            String dateString = TimeUtils.calendar2FormatString(WebexMyMeetingActivity.this, webexMeeting.getStartDateCalendar(), format);
            return dateString;
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
                return 1;
            } else if (fromA < fromB) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnWebexMeetingSuccess(WebexMeeting webexMeeting) {
            webexMeetingOpen = webexMeeting;
            if (isOwner(webexMeeting)) {
                getWebexTK();
            } else {
                LoadingDialog.dimissDlg(loadingDlg);
                joinWebexMeeting(webexMeetingOpen);
            }

        }

        @Override
        public void returnWebexMeetingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }


        @Override
        public void returnWebexMeetingListSuccess(GetWebexMeetingListResult getWebexMeetingListResult) {
            swipeRefreshLayout.setRefreshing(false);
            webexMeetingList = getWebexMeetingListResult.getWebexMeetingList();
            initData();
        }

        @Override
        public void returnWebexMeetingListFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

        @Override
        public void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            String tk = getWebexTKResult.getTk();
            startWebexMeeting(tk);
        }

        @Override
        public void returnWebexTKFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }
    }
}
