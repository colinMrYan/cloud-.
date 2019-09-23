package com.inspur.emmcloud.baselib.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
    private List<VolumeActionData> volumeActionDataList = new ArrayList<>();
    private VolumeActionClickListener volumeActionClickListener;
    private VolumeActionAdapter volumeActionAdapter;
    private Context context;

    public VolumeActionLayout(Context context) {
        super(context);
        LayoutInflater mInflater = LayoutInflater.from(context);
        View layoutRootView = mInflater.inflate(R.layout.volume_action_layout, null);
        mRecyclerView = layoutRootView.findViewById(R.id.rv_volume_action);
        addView(layoutRootView);
    }


    public void setVolumeActionData(List<VolumeActionData> volumeActionDataList, VolumeActionClickListener volumeActionClickListener) {
        this.volumeActionDataList = volumeActionDataList;
        this.volumeActionClickListener = volumeActionClickListener;
        volumeActionAdapter = new VolumeActionAdapter();
        mRecyclerView.setAdapter(volumeActionAdapter);
        volumeActionAdapter.notifyDataSetChanged();
    }

    public interface VolumeActionClickListener {
        public void volumeActionSelectedListener(int position);
    }

    /**
     * 展示数据
     **/
    public class VolumeActionData {
        private String actionName;
        private int actionIc;

        public String getActionName() {
            return actionName;
        }

        public void setActionName(String actionName) {
            this.actionName = actionName;
        }

        public int getActionIc() {
            return actionIc;
        }

        public void setActionIc(int actionIc) {
            this.actionIc = actionIc;
        }
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
            holder.imageView.setImageResource(volumeActionDataList.get(position).getActionIc());
            holder.textView.setText(volumeActionDataList.get(position).getActionName());
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    volumeActionClickListener.volumeActionSelectedListener(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return volumeActionDataList.size();
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
