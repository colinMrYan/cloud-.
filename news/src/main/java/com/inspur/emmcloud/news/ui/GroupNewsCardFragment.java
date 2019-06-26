package com.inspur.emmcloud.news.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.news.R;
import com.inspur.emmcloud.news.adapter.NewsListAdapter;
import com.inspur.emmcloud.news.api.NewsAPIInsterfaceImpl;
import com.inspur.emmcloud.news.api.NewsApiService;
import com.inspur.emmcloud.news.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.news.bean.GroupNews;
import com.inspur.emmcloud.news.bean.NewsIntrcutionUpdateEvent;
import com.inspur.emmcloud.news.widget.WaterMarkBgSingleLine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻列表页面
 *
 * @author sunqx
 */
@SuppressLint("ValidFragment")
public class GroupNewsCardFragment extends Fragment implements MySwipeRefreshLayout.OnRefreshListener, MySwipeRefreshLayout.OnLoadListener {
    private static final String ARG_POSITION = "position";
    private View rootView;
    private LoadingDialog loadingDlg;
    private ListView newsListView;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private int page = 0;
    private List<GroupNews> groupnNewsList = new ArrayList<GroupNews>();
    private String pagerTitle = "";
    private NewsListAdapter newsAdapter;

    public GroupNewsCardFragment() {
    }

    public GroupNewsCardFragment(int position, String catagoryId, String title, boolean hasExtraPermission) {
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        b.putString("catagoryid", catagoryId);
        b.putBoolean("hasExtraPermission", hasExtraPermission);
        this.setArguments(b);
        this.pagerTitle = title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.news_fragment, null);
        loadingDlg = new LoadingDialog(getActivity());
        swipeRefreshLayout = rootView.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnLoadListener(this);
        newsListView = rootView.findViewById(R.id.news_listView);
        newsListView.setVerticalScrollBarEnabled(false);
        newsListView.setOnItemClickListener(new ListItemOnClickListener());
        newsAdapter = new NewsListAdapter(getActivity(), groupnNewsList);
        newsListView.setAdapter(newsAdapter);
        String myInfo = PreferencesUtils.getString(getContext(), "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        newsListView.setBackground(new WaterMarkBgSingleLine(getContext(), getMyInfoResult.getCode()));
        getGroupNewsList(getArguments().getString("catagoryid"), 0, true);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.news_fragment, container, false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    /**
     * 根据新闻Id更新新闻批示
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateNewsDataById(NewsIntrcutionUpdateEvent messageEvent) {
        for (int i = 0; i < groupnNewsList.size(); i++) {
            GroupNews groupNews = groupnNewsList.get(i);
            if (groupNews.getId().equals(messageEvent.getId())) {
                groupNews.setOriginalEditorComment(messageEvent.getOriginalEditorComment());
                groupNews.setEditorCommentCreated(messageEvent.isEditorCommentCreated());
                break;
            }
        }
    }

    /**
     * 处理新闻列表，记录当前刷新成功的页编号，设置显示位置
     *
     * @param getGroupNewsDetailResult
     */
    private void handleNewsList(GetGroupNewsDetailResult getGroupNewsDetailResult) {
        //如果page是0即下拉刷新时，需要清空新闻列表
        if (page == 0) {
            groupnNewsList.clear();
        }
        List<GroupNews> newgroupNewsList = getGroupNewsDetailResult.getGroupNews();
        //返回结果少于20时，说明服务端暂时没有更新数据，则禁止上拉加载，并提示，减少向服务端发送无效请求
        swipeRefreshLayout.setCanLoadMore(newgroupNewsList.size() >= 20);
        //添加新数据，如果page是0则重新添加，不是0则在末尾继续添加
        groupnNewsList.addAll(newgroupNewsList);
        //刷新数据
        newsAdapter.reFreshNewsList(groupnNewsList);
        swipeRefreshLayout.setLoading(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 获取每个标题下的新闻列表
     */
    private void getGroupNewsList(String catagoryId, int page, boolean isRefresh) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            if (isRefresh) {
                swipeRefreshLayout.setRefreshing(true);
            }
            NewsApiService apiService = new NewsApiService(getActivity());
            apiService.setAPIInterface(new WebService());
            apiService.getGroupNewsDetail(catagoryId, page);
        } else {
            swipeRefreshLayout.setLoading(false);
        }
    }

    @Override
    public void onRefresh() {
        getGroupNewsList(getArguments().getString("catagoryid"), 0, false);
    }

    @Override
    public void onLoadMore() {
        getGroupNewsList(getArguments().getString("catagoryid"), page + 1, false);
    }

    class ListItemOnClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), NewsWebDetailActivity.class);
            intent.putExtra("groupNews", groupnNewsList.get(position));
            startActivity(intent);
        }
    }

    class WebService extends NewsAPIInsterfaceImpl {
        @Override
        public void returnGroupNewsDetailSuccess(
                GetGroupNewsDetailResult getGroupNewsDetailResult, int page) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            //请求成功，记录当前page数值
            GroupNewsCardFragment.this.page = page;
            //处理新闻列表
            handleNewsList(getGroupNewsDetailResult);
        }

        @Override
        public void returnGroupNewsDetailFail(String error, int errorCode, int page) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            //记录刷新页数，刷新失败保持已经刷新出来的页编号，下拉刷新则把页面编号设为0
            GroupNewsCardFragment.this.page = (page > 0) ? (page - 1) : page;
            swipeRefreshLayout.setLoading(false);
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }
    }
}
