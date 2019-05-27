package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.util.common.ImageUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/4/26.
 */

public class ChannelAdapter extends BaseAdapter {
    private Context context;
    private List<Channel> dataList = new ArrayList<>();

    public ChannelAdapter(Context context) {
        this.context = context;
    }

    public void setDataList(List<Channel> channelList) {
        dataList.clear();
        dataList.addAll(channelList);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.msg_item_view, null);
            holder.mainLayout = (RelativeLayout) convertView
                    .findViewById(R.id.main_layout);
            holder.channelPhotoImg = (CircleTextImageView) convertView
                    .findViewById(R.id.msg_img);
            holder.channelTitleText = (TextView) convertView
                    .findViewById(R.id.tv_name);
            holder.channelContentText = (TextView) convertView
                    .findViewById(R.id.tv_content);
            holder.channelTimeText = (TextView) convertView
                    .findViewById(R.id.time_text);
            holder.channelNotReadCountLayout = (RelativeLayout) convertView
                    .findViewById(R.id.msg_new_layout);
            holder.channelNotReadCountText = (TextView) convertView
                    .findViewById(R.id.msg_new_text);
            holder.dndImg = (ImageView) convertView
                    .findViewById(R.id.msg_dnd_img);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Channel channel = dataList.get(position);
        setChannelIcon(channel, holder.channelPhotoImg);
        setChannelMsgReadStateUI(channel, holder);
        holder.channelTitleText.setText(channel.getDisplayTitle());
        holder.dndImg.setVisibility(channel.getDnd() ? View.VISIBLE : View.GONE);
        holder.mainLayout
                .setBackgroundResource(channel.getIsSetTop() ? R.drawable.selector_set_top_msg_list : R.drawable.selector_list);
        return convertView;
    }

    /**
     * 设置Channel的Icon
     *
     * @param channel
     */
    private void setChannelIcon(Channel channel, CircleTextImageView channelPhotoImg) {
        // TODO Auto-generated method stub
        if (channel.getType().equals("GROUP")) {
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + channel.getCid() + "_100.png1");
            channelPhotoImg.setTag("");
            if (file.exists()) {
                channelPhotoImg.setImageBitmap(ImageUtils.getBitmapByFile(file));
            } else {
                channelPhotoImg.setImageResource(R.drawable.icon_channel_group_default);
            }
        } else if (channel.getType().equals("DIRECT") || channel.getType().equals("SERVICE")) {
            ImageDisplayUtils.getInstance().displayImageByTag(channelPhotoImg, channel.getShowIcon(), R.drawable.icon_person_default);
        } else if (channel.getType().equals("LINK")) {
            ImageDisplayUtils.getInstance().displayImage(channelPhotoImg, channel.getShowIcon(), R.drawable.icon_channel_group_default);
        } else {
            channelPhotoImg.setTag("");
            channelPhotoImg.setImageResource(R.drawable.icon_channel_group_default);
        }

    }


    /**
     * 设置频道未读和已读消息的显示
     *
     * @param channel
     */
    private void setChannelMsgReadStateUI(final Channel channel, ViewHolder holder) {
        // TODO Auto-generated method stub
        long unReadCount = channel.getUnReadCount();
        holder.channelTimeText.setText(TimeUtils.getDisplayTime(
                context, channel.getMsgLastUpdate()));
        String chatDrafts = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(channel.getCid()), null);
        if (chatDrafts != null) {
            String content = "<font color='#FF0000'>" + context.getString(R.string.message_type_drafts) + "</font>" + chatDrafts;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.channelContentText.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, null, null));
            } else {
                holder.channelContentText.setText(Html.fromHtml(content));
            }
        } else {
            holder.channelContentText.setText(channel
                    .getNewMsgContent());
        }
        TransHtmlToTextUtils.stripUnderlines(holder.channelContentText,
                R.color.msg_content_color);
        boolean isHasUnReadMsg = (unReadCount != 0);
        holder.channelNotReadCountLayout.setVisibility(isHasUnReadMsg ? View.VISIBLE : View.INVISIBLE);
        if (isHasUnReadMsg) {
            holder.channelNotReadCountText.setText(unReadCount > 99 ? "99+" : "" + unReadCount);
        }
    }

    static class ViewHolder {
        RelativeLayout mainLayout;
        CircleTextImageView channelPhotoImg;
        TextView channelContentText;
        TextView channelTitleText;
        TextView channelTimeText;
        RelativeLayout channelNotReadCountLayout;
        TextView channelNotReadCountText;
        ImageView dndImg;
    }
}
