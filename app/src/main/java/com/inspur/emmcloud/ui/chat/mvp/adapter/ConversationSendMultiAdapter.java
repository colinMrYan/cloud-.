package com.inspur.emmcloud.ui.chat.mvp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Date：2022/5/4
 * Author：wang zhen
 * Description 转发多人时adapter
 */
public class ConversationSendMultiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<Conversation> list = new ArrayList<>();
    AdapterListener adapterListener;
    private boolean isMultiSelect = false;
    private final int TYPE_HEADER = 1000;
    private final int TYPE_CONTENT = 1001;

    private List<MessageForwardMultiBean> selectList = new ArrayList<>();

    public ConversationSendMultiAdapter(Context context, List<Conversation> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = View.inflate(context, R.layout.member_send_more_head_item_view, null);
            HeadViewHolder holder = new HeadViewHolder(view);
            holder.headTv = view.findViewById(R.id.tv_head);
            return holder;
        } else {
            View view = View.inflate(context, R.layout.member_send_more_item_view, null);
            ContentViewHolder holder = new ContentViewHolder(view, adapterListener);
            holder.headImage = view.findViewById(R.id.img_photo);
            holder.selectImage = view.findViewById(R.id.selected_img);
            holder.nameTv = view.findViewById(R.id.tv_name);
            return holder;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Conversation conversation = list.get(position);
        // conversation == null 代表head，否则代表内容
        if (list.size() > 0 && conversation == null) {
            return TYPE_HEADER;
        } else {
            return TYPE_CONTENT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Conversation conversation = list.get(position);
        if (holder instanceof HeadViewHolder) {
            if (position == 0) {
                ((HeadViewHolder) holder).headTv.setText(context.getString(R.string.recent_transmit));
            } else {
                ((HeadViewHolder) holder).headTv.setText(context.getString(R.string.recent_conversation));
            }
        } else {
            int defaultIcon = conversation.getType().equals(Conversation.TYPE_GROUP) ?
                    R.drawable.icon_channel_group_default : R.drawable.icon_person_default;
            String imageUrl = CommunicationUtils.getHeadUrl(conversation);
            ((ContentViewHolder) holder).headImage.setTag(imageUrl);
            ImageDisplayUtils.getInstance().displayImageByTag(((ContentViewHolder) holder).headImage, imageUrl, defaultIcon);
            ((ContentViewHolder) holder).nameTv.setText(CommunicationUtils.getName(context, conversation));
            if (isMultiSelect) {
                ((ContentViewHolder) holder).selectImage.setVisibility(View.VISIBLE);
                SearchModel searchModel = conversation.conversation2SearchModel();
                // 转换成统一bean：已选list可能包含会话，也可能包含联系人
                MessageForwardMultiBean messageForwardMultiBean = new MessageForwardMultiBean(searchModel.getId(),
                        searchModel.getName(), searchModel.getType(), searchModel.getIcon(), "");
                if (selectList.contains(messageForwardMultiBean)) {
                    ((ContentViewHolder) holder).selectImage.setImageResource(R.drawable.ic_select_yes);
                } else {
                    ((ContentViewHolder) holder).selectImage.setImageResource(R.drawable.ic_select_no);
                }
            } else {
                ((ContentViewHolder) holder).selectImage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);
    }

    public void notifySelectMode(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
        notifyDataSetChanged();
    }

    public void setSelectList(List<MessageForwardMultiBean> selectList) {
        this.selectList = selectList;
        notifyDataSetChanged();
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView headImage;
        private ImageView selectImage;
        private TextView nameTv;
        private AdapterListener adapterListener;

        public ContentViewHolder(View itemView, AdapterListener listener) {
            super(itemView);
            this.adapterListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adapterListener.onItemClick(view, getAdapterPosition());
        }
    }

    static class HeadViewHolder extends RecyclerView.ViewHolder {
        private TextView headTv;

        public HeadViewHolder(View itemView) {
            super(itemView);
        }
    }


}
