package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.ui.appcenter.AppGroupActivity;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/8.
 */

public class AppGroupAdapter extends RecyclerView.Adapter<AppGroupAdapter.AppGroupHodler>{

    private Context context;
    private LayoutInflater inflater;
    private List<App> appList = new ArrayList<>();
    private AppGroupActivity.GroupAppListClickListener listener;
    public AppGroupAdapter(Context context,List<App> appList){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.appList = appList;
    }

    @Override
    public AppGroupHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.my_app_group_item,null);
        AppGroupHodler hodler = new AppGroupHodler(view);
        hodler.imageView = (ImageView) view
                .findViewById(R.id.icon_image);
        hodler.textView = (TextView) view.findViewById(R.id.name_text);
        return hodler;
    }

    @Override
    public void onBindViewHolder(AppGroupHodler holder, final int position) {
        ImageDisplayUtils.getInstance().displayImage(holder.imageView,appList.get(position).getAppIcon(),R.drawable.ic_app_default);
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
     * @param l
     */
    public void setGroupListener(AppGroupActivity.GroupAppListClickListener l){
        this.listener = l;
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    class AppGroupHodler extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView;
        private View itemView;
        public AppGroupHodler(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
