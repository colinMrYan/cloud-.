package com.inspur.emmcloud.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.GetOfficeResult.Office;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

public class MeetingRoomPopAdater extends BaseAdapter {

	private Activity context;
	private List<Office> officeList;
	private String userId;
	private RefreshOffices refreshOffices;
	private WorkAPIService apiService;
	private LoadingDialog loadingDialog;
	private List<String> myCommonBuildingIdList = new ArrayList<String>();
	private int deletePosition;

	public MeetingRoomPopAdater(Activity context, List<Office> offices) {
		this.context = context;
		officeList = offices;
		userId = PreferencesUtils.getString(context, "userID");
		apiService = new WorkAPIService(context);
		apiService.setAPIInterface(new WebService());
		loadingDialog = new LoadingDialog(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return officeList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		convertView = LayoutInflater.from(context).inflate(
				R.layout.office_date_list_item, null);
		TextView officeLocItem = (TextView) convertView
				.findViewById(R.id.office_item_textView);
		officeLocItem.setText(officeList.get(position).getBuidingName() + "-"
				+ officeList.get(position).getLocation().getName());
		RelativeLayout officeLayout = (RelativeLayout) convertView
				.findViewById(R.id.office_expand_layout);

		final ImageView officeImg = (ImageView) convertView
				.findViewById(R.id.meeting_office_ring_img);

		String myCommonBuildingIds = PreferencesUtils.getString(context,
				UriUtils.tanent + userId + "myCommonBuildingIds");
		if (myCommonBuildingIds != null) {
			myCommonBuildingIdList = (List) JSON.parseArray(
					myCommonBuildingIds, String.class);
		}
		if (myCommonBuildingIdList.contains(officeList.get(position)
				.getOfficeId())) {
			officeImg.setVisibility(View.VISIBLE);
		} else {
			officeImg.setVisibility(View.GONE);
		}

		officeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 记录选择的地点
				if (officeImg.getVisibility() == View.GONE) {
					officeImg.setVisibility(View.VISIBLE);
					myCommonBuildingIdList.add(officeList.get(position)
							.getOfficeId());
				} else {
					officeImg.setVisibility(View.GONE);
					myCommonBuildingIdList.remove(officeList.get(position)
							.getOfficeId());
				}
				recordOfficeid();
				notifyDataSetChanged();
			}
		});

		officeLayout.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (which == -1) {
							deleteCommonOffice(position);
						}
						dialog.dismiss();
					}

				};
				EasyDialog.showDialog(context,
						context.getString(R.string.prompt),
						context.getString(R.string.office_delete_position),
						context.getString(R.string.ok),
						context.getString(R.string.cancel), listener, true);
				return false;
			}
		});

		return convertView;
	}

	/**
	 * 删除常用办公地点
	 *
	 * @param position
	 *            list中的位置
	 */
	private void deleteCommonOffice(int position) {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(context)) {
			loadingDialog.show();
			deletePosition = position;
			apiService.deleteOffice(officeList.get(position).getOfficeId());
		}
	}

	protected void recordOfficeid() {
		String myCommonBuildingIds = JSON.toJSONString(myCommonBuildingIdList);
		PreferencesUtils.putString(context, UriUtils.tanent + userId
				+ "myCommonBuildingIds", myCommonBuildingIds);
		PreferencesUtils.putInt(context, UriUtils.tanent + userId
				+ "officeSize", myCommonBuildingIdList.size());
	}

	class WebService extends APIInterfaceInstance {
		@Override
		public void returnDeleteOfficeSuccess() {
			// TODO Auto-generated method stub
			super.returnDeleteOfficeSuccess();

			if (loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			officeList.remove(deletePosition);
			recordOfficeid();
			notifyDataSetChanged();

			List<String> allCommonOfficeIdList = new ArrayList<String>();
			for (int i = 0; i < officeList.size(); i++) {
				allCommonOfficeIdList.add(officeList.get(i).getBuildingId());
			}
			String allCommonOfficeIds = JSON
					.toJSONString(allCommonOfficeIdList);
			PreferencesUtils.putString(context, UriUtils.tanent + userId
					+ "allCommonOfficeIds",
					allCommonOfficeIds);
		}

		@Override
		public void returnDeleteOfficeFail(String error) {
			// TODO Auto-generated method stub
			super.returnDeleteOfficeFail(error);

			if (loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
		}
	}

	public void setRefreshOffices(RefreshOffices refreshOffices) {
		this.refreshOffices = refreshOffices;
	}

	public interface RefreshOffices {
		void refreshOffices();
	};
}
