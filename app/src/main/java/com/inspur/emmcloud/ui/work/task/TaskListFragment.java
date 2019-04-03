package com.inspur.emmcloud.ui.work.task;

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
import com.inspur.emmcloud.bean.work.TaskColorTag;
import com.inspur.emmcloud.bean.work.TaskResult;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
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
    private boolean injected = false;
    @ViewInject(R.id.lv_task)
    private ListView taskListView;
    private TaskListAdapter adapter;
    private WorkAPIService apiService;
    private LoadingDialog loadingDialog;
    private int nowIndex = 0;
    private ArrayList<TaskResult> taskList = new ArrayList<TaskResult>();
    @ViewInject(R.id.refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;
    private String orderBy = "PRIORITY";
    private String orderType = "ASC";
    @ViewInject(R.id.ll_no_search_result)
    private LinearLayout noSearchResultLayout;
    @ViewInject(R.id.tv_no_result)
    private TextView noResultText;
    private int deletePosition = -1;
    private boolean isNeedRefresh = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        injected = true;
        LogUtils.YfcDebug("onCreateView");
        return x.view().inject(this, inflater, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!injected) {
            x.view().inject(this, this.getView());
        }
        LogUtils.YfcDebug("onViewCreated");
        initViews();
    }

    private void initViews() {
        nowIndex = getArguments().getInt(AllTaskListFragment.MY_TASK_TYPE,AllTaskListFragment.MY_MINE);
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        getOrder();
        initPullRefreshLayout();
        taskListView.setOnItemClickListener(new OnTaskClickListener());
//        taskListView.setOnItemLongClickListener(new OnTaskLongClickListener());
        loadingDialog = new LoadingDialog(getActivity());
        getCurrentTaskList();
    }

    public void refeshView(){
        initViews();
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
                getCurrentTaskList();
            }
        });
    }

    private void getCurrentTaskList(){
        nowIndex = getArguments().getInt(AllTaskListFragment.MY_TASK_TYPE,AllTaskListFragment.MY_MINE);
        if (NetUtils.isNetworkConnected(getActivity())) {
            if (nowIndex == AllTaskListFragment.MY_MINE) {
                getMineTasks(false);
            } else if (nowIndex == AllTaskListFragment.MY_INVOLVED) {
                getInvolvedTasks(false);
            } else if (nowIndex == AllTaskListFragment.MY_FOCUSED) {
                getFocusedTasks(false);
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
            loadingDialog.show(isDialogShow);
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
            loadingDialog.show(isDialogShow);
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
            loadingDialog.show(isDialogShow);
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
    public ArrayList<TaskResult> handleTaskList(
            GetTaskListResult getTaskListResult, ArrayList<String> chooseTagList) {
        // String[] tags = chooseTags.split(":");
        if (chooseTagList.size() == 0) {
            noSearchResultLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            taskList = getTaskListResult.getTaskList();
        } else {
            taskList = new ArrayList<TaskResult>();
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
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            noResultText.setVisibility(View.GONE);
            TaskResult taskResult = new TaskResult();
//            taskResult.setTitle(messionAddEdit.getText().toString());
            taskResult.setId(getTaskAddResult.getId());
            taskResult.setOwner(PreferencesUtils.getString(
                    getActivity(), "userID"));
            taskResult.setState("ACTIVED");
            taskList.add(taskResult);
            adapter.notifyDataSetChanged();
            isNeedRefresh = true;
        }

        @Override
        public void returnCreateTaskFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
            super.returnRecentTasksSuccess(getTaskListResult);
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
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
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
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
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

    }

}
