package com.inspur.emmcloud.ui.work.task;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.TaskTagColorUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

import java.util.ArrayList;

public class MessionFinishListActivity extends BaseActivity implements
        MySwipeRefreshLayout.OnRefreshListener, MySwipeRefreshLayout.OnLoadListener {

    private static final int OPEN_DETAIL = 0;
    private static final int CAN_NOT_CHANGE = 2;
    private MessionListAdapter adapter;
    private WorkAPIService apiService;
    private LoadingDialog loadingDialog;
    private ArrayList<Task> taskList;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private int page = 0;
    private boolean isPullup = false;


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
        taskList = new ArrayList<>();
        swipeRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout
                .setOnRefreshListener(MessionFinishListActivity.this);
        loadingDialog = new LoadingDialog(MessionFinishListActivity.this);
        apiService = new WorkAPIService(MessionFinishListActivity.this);
        apiService.setAPIInterface(new WebService());
        getAllTasks();
        ListView messionListView = (ListView) findViewById(R.id.mession_list);
        messionListView
                .setOnItemLongClickListener(new MessionLongClickListener());
        messionListView.setVerticalScrollBarEnabled(false);
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
            case R.id.ibt_back:
                setResult(RESULT_OK);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent(Constant.ACTION_TASK);
        mIntent.putExtra("refreshTask", "refreshTask");
        // 发送广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
        setResult(RESULT_OK);
        finish();
    }

    /**
     * 更新任务状态
     *
     * @param taskResult
     */
    protected void updateTask(Task taskResult, int position) {
        if (NetUtils.isNetworkConnected(MessionFinishListActivity.this)) {
            loadingDialog.show();
            apiService.updateTask(JSONUtils.toJSONString(taskResult), position);
        }
    }

    @Override
    public void onRefresh() {
        if (NetUtils.isNetworkConnected(MessionFinishListActivity.this)) {
            apiService.getAllTasks(0, 12, "REMOVED");
            page = 0;
        } else {
            swipeRefreshLayout.setLoading(false);
        }
        isPullup = false;
    }

    @Override
    public void onLoadMore() {
        if (NetUtils.isNetworkConnected(MessionFinishListActivity.this)) {
            page = page + 1;
            apiService.getAllTasks(page, 12, "REMOVED");
            isPullup = true;
        } else {
            swipeRefreshLayout.setLoading(false);
        }
    }

    class MessionListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return (taskList != null && taskList.size() > 0) ? taskList.size() : 0;
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
                TaskTagColorUtils.setTagColorImg((ImageView) convertView
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
                        Task taskResult = taskList.get(position);
                        taskResult.setState("ACTIVED");
//						deletePosition = position;
                        updateTask(taskResult, position);
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

    class OnMessionClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.putExtra("task", taskList.get(position));
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
            swipeRefreshLayout.setLoading(false);
            if (isPullup) {
                taskList.addAll(getTaskListResult.getTaskList());
            } else {
                taskList = getTaskListResult.getTaskList();
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            swipeRefreshLayout.setLoading(false);
            WebServiceMiddleUtils.hand(MessionFinishListActivity.this, error, errorCode);
        }

        @Override
        public void returnUpdateTaskSuccess(int position) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            taskList.remove(position);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnUpdateTaskFail(String error, int errorCode, int position) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionFinishListActivity.this, error, errorCode);
        }

    }
}
