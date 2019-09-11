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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationFromChatContent;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/8/9.
 */

public class CommunicationSearchGroupContactActivity extends BaseActivity implements View.OnClickListener, ListView.OnItemClickListener {

    public static final String SEARCH_ALL = "search_all";
    public static final String SEARCH_CONTACT = "search_contact";
    public static final String SEARCH_GROUP = "search_group";
    public static final String SEARCH_ALL_FROM_CHAT = "search_all_from_chat";
    public static final String SEARCH_CONTENT = "search_content";
    public static final int REFRESH_DATA = 1;
    public static final int CLEAR_DATA = 2;

    @BindView(R.id.lv_search_contact)
    ListView searchContactListView;
    @BindView(R.id.lv_search_group)
    ListView searchGroupListView;
    @BindView(R.id.lv_search_contact_from_chat)
    ListView searchContactFromChatListView;
    @BindView(R.id.rl_search_more_group)
    RelativeLayout searchMoreGroupLayout;
    @BindView(R.id.rl_search_more_contact_from_chat)
    RelativeLayout searchMoreContactFromChatLayout;
    @BindView(R.id.rl_search_more_contact)
    RelativeLayout searchMoreContentLayout;
    @BindView(R.id.ll_all_contact)
    LinearLayout allContactLayout;
    @BindView(R.id.ll_all_group)
    LinearLayout allGroupLayout;
    @BindView(R.id.ll_all_content)
    LinearLayout allContentLayout;
    @BindView(R.id.ev_search_input)
    ClearEditText searchEdit;
    private List<SearchModel> contactsList = new ArrayList<>();
    private List<SearchModel> groupsList = new ArrayList<>();
    private List<ConversationFromChatContent> conversationFromChatContentList = new ArrayList<>();
    private Runnable searchRunnable;
    private String searchArea = SEARCH_ALL;
    private Handler handler;
    private GroupOrContactAdapter groupAdapter;
    private GroupOrContactAdapter contactAdapter;
    private ConversationFromChatContentAdapter conversationFromChatContentAdapter;
    private String searchText;
    private long lastSearchTime = 0;
    /**
     * 虚拟键盘
     */
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // TODO Auto-generated method stub
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodUtils.hide(CommunicationSearchGroupContactActivity.this);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        ImmersionBar.with(this).statusBarColor(R.color.search_contact_header_bg).statusBarDarkFont(true, 0.2f).navigationBarColor(R.color.white).navigationBarDarkIcon(true, 1.0f).init();
        handMessage();
        initSearchRunnable();
        groupAdapter = new GroupOrContactAdapter();
        contactAdapter = new GroupOrContactAdapter();
        conversationFromChatContentAdapter = new ConversationFromChatContentAdapter();
        searchEdit.setOnEditorActionListener(onEditorActionListener);
        searchEdit.addTextChangedListener(new SearchWatcher());
        searchContactListView.setAdapter(contactAdapter);
        searchGroupListView.setAdapter(groupAdapter);
        searchContactFromChatListView.setAdapter(conversationFromChatContentAdapter);
        searchContactListView.setOnItemClickListener(this);
        searchGroupListView.setOnItemClickListener(this);
        searchContactFromChatListView.setOnItemClickListener(this);
        searchMoreContentLayout.setVisibility(View.GONE);
        searchMoreGroupLayout.setVisibility(View.GONE);
        searchMoreContactFromChatLayout.setVisibility(View.GONE);
        allContactLayout.setVisibility(View.GONE);
        allContentLayout.setVisibility(View.GONE);
        allGroupLayout.setVisibility(View.GONE);
        InputMethodUtils.display(this, searchEdit);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.communication_search_group_contact_activity;
    }

    @Override
    protected int getStatusType() {
        return STATUS_NO_SET;
    }

    /**
     * 异步处理数据*/
    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_DATA:
                        /**刷新Ui*/
                        if (groupsList == null) {
                            groupsList = new ArrayList<>();
                        }
                        if (contactsList == null) {
                            contactsList = new ArrayList<>();
                        }
                        searchMoreContentLayout.setVisibility(contactsList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMoreGroupLayout.setVisibility(groupsList.size() > 2 ? View.VISIBLE : View.GONE);
                        searchMoreContactFromChatLayout.setVisibility(conversationFromChatContentList.size() > 2 ? View.VISIBLE : View.GONE);
                        allGroupLayout.setVisibility(groupsList.size() > 0 ? View.VISIBLE : View.GONE);
                        allContactLayout.setVisibility(contactsList.size() > 0 ? View.VISIBLE : View.GONE);
                        allContentLayout.setVisibility(conversationFromChatContentList.size() > 0 ? View.VISIBLE : View.GONE);
                        groupAdapter.setContentList(groupsList);
                        contactAdapter.setContentList(contactsList);
                        groupAdapter.notifyDataSetChanged();
                        contactAdapter.notifyDataSetChanged();
                        conversationFromChatContentAdapter.notifyDataSetChanged();
                        break;
                    case CLEAR_DATA:
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        Bundle bundle;
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.rl_search_more_group:
                bundle = new Bundle();
                bundle.putString("search_type", SEARCH_GROUP);
                bundle.putString("search_content", searchText);
                IntentUtils.startActivity(this, CommunicationSearchModelMoreActivity.class, bundle, false);
                //跳转到群组列表页面
                break;
            case R.id.rl_search_more_contact:
                bundle = new Bundle();
                bundle.putString("search_type", SEARCH_CONTACT);
                bundle.putString("search_content", searchText);
                IntentUtils.startActivity(this, CommunicationSearchModelMoreActivity.class, bundle, false);
                //跳转到查找人列表
                break;
            case R.id.rl_search_more_contact_from_chat:
                bundle = new Bundle();
                bundle.putString("search_type", SEARCH_ALL_FROM_CHAT);
                bundle.putString("search_content", searchText);
                IntentUtils.startActivity(this, CommunicationSearchModelMoreActivity.class, bundle, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.lv_search_group:
                groupsList.get(i);
                startChannelActivity(groupsList.get(i).getId());
                break;
            case R.id.lv_search_contact:
                Bundle bundle = new Bundle();
                bundle.putString("uid", contactsList.get(i).getId());
                IntentUtils.startActivity(this, UserInfoActivity.class, bundle);
                break;
            case R.id.lv_search_contact_from_chat:
                Intent intent = new Intent(CommunicationSearchGroupContactActivity.this, CommunicationSearchMessagesActivity.class);
                intent.putExtra(SEARCH_ALL_FROM_CHAT, conversationFromChatContentList.get(i));
                intent.putExtra(SEARCH_CONTENT, searchText);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        IntentUtils.startActivity(CommunicationSearchGroupContactActivity.this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(CommunicationSearchGroupContactActivity.this, uid,
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
            new ChatCreateUtils().createDirectChannel(CommunicationSearchGroupContactActivity.this, uid,
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

    /**
     * 初始化异步方法*/
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
                            case SEARCH_ALL:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    groupsSearchList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    groupsSearchList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                groupsList = new ArrayList<>();
                                for (int i = 0; i < (groupsSearchList.size() > 3 ? 3 : groupsSearchList.size()); i++) {
                                    groupsList.add(groupsSearchList.get(i));
                                }
                                contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 3);
                                contactsList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    contactsList.add(contactsSearchList.get(j).contact2SearchModel());
                                }
                                conversationFromChatContentList = new ArrayList<>();
                                conversationFromChatContentList = MessageCacheUtil.getConversationListByContent(MyApplication.getInstance(), searchText);
                                break;
                            case SEARCH_GROUP:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    groupsSearchList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    groupsSearchList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                groupsList = new ArrayList<>();
                                for (int i = 0; i < (groupsSearchList.size() > 3 ? 3 : groupsSearchList.size()); i++) {
                                    groupsList.add(groupsSearchList.get(i));
                                }
                                break;
                            case SEARCH_CONTACT:
                                contactsSearchList = ContactUserCacheUtils.getSearchContact(searchText, null, 3);
                                contactsList = new ArrayList<>();
                                for (int j = 0; j < contactsSearchList.size(); j++) {
                                    contactsList.add(contactsSearchList.get(j).contact2SearchModel());
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

    /***/
    class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
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
                contactsList.clear();
                groupsList.clear();
                conversationFromChatContentList.clear();
            }
        }
    }

    /***/
    class SearchHolder {
        public CircleTextImageView headImageView;
        public TextView nameTextView;
        public TextView detailTextView;
    }

    /**
     * 根据去群组或者联系人名称搜获获得SearchModel的适配
     */
    class GroupOrContactAdapter extends BaseAdapter {

        private List<SearchModel> contentList = new ArrayList<>();

        public void setContentList(List<SearchModel> contentList) {
            this.contentList = contentList;
        }

        @Override
        public int getCount() {
            return contentList.size();
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
                view = LayoutInflater.from(CommunicationSearchGroupContactActivity.this).inflate(R.layout.communication_search_contact_item, null);
                searchHolder.headImageView = view.findViewById(R.id.iv_contact_head);
                searchHolder.nameTextView = view.findViewById(R.id.tv_contact_name);
                searchHolder.detailTextView = view.findViewById(R.id.tv_contact_detail);
                view.setTag(searchHolder);
            } else {
                searchHolder = (SearchHolder) view.getTag();
            }
            SearchModel searchModel = contentList.get(i);
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
     * 从聊天记录中搜索联系人
     */
    class ConversationFromChatContentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (conversationFromChatContentList.size() > 3) {
                return 3;
            } else {
                return conversationFromChatContentList.size();
            }
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
                view = LayoutInflater.from(CommunicationSearchGroupContactActivity.this).inflate(R.layout.communication_search_contact_item, null);
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
