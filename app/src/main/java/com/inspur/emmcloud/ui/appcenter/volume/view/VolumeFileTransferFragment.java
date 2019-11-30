package com.inspur.emmcloud.ui.appcenter.volume.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.adapter.VolumeFileTransferAdapter;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.VolumeActionData;
import com.inspur.emmcloud.baselib.widget.VolumeActionLayout;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.BaseMvpFragment;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClickRuleUtil;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.ui.appcenter.volume.contract.VolumeFileTransferContract;
import com.inspur.emmcloud.ui.appcenter.volume.presenter.VolumeFileTransferPresenter;
import com.inspur.emmcloud.util.privates.ShareFile2OutAppUtils;
import com.inspur.emmcloud.util.privates.ShareUtil;
import com.inspur.emmcloud.util.privates.VolumeFileDownloadManager;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.PlatformName;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 文件传输
 *
 * @author zhangyj.lc
 */
public class VolumeFileTransferFragment extends BaseMvpFragment<VolumeFileTransferPresenter> implements VolumeFileTransferContract.View {
    int currentIndex = 0;
    @BindView(R.id.volume_file_transfer_empty_layout)
    View noDataLayout;
    protected static final int SHARE_IMAGE_OR_FILES = 7;
    final List<VolumeActionData> volumeActionDataList = new ArrayList<>();
    final List<VolumeActionData> volumeActionHideList = new ArrayList<>();
    @BindView(R.id.batch_operation_header_text)
    TextView batchOprationHeaderText;
    @BindView(R.id.batch_operation_select_all_text)
    TextView getBatchOprationSelectAllText;
    @BindView(R.id.ll_volume_action)
    VolumeActionLayout volumeActionLayout;
    CustomShareListener mShareListener;

    @BindView(R.id.operation_total_layout)
    View operationTotalLayout;
    @BindView(R.id.load_count_tip)
    TextView countTipTv;
    @BindView(R.id.operation_total_btn)
    TextView operationTotalBtn;
    @BindView(R.id.volume_file_transfer_recycler)
    RecyclerView recyclerView;
    VolumeFileTransferAdapter adapter;
    VolumeFileAdapter downloadedAdapter;
    SelectCallBack selectCallBack;

    List<VolumeFile> volumeFileList = new ArrayList<>();
    VolumeFileTransferPresenter presenter;
    TextView headerRightTv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volume_file_transfer, null);
        unbinder = ButterKnife.bind(this, rootView);
        init();
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof SelectCallBack) {
            selectCallBack = (SelectCallBack) context;  //往activity传值
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        selectCallBack = null;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    String deleteAction, shareTo; //弹框点击状态

    private void init() {
        presenter = new VolumeFileTransferPresenter();
        presenter.attachView(this);

        presenter.setData();

        if (getArguments() != null) {
            currentIndex = getArguments().getInt("position");
        }

        volumeFileList = presenter.getVolumeFileList(currentIndex);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        switch (currentIndex) {
            case 0:
                adapter = new VolumeFileTransferAdapter(getActivity(), volumeFileList, Constant.TYPE_DOWNLOAD);
                recyclerView.setAdapter(adapter);
                refreshOperationTotal(volumeFileList);
                setRefreshCallBack();
                break;
            case 1:
                adapter = new VolumeFileTransferAdapter(getActivity(), volumeFileList, Constant.TYPE_UPLOAD);
                recyclerView.setAdapter(adapter);
                refreshOperationTotal(volumeFileList);
                setRefreshCallBack();
                break;
            case 2:
                downloadedAdapter = new VolumeFileAdapter(getActivity(), volumeFileList);
                recyclerView.setAdapter(downloadedAdapter);
                setListIemClick();
                setHeaderOperation();
                break;
        }
    }

    @OnClick(R.id.operation_total_btn)
    public void onClick(View v) {
        if (ClickRuleUtil.isFastClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.operation_total_btn:
                switch (currentIndex) {
                    case 0:
                        handleDownloadOperation();
                        break;
                    case 1:
                        handleUploadOperation();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 点击全部下载 暂停
     */
    private void handleDownloadOperation() {
        if (operationTotalBtn.isSelected()) {
            operationTotalBtn.setSelected(false);
            for (VolumeFile volumeFile : volumeFileList) {
                volumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_IND);
                VolumeFileDownloadManager.getInstance().reDownloadFile(volumeFile, volumeFile.getVolumeFileAbsolutePath());
            }
        } else {
            operationTotalBtn.setSelected(true);
            for (VolumeFile volumeFile : volumeFileList) {
                volumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_PAUSE);
                VolumeFileDownloadManager.getInstance().cancelDownloadVolumeFile(volumeFile);
            }
        }
        operationTotalBtn.setText(operationTotalBtn.isSelected() ? R.string.volume_file_transfer_list_continue :
                R.string.volume_file_transfer_list_pause);
        adapter.notifyDataSetChanged();
    }

    private void handleUploadOperation() {
        if (operationTotalBtn.isSelected()) {
            operationTotalBtn.setSelected(false);
            for (VolumeFile volumeFile : volumeFileList) {
                volumeFile.setStatus(VolumeFile.STATUS_UPLOAD_IND);
                VolumeFileUploadManager.getInstance().reUploadFile(volumeFile);
            }
        } else {
            operationTotalBtn.setSelected(true);
            for (VolumeFile volumeFile : volumeFileList) {
                volumeFile.setStatus(VolumeFile.STATUS_UPLOAD_PAUSE);
                VolumeFileUploadManager.getInstance().pauseVolumeFileUploadService(volumeFile);
            }
        }
        operationTotalBtn.setText(operationTotalBtn.isSelected() ? R.string.volume_file_transfer_list_continue :
                R.string.volume_file_transfer_list_pause);
        adapter.notifyDataSetChanged();
    }

    /**
     * 全部下载  全部暂停UI
     */
    private void refreshOperationTotal(List<VolumeFile> list) {
        operationTotalLayout.setVisibility(list.size() > 0 ? View.VISIBLE : View.GONE);
        boolean isShowPause = false;
        for (VolumeFile volumeFile : list) {
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_IND)
                    || volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND)) {
                isShowPause = true;
                break;
            }
        }
        operationTotalBtn.setSelected(!isShowPause);
        operationTotalBtn.setText(isShowPause ? R.string.volume_file_transfer_list_pause : R.string.volume_file_transfer_list_continue);
        if (currentIndex == 0) {
            countTipTv.setText(getString(R.string.volume_file_transfer_list_download_count, list.size()));
        } else if (currentIndex == 1) {
            countTipTv.setText(getString(R.string.volume_file_transfer_list_upload_count, list.size()));
        }
    }

    private void setRefreshCallBack() {
        adapter.setCallBack(new VolumeFileTransferAdapter.CallBack() {
            @Override
            public void refreshView(List<VolumeFile> fileList) {
                //上传、下载成功 or 长按删除   回调
                if (operationTotalLayout != null) {
                    operationTotalLayout.setVisibility(fileList.size() > 0 ? View.VISIBLE : View.GONE);
                }
                if (fileList.size() == 0) {
                    showNoDataLayout();
                } else {
                    if (countTipTv != null) {       //长按删除时 更新数量
                        if (currentIndex == 0) {
                            countTipTv.setText(getString(R.string.volume_file_transfer_list_download_count, fileList.size()));
                        } else if (currentIndex == 1) {
                            countTipTv.setText(getString(R.string.volume_file_transfer_list_upload_count, fileList.size()));
                        }
                    }
                }
            }

            @Override
            public void onStatusChange(List<VolumeFile> fileList) {  //点击item 暂停  继续
                refreshOperationTotal(fileList);
            }
        });
    }

    private void setListIemClick() {
        downloadedAdapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {

            @Override
            public void onSelectedItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
//                if (!volumeFile.getStatus().equals("normal")) {
//                    return;
//                }
                downloadedAdapter.setVolumeFileSelect(position);
                if (selectCallBack != null) {
                    selectCallBack.onSelect(downloadedAdapter.getSelectVolumeFileList());
                }
                if (downloadedAdapter.getSelectVolumeFileList().size() == volumeFileList.size()) {
                    headerRightTv.setSelected(false);
                    headerRightTv.setText(R.string.clouddriver_select_nothing);
                } else {
                    headerRightTv.setSelected(true);
                    headerRightTv.setText(R.string.select_all);
                }
                setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
            }

            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
//                if (volumeFile.getStatus().equals("normal")) {
                    if (downloadedAdapter.getSelectVolumeFileList().size() == 0) {
                        if (!downloadedAdapter.getMultiselect()) {
                            downloadOrOpenVolumeFile(volumeFile);
                        }
                    } else {
                        downloadedAdapter.setVolumeFileSelect(position);
                        setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
                    }
//                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (/*volumeFile.getStatus().equals("normal") && */!downloadedAdapter.getMultiselect()) {
                    showFileOperationDlg(volumeFileList.get(position));
                }
            }

            @Override
            public void onItemDropDownImgClick(View view, int position) {
                if (!downloadedAdapter.getMultiselect()) {
                    showFileOperationDlg(volumeFileList.get(position));
                } else {
                    downloadedAdapter.setVolumeFileSelect(position);
                }

            }

            @Override
            public void onItemOperationTextClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND)) {
                    //取消上传
                    VolumeFileUploadManager.getInstance().cancelVolumeFileUploadService(volumeFile);
                    volumeFileList.remove(position);
                    adapter.notifyItemRemoved(position);
                } else if (NetUtils.isNetworkConnected(getActivity())) {
                    //重新上传
                    volumeFile.setStatus(VolumeFile.STATUS_UPLOAD_IND);
                    VolumeFileUploadManager.getInstance().reUploadFile(volumeFile);
                    adapter.notifyItemChanged(position);
                }
            }
        });
    }

    /**
     * 头部  取消、全选 操作
     */
    private void setHeaderOperation() {
        if (getActivity() instanceof VolumeFileTransferActivity) {
            TextView headerLeftTv = ((VolumeFileTransferActivity) getActivity()).getHeaderLeftTv();
            headerRightTv = ((VolumeFileTransferActivity) getActivity()).getHeaderRightTv();
            headerRightTv.setSelected(true);
            headerLeftTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClickRuleUtil.isFastClick()) {
                        return;
                    }
                    hideBottomOperationItemShow();
                }
            });
            headerRightTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ClickRuleUtil.isFastClick()) {
                        return;
                    }
                    boolean isSelect = headerRightTv.isSelected();
                    List<VolumeFile> list = new ArrayList<>(volumeFileList);
                    downloadedAdapter.setSelectVolumeFileList(isSelect ? list : new ArrayList<VolumeFile>());
                    headerRightTv.setText(isSelect ? R.string.clouddriver_select_nothing : R.string.select_all);
                    downloadedAdapter.notifyDataSetChanged();
                    headerRightTv.setSelected(!isSelect);
                    setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
                    if (selectCallBack != null) {
                        selectCallBack.onSelect(isSelect ? list : null);
                    }
                }
            });
        }
    }

    /**
     * tab切换时reset 已下载界面
     */
    public void hideBottomOperationItemShow() {
        if (downloadedAdapter != null && volumeActionLayout != null) {
            volumeActionLayout.setVisibility(View.GONE);
            if (selectCallBack != null) {
                selectCallBack.onSelect(new ArrayList<VolumeFile>());
            }
            downloadedAdapter.clearSelectedVolumeFileList();
            downloadedAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 根据所选文件的类型展示操作按钮
     */
    protected void setBottomOperationItemShow(List<VolumeFile> selectVolumeFileList) {
        deleteAction = getString(R.string.delete);
        shareTo = getString(R.string.baselib_share_to);
        volumeActionDataList.clear();
        volumeActionHideList.clear();
        volumeActionDataList.add(new VolumeActionData(deleteAction, R.drawable.ic_volume_delete, true));
        volumeActionDataList.add(new VolumeActionData(shareTo, R.drawable.ic_volume_share, selectVolumeFileList.size() == 1));
        for (int i = 0; i < volumeActionDataList.size(); i++) {
            if (!volumeActionDataList.get(i).isShow()) {
                volumeActionDataList.remove(i);
                i--;
                continue;
            }
        }

        volumeActionLayout.setVisibility(selectVolumeFileList.size() > 0 && volumeActionDataList.size() > 0 ? View.VISIBLE : View.GONE);
        volumeActionLayout.clearView();
        volumeActionLayout.setVolumeActionData(volumeActionDataList, new VolumeActionLayout.VolumeActionClickListener() {
            @Override
            public void volumeActionSelectedListener(String actionName) {
                volumeActionLayout.setVisibility(View.GONE);
                handleVolumeAction(actionName);
            }
        });
    }

    /**
     * 处理Volume 相关的Action
     */
    private void handleVolumeAction(String action) {
        VolumeFile volumeFile = downloadedAdapter.getSelectVolumeFileList().get(0);
        if (action.equals(deleteAction)) {
            if (downloadedAdapter.getSelectVolumeFileList().size() > 0) {
                showFileDelWranibgDlg(downloadedAdapter.getSelectVolumeFileList());
            }
        } else if (action.equals(shareTo)) {
            String fileSavePath = volumeFile.getLocalFilePath();
            shareFile(fileSavePath, downloadedAdapter.getSelectVolumeFileList().get(0));
            downloadedAdapter.clearSelectedVolumeFileList();
            downloadedAdapter.notifyDataSetChanged();
            if (selectCallBack != null) {
                selectCallBack.onSelect(new ArrayList<VolumeFile>());
            }
            setBottomOperationItemShow(new ArrayList<VolumeFile>());
        }
    }

    /**
     * 弹出文件删除提示框
     *
     * @param deleteVolumeFile
     */
    protected void showFileDelWranibgDlg(final List<VolumeFile> deleteVolumeFile) {
        new CustomDialog.MessageDialogBuilder(getActivity())
                .setMessage(R.string.clouddriver_sure_delete_file)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
                        if (selectCallBack != null) {
                            selectCallBack.onSelect(downloadedAdapter.getSelectVolumeFileList());
                        }
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFile(deleteVolumeFile);
                        downloadedAdapter.setSelectVolumeFileList(new ArrayList<VolumeFile>());
                        if (selectCallBack != null) {
                            selectCallBack.onSelect(new ArrayList<VolumeFile>());
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 删除本地文件
     *
     * @param deleteVolumeFile
     */
    protected void deleteFile(List<VolumeFile> deleteVolumeFile) {
        List<String> filePathList = new ArrayList<>();
        for (VolumeFile volumeFile : deleteVolumeFile) {
            filePathList.add(volumeFile.getLocalFilePath());
            volumeFileList.remove(volumeFile);
            VolumeFileDownloadManager.getInstance().resetVolumeFileStatus(volumeFile);
            if (volumeFileList.size() == 0) {
                hideBottomOperationItemShow();
                showNoDataLayout();
            }
            downloadedAdapter.notifyDataSetChanged();
        }
        FileDownloadManager.getInstance().deleteDownloadFile(filePathList);
    }

    /**
     * 分享到微信 QQ
     **/
    public void shareFile(final String filePath, final VolumeFile volumeFile) {
        mShareListener = new CustomShareListener((BaseActivity) getActivity());
        ShareAction shareAction = new ShareAction(getActivity())
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (snsPlatform.mKeyword.equals("WEIXIN")) {
                            if (!StringUtils.isBlank(filePath)) {
                                ShareFile2OutAppUtils.shareFile2WeChat(BaseApplication.getInstance(), filePath);
                            } else {
                                ToastUtils.show(getString(R.string.clouddriver_volume_frist_download));
                            }
                        } else if (snsPlatform.mKeyword.equals("QQ")) {
                            if (!StringUtils.isBlank(filePath)) {
                                ShareFile2OutAppUtils.shareFileToQQ(BaseApplication.getInstance(), filePath);
                            } else {
                                ToastUtils.show(getString(R.string.clouddriver_volume_frist_download));
                            }
                        } else if (snsPlatform.mKeyword.equals("CLOUDPLUSE")) {
                            shareToFriends(volumeFile);
                        }
                    }
                });
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_WECHAT)) {
            shareAction.addButton(PlatformName.WEIXIN, "WEIXIN", "umeng_socialize_wechat", "umeng_socialize_wechat");
        }
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_MOBILE_QQ)) {
            shareAction.addButton(PlatformName.QQ, "QQ", "umeng_socialize_qq", "umeng_socialize_qq");
        }
        shareAction.addButton(getString(R.string.clouddrive_internal_sharing), "CLOUDPLUSE", "ic_launcher_share", "ic_launcher_share");
        shareAction.open();

    }

    /**
     * 分享到频道
     */
    private void shareToFriends(VolumeFile volumeFile) {
        List<String> urlList = new ArrayList<>();
        String filePath = volumeFile.getLocalFilePath();
        if (StringUtils.isBlank(filePath) || !FileUtils.isFileExist(filePath)) {
            ToastUtils.show(getActivity(), getString(R.string.share_not_support));
            return;
        }
        urlList.add(filePath);
        ShareUtil.startVolumeShareActivity(getActivity(), urlList);
    }

    /**
     * 下载或打开文件
     *
     * @param volumeFile
     */
    protected void downloadOrOpenVolumeFile(VolumeFile volumeFile) {
        String fileSavePath = volumeFile.getLocalFilePath();
        if (!StringUtils.isBlank(fileSavePath)) {
            try {
                FileUtils.openFile(BaseApplication.getInstance(), fileSavePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 弹出文件操作框
     *
     * @param volumeFile
     */
    protected void showFileOperationDlg(final VolumeFile volumeFile) {
        //我的文件那个网盘不再显示权限管理,共享网盘也要是自己的才能显示权限管理，否则不显示
        ActionSheetDialog.ActionListSheetBuilder builder = new ActionSheetDialog.ActionListSheetBuilder(getActivity());
        for (int i = 0; i < volumeActionHideList.size(); i++) {
            builder.addItem(volumeActionHideList.get(i).getActionName(), volumeActionHideList.get(i).isShow());
        }
        builder.setTitle(volumeFile.getName())
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        String action = (String) itemView.getTag();
                        handleVolumeAction(action);
                        dialog.dismiss();
                    }
                })
                .setOnActionSheetDlgDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
                    }
                })
                .build()
                .show();
    }


    @Override
    public void showNoDataLayout() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            noDataLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showListLayout() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            noDataLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<BaseActivity> mActivity;

        private CustomShareListener(BaseActivity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_success);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_fail);
            if (t != null) {
                LogUtils.jasonDebug("throw:" + t.getMessage());
            }

        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }

    public interface SelectCallBack {
        void onSelect(List<VolumeFile> selectVolumeFileList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSimpleEventMessage(SimpleEventMessage simpleEventMessage) {
        if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_DOWNLOAD_SUCCESS) && currentIndex == 2) {
            VolumeFile volumeFile = (VolumeFile) simpleEventMessage.getMessageObj();
            if (!volumeFileList.contains(volumeFile)) {
                volumeFileList.add(0, (VolumeFile) simpleEventMessage.getMessageObj());
                showListLayout();
                downloadedAdapter.notifyDataSetChanged();
            }
        }
    }
}
