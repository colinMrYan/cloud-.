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
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;

import java.io.File;
import java.util.List;

/**
 * Created by chenmch on 2018/4/26.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<UIConversation> uiConversationList;
    private AdapterListener adapterListener;
    private Context context;

    private RecyclerView mRecyclerView;
    private View VIEW_HEADER;
    //Type
    private int TYPE_NORMAL = 1000;
    private int TYPE_HEADER = 1001;

    public ConversationAdapter(Context context, List<UIConversation> uiConversationList) {
        this.uiConversationList = uiConversationList;
        this.context = context;
        if (adapterListener != null) {
            adapterListener.onDataChange();
        }
    }

    public void setData(List<UIConversation> uiConversationList) {
        synchronized (this) {
            this.uiConversationList.clear();
            this.uiConversationList.addAll(uiConversationList);
            if (adapterListener != null) {
                adapterListener.onDataChange();
            }
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new ViewHolder(VIEW_HEADER, adapterListener);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_view, parent, false);
            ViewHolder holder = new ViewHolder(view, adapterListener);
            return holder;
        }
    }

    public boolean haveHeaderView() {
        return VIEW_HEADER != null;
    }

    private boolean isHeaderView(int position) {
        return haveHeaderView() && position == 0;
    }

    /**
     * 网络异常提示
     *
     * @param NetState 当前网络状态
     */
    public void setNetExceptionView(Boolean NetState) {
        if (false == NetState && !haveHeaderView()) {
            addHeaderView(LayoutInflater.from(context).inflate(R.layout.recycleview_header_item, null));
        } else if (true == NetState && haveHeaderView()) {
            deleteHeaderView();
        }
    }

    /**
     * 添加异常headerView
     *
     * @param headerView 要添加的View
     */
    private void addHeaderView(View headerView) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerView.setLayoutParams(params);
        VIEW_HEADER = headerView;
        notifyItemInserted(0);
        mRecyclerView.getLayoutManager().scrollToPosition(0);
    }

    /**
     * 删除HeaderView
     */
    public void deleteHeaderView() {
        if (haveHeaderView()) {
            notifyItemRemoved(0);
            VIEW_HEADER = null;
        }
    }

    /**
     * 更新单个列表数据
     */
    public void notifyRealItemChanged(int position) {
        if (haveHeaderView()) {
            this.notifyItemChanged(position + 1);
        } else {
            this.notifyItemChanged(position);
        }
    }

    /**
     * 删除单个列表数据
     */
    public void notifyRealItemRemoved(int position) {
        if (haveHeaderView()) {
            this.notifyItemRemoved(position + 1);
        } else {
            this.notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return TYPE_HEADER;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        try {
            if (mRecyclerView == null && mRecyclerView != recyclerView) {
                mRecyclerView = recyclerView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!isHeaderView(position)) {
            if (haveHeaderView()) {
                position--;
            }
            UIConversation uiConversation = uiConversationList.get(position);
            holder.titleText.setText(uiConversation.getTitle());
            holder.timeText.setText(TimeUtils.getDisplayTime(context, uiConversation.getLastUpdate()));
            holder.dndImg.setVisibility(uiConversation.getConversation().isDnd() ? View.VISIBLE : View.GONE);
            holder.mainLayout.setBackgroundResource(uiConversation.getConversation().isStick() ? R.drawable.selector_set_top_msg_list : R.drawable.selector_list);
            boolean isConversationTypeGroup = uiConversation.getConversation().getType().equals(Conversation.TYPE_GROUP);
            if (isConversationTypeGroup) {
                File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH + "/" + MyApplication.getInstance().getTanent() + uiConversation.getId() + "_100.png1");
                holder.photoImg.setTag("");
                if (file.exists()) {
                    holder.photoImg.setImageBitmap(ImageUtils.getBitmapByFile(file));
                } else {
                    holder.photoImg.setImageResource(R.drawable.icon_channel_group_default);
                }
            } else if (uiConversation.getConversation().getType().equals(Conversation.TYPE_TRANSFER)) { /**文件传输助手**/
                String conversationName = BaseApplication.getInstance().getString(R.string.chat_file_transfer);
                holder.titleText.setText(conversationName);
                ImageDisplayUtils.getInstance().displayImageByTag(holder.photoImg, uiConversation.getIcon(), R.drawable.icon_channel_group_default);
            } else {
                ImageDisplayUtils.getInstance().displayImageByTag(holder.photoImg, uiConversation.getIcon(), isConversationTypeGroup ? R.drawable.icon_channel_group_default : R.drawable.icon_person_default);
            }
            setConversationLastMessageSendStatus(holder, uiConversation);
            setConversationContent(holder, uiConversation);
            setConversationUnreadState(holder, uiConversation);
        }
    }

    /**
     * 设置频道中最后一条消息的消息状态
     *
     * @param holder
     * @param uiConversation
     */
    private void setConversationLastMessageSendStatus(ViewHolder holder, UIConversation uiConversation) {
        List<Message> messageList = uiConversation.getMessageList();
        if (messageList != null && messageList.size() > 0) {
            Message message = messageList.get(messageList.size() - 1);
            int status = message.getSendStatus();
            ;
//            if (message.getSendStatus() == Message.MESSAGE_SEND_ING) {
//                status = ChatFileUploadManagerUtils.getInstance().isMessageResourceUploading(
//                        message) ? Message.MESSAGE_SEND_ING : Message.MESSAGE_SEND_FAIL;
//            } else {
//                status = message.getSendStatus();
//            }
            switch (status) {
                case Message.MESSAGE_SEND_ING:
                    holder.sendStatusImg.setVisibility(View.VISIBLE);
                    holder.sendStatusImg.setImageResource(R.drawable.icon_message_sending);
                    break;
                case Message.MESSAGE_SEND_FAIL:
                    holder.sendStatusImg.setVisibility(View.VISIBLE);
                    holder.sendStatusImg.setImageResource(R.drawable.icon_message_send_fail);
                    break;
                default:
                    holder.sendStatusImg.setVisibility(View.GONE);
            }
        } else {
            holder.sendStatusImg.setVisibility(View.GONE);
        }
    }

    /**
     * 设置会话内容
     *
     * @param holder
     * @param uiConversation
     */
    private void setConversationContent(ViewHolder holder, UIConversation uiConversation) {
        String chatDrafts = uiConversation.getConversation().getDraft();
        if (!StringUtils.isBlank(chatDrafts)) {
            String content = "<font color='#FF0000'>" + context.getString(R.string.message_type_drafts) + "</font>" + chatDrafts;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.contentText.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, null, null));
            } else {
                holder.contentText.setText(Html.fromHtml(content));
            }
        } else {
            holder.contentText.setText(uiConversation.getContent());
        }
        TransHtmlToTextUtils.stripUnderlines(holder.contentText, R.color.msg_content_color);
    }


    /**
     * 设置会话已读未读状态
     *
     * @param holder
     * @param uiConversation
     */
    private void setConversationUnreadState(ViewHolder holder, UIConversation uiConversation) {
        holder.unreadLayout.setVisibility(uiConversation.getUnReadCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        if (uiConversation.getUnReadCount() > 0) {
            holder.unreadText.setText(uiConversation.getUnReadCount() > 99 ? "99+" : "" + uiConversation.getUnReadCount());
        }
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;

    }

    @Override
    public int getItemCount() {
        int count = (uiConversationList == null ? 0 : uiConversationList.size());
        if (VIEW_HEADER != null) {
            count++;
        }
        return count;
    }

    /**
     * 创建一个回调接口
     */
    public interface AdapterListener {
        void onItemClick(View view, UIConversation uiConversation);

        boolean onItemLongClick(View view, UIConversation uiConversation);

        void onDataChange();

        void onNetExceptionWightClick();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private RelativeLayout mainLayout;
        private CircleTextImageView photoImg;
        private ImageView sendStatusImg;
        private TextView contentText;
        private TextView titleText;
        private TextView timeText;
        private RelativeLayout unreadLayout;
        private TextView unreadText;
        private ImageView dndImg;
        private AdapterListener adapterListener;

        public ViewHolder(View convertView, AdapterListener adapterListener) {
            super(convertView);
            this.adapterListener = adapterListener;
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
            sendStatusImg = (ImageView) convertView
                    .findViewById(R.id.img_sending_status);
        }


        @Override
        public void onClick(View v) {
            if (adapterListener != null) {
                if (haveHeaderView()) {
                    if (0 == getAdapterPosition()) {
                        adapterListener.onNetExceptionWightClick();   //点击进入新的Activity
                    } else {
                        adapterListener.onItemClick(v, uiConversationList.get(getAdapterPosition() - 1));
                    }
                } else {
                    adapterListener.onItemClick(v, uiConversationList.get(getAdapterPosition()));
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (adapterListener != null) {
                if (haveHeaderView()) {
                    if (0 == getAdapterPosition()) {
                        LogUtils.LbcDebug("onLongClick");
                        return true;
                    } else {
                        //网络异常状态
                        return adapterListener.onItemLongClick(v, uiConversationList.get(getAdapterPosition() - 1));
                    }
                } else {
                    return adapterListener.onItemLongClick(v, uiConversationList.get(getAdapterPosition()));
                }

            }
            return false;
        }
    }

}

