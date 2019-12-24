package com.inspur.emmcloud.application.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.ui.AppGroupActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/8.
 */

public class AppGroupAdapter extends RecyclerView.Adapter<AppGroupAdapter.AppGroupHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<App> appList = new ArrayList<>();
    private AppGroupActivity.GroupAppListClickListener listener;

    public AppGroupAdapter(Context context, List<App> appList) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.appList = appList;
    }

    @Override
    public AppGroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.application_app_group_item, null);
        AppGroupHolder holder = new AppGroupHolder(view);
        holder.imageView = (ImageView) view
                .findViewById(R.id.img_group_icon);
        holder.textView = (TextView) view.findViewById(R.id.tv_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(AppGroupHolder holder, final int position) {
        ImageDisplayUtils.getInstance().displayImage(holder.imageView, appList.get(position).getAppIcon(), R.drawable.ic_app_default);
        holder.textView.setText(appList.get(position).getAppName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onGroupAppClick(appList.get(position));
            }
        });
    }

    /**
     * 设置监听器
     *
     * @param l
     */
    public void setGroupListener(AppGroupActivity.GroupAppListClickListener l) {
        this.listener = l;
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    class AppGroupHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;
        private View itemView;

        public AppGroupHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
