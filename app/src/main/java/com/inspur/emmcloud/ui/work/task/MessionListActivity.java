package com.inspur.emmcloud.ui.work.task;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.schedule.task.GetTaskAddResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.bean.schedule.task.TaskColorTag;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.TaskTagColorUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SegmentControl;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务列表页
 */
public class MessionListActivity extends BaseActivity {

    private static final int MY_MINE = 0;
    private static final int MY_INVOLVED = 1;
    private static final int MY_FOCUSED = 2;
    private static final int MESSION_SET = 5;
    private ListView messionListView;
    private EditText messionAddEdit;
    private MessionListAdapter adapter;
    private WorkAPIService apiService;
    private LoadingDialog loadingDialog;
    private ArrayList<Task> taskList = new ArrayList<Task>();
    private int nowIndex = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout addLayout;
    private String orderBy = "PRIORITY";
    private String orderType = "ASC";
    private SegmentControl segmentControl;
    private LinearLayout noSearchResultLayout;
    private TextView noResultText;
    private int deletePosition = -1;
    private boolean isNeedRefresh = false;
    private BroadcastReceiver taskEventReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mession_list);
        initView();
        // registerTaskReceiver();
    }

    // 这里这一段是否需要加上待定，
    // /**
    // * 注册刷新任务的广播
    // */
    // private void registerTaskReceiver() {
    // taskEventReceiver = new BroadcastReceiver() {
    // @Override
    // public void onReceive(Context context, Intent intent) {
    // if (intent.hasExtra("refreshTask")) {
    // if (NetUtils.isNetworkConnected(MessionListActivity.this)) {
    // getOrder();
    // apiService.getFocusedTasks(orderBy, orderType);
    // }
    // adapter.notifyDataSetChanged();
    // }
    // }
    // };
    // IntentFilter myIntentFilter = new IntentFilter();
    // myIntentFilter.addAction(ACTION_TASK);
    // // 注册广播
    // MessionListActivity.this.registerReceiver(taskEventReceiver,
    // myIntentFilter);
    // }
    //
    // @Override
    // protected void onDestroy() {
    // super.onDestroy();
    // if (taskEventReceiver != null) {
    // MessionListActivity.this.unregisterReceiver(taskEventReceiver);
    // taskEventReceiver = null;
    // }
    // }

    /**
     * 初始化views
     */
    private void initView() {
        apiService = new WorkAPIService(MessionListActivity.this);
        apiService.setAPIInterface(new WebService());
        getOrder();
        adapter = new MessionListAdapter();
        initPullRefreshLayout();
        noSearchResultLayout = (LinearLayout) findViewById(R.id.nosearch_result_layout);
        noResultText = (TextView) findViewById(R.id.mession_no_result_text);
        final RelativeLayout addLayout = (RelativeLayout) findViewById(R.id.mession_add_layout);
        loadingDialog = new LoadingDialog(MessionListActivity.this);
        segmentControl = (SegmentControl) findViewById(R.id.segment_control);
        segmentControl
                .setOnSegmentControlClickListener(new SegmentControl.OnSegmentControlClickListener() {
                    @Override
                    public void onSegmentControlClick(int index) {
                        // 处理点击标签的事件
                        if (index == MY_MINE) {
                            getMineMessions(true);
                            nowIndex = index;
                            addLayout.setVisibility(View.VISIBLE);
                        } else if (index == MY_INVOLVED) {
                            getInvolvedMessions(true);
                            nowIndex = index;
                            addLayout.setVisibility(View.GONE);
                        } else if (index == MY_FOCUSED) {
                            getFocusedMessions(true);
                            nowIndex = index;
                            addLayout.setVisibility(View.GONE);
                        }
                    }
                });
        if (getIntent().hasExtra("index")) {
            nowIndex = getIntent().getIntExtra("index", 0);
            segmentControl.setCurrentIndex(nowIndex);
            if (nowIndex == MY_FOCUSED) {
                getFocusedMessions(true);
                addLayout.setVisibility(View.GONE);
            } else if (nowIndex == MY_INVOLVED) {
                getInvolvedMessions(true);
                addLayout.setVisibility(View.GONE);
            }
        } else {
            getMineMessions(true);
        }
        messionAddEdit = (EditText) findViewById(R.id.mession_add_text);
        messionListView = (ListView) findViewById(R.id.mession_list);
        messionListView
                .setOnItemLongClickListener(new MessionLongClickListener());
        messionListView.setOnItemClickListener(new OnMessionClickListener());
    }

    /**
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetUtils.isNetworkConnected(MessionListActivity.this)) {
                    if (nowIndex == MY_MINE) {
                        getMineMessions(false);
                    } else if (nowIndex == MY_INVOLVED) {
                        getInvolvedMessions(false);
                    } else if (nowIndex == MY_FOCUSED) {
                        getFocusedMessions(false);
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }


    /**
     * 获取关注的任务
     *
     * @param isDialogShow
     */
    protected void getFocusedMessions(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(MessionListActivity.this)) {
            loadingDialog.show(isDialogShow);
            apiService.getFocusedTasks(orderBy, orderType);
        }
    }

    /**
     * 获取我参与的任务
     *
     * @param isDialogShow
     */
    protected void getInvolvedMessions(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(MessionListActivity.this)) {
            loadingDialog.show(isDialogShow);
            apiService.getInvolvedTasks(orderBy, orderType);
        }
    }

    /**
     * 获取我的任务
     *
     * @param isDialogShow
     */
    protected void getMineMessions(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(MessionListActivity.this)) {
            loadingDialog.show(isDialogShow);
            apiService.getRecentTasks(orderBy, orderType);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            isNeedRefresh = true;
            nowIndex = segmentControl.getCurrentIndex();
            if (requestCode == MESSION_SET) {
                noSearchResultLayout.setVisibility(View.GONE);
            }
            getOrder();
            refreshMessionList();
        }
    }

    /**
     * 刷新任务列表
     */
    private void refreshMessionList() {
        if (nowIndex == MY_MINE) {
            getMineMessions(true);
        } else if (nowIndex == MY_INVOLVED) {
            getInvolvedMessions(true);
        } else if (nowIndex == MY_FOCUSED) {
            getFocusedMessions(true);
        }
    }

    /**
     * 获取缓存的排序规则
     */
    private void getOrder() {
        orderBy = PreferencesUtils.getString(MessionListActivity.this,
                "order_by", "PRIORITY");
        orderType = PreferencesUtils.getString(MessionListActivity.this,
                "order_type", "DESC");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                onBackPressed();
                break;
            case R.id.mession_settiing_img:
                Intent intent = new Intent();
                intent.setClass(MessionListActivity.this, MessionSetActivity.class);
                startActivityForResult(intent, nowIndex);
                break;
            case R.id.mession_add_img:
                String messionTitle = messionAddEdit.getText().toString();
                if (StringUtils.isBlank(messionTitle)) {
                    ToastUtils.show(MessionListActivity.this,
                            getString(R.string.mession_list_name));
                    break;
                }
                if (messionTitle.length() > 64) {
                    ToastUtils.show(MessionListActivity.this,
                            getString(R.string.mession_no_more));
                    break;
                }
                createTask(messionTitle);
                break;
            case R.id.reset_btn:
                Intent intentReset = new Intent();
                intentReset.setClass(MessionListActivity.this,
                        MessionSetActivity.class);
                startActivityForResult(intentReset, MESSION_SET);
                break;
            case R.id.cancel_btn:
                String userId = ((MyApplication) getApplicationContext()).getUid();
                PreferencesUtils.putString(MessionListActivity.this,
                        MyApplication.getInstance().getTanent() + userId + "chooseTags", "");
                noSearchResultLayout.setVisibility(View.GONE);
                refreshMessionList();
                break;
            default:
                break;
        }
    }

    /**
     * 创建任务
     *
     * @param messionTitle
     */
    private void createTask(String messionTitle) {
        if (NetUtils.isNetworkConnected(MessionListActivity.this)) {
            loadingDialog.show();
            apiService.createTasks(messionTitle);
        }
    }

    @Override
    public void onBackPressed() {
        if (isNeedRefresh) {
            Intent mIntent = new Intent(Constant.ACTION_TASK);
            mIntent.putExtra("refreshTask", "refreshTask");
            LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
            setResult(RESULT_OK);
        }
        finish();
    }

    /**
     * 删除任务
     *
     * @param position
     */
    protected void deleteTasks(int position) {
        if (NetUtils.isNetworkConnected(MessionListActivity.this)) {
            loadingDialog.show();
            apiService.deleteTasks(taskList.get(position).getId());
            deletePosition = position;
        }
    }

    /**
     * 处理
     *
     * @param chooseTags
     */
    public void handleResultUI(ArrayList<String> chooseTags) {
        if (taskList.size() == 0 && chooseTags.size() == 0) {
            noResultText.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            noResultText.setVisibility(View.GONE);
        }
    }

    /**
     * 整理任务列表
     *
     * @param getTaskListResult
     * @param chooseTagList
     * @return
     */
    public ArrayList<Task> handleTaskList(
            GetTaskListResult getTaskListResult, ArrayList<String> chooseTagList) {
        // String[] tags = chooseTags.split(":");
        if (chooseTagList.size() == 0) {
            noSearchResultLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            taskList = getTaskListResult.getTaskList();
        } else {
            taskList = new ArrayList<Task>();
            int taskSize = getTaskListResult.getTaskList().size();
            int chooseTagLength = chooseTagList.size();
            for (int i = 0; i < taskSize; i++) {
                // 一个任务里的所有标签
                List<TaskColorTag> taskColorTags = getTaskListResult
                        .getTaskList().get(i).getTags();
                ArrayList<String> taskColorList = new ArrayList<String>();
                for (int k = 0; k < taskColorTags.size(); k++) {
                    taskColorList.add(taskColorTags.get(k).getTitle());
                }
                // 如果任务里的标签包含所有已选中标签
                if (taskColorList.containsAll(chooseTagList)) {
                    taskList.add(getTaskListResult.getTaskList().get(i));
                }
            }
            if (taskList.size() == 0) {
                noResultText.setVisibility(View.VISIBLE);
                noSearchResultLayout.setVisibility(View.VISIBLE);
            } else {
                noSearchResultLayout.setVisibility(View.GONE);
            }
        }
        return taskList;
    }

    /**
     * 任务列表
     */
    class MessionListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (taskList != null && taskList.size() > 0) {
                return taskList.size();
            } else {
                return 0;
            }
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
            convertView = LayoutInflater.from(MessionListActivity.this)
                    .inflate(R.layout.meession_list_item, null);
            ((TextView) convertView.findViewById(R.id.mession_text))
                    .setText(taskList.get(position).getTitle());
            if (taskList.get(position).getTags().size() > 0) {
                TaskTagColorUtils.setTagColorImg((ImageView) convertView
                                .findViewById(R.id.mession_color),
                        taskList.get(position).getTags().get(0).getColor());
            } else {
                // 如果没有tag，显示默认tag
                TaskTagColorUtils.setTagColorImg((ImageView) convertView
                        .findViewById(R.id.mession_color), "YELLOW");
            }
            if (taskList.get(position).getPriority() == 1) {
                // 当重要程度为1时可能后续需要做处理
                // ((ImageView) convertView
                // .findViewById(R.id.mession_state_img))
                // .setVisibility(View.VISIBLE);
            } else if (taskList.get(position).getPriority() == 2) {
                convertView.findViewById(R.id.mession_state_img)
                        .setVisibility(View.VISIBLE);
                // 当重要程度为2时后续可能需要添加两个叹号
                // ((ImageView) convertView
                // .findViewById(R.id.mession_state_img2))
                // .setVisibility(View.VISIBLE);
            }
            return convertView;
        }

    }

    /**
     * 长按事件监听
     */
    class MessionLongClickListener implements OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       final int position, long id) {
            if (nowIndex == 0 || nowIndex == 1) {
                new MyQMUIDialog.MessageDialogBuilder(MessionListActivity.this)
                        .setMessage(R.string.mession_set_finish)
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
                                deleteTasks(position);
                            }
                        })
                        .show();

            }
            return true;
        }

    }

    /**
     * 任务点击事件
     */
    class OnMessionClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.putExtra("task", taskList.get(position));
            intent.putExtra("tabIndex", nowIndex);
            intent.setClass(MessionListActivity.this,
                    MessionDetailActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            noResultText.setVisibility(View.GONE);
            Task taskResult = new Task();
            taskResult.setTitle(messionAddEdit.getText().toString());
            taskResult.setId(getTaskAddResult.getId());
            taskResult.setOwner(PreferencesUtils.getString(
                    MessionListActivity.this, "userID"));
            taskResult.setState("ACTIVED");
            taskList.add(taskResult);
            messionAddEdit.setText("");
            adapter.notifyDataSetChanged();
            isNeedRefresh = true;
        }

        @Override
        public void returnCreateTaskFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionListActivity.this, error, errorCode);
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            super.returnRecentTasksSuccess(getTaskListResult);
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            swipeRefreshLayout.setRefreshing(false);
            String userId = ((MyApplication) getApplicationContext()).getUid();
            String chooseTags = PreferencesUtils.getString(
                    MessionListActivity.this, MyApplication.getInstance().getTanent() + userId
                            + "chooseTags", "");
            ArrayList<String> chooseTagList = JSONUtils.JSONArray2List(chooseTags, new ArrayList<String>());
            ;
            handleTaskList(getTaskListResult, chooseTagList);
            handleResultUI(chooseTagList);
            adapter = new MessionListAdapter();
            messionListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionListActivity.this, error, errorCode);
        }

        @Override
        public void returnDeleteTaskSuccess() {
            super.returnDeleteTaskSuccess();
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if (deletePosition != -1) {
                taskList.remove(deletePosition);
                adapter.notifyDataSetChanged();
            }
            if (taskList.size() == 0) {
                noResultText.setVisibility(View.VISIBLE);
            }
            isNeedRefresh = true;
        }

        @Override
        public void returnDeleteTaskFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionListActivity.this, error, errorCode);
        }

    }


}
