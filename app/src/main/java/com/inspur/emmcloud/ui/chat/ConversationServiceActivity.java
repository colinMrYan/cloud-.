package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.bean.chat.GetServiceChannelInfoListResult;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.ServiceChannelInfo;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ConversationServiceActivity extends BaseActivity {
    public static final String EXTRA_CONVERSATION_ID = "cid";
    private Conversation conversation;
    private LoadingDialog loadingDlg;
    private boolean apiRequesting = false;
    private PageState state = PageState.ALL;
    private ConversationServiceAdapter adapter;
    private List<ServiceChannelInfo> conversationServiceList = new ArrayList<>();
    private TextView searchStateView;
    private List<Conversation> serviceConversationList = new ArrayList<>();

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
        cacheConversationList(null);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getConversationServiceList();
    }

    /**
     * 获取消息会话列表
     */
    private void getConversationList() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationServiceActivity.this);
            apiService.setAPIInterface(new WebService());
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("private");
            apiService.getConversationList(jsonArray);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_service;
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.rl_search_contact) {
            IntentUtils.startActivity(this, ConversationServiceSearchActivity.class);
        } else if (id == R.id.header_service_state) {
            changePageState();
            getConversationServiceList();
        }
    }

    private void initView() {
        loadingDlg = new LoadingDialog(ConversationServiceActivity.this);
        ((TextView) findViewById(R.id.header_text)).setText(getString(R.string.address_servicenum_text));
        searchStateView = (TextView) findViewById(R.id.header_service_state);
        changePageState();
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

    private void requestFollowService(String cid, boolean subscribeAlready) {
        if (apiRequesting) return;
        apiRequesting = true;
        loadingDlg.show();
        ChatAPIService apiService = new ChatAPIService(ConversationServiceActivity.this);
        apiService.setAPIInterface(new WebService());
        apiService.requestFollowOrRemoveConversationService(cid, subscribeAlready);
    }

    private void updatePageByInterface(boolean getDateSuccess, GetServiceChannelInfoListResult result) {
        loadingDlg.dismiss();
        if (getDateSuccess) {
            conversationServiceList.clear();
            conversationServiceList.addAll(result.getConversationList());
            adapter.notifyDataSetChanged();
        } else {
            changePageState();
        }
    }

    private void changePageState() {
        switch (state) {
            case ALL:
                state = PageState.FOCUS;
                searchStateView.setText(getString(R.string.all));
                break;
            case FOCUS:
                state = PageState.ALL;
                searchStateView.setText(getString(R.string.focus));
                break;
        }
    }

    private void cacheConversationList(final GetConversationListResult getConversationListResult) {
        List<Conversation> conversationList = new ArrayList<>(ConversationCacheUtils.getConversationList(this));
        if (getConversationListResult != null) {
            conversationList.addAll(getConversationListResult.getConversationList());
        }
        for (Conversation conversation : conversationList) {
            if (conversation.getId().startsWith("FIBER")) {
                serviceConversationList.add(conversation);
            }
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetConversationServiceListSuccess(GetServiceChannelInfoListResult result) {
            updatePageByInterface(true, result);
        }

        @Override
        public void returnGetConversationServiceListFail(String error, int errorCode) {
            updatePageByInterface(false, null);
        }

        @Override
        public void returnGetConversationServiceListAllSuccess(GetServiceChannelInfoListResult result) {
            updatePageByInterface(true, result);
        }

        @Override
        public void returnGetConversationServiceListAllFail(String error, int errorCode) {
            updatePageByInterface(false, null);
        }

        @Override
        public void returnFollowConversationServiceSuccess(ServiceChannelInfo changedConversations) {
            loadingDlg.dismiss();
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SERVICE_CHANNEL_UPDATE));
            for (int i = 0; i < conversationServiceList.size(); i++) {
                ServiceChannelInfo serviceChannelInfo = conversationServiceList.get(i);
                if (serviceChannelInfo.getId().equals(changedConversations.getId())) {
                    serviceChannelInfo.setSubscribe(!serviceChannelInfo.isSubscribe());
                    adapter.notifyItemChanged(i);
                    getConversationList();
                    break;
                }
            }
            apiRequesting = false;
        }

        @Override
        public void returnFollowConversationServiceFail(String error, int errorCode) {
            loadingDlg.dismiss();
            apiRequesting = false;
        }

        @Override
        public void returnConversationListSuccess(GetConversationListResult getConversationListResult) {
            cacheConversationList(getConversationListResult);
            loadingDlg.dismiss();
        }

        @Override
        public void returnConversationListFail(String error, int errorCode) {
            loadingDlg.dismiss();
        }
    }

    public class ConversationServiceAdapter extends RecyclerView.Adapter<ServiceItemViewHolder> {

        @Override
        public int getItemCount() {
            return conversationServiceList.size();
        }

        @Override
        public void onBindViewHolder(ServiceItemViewHolder holder, int arg1) {
            final ServiceChannelInfo serviceChannelInfo = conversationServiceList.get(arg1);
            if (serviceChannelInfo == null) return;
            holder.itemImg.setBackgroundResource(R.drawable.design3_icon_service);
            holder.titleText.setText(serviceChannelInfo.getName());
            holder.titleDesc.setText(serviceChannelInfo.getDescription());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case ALL:
                            requestFollowService(serviceChannelInfo.getId(), serviceChannelInfo.isSubscribe());
                            break;
                        case FOCUS:
                        default:
                            Conversation conversation = ConversationCacheUtils.getConversation(ConversationServiceActivity.this, serviceChannelInfo.getFiber());
                            if (conversation == null) {
                                conversation = getServiceConversation(serviceChannelInfo.getFiber());
                            }
                            if (conversation == null) {
                                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SERVICE_CHANNEL_UPDATE));
                                ToastUtils.show(R.string.error);
                                return;
                            }
                            if (ConversationCacheUtils.getConversation(BaseApplication.getInstance(), serviceChannelInfo.getGroup()) != null) {
                                ToastUtils.show(R.string.warning_service_enter_by_creator);
                                return;
                            }
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                            setConversationRead(new UIConversation(conversation));
                            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SERVICE_CHANNEL_UPDATE));
                            IntentUtils.startActivity(ConversationServiceActivity.this, ConversationActivity.class, bundle);
                    }
                }
            });
            switch (state) {
                case ALL:
                    holder.itemNewMsg.setVisibility(View.INVISIBLE);
                    holder.itemFocusImg.setVisibility(View.VISIBLE);
                    if (serviceChannelInfo.isSubscribe()) {
                        holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.ic_channel_service_foucus));
                    } else {
                        holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.ic_channel_service_unfoucus));
                    }
                    break;
                case FOCUS:
                    holder.itemFocusImg.setVisibility(View.INVISIBLE);
                    holder.itemNewMsg.setVisibility(View.INVISIBLE);
                    Conversation conversation = ConversationCacheUtils.getConversation(ConversationServiceActivity.this, serviceChannelInfo.getFiber());
                    if (conversation == null) {
                        conversation = getServiceConversation(serviceChannelInfo.getFiber());
                    }
                    if (conversation != null) {
                        UIConversation uiConversation = new UIConversation(conversation);
                        if (uiConversation.getUnReadCount() > 0) {
                            holder.itemNewMsg.setVisibility(View.VISIBLE);
                            holder.itemNewMsg.setText(uiConversation.getUnReadCount() > 99 ? "99+" : "" + uiConversation.getUnReadCount());
                        }
                    }
            }
        }

        @Override
        public ServiceItemViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
            LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = mInflater.inflate(R.layout.conversation_service_item_view, arg0, false);
            return new ServiceItemViewHolder(view);
        }

        private Conversation getServiceConversation(String cid) {
            for (Conversation conversation : serviceConversationList) {
                if (conversation.getServiceConversationId().equals(cid)) return conversation;
            }
            return null;
        }

        /**
         * 将单个频道消息置为已读
         *
         * @param uiConversation
         */
        private void setConversationRead(final UIConversation uiConversation) {
            if (uiConversation.getUnReadCount() > 0) {
                MessageCacheUtil.setChannelMessageRead(MyApplication.getInstance(), uiConversation.getId());
            }
        }
    }

    public static class ServiceItemViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView titleDesc;
        ImageView itemImg;
        ImageView itemFocusImg;
        TextView itemNewMsg;

        public ServiceItemViewHolder(View view) {
            super(view);
            titleText = (TextView) view.findViewById(R.id.tv_name);
            titleDesc = (TextView) view.findViewById(R.id.tv_desc);
            itemImg = (ImageView) view.findViewById(R.id.item_icon);
            itemFocusImg = (ImageView) view.findViewById(R.id.item_focus);
            itemNewMsg = (TextView) view.findViewById(R.id.msg_new_text);
        }

    }
}
