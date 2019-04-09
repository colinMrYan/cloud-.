package com.inspur.emmcloud.ui.schedule.task;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.TaskListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetTaskAddResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.bean.work.TaskColorTag;
import com.inspur.emmcloud.ui.work.task.MessionDetailActivity;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/4/1.
 */
@ContentView(R.layout.fragment_task_list)
public class TaskListFragment extends Fragment {

    @ViewInject(R.id.lv_task)
    private ListView taskListView;
    @ViewInject(R.id.refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.ll_no_search_result)
    private LinearLayout noSearchResultLayout;
    @ViewInject(R.id.tv_no_result)
    private TextView noResultText;
    private boolean injected = false;
    private String orderBy = "PRIORITY";
    private String orderType = "ASC";
    private int deletePosition = -1;
    private boolean isNeedRefresh = false;
    private String searchContent = "";
    private ArrayList<Task> searchTaskList = new ArrayList<>();
    private TaskListAdapter adapter;
    private WorkAPIService apiService;
    private int nowIndex = 0;
    private ArrayList<Task> taskList = new ArrayList<Task>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        injected = true;
        return x.view().inject(this, inflater, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!injected) {
            x.view().inject(this, this.getView());
        }
        initViews();
    }

    /**
     * 传入搜索内容
     * @param searchContent
     */
    public void setSearchContent(String searchContent){
        this.searchContent = searchContent;
        if(adapter != null){
            searchTaskListBySearchContent();
        }
    }

    /**
     * 根据搜索内容搜索列表
     */
    private void searchTaskListBySearchContent() {
        searchTaskList.clear();
        for(Task task:taskList){
            if(task.getTitle().contains(searchContent)){
                searchTaskList.add(task);
            }
        }
        adapter.setAndChangeData(searchTaskList);
    }

    private void initViews() {
        nowIndex = getArguments().getInt(TaskFragment.MY_TASK_TYPE, TaskFragment.MY_MINE);
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        getOrder();
        initPullRefreshLayout();
        taskListView.setOnItemClickListener(new OnTaskClickListener());
//        taskListView.setOnItemLongClickListener(new OnTaskLongClickListener());
        getCurrentTaskList(true);
    }

    /**
     * 获取缓存的排序规则
     */
    private void getOrder() {
        orderBy = PreferencesUtils.getString(getActivity(), "order_by", "PRIORITY");
        orderType = PreferencesUtils.getString(getActivity(), "order_type", "DESC");
    }


    /**
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCurrentTaskList(false);
            }
        });
    }

    private void getCurrentTaskList(boolean isDialogShow){
        nowIndex = getArguments().getInt(TaskFragment.MY_TASK_TYPE, TaskFragment.MY_MINE);
        if (NetUtils.isNetworkConnected(getActivity())) {
            if (nowIndex == TaskFragment.MY_MINE) {
                getMineTasks(isDialogShow);
            } else if (nowIndex == TaskFragment.MY_INVOLVED) {
                getInvolvedTasks(isDialogShow);
            } else if (nowIndex == TaskFragment.MY_FOCUSED) {
                getFocusedTasks(isDialogShow);
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 获取关注的任务
     *
     * @param isDialogShow
     */
    protected void getFocusedTasks(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getFocusedTasks(orderBy, orderType);
        }
    }

    /**
     * 获取我参与的任务
     *
     * @param isDialogShow
     */
    protected void getInvolvedTasks(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getInvolvedTasks(orderBy, orderType);
        }
    }

    /**
     * 获取我的任务
     *
     * @param isDialogShow
     */
    protected void getMineTasks(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getRecentTasks(orderBy, orderType);
        }
    }


    /**
     * 删除任务
     *
     * @param position
     */
    protected void deleteTasks(int position) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            swipeRefreshLayout.setRefreshing(true);
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
     * 长按事件监听
     */
    class OnTaskLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       final int position, long id) {
            if (nowIndex == 0 || nowIndex == 1) {
                new MyQMUIDialog.MessageDialogBuilder(getActivity())
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
    class OnTaskClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.putExtra("task", taskList.get(position));
            intent.putExtra("tabIndex", nowIndex);
            intent.setClass(getActivity(),
                    MessionDetailActivity.class);
            startActivityForResult(intent, 0);
        }
    }


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
            swipeRefreshLayout.setRefreshing(false);
            noResultText.setVisibility(View.GONE);
            Task task = new Task();
//            taskResult.setTitle(messionAddEdit.getText().toString());
            task.setId(getTaskAddResult.getId());
            task.setOwner(PreferencesUtils.getString(
                    getActivity(), "userID"));
            task.setState("ACTIVED");
            taskList.add(task);
            adapter.notifyDataSetChanged();
            isNeedRefresh = true;
        }

        @Override
        public void returnCreateTaskFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            super.returnRecentTasksSuccess(getTaskListResult);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setRefreshing(false);
            String userId = ((MyApplication) getActivity().getApplicationContext()).getUid();
            String chooseTags = PreferencesUtils.getString(
                    getActivity(), MyApplication.getInstance().getTanent() + userId
                            + "chooseTags", "");
            ArrayList<String> chooseTagList = JSONUtils.JSONArray2List(chooseTags, new ArrayList<String>());
            handleTaskList(getTaskListResult, chooseTagList);
            handleResultUI(chooseTagList);
            adapter = new TaskListAdapter(getActivity(),taskList);
            taskListView.setAdapter(adapter);
//            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnDeleteTaskSuccess() {
            super.returnDeleteTaskSuccess();
            swipeRefreshLayout.setRefreshing(false);
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
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

    }

}
