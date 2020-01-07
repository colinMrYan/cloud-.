package com.inspur.emmcloud.baselib.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.baselib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/9/23.
 */

public class FileActionLayout extends LinearLayout {

    private RecyclerView mRecyclerView;
    private List<FileActionData> showFileActionDataList = new ArrayList<>();
    private FileActionClickListener fileActionClickListener;
    private FileActionAdapter fileActionAdapter;
    private Context context;

    public FileActionLayout(Context context) {
        super(context);
        this.context = context;
        LayoutInflater mInflater = LayoutInflater.from(context);
        View layoutRootView = mInflater.inflate(R.layout.file_action_layout, null);
        mRecyclerView = layoutRootView.findViewById(R.id.rv_file_action);
        addView(layoutRootView);
    }

    public FileActionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater mInflater = LayoutInflater.from(context);
        View layoutRootView = mInflater.inflate(R.layout.file_action_layout, null);
        mRecyclerView = layoutRootView.findViewById(R.id.rv_file_action);
        fileActionAdapter = new FileActionAdapter();
        addView(layoutRootView);
    }

    public void setFileActionData(List<FileActionData> fileActionDataList, FileActionClickListener fileActionClickListener) {
        if (showFileActionDataList.size() > 0) {
            showFileActionDataList.clear();
        }
        for (int i = 0; i < fileActionDataList.size(); i++) {
            if (fileActionDataList.get(i).isShow()) {
                showFileActionDataList.add(fileActionDataList.get(i));
            }
        }
        if (showFileActionDataList.size() > 0) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, showFileActionDataList.size()));//这里用线性宫格显示 类似于gridview
        }
        this.fileActionClickListener = fileActionClickListener;
        mRecyclerView.setAdapter(fileActionAdapter);
        fileActionAdapter.notifyDataSetChanged();
    }

    public void clearView() {
        showFileActionDataList.clear();
        fileActionAdapter.notifyDataSetChanged();
    }

    public interface FileActionClickListener {
        public void fileActionSelectedListener(String actionName);
    }

    private class FileActionAdapter extends RecyclerView.Adapter<FileActionAdapter.MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.file_action_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            holder.imageView.setImageResource(showFileActionDataList.get(position).getActionIc());
            holder.textView.setText(showFileActionDataList.get(position).getActionName());
            holder.itemView.setVisibility(showFileActionDataList.get(position).isShow() ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    fileActionClickListener.fileActionSelectedListener(showFileActionDataList.get(position).getActionName());
                }
            });
        }

        @Override
        public int getItemCount() {
            return showFileActionDataList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView imageView;

            public MyViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.tv_action_name);
                imageView = view.findViewById(R.id.iv_action_ic);
            }
        }
    }

}
