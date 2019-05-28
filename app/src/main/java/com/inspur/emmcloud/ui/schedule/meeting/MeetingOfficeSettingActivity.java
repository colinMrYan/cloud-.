package com.inspur.emmcloud.ui.schedule.meeting;

import java.util.ArrayList;
import java.util.List;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MeetingOfficeAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.meeting.Building;
import com.inspur.emmcloud.bean.schedule.meeting.GetLocationResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetOfficeListResult;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingLocation;
import com.inspur.emmcloud.bean.schedule.meeting.Office;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import android.view.View;
import android.widget.ExpandableListView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/4/15.
 */

public class MeetingOfficeSettingActivity extends BaseActivity implements ExpandableListView.OnChildClickListener {
    @BindView(R.id.expandable_listView)
    ExpandableListView expandableListView;
    private LoadingDialog loadingDlg;
    private ScheduleApiService apiService;
    private List<MeetingLocation> locationList = new ArrayList<>();
    private MeetingOfficeAdapter adapter;
    private List<Office> officeList = new ArrayList<>();
    private List<String> officeIdList = new ArrayList<>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        getMyMeetingOfficeIdList();
        initView();
        getOfficeList();
        getMeetingLocation();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_meeting_office_setting;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        expandableListView.setGroupIndicator(null);
        expandableListView.setVerticalScrollBarEnabled(false);
        expandableListView.setHeaderDividersEnabled(false);
        expandableListView.setOnChildClickListener(this);
        adapter = new MeetingOfficeAdapter(this, officeList);
        expandableListView.setAdapter(adapter);
        apiService = new ScheduleApiService(this);
        apiService.setAPIInterface(new WebService());
    }

    private void getMyMeetingOfficeIdList() {
        String officeIdListJson = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MEETING_OFFICE_ID_LIST, null);
        if (officeIdListJson != null) {
            officeIdList = JSONUtils.JSONArray2List(officeIdListJson, new ArrayList<String>());
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Building building = locationList.get(groupPosition).getOfficeBuildingList().get(childPosition);
        Office office = getBuildingOfOffice(building);
        if (office != null) {
            deleteOffice(office);
        } else {
            addOffice(building);
        }
        return false;
    }

    private Office getBuildingOfOffice(Building building) {
        for (Office office : officeList) {
            if (office.getOfficeBuilding().getId().equals(building.getId())) {
                return office;
            }
        }
        return null;
    }


    public void onClick(View view) {
        if (view.getId() == R.id.ibt_back) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    private void getOfficeList() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.getOfficeList();
        }
    }

    private void getMeetingLocation() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.getMeetingLocation();
        }
    }

    private void addOffice(Building building) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.addMeetingOffice(building);
        }
    }


    private void deleteOffice(Office office) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.deleteMeetingOffice(office);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnLocationResultSuccess(GetLocationResult getLocationResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            locationList = getLocationResult.getLocList();
            adapter.setData(locationList);

        }

        @Override
        public void returnLocationResultFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingOfficeSettingActivity.this, error, errorCode);
        }

        @Override
        public void returnAddMeetingOfficeSuccess(Office office, Building building) {
            LoadingDialog.dimissDlg(loadingDlg);
            officeIdList.add(office.getId());
            officeList.add(office);
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MEETING_OFFICE_ID_LIST, JSONUtils.toJSONString(officeIdList));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnAddMeetingOfficeFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingOfficeSettingActivity.this, error, errorCode);
        }

        @Override
        public void returnDeleteOfficeSuccess(Office office) {
            LoadingDialog.dimissDlg(loadingDlg);
            officeList.remove(office);
            officeIdList.remove(office.getId());
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MEETING_OFFICE_ID_LIST, JSONUtils.toJSONString(officeIdList));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnDeleteOfficeFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingOfficeSettingActivity.this, error, errorCode);
        }

        @Override
        public void returnOfficeListResultSuccess(GetOfficeListResult getOfficeListResult) {
            officeList.clear();
            officeList.addAll(getOfficeListResult.getOfficeList());
            officeIdList = getOfficeListResult.getOfficeIdList();
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MEETING_OFFICE_ID_LIST, JSONUtils.toJSONString(officeIdList));
            adapter.notifyDataSetChanged();
        }


        @Override
        public void returnOfficeListResultFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(MeetingOfficeSettingActivity.this, error, errorCode);
        }
    }
}
