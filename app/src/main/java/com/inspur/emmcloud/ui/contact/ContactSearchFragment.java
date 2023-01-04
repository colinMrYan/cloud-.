package com.inspur.emmcloud.ui.contact;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ListViewUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.FlowLayout;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.baselib.widget.MaxHeightScrollView;
import com.inspur.emmcloud.baselib.widget.NoHorScrollView;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.util.AppTabUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.ContactClickMessage;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.FirstGroupTextModel;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.CommonContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yufuchang on 2018/6/7.
 */

public class ContactSearchFragment extends ContactSearchBaseFragment {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MULTI_SELECT = "isMulti_select";
    public static final String EXTRA_TYPE = "select_content";
    public static final String EXTRA_CREATE_NEW_GROUP = "create_new_group";// 创建群聊
    // 从单聊详情页点击+号创建群聊
    public static final String EXTRA_CREATE_NEW_GROUP_FROM_DIRECT = "create_new_group_from_direct";
    public static final String EXTRA_CREATE_NEW_GROUP_UID_DIRECT = "create_new_group_uid_direct";
    public static final String EXTRA_CONTAIN_ME = "isContainMe";
    public static final String EXTRA_HAS_SELECT = "hasSearchResult";
    public static final String EXTRA_EXCLUDE_SELECT = "excludeContactUidList";
    public static final String EXTRA_LIMIT = "select_limit";
    public static final String EXTRA_SHOW_COMFIRM_DIALOG = "show_sure_dialog";
    public static final String EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE = "show_sure_dialog_with_message";
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_CHANNELGROUP = 1;
    private static final int SEARCH_CONTACT = 2;
    private static final int SEARCH_NOTHIING = 4;
    private static final int REQUEST_SEARCH_MORE = 5;
    @BindView(R.id.flowlayout)
    FlowLayout flowLayout;
    @BindView(R.id.tv_header)
    TextView headerText;
    @BindView(R.id.tv_tab_header)
    TextView tabHeaderText;
    @BindView(R.id.ibt_back)
    ImageButton backImgBtn;
    @BindView(R.id.origin_layout)
    LinearLayout originLayout; // 进入后看到的页面
    @BindView(R.id.origin_all_layout)
    RelativeLayout originAllLayout;// 进入后看到的界面和打开某项后的list的layout
    @BindView(R.id.ok_text)
    TextView okText;
    @BindView(R.id.open_group_layout)
    LinearLayout openGroupLayout;// 打开某项后的layout
    @BindView(R.id.open_title_list)
    RecyclerView openGroupTitleListView; // 第一组 通讯录导航列表
    @BindView(R.id.second_title_text)
    TextView secondTitleText;
    @BindView(R.id.open_first_group_list)
    ListView openGroupListView; // 第一组数据
    @BindView(R.id.second_group_list)
    ScrollViewWithListView secondGroupListView;// 第二组数据
    @BindView(R.id.search_edit_layout)
    MaxHeightScrollView searchEditLayout;
    @BindView(R.id.pop_second_group_layout)
    LinearLayout popSecondGroupLayout;
    @BindView(R.id.pop_third_group_layout)
    LinearLayout popThirdGroupLayout;
    @BindView(R.id.pop_second_group_list)
    ListView popSecondGroupListView;
    @BindView(R.id.pop_third_group_list)
    ListView popThirdGroupListView;
    @BindView(R.id.pop_second_group_more_text)
    TextView popSecondGroupMoreText;
    @BindView(R.id.pop_third_group_more_text)
    TextView popThirdGroupMoreText;
    @BindView(R.id.pop_layout)
    NoHorScrollView popLayout;
    @BindView(R.id.rl_select_all)
    RelativeLayout selectAllLayout;
    @BindView(R.id.iv_select_all)
    ImageView selectAllImg;
    @BindView(R.id.pop_second_title_list)
    RecyclerView popSecondGroupTitleListView; // 第二组 群组导航列表
    @BindView(R.id.pop_third_title_list)
    RecyclerView popThirdGroupTitleListView; // 第三组 通讯录导航列表
    @BindView(R.id.struct_layout)
    RelativeLayout structLayout;
    @BindView(R.id.channel_group_layout)
    RelativeLayout channelGroupLayout;
    @BindView(R.id.title_all_text)
    TextView titleAllTv;

    private boolean isSearchSingle = false; // 判断是否搜索单一项
    private boolean isContainMe = false; // 搜索结果是否可以包含自己
    private boolean isMultiSelect = false;
    private int searchContent = -1;
    private EditText searchEdit;
    private List<SearchModel> commonContactList = new ArrayList<>();
    private List<Contact> openGroupContactList = new ArrayList<>();
    private List<SearchModel> openGroupChannelList = new ArrayList<>();
    private List<SearchModel> selectMemList = new ArrayList<>();
    private List<FirstGroupTextModel> openGroupTextList = new ArrayList<>();
    private MyTextWatcher myTextWatcher;
    private GroupTitleAdapter groupTitleAdapter;
    private OpenGroupListAdapter openGroupAdapter;
    private SecondGroupListAdapter secondGroupListAdapter;
    private int originCurrentArea = 0; // orgin页面目前的搜索模式
    private int searchArea = 0; // 搜索范围
    private String title;
    private PopAdapter popSecondGroupAdapter;
    private PopAdapter popThirdGroupAdapter;
    private List<FirstGroupTextModel> popSecondGroupTextList = new ArrayList<>();
    private List<FirstGroupTextModel> popThirdGroupTextList = new ArrayList<>();
    private GroupTitleAdapter popSecondGroupTitleAdapter;
    private GroupTitleAdapter popThirdGroupTitleAdapter;
    private Runnable searchRunnable;
    private String searchText;
    private long lastSearchTime = 0L;
    private List<SearchModel> excludeSearchModelList = new ArrayList<>();//显示灰色选中
    private long lastBackTime;
    private int selectLimit = 5000;
    private List<SearchModel> currentSelectAllSearchModelList = new ArrayList<>();
    private boolean isShowConfirmDialog = false;
    private String sureDialogMessage;
    private View view;
    private ScrollView searchOuterScroller;
    private boolean isCreateGroup = false;// 来自创建群聊
    private boolean isCreateGroupFromDirect = false;// 来自单聊详情页，创建群聊
    private String fromDirectUid;// 单聊时对方的uid
    private JSONObject searchResultObj;
    private int mMultiMessageType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_contact_search, container, false);
        unbinder = ButterKnife.bind(this, view);
        setFragmentStatusBarCommon();
        getIntentData();
        initView();
        initSearchRunnable();
        adjustListHeight();
        return view;
    }

    private void adjustListHeight() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            searchOuterScroller = view.findViewById(R.id.contact_search_outer_scroller);
            searchOuterScroller.post(new Runnable() {


                @Override
                public void run() {
                    if (openGroupListView == null) {
                        return;
                    }
                    int selectAllLayoutHeight = 0;
                    if (View.VISIBLE == selectAllLayout.getVisibility()) {
                        selectAllLayoutHeight = selectAllLayout.getHeight();
                    }
                    int scrollerHeight = searchOuterScroller.getHeight();
                    int searchEditLayoutHeight = searchEditLayout.getHeight();
                    int titleAllTvHeight = titleAllTv.getHeight();
                    int openGroupListViewHeight = scrollerHeight - searchEditLayoutHeight - titleAllTvHeight - selectAllLayoutHeight - 3;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, openGroupListViewHeight);
                    openGroupListView.setLayoutParams(params);
                    openGroupListView.requestLayout();
                }
            });
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setFragmentStatusBarCommon();
        }
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
                    openGroupChannelList = Conversation.conversationList2SearchModelList(ConversationCacheUtils
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
            isCreateGroup = intent.getExtras().getBoolean(EXTRA_CREATE_NEW_GROUP);
            isCreateGroupFromDirect = intent.getExtras().getBoolean(EXTRA_CREATE_NEW_GROUP_FROM_DIRECT);
            fromDirectUid = intent.getExtras().getString(EXTRA_CREATE_NEW_GROUP_UID_DIRECT);
            if (intent.hasExtra(EXTRA_LIMIT)) {
                selectLimit = intent.getIntExtra(EXTRA_LIMIT, 5000);
            }
            title = intent.getExtras().getString(EXTRA_TITLE);
            isMultiSelect = intent.getExtras().getBoolean(EXTRA_MULTI_SELECT);
            if (selectLimit == 1) {
                isMultiSelect = false;
            }
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
                List<String> excludeContactUidList = (List<String>) intent.getExtras().getSerializable(EXTRA_EXCLUDE_SELECT);
                List<Contact> excludeContactList = Contact.contactUserList2ContactList(ContactUserCacheUtils.getContactUserListById(excludeContactUidList));
                if (excludeContactList != null && excludeContactList.size() > 0) {
                    for (int i = 0; i < excludeContactList.size(); i++) {
                        excludeSearchModelList.add(excludeContactList.get(i).contact2SearchModel());
                    }
                }
            }
            if (intent.hasExtra(EXTRA_SHOW_COMFIRM_DIALOG)) {
                isShowConfirmDialog = true;
            }
            if ((intent.hasExtra(EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE))) {
                sureDialogMessage = intent.getExtras().getString(EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE);
            } else {
                sureDialogMessage = "";
            }
        }
        initSearchArea();
        if (searchContent == SEARCH_CHANNELGROUP) {
            structLayout.setVisibility(View.GONE);
        }
        if (searchContent == SEARCH_CONTACT) {
            channelGroupLayout.setVisibility(View.GONE);
        }
        if (searchContent == SEARCH_NOTHIING) {
            originCurrentArea = SEARCH_ALL;
        }
    }

    /**
     * 初始化搜索区域
     */
    private void initSearchArea() {
        // TODO Auto-generated method stub
        originCurrentArea = searchContent;
        if (originCurrentArea == SEARCH_NOTHIING) {
            originCurrentArea = SEARCH_ALL;
        }
        isSearchSingle = false;
    }

    private void initView() {
        // TODO Auto-generated method stub
        boolean isFromTab = getActivity().getClass().getSimpleName().equals(IndexActivity.class.getSimpleName());
        backImgBtn.setVisibility(isFromTab ? View.GONE : View.VISIBLE);
        headerText.setVisibility(isFromTab ? View.GONE : View.VISIBLE);
        tabHeaderText.setVisibility(isFromTab ? View.VISIBLE : View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        openGroupTitleListView.setLayoutManager(layoutManager);

        myTextWatcher = new MyTextWatcher();

        // 单选时隐藏输入框或者不选时
        if (!isMultiSelect || searchContent == SEARCH_NOTHIING) {
            okText.setVisibility(View.GONE);
        }
        flowAddEdit();
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

    }


    /**
     * 初始化第二组的数据
     */
    private void initSecondGroup() {
        secondTitleText.setText(getString(R.string.recently_used));
        commonContactList = CommonContactCacheUtils.getCommonContactList(
                getActivity().getApplicationContext(), 5, searchContent, null);
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
                SearchModel searchModel = commonContactList.get(position);
                if (!isHaveStaticSearchModel(searchModel)) {
                    showRecentChannelOperationDlg(position);
                }
                return true;
            }

        });
    }

    private boolean isHaveStaticSearchModel(SearchModel searchModel) {
        return excludeSearchModelList.contains(searchModel);
    }

    private void showRecentChannelOperationDlg(final int position) {
        new CustomDialog.MessageDialogBuilder(getActivity())
                .setMessage(R.string.if_delect_current_item)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SearchModel searchModel = commonContactList.get(position);
                        CommonContactCacheUtils.delectCommonContact(getActivity().getApplicationContext(), searchModel);
                        commonContactList.remove(position);
                        secondGroupListAdapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    /**
     * 群组或通讯录打开浏览页面
     */
    private void displayOpenLayout() {
        if (openGroupLayout.getVisibility() != View.VISIBLE) {
            originLayout.setVisibility(View.GONE);
            openGroupLayout.setVisibility(View.VISIBLE);
            groupTitleAdapter = new GroupTitleAdapter();
            openGroupTitleListView.setAdapter(groupTitleAdapter);
            if (openGroupTextList.size() > 0) {
                openGroupTitleListView.smoothScrollToPosition(openGroupTextList
                        .size() - 1);
            }
            groupTitleAdapter.setOnItemClickListener(new MyItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    clickGroupTitle(position);
                }
            });
            openGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    if (originCurrentArea == SEARCH_CONTACT) {
                        Contact contact = openGroupContactList.get(position);
                        changeMembers(contact.contact2SearchModel());
                    } else if (originCurrentArea == SEARCH_CHANNELGROUP) {
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
        currentSelectAllSearchModelList.clear();
        //判断全选按钮是否显示
        boolean isSelectAllLayoutShow = searchContent != SEARCH_NOTHIING && isMultiSelect == true;
        if (isSelectAllLayoutShow) {
            boolean isContainUser = false;
            if (originCurrentArea == SEARCH_CONTACT) {
                for (Contact contact : openGroupContactList) {
                    if (contact.getType().equals(SearchModel.TYPE_USER)) {
                        isContainUser = true;
                        break;
                    }

                }

            }
            isSelectAllLayoutShow = isContainUser;
        }
        selectAllLayout.setVisibility(isSelectAllLayoutShow ? View.VISIBLE : View.GONE);
        selectAllImg.setSelected(false);
        selectAllImg.setImageResource(R.drawable.ic_select_no);
        if (View.VISIBLE == selectAllLayout.getVisibility()) {
            adjustListHeight();
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
            openGroupLayout.setVisibility(View.GONE);
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
        openGroupContactList = ContactOrgCacheUtils.getChildContactList(id, null);
        originCurrentArea = SEARCH_CONTACT;
        isSearchSingle = true;
        displayOpenLayout();
    }

    /**
     * 显示所有的群组
     */
    private void showAllChannelGroup() {
        // TODO Auto-generated method stub
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            openGroupChannelList = ChannelGroup.channelGroupList2SearchModelList(ChannelGroupCacheUtils
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
            openGroupChannelList = Conversation.conversationList2SearchModelList(conversationList);
        }

        openGroupTextList.add(new FirstGroupTextModel(getString(R.string.all),
                ""));
        openGroupTextList.add(new FirstGroupTextModel(
                getString(R.string.channel_group), ""));
        originCurrentArea = SEARCH_CHANNELGROUP;
        isSearchSingle = true;
        displayOpenLayout();
    }


    private void changeMembers(SearchModel searchModel) {
        changeMembers(searchModel, false);
    }

    /**
     * 更改SearchModel选中状态或者打开SearchModel
     *
     * @param searchModel
     * @param isSelectImgClick 是否是点击选择按钮进入到此方法
     */
    private void changeMembers(SearchModel searchModel, boolean isSelectImgClick) {
        if (searchModel != null) {
            if (searchContent == SEARCH_NOTHIING || (!isSelectImgClick && searchModel.getType().equals(SearchModel.TYPE_STRUCT) && !selectMemList.contains(searchModel))) {
                checkInfoOrEnterChannel(searchModel);
                return;
            }

            if (isHaveStaticSearchModel(searchModel)) {
                return;
            }
            if (!selectMemList.contains(searchModel)) {
                if (selectMemList.size() < selectLimit) {
                    selectMemList.add(searchModel);
                    CommonContactCacheUtils.saveCommonContact(
                            getActivity().getApplicationContext(), searchModel);
                    if (!isMultiSelect) {
                        returnSearchResultData();
                    } else {
                        notifyFlowLayoutDataChange();
                    }

                } else {
                    ToastUtils.show(MyApplication.getInstance(), R.string.contact_select_limit_warning);
                }

            } else {
                selectMemList.remove(searchModel);
                notifyFlowLayoutDataChange();
                notifySelectAllStatus(searchModel);
            }

        }
    }

    /**
     * 当删除选中人员时，判断是否取消全选状态
     *
     * @param removeSearchModel
     */
    private void notifySelectAllStatus(SearchModel removeSearchModel) {
        if (openGroupLayout.getVisibility() == View.VISIBLE && selectAllImg.getVisibility() == View.VISIBLE && selectAllImg.isSelected()) {
            if (currentSelectAllSearchModelList.contains(removeSearchModel)) {
                selectAllImg.setImageResource(R.drawable.ic_select_no);
                selectAllImg.setSelected(false);
            }

        }
    }

    /**
     * 设置组织架构人员是否全选
     */
    private void setSelectAllMembers() {
        List<SearchModel> searchModelList = null;
        currentSelectAllSearchModelList.clear();
        boolean isSetSelectAll = !selectAllImg.isSelected();
        if (isSetSelectAll) {
            selectAllImg.setImageResource(R.drawable.ic_select_yes);
            selectAllImg.setSelected(true);
        } else {
            selectAllImg.setImageResource(R.drawable.ic_select_no);
            selectAllImg.setSelected(false);
        }

        if (originCurrentArea == SEARCH_CONTACT) {
            searchModelList = Contact.contactList2SearchModelList(openGroupContactList);
        } else {
            searchModelList = openGroupChannelList;
        }
        for (SearchModel searchModel : searchModelList) {
            if (isHaveStaticSearchModel(searchModel)) {
                continue;
            }
            if (isSetSelectAll) {
                if (!selectMemList.contains(searchModel)) {
                    selectMemList.add(searchModel);
                }
                currentSelectAllSearchModelList.add(searchModel);
            } else {
                selectMemList.remove(searchModel);
                currentSelectAllSearchModelList.remove(searchModel);
            }
        }
        notifyFlowLayoutDataChange();
    }

    /**
     * 查看信息
     *
     * @param searchModel
     */
    private void checkInfoOrEnterChannel(final SearchModel searchModel) {
        // TODO Auto-generated method stub

        if (searchModel.getId().equals("null")) {
            ToastUtils.show(getActivity().getApplicationContext(), R.string.cannot_view_info);
            return;
        }
        switch (searchModel.getType()) {
            case SearchModel.TYPE_USER:
                CommonContactCacheUtils.saveCommonContact(getActivity().getApplicationContext(), searchModel);
                startActivity(new Intent(getActivity(), UserInfoActivity.class).putExtra("uid", searchModel.getId()));
                break;
            case SearchModel.TYPE_STRUCT:
                openContact(Contact.SearchModel2Contact(searchModel));
                break;
            case SearchModel.TYPE_GROUP:
            case SearchModel.TYPE_TRANSFER:
                startActivity(new Intent(getActivity().getApplicationContext(),
                        WebServiceRouterManager.getInstance().isV0VersionChat() ?
                                ChannelV0Activity.class : ConversationActivity.class).putExtra("title", searchModel.getName()).putExtra("cid", searchModel.getId()).putExtra("channelType", searchModel.getType()));
                break;
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
            params.leftMargin = DensityUtil.dip2px(5);
            params.rightMargin = DensityUtil.dip2px(3);
            params.topMargin = DensityUtil.dip2px(MyApplication.getInstance(), 2);
            params.bottomMargin = params.topMargin;
            searchResultText.setLayoutParams(params);
            int paddingTop = DensityUtil.dip2px(getActivity().getApplicationContext(), 1);
            searchResultText.setPadding(0, paddingTop, 0, paddingTop);
            searchResultText.setGravity(Gravity.CENTER);
            searchResultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            searchResultText.setTextColor(searchModel.getType().equals(SearchModel.TYPE_STRUCT) ? Color.parseColor("#0C6FB7") : Color.parseColor("#36A5F6"));
            searchResultText.setText(selectMemList.get(i).getName() + ",");
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
            @Override
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
            params.topMargin = DensityUtil.dip2px(2);
            params.bottomMargin = params.topMargin;
            int piddingTop = DensityUtil.dip2px(1);
            int piddingRight = DensityUtil.dip2px(5);
            searchEdit.setPadding(piddingRight, piddingTop, piddingRight, piddingTop);
            searchEdit.setLayoutParams(params);
            searchEdit.setSingleLine(true);
            searchEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            searchEdit.setBackground(null);
            searchEdit.setHint(getString(R.string.msg_key_search_member));
            searchEdit.setTextColor(DarkUtil.getTextColor());
            searchEdit.setHintTextColor(Color.parseColor("#666666"));

            searchEdit.addTextChangedListener(myTextWatcher);
        }

        if (searchEdit.getParent() == null) {
            flowLayout.addView(searchEdit);
        }
    }

    private void initSearchRunnable() {
        searchRunnable = new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switch (searchArea) {
                            case SEARCH_ALL:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    threadSearchChannelGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    threadSearchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }

                                threadSearchContactList = ContactUserCacheUtils.getSearchContact(searchText, null, 4);
                                break;
                            case SEARCH_CHANNELGROUP:
                                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                                    threadSearchChannelGroupList = ChannelGroupCacheUtils
                                            .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                                    searchText);
                                } else {
                                    threadSearchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                                }
                                break;
                            case SEARCH_CONTACT:
                                threadSearchContactList = ContactUserCacheUtils.getSearchContact(searchText, null, 4);
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
        searchResultObj = new JSONObject();
        //将选中的组织转化成组织下的所有人员
        convertContactOrg2ContactUser();
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
        if (isShowConfirmDialog) {
            String searchResult = searchResultObj.toString();
            showConfirmDialog(searchResult, selectMemList.get(0), selectMemList.get(0).getType().equals(SearchModel.TYPE_GROUP));
        } else {
            // 新建群组
            if (isCreateGroup) {
                // 创建群组时，群聊的人员列表（包括用户本人）
                checkIsExistGroupChat();
            } else if (isCreateGroupFromDirect) {
                // 从单聊聊天详情页新建群组
                checkIsExistGroupChat();
            } else {
                setResultAndFinish();
            }
        }
    }

    /**
     * 创建群聊时检验是否已存在相同群成员群组
     */
    private void checkIsExistGroupChat() {
        List<String> createGroupMemberList = new ArrayList<>();
        for (int j = 0; j < selectMemList.size(); j++) {
            createGroupMemberList.add(selectMemList.get(j).getId());
        }
        createGroupMemberList.add(MyApplication.getInstance().getUid());
        if (isCreateGroupFromDirect) {
            createGroupMemberList.add(fromDirectUid);
        }
        if (selectMemList != null && selectMemList.size() > 0) {
            if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                List<ChannelGroup> allChannelGroupList = ChannelGroupCacheUtils.getAllChannelGroupList(MyApplication.getInstance());
                for (int i = 0; i < allChannelGroupList.size(); i++) {
                    ChannelGroup channelGroup = allChannelGroupList.get(i);
                    ArrayList<String> memberList = channelGroup.getMemberList();
                    // 存在选了一个人，且之前存在两个人的群组，满足了下面条件
                    if (memberList == null || memberList.size() == 2 || memberList.size() != createGroupMemberList.size()) {
                        continue;
                    }
                    if (memberList.containsAll(createGroupMemberList)) {
                        // 群聊重复
                        showAddGroupWarningDlg();
                        return;
                    }
                }
            } else {
                List<Conversation> conversationList = ConversationCacheUtils.getConversationList(MyApplication.getInstance());
                for (Conversation conversation : conversationList) {
                    ArrayList<String> uidList = conversation.getMemberList();
                    if (uidList == null || uidList.size() == 2 || uidList.size() != createGroupMemberList.size()) {
                        continue;
                    }
                    if (uidList.containsAll(createGroupMemberList)) {
                        // 群聊重复
                        showAddGroupWarningDlg();
                        return;
                    }
                }
            }
        }
        setResultAndFinish();
    }

    // 已存在相同成员的群组时，提示用户
    private void showAddGroupWarningDlg() {
        new CustomDialog.MessageDialogBuilder(getActivity())
                .setMessage(getString(R.string.add_same_group_warning_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setResultAndFinish();
                    }
                })
                .show();
    }

    // 设置setResult结果并返回
    private void setResultAndFinish() {
        String searchResult = searchResultObj.toString();
        Intent intent = new Intent();
        intent.putExtra("searchResult", searchResult);
        intent.putExtra("selectMemList", (Serializable) selectMemList);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    private void convertContactOrg2ContactUser() {
        List<String> contactOrgIdList = new ArrayList<>();
        if (selectMemList.size() > 0) {
            Iterator<SearchModel> it = selectMemList.iterator();
            while (it.hasNext()) {
                SearchModel searchModel = it.next();
                if (searchModel.getType().equals(SearchModel.TYPE_STRUCT)) {
                    contactOrgIdList.add(searchModel.getId());
                    it.remove();
                }

            }
        }
        if (contactOrgIdList.size() > 0) {
            List<ContactUser> contactUserList = ContactUserCacheUtils.getContactUserListInContactOrgList(contactOrgIdList);
            selectMemList.addAll(Contact.contactUser2SearchModelList(contactUserList));
            selectMemList.removeAll(excludeSearchModelList);
        }
    }

    /**
     * 弹出分享确认框
     */
    private void showConfirmDialog(final String searchResult, final SearchModel searchModel,
                                   final boolean isGroup) {
        final MyDialog dialog = new MyDialog(getActivity(),
                R.layout.chat_out_share_sure_dialog);
        Button okBtn = dialog.findViewById(R.id.ok_btn);
        ImageViewRound userHeadImage = dialog.findViewById(R.id.iv_share_user_head);
        TextView fileNameText = dialog.findViewById(R.id.tv_share_file_name);
        TextView userNameText = dialog.findViewById(R.id.tv_share_user_name);
        dialog.setCancelable(false);
        displayImg(searchModel, userHeadImage);
        okBtn.setText(getString(R.string.ok));
        userNameText.setText(searchModel.getName());
        fileNameText.setText(sureDialogMessage);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra("searchResult", searchResult);
                intent.putExtra("selectMemList", (Serializable) selectMemList);
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            }
        });
        Button cancelBt = dialog.findViewById(R.id.cancel_btn);
        cancelBt.setText(getString(R.string.cancel));
        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                selectMemList.clear();
                notifyFlowLayoutDataChange();

            }
        });
        dialog.show();
    }

    private void initPopView() {
        // TODO Auto-generated method stub
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

        popSecondGroupAdapter = new PopAdapter(2);
        popThirdGroupAdapter = new PopAdapter(3);
        popSecondGroupListView.setAdapter(popSecondGroupAdapter);
        popThirdGroupListView.setAdapter(popThirdGroupAdapter);

        popSecondGroupListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        changeMembers(searchChannelGroupList.get(position));

                    }
                });
        popThirdGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Contact contact = searchContactList.get(position);
                changeMembers(contact.contact2SearchModel());

            }
        });
    }

    @Override
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
            popSecondGroupLayout.setVisibility(View.VISIBLE);
            popThirdGroupLayout.setVisibility(View.VISIBLE);
            refreshListView(popSecondGroupListView, popSecondGroupAdapter);
            refreshListView(popThirdGroupListView, popThirdGroupAdapter);
        } else if (!isSearchSingle) {
            if (searchArea == SEARCH_CONTACT) {
                popThirdGroupMoreText.setVisibility(searchContactList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGroupLayout.setVisibility(View.GONE);
                popThirdGroupLayout.setVisibility(View.VISIBLE);
                refreshListView(popThirdGroupListView, popThirdGroupAdapter);
            } else if (searchArea == SEARCH_CHANNELGROUP) {
                popSecondGroupMoreText.setVisibility(searchChannelGroupList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGroupLayout.setVisibility(View.VISIBLE);
                popThirdGroupLayout.setVisibility(View.GONE);
                popSecondGroupAdapter.notifyDataSetChanged();
            }
            refreshListView(popSecondGroupListView, popSecondGroupAdapter);
        } else {
            if (searchArea == SEARCH_CONTACT) {
                popThirdGroupMoreText.setVisibility(searchContactList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGroupLayout.setVisibility(View.GONE);
                popThirdGroupLayout.setVisibility(View.VISIBLE);
                refreshListView(popThirdGroupListView, popThirdGroupAdapter);
            } else {
                popSecondGroupMoreText.setVisibility(searchChannelGroupList.size() > 3 ? View.VISIBLE : View.GONE);
                popSecondGroupLayout.setVisibility(View.VISIBLE);
                popThirdGroupLayout.setVisibility(View.GONE);
                refreshListView(popSecondGroupListView, popSecondGroupAdapter);
            }
        }
        // notifyPopFirstGroupText(openGroupTextList);
        if (popSecondGroupLayout.getVisibility() == View.VISIBLE) {
            notifyPopSecondGroupText(openGroupTextList);
        }
        if (popThirdGroupLayout.getVisibility() == View.VISIBLE) {
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
        isSearchSingle = openGroupLayout.getVisibility() == View.VISIBLE;
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
        if ((resultCode == RESULT_OK) && (requestCode == REQUEST_SEARCH_MORE)) {
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
    private void displayImg(SearchModel searchModel, ImageViewRound photoImg) {
        Integer defaultIcon = null; // 默认显示图标
        String icon = null;
        String type = searchModel.getType();
        photoImg.setTag("");
        if (type.equals(SearchModel.TYPE_GROUP)) {
            defaultIcon = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.design3_icon_group_default);
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + searchModel.getId() + "_100.png1");
            if (file.exists()) {
                icon = "file://" + file.getAbsolutePath();
                ImageDisplayUtils.getInstance().displayImageNoCache(photoImg, icon, defaultIcon);
            } else {
                photoImg.setImageResource(defaultIcon);
            }
        } else if (type.equals(SearchModel.TYPE_STRUCT)) {
            photoImg.setImageResource(R.drawable.design3_icon_contact_struct);
        } else {
            defaultIcon = ResourceUtils.getResValueOfAttr(getActivity(), R.attr.design3_icon_person_default);
            if (!searchModel.getId().equals("null")) {
                icon = APIUri.getChannelImgUrl(MyApplication.getInstance(), searchModel.getId());
            }
            photoImg.setTag(icon);
            ImageDisplayUtils.getInstance().displayImageByTag(photoImg, icon, defaultIcon);
        }

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
        return openGroupLayout.getVisibility() == View.VISIBLE && openGroupTextListSize > 1;
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
                        new OnCreateDirectConversationListener() {
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

    /**
     * 中文名+英文名
     *
     * @return
     */
    public String getCompleteName(SearchModel searchModel) {
        String completeName = searchModel.getName();
        String globalName = null;
        if (searchModel.getType().equals(SearchModel.TYPE_USER)) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(searchModel.getId());
            if (contactUser != null) {
                globalName = contactUser.getNameGlobal();
            }
        } else if (searchModel.getType().equals(SearchModel.TYPE_STRUCT)) {
            ContactOrg contactOrg = ContactOrgCacheUtils.getContactOrg(searchModel.getId());
            if (contactOrg != null) {
                globalName = contactOrg.getNameGlobal();
            }
        }
//        if (!StringUtils.isBlank(globalName)) {
//            completeName = completeName + "（" + globalName + "）";
//        }
        return completeName;
    }

    @OnClick({R.id.pop_second_group_more_text, R.id.ibt_back, R.id.ok_text, R.id.struct_layout, R.id.channel_group_layout, R.id.layout, R.id.pop_third_group_more_text, R.id.rl_select_all})
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
                if (excludeSearchModelList.size() > 0) {
                    intent.putExtra(ContactSearchMoreActivity.EXTRA_EXCLUDE_SEARCH_MODEL_LIST, (Serializable) excludeSearchModelList);
                }
                startActivityForResult(intent, REQUEST_SEARCH_MORE);
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
                if (excludeSearchModelList.size() > 0) {
                    intent.putExtra(ContactSearchMoreActivity.EXTRA_EXCLUDE_SEARCH_MODEL_LIST, (Serializable) excludeSearchModelList);
                }
                startActivityForResult(intent, REQUEST_SEARCH_MORE);
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

            case R.id.rl_select_all:
                setSelectAllMembers();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
        EventBus.getDefault().unregister(this);
    }

    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class ViewHolder {
        TextView nameText;
        TextView descTv;
        ImageViewRound photoImg;
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
                    searchArea = originCurrentArea;
                }
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
            if (originCurrentArea == SEARCH_CONTACT) {
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
                convertView = mInflater.inflate(R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.descTv = convertView.findViewById(R.id.tv_desc);
                viewHolder.rightArrowImg = (ImageView) convertView.findViewById(R.id.arrow_img);
                viewHolder.selectedImg = (ImageView) convertView.findViewById(R.id.selected_img);
                viewHolder.photoImg = (ImageViewRound) convertView.findViewById(R.id.img_photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final SearchModel searchModel = originCurrentArea == SEARCH_CONTACT ? openGroupContactList.get(position).contact2SearchModel() : openGroupChannelList.get(position);
            if (originCurrentArea == SEARCH_CONTACT) {
                if (searchModel.getType().equals(Contact.TYPE_USER) || selectMemList.contains(searchModel)) { // 如果通讯录是人的话就显示头像
                    viewHolder.rightArrowImg.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.rightArrowImg.setVisibility(View.VISIBLE);
                }
                viewHolder.nameText.setText(getCompleteName(searchModel));

            } else {
                viewHolder.rightArrowImg.setVisibility(View.INVISIBLE);
                viewHolder.nameText.setText(searchModel.getName());
            }
            CommunicationUtils.setUserDescText(searchModel, viewHolder.descTv, false);

            displayImg(searchModel, viewHolder.photoImg);
            if (searchContent == SEARCH_NOTHIING || !isMultiSelect) {
                viewHolder.selectedImg.setVisibility(View.GONE);
            } else {
                viewHolder.selectedImg.setVisibility(View.VISIBLE);
                if (isHaveStaticSearchModel(searchModel)) {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_not_cancel);
                } else if (selectMemList.contains(searchModel)) {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_yes);
                } else {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_no);
                }
            }
            viewHolder.selectedImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeMembers(searchModel, true);
                }
            });
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.descTv = convertView.findViewById(R.id.tv_desc);
                viewHolder.selectedImg = (ImageView) convertView.findViewById(R.id.selected_img);
                viewHolder.photoImg = (ImageViewRound) convertView.findViewById(R.id.img_photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SearchModel searchModel = commonContactList.get(position);
            viewHolder.nameText.setText(getCompleteName(searchModel));
            displayImg(searchModel, viewHolder.photoImg);
            if (searchContent == SEARCH_NOTHIING || !isMultiSelect) {
                viewHolder.selectedImg.setVisibility(View.GONE);
            } else if (searchModel.getType().equals(SearchModel.TYPE_STRUCT)) {
                viewHolder.selectedImg.setVisibility(View.GONE);
            } else {
                viewHolder.selectedImg.setVisibility(View.VISIBLE);
                if (isHaveStaticSearchModel(searchModel)) {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_not_cancel);
                } else if (selectMemList.contains(searchModel)) {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_yes);
                } else {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_no);
                }
            }
            CommunicationUtils.setUserDescText(searchModel, viewHolder.descTv, true);
            return convertView;
        }
    }

    /**
     * pop页面list的公共Adapter
     *
     * @author Administrator
     */
    private class PopAdapter extends BaseAdapter {
        private int groupPosition;

        public PopAdapter(int groupPosition) {
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
                convertView = mInflater.inflate(R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.descTv = convertView.findViewById(R.id.tv_desc);
                viewHolder.selectedImg = (ImageView) convertView.findViewById(R.id.selected_img);
                viewHolder.photoImg = (ImageViewRound) convertView.findViewById(R.id.img_photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SearchModel searchModel = null;
            if (groupPosition == 2) {
                searchModel = searchChannelGroupList.get(position);
            } else {
                Contact contact = searchContactList.get(position);
                searchModel = contact.contact2SearchModel();
            }
            displayImg(searchModel, viewHolder.photoImg);
            viewHolder.nameText.setText(getCompleteName(searchModel));
            CommunicationUtils.setUserDescText(searchModel, viewHolder.descTv, true);


            if (searchContent == SEARCH_NOTHIING || !isMultiSelect) {
                viewHolder.selectedImg.setVisibility(View.GONE);
            } else if (searchModel.getType().equals(SearchModel.TYPE_STRUCT)) {
                viewHolder.selectedImg.setVisibility(View.GONE);
            } else {
                viewHolder.selectedImg.setVisibility(View.VISIBLE);
                if (isHaveStaticSearchModel(searchModel)) {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_not_cancel);
                } else if (selectMemList.contains(searchModel)) {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_yes);
                } else {
                    viewHolder.selectedImg.setImageResource(R.drawable.ic_select_no);
                }
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
                                .setTextColor(Color.parseColor("#36A5F6"));
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
                    arg0.titleText.setTextColor(Color.parseColor("#36A5F6"));
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