package com.inspur.emmcloud.ui.contact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.FirstGroupTextModel;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils.OnCreateDirectChannelListener;
import com.inspur.emmcloud.util.privates.cache.CommonContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.ListViewUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.FlowLayout;
import com.inspur.emmcloud.widget.MaxHightScrollView;
import com.inspur.emmcloud.widget.NoHorScrollView;
import com.inspur.emmcloud.widget.WeakHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通讯录选择界面
 *
 * @author Administrator
 */
public class ContactSearchActivity extends BaseActivity {
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_CONTACT = 2;
    private static final int SEARCH_CHANNELGROUP = 1;
    private static final int SEARCH_RECENT = 3;
    private static final int SEARCH_NOTHIING = 4;
    private static final int SEARCH_MORE = 5;
    private static final int REFRESH_DATA = 6;
    private boolean isSearchSingle = false; // 判断是否搜索单一项
    private boolean isContainMe = false; // 搜索结果是否可以包含自己
    private boolean isMultiSelect = false;
    private int searchContent;
    private FlowLayout flowLayout;
    private EditText searchEdit;
    private LinearLayout originLayout; // 进入后看到的页面
    private RelativeLayout originAllLayout;// 进入后看到的界面和打开某项后的list的layout
    private LinearLayout openGroupLayou;// 打开某项后的layout
    private RecyclerView openGroupTitleListView; // 第一组 通讯录导航列表
    private TextView secondTitleText;

    private ListView openGroupListView; // 第一组数据
    private ListView secondGroupListView;// 第二组数据
    private MaxHightScrollView searchEditLayout;

    private List<SearchModel> commonContactList = new ArrayList<SearchModel>();
    private List<Contact> openGroupContactList = new ArrayList<Contact>();
    private List<ChannelGroup> openGroupChannelList = new ArrayList<ChannelGroup>();
    private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
    private List<FirstGroupTextModel> openGroupTextList = new ArrayList<FirstGroupTextModel>();
    private MyTextWatcher myTextWatcher;
    private GroupTitleAdapter groupTitleAdapter;
    private OpenGroupListAdapter openGroupAdapter;
    private SecondGroupListAdapter secondGroupListAdapter;
    private Contact rootContact;
    private int orginCurrentArea = 0; // orgin页面目前的搜索模式
    private int searchArea = 0; // 搜索范围
    private String title;
    private List<ChannelGroup> searchChannelGroupList = new ArrayList<ChannelGroup>(); // 群组搜索结果
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
    private WeakHandler handler;
    private Runnable searchRunnbale;
    private String searchText;
    private long lastSearchTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search);
        rootContact = ContactCacheUtils
                .getRootContact(ContactSearchActivity.this);
        getIntentData();
        handMessage();
        initView();
        initSearchRunnable();
    }

    /**
     * 获取从其他Activity传来的数据
     */
    private void getIntentData() {
        // TODO Auto-generated method stub
        title = getIntent().getExtras().getString("title");
        isMultiSelect = getIntent().getExtras().getBoolean("isMulti_select");
        searchContent = getIntent().getExtras().getInt("select_content");
        if (getIntent().getExtras().containsKey("isContainMe")) {
            isContainMe = true;
        }
        initSearchArea();
        if (searchContent == SEARCH_CHANNELGROUP) {
            (findViewById(R.id.struct_layout))
                    .setVisibility(View.GONE);
        }
        if (searchContent == SEARCH_CONTACT) {
            (findViewById(R.id.channel_group_layout))
                    .setVisibility(View.GONE);
        }
        if (searchContent == SEARCH_NOTHIING) {
            orginCurrentArea = SEARCH_ALL;
        }

        if (getIntent().hasExtra("hasSearchResult")) {
            selectMemList = (List<SearchModel>) getIntent().getExtras()
                    .getSerializable("hasSearchResult");
            if (selectMemList == null) {
                selectMemList = new ArrayList<SearchModel>();
            }
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
        ((TextView) findViewById(R.id.header_text)).setText(title);
        originAllLayout = (RelativeLayout) findViewById(R.id.origin_all_layout);
        originLayout = (LinearLayout) findViewById(R.id.origin_layout);
        openGroupLayou = (LinearLayout) findViewById(R.id.open_group_layout);
        openGroupTitleListView = (RecyclerView) findViewById(R.id.open_title_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        openGroupTitleListView.setLayoutManager(layoutManager);
        openGroupListView = (ListView) findViewById(R.id.open_first_group_list);
        secondGroupListView = (ListView) findViewById(R.id.second_group_list);
        secondTitleText = (TextView) findViewById(R.id.second_title_text);
        myTextWatcher = new MyTextWatcher();
        flowLayout = (FlowLayout) findViewById(R.id.flowlayout);
        TextView okText = (TextView) findViewById(R.id.ok_text);
        // 单选时隐藏输入框或者不选时
        if (!isMultiSelect || searchContent == SEARCH_NOTHIING) {
            okText.setVisibility(View.GONE);
        }
        flowAddEdit();
        searchEditLayout = (MaxHightScrollView) findViewById(R.id.search_edit_layout);
        initSecondGroup();
        initPopView();
        if (selectMemList.size() > 0) {
            notifyFlowLayoutDataChange();
        }
    }

    private void handMessage() {
        handler = new WeakHandler(ContactSearchActivity.this) {

            @Override
            protected void handleMessage(Object o, Message message) {
                switch (message.what) {
                    case REFRESH_DATA:
                        showSearchPop();
                        break;
                }
            }

        };
    }

    /**
     * 初始化第二组的数据
     */
    private void initSecondGroup() {
        // TODO Auto-generated method stub
        secondTitleText.setText(getString(R.string.recently_used));
        commonContactList = CommonContactCacheUtils.getCommonContactList(
                getApplicationContext(), 5, searchContent);
        secondGroupListAdapter = new SecondGroupListAdapter();
        secondGroupListView.setAdapter(secondGroupListAdapter);
        secondGroupListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                SearchModel searchModel = commonContactList.get(position);
                changeMembers(searchModel);
            }
        });
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
            openGroupListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    if (orginCurrentArea == SEARCH_CONTACT) {
                        Contact contact = openGroupContactList.get(position);
                        if (contact.getType().equals("user")) {
                            changeMembers(new SearchModel(contact));
                        } else {
                            openContact(contact);
                        }
                    } else if (orginCurrentArea == SEARCH_CHANNELGROUP) {
                        ChannelGroup channelGroup = openGroupChannelList
                                .get(position);
                        changeMembers(new SearchModel(channelGroup));
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
            currentStruct = rootContact;
        }

        if (currentStruct == null) {
            ToastUtils.show(ContactSearchActivity.this,
                    getString(R.string.contact_exception));
            return;
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
        openGroupContactList = ContactCacheUtils.getChildContactList(
                getApplicationContext(), id);
        orginCurrentArea = SEARCH_CONTACT;
        isSearchSingle = true;
        displayOpenLayout();
    }

    /**
     * 显示所有的群组
     */
    private void showAllChannelGroup() {
        // TODO Auto-generated method stub
        openGroupChannelList = ChannelGroupCacheUtils
                .getAllChannelGroupList(getApplicationContext());
        openGroupTextList.add(new FirstGroupTextModel(getString(R.string.all),
                ""));
        openGroupTextList.add(new FirstGroupTextModel(
                getString(R.string.channel_group), ""));
        orginCurrentArea = SEARCH_CHANNELGROUP;
        isSearchSingle = true;
        displayOpenLayout();
    }

    private void flowAddEdit() {
        if (searchEdit == null) {
            searchEdit = new EditText(this);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
                    getApplicationContext(), 45));
            int paddingRight = DensityUtil.dip2px(getApplicationContext(), 80);
            searchEdit.setPadding(0, 0, paddingRight, 0);
            searchEdit.setLayoutParams(params);
            searchEdit.setSingleLine(true);
            searchEdit.setHint(getString(R.string.seach_blank));
            searchEdit.setGravity(Gravity.CENTER_VERTICAL);
            searchEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            // searchEdit.setTextSize(getResources().getDimension(R.dimen.content_title_textsize));
            searchEdit.setBackgroundDrawable(null);
            searchEdit.addTextChangedListener(myTextWatcher);
        }
        flowLayout.addView(searchEdit);
    }

    /**
     * 添加联系人
     */
    private void changeMembers(SearchModel searchModel) {
        if (searchModel != null) {
            if (searchContent == SEARCH_NOTHIING) {
                CommonContactCacheUtils.saveCommonContact(
                        getApplicationContext(), searchModel);
                checkInfoOrEnterChannel(searchModel);
                return;
            }
            if (!selectMemList.contains(searchModel)) {
                selectMemList.add(searchModel);
                CommonContactCacheUtils.saveCommonContact(
                        getApplicationContext(), searchModel);
                if (!isMultiSelect) {
                    returnSearchResultData();
                    return;
                }
                notifyFlowLayoutDataChange();
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
            ToastUtils.show(getApplicationContext(), R.string.cannot_view_info);
            return;
        }
        CommonContactCacheUtils.saveCommonContact(getApplicationContext(),
                searchModel);
        if (type.equals("USER")) {
            intent.putExtra("uid", id);
            intent.setClass(getApplicationContext(), UserInfoActivity.class);
            startActivity(intent);
        } else {
            intent.setClass(getApplicationContext(), ChannelActivity.class);
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
            TextView searchResultText = new TextView(this);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.rightMargin = DensityUtil.dip2px(getApplicationContext(), 5);
            params.topMargin = DensityUtil.dip2px(getApplicationContext(), 11);
            searchResultText.setLayoutParams(params);
            int piddingTop = DensityUtil.dip2px(getApplicationContext(), 2);
            searchResultText.setPadding(0, piddingTop, 0, piddingTop);
            searchResultText.setGravity(Gravity.CENTER_VERTICAL);
            searchResultText.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.bg_select_mem));
            searchResultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            searchResultText.setTextColor(getResources()
                    .getColor(R.color.white));
            searchResultText.setText(selectMemList.get(i).getName());
            searchResultText.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    changeMembers(searchModel);
                }
            });
            int paddingLeft = DensityUtil.dip2px(getApplicationContext(), 5);
            int paddingTop = DensityUtil.dip2px(getApplicationContext(), 1);
            searchResultText.setPadding(paddingLeft, paddingTop, paddingLeft,
                    paddingTop);
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

    private void initSearchRunnable() {
        searchRunnbale = new Runnable() {
            @Override
            public void run() {
                lastSearchTime = System.currentTimeMillis();
                switch (searchArea) {
                    case SEARCH_ALL:
                        searchChannelGroupList = ChannelGroupCacheUtils
                                .getSearchChannelGroupList(getApplicationContext(),
                                        searchText);
                        searchContactList = ContactCacheUtils.getSearchContact(
                                getApplicationContext(), searchText, null,
                                4);
                        break;
                    case SEARCH_CHANNELGROUP:
                        searchChannelGroupList = ChannelGroupCacheUtils
                                .getSearchChannelGroupList(getApplicationContext(),
                                        searchText);
                        break;
                    case SEARCH_CONTACT:
                        searchContactList = ContactCacheUtils.getSearchContact(
                                getApplicationContext(), searchText, null,
                                4);
                        break;

                    default:
                        break;
                }
                handler.sendEmptyMessage(REFRESH_DATA);
            }
        };
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
            } else {
                lastSearchTime = 0;
                handler.removeCallbacks(searchRunnbale);
                hideSearchPop();
            }
        }

    }

    /**
     * 处理点击事件
     *
     * @param v
     */
    public void onClick(View v) {
        List<FirstGroupTextModel> list = new ArrayList<FirstGroupTextModel>();
        Intent intent = new Intent(getApplicationContext(),
                ContactSearchMoreActivity.class);
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.ok_text:
                returnSearchResultData();
                break;
            case R.id.struct_layout:
                openContact(null);
                break;
            case R.id.channel_group_layout:
                showAllChannelGroup();
                break;

            case R.id.pop_second_group_more_text:
                intent.putExtra("groupTextList",
                        (Serializable) popSecondGroupTextList);
                intent.putExtra("selectMemList", (Serializable) selectMemList);
                intent.putExtra("groupPosition", 2);
                intent.putExtra("searchText", searchEdit.getText().toString());
                intent.putExtra("searchContent", searchContent);
                intent.putExtra("isMultiSelect", isMultiSelect);
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
                startActivityForResult(intent, SEARCH_MORE);
                break;
            default:
                break;
        }
    }

    /**
     * 返回搜索结果
     */
    private void returnSearchResultData() {
        // TODO Auto-generated method stub
        InputMethodUtils.hide(ContactSearchActivity.this);
        JSONArray peopleArray = new JSONArray();
        JSONArray channelGroupArray = new JSONArray();
        JSONObject searchResultObj = new JSONObject();
        for (int i = 0; i < selectMemList.size(); i++) {
            SearchModel searchModel = selectMemList.get(i);
            if (searchModel.getType().equals("USER")) {
                String myUid = PreferencesUtils.getString(
                        getApplicationContext(), "userID");
                if (!(myUid.equals(searchModel.getId()) && isContainMe == false)) {
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
            // if (searchContent == SEARCH_CONTACT &&
            // channelGroupArray.length()>0 ) {
            //
            // }
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
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initPopView() {
        // TODO Auto-generated method stub
        popLayout = (NoHorScrollView) findViewById(R.id.pop_layout);
        popSecondGrouplayou = (LinearLayout) findViewById(R.id.pop_second_group_layout);
        popThirdGrouplayou = (LinearLayout) findViewById(R.id.pop_third_group_layout);
        popSecondGroupListView = (ListView) findViewById(R.id.pop_second_group_list);
        popThirdGroupListView = (ListView) findViewById(R.id.pop_third_group_list);
        popSecondGroupMoreText = (TextView) findViewById(R.id.pop_second_group_more_text);
        popThirdGroupMoreText = (TextView) findViewById(R.id.pop_third_group_more_text);
        popSecondGroupTitleListView = (RecyclerView) findViewById(R.id.pop_second_title_list);
        popThirdGroupTitleListView = (RecyclerView) findViewById(R.id.pop_third_title_list);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
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
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        // TODO Auto-generated method stub
                        ChannelGroup channelGroup = searchChannelGroupList
                                .get(position);
                        changeMembers(new SearchModel(channelGroup));

                    }
                });
        popThirdGroupListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Contact contact = searchContactList.get(position);
                changeMembers(new SearchModel(contact));

            }
        });

    }

    private void showSearchPop() {
        // TODO Auto-generated method stub
        if (StringUtils.isBlank(searchText)) {
            return;
        }
        originAllLayout.setVisibility(View.GONE);
        popLayout.setVisibility(View.VISIBLE);
        if (searchArea == SEARCH_ALL) {
            // 控制“更多”按钮的显示和隐藏 当全选时三个group的进行设置
            if (searchChannelGroupList.size() > 3) {
                popSecondGroupMoreText.setVisibility(View.VISIBLE);
            } else {
                popSecondGroupMoreText.setVisibility(View.GONE);
            }
            if (searchContactList.size() > 3) {
                popThirdGroupMoreText.setVisibility(View.VISIBLE);
            } else {
                popThirdGroupMoreText.setVisibility(View.GONE);
            }
            popSecondGrouplayou.setVisibility(View.VISIBLE);
            popThirdGrouplayou.setVisibility(View.VISIBLE);
            refreshListView(popSecondGroupListView, popSecondGroupAdapter);
            refreshListView(popThirdGroupListView, popThirdGroupAdapter);
        } else if (!isSearchSingle) {
            if (searchArea == SEARCH_CONTACT) {
                if (searchContactList.size() > 3) {
                    popThirdGroupMoreText.setVisibility(View.VISIBLE);
                } else {
                    popThirdGroupMoreText.setVisibility(View.GONE);
                }
                popSecondGrouplayou.setVisibility(View.GONE);
                popThirdGrouplayou.setVisibility(View.VISIBLE);
                refreshListView(popThirdGroupListView, popThirdGroupAdapter);
            }

            if (searchArea == SEARCH_CHANNELGROUP) {
                if (searchChannelGroupList.size() > 3) {
                    popSecondGroupMoreText.setVisibility(View.VISIBLE);
                } else {
                    popSecondGroupMoreText.setVisibility(View.GONE);
                }
                popSecondGrouplayou.setVisibility(View.VISIBLE);
                popThirdGrouplayou.setVisibility(View.GONE);
                popSecondGroupAdapter.notifyDataSetChanged();
            }
            refreshListView(popSecondGroupListView, popSecondGroupAdapter);
        } else {
            if (searchArea == SEARCH_CONTACT) {
                if (searchContactList.size() > 3) {
                    popThirdGroupMoreText.setVisibility(View.VISIBLE);
                } else {
                    popThirdGroupMoreText.setVisibility(View.GONE);
                }
                popSecondGrouplayou.setVisibility(View.GONE);
                popThirdGrouplayou.setVisibility(View.VISIBLE);
                refreshListView(popThirdGroupListView, popThirdGroupAdapter);
            } else {
                if (searchChannelGroupList.size() > 3) {
                    popSecondGroupMoreText.setVisibility(View.VISIBLE);
                } else {
                    popSecondGroupMoreText.setVisibility(View.GONE);
                }
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
        if (openGroupLayou.getVisibility() == View.VISIBLE) { // 重置origin页面的一些搜索参数
            isSearchSingle = true;
        } else {
            isSearchSingle = false;
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(
                        R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.name_text);
                viewHolder.rightArrowImg = (ImageView) convertView
                        .findViewById(R.id.arrow_img);
                viewHolder.selectedImg = (ImageView) convertView
                        .findViewById(R.id.selected_img);
                viewHolder.photoImg = (CircleImageView) convertView.findViewById(R.id.photo_img);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SearchModel searchModel = null;
            if (orginCurrentArea == SEARCH_CONTACT)

            {
                Contact contact = openGroupContactList.get(position);
                searchModel = new SearchModel(contact);
                if (contact.getType().equals("user")) { // 如果通讯录是人的话就显示头像
                    viewHolder.rightArrowImg.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.rightArrowImg.setVisibility(View.VISIBLE);
                }
                viewHolder.nameText.setText(searchModel
                        .getCompleteName(getApplicationContext()));

            } else {
                viewHolder.rightArrowImg.setVisibility(View.INVISIBLE);
                ChannelGroup channelGroup = openGroupChannelList.get(position);
                searchModel = new SearchModel(channelGroup);
                viewHolder.nameText.setText(channelGroup.getChannelName());

            }
            if (searchModel != null) {
                displayImg(searchModel, viewHolder.photoImg);
            }
            if (searchModel != null && selectMemList.contains(searchModel)) {
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
                LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(
                        R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.name_text);
                viewHolder.selectedImg = (ImageView) convertView
                        .findViewById(R.id.selected_img);
                viewHolder.photoImg = (CircleImageView) convertView.findViewById(R.id.photo_img);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            SearchModel searchModel = commonContactList.get(position);
            viewHolder.nameText.setText(searchModel
                    .getCompleteName(getApplicationContext()));
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

    public static class ViewHolder {
        TextView nameText;
        CircleImageView photoImg;
        ImageView rightArrowImg;
        ImageView selectedImg;
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
                LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(
                        R.layout.member_search_item_view, null);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.name_text);
                viewHolder.selectedImg = (ImageView) convertView
                        .findViewById(R.id.selected_img);
                viewHolder.photoImg = (CircleImageView) convertView.findViewById(R.id.photo_img);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            convertView.setBackgroundColor(Color.parseColor("#F4F4F4"));
            SearchModel searchModel = null;
            if (groupPosition == 2) {
                ChannelGroup channelGroup = searchChannelGroupList
                        .get(position);
                searchModel = new SearchModel(channelGroup);
            } else {
                Contact contact = searchContactList.get(position);
                searchModel = new SearchModel(contact);

            }
            displayImg(searchModel, viewHolder.photoImg);
            viewHolder.nameText.setText(searchModel
                    .getCompleteName(getApplicationContext()));
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
     * 第一个group title中list的adapter
     *
     * @author Administrator
     */
    public class GroupTitleAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private boolean isPopTitle = false;
        private int groupPosition;

        public GroupTitleAdapter() {

        }

        public GroupTitleAdapter(boolean isPopTitle, int groupPosition) {
            this.isPopTitle = isPopTitle;
            this.groupPosition = groupPosition;
        }

        private MyItemClickListener mItemClickListener;

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
                    arg0.titleText.setBackgroundColor(Color
                            .parseColor("#d8d8d8"));
                    if (arg1 != count - 1) {
                        arg0.titleImg
                                .setImageResource(R.drawable.icon_group_title_mid_img);
                        arg0.titleText
                                .setTextColor(Color.parseColor("#EFEFF4"));
                    } else {
                        arg0.titleImg
                                .setImageResource(R.drawable.icon_group_title_end_img);
                        arg0.titleText
                                .setTextColor(Color.parseColor("#ffffff"));
                    }
                } else {
                    arg0.titleText.setBackgroundColor(Color
                            .parseColor("#00000000"));
                    arg0.titleText.setTextColor(Color.parseColor("#8f8e94"));
                    arg0.titleImg.setImageDrawable(null);
                }

            } else {
                arg0.titleText.setBackgroundColor(Color.parseColor("#d8d8d8"));
                arg0.titleText.setText(openGroupTextList.get(arg1).getName());
                if (arg1 != count - 1) {
                    arg0.titleImg
                            .setImageResource(R.drawable.icon_group_title_mid_img);
                    arg0.titleText.setTextColor(Color.parseColor("#0f7bca"));
                } else {
                    arg0.titleImg
                            .setImageResource(R.drawable.icon_group_title_end_img);
                    arg0.titleText.setTextColor(Color.parseColor("#ffffff"));
                }
            }

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
            // TODO Auto-generated method stub
            LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
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

    public interface MyItemClickListener {
        public void onItemClick(View view, int position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements
            OnClickListener {
        private MyItemClickListener mListener;
        TextView titleText;
        ImageView titleImg;
        View view;

        public MyViewHolder(View view, MyItemClickListener listener) {
            super(view);
            this.view = view;
            titleText = (TextView) view.findViewById(R.id.title_text);
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

    /**
     * 统一显示图片
     *
     * @param searchModel
     * @param photoImg
     */
    private void displayImg(SearchModel searchModel, CircleImageView photoImg) {
        Integer defaultIcon = null; // 默认显示图标
        String icon = null;
        String type = searchModel.getType();
        if (type.equals("GROUP")) {
            defaultIcon = R.drawable.icon_channel_group_default;
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + searchModel.getId() + "_100.png1");
            if (file.exists()) {
                icon = "file://" + file.getAbsolutePath();
                ImageDisplayUtils.getInstance().displayImageNoCache(photoImg, icon, defaultIcon);
                return;
            }
        } else if (type.equals("STRUCT")) {
            defaultIcon = R.drawable.icon_channel_group_default;
        } else {
            defaultIcon = R.drawable.icon_person_default;
            if (!searchModel.getId().equals("null")) {
                icon = searchModel.getIcon(ContactSearchActivity.this);
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

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        int openGroupTextListSize = openGroupTextList.size();
        // 当正在显示搜索结果时，点击返回后隐藏搜索结果
        if (popLayout.getVisibility() == View.VISIBLE) {
            searchEdit.setText("");
        } else if (openGroupLayou.getVisibility() == View.VISIBLE
                && openGroupTextListSize > 1) {// 当正在查看多级组织架构时，点击返回后返回上一级的组织架构
            int position = openGroupTextListSize - 2;
            clickGroupTitle(position);
        } else {
            super.onBackPressed();
        }

    }


    /**
     * 创建或进入频道
     *
     * @param searchModel
     */
    private void creatOrInterChannel(SearchModel searchModel) {
        // TODO Auto-generated method stub
        String uid = ((MyApplication) getApplication()).getUid();
        if (uid.equals(searchModel.getId())) {
            ToastUtils.show(ContactSearchActivity.this,
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
            IntentUtils.startActivity(ContactSearchActivity.this,
                    ChannelActivity.class, bundle);

        }
    }

    /**
     * 创建单聊
     *
     * @param id
     */
    private void creatDirectChannel(String id) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            new ChatCreateUtils().createDirectChannel(
                    ContactSearchActivity.this, id,
                    new OnCreateDirectChannelListener() {

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
                                            .getName(getApplicationContext()));
                            IntentUtils.startActivity(
                                    ContactSearchActivity.this,
                                    ChannelActivity.class, bundle);
                        }

                        @Override
                        public void createDirectChannelFail() {
                            // TODO Auto-generated method stub

                        }
                    });
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

}
