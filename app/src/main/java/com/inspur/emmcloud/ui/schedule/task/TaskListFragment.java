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

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.TaskListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.ui.work.task.MessionDetailActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2019/4/1.
 */
@ContentView(R.layout.fragment_task_list)
public class TaskListFragment extends Fragment {

    @ViewInject(R.id.lv_task)
    private ListView taskListView;
    @ViewInject(R.id.refresh_layout)
    private MySwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.ll_no_search_result)
    private LinearLayout noSearchResultLayout;
    @ViewInject(R.id.tv_no_result)
    private TextView noResultText;
    private boolean injected = false;
    private String orderBy = "PRIORITY";
    private String orderType = "ASC";
    private int deletePosition = -1;
    private String searchContent = "";
    private ArrayList<Task> uiTaskList = new ArrayList<>();
    private TaskListAdapter adapter;
    private WorkAPIService apiService;
    private int nowIndex = 0;
    private ArrayList<Task> taskList = new ArrayList<Task>();
    private boolean isPullUp =false;
    private int page = 0;


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
        swipeRefreshLayout.setCanLoadMore(StringUtils.isBlank(searchContent));
        swipeRefreshLayout.setEnabled(StringUtils.isBlank(searchContent));
        this.searchContent = searchContent;
        if(adapter != null){
            searchTaskListBySearchContent();
        }
    }

    /**
     * 告知Fragment当前索引
     * @param nowIndex
     */
    public void setNowIndex(int nowIndex){
        this.nowIndex = nowIndex;
        swipeRefreshLayout.setCanLoadMore(nowIndex == TaskFragment.MY_DONE);
    }

    /**
     * 根据搜索内容搜索列表
     */
    private void searchTaskListBySearchContent() {
        uiTaskList.clear();
        for(Task task:taskList){
            if(task.getTitle().contains(searchContent)){
                uiTaskList.add(task);
            }
        }
        adapter.setAndChangeData(uiTaskList);
    }

    private void initViews() {
        nowIndex = getArguments().getInt(TaskFragment.MY_TASK_TYPE, TaskFragment.MY_MINE);
        apiService = new WorkAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        getOrder();
        initPullRefreshLayout();
        taskListView.setOnItemClickListener(new OnTaskClickListener());
        adapter = new TaskListAdapter(getActivity(),uiTaskList);
        taskListView.setAdapter(adapter);
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
        //已完成页面设置可以上拉加载
        swipeRefreshLayout.setCanLoadMore(nowIndex == TaskFragment.MY_DONE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPullUp = false;
                page = 0;
                if(NetUtils.isNetworkConnected(getActivity())){
                    getCurrentTaskList(false);
                }else{
                    swipeRefreshLayout.setLoading(false);
                }
                swipeRefreshLayout.setCanLoadMore(nowIndex == TaskFragment.MY_DONE);
            }
        });
        swipeRefreshLayout.setOnLoadListener(new MySwipeRefreshLayout.OnLoadListener() {
            @Override
            public void onLoadMore() {
                if (NetUtils.isNetworkConnected(getActivity())) {
                    apiService.getAllTasks(page, 12, "REMOVED");
                    isPullUp = true;
                } else {
                    swipeRefreshLayout.setLoading(false);
                }
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
            }else if(nowIndex == TaskFragment.MY_DONE){
                getAllFinishTasks(isDialogShow);
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
     * 获取所有任务
     */
    private void getAllFinishTasks(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            swipeRefreshLayout.setRefreshing(true);
            apiService.getAllTasks(0, 12, "REMOVED");
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
            apiService.deleteTasks(uiTaskList.get(position).getId());
            deletePosition = position;
        }
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
            intent.putExtra("task", uiTaskList.get(position));
            intent.putExtra("tabIndex", nowIndex);
            intent.setClass(getActivity(),
                    MessionDetailActivity.class);
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
                swipeRefreshLayout.setCanLoadMore(nowIndex == TaskFragment.MY_DONE && getTaskListResult.getTaskList().size()>=12);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                taskList = getTaskListResult.getTaskList();
            }
            noResultText.setVisibility(taskList.size()>0?View.GONE:View.VISIBLE);
            uiTaskList.clear();
            uiTaskList.addAll(taskList);
            adapter.setAndChangeData(uiTaskList);
        }

        @Override
        public void returnRecentTasksFail(String error, int errorCode) {
            swipeRefreshLayout.setLoading(false);
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

        @Override
        public void returnDeleteTaskSuccess() {
            super.returnDeleteTaskSuccess();
            swipeRefreshLayout.setRefreshing(false);
            if (deletePosition != -1) {
                uiTaskList.remove(deletePosition);
                adapter.notifyDataSetChanged();
            }
            if (uiTaskList.size() == 0) {
                noResultText.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void returnDeleteTaskFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

    }

}
