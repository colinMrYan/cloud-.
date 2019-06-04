package com.inspur.emmcloud.ui.contact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ListViewUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.ContactClickMessage;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.FirstGroupTextModel;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.InputMethodUtils;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.CommonContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.FlowLayout;
import com.inspur.emmcloud.widget.MaxHightScrollView;
import com.inspur.emmcloud.widget.NoHorScrollView;
import com.inspur.emmcloud.widget.dialogs.CustomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by yufuchang on 2018/6/7.
 */

public class ContactSearchFragment extends ContactSearchBaseFragment {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MULTI_SELECT = "isMulti_select";
    public static final String EXTRA_TYPE = "select_content";
    public static final String EXTRA_CONTAIN_ME = "isContainMe";
    public static final String EXTRA_HAS_SELECT = "hasSearchResult";
    public static final String EXTRA_EXCLUDE_SELECT = "excludeContactUidList";
    public static final String EXTRA_LIMIT = "select_limit";
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_CHANNELGROUP = 1;
    private static final int SEARCH_CONTACT = 2;
    private static final int SEARCH_NOTHIING = 4;
    private static final int SEARCH_MORE = 5;
    private View rootView;
    private boolean isSearchSingle = false; // 判断是否搜索单一项
    private boolean isContainMe = false; // 搜索结果是否可以包含自己
    private boolean isMultiSelect = false;
    private int searchContent = -1;
    private FlowLayout flowLayout;
    private EditText searchEdit;
    private TextView headerText;
    private TextView tabHeaderText;
    private LinearLayout originLayout; // 进入后看到的页面
    private RelativeLayout originAllLayout;// 进入后看到的界面和打开某项后的list的layout
    private LinearLayout openGroupLayou;// 打开某项后的layout
    private RecyclerView openGroupTitleListView; // 第一组 通讯录导航列表
    private TextView secondTitleText;

    private ListView openGroupListView; // 第一组数据
    private ListView secondGroupListView;// 第二组数据
    private MaxHightScrollView searchEditLayout;

    private List<SearchModel> commonContactList = new ArrayList<>();
    private List<Contact> openGroupContactList = new ArrayList<>();
    private List<SearchModel> openGroupChannelList = new ArrayList<>();
    private List<SearchModel> selectMemList = new ArrayList<>();
    private List<FirstGroupTextModel> openGroupTextList = new ArrayList<>();
    private MyTextWatcher myTextWatcher;
    private GroupTitleAdapter groupTitleAdapter;
    private OpenGroupListAdapter openGroupAdapter;
    private SecondGroupListAdapter secondGroupListAdapter;
    private int orginCurrentArea = 0; // orgin页面目前的搜索模式
    private int searchArea = 0; // 搜索范围
    private String title;
    private List<SearchModel> searchChannelGroupList = new ArrayList<>(); // 群组搜索结果
    private List<Contact> searchContactList = new ArrayList<Contact>(); // 通讯录搜索结果
    // popupWindow中的控件与数据
    private LinearLayout popSecondGrouplayou;
    private LinearLayout popThirdGrouplayou;
    private ListView popSecondGroupListView;
    private ListView popThirdGroupListView;
    private popAdapter popSecondGroupAdapter;
    private popAdapter popThirdGroupAdapter;
    private TextView popSecondGroupMoreText;
    private TextView popThirdGroupMoreText;
    private NoHorScrollView popLayout;
    private RecyclerView popSecondGroupTitleListView; // 第二组 群组导航列表
    private RecyclerView popThirdGroupTitleListView; // 第三组 通讯录导航列表
    private List<FirstGroupTextModel> popSecondGroupTextList = new ArrayList<>();
    private List<FirstGroupTextModel> popThirdGroupTextList = new ArrayList<>();
    private GroupTitleAdapter popSecondGroupTitleAdapter;
    private GroupTitleAdapter popThirdGroupTitleAdapter;
    private Runnable searchRunnbale;
    private String searchText;
    private long lastSearchTime = 0L;
    private List<String> excludeContactUidList = new ArrayList<>();
    private List<Contact> excludeContactList = new ArrayList<>();//不显示某些数据
    private long lastBackTime;
    private int selectLimit = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.activity_contact_search, null);
        getIntentData();
        initView();
        initSearchRunnable();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setFragmentStatusBarCommon();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.activity_contact_search, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 返回事件是否需要被ui消费掉
     *
     * @return
     */
    public boolean onBackPressedConsumeByUI() {
        if (isPopLayoutVisible()) {
            searchEdit.setText("");
            return true;
        } else if (isOpenGroupLayoutVisiable()) {
            int openGroupTextListSize = openGroupTextList.size();
            int position = openGroupTextListSize - 2;
            clickGroupTitle(position);
            return true;
        }
        return false;
    }

    /**
     * 处理从Activity传递到fragment的返回键的点击事件
     *
     * @param contactClickMessage
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateUI(ContactClickMessage contactClickMessage) {
        if (getActivity().getClass().getSimpleName().equals(IndexActivity.class.getSimpleName())
                && contactClickMessage.getTabId().equals(Constant.APP_TAB_BAR_CONTACT) && contactClickMessage.getViewId() == -1) {
            if (!onBackPressedConsumeByUI()) {
                if ((System.currentTimeMillis() - lastBackTime) > 2000) {
                    ToastUtils.show(getActivity(),
                            getString(R.string.reclick_to_desktop));
                    lastBackTime = System.currentTimeMillis();
                } else {
                    MyApplication.getInstance().exit();
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_QUIT_CHANNEL_GROUP:
            case Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME:
                if (openGroupChannelList.size() > 0) {
                    openGroupChannelList = SearchModel.conversationList2SearchModelList(ConversationCacheUtils
                            .getConversationList(MyApplication.getInstance(), Conversation.TYPE_GROUP));
                    if (openGroupAdapter != null) {
                        openGroupAdapter.notifyDataSetChanged();
                    }
                }
                if (searchChannelGroupList.size() > 0) {
                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                    if (popSecondGroupAdapter != null) {
                        popSecondGroupAdapter.notifyDataSetChanged();
                    }
                }

                break;
        }
    }

    /**
     * 获取从其他Activity传来的数据
     */
    private void getIntentData() {
        // TODO Auto-generated method stub
        if (getActivity().getClass().getSimpleName().equals(IndexActivity.class.getSimpleName())) {
            searchContent = SEARCH_NOTHIING;
        } else {
            Intent intent = getActivity().getIntent();
            if (intent.hasExtra(EXTRA_LIMIT)) {
                selectLimit = intent.getIntExtra(EXTRA_LIMIT, 5000);
            }
            title = intent.getExtras().getString(EXTRA_TITLE);
            isMultiSelect = intent.getExtras().getBoolean(EXTRA_MULTI_SELECT);
            searchContent = intent.getExtras().getInt(EXTRA_TYPE);
            isContainMe = intent.getExtras().containsKey(EXTRA_CONTAIN_ME) && intent.getBooleanExtra(EXTRA_CONTAIN_ME, false);
            if (intent.hasExtra(EXTRA_HAS_SELECT)) {
                selectMemList = (List<SearchModel>) intent.getExtras()
                        .getSerializable(EXTRA_HAS_SELECT);
                if (selectMemList == null) {
                    selectMemList = new ArrayList<>();
                }
            }

            if (intent.hasExtra(EXTRA_EXCLUDE_SELECT)) {
                excludeContactUidList = (List<String>) intent.getExtras().getSerializable(EXTRA_EXCLUDE_SELECT);
                excludeContactList = Contact.contactUserList2ContactList(ContactUserCacheUtils.getContactUserListById(excludeContactUidList));

            }
        }
        initSearchArea();
        if (searchContent == SEARCH_CHANNELGROUP) {
            (rootView.findViewById(R.id.struct_layout))
                    .setVisibility(View.GONE);
        }
        if (searchContent == SEARCH_CONTACT) {
            (rootView.findViewById(R.id.channel_group_layout))
                    .setVisibility(View.GONE);
        }
        if (searchContent == SEARCH_NOTHIING) {
            orginCurrentArea = SEARCH_ALL;
        }
    }

    /**
     * 初始化搜索区域
     */
    private void initSearchArea() {
        // TODO Auto-generated method stub
        orginCurrentArea = searchContent;
        if (orginCurrentArea == SEARCH_NOTHIING) {
            orginCurrentArea = SEARCH_ALL;
        }
        isSearchSingle = false;
    }

    private void initView() {
        // TODO Auto-generated method stub
        headerText = rootView.findViewById(R.id.tv_header);
        tabHeaderText = rootView.findViewById(R.id.tv_tab_header);
        boolean isFromTab = getActivity().getClass().getSimpleName().equals(IndexActivity.class.getSimpleName());
        rootView.findViewById(R.id.ibt_back).setVisibility(isFromTab ? View.GONE : View.VISIBLE);
        headerText.setVisibility(isFromTab ? View.GONE : View.VISIBLE);
        tabHeaderText.setVisibility(isFromTab ? View.VISIBLE : View.GONE);
        originAllLayout = (RelativeLayout) rootView.findViewById(R.id.origin_all_layout);
        originLayout = (LinearLayout) rootView.findViewById(R.id.origin_layout);
        openGroupLayou = (LinearLayout) rootView.findViewById(R.id.open_group_layout);
        openGroupTitleListView = (RecyclerView) rootView.findViewById(R.id.open_title_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        openGroupTitleListView.setLayoutManager(layoutManager);
        openGroupListView = (ListView) rootView.findViewById(R.id.open_first_group_list);
        secondGroupListView = (ListView) rootView.findViewById(R.id.second_group_list);
        secondTitleText = (TextView) rootView.findViewById(R.id.second_title_text);
        myTextWatcher = new MyTextWatcher();
        flowLayout = (FlowLayout) rootView.findViewById(R.id.flowlayout);
        TextView okText = (TextView) rootView.findViewById(R.id.ok_text);
        // 单选时隐藏输入框或者不选时
        if (!isMultiSelect || searchContent == SEARCH_NOTHIING) {
            okText.setVisibility(View.GONE);
        }
        flowAddEdit();
        searchEditLayout = (MaxHightScrollView) rootView.findViewById(R.id.search_edit_layout);
        initSecondGroup();
        initPopView();
        if (selectMemList.size() > 0) {
            notifyFlowLayoutDataChange();
        }
        if (StringUtils.isBlank(title)) {
            title = AppTabUtils.getTabTitle(getActivity(), ContactSearchFragment.class.getSimpleName());
        }
        headerText.setText(title);
        tabHeaderText.setText(title);
        setOnClickListeners();
    }

    /**
     * 设置响应事件
     */
    private void setOnClickListeners() {
        ContactSearchClickListener contactSearchClickListener = new ContactSearchClickListener();
        rootView.findViewById(R.id.ibt_back).setOnClickListener(contactSearchClickListener);
        rootView.findViewById(R.id.ok_text).setOnClickListener(contactSearchClickListener);
        rootView.findViewById(R.id.struct_layout).setOnClickListener(contactSearchClickListener);
        rootView.findViewById(R.id.channel_group_layout).setOnClickListener(contactSearchClickListener);
        rootView.findViewById(R.id.layout).setOnClickListener(contactSearchClickListener);
        rootView.findViewById(R.id.pop_second_group_more_text).setOnClickListener(contactSearchClickListener);
        rootView.findViewById(R.id.pop_third_group_more_text).setOnClickListener(contactSearchClickListener);
    }

    /**
     * 初始化第二组的数据
     */
    private void initSecondGroup() {
        // TODO Auto-generated method stub
        secondTitleText.setText(getString(R.string.recently_used));
        commonContactList = CommonContactCacheUtils.getCommonContactList(
                getActivity().getApplicationContext(), 5, searchContent, excludeContactList);
        secondGroupListAdapter = new SecondGroupListAdapter();
        secondGroupListView.setAdapter(secondGroupListAdapter);
        secondGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                if (getActivity().getClass().getSimpleName().equals(IndexActivity.class.getSimpleName()) && searchContent == -1) {
                    searchContent = SEARCH_NOTHIING;
                }
                SearchModel searchModel = commonContactList.get(position);
                changeMembers(searchModel);
            }
        });

        secondGroupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                // TODO Auto-generated method stub
                showRecentChannelOperationDlg(position);
                return true;
            }

        });
    }

    private void showRecentChannelOperationDlg(final int position) {
        new CustomDialog.MessageDialogBuilder(getActivity())
                .setMessage(R.string.if_delect_current_item)
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    SearchModel searchModel = commonContactList.get(position);
                    CommonContactCacheUtils.delectCommonContact(getActivity().getApplicationContext(), searchModel);
                    commonContactList.remove(position);
                    secondGroupListAdapter.notifyDataSetChanged();
                })
                .show();

    }

    /**
     * 群组或通讯录打开浏览页面
     */
    private void displayOpenLayout() {
        if (openGroupLayou.getVisibility() != View.VISIBLE) {
            originLayout.setVisibility(View.GONE);
            openGroupLayou.setVisibility(View.VISIBLE);
            groupTitleAdapter = new GroupTitleAdapter();
            openGroupTitleListView.setAdapter(groupTitleAdapter);
            if (openGroupTextList.size() > 0) {
                openGroupTitleListView.smoothScrollToPosition(openGroupTextList
                        .size() - 1);
            }
            groupTitleAdapter.setOnItemClickListener(new MyItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    // TODO Auto-generated method stub
                    clickGroupTitle(position);
                }
            });
            openGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    if (orginCurrentArea == SEARCH_CONTACT) {
                        Contact contact = openGroupContactList.get(position);
                        if (contact.getType().toLowerCase().equals("user")) {
                            changeMembers(new SearchModel(contact));
                        } else {
                            openContact(contact);
                        }
                    } else if (orginCurrentArea == SEARCH_CHANNELGROUP) {
                        changeMembers(openGroupChannelList.get(position));
                    }
                }
            });
            openGroupAdapter = new OpenGroupListAdapter();
            openGroupListView.setAdapter(openGroupAdapter);
        } else {
            groupTitleAdapter.notifyDataSetChanged();
            if (openGroupTextList.size() > 0) {
                openGroupTitleListView.smoothScrollToPosition(openGroupTextList
                        .size() - 1);
            }
            openGroupAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 点击组织分级列表
     *
     * @param position
     */
    private void clickGroupTitle(int position) {
        if (position == 0) {
            initSearchArea();
            openGroupTextList.clear();
            originLayout.setVisibility(View.VISIBLE);
            openGroupLayou.setVisibility(View.GONE);
        } else if (position != openGroupTextList.size() - 1) {
            FirstGroupTextModel firstGroupTextModel = openGroupTextList
                    .get(position);
            openGroupTextList = openGroupTextList.subList(0, position);
            openContact(firstGroupTextModel.getId(),
                    firstGroupTextModel.getName()
            );
        }
    }

    /**
     * 打开通讯录
     *
     * @param currentStruct
     */
    private void openContact(Contact currentStruct) {
        // TODO Auto-generated method stub
        if (currentStruct == null) {
            ContactOrg org = ContactOrgCacheUtils.getRootContactOrg();
            if (org == null) {
                ToastUtils.show(MyApplication.getInstance(),
                        getString(R.string.contact_exception));
                return;
            }
            currentStruct = new Contact(ContactOrgCacheUtils.getRootContactOrg());
        }
        openContact(currentStruct.getId(), currentStruct.getName());
    }

    /**
     * 打开通讯录
     *
     * @param id
     * @param name
     */
    private void openContact(String id, String name) {
        if (openGroupTextList.size() == 0) {
            openGroupTextList.add(new FirstGroupTextModel(
                    getString(R.string.all), ""));
        }
        openGroupTextList.add(new FirstGroupTextModel(name, id));
        openGroupContactList = ContactOrgCacheUtils.getChildContactList(id);
        orginCurrentArea = SEARCH_CONTACT;
        isSearchSingle = true;
        displayOpenLayout();
    }

    /**
     * 显示所有的群组
     */
    private void showAllChannelGroup() {
        // TODO Auto-generated method stub
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            openGroupChannelList = SearchModel.channelGroupList2SearchModelList(ChannelGroupCacheUtils
                    .getAllChannelGroupList(MyApplication.getInstance()));
        } else {
            List<Conversation> conversationList = ConversationCacheUtils
                    .getConversationListByLastUpdate(MyApplication.getInstance(), Conversation.TYPE_GROUP);
            List<Conversation> stickConversationList = new ArrayList<>();
            Iterator<Conversation> it = conversationList.iterator();
            while (it.hasNext()) {
                Conversation conversation = it.next();
                if (conversation.isStick()) {
                    stickConversationList.add(conversation);
                    it.remove();
                }
            }
            conversationList.addAll(0, stickConversationList);
            openGroupChannelList = SearchModel.conversationList2SearchModelList(conversationList);
        }

        openGroupTextList.add(new FirstGroupTextModel(getString(R.string.all),
                ""));
        openGroupTextList.add(new FirstGroupTextModel(
                getString(R.string.channel_group), ""));
        orginCurrentArea = SEARCH_CHANNELGROUP;
        isSearchSingle = true;
        displayOpenLayout();
    }

    /**
     * 添加联系人
     */
    private void changeMembers(SearchModel searchModel) {
        if (searchModel != null) {
            if (searchContent == SEARCH_NOTHIING) {
                CommonContactCacheUtils.saveCommonContact(
                        getActivity().getApplicationContext(), searchModel);
                checkInfoOrEnterChannel(searchModel);
                return;
            }
            if (!selectMemList.contains(searchModel)) {
                if (selectMemList.size() < selectLimit) {
                    selectMemList.add(searchModel);
                    CommonContactCacheUtils.saveCommonContact(
                            getActivity().getApplicationContext(), searchModel);
                    if (!isMultiSelect) {
                        returnSearchResultData();
                        return;
                    }
                    notifyFlowLayoutDataChange();
                } else {
                    ToastUtils.show(MyApplication.getInstance(), R.string.contact_select_limit_warning);
                }

            } else {
                selectMemList.remove(searchModel);
                notifyFlowLayoutDataChange();
            }

        }
    }

    /**
     * 查看信息
     *
     * @param searchModel
     */
    private void checkInfoOrEnterChannel(final SearchModel searchModel) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        String id = searchModel.getId();
        String type = searchModel.getType();
        if (type.equals("STRUCT")) {
            return;
        }
        if (id.equals("null")) {
            ToastUtils.show(getActivity().getApplicationContext(), R.string.cannot_view_info);
            return;
        }
        CommonContactCacheUtils.saveCommonContact(getActivity().getApplicationContext(),
                searchModel);
        if (type.equals("USER")) {
            intent.putExtra("uid", id);
            intent.setClass(getActivity().getApplicationContext(), UserInfoActivity.class);
            startActivity(intent);
        } else {
            intent.setClass(getActivity().getApplicationContext(), WebServiceRouterManager.getInstance().isV0VersionChat() ? ChannelV0Activity.class : ConversationActivity.class);
            intent.putExtra("title", searchModel.getName());
            intent.putExtra("cid", searchModel.getId());
            intent.putExtra("channelType", searchModel.getType());
            startActivity(intent);
        }

    }

    /**
     * 刷新FlowLayout
     */
    private void notifyFlowLayoutDataChange() {
        searchEdit.setText("");
        flowLayout.removeAllViews();
        for (int i = 0; i < selectMemList.size(); i++) {
            final SearchModel searchModel = selectMemList.get(i);
            TextView searchResultText = new TextView(getActivity());
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = DensityUtil.dip2px(MyApplication.getInstance(), 5);
            params.topMargin = DensityUtil.dip2px(MyApplication.getInstance(), 2);
            params.bottomMargin = params.topMargin;
            searchResultText.setLayoutParams(params);
            int piddingTop = DensityUtil.dip2px(getActivity().getApplicationContext(), 1);
            int piddingLeft = DensityUtil.dip2px(getActivity().getApplicationContext(), 5);
            searchResultText.setPadding(piddingLeft, piddingTop, piddingLeft, piddingTop);
            searchResultText.setGravity(Gravity.CENTER);
            searchResultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            searchResultText.setTextColor(Color.parseColor("#0F7BCA"));
            searchResultText.setText(selectMemList.get(i).getName());
            searchResultText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    changeMembers(searchModel);
                }
            });
            flowLayout.addView(searchResultText);
        }
        flowAddEdit();
        searchEditLayout.post(new Runnable() {
            public void run() {
                searchEditLayout.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        notifyAllDataChanged();
    }

    private void flowAddEdit() {
        if (searchEdit == null) {
            searchEdit = new EditText(getActivity());
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
                    getActivity().getApplicationContext(), ViewGroup.LayoutParams.WRAP_CONTENT));
            params.topMargin = DensityUtil.dip2px(getActivity().getApplicationContext(), 2);
            params.bottomMargin = params.topMargin;
            int piddingTop = DensityUtil.dip2px(MyApplication.getInstance(), 1);
            int piddingLeft = DensityUtil.dip2px(MyApplication.getInstance(), 10);
            searchEdit.setPadding(piddingLeft, piddingTop, piddingLeft, piddingTop);
            searchEdit.setLayoutParams(params);
            searchEdit.setSingleLine(true);
            searchEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            searchEdit.setBackground(null);
            searchEdit.setHint(getString(R.string.msg_key_search_member));
            searchEdit.addTextChangedListener(myTextWatcher);
        }

        if (searchEdit.getParent() == null) {
            flowLayout.addView(searchEdit);
        }
    }

    private void initSearchRunnable() {
        searchRunnbale = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switch (searchArea) {
                            case SEARCH_ALL:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    searchChannelGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }

                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeContactList, 4);
                                break;
                            case SEARCH_CHANNELGROUP:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    searchChannelGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                break;
                            case SEARCH_CONTACT:
                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeContactList, 4);
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
     * 返回搜索结果
     */
    private void returnSearchResultData() {
        // TODO Auto-generated method stub
        InputMethodUtils.hide(getActivity());
        JSONArray peopleArray = new JSONArray();
        JSONArray channelGroupArray = new JSONArray();
        JSONObject searchResultObj = new JSONObject();
        for (int i = 0; i < selectMemList.size(); i++) {
            SearchModel searchModel = selectMemList.get(i);
            if (searchModel.getType().equals(SearchModel.TYPE_USER)) {
                if (!(MyApplication.getInstance().getUid().equals(searchModel.getId()) && isContainMe == false)) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("pid", searchModel.getId());
                        jsonObject.put("name", searchModel.getName());
                        peopleArray.put(jsonObject);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            } else {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("cid", searchModel.getId());
                    jsonObject.put("name", searchModel.getName());
                    channelGroupArray.put(jsonObject);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        try {
            searchResultObj.put("people", peopleArray);
            searchResultObj.put("channelGroup", channelGroupArray);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String searchResult = searchResultObj.toString();
        Intent intent = new Intent();
        intent.putExtra("searchResult", searchResult);
        intent.putExtra("selectMemList", (Serializable) selectMemList);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    private void initPopView() {
        // TODO Auto-generated method stub
        popLayout = (NoHorScrollView) rootView.findViewById(R.id.pop_layout);
        popSecondGrouplayou = (LinearLayout) rootView.findViewById(R.id.pop_second_group_layout);
        popThirdGrouplayou = (LinearLayout) rootView.findViewById(R.id.pop_third_group_layout);
        popSecondGroupListView = (ListView) rootView.findViewById(R.id.pop_second_group_list);
        popThirdGroupListView = (ListView) rootView.findViewById(R.id.pop_third_group_list);
        popSecondGroupMoreText = (TextView) rootView.findViewById(R.id.pop_second_group_more_text);
        popThirdGroupMoreText = (TextView) rootView.findViewById(R.id.pop_third_group_more_text);
        popSecondGroupTitleListView = (RecyclerView) rootView.findViewById(R.id.pop_second_title_list);
        popThirdGroupTitleListView = (RecyclerView) rootView.findViewById(R.id.pop_third_title_list);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        popSecondGroupTitleListView.setLayoutManager(layoutManager1);
        popThirdGroupTitleListView.setLayoutManager(layoutManager2);
        popSecondGroupTitleAdapter = new GroupTitleAdapter(true, 2);
        popThirdGroupTitleAdapter = new GroupTitleAdapter(true, 3);
        popSecondGroupTitleListView.setAdapter(popSecondGroupTitleAdapter);
        popThirdGroupTitleListView.setAdapter(popThirdGroupTitleAdapter);

        popSecondGroupAdapter = new popAdapter(2);
        popThirdGroupAdapter = new popAdapter(3);
        popSecondGroupListView.setAdapter(popSecondGroupAdapter);
        popThirdGroupListView.setAdapter(popThirdGroupAdapter);

        popSecondGroupListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // TODO Auto-generated method stub
                        changeMembers(searchChannelGroupList.get(position));

                    }
                });
        popThirdGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Contact contact = searchContactList.get(position);
                changeMembers(new SearchModel(contact));

            }
        });

    }

    protected void showSearchPop() {
        // TODO Auto-generated method stub
        if (StringUtils.isBlank(searchText)) {
            return;
        }
        originAllLayout.setVisibility(View.GONE);
        popLayout.setVisibility(View.VISIBLE);
        if (searchArea == SEARCH_ALL) {
            // 控制“更多”按钮的显示和隐藏 当全选时三个group的进行设置
            popSecondGroupMoreText.setVisibility(searchChannelGroupList.size() > 3 ? View.VISIBLE : View.GONE);
            popThirdGroupMoreText.setVisibility(searchContactList.size() > 3 ? View.VISIBLE : View.GONE);
            popSecondGrouplayou.setVisibility(View.VISIBLE);
            popThirdGrouplayou.setVisibility(View.VISIBLE);
            refreshListView(popSecondGroupListView, popSecondGroupAdapter);
            refreshListView(popThirdGroupListView, popThirdGroupAdapter);
        } else if (!isSearchSingle) {
            if (searchArea == SEARCH_CONTACT) {
                popThirdGroupMoreText.setVisibility(searchContactList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGrouplayou.setVisibility(View.GONE);
                popThirdGrouplayou.setVisibility(View.VISIBLE);
                refreshListView(popThirdGroupListView, popThirdGroupAdapter);
            } else if (searchArea == SEARCH_CHANNELGROUP) {
                popSecondGroupMoreText.setVisibility(searchChannelGroupList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGrouplayou.setVisibility(View.VISIBLE);
                popThirdGrouplayou.setVisibility(View.GONE);
                popSecondGroupAdapter.notifyDataSetChanged();
            }
            refreshListView(popSecondGroupListView, popSecondGroupAdapter);
        } else {
            if (searchArea == SEARCH_CONTACT) {
                popThirdGroupMoreText.setVisibility(searchContactList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGrouplayou.setVisibility(View.GONE);
                popThirdGrouplayou.setVisibility(View.VISIBLE);
                refreshListView(popThirdGroupListView, popThirdGroupAdapter);
            } else {
                popSecondGroupMoreText.setVisibility(searchChannelGroupList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGrouplayou.setVisibility(View.VISIBLE);
                popThirdGrouplayou.setVisibility(View.GONE);
                refreshListView(popSecondGroupListView, popSecondGroupAdapter);
            }
        }
        // notifyPopFirstGroupText(openGroupTextList);
        if (popSecondGrouplayou.getVisibility() == View.VISIBLE) {
            notifyPopSecondGroupText(openGroupTextList);
        }
        if (popThirdGrouplayou.getVisibility() == View.VISIBLE) {
            notifyPopThirdGroupText(openGroupTextList);
        }

    }

    /**
     * 隐藏搜索界面
     */
    private void hideSearchPop() {
        // TODO Auto-generated method stub
        popLayout.setVisibility(View.GONE);
        originAllLayout.setVisibility(View.VISIBLE);
        // 重置origin页面的一些搜索参数
        isSearchSingle = openGroupLayou.getVisibility() == View.VISIBLE;
    }

    /**
     * 刷新所有页面的list数据
     */
    private void notifyAllDataChanged() {
        notifyOrginDataChanged();
        notifyPopDataChanged();
    }

    /**
     * 刷新原页面的list数据
     */
    private void notifyOrginDataChanged() {
        if (openGroupAdapter != null) {
            openGroupAdapter.notifyDataSetChanged();
        }
        secondGroupListAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新pop页面的list数据
     */
    private void notifyPopDataChanged() {
        if (popSecondGroupAdapter != null) {
            refreshListView(popSecondGroupListView, popSecondGroupAdapter);
        }
        if (popThirdGroupAdapter != null) {
            refreshListView(popThirdGroupListView, popThirdGroupAdapter);
        }

    }

    /**
     * 刷新pop页面的second group title数据
     */
    private void notifyPopSecondGroupText(List<FirstGroupTextModel> list) {
        // TODO Auto-generated method stub
        popSecondGroupTextList.clear();
        if (list.size() == 0) {
            popSecondGroupTextList.add(new FirstGroupTextModel(
                    getString(R.string.channel_group), ""));
        } else {
            popSecondGroupTextList.addAll(list);
        }
        popSecondGroupTitleAdapter.notifyDataSetChanged();
        if (popSecondGroupTextList.size() > 0) {
            popSecondGroupTitleListView
                    .smoothScrollToPosition(popSecondGroupTextList.size() - 1);
        }
    }

    /**
     * 刷新pop页面的second group title数据
     */
    private void notifyPopThirdGroupText(List<FirstGroupTextModel> list) {
        // TODO Auto-generated method stub
        popThirdGroupTextList.clear();
        if (list.size() == 0) {
            popThirdGroupTextList.add(new FirstGroupTextModel(
                    getString(R.string.origanization_struct), ""));
        } else {
            popThirdGroupTextList.addAll(list);
        }
        popThirdGroupTitleAdapter.notifyDataSetChanged();
        if (popThirdGroupTextList.size() > 0) {
            popThirdGroupTitleListView
                    .smoothScrollToPosition(popThirdGroupTextList.size() - 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK) && (requestCode == SEARCH_MORE)) {
            selectMemList = (List<SearchModel>) data
                    .getSerializableExtra("selectMemList");
            if (isMultiSelect) {
                notifyFlowLayoutDataChange();
            } else if (selectMemList.size() > 0) {
                if (searchContent == SEARCH_NOTHIING) {
                    creatOrInterChannel(selectMemList.get(0));
                } else {
                    returnSearchResultData();
                }
            }

        }
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

    /**
     * list显示item的数量（最多显示3个）
     *
     * @param objectList
     * @return
     */
    public int getListDisplayCount(List<?> objectList) {
        if (objectList.size() > 3) {
            return 3;
        }
        return objectList.size();
    }

    /**
     * 检查popLayout是否可见
     *
     * @return
     */
    public boolean isPopLayoutVisible() {
        return popLayout.getVisibility() == View.VISIBLE;
    }

    /**
     * 检查是否需要回到上一级
     *
     * @return
     */
    public boolean isOpenGroupLayoutVisiable() {
        int openGroupTextListSize = openGroupTextList.size();
        return openGroupLayou.getVisibility() == View.VISIBLE
                && openGroupTextListSize > 1;
    }

    /**
     * 创建或进入频道
     *
     * @param searchModel
     */
    private void creatOrInterChannel(SearchModel searchModel) {
        // TODO Auto-generated method stub
        String uid = MyApplication.getInstance().getUid();
        if (uid.equals(searchModel.getId())) {
            ToastUtils.show(getActivity(),
                    getString(R.string.do_not_select_yourself));
            return;
        }
        if (searchModel.getType().equals("USER")) {
            creatDirectChannel(searchModel.getId());
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("title", searchModel.getName());
            bundle.putString("cid", searchModel.getId());
            bundle.putString("channelType", searchModel.getType());
            IntentUtils.startActivity(getActivity(),
                    WebServiceRouterManager.getInstance().isV0VersionChat() ?
                            ChannelV0Activity.class : ConversationActivity.class, bundle);

        }
    }

    /**
     * 创建单聊
     *
     * @param id
     */
    private void creatDirectChannel(String id) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getActivity().getApplicationContext())) {
            if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
                new ConversationCreateUtils().createDirectConversation(getActivity(), id,
                        new ConversationCreateUtils.OnCreateDirectConversationListener() {
                            @Override
                            public void createDirectConversationSuccess(Conversation conversation) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                                IntentUtils.startActivity(getActivity(), ConversationActivity.class, bundle);
                            }

                            @Override
                            public void createDirectConversationFail() {

                            }
                        });
            } else {
                new ChatCreateUtils().createDirectChannel(
                        getActivity(), id,
                        new ChatCreateUtils.OnCreateDirectChannelListener() {

                            @Override
                            public void createDirectChannelSuccess(
                                    GetCreateSingleChannelResult getCreateSingleChannelResult) {
                                // TODO Auto-generated method stub
                                Bundle bundle = new Bundle();
                                bundle.putString("cid",
                                        getCreateSingleChannelResult.getCid());
                                bundle.putString("channelType",
                                        getCreateSingleChannelResult.getType());
                                bundle.putString("title",
                                        getCreateSingleChannelResult
                                                .getName(getActivity().getApplicationContext()));
                                IntentUtils.startActivity(
                                        getActivity(), ChannelV0Activity.class, bundle);
                            }

                            @Override
                            public void createDirectChannelFail() {
                                // TODO Auto-generated method stub

                            }
                        });
            }
        }
    }

    /**
     * 刷新ScrollViewWithListView，并计算其高度
     *
     * @param listView
     * @param adpter
     */
    private void refreshListView(ListView listView, BaseAdapter adpter) {
        adpter.notifyDataSetChanged();
        ListViewUtils.setListViewHeightBasedOnChildren(listView);
    }

    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class ViewHolder {
        TextView nameText;
        CircleTextImageView photoImg;
        ImageView rightArrowImg;
        ImageView selectedImg;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        TextView titleText;
        ImageView titleImg;
        View view;
        private MyItemClickListener mListener;

        public MyViewHolder(View view, MyItemClickListener listener) {
            super(view);
            this.view = view;
            titleText = (TextView) view.findViewById(R.id.tv_name_tips);
            titleImg = (ImageView) view.findViewById(R.id.title_img);
            this.mListener = listener;
            view.setOnClickListener(this);

        }

        public View getView() {
            return view;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }

        }

    }

    class ContactSearchClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity().getApplicationContext(),
                    ContactSearchMoreActivity.class);
            intent.putExtra(EXTRA_LIMIT, selectLimit);
            switch (v.getId()) {
                case R.id.pop_second_group_more_text:
                    intent.putExtra("groupTextList",
                            (Serializable) popSecondGroupTextList);
                    intent.putExtra("groupTextList",
                            (Serializable) popSecondGroupTextList);
                    intent.putExtra("selectMemList", (Serializable) selectMemList);
                    intent.putExtra("groupPosition", 2);
                    intent.putExtra("searchText", searchEdit.getText().toString());
                    intent.putExtra("searchContent", searchContent);
                    intent.putExtra("isMultiSelect", isMultiSelect);
                    if (excludeContactUidList.size() > 0) {
                        intent.putExtra(ContactSearchMoreActivity.EXTRA_EXCLUDE_SELECT, (Serializable) excludeContactUidList);
                    }
                    startActivityForResult(intent, SEARCH_MORE);
                    break;
                case R.id.pop_third_group_more_text:
                    FirstGroupTextModel groupTextModel = new FirstGroupTextModel(getString(R.string.origanization_struct), "");
                    List<FirstGroupTextModel> popGroupTextList = new ArrayList<>();
                    popGroupTextList.add(groupTextModel);
                    intent.putExtra("groupTextList",
                            (Serializable) popGroupTextList);
                    intent.putExtra("selectMemList", (Serializable) selectMemList);
                    intent.putExtra("groupPosition", 3);
                    intent.putExtra("searchText", searchEdit.getText().toString());
                    intent.putExtra("searchContent", searchContent);
                    intent.putExtra("isMultiSelect", isMultiSelect);
                    if (excludeContactUidList.size() > 0) {
                        intent.putExtra(ContactSearchMoreActivity.EXTRA_EXCLUDE_SELECT, (Serializable) excludeContactUidList);
                    }
                    startActivityForResult(intent, SEARCH_MORE);
                    break;
                case R.id.ibt_back:
                    InputMethodUtils.hide(getActivity());
                    getActivity().finish();
                    break;
                case R.id.ok_text:
                    InputMethodUtils.hide(getActivity());
                    returnSearchResultData();
                    break;
                case R.id.struct_layout:
                    openContact(null);
                    break;
                case R.id.channel_group_layout:
                    showAllChannelGroup();
                    break;
                case R.id.layout:
                    if (searchEdit != null) {
                        InputMethodUtils.display(getActivity(), searchEdit);
                    }
                    break;
            }
        }
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            searchText = searchEdit.getText().toString().trim();
            if (!StringUtils.isBlank(searchText)) {
                if (popLayout.getVisibility() == View.GONE) {
                    searchArea = orginCurrentArea;
                }
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSearchTime > 500) {
                    handler.post(searchRunnbale);
                } else {
                    handler.removeCallbacks(searchRunnbale);
                    handler.postDelayed(searchRunnbale, 500);
                }
                lastSearchTime = System.currentTimeMillis();
            } else {
                lastSearchTime = 0;
                handler.removeCallbacks(searchRunnbale);
                hideSearchPop();
            }
        }

    }

    /**
     * 原界面第一个list的Adapter
     *
     * @author Administrator
     */
    private class OpenGroupListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (orginCurrentArea == SEARCH_CONTACT) {
                return openGroupContactList.size();
            } else {
                return openGroupChannelList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(
                        R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.tv_name);
                viewHolder.rightArrowImg = (ImageView) convertView
                        .findViewById(R.id.arrow_img);
                viewHolder.selectedImg = (ImageView) convertView
                        .findViewById(R.id.selected_img);
                viewHolder.photoImg = (CircleTextImageView) convertView.findViewById(R.id.img_photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SearchModel searchModel = null;
            if (orginCurrentArea == SEARCH_CONTACT)

            {
                Contact contact = openGroupContactList.get(position);
                searchModel = new SearchModel(contact);
                if (contact.getType().equals(Contact.TYPE_USER)) { // 如果通讯录是人的话就显示头像
                    viewHolder.rightArrowImg.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.rightArrowImg.setVisibility(View.VISIBLE);
                }
                viewHolder.nameText.setText(searchModel.getCompleteName());

            } else {
                viewHolder.rightArrowImg.setVisibility(View.INVISIBLE);
                searchModel = openGroupChannelList.get(position);
                viewHolder.nameText.setText(searchModel.getName());

            }
            if (searchModel != null) {
                displayImg(searchModel, viewHolder.photoImg);
                if (selectMemList.contains(searchModel)) {
                    viewHolder.selectedImg.setVisibility(View.VISIBLE);
                    viewHolder.nameText.setTextColor(Color.parseColor("#0f7bca"));
                } else {
                    viewHolder.selectedImg.setVisibility(View.INVISIBLE);
                    viewHolder.nameText.setTextColor(Color.parseColor("#030303"));
                }

            }
            return convertView;
        }

    }

    /**
     * 原界面第二个list的Adapter
     *
     * @author Administrator
     */
    private class SecondGroupListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return commonContactList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.tv_name);
                viewHolder.selectedImg = (ImageView) convertView
                        .findViewById(R.id.selected_img);
                viewHolder.photoImg = (CircleTextImageView) convertView.findViewById(R.id.img_photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SearchModel searchModel = commonContactList.get(position);
            viewHolder.nameText.setText(searchModel.getCompleteName());
            displayImg(searchModel, viewHolder.photoImg);
            if (selectMemList.contains(searchModel)) {
                viewHolder.selectedImg.setVisibility(View.VISIBLE);
                viewHolder.nameText.setTextColor(Color.parseColor("#0f7bca"));
            } else {
                viewHolder.selectedImg.setVisibility(View.INVISIBLE);
                viewHolder.nameText.setTextColor(Color.parseColor("#030303"));
            }
            return convertView;
        }

    }

    /**
     * pop页面list的公共Adapter
     *
     * @author Administrator
     */
    private class popAdapter extends BaseAdapter {
        private int groupPosition;

        public popAdapter(int groupPosition) {
            // TODO Auto-generated constructor stub
            this.groupPosition = groupPosition;
        }

        @Override
        public int getCount() { // 当筛选的结果比较多时先默认显示其中的三项
            // TODO Auto-generated method stub
            if (groupPosition == 2) {
                return getListDisplayCount(searchChannelGroupList);
            } else {
                return getListDisplayCount(searchContactList);
            }
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(
                        R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.tv_name);
                viewHolder.selectedImg = (ImageView) convertView
                        .findViewById(R.id.selected_img);
                viewHolder.photoImg = (CircleTextImageView) convertView.findViewById(R.id.img_photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            convertView.setBackgroundColor(Color.parseColor("#F4F4F4"));
            SearchModel searchModel = null;
            if (groupPosition == 2) {
                searchModel = searchChannelGroupList.get(position);
            } else {
                Contact contact = searchContactList.get(position);
                searchModel = new SearchModel(contact);

            }
            displayImg(searchModel, viewHolder.photoImg);
            viewHolder.nameText.setText(searchModel.getCompleteName());
            if (selectMemList.contains(searchModel)) {
                viewHolder.selectedImg.setVisibility(View.VISIBLE);
            } else {
                viewHolder.selectedImg.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

    }

    /**
     * 第一个group title中list的adapter
     *
     * @author Administrator
     */
    public class GroupTitleAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private boolean isPopTitle = false;
        private int groupPosition;
        private MyItemClickListener mItemClickListener;

        public GroupTitleAdapter() {

        }

        public GroupTitleAdapter(boolean isPopTitle, int groupPosition) {
            this.isPopTitle = isPopTitle;
            this.groupPosition = groupPosition;
        }

        @Override
        public int getItemCount() {
            // TODO Auto-generated method stub
            if (isPopTitle) {
                if (groupPosition == 2) {
                    return popSecondGroupTextList.size();
                } else {
                    return 1;
                }

            } else {
                return openGroupTextList.size();
            }

        }

        @Override
        public void onBindViewHolder(MyViewHolder arg0, int arg1) {
            // TODO Auto-generated method stub
            int count = getItemCount();
            if (isPopTitle) {
                if (groupPosition == 2) {
                    arg0.titleText.setText(popSecondGroupTextList.get(arg1)
                            .getName());
                } else {
                    arg0.titleText.setText(R.string.origanization_struct);
                }

                if (count > 1) {
                    if (arg1 != count - 1) {
                        arg0.titleImg.setVisibility(View.VISIBLE);
                        arg0.titleText
                                .setTextColor(Color.parseColor("#018DD4"));
                    } else {
                        arg0.titleImg.setVisibility(View.GONE);
                        arg0.titleText
                                .setTextColor(Color.parseColor("#666666"));
                    }
                } else {
                    arg0.titleText.setTextColor(Color.parseColor("#666666"));
                    arg0.titleImg.setVisibility(View.GONE);
                }

            } else {
                arg0.titleText.setText(openGroupTextList.get(arg1).getName());
                if (arg1 != count - 1) {
                    arg0.titleImg.setVisibility(View.VISIBLE);
                    arg0.titleText.setTextColor(Color.parseColor("#018DD4"));
                } else {
                    arg0.titleImg.setVisibility(View.GONE);
                    arg0.titleText.setTextColor(Color.parseColor("#666666"));
                }
            }

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
            // TODO Auto-generated method stub
            LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = mInflater.inflate(R.layout.contact_header_item_view,
                    arg0, false);
            // view.setBackgroundColor(Color.RED);
            MyViewHolder viewHolder = new MyViewHolder(view, mItemClickListener);
            return viewHolder;
        }

        /**
         * 设置Item点击监听
         *
         * @param listener
         */
        public void setOnItemClickListener(MyItemClickListener listener) {
            this.mItemClickListener = listener;
        }

    }


}