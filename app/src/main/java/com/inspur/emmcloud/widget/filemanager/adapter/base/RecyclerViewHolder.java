package com.inspur.emmcloud.widget.filemanager.adapter.base;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 *
 */

public abstract class RecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBindViewHolder(T t, RecyclerViewAdapter adapter, int position);
}
