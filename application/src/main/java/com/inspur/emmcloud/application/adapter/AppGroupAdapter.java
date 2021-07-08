package com.inspur.emmcloud.application.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.ui.AppGroupActivity;
import com.inspur.emmcloud.application.widget.GradientDrawableBuilder;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yufuchang on 2018/8/8.
 */

public class AppGroupAdapter extends RecyclerView.Adapter<AppGroupAdapter.AppGroupHolder> {

    private Context context;
    private LayoutInflater inflater;
    private final List<App> appList;
    private Map<String, Integer> mAppStoreBadgeMap;
    private AppGroupActivity.GroupAppListClickListener listener;


    public AppGroupAdapter(Context context, List<App> appList, Map<String, Integer> appStoreBadgeMap) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.appList = appList;
        this.mAppStoreBadgeMap = appStoreBadgeMap;
    }

    @Override
    public AppGroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.application_app_group_item, null);
        AppGroupHolder holder = new AppGroupHolder(view);
        holder.imageView = (ImageView) view
                .findViewById(R.id.img_group_icon);
        holder.textView = (TextView) view.findViewById(R.id.tv_name);
        holder.unhandledBadgesText = (TextView) view.findViewById(R.id.unhandled_badges_text);
        return holder;
    }

    @Override
    public void onBindViewHolder(AppGroupHolder holder, final int position) {
        App app = appList.get(position);
        ImageDisplayUtils.getInstance().displayImage(holder.imageView, app.getAppIcon(), R.drawable.ic_app_default);
        setUnHandledBadgesDisplay(app, holder.unhandledBadgesText);
        holder.textView.setText(appList.get(position).getAppName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onGroupAppClick(appList.get(position));
            }
        });
    }

    /**
     * 处理未处理消息个数的显示
     *
     * @param app
     * @param unhandledBadges∞
     */
    private void setUnHandledBadgesDisplay(App app, TextView unhandledBadges) {
        List<App> subAppList = app.getSubAppList();
        if (subAppList == null || subAppList.isEmpty()) {
            Integer appBadgeNum = mAppStoreBadgeMap.get(app.getAppID());
            if (appBadgeNum != null && appBadgeNum > 0) {
                applyAppNum(appBadgeNum, unhandledBadges);
            } else {
                unhandledBadges.setVisibility(View.GONE);
            }
        } else {
            int allBadgeNum = 0;
            for (App subApp : subAppList) {
                Integer subAppBadgeNum = mAppStoreBadgeMap.get(subApp.getAppID());
                if (subAppBadgeNum != null && subAppBadgeNum > 0) {
                    allBadgeNum += subAppBadgeNum;
                }
            }
            if (allBadgeNum > 0) {
                applyAppNum(allBadgeNum, unhandledBadges);
            } else {
                unhandledBadges.setVisibility(View.GONE);
            }
        }
    }

    private void applyAppNum(int num, TextView unhandledBadges) {
        unhandledBadges.setVisibility(View.VISIBLE);
        GradientDrawable gradientDrawable = new GradientDrawableBuilder()
                .setCornerRadius(DensityUtil.dip2px(context, 40))
                .setBackgroundColor(0xFFF74C31)
                .setStrokeColor(0xFFF74C31).build();
        unhandledBadges.setBackground(gradientDrawable);
        unhandledBadges.setText(num > 99 ? "99+" : (num + ""));
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

    public void updateBadgeNum(Map<String, Integer> appStoreBadgeMap) {
        mAppStoreBadgeMap = appStoreBadgeMap;
        notifyDataSetChanged();
    }

    class AppGroupHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;
        private TextView unhandledBadgesText;
        private View itemView;

        public AppGroupHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
