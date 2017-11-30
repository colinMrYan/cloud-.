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
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.callback.ProgressCallback;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.VolumeFileIconUtils;
import com.inspur.emmcloud.util.VolumeFileUploadUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenmch on 2017/11/16.
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

    public void setShowFileOperationDropDownImg(boolean isShowFileOperationDropDownImg) {
        this.isShowFileOperationDropDownImg = isShowFileOperationDropDownImg;
    }

    public void setMultiselect(boolean isMultiselect) {
        this.isMultiselect = isMultiselect;
        if (!isMultiselect) {
            selectVolumeFileList.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 设置是否全选
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

    public List<VolumeFile> getSelectVolumeFileList() {
        return selectVolumeFileList;
    }

    public boolean getMultiselect() {
        return isMultiselect;
    }

    public void setVolumeFileSelect(int position) {
        VolumeFile volumeFile = volumeFileList.get(position);
        if (selectVolumeFileList.contains(volumeFile)) {
            selectVolumeFileList.remove(volumeFile);
        } else {
            selectVolumeFileList.add(volumeFile);
        }
        notifyDataSetChanged();
    }

    public void replaceVolumeFile(VolumeFile oldVolumeFile, VolumeFile newVolumeFile) {
        int position = volumeFileList.indexOf(oldVolumeFile);
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
        boolean isStatusNomal = volumeFile.getStatus().equals("normal");
        holder.uploadProgressBar.setVisibility(isStatusNomal ? View.GONE : View.VISIBLE);
        holder.uploadCancelText.setVisibility(isStatusNomal ? View.GONE : View.VISIBLE);
        holder.fileInfoLayout.setVisibility(isStatusNomal ? View.VISIBLE : View.GONE);
        holder.fileOperationDropDownImg.setVisibility((isStatusNomal && isShowFileOperationDropDownImg) ? View.VISIBLE : View.GONE);
        if (isMultiselect && isStatusNomal) {
            holder.fileSelcetImg.setVisibility(View.VISIBLE);
            holder.fileSelcetImg.setImageResource(selectVolumeFileList.contains(volumeFile) ? R.drawable.ic_volume_file_selct_yes : R.drawable.ic_volume_file_selct_no);
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
            VolumeFileUploadUtils.getInstance().setOssUploadProgressCallback(volumeFile, new ProgressCallback() {
                @Override
                public void onSuccess(VolumeFile newVolumeFile) {
                    replaceVolumeFile(volumeFile, newVolumeFile);
                }

                @Override
                public void onLoading(int progress) {
                    holder.uploadProgressBar.setProgress(progress);
                }

                @Override
                public void onFail() {
                    holder.uploadCancelText.setText("上传失败");
                }
            });
            holder.uploadCancelText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VolumeFileUploadUtils.getInstance().removeOssService(volumeFile.getId());
                    volumeFileList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return volumeFileList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyItemClickListener myItemClickListener;
        private MyItemDropDownImgClickListener myItemDropDownImgClickListener;

        @ViewInject(R.id.file_type_img)
        private ImageView fileTypeImg;

        @ViewInject(R.id.file_name_text)
        private TextView fileNameText;

        @ViewInject(R.id.file_time_text)
        private TextView fileTimeText;

        @ViewInject(R.id.file_size_text)
        private TextView fileSizeText;

        @ViewInject(R.id.file_select_img)
        private ImageView fileSelcetImg;

        @ViewInject(R.id.upload_cancel_text)
        private TextView uploadCancelText;

        @ViewInject(R.id.upload_progress)
        private ProgressBar uploadProgressBar;

        @ViewInject(R.id.file_info_layout)
        private RelativeLayout fileInfoLayout;

        @ViewInject(R.id.file_operation_drop_down_img)
        private ImageView fileOperationDropDownImg;

        public ViewHolder(View itemView, MyItemClickListener myItemClickListener, MyItemDropDownImgClickListener myItemDropDownImgClickListener) {
            super(itemView);
            x.view().inject(this, itemView);
            this.myItemClickListener = myItemClickListener;
            this.myItemDropDownImgClickListener = myItemDropDownImgClickListener;
            itemView.setOnClickListener(this);
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

    }

    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }

    public void setItemDropDownImgClickListener(MyItemDropDownImgClickListener myItemDropDownImgClickListener) {
        this.myItemDropDownImgClickListener = myItemDropDownImgClickListener;
    }

    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface MyItemDropDownImgClickListener {
        void onItemDropDownImgClick(View view, int position);
    }
}
