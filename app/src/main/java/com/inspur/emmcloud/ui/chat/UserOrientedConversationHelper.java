package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private Context mContext;
    private final GridLayoutManager gridLayoutManager;
    private String membersDetail; // 群昵称时使用
    private JSONArray membersDetailArray;

    public enum ConversationType {
        STANDARD, WHISPER, BURN, REPLY
    }

    public interface OnWhisperEventListener {
        void closeFunction();

        void showFunction();
    }

    public UserOrientedConversationHelper(View targetView, String type, Context context, OnWhisperEventListener onWhisperEventListener) {
        this.mentionView = targetView.findViewById(R.id.mention_layout);
        this.mContext = context;
        closeBtn = mentionView.findViewById(R.id.close_icon);
        userInfoView = mentionView.findViewById(R.id.mention_info);
        memberListView = targetView.findViewById(R.id.members_recyclerview);
        closeBtn.setOnClickListener(this);
        // 适配横屏头像显示
        gridLayoutManager = new GridLayoutManager(context, 6);
        Configuration configuration = mContext.getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager.setSpanCount(8);
        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager.setSpanCount(6);
        }
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

    /**
     * 悄悄话
     *
     * @param userIds       群其他成员列表
     * @param membersDetail 展示昵称时使用
     */
    public void showUserOrientedLayout(ArrayList<String> userIds, String membersDetail) {
        this.membersDetail = membersDetail;
        membersDetailArray = JSONUtils.getJSONArray(membersDetail, new JSONArray());
        ArrayList<String> robotIds = new ArrayList<>();
        for (String userId : userIds) {
            if (ContactUserCacheUtils.getContactUserByUid(userId) == null) robotIds.add(userId);
        }
        robotIds.add(BaseApplication.getInstance().getUid());
        ArrayList<String> targetUsers = userIds;
        targetUsers.removeAll(robotIds);
        setDisplayingUI(true);
        initAndUpdateChannelType();
        if (conversationType.equals(ConversationType.BURN)) {
            setViewInfo(createAlertContentByUidList(targetUsers));
        } else {
            setViewInfo(createAlertContentByUidList(new ArrayList<String>()));
        }
        if (listener != null) listener.showFunction();
        recyclerGridAdapter.setContactUserList(targetUsers);
        adjustViewHeight(targetUsers.size() > 6);
    }

    public ArrayList<String> getSelectedUser() {
        return selectedUser;
    }

    public boolean isDisplayingUI() {
        return displayingUI;
    }

    public void closeUserOrientedLayout() {
        if (listener != null) listener.closeFunction();
        setDisplayingUI(false);
        selectedUser.clear();
        if (mentionView != null) mentionView.setVisibility(View.GONE);
        if (memberListView != null) memberListView.setVisibility(View.GONE);
        setConversationType(ConversationType.STANDARD);
    }

    private void setDisplayingUI(boolean displayingUI) {
        this.displayingUI = displayingUI;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    private void adjustViewHeight(final boolean heightForSure) {
        memberListView.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) memberListView.getLayoutParams();
                if (heightForSure) {
                    View itemView = gridLayoutManager.getChildAt(0);
                    if (itemView != null) {
                        layoutParams.height = itemView.getHeight() * 2;
                    }
                } else {
                    layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                }
                memberListView.setLayoutParams(layoutParams);
            }
        });
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
            default:
                setConversationType(ConversationType.STANDARD);
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
            if (mentionView != null) mentionView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(content)) {
                userInfoView.setText(userInfoView.getContext().getString(R.string.voice_input_mention) + ": " + content);
            } else {
                userInfoView.setText(userInfoView.getContext().getString(R.string.voice_input_mention));
            }
        }
    }

    private String createAlertContentByUidList(ArrayList<String> uids) {
        if (uids.isEmpty()) return "";
        StringBuilder nameBuilder = new StringBuilder();
        String firstUid = uids.get(0);
        String firstName;
//        ContactUser firstContactUser = ContactUserCacheUtils.getContactUserByUid(firstUid);
//        if (firstContactUser != null) nameBuilder.append(firstName);
        if (!TextUtils.isEmpty(membersDetail)) {
            firstName = ChatMsgContentUtils.getUserNicknameOrName(membersDetailArray, firstUid);
        } else {
            firstName = ContactUserCacheUtils.getUserName(firstUid);
        }
        if (!TextUtils.isEmpty(firstName)) nameBuilder.append(firstName);
        if (uids.size() == 1) return nameBuilder.toString();
        uids.remove(firstUid);
        String userName;
        for (String uid : uids) {
            if (!TextUtils.isEmpty(membersDetail)) {
                userName = ChatMsgContentUtils.getUserNicknameOrName(membersDetailArray, uid);
            } else {
                userName = ContactUserCacheUtils.getUserName(uid);
            }
            if (userName != null) nameBuilder.append("、").append(userName);
//            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
//            if (contactUser != null) nameBuilder.append("、").append(contactUser.getName());
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
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
            if (contactUser == null) return;
            String userPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
            String userName;
            // 先获取昵称，昵称为空则显示通讯录名称
            if (!TextUtils.isEmpty(membersDetail)) {
                userName = ChatMsgContentUtils.getUserNicknameOrName(membersDetailArray, uid);
            } else {
                userName = ContactUserCacheUtils.getUserName(uid);
            }
            holder.nameTv.setText(userName);
            holder.selectImg.setVisibility(selectedUser.contains(uid) ? View.VISIBLE : View.GONE);
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
