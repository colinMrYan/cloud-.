package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeHomePageDirectory;

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

    private boolean isCopyOrMove = false;
    private Volume copyFromVolume;
    private String operationFileDirAbsolutePath;
    private String title;
    private List<VolumeFile> fromVolumeVolumeFileList = new ArrayList<>();


    private Volume myVolume;
    private List<Volume> shareVolumeList;
    private BaseAdapter adapter;
    private List<VolumeHomePageDirectory> volumeHomePageDirectoryList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
        getVolumeList(true, true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_volume_homepage;
    }

    private void init() {
        if (getIntent().hasExtra("isFunctionCopy")) {
            isCopyOrMove = true;
            copyFromVolume = (Volume) getIntent().getSerializableExtra("fromVolume");
            operationFileDirAbsolutePath = getIntent().getStringExtra("operationFileDirAbsolutePath");
            title = getIntent().getStringExtra("title");
            fromVolumeVolumeFileList = (List<VolumeFile>) (getIntent().getSerializableExtra("volumeFileList"));
        }
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
                if (getIntent() != null && getIntent().hasExtra(Constant.SHARE_FILE_URI_LIST)) {
                    uriList = (List<Uri>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
                }
                if (isCopyOrMove) {
                    bundle.putSerializable("fromVolume", copyFromVolume);
                    bundle.putSerializable("volumeFileList", (Serializable) fromVolumeVolumeFileList);
                    bundle.putBoolean("isFunctionCopy", true);
                    bundle.putString("operationFileDirAbsolutePath", operationFileDirAbsolutePath);
                }

                switch (position) {
                    case 0:
                        if (myVolume != null && isCopyOrMove == false) {
                            if (uriList != null && uriList.size() > 0) {
                                bundle.putSerializable("fileShareUriList", (Serializable) uriList);
                                bundle.putString("operationFileDirAbsolutePath", "/");
                                IntentUtils.startActivity(VolumeHomePageActivity.this, VolumeFileLocationSelectActivity.class, bundle);
                            } else {
                                bundle.putInt(VolumeFileBaseActivity.VOLUME_FROM, VolumeFileBaseActivity.MY_VOLUME);
                                IntentUtils.startActivity(VolumeHomePageActivity.this, VolumeFileActivity.class,
                                        bundle);
                            }
                        }
                        if (isCopyOrMove) {
                            Intent intent = new Intent(VolumeHomePageActivity.this, VolumeFileLocationSelectActivity.class);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, VolumeFileBaseActivity.REQUEST_COPY_FILE);
                        }
                        break;
                    case 1:
                        if (isCopyOrMove) {
                                Intent intent = new Intent(VolumeHomePageActivity.this, ShareVolumeActivity.class);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, VolumeFileBaseActivity.REQUEST_COPY_FILE);
                        } else {
                            bundle.putSerializable("shareVolumeList", (Serializable) shareVolumeList);
                            if (uriList != null && uriList.size() > 0) {
                                bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
                            }
                            IntentUtils.startActivity(VolumeHomePageActivity.this, ShareVolumeActivity.class,
                                    bundle);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * EventBus传递消息
     * @param eventMessage
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_LOCATION_SELECT_CLOSE)) {
            if (getIntent() != null && getIntent().hasExtra(Constant.SHARE_FILE_URI_LIST)) {
                finish();
            }

        }
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
        getVolumeList(false, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getVolumeList(false, false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VolumeFileBaseActivity.REQUEST_COPY_FILE:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
            }
        }
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList(boolean isShowDlg, boolean isShowNetToast) {
        if (NetUtils.isNetworkConnected(getApplicationContext(), isShowNetToast)) {
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
