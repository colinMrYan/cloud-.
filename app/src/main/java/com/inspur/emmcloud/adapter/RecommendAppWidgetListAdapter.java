package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.interf.OnRecommendAppWidgetItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yufuchang on 2017/11/13.
 */

public class RecommendAppWidgetListAdapter extends RecyclerView.Adapter<RecommendAppWidgetListAdapter.RecommendAppAdapterHolder> {
    private OnRecommendAppWidgetItemClickListener onRecommendAppWidgetItemClickListener;
    private LayoutInflater inflater;
    private List<App> recommendList = new ArrayList<>();
    private Context context;

    public RecommendAppWidgetListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public RecommendAppWidgetListAdapter.RecommendAppAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.app_recommand_widget_app_item, null);
        RecommendAppAdapterHolder recommendAppAdapterHolder = new RecommendAppAdapterHolder(view);
        recommendAppAdapterHolder.recommendAppImg = (ImageView) view.findViewById(R.id.my_app_recommend_app_widget_img);
        return recommendAppAdapterHolder;
    }

    @Override
    public void onBindViewHolder(final RecommendAppWidgetListAdapter.RecommendAppAdapterHolder holder, final int position) {
        ImageDisplayUtils.getInstance().displayImage(holder.recommendAppImg, recommendList.get(position).getAppIcon(), R.drawable.ic_app_default);
        if (onRecommendAppWidgetItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecommendAppWidgetItemClickListener.onRecommendAppWidgetItemClick(recommendList.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return recommendList.size();
    }

    /**
     * 设置监听器
     *
     * @param l
     */
    public void setOnRecommendAppWidgetItemClickListener(OnRecommendAppWidgetItemClickListener l) {
        this.onRecommendAppWidgetItemClickListener = l;
    }

    /**
     * 更新数据并刷新
     *
     * @param recommendList
     */
    public void setAndReFreshRecommendList(List<App> recommendList) {
        this.recommendList = recommendList;
        notifyDataSetChanged();
    }

    public class RecommendAppAdapterHolder extends RecyclerView.ViewHolder {
        ImageView recommendAppImg;

        public RecommendAppAdapterHolder(View itemView) {
            super(itemView);
        }
    }
}
