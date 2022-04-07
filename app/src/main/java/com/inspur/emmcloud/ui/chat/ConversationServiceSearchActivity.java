package com.inspur.emmcloud.ui.chat;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import java.util.ArrayList;
import java.util.List;

public class ConversationServiceSearchActivity extends BaseActivity {
    private LoadingDialog loadingDlg;
    private boolean apiRequesting = false;
    private ConversationServiceSearchAdapter adapter;
    private List<Conversation> conversationServiceList = new ArrayList<>();
    private ClearEditText searchEdit;
    /**
     * 虚拟键盘
     */
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
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
        ((TextView) findViewById(R.id.header_text)).setText(getString(R.string.communication_search));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView recyclerView = ((RecyclerView) findViewById(R.id.rv_service_list));
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ConversationServiceSearchAdapter();
        searchEdit = findViewById(R.id.ev_search_input);
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

    private void requestFollowService(String cid) {
        if (apiRequesting) return;
        apiRequesting = true;
        ChatAPIService apiService = new ChatAPIService(ConversationServiceSearchActivity.this);
        apiService.setAPIInterface(new WebService());
        apiService.requestFollowOrRemoveConversationService(cid, true);
    }

    private void updatePageByInterface(boolean getDateSuccess, GetConversationListResult result) {
        if (getDateSuccess) {
            conversationServiceList.clear();
            conversationServiceList.addAll(result.getConversationList());
            adapter.notifyDataSetChanged();
        }
        loadingDlg.dismiss();
    }

    private class WebService extends APIInterfaceInstance {
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

        @Override
        public void returnSearchConversationServiceSuccess(GetConversationListResult getConversationListResult) {
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
            final Conversation conversation = conversationServiceList.get(arg1);
            if (conversation == null) return;
            holder.titleText.setText(conversation.getName());
            holder.titleDesc.setText(conversation.getAction());
            holder.itemFocusImg.setVisibility(View.VISIBLE);
            // todo 关注icon
            if (conversation.getFocus() == 1) {
                holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.icon_photo_default));
            } else {
                holder.itemFocusImg.setImageDrawable(getDrawable(R.drawable.icon_chat_owner));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cid = conversation.getId();
                    if (TextUtils.isEmpty(cid)) return;
                    requestFollowService(cid);
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
            String searchText = searchEdit.getText().toString().trim();
            if (!StringUtils.isBlank(searchText)) {
                getSearchConversationServiceList(searchText);
            } else {
                ToastUtils.show("搜索内容不可为空");
            }
        }
    }
}
