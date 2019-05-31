package com.inspur.emmcloud.ui.schedule.task;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.TaskListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.task.GetTaskListResult;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.widget.dialogs.CustomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2019/4/1.
 */
public class TaskListFragment extends Fragment {

    public static final String TASK_TASK_ENTITY = "task";
    public static final String TASK_CURRENT_INDEX = "tabIndex";
    private ListView taskListView;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout noSearchResultLayout;
    private TextView noResultText;
    private ImageView taskNoResultImageView;
    private String orderBy = "PRIORITY";
    private String orderType = "ASC";
    private int deletePosition = -1;
    private String searchContent = "";
    private ArrayList<Task> uiTaskList = new ArrayList<>();
    private TaskListAdapter adapter;
    private ScheduleApiService apiService;
    private int currentIndex = 0;
    private ArrayList<Task> taskList = new ArrayList<Task>();
    private boolean isPullUp = false;
    private int page = 0;
    private View rootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_task_list, null);
        initViews();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater
                    .inflate(R.layout.fragment_task_list, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SCHEDULE_TASK_DATA_CHANGED:
            case Constant.EVENTBUS_TASK_ORDER_CHANGE:
                getTaskOrder();
                getCurrentTaskList();
                break;
        }
    }

    /**
     * 传入搜索内容
     *
     * @param searchContent
     */
    public void setSearchContent(String searchContent) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setCanLoadMore(StringUtils.isBlank(searchContent));
            swipeRefreshLayout.setEnabled(StringUtils.isBlank(searchContent));
        }
        this.searchContent = searchContent;
        if (adapter != null) {
            searchTaskListBySearchContent();
        }
    }

//    /**
//     * 告知Fragment当前索引
//     *
//     * @param currentIndex
//     */
//    public void setCurrentIndex(int currentIndex) {
//        this.currentIndex = currentIndex;
//        if(swipeRefreshLayout != null){
//            swipeRefreshLayout.setCanLoadMore(currentIndex == TaskFragment.MY_DONE && (uiTaskList.size() % 12 == 0));
//        }
//    }

    /**
     * 根据搜索内容搜索列表
     */
    private void searchTaskListBySearchContent() {
        uiTaskList.clear();
        for (Task task : taskList) {
            if (task.getTitle().contains(searchContent)) {
                uiTaskList.add(task);
            }
        }
        adapter.setAndChangeData(uiTaskList);
    }

    private void initViews() {

        taskListView = rootView.findViewById(R.id.lv_task);
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);
        noSearchResultLayout = rootView.findViewById(R.id.ll_no_search_result);
        noResultText = rootView.findViewById(R.id.tv_no_result);
        taskNoResultImageView = rootView.findViewById(R.id.iv_task_no_result);
        currentIndex = getArguments().getInt(TaskFragment.MY_TASK_TYPE, TaskFragment.MY_MINE);
        apiService = new ScheduleApiService(getActivity());
        apiService.setAPIInterface(new WebService());
        getTaskOrder();
        initPullRefreshLayout();
        adapter = new TaskListAdapter(getActivity(), uiTaskList);
        taskListView.setAdapter(adapter);
        taskListView.setOnItemClickListener(new OnTaskClickListener());
        taskListView.setOnItemLongClickListener(new OnTaskLongClickListener());
        getCurrentTaskList();
    }

    /**
     * 获取缓存的排序规则
     */
    private void getTaskOrder() {
        orderBy = PreferencesUtils.getString(getActivity(), TaskSetActivity.TASK_ORDER_BY, TaskSetActivity.TASK_ORDER_PRIORITY);
//        orderType = PreferencesUtils.getString(getActivity(), TaskSetActivity.TASK_ORDER_TYPE, TaskSetActivity.TASK_ORDER_TYPE_DESC);
        orderType = TaskSetActivity.TASK_ORDER_TYPE_DESC;
    }

    /**
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout() {
        //已完成页面设置可以上拉加载
        swipeRefreshLayout.setCanLoadMore(currentIndex == TaskFragment.MY_DONE && (uiTaskList.size() % 12 == 0));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPullUp = false;
                page = 0;
                if (NetUtils.isNetworkConnected(getActivity(), false)) {
                    getCurrentTaskList();
                } else {
                    swipeRefreshLayout.setLoading(false);
                }
                swipeRefreshLayout.setCanLoadMore(currentIndex == TaskFragment.MY_DONE && (uiTaskList.size() % 12 == 0));
            }
        });
        swipeRefreshLayout.setOnLoadListener(new MySwipeRefreshLayout.OnLoadListener() {
            @Override
            public void onLoadMore() {
                if (NetUtils.isNetworkConnected(getActivity(), false)) {
                    apiService.getFinishTasks(page, 12, "REMOVED");
                    isPullUp = true;
                } else {
                    swipeRefreshLayout.setLoading(false);
                }
            }
        });
    }

    private void getCurrentTaskList() {
        currentIndex = getArguments().getInt(TaskFragment.MY_TASK_TYPE, TaskFragment.MY_MINE);
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            if (currentIndex == TaskFragment.MY_MINE) {
                getMineTasks();
            } else if (currentIndex == TaskFragment.MY_INVOLVED) {
                getInvolvedTasks();
            } else if (currentIndex == TaskFragment.MY_FOCUSED) {
                getFocusedTasks();
            } else if (currentIndex == TaskFragment.MY_DONE) {
                getAllFinishTasks();
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 获取关注的任务
     */
    protected void getFocusedTasks() {
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getFocusedTasks(orderBy, orderType);
        }
    }

    /**
     * 获取我参与的任务
     */
    protected void getInvolvedTasks() {
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getInvolvedTasks(orderBy, orderType);
        }
    }

    /**
     * 获取我的任务
     */
    protected void getMineTasks() {
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getMineTasks(orderBy, orderType);
        }
    }

    /**
     * 获取所有任务
     */
    private void getAllFinishTasks() {
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getFinishTasks(0, 12, "REMOVED");
        }
    }


    /**
     * 删除任务
     *
     * @param position
     */
    protected void deleteTasks(int position) {
        if (NetUtils.isNetworkConnected(getActivity(), false)) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.setTaskFinishById(uiTaskList.get(position).getId());
            deletePosition = position;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 长按事件监听
     */
    class OnTaskLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       final int position, long id) {
            if (currentIndex == 0 || currentIndex == 1) {
                new CustomDialog.MessageDialogBuilder(getActivity())
                        .setMessage(R.string.mession_set_finish)
                        .setNegativeButton(getString(R.string.cancel), (dialog, index) -> dialog.dismiss())
                        .setPositiveButton(getString(R.string.ok), (dialog, index) -> {
                            dialog.dismiss();
                            deleteTasks(position);
                        })
                        .show();

            }
            return true;
        }

    }

    /**
     * 任务点击事件
     */
    class OnTaskClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.putExtra(TASK_TASK_ENTITY, uiTaskList.get(position));
            intent.putExtra(TASK_CURRENT_INDEX, currentIndex);
            intent.setClass(getActivity(),
                    TaskAddActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            super.returnRecentTasksSuccess(getTaskListResult);
            if (isPullUp) {
                swipeRefreshLayout.setLoading(false);
                page = page + 1;
                taskList.addAll(getTaskListResult.getTaskList());
            } else {
                swipeRefreshLayout.setRefreshing(false);
                taskList = getTaskListResult.getTaskList();
            }
            uiTaskList.clear();
            uiTaskList.addAll(taskList);
            adapter.setAndChangeData(uiTaskList);
            swipeRefreshLayout.setCanLoadMore(currentIndex == TaskFragment.MY_DONE && (uiTaskList.size() % 12 == 0));
            noResultText.setVisibility(uiTaskList.size() > 0 ? View.GONE : View.VISIBLE);
            taskNoResultImageView.setVisibility(uiTaskList.size() > 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            swipeRefreshLayout.setLoading(false);
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
            noResultText.setVisibility(taskList.size() > 0 ? View.GONE : View.VISIBLE);
            taskNoResultImageView.setVisibility(taskList.size() > 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void returnDeleteTaskSuccess() {
            super.returnDeleteTaskSuccess();
            swipeRefreshLayout.setRefreshing(false);
            if (deletePosition != -1) {
                uiTaskList.remove(deletePosition);
                adapter.notifyDataSetChanged();
            }
            noResultText.setVisibility(uiTaskList.size() > 0 ? View.GONE : View.VISIBLE);
            taskNoResultImageView.setVisibility(uiTaskList.size() > 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void returnDeleteTaskFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
            noResultText.setVisibility(uiTaskList.size() > 0 ? View.GONE : View.VISIBLE);
            taskNoResultImageView.setVisibility(uiTaskList.size() > 0 ? View.GONE : View.VISIBLE);
        }

    }

}
