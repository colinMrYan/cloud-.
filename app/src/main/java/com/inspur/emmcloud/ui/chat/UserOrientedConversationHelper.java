package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

public class UserOrientedConversationHelper implements View.OnClickListener {
    private TextView userInfoView;
    private ImageView closeBtn;
    private View mentionView;
    private ConversationType conversationType;
    private RecyclerView memberListView;
    private GridLayoutManager manager;
    private RecyclerGridAdapter recyclerGridAdapter;
    private ArrayList<String> selectedUser;

    public enum ConversationType {
        STANDARD, WHISPER, BURN
    }

    public UserOrientedConversationHelper(View targetView, String type, Context context) {
        this.mentionView = targetView.findViewById(R.id.mention_layout);
        closeBtn = mentionView.findViewById(R.id.close_icon);
        userInfoView = mentionView.findViewById(R.id.mention_info);
        memberListView = targetView.findViewById(R.id.members_recyclerview);
        closeBtn.setOnClickListener(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 5);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        memberListView.setLayoutManager(gridLayoutManager);
        recyclerGridAdapter = new RecyclerGridAdapter(context, new ArrayList<String>());
        recyclerGridAdapter.setCallback(new FunctionCallback() {
            @Override
            public void itemSelected(String uid) {
                if (!selectedUser.remove(uid)) {
                    selectedUser.add(uid);
                }
            }
        });
        memberListView.setAdapter(recyclerGridAdapter);
        switch (type) {
            case "GROUP":
                setConversationType(ConversationType.WHISPER);
                break;
            case "DIRECT":
                setConversationType(ConversationType.BURN);
                break;
        }
    }

    public ConversationType getConversationType() {
        return conversationType;
    }

    public void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    public void showUserOrientedLayout(ArrayList<String> userIds) {
        if (mentionView != null) mentionView.setVisibility(View.VISIBLE);
        setViewInfo(createAlertContentByUidList(userIds));
        recyclerGridAdapter.setContactUserList(userIds);
    }

    private void closeUserOrientedLayout() {
        if (mentionView != null) mentionView.setVisibility(View.GONE);
        if (memberListView != null) memberListView.setVisibility(View.VISIBLE);
        setConversationType(ConversationType.STANDARD);
    }

    private void setViewInfo(String content) {
        if (userInfoView != null)
            userInfoView.setText(userInfoView.getContext().getString(R.string.voice_input_mention, content));
        switch (conversationType) {
            case WHISPER:
                if (memberListView != null) memberListView.setVisibility(View.VISIBLE);
                break;
            default:
                memberListView.setVisibility(View.GONE);
                break;
        }
    }

    private String createAlertContentByUidList(ArrayList<String> uids) {
        StringBuilder nameBuilder = new StringBuilder();
        String firstUid = uids.get(0);
        ContactUser firstContactUser = ContactUserCacheUtils.getContactUserByUid(firstUid);
        if (firstContactUser != null) nameBuilder.append(firstContactUser.getName());
        if (uids.size() == 1) return nameBuilder.toString();
        uids.remove(firstUid);
        for (String uid : uids) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
            if (contactUser != null) nameBuilder.append("、").append(contactUser.getName());
        }
        return nameBuilder.toString();
    }

    public ArrayList<String> getSelectedUser() {
        return selectedUser;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_icon:
                closeUserOrientedLayout();
                break;
        }
    }

    public interface FunctionCallback {
        void itemSelected(String uid);
    }

    public class RecyclerGridAdapter extends RecyclerView.Adapter<RecyclerGridAdapter.MyViewHolder> {
        private List<String> contactUserList = new ArrayList<>();
        private Context mContext;
        private FunctionCallback callback;

        public RecyclerGridAdapter(Context context, List<String> userList) {
            this.mContext = context;
            contactUserList = userList;
        }

        public void setCallback(FunctionCallback callback) {
            this.callback = callback;
        }

        public void setContactUserList(List<String> contactUserList) {
            this.contactUserList = contactUserList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            final String uid = contactUserList.get(position);
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(contactUserList.get(position));
            if (contactUser == null) return;
            String userName = ContactUserCacheUtils.getUserName(uid);
            String userPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
            holder.nameTv.setText(userName);
            ImageDisplayUtils.getInstance().displayImageByTag(holder.headerImg, userPhotoUrl, R.drawable.icon_person_default);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedState = holder.selectImg.getVisibility();
                    holder.selectImg.setVisibility(8 - selectedState);
                    callback.itemSelected(uid);
                }
            });
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public ImageView headerImg;
            public TextView nameTv;
            public ImageView selectImg;

            public MyViewHolder(View itemView) {
                super(itemView);
                headerImg = itemView.findViewById(R.id.iv_chat_members_head);
                nameTv = itemView.findViewById(R.id.tv_chat_members_name);
                selectImg = itemView.findViewById(R.id.iv_chat_members_selected);
            }
        }

        @Override
        public int getItemCount() {
            return contactUserList.size();
        }
    }
}
