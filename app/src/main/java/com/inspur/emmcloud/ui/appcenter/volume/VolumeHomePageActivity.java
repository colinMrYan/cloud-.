package com.inspur.emmcloud.ui.appcenter.volume;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeRecentUseAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeHomePageDirectory;
import com.inspur.emmcloud.bean.system.ClearShareDataBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 云盘首页
 */
public class VolumeHomePageActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.list)
    ListView listView;
    @BindView(R.id.volume_recent_use_list)
    ListView volumeRecentUseListView;
    private VolumeRecentUseAdapter volumeRecentUseAdapter;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;

    private Volume myVolume;
    private List<Volume> shareVolumeList;
    private BaseAdapter adapter;
    private List<VolumeHomePageDirectory> volumeHomePageDirectoryList = new ArrayList<>();
    private boolean isShareState = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
        getVolumeList(true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_volume_homepage;
    }

    private void init() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        volumeRecentUseAdapter = new VolumeRecentUseAdapter(this);
        volumeRecentUseListView.setAdapter(volumeRecentUseAdapter);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue), ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(this);
        volumeHomePageDirectoryList.add(new VolumeHomePageDirectory(R.drawable.ic_volume_my_file, getString(R.string.clouddriver_my_file), ""));
        volumeHomePageDirectoryList.add(new VolumeHomePageDirectory(R.drawable.ic_volume_share_volume, getString(R.string.clouddriver_share_volume), ""));
        adapter = new Adapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("volume", myVolume);
                bundle.putString("title", getString(R.string.clouddriver_my_file));
                List<Uri> uriList = null;
                if (isShareState) {
                    uriList = (List<Uri>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
                }
                switch (position) {
                    case 0:
                        if (myVolume != null) {
                            if (uriList != null && uriList.size() > 0) {
                                bundle.putSerializable("fileShareUriList", (Serializable) uriList);
                                bundle.putString("operationFileDirAbsolutePath", "/");
                                IntentUtils.startActivity(VolumeHomePageActivity.this, VolumeFileLocationSelectActivity.class, bundle);
                            } else {
                                IntentUtils.startActivity(VolumeHomePageActivity.this, VolumeFileActivity.class,
                                        bundle);
                            }
                        }
                        break;
                    case 1:
                        bundle.putSerializable("shareVolumeList", (Serializable) shareVolumeList);
                        if (uriList != null && uriList.size() > 0) {
                            bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
                        }
                        IntentUtils.startActivity(VolumeHomePageActivity.this, ShareVolumeActivity.class,
                                bundle);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 接收来自上传页面的消息
     *
     * @param clearShareDataBean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void clearShareData(ClearShareDataBean clearShareDataBean) {
        isShareState = false;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.option_img:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRefresh() {
        getVolumeList(false);
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show(isShowDlg);
            apiService.getVolumeList();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return volumeHomePageDirectoryList.size();
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
            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.app_volume_recent_use_item_view, null);
            VolumeHomePageDirectory volumeHomePageDirectory = volumeHomePageDirectoryList.get(position);
            ((TextView) convertView.findViewById(R.id.volume_name_text)).setText(volumeHomePageDirectory.getName());
            ((TextView) convertView.findViewById(R.id.volume_capacity_text)).setText(volumeHomePageDirectory.getText());
            ((ImageView) convertView.findViewById(R.id.volume_img)).setImageResource(volumeHomePageDirectory.getIcon());
            return convertView;
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            swipeRefreshLayout.setRefreshing(false);
            shareVolumeList = getVolumeListResult.getShareVolumeList();
            myVolume = getVolumeListResult.getMyVolume();
            if (myVolume != null) {
                String volumeUsedSize = FileUtils.formatFileSize(myVolume.getQuotaUsed());
                String volumeMaxSize = FileUtils.formatFileSize(myVolume.getQuotaTotal());
                volumeHomePageDirectoryList.get(0).setText(volumeUsedSize + " / " + volumeMaxSize);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnVolumeListFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }

}
