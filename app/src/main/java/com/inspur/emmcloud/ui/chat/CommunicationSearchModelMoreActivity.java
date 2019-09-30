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
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.basemodule.util.dialog.ShareDialog;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationFromChatContent;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    private List<SearchModel> searchGroupList = new ArrayList<>(); // 群组搜索结果
    private List<Contact> searchContactList = new ArrayList<>(); // 人员搜索结果
    private List<ConversationFromChatContent> conversationFromChatContentList = new ArrayList<>();//搜索消息
    private String searchArea = SEARCH_GROUP;
    private String searchText;
    private Handler handler;
    private long lastSearchTime = 0;
    private GroupAdapter groupAdapter;
    private ContactAdapter contactAdapter;
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
            conversationFromChatContentAdapter = new ConversationFromChatContentAdapter();
            searchGroupListView.setAdapter(conversationFromChatContentAdapter);
        } else if (searchArea.equals(SEARCH_GROUP)) {
            groupAdapter = new GroupAdapter();
            searchGroupListView.setAdapter(groupAdapter);
        } else if (searchArea.equals(SEARCH_PRIVATE_CHAT)) {
            groupAdapter = new GroupAdapter();
            searchGroupListView.setAdapter(groupAdapter);
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
                            conversationFromChatContentAdapter.notifyDataSetChanged();
                        } else if (searchArea.equals(SEARCH_GROUP) || searchArea.equals(SEARCH_PRIVATE_CHAT)) {
                            groupAdapter.notifyDataSetChanged();
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
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    searchGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                if (searchGroupList == null) {
                                    searchGroupList = new ArrayList<>();
                                }
                                break;
                            case SEARCH_PRIVATE_CHAT:
                                searchGroupList.clear();
                                searchGroupList = ConversationCacheUtils.getSearchConversationPrivateChatSearchModelList(MyApplication.getInstance(), searchText);
                                if (searchGroupList == null) {
                                    searchGroupList = new ArrayList<>();
                                }
                                break;
                            case SEARCH_CONTACT:
                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, null, 25);
                                break;
                            case SEARCH_ALL_FROM_CHAT:
                                conversationFromChatContentList = new ArrayList<>();
                                conversationFromChatContentList = oriChannelInfoByKeyword(searchText);
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

    private List<ConversationFromChatContent> oriChannelInfoByKeyword(String searchData) {
        Map<String, Integer> cidNumMap = new HashMap<>();
        List<com.inspur.emmcloud.bean.chat.Message> allMessageListByKeyword = new ArrayList<>();
        List<ConversationFromChatContent> conversationFromChatContentList = new ArrayList<>();
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
        for (int i = 0; i < conversationList.size(); i++) {
            Conversation tempConversation = conversationList.get(i);
            if (cidNumMap.containsKey(tempConversation.getId())) {
                ConversationFromChatContent conversationFromChatContent =
                        new ConversationFromChatContent(tempConversation, cidNumMap.get(tempConversation.getId()));
                if (tempConversation.getType().equals(Conversation.TYPE_DIRECT)) {
                    conversationFromChatContent.initSingleChatContact();
                }
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
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
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
        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {
            Intent intent = new Intent(CommunicationSearchModelMoreActivity.this, CommunicationSearchMessagesActivity.class);
            intent.putExtra(SEARCH_ALL_FROM_CHAT, conversationFromChatContentList.get(i));
            intent.putExtra(SEARCH_CONTENT, searchText);
            startActivity(intent);
        } else {
            switch (searchArea) {
                case SEARCH_PRIVATE_CHAT:
                case SEARCH_GROUP:
                    if (!searchGroupList.get(i).getId().equals(BaseApplication.getInstance().getUid())) {
                        switch (searchGroupList.get(i).getType()) {
                            case SearchModel.TYPE_GROUP:
                                startChannelActivity(searchGroupList.get(i).getId());
                                break;
                            case SearchModel.TYPE_USER:
                                createDirectChannel(searchGroupList.get(i).getId());
                                break;
                        }
                    }
                    break;
                case SEARCH_CONTACT:
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", searchContactList.get(i).getId());
                    IntentUtils.startActivity(this, UserInfoActivity.class, bundle);
                    break;
            }
        }
    }

    private void handleShare(int position) {
        if (searchArea.equals(SEARCH_ALL_FROM_CHAT)) {
            ConversationFromChatContent conversationFromChatContent = conversationFromChatContentList.get(position);
            final Conversation conversation = conversationFromChatContent.getConversation();

            String name = CommunicationUtils.getName(this, conversation);
            String headUrl = CommunicationUtils.getHeadUrl(conversation);
            //分享到
            ShareDialog.Builder builder = new ShareDialog.Builder(this);
            builder.setUserName(name);
            builder.setContent(shareContent);
            builder.setDefaultResId(R.drawable.ic_app_default);
            builder.setHeadUrl(headUrl);
            final ShareDialog dialog = builder.build();
            dialog.setCallBack(new ShareDialog.CallBack() {
                @Override
                public void onConfirm(View view) {
                    Intent intent = new Intent();
                    SearchModel searchModel = conversation.conversation2SearchModel();
                    intent.putExtra("searchModel", searchModel);
                    setResult(RESULT_OK, intent);
                    dialog.dismiss();
                    finish();
                }

                @Override
                public void onCancel() {
                    dialog.dismiss();
                }
            });
            dialog.show();
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
        String name = searchModel.getName();
        String headUrl = searchModel.getIcon();
        //分享到
        ShareDialog.Builder builder = new ShareDialog.Builder(this);
        builder.setUserName(name);
        builder.setContent(shareContent);
        builder.setDefaultResId(R.drawable.ic_app_default);
        builder.setHeadUrl(headUrl);
        final ShareDialog dialog = builder.build();
        dialog.setCallBack(new ShareDialog.CallBack() {
            @Override
            public void onConfirm(View view) {
                Intent intent = new Intent();
                intent.putExtra("searchModel", searchModel);
                setResult(RESULT_OK, intent);
                dialog.dismiss();
                finish();
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 统一显示图片
     *
     * @param searchModel
     * @param photoImg
     */
    private void displayImg(SearchModel searchModel, CircleTextImageView photoImg) {
        Integer defaultIcon = null; // 默认显示图标
        String icon = null;
        String type = searchModel.getType();
        if (type.equals(SearchModel.TYPE_GROUP)) {
            defaultIcon = R.drawable.icon_channel_group_default;
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + searchModel.getId() + "_100.png1");
            if (file.exists()) {
                icon = "file://" + file.getAbsolutePath();
                ImageDisplayUtils.getInstance().displayImageNoCache(photoImg, icon, defaultIcon);
                return;
            }
        } else if (type.equals(SearchModel.TYPE_STRUCT)) {
            defaultIcon = R.drawable.ic_contact_sub_struct;
        } else {
            defaultIcon = R.drawable.icon_person_default;
            if (!searchModel.getId().equals("null")) {
                icon = APIUri.getChannelImgUrl(MyApplication.getInstance(), searchModel.getId());
            }

        }
        ImageDisplayUtils.getInstance().displayImage(
                photoImg, icon, defaultIcon);

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

    class SearchHolder {
        public CircleTextImageView headImageView;
        public TextView nameTextView;
        public TextView detailTextView;
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
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                CommunicationUtils.setUserDescText(searchModel, searchHolder.detailTextView);
            }
            //刷新数据
            return view;
        }
    }

    /**
     * 群组Adapter
     */
    class GroupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchGroupList.size();
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
            SearchModel searchModel = searchGroupList.get(i);
            if (searchModel != null) {
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
            }
            //刷新数据
            return view;
        }
    }

    /**
     * 从聊天记录中搜索联系人
     */
    class ConversationFromChatContentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return conversationFromChatContentList.size();
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
            Conversation conversation = conversationFromChatContentList.get(i).getConversation();
            if (conversation != null && conversation.getType().equals(Conversation.TYPE_GROUP)) {
                SearchModel searchModel = conversation.conversation2SearchModel();
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                String string = getString(R.string.chat_contact_related_message, conversationFromChatContentList.get(i).getMessageNum());
                searchHolder.detailTextView.setText(string);
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }
            if (conversation != null && conversation.getType().equals(Conversation.TYPE_CAST)) {
                String icon = DirectChannelUtils.getRobotIcon(MyApplication.getInstance(), conversation.getName());
                UIConversation uiConversation = new UIConversation(conversation);
                searchHolder.nameTextView.setText(uiConversation.getTitle());
                ImageDisplayUtils.getInstance().displayImage(searchHolder.headImageView, icon, R.drawable.icon_person_default);
                String string = getString(R.string.chat_contact_related_message, conversationFromChatContentList.get(i).getMessageNum());
                searchHolder.detailTextView.setText(string);
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }
            Contact contact = conversationFromChatContentList.get(i).getSingleChatContactUser();
            if (contact != null && conversation.getType().equals(Conversation.TYPE_DIRECT)) {
                SearchModel searchModel = contact.contact2SearchModel();
                displayImg(searchModel, searchHolder.headImageView);
                searchHolder.nameTextView.setText(searchModel.getName().toString());
                String string = getString(R.string.chat_contact_related_message, conversationFromChatContentList.get(i).getMessageNum());
                searchHolder.detailTextView.setText(string);
                searchHolder.detailTextView.setVisibility(View.VISIBLE);
            }
            //刷新数据
            return view;
        }
    }
}
