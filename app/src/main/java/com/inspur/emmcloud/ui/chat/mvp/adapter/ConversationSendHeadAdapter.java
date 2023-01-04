package com.inspur.emmcloud.ui.chat.mvp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import java.util.List;

/**
 * Date：2022/9/28
 * Author：wang zhen
 * Description 转发页头部布局
 */
public class ConversationSendHeadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<MessageForwardMultiBean> conversationList;
    private HeadAdapterListener listener;

    public ConversationSendHeadAdapter(Context context, List<MessageForwardMultiBean> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = View.inflate(context, R.layout.conversation_send_multi_head_item, null);
        HeadViewHolder holder = new HeadViewHolder(view, listener);
        holder.headImage = view.findViewById(R.id.iv_members_head);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        MessageForwardMultiBean messageForwardMultiBean = conversationList.get(position);
        int defaultIcon = messageForwardMultiBean.getType().equals(Conversation.TYPE_GROUP) ?
                ResourceUtils.getResValueOfAttr(context, R.attr.design3_icon_group_default) :
                ResourceUtils.getResValueOfAttr(context, R.attr.design3_icon_person_default);
        String imageUrl = CommunicationUtils.getHeadUrl(messageForwardMultiBean);
        ((HeadViewHolder) viewHolder).headImage.setTag(imageUrl);
        ImageDisplayUtils.getInstance().displayImageByTag(((HeadViewHolder) viewHolder).headImage, imageUrl, defaultIcon);
    }

    @Override
    public int getItemCount() {
        return conversationList == null ? 0 : conversationList.size();
    }

    public void setHeadItemClickListener(HeadAdapterListener listener) {
        this.listener = listener;
    }

    public interface HeadAdapterListener {
        void onItemClick(View view, int position);
    }

    static class HeadViewHolder extends RecyclerView.ViewHolder {
        private HeadAdapterListener adapterListener;
        private ImageView headImage;

        public HeadViewHolder(View itemView, HeadAdapterListener listener) {
            super(itemView);
            this.adapterListener = listener;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapterListener.onItemClick(view, getAdapterPosition());
                }
            });
        }
    }

    public void setSelectHead(List<MessageForwardMultiBean> conversationList) {
        this.conversationList = conversationList;
        notifyDataSetChanged();
    }
}
