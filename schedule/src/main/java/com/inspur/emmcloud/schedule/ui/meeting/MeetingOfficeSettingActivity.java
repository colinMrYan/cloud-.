package com.inspur.emmcloud.schedule.ui.meeting;

import android.view.View;
import android.widget.ExpandableListView;

import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.adapter.MeetingOfficeAdapter;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.api.ScheduleAPIService;
import com.inspur.emmcloud.schedule.bean.meeting.Building;
import com.inspur.emmcloud.schedule.bean.meeting.GetLocationResult;
import com.inspur.emmcloud.schedule.bean.meeting.MeetingLocation;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/4/15.
 */

public class MeetingOfficeSettingActivity extends BaseActivity implements ExpandableListView.OnChildClickListener {

    @BindView(R.id.expandable_listView)
    ExpandableListView expandableListView;
    private LoadingDialog loadingDlg;
    private ScheduleAPIService apiService;
    private List<MeetingLocation> locationList = new ArrayList<>();
    private MeetingOfficeAdapter adapter;
    private boolean isMeetingOfficeChanged = false;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
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
        adapter = new MeetingOfficeAdapter(this);
        expandableListView.setAdapter(adapter);
        apiService = new ScheduleAPIService(this);
        apiService.setAPIInterface(new WebService());
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Building building = locationList.get(groupPosition).getOfficeBuildingList().get(childPosition);
        if (building.isFavorite() == true) {
            cancelCommonBuilding(building);
        } else {
            setCommonBuilding(building);
        }
        return false;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.ibt_back) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (isMeetingOfficeChanged) {
            setResult(RESULT_OK);
        }
        finish();
    }


    private void getMeetingLocation() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDlg.show();
            apiService.getMeetingLocation();
        }
    }

    /**
     * 设置常用地点
     */
    private void setCommonBuilding(Building building) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDlg.show();
            apiService.setMeetingCommonBuilding(building);
        }
    }

    /**
     * 取消常用地点
     **/
    private void cancelCommonBuilding(Building building) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            loadingDlg.show();
            apiService.cancelMeetingCommonBuilding(building);
        }
    }

    private void changeBuildingIsFavoriteState(Building building) {
        for (int i = 0; i < locationList.size(); i++) {
            for (int j = 0; j < locationList.get(i).getOfficeBuildingList().size(); j++) {
                if (locationList.get(i).getOfficeBuildingList().get(j).getId().equals(building.getId())) {
                    locationList.get(i).getOfficeBuildingList().get(j).setFavorite(!building.isFavorite());
                }
            }
        }
    }

    private class WebService extends ScheduleAPIInterfaceImpl {
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
        public void returnCancelMeetingCommonBuildingSuccess(Building building) {
            isMeetingOfficeChanged = true;
            LoadingDialog.dimissDlg(loadingDlg);
            changeBuildingIsFavoriteState(building);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnCancelMeetingCommonBuildingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            super.returnCancelMeetingCommonBuildingFail(error, errorCode);
    }

        @Override
        public void returnSetMeetingCommonBuildingSuccess(Building building) {
            isMeetingOfficeChanged = true;
            LoadingDialog.dimissDlg(loadingDlg);
            changeBuildingIsFavoriteState(building);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnSetMeetingCommonBuildingFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            super.returnSetMeetingCommonBuildingFail(error, errorCode);
        }
    }
}
