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
import android.widget.RelativeLayout;
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
import com.inspur.emmcloud.ui.appcenter.volume.view.VolumeFileTransferActivity;
import com.inspur.emmcloud.util.privates.VolumeFileDownloadManager;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


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
    @BindView(R.id.rl_tip_view)
    RelativeLayout tipViewLayout;
    @BindView(R.id.tv_volume_tip)
    TextView volumeTipTextView;
    @BindView(R.id.iv_down_up_list)
    ImageView downUpListIv;
    private VolumeRecentUseAdapter volumeRecentUseAdapter;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;

    private boolean isCopyOrMove = false;
    private boolean isHaveCopyOrMove = false;
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
        if (getIntent().hasExtra(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE)) {
            isHaveCopyOrMove = true;
            isCopyOrMove = getIntent().getBooleanExtra(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE, true);
            copyFromVolume = (Volume) getIntent().getSerializableExtra(VolumeFileBaseActivity.EXTRA_FROM_VOLUME);
            operationFileDirAbsolutePath = getIntent().getStringExtra(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH);
            title = getIntent().getStringExtra(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_TITLE);
            fromVolumeVolumeFileList = (List<VolumeFile>) (getIntent().getSerializableExtra(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_LIST));
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
                /**功能划分:: 复制移动 、 网盘分享、普通打开**/
                if (isHaveCopyOrMove) {
                    bundle.putSerializable(VolumeFileBaseActivity.EXTRA_FROM_VOLUME, copyFromVolume);
                    bundle.putSerializable(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_LIST, (Serializable) fromVolumeVolumeFileList);
                    bundle.putBoolean(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE, isCopyOrMove);
                    bundle.putString(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH, operationFileDirAbsolutePath);
                    Intent intent = new Intent(VolumeHomePageActivity.this, position == 0 ? VolumeFileLocationSelectActivity.class : ShareVolumeActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, isCopyOrMove ? VolumeFileBaseActivity.REQUEST_COPY_FILE : VolumeFileBaseActivity.REQUEST_MOVE_FILE);
                } else if (myVolume != null && isHaveCopyOrMove == false && uriList != null && uriList.size() > 0) {
                    switch (position) {
                        case 0:
                            bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
                            bundle.putString(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH, "/");
                            IntentUtils.startActivity(VolumeHomePageActivity.this, VolumeFileLocationSelectActivity.class, bundle);
                            break;
                        case 1:
                            bundle.putSerializable("shareVolumeList", (Serializable) shareVolumeList);
                            bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
                            IntentUtils.startActivity(VolumeHomePageActivity.this, ShareVolumeActivity.class, bundle);
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            bundle.putInt(VolumeFileBaseActivity.VOLUME_FROM, VolumeFileBaseActivity.MY_VOLUME);
                            IntentUtils.startActivity(VolumeHomePageActivity.this, VolumeFileActivity.class, bundle);
                            break;
                        case 1:
                            bundle.putSerializable("shareVolumeList", (Serializable) shareVolumeList);
                            IntentUtils.startActivity(VolumeHomePageActivity.this, ShareVolumeActivity.class, bundle);
                            break;
                    }
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
        } else if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_UPLOAD_SUCCESS) ||
                eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_DOWNLOAD_SUCCESS)) {
            refreshTipViewLayout();
        }

    }

    @OnClick(R.id.iv_down_up_list)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.option_img:
                break;
            case R.id.iv_down_up_list:
                startActivity(new Intent(this, VolumeFileTransferActivity.class));
                break;
            default:
                break;
        }
    }

    /**
     * 小红点显示状态
     */
    public void refreshTipViewLayout() {
        if (VolumeFileUploadManager.getInstance().getAllUploadVolumeFile().size() > 0 ||
                VolumeFileDownloadManager.getInstance().getAllDownloadVolumeFile().size() > 0) {
            tipViewLayout.setVisibility(View.VISIBLE);
        } else {
            tipViewLayout.setVisibility(View.GONE);
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
        refreshTipViewLayout(); //小红点
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VolumeFileBaseActivity.REQUEST_MOVE_FILE:
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
