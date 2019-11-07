package com.inspur.emmcloud.adapter;

import android.content.Context;
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
import com.inspur.emmcloud.baselib.widget.progressbar.CircleProgressBar;
import com.inspur.emmcloud.basemodule.util.ClickRuleUtil;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;

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

    public VolumeFileTransferAdapter(Context context, List<VolumeFile> fileList) {
        this.context = context;
        this.fileList = fileList;
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
        VolumeFile volumeFile = fileList.get(position);
        holder.itemView.setTag(position);
        //显示图标
        showVolumeFileTypeImg(holder.icon, volumeFile);
        holder.nameTv.setText(volumeFile.getName());
        showVolumeFileDesc(holder.descTv, volumeFile);
        holder.progressBar.setProgress(volumeFile.getProgress());
        showVolumeFileSpeed(holder, volumeFile);
        holder.statusLayout.setOnClickListener(new OnClickListener(position));
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
            descTv.setText(R.string.volume_file_transfer_list_wait_tip);
        } else {
            descTv.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        }
    }

    private void showVolumeFileSpeed(final ViewHolder holder, final VolumeFile volumeFile) {
        holder.progressBar.setStatus(volumeFile.transfer2ProgressStatus(volumeFile.getStatus()));
        if (volumeFile.getProgress() == -1) {
            holder.progressBar.setProgress(0);
        } else {
            holder.progressBar.setProgress(volumeFile.getProgress());
        }
        if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND)) {
            holder.speedTv.setVisibility(View.VISIBLE);
            holder.speedTv.setText("10 K/S");
            Log.d("zhang", "showVolumeFileSpeed: 10K/S");
            VolumeFileUploadManager.getInstance().setBusinessProgressCallback(volumeFile, new ProgressCallback() {
                @Override
                public void onSuccess(VolumeFile volumeFile) {
                    holder.progressBar.setStatus(CircleProgressBar.Status.End);
                }

                @Override
                public void onLoading(int progress, String uploadSpeed) {
                    if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOAD_IND)) {
                        if (!StringUtils.isBlank(uploadSpeed)) {
                            holder.speedTv.setText(uploadSpeed);
                        }
                    }
                    Log.d("zhang", "onLoading: progress = " + progress
                            + ",uploadSpeed = " + uploadSpeed + ",status = " + volumeFile.getStatus());
                    if (progress > 0) {
                        holder.progressBar.setProgress(progress);
                    }
                }

                @Override
                public void onFail() {
                    holder.progressBar.setStatus(CircleProgressBar.Status.End);
                }
            });
        } else {
            holder.speedTv.setVisibility(View.GONE);
        }
    }

    public interface MyItemOnClickListener {
        void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
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
        }
    }

    class OnClickListener implements View.OnClickListener {
        private int position;

        public OnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (ClickRuleUtil.isFastClick()) {
                return;
            }
            switch (v.getId()) {
                case R.id.volume_file_transfer_item_status:
                    changeStatus();
                    notifyItemChanged(position);
                    break;
                default:
                    break;
            }
        }

        private void changeStatus() {
            VolumeFile volumeFile = fileList.get(position);
            String status = volumeFile.getStatus();
            if (status.equals(VolumeFile.STATUS_UPLOAD_IND)) {
                //暂停上传
                fileList.get(position).setStatus(VolumeFile.STATUS_UPLOAD_PAUSE);
                VolumeFileUploadManager.getInstance().pauseVolumeFileUploadService(volumeFile);
            } else if (status.equals(VolumeFile.STATUS_UPLOAD_PAUSE)) {
                //开始上传
                fileList.get(position).setStatus(VolumeFile.STATUS_UPLOAD_IND);
                VolumeFileUploadManager.getInstance().reUploadFile(volumeFile);
            } else if (status.equals(VolumeFile.STATUS_DOWNLOAD_IND)) {
                //点击下载中
                fileList.get(position).setStatus(VolumeFile.STATUS_DOWNLOAD_PAUSE);
            } else if (status.equals(VolumeFile.STATUS_NORMAL)) {

            }
        }
    }
}
