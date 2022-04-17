package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;

public class UserOrientedConversationHelper implements View.OnClickListener {
    private TextView userInfoView;
    private ImageView closeBtn;
    private View mentionView;
    private ConversationType conversationType = ConversationType.STANDARD;
    private RecyclerView memberListView;
    private RecyclerGridAdapter recyclerGridAdapter;
    private ArrayList<String> selectedUser = new ArrayList<>();
    private String channelType = "";
    private boolean displayingUI = false;
    private OnWhisperEventListener listener;

    public enum ConversationType {
        STANDARD, WHISPER, BURN
    }

    public interface OnWhisperEventListener{
        void closeFunction();
        void showFunction();
    }

    public UserOrientedConversationHelper(View targetView, String type, Context context, OnWhisperEventListener onWhisperEventListener) {
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
                ArrayList<String> copyUsers = new ArrayList<>(selectedUser);
                String content = createAlertContentByUidList(copyUsers);
                showMentionView(content);
            }
        });
        memberListView.setAdapter(recyclerGridAdapter);
        channelType = type;
        conversationType = ConversationType.STANDARD;
        listener = onWhisperEventListener;
    }

    public ConversationType getConversationType() {
        return conversationType;
    }

    public void showUserOrientedLayout(ArrayList<String> userIds) {
        if (listener != null) listener.showWhisper();
        ArrayList<String> targetUsers = userIds;
        targetUsers.remove(BaseApplication.getInstance().getUid());
        if (targetUsers.isEmpty()) return;
        adjustViewHeight(targetUsers.size() > 5);
        setDisplayingUI(true);
        initAndUpdateChannelType();
        if (conversationType.equals(ConversationType.BURN)) {
            setViewInfo(createAlertContentByUidList(targetUsers));
        } else {
            setViewInfo(createAlertContentByUidList(new ArrayList<String>()));
        }
        recyclerGridAdapter.setContactUserList(targetUsers);
    }

    public ArrayList<String> getSelectedUser() {
        return selectedUser;
    }

    public boolean isDisplayingUI() {
        return displayingUI;
    }

    public void closeUserOrientedLayout() {
        if (listener != null) listener.closeWhisper();
        setDisplayingUI(false);
        selectedUser.clear();
        if (mentionView != null) mentionView.setVisibility(View.GONE);
        if (memberListView != null) memberListView.setVisibility(View.GONE);
        setConversationType(ConversationType.STANDARD);
    }

    private void setDisplayingUI(boolean displayingUI) {
        this.displayingUI = displayingUI;
    }

    private void adjustViewHeight(boolean heightForSure){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) memberListView.getLayoutParams();
        if (heightForSure){
            layoutParams.height = (int)dp2px(210);
        } else {
            layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        }
        memberListView.setLayoutParams(layoutParams);
    }

    private float dp2px(int dp) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    private void initAndUpdateChannelType() {
        switch (channelType) {
            case "GROUP":
                setConversationType(ConversationType.WHISPER);
                break;
            case "DIRECT":
                setConversationType(ConversationType.BURN);
                break;
        }
    }

    private void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    private void setViewInfo(String content) {
        showMentionView(content);
        switch (conversationType) {
            case WHISPER:
                if (memberListView != null && memberListView.getVisibility() == View.GONE)
                    memberListView.setVisibility(View.VISIBLE);
                break;
            default:
                memberListView.setVisibility(View.GONE);
                break;
        }
    }

    private void showMentionView(String content) {
        if (userInfoView != null) {
            if (!TextUtils.isEmpty(content)) {
                if (mentionView != null) mentionView.setVisibility(View.VISIBLE);
                userInfoView.setText(userInfoView.getContext().getString(R.string.voice_input_mention, content));
            } else {
                userInfoView.setText("");
                mentionView.setVisibility(View.GONE);
            }
        }
    }

    private String createAlertContentByUidList(ArrayList<String> uids) {
        if (uids.isEmpty()) return "";
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
            holder.selectImg.setVisibility(View.GONE);
            ImageDisplayUtils.getInstance().displayImageByTag(holder.headerImg, userPhotoUrl, R.drawable.icon_person_default);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedState = holder.selectImg.getVisibility();
                    holder.selectImg.setVisibility(View.GONE - selectedState);
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
