package com.inspur.emmcloud.ui.app.groupnews;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.NewsListAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.bean.GroupNews;
import com.inspur.emmcloud.bean.NewsIntrcutionUpdateEvent;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;

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
public class GroupNewsCardFragment extends Fragment implements
        OnRefreshListener {
    private static final String ARG_POSITION = "position";
    private View v;
    private LayoutInflater inflater;
    private LoadingDialog loadingDlg;
    private MyAppAPIService apiService;
    private ListAdapter adapter;
    private PullableListView myListView;
    private PullToRefreshLayout pullToRefreshLayout;
    private int page = 0;
    private boolean haveData = false;
    private boolean isPullup = true;
    private List<GroupNews> groupnNewsList = new ArrayList<GroupNews>();
    private String pagerTitle = "";

    public GroupNewsCardFragment() {
    }

    public GroupNewsCardFragment(int position, String catagoryid, String title, boolean hasExtraPermission) {
        // TODO Auto-generated constructor stub
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        b.putString("catagoryid", catagoryid);
        b.putBoolean("hasExtraPermission", hasExtraPermission);
        this.setArguments(b);
        this.pagerTitle = title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.fragment_news, null);
        loadingDlg = new LoadingDialog(getActivity());
        apiService = new MyAppAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        pullToRefreshLayout = (PullToRefreshLayout) v
                .findViewById(R.id.refresh_view);
        pullToRefreshLayout.setOnRefreshListener(this);
        myListView = (PullableListView) v.findViewById(R.id.news_listView);
        myListView.setVerticalScrollBarEnabled(false);
        myListView.setCanPullUp(true);
        myListView.setOnItemClickListener(new ListItemOnClickListener());
        getGroupNewsList(getArguments().getString("catagoryid"),0);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateNewsDataById(NewsIntrcutionUpdateEvent messageEvent) {
        for (int i = 0; i < groupnNewsList.size(); i++) {
            if (groupnNewsList.get(i).getId().equals(messageEvent.getId())) {
                groupnNewsList.get(i).setOriginalEditorComment(messageEvent.getOriginalEditorComment());
                groupnNewsList.get(i).setEditorCommentCreated(messageEvent.isEditorCommentCreated());
                break;
            }
        }
    }

    /**
     * 获取每个标题下的新闻列表
     */
    private void getGroupNewsList(String catagoryid,int page) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getActivity())) {
            loadingDlg.show();
            apiService.getGroupNewsDetail(catagoryid, page);
        } else {
            if (pullToRefreshLayout != null) {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (v == null) {
            v = inflater.inflate(R.layout.fragment_work, container, false);
        }
        ViewGroup parent = (ViewGroup) v.getParent();
        if (parent != null) {
            parent.removeView(v);
        }
        return v;
    }

    class ListItemOnClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), NewsWebDetailActivity.class);
            intent.putExtra("groupNews",groupnNewsList.get(position));
            startActivity(intent);
        }

    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGroupNewsDetailSuccess(
                GetGroupNewsDetailResult getGroupNewsDetailResult) {
            if (!isPullup) {
                groupnNewsList.clear();
            }
            groupnNewsList.addAll(getGroupNewsDetailResult.getGroupNews());
            if (groupnNewsList != null && groupnNewsList.size() > 0) {
                adapter = new NewsListAdapter(getActivity(), groupnNewsList);
                if (getGroupNewsDetailResult.getGroupNews().size() < 20) {
                    haveData = false;
                } else {
                    haveData = true;
                }
                myListView.setAdapter(adapter);
                if (groupnNewsList.size() <= 20) {
                    myListView.setSelection(0);
                } else {
                    myListView.setSelection(groupnNewsList.size() - (getGroupNewsDetailResult.getGroupNews()
                            .size() + 4));
                }
            }
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
        }

        @Override
        public void returnGroupNewsDetailFail(String error, int errorCode) {
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }

    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        if (NetUtils.isNetworkConnected(getActivity())) {
            isPullup = false;
            getGroupNewsList(getArguments().getString("catagoryid"),0);
            page = 0;
        } else {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
        }
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        // TODO Auto-generated method stub
        isPullup = true;
        if (haveData) {
            page = page + 1;
            getGroupNewsList(getArguments().getString("catagoryid"), page);
        } else {
            Toast.makeText(getActivity(),
                    getString(R.string.no_more_data),
                    Toast.LENGTH_SHORT).show();
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
        }

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
