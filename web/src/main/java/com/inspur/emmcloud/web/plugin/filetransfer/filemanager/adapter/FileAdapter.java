package com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.FileUtil;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.base.RecyclerViewAdapter;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.FileBean;
import com.inspur.emmcloud.web.plugin.filetransfer.filemanager.bean.FileType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${zhaoyanjun} on 2017/1/11.
 */

public class FileAdapter extends RecyclerViewAdapter {

    private Context context;
    private List<FileBean> list;
    private LayoutInflater mLayoutInflater;
    private List<FileBean> selectFileBeanList = new ArrayList<>();
    private int maximum;

    public FileAdapter(Context context, List<FileBean> list, List<FileBean> selectFileBeanList, int maximum) {
        this.context = context;
        this.list = list;
        this.maximum = maximum;
        mLayoutInflater = LayoutInflater.from(context);
        this.selectFileBeanList = selectFileBeanList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = mLayoutInflater.inflate(R.layout.web_filemanager_list_item, parent, false);
        return new FileHolder(view);
    }

    @Override
    public void onBindViewHolders(RecyclerView.ViewHolder holder, int position) {
        FileHolder fileHolder = (FileHolder) holder;
        FileBean fileBean = list.get(position);
        fileHolder.fileName.setText(fileBean.getName());
        FileType fileType = fileBean.getFileType();
        if (fileType == FileType.directory) {
            fileHolder.dir_enter_image.setVisibility(View.VISIBLE);
            fileHolder.fileChildCount.setVisibility(View.VISIBLE);
            fileHolder.fileChildCount.setText(context.getString(R.string.file) + ": " + fileBean.getChildCount());

            fileHolder.fileSize.setVisibility(View.GONE);
            fileHolder.dir_enter_image.setImageResource(R.drawable.ic_arrow_right);

        } else {
            fileHolder.fileChildCount.setVisibility(View.GONE);
            fileHolder.fileSize.setVisibility(View.VISIBLE);
            fileHolder.fileSize.setText(FileUtil.sizeToChange(fileBean.getSize()));
            fileHolder.dir_enter_image.setVisibility(maximum == 1 ? View.INVISIBLE : View.VISIBLE);
            boolean isSelected = selectFileBeanList.contains(fileBean);
            fileHolder.dir_enter_image.setImageResource(isSelected ? R.drawable.ic_select_yes : R.drawable.ic_select_no);
        }

        //设置图标
        if (fileType == FileType.directory) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_forder);
        } else if (fileType == FileType.music) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_audio);
        } else if (fileType == FileType.video) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_video);
        } else if (fileType == FileType.txt) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_txt);
        } else if (fileType == FileType.zip) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_zip);
        } else if (fileType == FileType.image) {
            ImageDisplayUtils.getInstance().displayImage(fileHolder.fileIcon, fileBean.getPath());
        } else if (fileType == FileType.word) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_word);
        } else if (fileType == FileType.excel) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_excel);
        } else if (fileType == FileType.ppt) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_ppt);
        } else if (fileType == FileType.pdf) {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_pdf);
        } else {
            fileHolder.fileIcon.setImageResource(R.drawable.ic_volume_file_typ_unknown);
        }
    }

    @Override
    public Object getAdapterData() {
        return list;
    }

    @Override
    public Object getItem(int positon) {
        return list.get(positon);
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getHolderType();
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public void refresh(List<FileBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public class FileHolder extends RecyclerView.ViewHolder {

        public ImageView fileIcon;
        public TextView fileName;
        public TextView fileChildCount;
        public TextView fileSize;
        public ImageView dir_enter_image;

        public FileHolder(View view) {
            super(view);
            fileIcon = (ImageView) view.findViewById(R.id.iv_file_icon);
            fileName = (TextView) view.findViewById(R.id.tv_file_name);
            fileChildCount = (TextView) view.findViewById(R.id.tv_file_child_count);
            fileSize = (TextView) view.findViewById(R.id.tv_file_size);
            dir_enter_image = (ImageView) view.findViewById(R.id.iv_dir_enter);
        }
    }
}