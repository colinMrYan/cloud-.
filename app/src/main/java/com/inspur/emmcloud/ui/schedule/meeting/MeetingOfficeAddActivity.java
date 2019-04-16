package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MeetingOfficeAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.meeting.Building;
import com.inspur.emmcloud.bean.schedule.meeting.MeetingLocation;
import com.inspur.emmcloud.bean.work.GetAddOfficeResult;
import com.inspur.emmcloud.bean.work.GetLocationResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/15.
 */

@ContentView(R.layout.activity_meeting_office_add)
public class MeetingOfficeAddActivity extends BaseActivity implements ExpandableListView.OnChildClickListener{
    @ViewInject(R.id.expandable_listView)
    private ExpandableListView expandableListView;
    private LoadingDialog loadingDlg;
    private ScheduleApiService apiService;
    private List<MeetingLocation> locationList = new ArrayList<>();
    private MeetingOfficeAdapter adapter;
    private List<String> officeIdList= new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMyMeetingOfficeIdList();
        initView();
        getMeetingLocation();
    }

    private void initView(){
        loadingDlg= new LoadingDialog(this);
        expandableListView.setGroupIndicator(null);
        expandableListView.setVerticalScrollBarEnabled(false);
        expandableListView.setHeaderDividersEnabled(false);
        expandableListView.setOnChildClickListener(this);
        adapter = new MeetingOfficeAdapter(this,officeIdList);
        expandableListView.setAdapter(adapter);
        apiService = new ScheduleApiService(this);
        apiService.setAPIInterface(new WebService());
    }

    private void getMyMeetingOfficeIdList(){
        String officeIdListJson = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MEETING_OFFICE_ID_LIST, null);
        LogUtils.jasonDebug("officeIdListJson=="+officeIdListJson);
        if (officeIdListJson != null) {
            officeIdList = JSONUtils.JSONArray2List(officeIdListJson, new ArrayList<String>());
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Building building = locationList.get(groupPosition).getOfficeBuildingList().get(childPosition);
        boolean isSelect = officeIdList.contains(building.getId());
        if (isSelect){
            deleteOffice(building);
        }else {
            addOffice(building);
        }
        return false;
    }



    public void onClick(View view){
        if (view.getId() == R.id.ibt_back){
            finish();
        }
    }

    private void getMeetingLocation(){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
            loadingDlg.show();
            apiService.getMeetingLoction();
        }
    }

    private void addOffice(Building building){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
            loadingDlg.show();
            apiService.addMeetingOffice(building);
        }
    }


    private void deleteOffice(Building building){
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
            loadingDlg.show();
            apiService.deleteMeetingOffice(building);
        }
    }

    private class WebService extends APIInterfaceInstance{
        @Override
        public void returnLocationResultSuccess(GetLocationResult getLocationResult) {
           LoadingDialog.dimissDlg(loadingDlg);
           locationList = getLocationResult.getLocList();
            adapter.setData(locationList);

        }

        @Override
        public void returnLocationResultFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingOfficeAddActivity.this, error, errorCode);
        }

        @Override
        public void returnAddMeetingOfficeSuccess(GetAddOfficeResult getCreateOfficeResult, Building building) {
            LoadingDialog.dimissDlg(loadingDlg);
            officeIdList.add(building.getId());
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MEETING_OFFICE_ID_LIST, JSONUtils.toJSONString(officeIdList));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnAddMeetingOfficeFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingOfficeAddActivity.this, error, errorCode);
        }

        @Override
        public void returnDeleteOfficeSuccess(Building building) {
            LoadingDialog.dimissDlg(loadingDlg);
            officeIdList.remove(building.getId());
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MEETING_OFFICE_ID_LIST, JSONUtils.toJSONString(officeIdList));
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnDeleteOfficeFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MeetingOfficeAddActivity.this, error, errorCode);
        }
    }
}
