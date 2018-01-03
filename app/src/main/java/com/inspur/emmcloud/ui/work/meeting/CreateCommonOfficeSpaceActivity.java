package com.inspur.emmcloud.ui.work.meeting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetCreateOfficeResult;
import com.inspur.emmcloud.bean.work.GetLoctionResult;
import com.inspur.emmcloud.bean.work.Location;
import com.inspur.emmcloud.bean.work.OfficeBuilding;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.pullableview.PullableExpandableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建常用办公地点
 * com.inspur.emmcloud.ui.work.meeting.CreateCommonOfficeSpaceActivity
 * create at 2016年10月15日 上午10:24:12
 */
public class CreateCommonOfficeSpaceActivity extends BaseActivity {

	private PullableExpandableListView expandListView;
	private LoadingDialog loadingDlg;
	private WorkAPIService apiService;
	private CreateBuildingAdapter adapter;
	private List<Location> locationList = new ArrayList<Location>();
	private List<String> allCommonBuildingIdList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_common_office_space);
		initView();
		getAllCommonOfficeIdList();
		getLocation();

	}

	/**
	 * 获取自己的所有的常用办公地点
	 */
	private void getAllCommonOfficeIdList() {
		String uid = ((MyApplication) getApplicationContext()).getUid();
		String allCommonBuildingIds = PreferencesUtils.getString(
				getApplicationContext(), MyApplication.getInstance().getTanent() + uid
						+ "allCommonBuildingIds");
		if (allCommonBuildingIds != null) {
			allCommonBuildingIdList = (List) JSON.parseArray(allCommonBuildingIds,
					String.class);
		}
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		// TODO Auto-generated method stub
		expandListView = (PullableExpandableListView) findViewById(R.id.expandable_list);
		expandListView.setGroupIndicator(null);
		expandListView.setVerticalScrollBarEnabled(false);
		expandListView.setHeaderDividersEnabled(false);
		expandListView.setCanpulldown(false);
		expandListView.setCanpullup(false);
		adapter = new CreateBuildingAdapter();
		expandListView.setAdapter(adapter);
		expandListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				OfficeBuilding building = locationList.get(groupPosition)
						.getOfficeBuildingList().get(childPosition);
				if (!allCommonBuildingIdList.contains(building.getId())) {
					creatCommonOfficeSpace(locationList.get(groupPosition)
							.getOfficeBuildingList().get(childPosition));
				}
				
				return false;
			}
		});
		loadingDlg = new LoadingDialog(CreateCommonOfficeSpaceActivity.this);
		apiService = new WorkAPIService(CreateCommonOfficeSpaceActivity.this);
		apiService.setAPIInterface(new WebService());
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		default:
			break;
		}
	}

	class CreateBuildingAdapter extends BaseExpandableListAdapter {

		PullableExpandableListView expandableListView;

		@Override
		public int getGroupCount() {
			return locationList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return locationList.get(groupPosition).getOfficeBuildingList()
					.size();
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
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			expandableListView = (PullableExpandableListView) parent;
			expandableListView.expandGroup(groupPosition);
			convertView = LayoutInflater.from(
					CreateCommonOfficeSpaceActivity.this).inflate(
					R.layout.building_expand_list, null);
			TextView chooseLocation = (TextView) convertView
					.findViewById(R.id.location_name_text);
			chooseLocation.setText(locationList.get(groupPosition).getName());
			return convertView;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			convertView = LayoutInflater.from(
					CreateCommonOfficeSpaceActivity.this).inflate(
					R.layout.office_building_item_view, null);
			TextView buildingNameText = (TextView) convertView
					.findViewById(R.id.building_textView);
			ImageView flagImg = (ImageView) convertView
					.findViewById(R.id.meeting_flag_img);
			RelativeLayout outSideLayout = (RelativeLayout) convertView.findViewById(R.id.choose_expand_layout);
			final OfficeBuilding building = locationList.get(groupPosition)
					.getOfficeBuildingList().get(childPosition);
			if (allCommonBuildingIdList.contains(building.getId())) {
				outSideLayout.setBackgroundColor(0xffcccccc);
				flagImg.setVisibility(View.VISIBLE);
			}else {
				outSideLayout.setBackgroundColor(getResources().getColor(R.color.white));
				flagImg.setVisibility(View.INVISIBLE);
			}
			buildingNameText.setText(building.getName());
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	class WebService extends APIInterfaceInstance {
		@Override
		public void returnLoctionResultSuccess(GetLoctionResult getLoctionResult) {
			super.returnLoctionResultSuccess(getLoctionResult);
			if (loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			locationList = getLoctionResult.getLocList();
			adapter.notifyDataSetChanged();

		}

		@Override
		public void returnLoctionResultFail(String error,int errorCode) {
			if ((loadingDlg != null) && (loadingDlg.isShowing())) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(CreateCommonOfficeSpaceActivity.this, error,errorCode);
		}

		@Override
		public void returnCreatOfficeSuccess(
				GetCreateOfficeResult getCreateOfficeResult) {
			if ((loadingDlg != null) && (loadingDlg.isShowing())) {
				loadingDlg.dismiss();
			}
			ToastUtils.show(CreateCommonOfficeSpaceActivity.this,
					getString(R.string.office_create_success));
			setResult(RESULT_OK);
			finish();
		}

		@Override
		public void returnCreatOfficeFail(String error,int errorCode) {
			if ((loadingDlg != null) && (loadingDlg.isShowing())) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(CreateCommonOfficeSpaceActivity.this, error,errorCode);
		}

	}

	/**
	 * 获取所有的办公地点
	 */
	public void getLocation() {
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			loadingDlg.show();
			apiService.getLoction();
		}
	}

	/**
	 * 创建常用办公地点
	 *
	 * @param building
	 */
	protected void creatCommonOfficeSpace(OfficeBuilding building) {
		String name = building.getName();
		String id = building.getId();
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			loadingDlg.show();
			apiService.creatOffice(name, id);
		}
	}

}
