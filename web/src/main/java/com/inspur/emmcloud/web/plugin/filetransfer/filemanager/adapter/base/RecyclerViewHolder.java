package com.inspur.emmcloud.web.plugin.filetransfer.filemanager.adapter.base;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by ${zhaoyanjun} on 2017/1/12.
 */

public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindViewHolder(T t, RecyclerViewAdapter adapter, int position);
}
