package com.inspur.emmcloud.ui.appcenter.volume.view;

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
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.BaseMvpFragment;
import com.inspur.emmcloud.basemodule.util.AppUtils;
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

import java.io.File;
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

    List<VolumeFile> volumeFileList = new ArrayList<>();
    VolumeFileTransferPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_volume_file_transfer, null);
        unbinder = ButterKnife.bind(this, rootView);
        init();
        return rootView;
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
        operationTotalBtn.setSelected(false);
        operationTotalBtn.setText(R.string.volume_file_transfer_list_pause);
        switch (currentIndex) {
            case 0:
                adapter = new VolumeFileTransferAdapter(getActivity(), volumeFileList, Constant.TYPE_DOWNLOAD);
                recyclerView.setAdapter(adapter);
                operationTotalLayout.setVisibility(volumeFileList.size() > 0 ? View.VISIBLE : View.GONE);
                countTipTv.setText("正在下载...(" + volumeFileList.size() + ")");
                break;
            case 1:
                adapter = new VolumeFileTransferAdapter(getActivity(), volumeFileList, Constant.TYPE_UPLOAD);
                recyclerView.setAdapter(adapter);
                operationTotalLayout.setVisibility(volumeFileList.size() > 0 ? View.VISIBLE : View.GONE);
                countTipTv.setText("正在上传...(" + volumeFileList.size() + ")");
                break;
            case 2:
                downloadedAdapter = new VolumeFileAdapter(getActivity(), volumeFileList);
                recyclerView.setAdapter(downloadedAdapter);
                setListIemClick();
                break;
        }
    }

    @OnClick(R.id.operation_total_btn)
    public void onClick(View v) {
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
                VolumeFileDownloadManager.getInstance().downloadFile(volumeFile, volumeFile.getVolumeFileAbsolutePath());
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

    private void setListIemClick() {
        downloadedAdapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {

            @Override
            public void onSelectedItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (!volumeFile.getStatus().equals("normal")) {
                    return;
                }
                downloadedAdapter.setVolumeFileSelect(position);
                batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, downloadedAdapter.getSelectVolumeFileList().size()));
                setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
                getBatchOprationSelectAllText.setText((volumeFileList.size() == downloadedAdapter.getSelectVolumeFileList().size()) ? R.string.clouddriver_select_nothing : R.string.clouddriver_select_all);
                batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, downloadedAdapter.getSelectVolumeFileList().size()));
            }

            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals("normal")) {
                    if (downloadedAdapter.getSelectVolumeFileList().size() == 0) {
                        if (!downloadedAdapter.getMultiselect()) {
                            downloadOrOpenVolumeFile(volumeFile);
                        }
                    } else {
                        downloadedAdapter.setVolumeFileSelect(position);
                        setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
                    }
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals("normal") && !downloadedAdapter.getMultiselect()) {
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
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setBottomOperationItemShow(downloadedAdapter.getSelectVolumeFileList());
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFile(deleteVolumeFile);
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
        for (VolumeFile volumeFile : deleteVolumeFile) {
            String path = volumeFile.getLocalFilePath();
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                volumeFileList.remove(volumeFile);
                downloadedAdapter.notifyDataSetChanged();
            }
        }

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
        String fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName());
        if (!StringUtils.isBlank(fileSavePath)) {
            FileUtils.openFile(BaseApplication.getInstance(), fileSavePath);
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
        noDataLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void showListLayout() {
        noDataLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
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
}
