package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import java.util.ArrayList;
import java.util.List;

public class ConversationServiceActivity extends BaseActivity {
    public static final String EXTRA_CONVERSATION_ID = "cid";
    private Conversation conversation;
    private LoadingDialog loadingDlg;
    private boolean apiRequesting = false;
    private PageState state = PageState.FOCUS;
    private ConversationServiceAdapter adapter;
    private List<Conversation> conversationServiceList = new ArrayList<>();
    private TextView searchStateView;

    public enum PageState {
        ALL, FOCUS
    }

    @Override
    public void onCreate() {
        String id = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        conversation = ConversationCacheUtils.getConversation(this, id);
        if (TextUtils.isEmpty(id) || conversation == null) {
            finish();
            return;
        }
        initView();
//        getConversationServiceList();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_service;
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.iv_search) {
            IntentUtils.startActivity(this, ConversationServiceSearchActivity.class);
        } else if (id == R.id.header_service_state) {
            changePageState();
            getConversationServiceList();
        }
    }

    private void initView() {
        loadingDlg = new LoadingDialog(ConversationServiceActivity.this);
        ((TextView) findViewById(R.id.header_text)).setText(conversation.getName());
        searchStateView = findViewById(R.id.header_service_state);
        searchStateView.setText(getString(R.string.all));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.rv_service_list));
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ConversationServiceAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void getConversationServiceList() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            if (loadingDlg.isShowing()) return;
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationServiceActivity.this);
            apiService.setAPIInterface(new WebService());
            switch (state) {
                case ALL:
                    apiService.getConversationServiceAllList();
                    break;
                case FOCUS:
                default:
                    apiService.getConversationServiceList();
                    break;
            }
        }
    }

    private void requestFollowService(String cid) {
        if (apiRequesting) return;
        apiRequesting = true;
        ChatAPIService apiService = new ChatAPIService(ConversationServiceActivity.this);
        apiService.setAPIInterface(new WebService());
        apiService.requestFollowOrRemoveConversationService(cid, true);
    }

    private void updatePageByInterface(boolean getDateSuccess, GetConversationListResult result) {
        if (getDateSuccess) {
            conversationServiceList.clear();
            conversationServiceList.addAll(result.getConversationList());
            adapter.notifyDataSetChanged();
        } else {
            changePageState();
        }
        loadingDlg.dismiss();
    }

    private void changePageState() {
        switch (state) {
            case ALL:
                state = PageState.FOCUS;
                searchStateView.setText(getString(R.string.focus));
                break;
            case FOCUS:
                state = PageState.ALL;
                searchStateView.setText(getString(R.string.all));
                break;
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetConversationServiceListSuccess(GetConversationListResult result) {
            updatePageByInterface(true, result);
        }

        @Override
        public void returnGetConversationServiceListFail(String error, int errorCode) {
            updatePageByInterface(false, null);
        }

        @Override
        public void returnGetConversationServiceListAllSuccess(GetConversationListResult result) {
            updatePageByInterface(true, result);
        }

        @Override
        public void returnGetConversationServiceListAllFail(String error, int errorCode) {
            updatePageByInterface(false, null);
        }

        @Override
        public void returnFollowConversationServiceSuccess(Conversation changedConversations) {
            for (Conversation conversation : conversationServiceList) {
                if (conversation.getId().equals(changedConversations.getId())) {
                    conversation.setFocus(changedConversations.getFocus());
                    break;
                }
            }
            adapter.notifyDataSetChanged();
            apiRequesting = false;
        }

        @Override
        public void returnFollowConversationServiceFail(String error, int errorCode) {
            apiRequesting = false;
        }
    }

    public class ConversationServiceAdapter extends RecyclerView.Adapter<ServiceItemViewHolder> {

        @Override
        public int getItemCount() {
            return conversationServiceList.size();
        }

        @Override
        public void onBindViewHolder(ServiceItemViewHolder holder, int arg1) {
            final Conversation conversation = conversationServiceList.get(arg1);
            if (conversation == null) return;
            holder.titleText.setText(conversation.getName());
            holder.titleDesc.setText(conversation.getAction());
            switch (state) {
                case ALL:
                    holder.itemFocusImg.setVisibility(View.VISIBLE);
                    // todo 关注icon
                    if (conversation.getFocus() == 1) {
                        holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.icon_photo_default));
                    } else {
                        holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.icon_chat_owner));
                    }
                    break;
                case FOCUS:
                    holder.itemFocusImg.setVisibility(View.INVISIBLE);
                    break;
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cid = conversation.getId();
                    if (TextUtils.isEmpty(cid)) return;
                    switch (state) {
                        case ALL:
                            requestFollowService(cid);
                            break;
                        case FOCUS:
                        default:
                            Conversation conversation = ConversationCacheUtils.getConversation(BaseApplication.getInstance(), cid);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                            IntentUtils.startActivity(ConversationServiceActivity.this, ConversationActivity.class, bundle);
                            break;
                    }
                }
            });
        }

        @Override
        public ServiceItemViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
            LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = mInflater.inflate(R.layout.conversation_service_item_view, arg0, false);
            return new ServiceItemViewHolder(view);
        }
    }

    public static class ServiceItemViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView titleDesc;
        ImageView itemImg;
        ImageView itemFocusImg;

        public ServiceItemViewHolder(View view) {
            super(view);
            titleText = (TextView) view.findViewById(R.id.tv_name);
            titleDesc = (TextView) view.findViewById(R.id.tv_desc);
            itemImg = (ImageView) view.findViewById(R.id.item_icon);
            itemFocusImg = (ImageView) view.findViewById(R.id.item_focus);
        }

    }
}
