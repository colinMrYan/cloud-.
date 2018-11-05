package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.ImageUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;

import java.io.File;
import java.util.List;

/**
 * Created by chenmch on 2018/4/26.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private  List<UIConversation> uiConversationList;
    private AdapterListener adapterListener;
    private Context context;
    //HeaderView, FooterView
    private View mHeaderView;
    private View mFooterView;

    public ConversationAdapter(Context context,List<UIConversation> uiConversationList){
        this.uiConversationList = uiConversationList;
        this.context = context;
        if (adapterListener != null){
            adapterListener.onDataChange();
        }
    }

    public void setData(List<UIConversation> uiConversationList){
        synchronized (this){
            this.uiConversationList.clear();
            this.uiConversationList.addAll(uiConversationList);
            if (adapterListener != null){
                adapterListener.onDataChange();
            }
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_view, parent, false);
        ViewHolder holder = new ViewHolder(view, adapterListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UIConversation uiConversation = uiConversationList.get(position);
        holder.titleText.setText(uiConversation.getTitle());
        holder.timeText.setText(TimeUtils.getDisplayTime(MyApplication.getInstance(), uiConversation.getLastUpdate()));
        holder.dndImg.setVisibility(uiConversation.getConversation().isDnd() ? View.VISIBLE : View.GONE);
        holder.mainLayout.setBackgroundResource(uiConversation.getConversation().isStick() ? R.drawable.selector_set_top_msg_list : R.drawable.selector_list);
        boolean isConversationTypeGroup = uiConversation.getConversation().getType().equals(Conversation.TYPE_GROUP);
        if (isConversationTypeGroup){
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH + "/" + MyApplication.getInstance().getTanent() + uiConversation.getId() + "_100.png1");
            holder.photoImg.setTag("");
            if (file.exists()) {
                holder.photoImg.setImageBitmap(ImageUtils.getBitmapByFile(file));
            }else {
                holder.photoImg.setImageResource(R.drawable.icon_channel_group_default);
            }
        }else {
            ImageDisplayUtils.getInstance().displayImageByTag(holder.photoImg, uiConversation.getIcon(), isConversationTypeGroup?R.drawable.icon_channel_group_default:R.drawable.icon_person_default);
        }

        setConversationContent(holder,uiConversation);
        setConversationUnreadState(holder,uiConversation);
    }

    /**
     * 设置会话内容
     * @param holder
     * @param uiConversation
     */
    private void setConversationContent(ViewHolder holder, UIConversation uiConversation){
        String chatDrafts = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(uiConversation.getId()),null);
        if (chatDrafts != null){
            String content = "<font color='#FF0000'>"+context.getString(R.string.message_type_drafts)+"</font>"+chatDrafts;
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                holder.contentText.setText(Html.fromHtml(content,Html.FROM_HTML_MODE_LEGACY, null, null));
            }else {
                holder.contentText.setText(Html.fromHtml(content));
            }
        }else {
            holder.contentText.setText(uiConversation.getContent());
        }
        TransHtmlToTextUtils.stripUnderlines(holder.contentText,R.color.msg_content_color);
    }


    /**
     * 设置会话已读未读状态
     * @param holder
     * @param uiConversation
     */
    private void setConversationUnreadState(ViewHolder holder, UIConversation uiConversation){
        holder.unreadLayout.setVisibility(uiConversation.getUnReadCount()>0 ? View.VISIBLE : View.INVISIBLE);
        if (uiConversation.getUnReadCount()>0) {
            holder.unreadText.setText(uiConversation.getUnReadCount() > 99 ? "99+" : "" + uiConversation.getUnReadCount());
        }
    }

    public void setAdapterListener(AdapterListener adapterListener){
        this.adapterListener = adapterListener;

    }

    @Override
    public int getItemCount() {
        return uiConversationList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        private RelativeLayout mainLayout;
        private CircleTextImageView photoImg;
        private TextView contentText;
        private TextView titleText;
        private TextView timeText;
        private RelativeLayout unreadLayout;
        private TextView unreadText;
        private ImageView dndImg;
        private AdapterListener adapterListener;
        public ViewHolder(View convertView,AdapterListener adapterListener) {
            super(convertView);
            this.adapterListener=adapterListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mainLayout = (RelativeLayout) convertView
                    .findViewById(R.id.main_layout);
            photoImg = (CircleTextImageView) convertView
                    .findViewById(R.id.msg_img);
            titleText = (TextView) convertView
                    .findViewById(R.id.tv_name);
            contentText = (TextView) convertView
                    .findViewById(R.id.tv_content);
            timeText = (TextView) convertView
                    .findViewById(R.id.time_text);
            unreadLayout = (RelativeLayout) convertView
                    .findViewById(R.id.msg_new_layout);
            unreadText = (TextView) convertView
                    .findViewById(R.id.msg_new_text);
            dndImg = (ImageView) convertView
                    .findViewById(R.id.msg_dnd_img);
        }



        @Override
        public void onClick(View v) {
            if (adapterListener != null){
                adapterListener.onItemClick(v,getAdapterPosition());
            }

        }

        @Override
        public boolean onLongClick(View v) {
            if (adapterListener != null){
                return adapterListener.onItemLongClick(v,getAdapterPosition());
            }
            return false;
        }
    }


    //HeaderView和FooterView的get和set函数
    public View getHeaderView() {
        return mHeaderView;
    }
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        LogUtils.LbcDebug("getheaderView");
        notifyItemInserted(0);
    }
    public View getFooterView() {
        return mFooterView;
    }
    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount()-1);
    }


    /**
     * 创建一个回调接口
     */
    public interface AdapterListener {
        void onItemClick(View view, int position);
        boolean onItemLongClick(View view, int position);
        void onDataChange();
    }

}

