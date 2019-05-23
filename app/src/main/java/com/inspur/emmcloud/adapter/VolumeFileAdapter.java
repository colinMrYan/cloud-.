package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.VolumeFileIconUtils;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManagerUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 云盘文件展示Adapter
 */

public class VolumeFileAdapter extends RecyclerView.Adapter<VolumeFileAdapter.ViewHolder> {

    private Context context;
    private List<VolumeFile> volumeFileList;
    private MyItemClickListener mItemClickListener;
    private MyItemDropDownImgClickListener myItemDropDownImgClickListener;
    private boolean isMultiselect = false;
    private List<VolumeFile> selectVolumeFileList = new ArrayList<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private boolean isShowFileOperationDropDownImg = true;

    public VolumeFileAdapter(Context context, List<VolumeFile> volumeFileList) {
        this.context = context;
        this.volumeFileList = volumeFileList;
    }

    public void setVolumeFileList(List<VolumeFile> volumeFileList) {
        this.volumeFileList = volumeFileList;
    }

    /**
     * 设置是否显示右侧箭头
     *
     * @param isShowFileOperationDropDownImg
     */
    public void setShowFileOperationDropDownImg(boolean isShowFileOperationDropDownImg) {
        this.isShowFileOperationDropDownImg = isShowFileOperationDropDownImg;
    }

    /**
     * 选中所有的文件
     *
     * @param isSelectAll
     */
    public void setSelectAll(boolean isSelectAll) {
        selectVolumeFileList.clear();
        if (isSelectAll) {
            for (int i = 0; i < volumeFileList.size(); i++) {
                VolumeFile volumeFile = volumeFileList.get(i);
                if (volumeFile.getStatus().equals("normal")) {
                    selectVolumeFileList.add(volumeFile);
                }
            }

        }
        notifyDataSetChanged();
    }

    /**
     * 获取被选中的文件列表
     *
     * @return
     */
    public List<VolumeFile> getSelectVolumeFileList() {
        return selectVolumeFileList;
    }

    /**
     * 判断是否处于多选状态
     *
     * @return
     */
    public boolean getMultiselect() {
        return isMultiselect;
    }

    /**
     * 设置是否全选
     *
     * @param isMultiselect
     */
    public void setMultiselect(boolean isMultiselect) {
        this.isMultiselect = isMultiselect;
        if (!isMultiselect) {
            selectVolumeFileList.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 设置此位置文件的选中状态
     *
     * @param position
     */
    public void setVolumeFileSelect(int position) {
        VolumeFile volumeFile = volumeFileList.get(position);
        if (selectVolumeFileList.contains(volumeFile)) {
            selectVolumeFileList.remove(volumeFile);
        } else {
            selectVolumeFileList.add(volumeFile);
        }
        notifyDataSetChanged();
    }

    /**
     * 文件上传成功之后，服务端返回的VolumeFile数据替换用于显示的客户端构造的数据
     *
     * @param mockVolumeFile
     * @param newVolumeFile
     */
    public void replaceVolumeFileData(VolumeFile mockVolumeFile, VolumeFile newVolumeFile) {
        int position = volumeFileList.indexOf(mockVolumeFile);
        if (position != -1) {
            volumeFileList.remove(position);
            volumeFileList.add(position, newVolumeFile);
            notifyItemChanged(position);
        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_volume_file_item_view, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener, myItemDropDownImgClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final VolumeFile volumeFile = volumeFileList.get(position);
        String volumeFileStatus = volumeFile.getStatus();
        boolean isStatusNomal = volumeFileStatus.equals("normal");
        holder.fileUploadStatusLayout.setVisibility(isStatusNomal ? View.GONE : View.VISIBLE);
        holder.uploadOperationText.setVisibility(isStatusNomal ? View.GONE : View.VISIBLE);
        holder.fileInfoLayout.setVisibility(isStatusNomal ? View.VISIBLE : View.GONE);
        holder.fileOperationDropDownImg.setVisibility((isStatusNomal && isShowFileOperationDropDownImg) ? View.VISIBLE : View.GONE);
        if (isMultiselect && isStatusNomal) {
            holder.fileSelcetImg.setVisibility(View.VISIBLE);
            holder.fileSelcetImg.setImageResource(selectVolumeFileList.contains(volumeFile) ? R.drawable.ic_select_yes : R.drawable.ic_select_no);
        } else {
            holder.fileSelcetImg.setVisibility(View.GONE);
        }
        int fileTypeImgResId = VolumeFileIconUtils.getIconResId(volumeFile);
        holder.fileTypeImg.setImageResource(fileTypeImgResId);
        holder.fileNameText.setText(volumeFile.getName());
        holder.fileSizeText.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        String fileTime = TimeUtils.getTime(volumeFile.getCreationDate(), format);
        holder.fileTimeText.setText(fileTime);
        if (!isStatusNomal) {
            boolean isStutasUploading = volumeFileStatus.equals(VolumeFile.STATUS_UPLOADIND);
            holder.uploadOperationText.setText(isStutasUploading ? R.string.upload_cancel : R.string.clouddriver_upload_again);
            holder.uploadProgressBar.setProgress(0);
            holder.uploadProgressBar.setVisibility(View.GONE);
            holder.uploadStatusText.setVisibility(View.VISIBLE);
            holder.uploadStatusText.setText(isStutasUploading ? R.string.clouddriver_upload_waiting : R.string.clouddriver_upload_fail);
            VolumeFileUploadManagerUtils.getInstance().setOssUploadProgressCallback(volumeFile, new ProgressCallback() {
                @Override
                public void onSuccess(VolumeFile newVolumeFile) {
                    replaceVolumeFileData(volumeFile, newVolumeFile);
                }

                @Override
                public void onLoading(int progress) {
                    holder.uploadProgressBar.setVisibility(View.VISIBLE);
                    holder.uploadStatusText.setVisibility(View.GONE);
                    holder.uploadProgressBar.setProgress(progress);
                }

                @Override
                public void onFail() {
                    volumeFile.setStatus(VolumeFile.STATUS_UPLOADIND_FAIL);
                    notifyItemChanged(position);
                }
            });
            holder.uploadOperationText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOADIND)) {
                        //取消上传
                        VolumeFileUploadManagerUtils.getInstance().removeVolumeFileUploadService(volumeFile);
                        volumeFileList.remove(position);
                        notifyItemRemoved(position);
                    } else if (NetUtils.isNetworkConnected(context)) {
                        //重新上传
                        volumeFile.setStatus(VolumeFile.STATUS_UPLOADIND);
                        VolumeFileUploadManagerUtils.getInstance().reUploadFile(volumeFile);
                        notifyItemChanged(position);
                    }


                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return volumeFileList.size();
    }

    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }

    public void setItemDropDownImgClickListener(MyItemDropDownImgClickListener myItemDropDownImgClickListener) {
        this.myItemDropDownImgClickListener = myItemDropDownImgClickListener;
    }


    public interface MyItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public interface MyItemDropDownImgClickListener {
        void onItemDropDownImgClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private MyItemClickListener myItemClickListener;
        private MyItemDropDownImgClickListener myItemDropDownImgClickListener;

        @BindView(R.id.file_type_img)
        ImageView fileTypeImg;

        @BindView(R.id.tv_file_name)
        TextView fileNameText;

        @BindView(R.id.file_time_text)
        TextView fileTimeText;

        @BindView(R.id.tv_file_size)
        TextView fileSizeText;

        @BindView(R.id.file_select_img)
        ImageView fileSelcetImg;

        @BindView(R.id.upload_cancel_text)
        TextView uploadOperationText;

        @BindView(R.id.upload_progress)
        ProgressBar uploadProgressBar;

        @BindView(R.id.upload_status_text)
        TextView uploadStatusText;

        @BindView(R.id.file_info_layout)
        RelativeLayout fileInfoLayout;

        @BindView(R.id.file_upload_status_layout)
        RelativeLayout fileUploadStatusLayout;

        @BindView(R.id.file_operation_drop_down_img)
        ImageView fileOperationDropDownImg;

        public ViewHolder(View itemView, MyItemClickListener myItemClickListener, MyItemDropDownImgClickListener myItemDropDownImgClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.myItemClickListener = myItemClickListener;
            this.myItemDropDownImgClickListener = myItemDropDownImgClickListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            fileOperationDropDownImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.file_operation_drop_down_img) {
                if (myItemDropDownImgClickListener != null) {
                    myItemDropDownImgClickListener.onItemDropDownImgClick(v, getAdapterPosition());
                }

            } else if (myItemClickListener != null) {
                myItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            myItemClickListener.onItemLongClick(v, getAdapterPosition());
            return false;
        }
    }
}
