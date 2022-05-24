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
public class ConversationSendMultiAdapter extends RecyclerView.Adapter<ConversationSendMultiAdapter.ViewHolder> {
    Context context;
    List<Conversation> list;
    AdapterListener adapterListener;
    private boolean isMultiSelect = false;
    private List<MessageForwardMultiBean> selectList = new ArrayList<>();

    public ConversationSendMultiAdapter(Context context, List<Conversation> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.member_send_more_item_view, null);
        ViewHolder holder = new ViewHolder(view, adapterListener);
        holder.headImage = view.findViewById(R.id.img_photo);
        holder.selectImage = view.findViewById(R.id.selected_img);
        holder.nameTv = view.findViewById(R.id.tv_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationSendMultiAdapter.ViewHolder holder, int position) {
        Conversation conversation = list.get(position);
        int defaultIcon = conversation.getType().equals(Conversation.TYPE_GROUP) ?
                R.drawable.icon_channel_group_default : R.drawable.icon_person_default;
        String imageUrl = CommunicationUtils.getHeadUrl(conversation);
        holder.headImage.setTag(imageUrl);
        ImageDisplayUtils.getInstance().displayImageByTag(holder.headImage, imageUrl, defaultIcon);
        holder.nameTv.setText(CommunicationUtils.getName(context, conversation));
        if (isMultiSelect) {
            holder.selectImage.setVisibility(View.VISIBLE);
            SearchModel searchModel = conversation.conversation2SearchModel();
            // 转换成统一bean：已选list可能包含会话，也可能包含联系人
            MessageForwardMultiBean messageForwardMultiBean = new MessageForwardMultiBean(searchModel.getId(),
                    searchModel.getName(), searchModel.getType(), searchModel.getIcon(), "");
            if (selectList.contains(messageForwardMultiBean)) {
                holder.selectImage.setImageResource(R.drawable.ic_select_yes);
            } else {
                holder.selectImage.setImageResource(R.drawable.ic_select_no);
            }
        } else {
            holder.selectImage.setVisibility(View.GONE);
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

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView headImage;
        private ImageView selectImage;
        private TextView nameTv;
        private AdapterListener adapterListener;

        public ViewHolder(View itemView, AdapterListener listener) {
            super(itemView);
            this.adapterListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adapterListener.onItemClick(view, getAdapterPosition());
        }
    }


}
