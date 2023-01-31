package com.inspur.emmcloud.volume.ui;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.volume.Volume;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.adapter.VolumeRecentUseAdapter;
import com.inspur.emmcloud.volume.api.VolumeAPIInterfaceInstance;
import com.inspur.emmcloud.volume.api.VolumeAPIService;
import com.inspur.emmcloud.volume.bean.GetVolumeListResult;
import com.inspur.emmcloud.volume.bean.VolumeHomePageDirectory;
import com.inspur.emmcloud.volume.ui.view.VolumeFileTransferActivity;
import com.inspur.emmcloud.volume.util.VolumeFileDownloadManager;
import com.inspur.emmcloud.volume.util.VolumeFileUploadManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VolumeHomePageFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R2.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R2.id.ibt_back)
    ImageButton backBtn;
    @BindView(R2.id.list)
    ListView listView;
    @BindView(R2.id.volume_recent_use_list)
    ListView volumeRecentUseListView;
    @BindView(R2.id.rl_tip_view)
    RelativeLayout tipViewLayout;
    @BindView(R2.id.tv_volume_tip)
    TextView volumeTipTextView;
    @BindView(R2.id.iv_down_up_list)
    ImageView downUpListIv;
    private VolumeRecentUseAdapter volumeRecentUseAdapter;
    private VolumeAPIService apiService;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.volume_fragment_homepage, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        init();
        getVolumeList(true, true);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setMineFragmentStatusBar();
        }
    }

    private void init() {
        if (getActivity().getIntent().hasExtra(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE)) {
            isHaveCopyOrMove = true;
            isCopyOrMove = getActivity().getIntent().getBooleanExtra(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE, true);
            copyFromVolume = (Volume) getActivity().getIntent().getSerializableExtra(VolumeFileBaseActivity.EXTRA_FROM_VOLUME);
            operationFileDirAbsolutePath = getActivity().getIntent().getStringExtra(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH);
            title = getActivity().getIntent().getStringExtra(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_TITLE);
            fromVolumeVolumeFileList = (List<VolumeFile>) (getActivity().getIntent().getSerializableExtra(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_LIST));
        }
        if (getActivity() instanceof VolumeHomePageActivity) {
            backBtn.setVisibility(VISIBLE);
        }
        loadingDlg = new LoadingDialog(getActivity());
        apiService = new VolumeAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        volumeRecentUseAdapter = new VolumeRecentUseAdapter(getActivity());
        volumeRecentUseListView.setAdapter(volumeRecentUseAdapter);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.header_bg_blue), ContextCompat.getColor(getContext(), R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(this);
        volumeHomePageDirectoryList.add(new VolumeHomePageDirectory(R.drawable.volume_ic_my_file, getString(R.string.volume_clouddriver_my_file), ""));
        volumeHomePageDirectoryList.add(new VolumeHomePageDirectory(R.drawable.volume_ic_share_volume, getString(R.string.volume_clouddriver_share_volume), ""));
        adapter = new Adapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("volume", myVolume);
                bundle.putString("title", getString(R.string.volume_clouddriver_my_file));
                List<Uri> uriList = null;
                if (getActivity().getIntent() != null && getActivity().getIntent().hasExtra(Constant.SHARE_FILE_URI_LIST)) {
                    uriList = (List<Uri>) getActivity().getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
                }
                /**功能划分:: 复制移动 、 网盘分享、普通打开**/
                if (isHaveCopyOrMove) {
                    bundle.putSerializable(VolumeFileBaseActivity.EXTRA_FROM_VOLUME, copyFromVolume);
                    bundle.putSerializable(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_LIST, (Serializable) fromVolumeVolumeFileList);
                    bundle.putBoolean(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE, isCopyOrMove);
                    bundle.putString(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH, operationFileDirAbsolutePath);
                    Intent intent = new Intent(getActivity(), position == 0 ? VolumeFileLocationSelectActivity.class : ShareVolumeActivity.class);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, isCopyOrMove ? VolumeFileBaseActivity.REQUEST_COPY_FILE : VolumeFileBaseActivity.REQUEST_MOVE_FILE);
                } else if (myVolume != null && isHaveCopyOrMove == false && uriList != null && uriList.size() > 0) {
                    switch (position) {
                        case 0:
                            bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
                            bundle.putString(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH, "/");
                            IntentUtils.startActivity(getActivity(), VolumeFileLocationSelectActivity.class, bundle);
                            break;
                        case 1:
                            bundle.putSerializable("shareVolumeList", (Serializable) shareVolumeList);
                            bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
                            IntentUtils.startActivity(getActivity(), ShareVolumeActivity.class, bundle);
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            bundle.putInt(VolumeFileBaseActivity.VOLUME_FROM, VolumeFileBaseActivity.MY_VOLUME);
                            IntentUtils.startActivity(getActivity(), VolumeFileActivity.class, bundle);
                            break;
                        case 1:
                            bundle.putSerializable("shareVolumeList", (Serializable) shareVolumeList);
                            IntentUtils.startActivity(getActivity(), ShareVolumeActivity.class, bundle);
                            break;
                    }
                }
            }
        });
    }

    /**
     * EventBus传递消息
     *
     * @param eventMessage
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_LOCATION_SELECT_CLOSE)) {
            if (getActivity().getIntent() != null && getActivity().getIntent().hasExtra(Constant.SHARE_FILE_URI_LIST)) {
                if (getActivity() instanceof VolumeHomePageActivity) {
                    getActivity().finish();
                }
            }
        } else if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_UPLOAD_SUCCESS) ||
                eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_DOWNLOAD_SUCCESS)) {
            refreshTipViewLayout();
        }

    }

    @OnClick({R2.id.iv_down_up_list, R2.id.option_img, R2.id.ibt_back})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            if (getActivity() instanceof VolumeHomePageActivity){
                getActivity().finish();
            }
        } else if (id == R.id.option_img) {

        } else if (id == R.id.iv_down_up_list) {
            startActivity(new Intent(getActivity(), VolumeFileTransferActivity.class));
        }
    }

    /**
     * 小红点显示状态
     */
    public void refreshTipViewLayout() {
        if (VolumeFileUploadManager.getInstance().getUnFinishUploadList().size() > 0 ||
                VolumeFileDownloadManager.getInstance().getUnFinishDownloadList().size() > 0) {
            tipViewLayout.setVisibility(View.VISIBLE);
        } else {
            tipViewLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRefresh() {
        getVolumeList(false, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getVolumeList(false, false);
        refreshTipViewLayout(); //小红点
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VolumeFileBaseActivity.REQUEST_MOVE_FILE:
                case VolumeFileBaseActivity.REQUEST_COPY_FILE:
                    getActivity().setResult(RESULT_OK, data);
                    if (getActivity() instanceof VolumeHomePageActivity) {
                        getActivity().finish();
                    }
                    break;
            }
        }
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList(boolean isShowDlg, boolean isShowNetToast) {
        if (NetUtils.isNetworkConnected(getActivity(), isShowNetToast)) {
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
            convertView = LayoutInflater.from(getActivity()).inflate(R.layout.volume_app_volume_recent_use_item_view, null);
            VolumeHomePageDirectory volumeHomePageDirectory = volumeHomePageDirectoryList.get(position);
            ((TextView) convertView.findViewById(R.id.volume_name_text)).setText(volumeHomePageDirectory.getName());
            ((TextView) convertView.findViewById(R.id.volume_capacity_text)).setText(volumeHomePageDirectory.getText());
            ((ImageView) convertView.findViewById(R.id.volume_img)).setImageResource(volumeHomePageDirectory.getIcon());
            return convertView;
        }
    }

    private class WebService extends VolumeAPIInterfaceInstance {
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
            WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
        }
    }
}
