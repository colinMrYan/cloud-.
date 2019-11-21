package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
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
    private List<VolumeFile> fileList = new ArrayList<>();
    private String type;    //上传 or 下载
    private CallBack callBack;

    public VolumeFileTransferAdapter(Context context, List<VolumeFile> fileList, String type) {
        this.context = context;
        this.fileList = fileList;
        this.type = type;
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
        if (fileList == null || fileList.size() == 0) {
            return;
        }
        VolumeFile volumeFile = fileList.get(position);
        holder.itemView.setTag(position);
        //显示图标
        showVolumeFileTypeImg(holder.icon, volumeFile);
        holder.nameTv.setText(volumeFile.getName());
        showVolumeFileDesc(holder.descTv, volumeFile);
        holder.progressBar.setProgress(volumeFile.getProgress());
        showVolumeFileSpeed(holder, position);
        holder.statusLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return fileList == null ? 0 : fileList.size();
    }

    private void showVolumeFileTypeImg(ImageView imageView, VolumeFile volumeFile) {
        Integer fileIconResId = null;
        imageView.setTag("");

        if (volumeFile.getFormat().startsWith("image/")) {
            String url = "";
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND)) {
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
     * @param descTv
     * @param volumeFile
     */
    private void showVolumeFileDesc(TextView descTv, VolumeFile volumeFile) {
        if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_PAUSE) || volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_PAUSE)) {
            descTv.setText(R.string.volume_file_transfer_list_pause_tip);
        } else {
            descTv.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        }
    }

    private void showVolumeFileSpeed(final ViewHolder holder, final int position) {
        if (fileList == null || fileList.size() == 0) {
            return;
        }
        final VolumeFile volumeFile = fileList.get(position);
        holder.progressBar.setStatus(volumeFile.transfer2ProgressStatus(volumeFile.getStatus()));
        if (volumeFile.getProgress() == -1) {
            holder.progressBar.setProgress(0);
        } else {
            holder.progressBar.setProgress(volumeFile.getProgress());
        }
        holder.speedTv.setVisibility(volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND) ||
                volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_IND) ? View.VISIBLE : View.GONE);
        if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND) ||
                volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_PAUSE)) {
            handleUploadCallback(holder, volumeFile);
        } else if (volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_IND) ||
                volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_PAUSE)) {
            handleDownloadCallback(holder, volumeFile);
        } else {
            holder.speedTv.setVisibility(View.GONE);
        }
    }

    /**
     * 监听上传回调
     */
    private void handleUploadCallback(final ViewHolder holder, final VolumeFile originVolumeFile) {
        VolumeFileUploadManager.getInstance().setBusinessProgressCallback(originVolumeFile, new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                Log.d("zhang", "handleUploadCallback onSuccess: ");
                holder.progressBar.setStatus(CircleProgressBar.Status.End);
                fileList.remove(originVolumeFile);
                notifyDataSetChanged();
                if (callBack != null) {
                    callBack.refreshView(fileList);
                }
            }

            @Override
            public void onLoading(int progress, String speed) {
                if (originVolumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND)) {
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
                holder.progressBar.setStatus(CircleProgressBar.Status.End);
            }
        });
    }

    /**
     * 监听下载回调
     */
    private void handleDownloadCallback(final ViewHolder holder, final VolumeFile volumeFile) {
        VolumeFileDownloadManager.getInstance().setBusinessProgressCallback(volumeFile, new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                Log.d("zhang", "handleDownloadCallback onSuccess: ");
                holder.progressBar.setStatus(CircleProgressBar.Status.End);
                fileList.remove(volumeFile);
                notifyDataSetChanged();
                if (callBack != null) {
                    callBack.refreshView(fileList);
                }
            }

            @Override
            public void onLoading(int progress, String speed) {
//                Log.d("zhang", "downLoading: progress = " + progress
//                        + ",speed = " + speed + ",status = " + volumeFile.getStatus());
                if (volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_IND)) {
                    if (!StringUtils.isBlank(speed)) {
                        holder.speedTv.setText(speed);
                    }
                }
                volumeFile.setProgress(progress);
                if (progress > 0) {
                    holder.progressBar.setProgress(progress);
                }
            }

            @Override
            public void onFail() {
                holder.progressBar.setStatus(CircleProgressBar.Status.End);
            }
        });
    }

    public interface MyItemOnClickListener {
        void onItemClick(View view, int position);
    }

    private void changeStatus(int position) {
        VolumeFile volumeFile = fileList.get(position);
        String status = volumeFile.getStatus();
        if (status.equals(VolumeFile.STATUS_UPLOAD_IND)) {
            //暂停上传
            fileList.get(position).setStatus(VolumeFile.STATUS_UPLOAD_PAUSE);
            VolumeFileUploadManager.getInstance().pauseVolumeFileUploadService(volumeFile);
        } else if (status.equals(VolumeFile.STATUS_UPLOAD_PAUSE) || status.equals(VolumeFile.STATUS_UPLOAD_FAIL)) {
            //开始上传
            fileList.get(position).setStatus(VolumeFile.STATUS_UPLOAD_IND);
            VolumeFileUploadManager.getInstance().reUploadFile(volumeFile);
        } else if (status.equals(VolumeFile.STATUS_DOWNLOAD_IND)) {
            //暂停下载
            Log.d("zhang", "changeStatus: STATUS_DOWNLOAD_PAUSE");
            fileList.get(position).setStatus(VolumeFile.STATUS_DOWNLOAD_PAUSE);
            VolumeFileDownloadManager.getInstance().cancelDownloadVolumeFile(volumeFile);
            VolumeFileDownloadCacheUtils.saveVolumeFile(volumeFile);
        } else if (status.equals(VolumeFile.STATUS_DOWNLOAD_PAUSE)) {
            //继续下载
            fileList.get(position).setStatus(VolumeFile.STATUS_DOWNLOAD_IND);
            Log.d("zhang", "changeStatus: downloadFile");
            volumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_IND);
            VolumeFileDownloadManager.getInstance().reDownloadFile(volumeFile, volumeFile.getVolumeFileAbsolutePath());
            VolumeFileDownloadCacheUtils.saveVolumeFile(volumeFile);
        } else if (status.equals(VolumeFile.STATUS_NORMAL)) {

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
                        callBack.onStatusChange(fileList);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final int position = (int) v.getTag();
            final VolumeFile recordVolumeFile = fileList.get(position);
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
                            if (!fileList.contains(recordVolumeFile)) {    //下载完成可能刷新列表导致位置不对
                                dialog.dismiss();
                                return;
                            }
                            try {
                                VolumeFile volumeFile = fileList.get(position);
                                if (type.equals(Constant.TYPE_UPLOAD)) {
                                    VolumeFileUploadManager.getInstance().cancelVolumeFileUploadService(volumeFile);
                                } else if (type.equals(Constant.TYPE_DOWNLOAD)) {
                                    VolumeFileDownloadManager.getInstance().cancelDownloadVolumeFile(volumeFile);
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
                                fileList.remove(position);
                                notifyDataSetChanged();
                                if (callBack != null) {
                                    callBack.refreshView(fileList);
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
