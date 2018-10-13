package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.WebexMeetingAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WebexAPIService;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexMeetingListResult;
import com.inspur.emmcloud.bean.appcenter.webex.WebexMeeting;
import com.inspur.emmcloud.util.common.GroupUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_my_meeting)
public class WebexMyMeetingActivity extends BaseActivity {
    private static final int REQUEST_SCHEDULE_WEBEX_MEETING = 1;
    private static final int REQUEST_REMOVE_WEBEX_MEETING = 1;
    @ViewInject(R.id.srl)
    private MySwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.elv_meeting)
    private ExpandableListView expandListView;
    @ViewInject(R.id.ll_no_meeting)
    private LinearLayout noMeetingLayout;
    private WebexMeetingAdapter adapter;
    private WebexAPIService apiService;
    private List<WebexMeeting> webexMeetingList = new ArrayList<>();
    private Map<String, List<WebexMeeting>> webexMeetingMap = new HashMap<>();
    private List<String> webexMeetingGroupList = new ArrayList<>();
    private LoadingDialog loadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getWxMeetingList(true);

    }

    private void initData() {
        webexMeetingMap = GroupUtils.group(webexMeetingList, new WebexMeetingGroup());
        if (webexMeetingMap == null){
            webexMeetingMap = new HashMap<>();
        }
        if (webexMeetingMap.size() > 0) {
            webexMeetingGroupList = new ArrayList<>(webexMeetingMap.keySet());
            Collections.sort(webexMeetingGroupList, new SortClass());
        }else {
            webexMeetingGroupList.clear();
        }
        adapter.setData(webexMeetingGroupList, webexMeetingMap);
        adapter.notifyDataSetChanged();

    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
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
        expandListView.setAdapter(adapter);
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                WebexMeeting webexMeeting = webexMeetingMap.get(webexMeetingGroupList.get(groupPosition)).get(childPosition);
                Intent intent = new Intent(WebexMyMeetingActivity.this, WebexMeetingDetailActivity.class);
                intent.putExtra(WebexMeetingDetailActivity.EXTRA_WEBEXMEETING,webexMeeting);
                startActivityForResult(intent,REQUEST_REMOVE_WEBEX_MEETING);
                return false;
            }
        });
        apiService = new WebexAPIService(this);
        apiService.setAPIInterface(new WebService());
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.iv_add_meeting:
            case R.id.bt_add_meeting:
                Intent intent = new Intent(this, WebexScheduleMeetingActivity.class);
                startActivityForResult(intent,REQUEST_SCHEDULE_WEBEX_MEETING);
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode ==  REQUEST_SCHEDULE_WEBEX_MEETING){
                getWxMeetingList(false);
            }else if(requestCode == REQUEST_REMOVE_WEBEX_MEETING){
                WebexMeeting webexMeeting = (WebexMeeting) data.getSerializableExtra(WebexMeetingDetailActivity.EXTRA_WEBEXMEETING);
                webexMeetingList.remove(webexMeeting);
                initData();
            }
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
            String dateString = TimeUtils.calendar2FormatString(MyApplication.getInstance(), webexMeeting.getStartDateCalendar(),format);
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


    public void getWxMeetingList(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show(isShowDlg);
            apiService.getWebexMeetingList();
        }else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnWebexMeetingListSuccess(GetWebexMeetingListResult getWebexMeetingListResult) {
            swipeRefreshLayout.setRefreshing(false);
            LoadingDialog.dimissDlg(loadingDlg);
            webexMeetingList = getWebexMeetingListResult.getWebexMeetingList();
            initData();
        }

        @Override
        public void returnWebexMeetingListFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }
    }
}
