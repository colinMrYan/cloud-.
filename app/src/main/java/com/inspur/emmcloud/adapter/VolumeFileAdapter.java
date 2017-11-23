package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.VolumeFileIconUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
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

    public VolumeFileAdapter(Context context, List<VolumeFile> volumeFileList){
        this.context = context;
        this.volumeFileList = volumeFileList;
    }

    public void setVolumeFileList(List<VolumeFile> volumeFileList){
        this.volumeFileList= volumeFileList;
    }

    public void setMultiselect(boolean isMultiselect ){
        this.isMultiselect = isMultiselect;
        LogUtils.jasonDebug("isMultiselect="+isMultiselect);
        notifyDataSetChanged();
    }

    public boolean getMultiselect(){
        return  isMultiselect;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_volume_file_item_view, parent, false);
        ViewHolder holder = new ViewHolder(view, mItemClickListener,myItemDropDownImgClickListener);
        x.view().inject(holder,view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VolumeFile volumeFile = volumeFileList.get(position);
        holder.fileSelcetImg.setVisibility(isMultiselect?View.VISIBLE:View.GONE);
        int fileTypeImgResId = VolumeFileIconUtils.getIconResId(volumeFile);
        holder.fileTypeImg.setImageResource(fileTypeImgResId);
        holder.fileNameText.setText(volumeFile.getName());
        holder.fileSizeText.setText(FileUtils.formatFileSize(volumeFile.getSize()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String fileTime = TimeUtils.getTime(volumeFile.getCreationDate(),format);
        holder.fileTimeText.setText(fileTime);
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

        public ViewHolder(View itemView,MyItemClickListener myItemClickListener,MyItemDropDownImgClickListener myItemDropDownImgClickListener) {
            super(itemView);
            this.myItemClickListener = myItemClickListener;
            this.myItemDropDownImgClickListener = myItemDropDownImgClickListener;
            itemView.setOnClickListener(this);
            (itemView.findViewById(R.id.file_operation_drop_down_img)).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.file_operation_drop_down_img ){
                if (myItemDropDownImgClickListener != null){
                    myItemDropDownImgClickListener.onItemDropDownImgClick(v,getAdapterPosition());
                }

            }else if (myItemClickListener != null) {
                myItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
    public void setItemDropDownImgClickListener(MyItemDropDownImgClickListener myItemDropDownImgClickListener){
        this.myItemDropDownImgClickListener = myItemDropDownImgClickListener;
    }

    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface  MyItemDropDownImgClickListener{
        void onItemDropDownImgClick(View view, int position);
    }
}
