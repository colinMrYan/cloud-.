package com.inspur.emmcloud.ui.chat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetServiceChannelInfoListResult;
import com.inspur.emmcloud.componentservice.communication.ServiceChannelInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ConversationServiceSearchActivity extends BaseActivity {
    private LoadingDialog loadingDlg;
    private boolean apiRequesting = false;
    private ConversationServiceSearchAdapter adapter;
    private List<ServiceChannelInfo> conversationServiceList = new ArrayList<>();
    private ClearEditText searchEdit;
    private TextView emptyText;
    /**
     * 虚拟键盘
     */
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchText = searchEdit.getText().toString().trim();
                if (!StringUtils.isBlank(searchText)) {
                    getSearchConversationServiceList(searchText);
                } else {
                    ToastUtils.show("搜索内容不可为空");
                }
                InputMethodUtils.hide(ConversationServiceSearchActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_service_search;
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        }
    }

    private void initView() {
        loadingDlg = new LoadingDialog(ConversationServiceSearchActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.rv_service_list));
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ConversationServiceSearchAdapter();
        searchEdit = findViewById(R.id.ev_search_input);
        emptyText = findViewById(R.id.empty_view);
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        recyclerView.setAdapter(adapter);
        InputMethodUtils.display(this, searchEdit);
    }

    private void getSearchConversationServiceList(String searchName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            if (loadingDlg.isShowing()) return;
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationServiceSearchActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.requestSearchConversationService(searchName);
        }
    }

    private void requestFollowService(String cid, boolean subscribeAlready) {
        if (apiRequesting) return;
        apiRequesting = true;
        ChatAPIService apiService = new ChatAPIService(ConversationServiceSearchActivity.this);
        apiService.setAPIInterface(new WebService());
        apiService.requestFollowOrRemoveConversationService(cid, subscribeAlready);
    }

    private void updatePageByInterface(boolean getDateSuccess, GetServiceChannelInfoListResult result) {
        if (getDateSuccess) {
            conversationServiceList.clear();
            conversationServiceList.addAll(result.getConversationList());
            adapter.notifyDataSetChanged();
        }
        loadingDlg.dismiss();
        emptyText.setVisibility(conversationServiceList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnFollowConversationServiceSuccess(ServiceChannelInfo changedConversations) {
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SERVICE_CHANNEL_UPDATE));
            for (ServiceChannelInfo serviceChannelInfo : conversationServiceList) {
                if (serviceChannelInfo.getId().equals(changedConversations.getId())) {
                    serviceChannelInfo.setSubscribe(!serviceChannelInfo.isSubscribe());
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

        @Override
        public void returnSearchConversationServiceSuccess(GetServiceChannelInfoListResult getConversationListResult) {
            updatePageByInterface(true, getConversationListResult);
        }

        @Override
        public void returnSearchConversationServiceFail(String error, int errorCode) {
            updatePageByInterface(false, null);
        }
    }

    public class ConversationServiceSearchAdapter extends RecyclerView.Adapter<ServiceItemViewHolder> {

        @Override
        public int getItemCount() {
            return conversationServiceList.size();
        }

        @Override
        public void onBindViewHolder(ServiceItemViewHolder holder, int arg1) {
            final ServiceChannelInfo serviceChannelInfo = conversationServiceList.get(arg1);
            if (serviceChannelInfo == null) return;
            holder.titleText.setText(serviceChannelInfo.getName());
            holder.titleDesc.setText(serviceChannelInfo.getDescription());
            holder.itemFocusImg.setVisibility(View.VISIBLE);
            // todo 关注icon
            if (serviceChannelInfo.isSubscribe()) {
                holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.ic_channel_service_foucus));
            } else {
                holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.ic_channel_service_unfoucus));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cid = serviceChannelInfo.getId();
                    if (TextUtils.isEmpty(cid)) return;
                    requestFollowService(cid, serviceChannelInfo.isSubscribe());
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

    /**
     * EditText  Watcher
     */
    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
