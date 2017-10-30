package com.inspur.emmcloud.ui.work.task;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.GetTaskListResult;
import com.inspur.emmcloud.bean.TaskResult;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.MessionTagColorUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;

import java.io.Serializable;
import java.util.ArrayList;

public class MessionFinishListActivity extends BaseActivity implements
		OnRefreshListener {

	private static final int OPEN_DETAIL = 0;
	private static final int CAN_NOT_CHANGE = 2;
	private MessionListAdapter adapter;
	private WorkAPIService apiService;
	private LoadingDialog loadingDialog;
	private ArrayList<TaskResult> taskList;
	private PullToRefreshLayout pullToRefreshLayout;
	private int page = 0;
	private boolean isPullup = false;
//	private int deletePosition = -1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messionfinish_list);
		initViews();
	}

	/**
	 * 初始化
	 */
	private void initViews() {
		taskList = new ArrayList<TaskResult>();
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
		pullToRefreshLayout
				.setOnRefreshListener(MessionFinishListActivity.this);
		loadingDialog = new LoadingDialog(MessionFinishListActivity.this);
		apiService = new WorkAPIService(MessionFinishListActivity.this);
		apiService.setAPIInterface(new WebService());
		getAllTasks();
		PullableListView messionListView = (PullableListView) findViewById(R.id.mession_list);
		messionListView
				.setOnItemLongClickListener(new MessionLongClickListener());
		messionListView.setDividerHeight(0);
		messionListView.setVerticalScrollBarEnabled(false);
		messionListView.setCanPullUp(true);
		messionListView.setOnItemClickListener(new OnMessionClickListener());
		adapter = new MessionListAdapter();
		messionListView.setAdapter(adapter);
	}

	/**
	 * 获取所有任务
	 */
	private void getAllTasks() {
		if (NetUtils.isNetworkConnected(MessionFinishListActivity.this)) {
			loadingDialog.show();
			apiService.getAllTasks(0, 12, "REMOVED");
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			setResult(RESULT_OK);
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		Intent mIntent = new Intent("com.inspur.task");
		mIntent.putExtra("refreshTask", "refreshTask");
		// 发送广播
		sendBroadcast(mIntent);
		setResult(RESULT_OK);
		finish();
	}

	class MessionListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return (taskList != null && taskList.size() > 0)?taskList.size():0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(MessionFinishListActivity.this)
					.inflate(R.layout.meession_list_item, null);
			((TextView) convertView.findViewById(R.id.mession_text))
					.setText(taskList.get(position).getTitle());
			if (taskList.get(position).getTags().size() > 0) {
				MessionTagColorUtils.setTagColorImg((ImageView) convertView
						.findViewById(R.id.mession_color),
						taskList.get(position).getTags().get(0).getColor());
			}
			if (taskList.get(position).getPriority() == 2) {
				(convertView.findViewById(R.id.mession_state_img))
						.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

	}

	class MessionLongClickListener implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == -2) {
					} else {
						TaskResult taskResult = taskList.get(position);
						taskResult.setState("ACTIVED");
//						deletePosition = position;
						updateTask(taskResult,position);
					}
				}

			};
			EasyDialog.showDialog(MessionFinishListActivity.this,
					getString(R.string.prompt),
					getString(R.string.mession_finish_recover),
					getString(R.string.ok), getString(R.string.cancel),
					listener, false);
			return true;
		}

	}

	/**
	 * 更新任务状态
	 * 
	 * @param taskResult
	 */
	protected void updateTask(TaskResult taskResult,int position) {
		if (NetUtils.isNetworkConnected(MessionFinishListActivity.this)) {
			loadingDialog.show();
			apiService.updateTask(JSON.toJSONString(taskResult),position);
		}
	}

	class OnMessionClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent();
			intent.putExtra("task", (Serializable) taskList.get(position));
			intent.putExtra("tabIndex", CAN_NOT_CHANGE);
			intent.setClass(MessionFinishListActivity.this,
					MessionDetailActivity.class);
			startActivityForResult(intent, OPEN_DETAIL);
		}
	}

	class WebService extends APIInterfaceInstance {
		@Override
		public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
			super.returnRecentTasksSuccess(getTaskListResult);
			if (loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			if (isPullup) {
				taskList.addAll(getTaskListResult.getTaskList());
			} else {
				taskList = getTaskListResult.getTaskList();
			}
			adapter.notifyDataSetChanged();
		}

		@Override
		public void returnRecentTasksFail(String error,int errorCode) {
			if (loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
			WebServiceMiddleUtils.hand(MessionFinishListActivity.this, error,errorCode);
		}

		@Override
		public void returnUpdateTaskSuccess(int position) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
//			if (deletePosition != -1) {
//				taskList.remove(deletePosition);
//				adapter.notifyDataSetChanged();
//			}
			taskList.remove(position);
			adapter.notifyDataSetChanged();
		}

		@Override
		public void returnUpdateTaskFail(String error,int errorCode,int position) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			WebServiceMiddleUtils.hand(MessionFinishListActivity.this, error,errorCode);
		}

	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		if (NetUtils.isNetworkConnected(MessionFinishListActivity.this)) {
			apiService.getAllTasks(0, 12, "REMOVED");
			page = 0;
		} else {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
		}
		isPullup = false;
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
		if (NetUtils.isNetworkConnected(MessionFinishListActivity.this)) {
			page = page + 1;
			apiService.getAllTasks(page, 12, "REMOVED");
			isPullup = true;
		}
	}
}
