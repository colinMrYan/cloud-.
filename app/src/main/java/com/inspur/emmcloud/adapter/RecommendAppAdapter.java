package com.inspur.emmcloud.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.inspur.emmcloud.interf.OnRecommendAppItemClickListener;

/**
 * Created by yufuchang on 2017/11/13.
 */

public class RecommendAppAdapter extends RecyclerView.Adapter<RecommendAppAdapter.RecommendAppAdapterHolder>{
    private OnRecommendAppItemClickListener onRecommendAppItemClickListener;
    @Override
    public RecommendAppAdapter.RecommendAppAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecommendAppAdapter.RecommendAppAdapterHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class RecommendAppAdapterHolder extends RecyclerView.ViewHolder {
        ImageView recommendAppImg;
//        TextView recommendAppName;
        public RecommendAppAdapterHolder(View itemView) {
            super(itemView);
        }
    }

    public void setOnRecommendAppItemClickListener(OnRecommendAppItemClickListener l) {
        this.onRecommendAppItemClickListener = l;
    }
}
