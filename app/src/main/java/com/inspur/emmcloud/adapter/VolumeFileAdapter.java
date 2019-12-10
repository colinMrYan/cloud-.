package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 云盘文件展示Adapter
 */

public class VolumeFileAdapter extends RecyclerView.Adapter<VolumeFileAdapter.ViewHolder> {

    public List<VolumeFile> selectVolumeFileList = new ArrayList<>();
    private Context context;
    private List<VolumeFile> volumeFileList;
    private MyItemClickListener mItemClickListener;
    private boolean isMultiselect = false;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private boolean isShowFileOperationDropDownImg = true;
    private boolean isShowFileOperationSelecteImage = true;
    private String currentDirAbsolutePath;
    public VolumeFileAdapter(Context context, List<VolumeFile> volumeFileList) {
        this.context = context;
        this.volumeFileList = volumeFileList;
    }

    public void setCurrentDirAbsolutePath(String currentDirAbsolutePath) {
        this.currentDirAbsolutePath = currentDirAbsolutePath;
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
     * 设置是否显示右侧选择按钮
     *
     * @param isShowFileOperationSelecteImage
     */
    public void setShowFileOperationSelcteImage(boolean isShowFileOperationSelecteImage) {
        this.isShowFileOperationSelecteImage = isShowFileOperationSelecteImage;
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

    public void setSelectVolumeFileList(List<VolumeFile> selectVolumeFileList) {
        this.selectVolumeFileList = selectVolumeFileList;
        notifyDataSetChanged();
    }

    public void clearSelectedVolumeFileList() {
        selectVolumeFileList.clear();
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
        ViewHolder holder = new ViewHolder(view, mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final VolumeFile volumeFile = volumeFileList.get(position);
        String volumeFileStatus = volumeFile.getStatus();
        boolean isStatusNomal = true;
        holder.fileInfoLayout.setVisibility(isStatusNomal ? View.VISIBLE : View.GONE);
        holder.fileOperationDropDownImg.setVisibility(View.GONE);
        holder.fileSelcetImg.setVisibility(isShowFileOperationSelecteImage && isStatusNomal ? View.VISIBLE : View.GONE);
        if (selectVolumeFileList.size() > 0) {
            holder.fileSelcetImg.setImageResource(selectVolumeFileList.contains(volumeFile) ? R.drawable.ic_select_yes : R.drawable.ic_select_no);
        } else {
            holder.fileSelcetImg.setImageResource(R.drawable.ic_volume_no_selected);
        }
        showVolumeFileTypeImg(holder.fileTypeImg, volumeFile);
        holder.fileNameText.setText(volumeFile.getName());
        if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
            holder.fileSizeText.setVisibility(View.INVISIBLE);
        } else {
            holder.fileSizeText.setVisibility(View.VISIBLE);
            holder.fileSizeText.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        }
        String fileTime = TimeUtils.getTime(volumeFile.getLastUpdate(), format);
        holder.fileTimeText.setText(fileTime);
//        if (!isStatusNomal) {
//            LogUtils.jasonDebug("volumeFileStatus==" + volumeFileStatus);
//            boolean isStutasUploading = volumeFileStatus.equals(VolumeFile.STATUS_UPLOAD_IND);
//            holder.uploadOperationText.setText(isStutasUploading ? R.string.upload_cancel : R.string.clouddriver_upload_again);
//            holder.uploadProgressBar.setProgress(0);
//            holder.uploadProgressBar.setVisibility(View.GONE);
//            holder.uploadStatusText.setVisibility(View.VISIBLE);
//            holder.uploadStatusText.setText(isStutasUploading ? R.string.clouddriver_upload_waiting : R.string.clouddriver_upload_fail);
//            if (volumeFileStatus.equals(VolumeFile.STATUS_UPLOAD_IND)) {
//                VolumeFileUploadManager.getInstance().setBusinessProgressCallback(volumeFile, new ProgressCallback() {
//                    @Override
//                    public void onSuccess(VolumeFile newVolumeFile) {
////                        replaceVolumeFileData(volumeFile, newVolumeFile);
//                    }
//
//                    @Override
//                    public void onLoading(int progress, String speed) {
//                        holder.uploadProgressBar.setVisibility(View.VISIBLE);
//                        holder.uploadStatusText.setVisibility(View.GONE);
//                        holder.uploadProgressBar.setProgress(progress);
//                    }
//
//                    @Override
//                    public void onFail() {
//                        volumeFile.setStatus(VolumeFile.STATUS_UPLOAD_FAIL);
//                        notifyItemChanged(position);
//                    }
//                });
//            }
//
//        }
    }

    private void showVolumeFileTypeImg(ImageView imageView, VolumeFile volumeFile) {
        Integer fileIconResId = null;
        imageView.setTag("");
        if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
            fileIconResId = R.drawable.baselib_file_type_folder;
        } else {
            if (volumeFile.getFormat().startsWith("image/")) {
                String url = "";
//                if (volumeFile.getStatus().equals(VolumeFile.STATUS_LOADING) ) {
//                    url = volumeFile.getLocalFilePath();
//                } else {
                String path = currentDirAbsolutePath + volumeFile.getName();
                url = APIUri.getVolumeFileTypeImgThumbnailUrl(volumeFile, path);
//                }
                imageView.setTag(url);
                ImageDisplayUtils.getInstance().displayImageByTag(imageView, url, R.drawable.baselib_file_type_img);
                return;
            } else {
                fileIconResId = FileUtils.getFileIconResIdByFormat(volumeFile.getFormat());
            }

        }
        imageView.setImageResource(fileIconResId);
    }

    @Override
    public int getItemCount() {
        return volumeFileList.size();
    }

    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }


    public interface MyItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onItemDropDownImgClick(View view, int position);
        void onSelectedItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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
        @BindView(R.id.file_info_layout)
        RelativeLayout fileInfoLayout;
        @BindView(R.id.file_operation_drop_down_img)
        ImageView fileOperationDropDownImg;
        private MyItemClickListener myItemClickListener;

        public ViewHolder(View itemView, MyItemClickListener myItemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.myItemClickListener = myItemClickListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            fileOperationDropDownImg.setOnClickListener(this);
            fileSelcetImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (myItemClickListener != null) {
                if (v.getId() == R.id.file_operation_drop_down_img) {
                    myItemClickListener.onItemDropDownImgClick(v, getAdapterPosition());
                } else if (v.getId() == R.id.file_select_img) {
                    myItemClickListener.onSelectedItemClick(v, getAdapterPosition());
                } else {
                    myItemClickListener.onItemClick(v, getAdapterPosition());
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            // myItemClickListener.onItemLongClick(v, getAdapterPosition());
            myItemClickListener.onSelectedItemClick(v, getAdapterPosition());
            return false;
        }
    }
}
