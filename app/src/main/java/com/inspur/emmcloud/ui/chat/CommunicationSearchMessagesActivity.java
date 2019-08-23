package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.GroupMessageSearchAdapter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationFromChatContent;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.bean.contact.Contact;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/23.
 */

public class CommunicationSearchMessagesActivity extends BaseActivity {
    public static final String SEARCH_CONTACT = "search_contact";
    public static final String SEARCH_GROUP = "search_group";
    public static final String SEARCH_ALL_FROM_CHAT = "search_all_from_chat";
    public static final int REFRESH_DATA = 1;
    public static final int CLEAR_DATA = 2;

    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    @BindView(R.id.tv_cancel)
    TextView cancelTextView;
    @BindView(R.id.lv_search_group_show)
    ListView searchGroupListView;
    private Runnable searchRunnable;
    private List<Message> searchMessagesList = new ArrayList<>(); // 群组搜索结果
    private ConversationFromChatContent conversationFromChatContent;
    private GroupMessageSearchAdapter groupMessageSearchAdapter;
    private String searchArea = SEARCH_GROUP;
    private String searchText;
    private Handler handler;
    private long lastSearchTime = 0;
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchMessagesActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        if (getIntent().hasExtra(SEARCH_ALL_FROM_CHAT)) {
            conversationFromChatContent = (ConversationFromChatContent) getIntent().getSerializableExtra(SEARCH_ALL_FROM_CHAT);
        }
        initSearchRunnable();
        handMessage();
        groupMessageSearchAdapter = new GroupMessageSearchAdapter(this);
        groupMessageSearchAdapter.setGroupMessageSearchListener(new GroupMessageSearchAdapter.GroupMessageSearchListener() {
            @Override
            public void onItemClick(UIMessage uiMessage) {
                if (conversationFromChatContent != null) {
                    if (conversationFromChatContent.getConversation().getType().equals(Conversation.TYPE_GROUP)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(ConversationActivity.EXTRA_CID, conversationFromChatContent.getConversation().getId());
                        bundle.putSerializable(ConversationActivity.EXTRA_UIMESSAGE, uiMessage);
                        dismissSoftKeyboard();
                        IntentUtils.startActivity(CommunicationSearchMessagesActivity.this, ConversationActivity.class, bundle, true);
                    } else {
                        ToastUtils.show("这是个人信息");
                    }
                }

            }
        });
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());

    }

    private void dismissSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_messages_activity;
    }

    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/

                        break;
                    case CLEAR_DATA:
                        break;
                }
            }
        };
    }

    private void initSearchRunnable() {
        searchRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<SearchModel> groupsSearchList;
                        List<Contact> contactsSearchList;
                        switch (searchArea) {
                            case SEARCH_GROUP:

                                break;
                            case SEARCH_CONTACT:

                                break;
                            default:
                                break;
                        }
                        if (handler != null) {
                            handler.sendEmptyMessage(REFRESH_DATA);
                        }
                    }
                }).start();
            }
        };
    }

    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int statr, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //handleSearchApp(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
            searchText = searchEdit.getText().toString().trim();
            if (!StringUtils.isBlank(searchText)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSearchTime > 500) {
                    handler.post(searchRunnable);
                } else {
                    handler.removeCallbacks(searchRunnable);
                    handler.postDelayed(searchRunnable, 500);
                }
                lastSearchTime = System.currentTimeMillis();
            } else {
                lastSearchTime = 0;
                handler.removeCallbacks(searchRunnable);
                handler.sendEmptyMessage(REFRESH_DATA);
            }
        }
    }

    class SearchHolder {
        public CircleTextImageView headImageView;
        public TextView nameTextView;
        public TextView detailTextView;
    }


}
