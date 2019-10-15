package com.inspur.emmcloud.ui.chat.mvp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ConversationMembersHeadAdapter extends RecyclerView.Adapter<ConversationMembersHeadAdapter.ViewHolder> {

    Context context;
    List<String> uidList;
    boolean isOwner = false;
    AdapterListener adapterListener;

    public ConversationMembersHeadAdapter(Context context, boolean isOwner, List<String> uidList) {
        this.isOwner = isOwner;
        this.context = context;
        this.uidList = uidList;
    }

    public void setUIUidList(List<String> uiIidList) {
        uidList = uiIidList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.chat_group_info_member_head_item, null);
        ViewHolder holder = new ViewHolder(view, adapterListener);
        holder.headImage = view.findViewById(R.id.iv_chat_members_head);
        holder.nameTv = view.findViewById(R.id.tv_chat_members_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String uid;
        String userName;
        String userPhotoUrl;
        if (uidList.get(position).equals("deleteUser")) {
            userPhotoUrl = "drawable://" + R.drawable.ic_delete_channel_member;
            userName = "";
        } else if (uidList.get(position).equals("addUser")) {
            userPhotoUrl = "drawable://" + R.drawable.ic_add_channel_member;
            userName = "";
        } else {
            uid = uidList.get(position);
            userName = ContactUserCacheUtils.getUserName(uid);
            userPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
        }
        holder.nameTv.setText(userName);
        ImageDisplayUtils.getInstance().displayImage(holder.headImage, userPhotoUrl, R.drawable.icon_person_default);

    }

    @Override
    public int getItemCount() {
        return uidList.size();
        // return size>13?(isOwner?15:14):(isOwner?size+2:size+1);/**有效数据大于13时显示查看更多群成员**/
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public interface AdapterListener {
        public void onItemClick(View view, int position);
    }

    /***/
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView headImage;
        private TextView nameTv;
        private View itemView;
        private AdapterListener adapterListener;

        public ViewHolder(View itemView, AdapterListener adapterListener) {
            super(itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
            this.adapterListener = adapterListener;
        }

        @Override
        public void onClick(View view) {
            adapterListener.onItemClick(view, getAdapterPosition());
        }
    }
}
