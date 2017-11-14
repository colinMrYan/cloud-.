package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.interf.OnRecommendAppItemClickListener;
import com.inspur.emmcloud.util.ImageDisplayUtils;

import java.util.List;

/**
 * Created by yufuchang on 2017/11/13.
 */

public class RecommendAppAdapter extends RecyclerView.Adapter<RecommendAppAdapter.RecommendAppAdapterHolder>{
    private OnRecommendAppItemClickListener onRecommendAppItemClickListener;
    private LayoutInflater inflater;
    private List<App> recommendList;
    private Context context;
    public RecommendAppAdapter(Context context, List<App> recommendList){
        inflater = LayoutInflater.from(context);
        this.recommendList = recommendList;
        this.context = context;
    }
    @Override
    public RecommendAppAdapter.RecommendAppAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.app_recommand_widget_app_item,null);
        RecommendAppAdapterHolder recommendAppAdapterHolder = new RecommendAppAdapterHolder(view);
        recommendAppAdapterHolder.recommendAppImg = (ImageView) view.findViewById(R.id.my_app_recommend_widget_img);
        return recommendAppAdapterHolder;
    }

    @Override
    public void onBindViewHolder(final RecommendAppAdapter.RecommendAppAdapterHolder holder, final int position) {
        ImageDisplayUtils.getInstance().displayImage(holder.recommendAppImg,"",R.drawable.ic_app_default);
        if(onRecommendAppItemClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecommendAppItemClickListener.onRecommendAppItemClick(holder.recommendAppImg,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class RecommendAppAdapterHolder extends RecyclerView.ViewHolder {
        ImageView recommendAppImg;
        public RecommendAppAdapterHolder(View itemView) {
            super(itemView);
        }
    }

    /**
     * 设置监听器
     * @param l
     */
    public void setOnRecommendAppItemClickListener(OnRecommendAppItemClickListener l) {
        this.onRecommendAppItemClickListener = l;
    }

    /**
     * 更新数据并刷新
     * @param recommendList
     */
    public void setRecommendList(List<App> recommendList){
        this.recommendList = recommendList;
        notifyDataSetChanged();
    }
}
