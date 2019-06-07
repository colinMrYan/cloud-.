package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.appcenter.news.GroupNews;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.itheima.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * 新闻列表
 *
 * @author sunqx
 */

public class NewsListAdapter extends BaseAdapter {

    private Context context;
    private List<GroupNews> groupNewsList;

    public NewsListAdapter(Context context, List<GroupNews> groupNewsList) {
        this.context = context;
        this.groupNewsList = groupNewsList;
    }

    @Override
    public int getCount() {
        return groupNewsList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupNewsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        NewsHolder holder = new NewsHolder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.news_item_view, null);
            holder.imageView = (RoundedImageView) convertView.findViewById(R.id.news_leftImg_img);
            holder.title = (TextView) convertView.findViewById(R.id.news_middleUp_text);
            holder.textPoser = (TextView) convertView.findViewById(R.id.news_middlemid_text);
            convertView.setTag(holder);
        } else {
            holder = (NewsHolder) convertView.getTag();
        }
        String uri = handlePoster(position);
        ImageDisplayUtils.getInstance().displayImage(holder.imageView, uri, R.drawable.ic_app_news_default_icon);
        holder.title.setTextColor(groupNewsList.get(position).isImportant() ? Color.RED : 0xff203b4f);
        holder.title.setText(groupNewsList.get(position).getTitle());
        String postTime = groupNewsList.get(position).getCreationDate();
        postTime = TimeUtils.Calendar2TimeString(TimeUtils.timeLong2Calendar(Long.parseLong(postTime)), TimeUtils.getFormat(context, TimeUtils.FORMAT_DEFAULT_DATE));
        String dataTime = postTime.substring(0, 10);
        holder.textPoser.setText(groupNewsList.get(position).getAuthor() + "   " + dataTime);
        return convertView;
    }

    /**
     * 刷新新闻列表
     *
     * @param groupNewsList
     */
    public void reFreshNewsList(List<GroupNews> groupNewsList) {
        this.groupNewsList = groupNewsList;
        notifyDataSetChanged();
    }

    /**
     * 处理poster
     *
     * @param position
     * @return
     */
    private String handlePoster(int position) {
        if (!StringUtils.isBlank(groupNewsList.get(position).getPoster())) {
            return APIUri.getPreviewUrl(groupNewsList.get(position).getPoster());
        }
        return null;
    }

    public static class NewsHolder {
        RoundedImageView imageView;
        TextView title;
        TextView textPoser;
    }
}
