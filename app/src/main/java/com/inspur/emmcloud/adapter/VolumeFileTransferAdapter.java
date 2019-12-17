package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.text.HtmlCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.progressbar.CircleProgressBar;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ClickRuleUtil;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.ui.appcenter.volume.observe.LoadObservable;
import com.inspur.emmcloud.util.privates.NetworkMobileTipUtil;
import com.inspur.emmcloud.util.privates.VolumeFileDownloadCacheUtils;
import com.inspur.emmcloud.util.privates.VolumeFileDownloadManager;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 文件传输  上传、下载适配
 *
 * @author zhangyj.lc
 */
public class VolumeFileTransferAdapter extends RecyclerView.Adapter<VolumeFileTransferAdapter.ViewHolder> {

    private Context context;
    private List<VolumeFile> unfinishedFileList = new ArrayList<>();
    private String type;    //上传 or 下载
    private CallBack callBack;

    public VolumeFileTransferAdapter(Context context, List<VolumeFile> unfinishedFileList, String type) {
        this.context = context;
        this.unfinishedFileList = unfinishedFileList;
        this.type = type;
    }

    public void setUnfinishedFileList(List<VolumeFile> fileList) {
        this.unfinishedFileList = fileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.app_volume_file_transfer_item_view, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (unfinishedFileList == null || unfinishedFileList.size() == 0) {
            return;
        }
        VolumeFile volumeFile = unfinishedFileList.get(position);
        holder.itemView.setTag(position);
        //显示图标
        showVolumeFileTypeImg(holder.icon, volumeFile);
        holder.nameTv.setText(volumeFile.getName());
        showVolumeFileDesc(holder, volumeFile);
        holder.progressBar.setProgress(volumeFile.getProgress());
        showVolumeFileSpeed(holder, position);
        holder.descTv.setTag(position);
        holder.statusLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return unfinishedFileList == null ? 0 : unfinishedFileList.size();
    }

    private synchronized void syncListData() {
        if (type.equals(Constant.TYPE_DOWNLOAD)) {
            unfinishedFileList = VolumeFileDownloadManager.getInstance().getUnFinishDownloadList();
        } else if (type.equals(Constant.TYPE_UPLOAD)) {
            unfinishedFileList = VolumeFileUploadManager.getInstance().getUnFinishUploadList();
        }
    }

    private void showVolumeFileTypeImg(ImageView imageView, VolumeFile volumeFile) {
        Integer fileIconResId = null;
        imageView.setTag("");

        if (volumeFile.getFormat().startsWith("image/")) {
            String url = "";
            if (volumeFile.getLoadType().equals(VolumeFile.STATUS_LOADING)) {
                url = volumeFile.getLocalFilePath();
            } else {
                String path = volumeFile.getLocalFilePath() + volumeFile.getName();
                url = APIUri.getVolumeFileTypeImgThumbnailUrl(volumeFile, path);
            }
            imageView.setTag(url);
            ImageDisplayUtils.getInstance().displayImageByTag(imageView, url, R.drawable.baselib_file_type_img);
            return;
        } else {
            fileIconResId = FileUtils.getFileIconResIdByFormat(volumeFile.getFormat());
        }

        imageView.setImageResource(fileIconResId);
    }

    /**
     * 文件描述  正在等待 or 文件大小
     *
     * @param volumeFile
     */
    private void showVolumeFileDesc(ViewHolder holder, VolumeFile volumeFile) {
        if (volumeFile.getStatus().equals(VolumeFile.STATUS_PAUSE)) {
            holder.descTv.setText(R.string.volume_file_transfer_list_pause_tip);
        } else if (volumeFile.getStatus().equals(VolumeFile.STATUS_SUCCESS)) {
            switch (volumeFile.getLoadType()) {
                case VolumeFile.TYPE_DOWNLOAD:
                    holder.descTv.setText(R.string.download_complete);
                    break;
                case VolumeFile.TYPE_UPLOAD:
                    holder.descTv.setText(R.string.clouddriver_upload_success);
                    break;
                default:
                    holder.descTv.setText(FileUtils.formatFileSize(volumeFile.getSize()));
                    break;
            }
        } else if (volumeFile.getStatus().equals(VolumeFile.STATUS_FAIL)) {
            switch (volumeFile.getLoadType()) {
                case VolumeFile.TYPE_DOWNLOAD:
                    String tipStr = context.getResources().getString(R.string.download_fail_tip);
                    holder.descTv.setText(HtmlCompat.fromHtml(tipStr, HtmlCompat.FROM_HTML_MODE_LEGACY));
                    break;
                case VolumeFile.TYPE_UPLOAD:
                    holder.descTv.setText(HtmlCompat.fromHtml(context.getResources().getString(R.string.upload_fail_tip), HtmlCompat.FROM_HTML_MODE_LEGACY));
                    break;
                default:
                    holder.descTv.setText(FileUtils.formatFileSize(volumeFile.getSize()));
                    break;
            }
        } else {
            holder.descTv.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        }
    }

    private void showVolumeFileSpeed(final ViewHolder holder, final int position) {
        if (unfinishedFileList == null || unfinishedFileList.size() == 0) {
            return;
        }
        final VolumeFile volumeFile = unfinishedFileList.get(position);
        holder.progressBar.setStatus(volumeFile.transfer2ProgressStatus(volumeFile.getStatus()));
        if (volumeFile.getProgress() == -1) {
            holder.progressBar.setProgress(0);
        } else {
            holder.progressBar.setProgress(volumeFile.getProgress());
        }
        if (volumeFile.getStatus().equals(VolumeFile.STATUS_LOADING) ||
                volumeFile.getStatus().equals(VolumeFile.STATUS_PAUSE)) {
            if (volumeFile.getLoadType().equals(VolumeFile.TYPE_UPLOAD)) {
                handleUploadCallback(holder, volumeFile);
            } else if (volumeFile.getLoadType().equals(VolumeFile.TYPE_DOWNLOAD)) {
                handleDownloadCallback(holder, volumeFile);
            }
        }
        holder.speedTv.setVisibility(volumeFile.getStatus().equals(VolumeFile.STATUS_LOADING) ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 监听上传回调
     */
    private void handleUploadCallback(final ViewHolder holder, final VolumeFile originVolumeFile) {
        VolumeFileUploadManager.getInstance().setBusinessProgressCallback(originVolumeFile, new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                Log.d("zhang", "handleUploadCallback onSuccess: ");
//                holder.descTv.setText(R.string.download_complete);
                holder.itemView.setEnabled(false);
//                fileList.remove(volumeFile);
                //下载成功停留2S中转换状态
//                VolumeFileUpload volumeFileUpload = VolumeFileUploadManager.getInstance().
//                        getVolumeFileUpload(volumeFile);
//                if (volumeFileUpload != null) {
//                    volumeFileUpload.setStatus(VolumeFile.STATUS_NORMAL);
//                    VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
//                }
                holder.progressBar.setStatus(CircleProgressBar.Status.Success);
                syncListData();
                notifyDataSetChanged();
                if (callBack != null) {
                    callBack.refreshView(unfinishedFileList);
                }
            }

            @Override
            public void onLoading(int progress, long current, String speed) {
                if (originVolumeFile.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                    if (!StringUtils.isBlank(speed)) {
                        holder.speedTv.setText(speed);
                    }
                }
                Log.d("zhang", "upLoading: progress = " + progress
                        + ",speed = " + speed + ",status = " + originVolumeFile.getStatus());
                originVolumeFile.setProgress(progress);
                if (progress > 0) {
                    holder.progressBar.setProgress(progress);
                }
            }

            @Override
            public void onFail() {
                holder.progressBar.setStatus(CircleProgressBar.Status.Fail);
                holder.descTv.setText(FileUtils.formatFileSize(originVolumeFile.getSize()));
                for (int i = 0; i < unfinishedFileList.size(); i++) {
                    if (unfinishedFileList.get(i).getId().equals(originVolumeFile.getId())) {
                        unfinishedFileList.get(i).setStatus(VolumeFile.STATUS_FAIL);
                        notifyDataSetChanged();
                        break;
                    }
                }
                if (callBack != null) {
                    callBack.onStatusChange(unfinishedFileList);
                }
            }
        });
    }

    /**
     * 监听下载回调
     */
    private void handleDownloadCallback(final ViewHolder holder, final VolumeFile originVolumeFile) {
        VolumeFileDownloadManager.getInstance().setBusinessProgressCallback(originVolumeFile, new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                Log.d("zhang", "handleDownloadCallback onSuccess: ");
                holder.progressBar.setStatus(CircleProgressBar.Status.Success);
                holder.descTv.setText(R.string.download_complete);
                holder.itemView.setEnabled(false);
//                fileList.remove(volumeFile);
                //下载成功停留2S中转换状态
//                originVolumeFile.setStatus(VolumeFile.STATUS_NORMAL);
//                VolumeFileDownloadCacheUtils.saveVolumeFile(originVolumeFile);
                syncListData();
                notifyDataSetChanged();
                if (callBack != null) {
                    callBack.refreshView(unfinishedFileList);
                }
            }

            @Override
            public void onLoading(int progress, long current, String speed) {
//                Log.d("zhang", "downLoading: progress = " + progress
//                        + ",speed = " + speed + ",status = " + volumeFile.getStatus());
                if (originVolumeFile.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                    if (!StringUtils.isBlank(speed)) {
                        holder.speedTv.setText(speed);
                    }
                }
                originVolumeFile.setProgress(progress);
                if (progress > 0) {
                    holder.progressBar.setProgress(progress);
                }
            }

            @Override
            public void onFail() {
                holder.progressBar.setStatus(CircleProgressBar.Status.Fail);
                for (VolumeFile item : unfinishedFileList) {
                    if (item.getId().equals(originVolumeFile.getId())) {
                        item.setStatus(VolumeFile.STATUS_FAIL);
//                        showVolumeFileDesc(holder, item);
                        notifyDataSetChanged();
                        break;
                    }
                }
                if (callBack != null) {
                    callBack.onStatusChange(unfinishedFileList);
                }
            }
        });
    }

    public interface MyItemOnClickListener {
        void onItemClick(View view, int position);
    }

    private void changeStatus(final int position) {
        final VolumeFile volumeFile = unfinishedFileList.get(position);
        String status = volumeFile.getStatus();
        if (status.equals(VolumeFile.STATUS_LOADING)) {
            if (volumeFile.getLoadType().equals(VolumeFile.TYPE_UPLOAD)) {
                //暂停上传
                unfinishedFileList.get(position).setStatus(VolumeFile.STATUS_PAUSE);
                VolumeFileUploadManager.getInstance().pauseVolumeFileUploadService(volumeFile);
            } else if (volumeFile.getLoadType().equals(VolumeFile.TYPE_DOWNLOAD)) {
                unfinishedFileList.get(position).setStatus(VolumeFile.STATUS_PAUSE);
                VolumeFileDownloadManager.getInstance().cancelDownloadVolumeFile(volumeFile);
                VolumeFileDownloadCacheUtils.saveVolumeFile(volumeFile);
            }
        } else if (status.equals(VolumeFile.STATUS_PAUSE)) {
            if (volumeFile.getLoadType().equals(VolumeFile.TYPE_UPLOAD)) {
                //继续上传
                NetworkMobileTipUtil.checkEnvironment(context, R.string.volume_file_upload_network_type_warning,
                        volumeFile.getSize(), new NetworkMobileTipUtil.Callback() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void onNext() {
                                unfinishedFileList.get(position).setStatus(VolumeFile.STATUS_LOADING);
                                volumeFile.setStatus(VolumeFile.STATUS_LOADING);
                                VolumeFileUploadManager.getInstance().reUploadFile(volumeFile);
                                notifyItemChanged(position);
                                if (callBack != null) {
                                    callBack.onStatusChange(unfinishedFileList);
                                }
                            }
                        });

            } else if (volumeFile.getLoadType().equals(VolumeFile.TYPE_DOWNLOAD)) {
                //继续下载
                NetworkMobileTipUtil.checkEnvironment(context, R.string.volume_file_download_network_type_warning,
                        volumeFile.getSize(), new NetworkMobileTipUtil.Callback() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void onNext() {
                                unfinishedFileList.get(position).setStatus(VolumeFile.STATUS_LOADING);
                                Log.d("zhang", "changeStatus: downloadFile");
                                volumeFile.setStatus(VolumeFile.STATUS_LOADING);
                                VolumeFileDownloadManager.getInstance().reDownloadFile(volumeFile, volumeFile.getVolumeFileAbsolutePath());
                                VolumeFileDownloadCacheUtils.saveVolumeFile(volumeFile);
                                notifyItemChanged(position);
                                if (callBack != null) {
                                    callBack.onStatusChange(unfinishedFileList);
                                }
                            }
                        });

            }
        }
    }

    private void handleDescFail(int position) {
        VolumeFile volumeFile = unfinishedFileList.get(position);
        String status = volumeFile.getStatus();

        if (status.equals(VolumeFile.STATUS_FAIL)) {
            if (volumeFile.getLoadType().equals(VolumeFile.TYPE_UPLOAD)) {
                //继续上传
                unfinishedFileList.get(position).setStatus(VolumeFile.STATUS_LOADING);
                volumeFile.setStatus(VolumeFile.STATUS_LOADING);
                VolumeFileUploadManager.getInstance().reUploadFile(volumeFile);
            } else if (volumeFile.getLoadType().equals(VolumeFile.TYPE_DOWNLOAD)) {
                //继续下载
                unfinishedFileList.get(position).setStatus(VolumeFile.STATUS_LOADING);
                Log.d("zhang", "changeStatus: downloadFile");
                volumeFile.setStatus(VolumeFile.STATUS_LOADING);
                VolumeFileDownloadManager.getInstance().reDownloadFile(volumeFile, volumeFile.getVolumeFileAbsolutePath());
                VolumeFileDownloadCacheUtils.saveVolumeFile(volumeFile);
            }
        }
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void refreshView(List<VolumeFile> fileList);

        void onStatusChange(List<VolumeFile> fileList);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.volume_file_transfer_item_img)
        ImageView icon;
        @BindView(R.id.volume_file_transfer_item_name)
        TextView nameTv;
        @BindView(R.id.volume_file_transfer_item_desc)
        TextView descTv;
        @BindView(R.id.volume_file_transfer_item_speed)
        TextView speedTv;
        @BindView(R.id.volume_file_transfer_item_status)
        View statusLayout;
        @BindView(R.id.volume_file_transfer_item_progressBar)
        CircleProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            descTv.setOnClickListener(this);
            statusLayout.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            if (ClickRuleUtil.isFastClick()) {
                return;
            }
            switch (v.getId()) {
                case R.id.volume_file_transfer_item_status:
                    changeStatus(position);
                    notifyItemChanged(position);
                    if (callBack != null) {
                        callBack.onStatusChange(unfinishedFileList);
                    }
                    break;
                case R.id.volume_file_transfer_item_desc:
                    handleDescFail(position);
                    notifyItemChanged(position);
                    if (callBack != null) {
                        callBack.onStatusChange(unfinishedFileList);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final int position = (int) v.getTag();
            final VolumeFile recordVolumeFile = unfinishedFileList.get(position);
            new CustomDialog.MessageDialogBuilder(context)
                    .setMessage(R.string.clouddriver_sure_delete_file)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!unfinishedFileList.contains(recordVolumeFile)) {    //下载完成可能刷新列表导致位置不对
                                dialog.dismiss();
                                return;
                            }
                            try {
                                VolumeFile volumeFile = unfinishedFileList.get(position);
                                if (type.equals(Constant.TYPE_UPLOAD)) {
                                    VolumeFileUploadManager.getInstance().cancelVolumeFileUploadService(volumeFile);
                                    VolumeFileUploadManager.getInstance().deleteUploadInfo(volumeFile);
                                } else if (type.equals(Constant.TYPE_DOWNLOAD)) {
                                    VolumeFileDownloadManager.getInstance().cancelDownloadVolumeFile(volumeFile);
                                    VolumeFileDownloadManager.getInstance().deleteDownloadInfo(volumeFile);
                                    VolumeFileDownloadCacheUtils.deleteVolumeFile(volumeFile);
                                    FileDownloadManager.getInstance().deleteDownloadFile(volumeFile.getLocalFilePath());
                                    if (!StringUtils.isBlank(volumeFile.getLocalFilePath())) {
                                        String tmpFilePath = volumeFile.getLocalFilePath() + ".tmp";
                                        File localTmpDownloadFile = new File(tmpFilePath);
                                        if (localTmpDownloadFile.exists()) {
                                            localTmpDownloadFile.delete();
                                        }
                                    }
                                }
                                unfinishedFileList.remove(position);
                                notifyDataSetChanged();
                                LoadObservable.getInstance().notifyDateChange();
                                if (callBack != null) {
                                    callBack.refreshView(unfinishedFileList);
                                }
                                if (callBack != null) {
                                    callBack.onStatusChange(unfinishedFileList);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                dialog.dismiss();
                            }
                        }
                    })
                    .show();
            return false;
        }
    }
}
