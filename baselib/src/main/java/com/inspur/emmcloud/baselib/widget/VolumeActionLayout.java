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

public class VolumeActionLayout extends LinearLayout {

    private RecyclerView mRecyclerView;
    private List<VolumeActionData> showVolumeActionDataList = new ArrayList<>();
    private VolumeActionClickListener volumeActionClickListener;
    private VolumeActionAdapter volumeActionAdapter;
    private Context context;

    public VolumeActionLayout(Context context) {
        super(context);
        this.context = context;
        LayoutInflater mInflater = LayoutInflater.from(context);
        View layoutRootView = mInflater.inflate(R.layout.volume_action_layout, null);
        mRecyclerView = layoutRootView.findViewById(R.id.rv_volume_action);
        addView(layoutRootView);
    }

    public VolumeActionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater mInflater = LayoutInflater.from(context);
        View layoutRootView = mInflater.inflate(R.layout.volume_action_layout, null);
        mRecyclerView = layoutRootView.findViewById(R.id.rv_volume_action);
        volumeActionAdapter = new VolumeActionAdapter();
        addView(layoutRootView);
    }


    public void setVolumeActionData(List<VolumeActionData> volumeActionDataList, VolumeActionClickListener volumeActionClickListener) {

        if (showVolumeActionDataList.size() > 0) {
            showVolumeActionDataList.clear();
        }
        for (int i = 0; i < volumeActionDataList.size(); i++) {
            if (volumeActionDataList.get(i).isShow()) {
                showVolumeActionDataList.add(volumeActionDataList.get(i));
            }
        }
        if (showVolumeActionDataList.size() > 0) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, showVolumeActionDataList.size()));//这里用线性宫格显示 类似于gridview
        }
        this.volumeActionClickListener = volumeActionClickListener;
        mRecyclerView.setAdapter(volumeActionAdapter);
        volumeActionAdapter.notifyDataSetChanged();
    }

    public interface VolumeActionClickListener {
        public void volumeActionSelectedListener(String actionName);
    }


    private class VolumeActionAdapter extends RecyclerView.Adapter<VolumeActionAdapter.MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.volume_action_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            holder.imageView.setImageResource(showVolumeActionDataList.get(position).getActionIc());
            holder.textView.setText(showVolumeActionDataList.get(position).getActionName());
            holder.itemView.setVisibility(showVolumeActionDataList.get(position).isShow() ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    volumeActionClickListener.volumeActionSelectedListener(showVolumeActionDataList.get(position).getActionName());
                }
            });
        }

        @Override
        public int getItemCount() {
            return showVolumeActionDataList.size();
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
