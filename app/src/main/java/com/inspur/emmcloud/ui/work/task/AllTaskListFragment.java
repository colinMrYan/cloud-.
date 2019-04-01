package com.inspur.emmcloud.ui.work.task;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AllScheduleFragmentAdapter;
import com.inspur.emmcloud.adapter.TaskListAdapter;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.TaskColorTag;
import com.inspur.emmcloud.bean.work.TaskResult;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by yufuchang on 2019/4/1.
 * 工作主页面下任务页面
 */
@ContentView(R.layout.fragment_all_task_list)
public class AllTaskListFragment extends Fragment{

    public static final String MY_TASK_TYPE = "task_type";
    public static final int MY_MINE = 0;
    public static final int MY_INVOLVED = 1;
    public static final int MY_FOCUSED = 2;
    public static final int MY_DONE = 3;
    public static final int MY_ALL = 4;
    private static final int MESSION_SET = 5;
    private boolean injected = false;
    @ViewInject(R.id.tl_schedule_task)
    private TabLayout tabLayoutSchedule;

    @ViewInject(R.id.lv_task)
    private ListView taskListView;
    private TaskListAdapter adapter;
    private WorkAPIService apiService;
    private LoadingDialog loadingDialog;
    private ArrayList<TaskResult> taskList = new ArrayList<TaskResult>();
    private int nowIndex = 0;
    @ViewInject(R.id.refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;
    private String orderBy = "PRIORITY";
    private String orderType = "ASC";
    @ViewInject(R.id.ll_no_search_result)
    private LinearLayout noSearchResultLayout;
    @ViewInject(R.id.tv_no_result)
    private TextView noResultText;
    @ViewInject(R.id.viewpager_calendar_holder)
    private ViewPager taskViewPager;
    private int deletePosition = -1;
    private boolean isNeedRefresh = false;
    private TaskListFragment allTaskListFragment,mineTaskListFragment,involvedTaskListFragment,focusedTaskListFragment,allReadyDoneTaskListFragment;

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

    private void initViews() {

        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText("我创建的"));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText("我参与的"));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText("我关注的"));
        tabLayoutSchedule.addTab(tabLayoutSchedule.newTab().setText("已完成的"));


//        apiService = new WorkAPIService(getActivity());
//        apiService.setAPIInterface(new WebService());
//        getOrder();
//        adapter = new TaskListAdapter(getActivity(),taskList);
//        initPullRefreshLayout();
//        loadingDialog = new LoadingDialog(getActivity());
        tabLayoutSchedule.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int index = tab.getPosition();
                if (index == MY_MINE) {
                    getMineMessions(true);
                    nowIndex = index;
                } else if (index == MY_INVOLVED) {
                    getInvolvedMessions(true);
                    nowIndex = index;
                } else if (index == MY_FOCUSED) {
                    getFocusedMessions(true);
                    nowIndex = index;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


//        if (getActivity().getIntent().hasExtra("index")) {
//            nowIndex = getActivity().getIntent().getIntExtra("index", 0);
//            if (nowIndex == MY_FOCUSED) {
//                getFocusedMessions(true);
//            } else if (nowIndex == MY_INVOLVED) {
//                getInvolvedMessions(true);
//            }
//        } else {
//            getMineMessions(true);
//        }
//        taskListView
//                .setOnItemLongClickListener(new TaskLongClickListener());
//        taskListView.setOnItemClickListener(new OnTaskClickListener());


        //建一个存放fragment的集合，并且把新的fragment放到集合中

        Bundle bundle = new Bundle();
        bundle.putInt(MY_TASK_TYPE,MY_MINE);
        allTaskListFragment = new TaskListFragment();
        allTaskListFragment.setArguments(bundle);
        mineTaskListFragment = new TaskListFragment();
        mineTaskListFragment.setArguments(bundle);
        involvedTaskListFragment = new TaskListFragment();
        involvedTaskListFragment.setArguments(bundle);
        focusedTaskListFragment = new TaskListFragment();
        focusedTaskListFragment.setArguments(bundle);
        allReadyDoneTaskListFragment = new TaskListFragment();
        allReadyDoneTaskListFragment.setArguments(bundle);
        List<Fragment> list = new ArrayList<Fragment>();
        list.add(allTaskListFragment);
        list.add(mineTaskListFragment);
        list.add(involvedTaskListFragment);
        list.add(focusedTaskListFragment);
        list.add(allReadyDoneTaskListFragment);

        //初始化adapter
        AllScheduleFragmentAdapter adapter = new AllScheduleFragmentAdapter(getActivity().getSupportFragmentManager(), list);
        //将适配器和ViewPager结合
        taskViewPager.setAdapter(adapter);

    }

    /**
     * 可能能实现控制指示器宽度的方法，未启用
     */
    private void setTabLayoutLength(){
        try{
            Field mTabStrip =tabLayoutSchedule.getClass().getDeclaredField("mTabStrip");
            mTabStrip.setAccessible(true);
            LinearLayout ltab = (LinearLayout) mTabStrip.get(tabLayoutSchedule);
            int childCount = ltab.getChildCount();
            for(int i =0; i < childCount; i++) {
                View childAt = ltab.getChildAt(i);
//                LinearLayout.LayoutParams params =new LinearLayout.LayoutParams(0, -1);
//                params.weight=1;
                childAt.setPadding(40,0,40,0);
//                childAt.setLayoutParams(params);
                childAt.invalidate();
            }

        }catch(Exception e) {
            e.printStackTrace();
        }

    }

//    /**
//     * 获取缓存的排序规则
//     */
//    private void getOrder() {
//        orderBy = PreferencesUtils.getString(getActivity(), "order_by", "PRIORITY");
//        orderType = PreferencesUtils.getString(getActivity(), "order_type", "DESC");
//    }

    /**
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetUtils.isNetworkConnected(getActivity())) {
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
    protected void getInvolvedMessions(boolean isDialogShow) {
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
    protected void getMineMessions(boolean isDialogShow) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            loadingDialog.show(isDialogShow);
            apiService.getRecentTasks(orderBy, orderType);
        }
    }

    /**
     * 创建任务
     *
     * @param messionTitle
     */
    private void createTask(String messionTitle) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            loadingDialog.show();
            apiService.createTasks(messionTitle);
        }
    }

//    @Override
//    public void onBackPressed() {
//        if (isNeedRefresh) {
//            Intent mIntent = new Intent(Constant.ACTION_TASK);
//            mIntent.putExtra("refreshTask", "refreshTask");
//            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(mIntent);
//            setResult(RESULT_OK);
//        }
//        finish();
//    }

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

//    /**
//     * 长按事件监听
//     */
//    class TaskLongClickListener implements AdapterView.OnItemLongClickListener {
//        @Override
//        public boolean onItemLongClick(AdapterView<?> parent, View view,
//                                       final int position, long id) {
//            if (nowIndex == 0 || nowIndex == 1) {
//                new MyQMUIDialog.MessageDialogBuilder(getActivity())
//                        .setMessage(R.string.mession_set_finish)
//                        .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
//                            @Override
//                            public void onClick(QMUIDialog dialog, int index) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
//                            @Override
//                            public void onClick(QMUIDialog dialog, int index) {
//                                dialog.dismiss();
//                                deleteTasks(position);
//                            }
//                        })
//                        .show();
//
//            }
//            return true;
//        }
//
//    }
//
//    /**
//     * 任务点击事件
//     */
//    class OnTaskClickListener implements AdapterView.OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position,
//                                long id) {
//            Intent intent = new Intent();
//            intent.putExtra("task", taskList.get(position));
//            intent.putExtra("tabIndex", nowIndex);
//            intent.setClass(getActivity(),
//                    MessionDetailActivity.class);
//            startActivityForResult(intent, 0);
//        }
//    }

//    class WebService extends APIInterfaceInstance {
//        @Override
//        public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {
//            if (loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            noResultText.setVisibility(View.GONE);
//            TaskResult taskResult = new TaskResult();
////            taskResult.setTitle(messionAddEdit.getText().toString());
//            taskResult.setId(getTaskAddResult.getId());
//            taskResult.setOwner(PreferencesUtils.getString(
//                    getActivity(), "userID"));
//            taskResult.setState("ACTIVED");
//            taskList.add(taskResult);
//            adapter.notifyDataSetChanged();
//            isNeedRefresh = true;
//        }
//
//        @Override
//        public void returnCreateTaskFail(String error, int errorCode) {
//            if (loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
//        }
//
//        @Override
//        public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {
//            super.returnRecentTasksSuccess(getTaskListResult);
//            if (loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            swipeRefreshLayout.setRefreshing(false);
//            String userId = ((MyApplication) getActivity().getApplicationContext()).getUid();
//            String chooseTags = PreferencesUtils.getString(
//                    getActivity(), MyApplication.getInstance().getTanent() + userId
//                            + "chooseTags", "");
//            ArrayList<String> chooseTagList = JSONUtils.JSONArray2List(chooseTags, new ArrayList<String>());
//            handleTaskList(getTaskListResult, chooseTagList);
//            handleResultUI(chooseTagList);
//            adapter = new TaskListAdapter(getActivity(),taskList);
//            taskListView.setAdapter(adapter);
////            adapter.notifyDataSetChanged();
//        }
//
//        @Override
//        public void returnRecentTasksFail(String error, int errorCode) {
//            if (loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
//        }
//
//        @Override
//        public void returnDeleteTaskSuccess() {
//            super.returnDeleteTaskSuccess();
//            if (loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            if (deletePosition != -1) {
//                taskList.remove(deletePosition);
//                adapter.notifyDataSetChanged();
//            }
//            if (taskList.size() == 0) {
//                noResultText.setVisibility(View.VISIBLE);
//            }
//            isNeedRefresh = true;
//        }
//
//        @Override
//        public void returnDeleteTaskFail(String error, int errorCode) {
//            if (loadingDialog.isShowing()) {
//                loadingDialog.dismiss();
//            }
//            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
//        }
//
//    }
}
