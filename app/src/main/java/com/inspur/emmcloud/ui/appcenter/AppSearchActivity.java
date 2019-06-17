package com.inspur.emmcloud.ui.appcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AppCenterAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.GetSearchAppResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用搜索界面
 *
 * @author Administrator
 */
public class AppSearchActivity extends BaseActivity {

    private static final String ACTION_NAME = "add_app";
    private ClearEditText searchEdit;
    private List<App> searchAppList = new ArrayList<>();
    private ListView searchListView;
    private LoadingDialog loadingDlg;
    private AppCenterAdapter searchAdapter;
    private MyAppAPIService apiService;
    private BroadcastReceiver mBroadcastReceiver;
    private OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(AppSearchActivity.this);
                onSearch(findViewById(R.id.search_btn));
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_app_search;
    }

    /**
     * 初始化views
     */
    private void initViews() {
        searchEdit = (ClearEditText) findViewById(R.id.search_edit);
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchListView = (ListView) findViewById(R.id.search_app_list);
        searchAdapter = new AppCenterAdapter(
                AppSearchActivity.this, searchAppList);
        searchListView.setAdapter(searchAdapter);
        searchListView
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position, long id) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(),
                                AppDetailActivity.class);
                        intent.putExtra("app",
                                searchAppList.get(position));
                        startActivity(intent);
                    }
                });
        loadingDlg = new LoadingDialog(AppSearchActivity.this);
        searchEdit.addTextChangedListener(new SearchWatcher());
        registerReceiver();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    private void registerReceiver() {
        // TODO Auto-generated method stub
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(ACTION_NAME)) {
                    App addApp = (App) intent.getExtras()
                            .getSerializable("app");
                    searchAdapter.addApp(addApp);
                }
            }
        };

        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        // 注册广播
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    public void onBack(View v) {
        finish();
    }

    public void onSearch(View v) {
        String keyword = searchEdit.getText().toString();
        if (StringUtils.isBlank(keyword)) {
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.app_input_search_key));
            return;
        }
        searchApp(keyword);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    /**
     * 处理本地文字变化之后的列表显示
     *
     * @param s
     */
    private void handleSearchApp(CharSequence s) {
        if (StringUtils.isBlank(s.toString())) {
            if (searchAppList != null && searchAdapter != null) {
                searchAppList.clear();
                searchAdapter.notifyListData(searchAppList);
            }
        }
        //本地搜索逻辑，目前先不用本地搜索
//		if(searchList  == null){
//			return;
//		}
//		List<App> localSearchList = new ArrayList<>();
//		for (int i = 0; i < searchList.size(); i++){
//			App app = searchList.get(i);
//			if(!StringUtils.isBlank(s.toString())&&app.getAppName().contains(s)){
//				localSearchList.add(app);
//			}
//		}
//		searchList.clear();
//		searchList.addAll(localSearchList);
//		searchAdapter.notifyDataSetChanged();
    }

    /**
     * 搜索app
     *
     * @param keyword
     */
    private void searchApp(String keyword) {
        if (NetUtils.isNetworkConnected(AppSearchActivity.this)) {
            loadingDlg.show();
            apiService = new MyAppAPIService(AppSearchActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.searchApp(keyword);
        }
    }

    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            handleSearchApp(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnSearchAppSuccess(GetSearchAppResult getAllAppResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            searchAppList = getAllAppResult.getAllAppList();
            if (searchAppList.size() == 0) {
                ToastUtils.show(AppSearchActivity.this,
                        getString(R.string.app_search_null));
            }
            searchAdapter.notifyListData(searchAppList);

        }

        @Override
        public void returnSearchAppFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(AppSearchActivity.this, error, errorCode);
        }

    }

}
