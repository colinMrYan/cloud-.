package com.inspur.emmcloud.ui.chat.mvp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import java.util.List;

public class ChatRecentAdapter extends RecyclerView.Adapter<ChatRecentAdapter.ViewHolder> {
    Context context;
    List<Conversation> list;
    AdapterListener adapterListener;

    public ChatRecentAdapter(Context context, List<Conversation> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.member_search_item_view, null);
        ViewHolder holder = new ViewHolder(view, adapterListener);
        holder.headImage = view.findViewById(R.id.img_photo);
        holder.nameTv = view.findViewById(R.id.tv_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRecentAdapter.ViewHolder holder, int position) {
        Conversation conversation = list.get(position);
        int defaultIcon = conversation.getType().equals(Conversation.TYPE_GROUP) ?
                R.drawable.icon_channel_group_default : R.drawable.icon_person_default;
        String imageUrl = CommunicationUtils.getHeadUrl(conversation);
        ImageDisplayUtils.getInstance().displayImage(holder.headImage, imageUrl, defaultIcon);
        holder.nameTv.setText(CommunicationUtils.getName(context, conversation));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public interface AdapterListener {
        public void onItemClick(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView headImage;
        private TextView nameTv;
        private View itemView;
        private AdapterListener adapterListener;

        public ViewHolder(View itemView, AdapterListener listener) {
            super(itemView);
            this.itemView = itemView;
            this.adapterListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            adapterListener.onItemClick(view, getAdapterPosition());
        }
    }


}
