package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ConversationFromChatContentAdapter;
import com.inspur.emmcloud.adapter.GroupOrContactAdapter;
import com.inspur.emmcloud.adapter.PrivateChatAdapter;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.ConversationWithMessageNum;
import com.inspur.emmcloud.bean.chat.SearchHolder;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationOrContactGetIconUtil;
import com.inspur.emmcloud.util.privates.ShareUtil;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/20.
 */

public class CommunicationSearchModelMoreActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener,
        MySwipeRefreshLayout.OnLoadListener, MySwipeRefreshLayout.OnRefreshListener {

    public static final String SEARCH_CONTACT = "search_contact";
    public static final String SEARCH_GROUP = "search_group";
    public static final String SEARCH_PRIVATE_CHAT = "search_private_chat";
    public static final String SEARCH_ALL_FROM_CHAT = "search_all_from_chat";
    public static final String SEARCH_CONTENT = "search_content";
    public static final int REFRESH_DATA = 1;
    public static final int CLEAR_DATA = 2;

    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    @BindView(R.id.tv_cancel)
    TextView cancelTextView;
    @BindView(R.id.lv_search_group_show)
    ListView searchGroupListView;
    @BindView(R.id.refresh_layout)
    MySwipeRefreshLayout mySwipeRefreshLayout;
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mySwipeRefreshLayout.setRefreshing(false);
            return true;
        }
    });
    private Runnable searchRunnable;
    private List<Conversation> searchPrivateConversationList = new ArrayList<>(); // 群组搜索结果
    private List<SearchModel> searchGroupList = new ArrayList<>(); // 群组搜索结果
    private List<Contact> searchContactList = new ArrayList<>(); // 人员搜索结果
    private List<ConversationWithMessageNum> conversationFromChatContentList = new ArrayList<>();//搜索消息
    private String searchArea = SEARCH_GROUP;
    private String searchText;
    private Handler handler;
    private long lastSearchTime = 0;
    private GroupOrContactAdapter groupAdapter;
    private ContactAdapter contactAdapter;
    private PrivateChatAdapter privateChatAdapter;
    private ConversationFromChatContentAdapter conversationFromChatContentAdapter;
    private String shareContent;
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchModelMoreActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        ImmersionBar.with(this).statusBarColor(R.color.search_contact_header_bg).statusBarDarkFont(true, 0.2f).navigationBarColor(R.color.white).navigationBarDarkIcon(true, 1.0f).init();
        searchArea = getIntent().getStringExtra("search_type");
        searchText = getIntent().getStringExtra("search_content");
        shareContent = (String) getIntent().getSerializableExtra(Constant.SHARE_CONTENT);
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        InputMethodUtils.display(this, searchEdit);
        cancelTextView.setOnClickListener(this);
        handMessage();
        initSearchRunnable();
        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {          //不同类型加载不同Adapter
            conversationFromChatContentAdapter = new ConversationFromChatContentAdapter(this);
            conversationFromChatContentAdapter.setLimited(false);
            searchGroupListView.setAdapter(conversationFromChatContentAdapter);
        } else if (searchArea.equals(SEARCH_GROUP)) {
            groupAdapter = new GroupOrContactAdapter(this);
            searchGroupListView.setAdapter(groupAdapter);
        } else if (searchArea.equals(SEARCH_PRIVATE_CHAT)) {
            privateChatAdapter = new PrivateChatAdapter(this);
            searchGroupListView.setAdapter(privateChatAdapter);
        } else if (searchArea.equals(SEARCH_CONTACT)) {
            contactAdapter = new ContactAdapter();
            searchGroupListView.setAdapter(contactAdapter);
        }
        searchGroupListView.setOnItemClickListener(this);
        searchEdit.setText(searchText);
        searchEdit.setSelection(searchText.length());
        mySwipeRefreshLayout.setOnLoadListener(this);
        mySwipeRefreshLayout.setOnRefreshListener(this);
        mySwipeRefreshLayout.setEnabled(true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_model_detail_activity;
    }

    @Override
    protected int getStatusType() {
        return STATUS_NO_SET;
    }

    @Override
    public void onLoadMore() {
        switch (searchArea) {
            case SEARCH_GROUP:
                mySwipeRefreshLayout.setCanLoadMore(false);
                break;
            case SEARCH_CONTACT:
                List<Contact> moreContactList = ContactUserCacheUtils.getSearchContact(searchText, searchContactList, 25);
                mySwipeRefreshLayout.setCanLoadMore((moreContactList.size() == 25));
                searchContactList.addAll(searchContactList.size(), moreContactList);
                contactAdapter.notifyDataSetChanged();
                mySwipeRefreshLayout.setLoading(false);
                break;
        }

    }

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/
                        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {
                            conversationFromChatContentAdapter.setConversationList(conversationFromChatContentList);
                            conversationFromChatContentAdapter.notifyDataSetChanged();
                        } else if (searchArea.equals(SEARCH_GROUP)) {
                            groupAdapter.setContentList(searchGroupList);
                            groupAdapter.notifyDataSetChanged();
                        } else if (searchArea.equals(SEARCH_PRIVATE_CHAT)) {
                            privateChatAdapter.setConversationList(searchPrivateConversationList);
                            privateChatAdapter.notifyDataSetChanged();
                        } else {
                            mySwipeRefreshLayout.setCanLoadMore((searchContactList.size() == 25));
                            contactAdapter.notifyDataSetChanged();
                        }
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
                                searchGroupList.clear();
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    searchGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(), searchText);
                                } else {
                                    searchGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                if (searchGroupList == null) {
                                    searchGroupList = new ArrayList<>();
                                }
                                break;
                            case SEARCH_PRIVATE_CHAT:
                                searchPrivateConversationList.clear();
                                searchPrivateConversationList = ConversationCacheUtils.getSearchConversationPrivateChatSearchModelList(MyApplication.getInstance(), searchText);
                                if (searchPrivateConversationList == null) {
                                    searchPrivateConversationList = new ArrayList<>();
                                }
                                break;
                            case SEARCH_CONTACT:
                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, null, 25);
                                if (searchContactList == null) {
                                    searchContactList = new ArrayList<>();
                                }
                                break;
                            case SEARCH_ALL_FROM_CHAT:
                                conversationFromChatContentList = new ArrayList<>();
                                conversationFromChatContentList = oriChannelInfoByKeyword(searchText);
                                if (conversationFromChatContentList == null) {
                                    conversationFromChatContentList = new ArrayList<>();
                                }
                                //分享过来  去除系统通知
                                if (!StringUtils.isBlank(shareContent)) {
                                    Iterator<ConversationWithMessageNum> iterator = conversationFromChatContentList.iterator();
                                    while (iterator.hasNext()) {
                                        ConversationWithMessageNum fromChatContent = iterator.next();
                                        if (fromChatContent.getConversation().getType().equals(Conversation.TYPE_CAST)) {
                                            iterator.remove();
                                        }
                                    }
                                }
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

    private List<ConversationWithMessageNum> oriChannelInfoByKeyword(String searchData) {
        Map<String, Integer> cidNumMap = new HashMap<>();
        List<com.inspur.emmcloud.bean.chat.Message> allMessageListByKeyword = new ArrayList<>();
        List<ConversationWithMessageNum> conversationFromChatContentList = new ArrayList<>();
        List<String> conversationIdList = new ArrayList<>();
        allMessageListByKeyword = MessageCacheUtil.getMessagesListByKeyword(MyApplication.getInstance(), searchData);
        if (allMessageListByKeyword != null) {
            for (int i = 0; i < allMessageListByKeyword.size(); i++) {
                String currentMessageConversation = allMessageListByKeyword.get(i).getChannel();
                if (cidNumMap != null && cidNumMap.containsKey(currentMessageConversation)) {
                    int num = cidNumMap.get(currentMessageConversation);
                    num = num + 1;
                    cidNumMap.put(currentMessageConversation, num);
                } else {
                    cidNumMap.put(currentMessageConversation, 1);
                    conversationIdList.add(currentMessageConversation);
                }
            }
        }
        List<Conversation> conversationList = ConversationCacheUtils.getConversationListByIdList(MyApplication.getInstance(), conversationIdList);
        if (!StringUtils.isBlank(shareContent)) {
            Iterator<Conversation> iterator = conversationList.iterator();
            while (iterator.hasNext()) {
                Conversation conversation = iterator.next();
                if (conversation.getType().equals(Conversation.TYPE_CAST)) {
                    iterator.remove();
                }
            }
        }
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation tempConversation = conversationList.get(i);
            if (cidNumMap.containsKey(tempConversation.getId())) {
                ConversationWithMessageNum conversationFromChatContent =
                        new ConversationWithMessageNum(tempConversation, cidNumMap.get(tempConversation.getId()));
                conversationFromChatContentList.add(conversationFromChatContent);
            }
        }
        return conversationFromChatContentList;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        IntentUtils.startActivity(this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(this, uid,
                    new OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!StringUtils.isBlank(shareContent)) {
            handleShare(i);
            return;
        }
        Bundle bundle = new Bundle();
        switch (searchArea) {
            case SEARCH_ALL_FROM_CHAT:
                Intent intent = new Intent(CommunicationSearchModelMoreActivity.this, CommunicationSearchMessagesActivity.class);
                intent.putExtra(SEARCH_ALL_FROM_CHAT, conversationFromChatContentList.get(i));
                intent.putExtra(SEARCH_CONTENT, searchText);
                startActivity(intent);
                break;
            case SEARCH_GROUP:
                startChannelActivity(searchGroupList.get(i).getId());
                break;
            case SEARCH_CONTACT:
                bundle.putString("uid", searchContactList.get(i).getId());
                IntentUtils.startActivity(this, UserInfoActivity.class, bundle);
                break;
            case SEARCH_PRIVATE_CHAT:
                bundle.putString(ConversationActivity.EXTRA_CID, searchPrivateConversationList.get(i).getId());
                IntentUtils.startActivity(this, ConversationActivity.class, bundle, true);
                break;
        }
    }

    private void handleShare(int position) {
        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {
            ConversationWithMessageNum conversationFromChatContent = conversationFromChatContentList.get(position);
            final Conversation conversation = conversationFromChatContent.getConversation();
            SearchModel searchModel = conversation.conversation2SearchModel();
            ShareUtil.share(this, searchModel, shareContent);
        } else {
            SearchModel searchModel;
            switch (searchArea) {
                case SEARCH_PRIVATE_CHAT:
                case SEARCH_GROUP:
                    searchModel = searchGroupList.get(position);
                    handleSearchModelShare(searchModel);
                    break;
                case SEARCH_CONTACT:
                    Contact contact = searchContactList.get(position);
                    searchModel = contact.contact2SearchModel();
                    handleSearchModelShare(searchModel);
                    break;
            }
        }
    }

    /**
     * 单人聊天  群组聊天
     *
     * @param searchModel
     */
    private void handleSearchModelShare(final SearchModel searchModel) {
        //分享到
        ShareUtil.share(this, searchModel, shareContent);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

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
                searchContactList.clear();
                searchGroupList.clear();
                conversationFromChatContentList.clear();
                handler.sendEmptyMessage(REFRESH_DATA);
                finish();
            }
        }
    }

    /**
     * 个人Adapter
     */
    class ContactAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchContactList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            SearchHolder searchHolder = new SearchHolder();
            if (view == null) {
                view = LayoutInflater.from(CommunicationSearchModelMoreActivity.this).inflate(R.layout.communication_search_contact_item, null);
                searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
                searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
                searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
                view.setTag(searchHolder);
            } else {
                searchHolder = (SearchHolder) view.getTag();
            }
            SearchModel searchModel = searchContactList.get(i).contact2SearchModel();
            if (searchModel != null) {
                ConversationOrContactGetIconUtil.displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                CommunicationUtils.setUserDescText(searchModel, searchHolder.detailTextView);
            }
            //刷新数据
            return view;
        }
    }
}
