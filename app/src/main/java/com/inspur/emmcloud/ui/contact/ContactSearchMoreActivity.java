package com.inspur.emmcloud.ui.contact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.FirstGroupTextModel;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.WebServiceRouterManager;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.CommonContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.FlowLayout;
import com.inspur.emmcloud.widget.MaxHightScrollView;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactSearchMoreActivity extends BaseActivity implements MySwipeRefreshLayout.OnLoadListener {
    public static final String EXTRA_EXCLUDE_SELECT = "excludeContactUidList";
    public static final String EXTRA_LIMIT = "select_limit";
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_CONTACT = 2;
    private static final int SEARCH_CHANNELGROUP = 1;
    private static final int SEARCH_RECENT = 3;
    private static final int SEARCH_NOTHIING = 4;
    private static final int REFRESH_CONTACT_DATA = 5;
    private List<SearchModel> searchChannelGroupList = new ArrayList<>(); // 群组搜索结果
    private List<Contact> searchContactList = new ArrayList<>(); // 通讯录搜索结果
    private List<Channel> searchRecentList = new ArrayList<>();// 常用联系人搜索结果
    private List<SearchModel> selectMemList = new ArrayList<>();
    private List<FirstGroupTextModel> groupTextList = new ArrayList<>();
    private int searchArea;
    private int searchContent;
    private boolean isMultiSelect = false;
    private String searchText;

    private FlowLayout flowLayout;
    private EditText searchEdit;
    private MyTextWatcher myTextWatcher;
    private MaxHightScrollView searchEditLayout;
    private ListView searchListView;
    private MySwipeRefreshLayout swipeRefreshLayout;
    private Adapter adapter;
    private RecyclerView groupTitleListView;
    private GroupTitleAdapter groupTitleAdapter;
    private Handler handler;
    private List<Contact> excludeContactList = new ArrayList<>();//不显示某些数据
    private int selectLimit = 5000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search_more);
        handMessage();
        initView();
        getIntentData();
        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_contact_search_more;
    }

    private void initView() {
        // TODO Auto-generated method stub
        flowLayout = (FlowLayout) findViewById(R.id.flowlayout);
        myTextWatcher = new MyTextWatcher();
        flowAddEdit();
        searchEditLayout = (MaxHightScrollView) findViewById(R.id.search_edit_layout);
        swipeRefreshLayout = (MySwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnLoadListener(this);
        swipeRefreshLayout.setEnabled(false);
        searchListView = (ListView) findViewById(R.id.search_list);
        adapter = new Adapter();
        searchListView.setAdapter(adapter);
        searchListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                if (selectMemList.size() < selectLimit) {
                    SearchModel searchModel = null;
                    if (searchArea == SEARCH_CHANNELGROUP) {
                        searchModel = searchChannelGroupList.get(position);
                    } else if (searchArea == SEARCH_CONTACT) {
                        searchModel = new SearchModel(searchContactList
                                .get(position));
                    } else {
                        searchModel = new SearchModel(searchRecentList
                                .get(position));
                    }
                    changeMembers(searchModel);
                } else {
                    ToastUtils.show(MyApplication.getInstance(), R.string.contact_select_limit_warning);
                }
            }
        });
        groupTitleListView = (RecyclerView) findViewById(R.id.title_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        groupTitleListView.setLayoutManager(layoutManager);
        groupTitleAdapter = new GroupTitleAdapter();
        groupTitleListView.setAdapter(groupTitleAdapter);
    }

    private void flowAddEdit() {
        if (searchEdit == null) {
            searchEdit = new EditText(this);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
                    getApplicationContext(), LayoutParams.WRAP_CONTENT));
            params.topMargin = DensityUtil.dip2px(getApplicationContext(), 2);
            params.bottomMargin = params.topMargin;
            int piddingTop = DensityUtil.dip2px(getApplicationContext(), 1);
            int piddingLeft = DensityUtil.dip2px(getApplicationContext(), 10);
            searchEdit.setPadding(piddingLeft, piddingTop, piddingLeft, piddingTop);
            searchEdit.setLayoutParams(params);
            searchEdit.setSingleLine(true);
            searchEdit.setHint(getString(R.string.msg_key_search_member));
            searchEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            searchEdit.requestFocus();
            searchEdit.requestFocusFromTouch();
            searchEdit.setBackground(null);
            searchEdit.addTextChangedListener(myTextWatcher);
        }
        flowLayout.addView(searchEdit);
    }

    private void getIntentData() {
        // TODO Auto-generated method stub
        selectLimit = getIntent().getIntExtra(EXTRA_LIMIT, 5000);
        isMultiSelect = getIntent().getBooleanExtra("isMultiSelect", false);
        selectMemList = (List<SearchModel>) getIntent().getSerializableExtra(
                "selectMemList");
        searchContent = getIntent().getIntExtra("searchContent", 1);
        // 单选时隐藏输入框或者不选时
        if (!isMultiSelect || searchContent == SEARCH_NOTHIING) {
            (findViewById(R.id.ok_text)).setVisibility(View.GONE);
        }
        searchText = getIntent().getStringExtra("searchText");
        int groupPosition = getIntent().getIntExtra("groupPosition", 1);
        groupTextList = (List<FirstGroupTextModel>) getIntent()
                .getSerializableExtra("groupTextList");
        groupTitleAdapter.notifyDataSetChanged();

        switch (groupPosition) {
            case 1:
                searchArea = SEARCH_RECENT;
                break;
            case 2:
                searchArea = SEARCH_CHANNELGROUP;
                break;

            case 3:
                searchArea = SEARCH_CONTACT;
                break;

            default:
                break;
        }
        if (getIntent().hasExtra(EXTRA_EXCLUDE_SELECT)) {
            List<String> excludeContactUidList = (List<String>) getIntent().getExtras().getSerializable(EXTRA_EXCLUDE_SELECT);
            excludeContactList = Contact.contactUserList2ContactList(ContactUserCacheUtils.getContactUserListById(excludeContactUidList));
        }
        notifyFlowLayoutDataChange(searchText);

    }

    /**
     * 刷新FlowLayout
     */
    private void notifyFlowLayoutDataChange(String content) {
        // searchEdit.removeTextChangedListener(myTextWatcher);
        EditTextUtils.setText(searchEdit, content);
        flowLayout.removeAllViews();
        for (int i = 0; i < selectMemList.size(); i++) {
            final SearchModel searchModel = selectMemList.get(i);
            TextView searchResultText = new TextView(this);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = DensityUtil.dip2px(MyApplication.getInstance(), 5);
            params.topMargin = DensityUtil.dip2px(MyApplication.getInstance(), 2);
            params.bottomMargin = params.topMargin;
            searchResultText.setLayoutParams(params);
            int piddingTop = DensityUtil.dip2px(MyApplication.getInstance(), 1);
            int piddingLeft = DensityUtil.dip2px(MyApplication.getInstance(), 5);
            searchResultText.setPadding(piddingLeft, piddingTop, piddingLeft, piddingTop);
            searchResultText.setGravity(Gravity.CENTER);
            searchResultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            searchResultText.setTextColor(Color.parseColor("#0F7BCA"));
            searchResultText.setText(selectMemList.get(i).getName());
            searchResultText.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    changeMembers(searchModel);
                }
            });
            int paddingLeft = DensityUtil.dip2px(getApplicationContext(), 10);
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
        adapter.notifyDataSetChanged();
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
            } else {
                selectMemList.remove(searchModel);
                notifyFlowLayoutDataChange("");
                return;
            }
            returnSelectData();
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
        if (id.equals("null")) {
            ToastUtils.show(getApplicationContext(), R.string.cannot_view_info);
            return;
        }
        CommonContactCacheUtils.saveCommonContact(getApplicationContext(),
                searchModel);
        if (type.equals(SearchModel.TYPE_USER)) {
            intent.putExtra("uid", id);
            intent.setClass(getApplicationContext(), UserInfoActivity.class);
            startActivity(intent);
        } else {
            intent.setClass(getApplicationContext(), WebServiceRouterManager.getInstance().isV0VersionChat() ? ChannelV0Activity.class : ConversationActivity.class);
            intent.putExtra("title", searchModel.getName());
            intent.putExtra("cid", searchModel.getId());
            intent.putExtra("channelType", searchModel.getType());
            startActivity(intent);
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.ok_text:
                returnSelectData();
                break;
            case R.id.layout:
                if (searchEdit != null) {
                    InputMethodUtils.display(ContactSearchMoreActivity.this, searchEdit);
                }
                break;
            default:
                break;
        }

    }

    private void returnSelectData() {
        // TODO Auto-generated method stub
        InputMethodUtils.hide(ContactSearchMoreActivity.this);
        Intent intent = new Intent();
        intent.putExtra("selectMemList", (Serializable) selectMemList);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void handMessage() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_CONTACT_DATA:
                        swipeRefreshLayout.setCanLoadMore((searchContactList.size() == 25));
                        adapter.notifyDataSetChanged();
                        break;
                }
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
            defaultIcon = R.drawable.icon_channel_group_default;
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
    public void onLoadMore() {
        // TODO Auto-generated method stub
        List<Contact> excludeSearchContactList = new ArrayList<>();
        excludeSearchContactList.addAll(searchContactList);
        excludeSearchContactList.addAll(excludeContactList);
        List<Contact> moreContactList = ContactUserCacheUtils.getSearchContact(searchText, excludeSearchContactList, 25);
        swipeRefreshLayout.setLoading(false);
        swipeRefreshLayout.setCanLoadMore((moreContactList.size() == 25));
        if (moreContactList.size() != 0) {
            searchContactList.addAll(searchContactList.size(), moreContactList);
            adapter.notifyDataSetChanged();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_QUIT_CHANNEL_GROUP:
            case Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME:
                if (searchChannelGroupList.size() > 0) {
                    searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler = null;
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static class ViewHolder {
        TextView nameText;
        CircleTextImageView photoImg;
        ImageView selectedImg;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements
            OnClickListener {
        TextView titleText;
        ImageView titleImg;

        public MyViewHolder(View view) {
            super(view);
            titleText = (TextView) view.findViewById(R.id.tv_name_tips);
            titleImg = (ImageView) view.findViewById(R.id.title_img);

        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

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
                switch (searchArea) {
                    case SEARCH_RECENT:
                        searchRecentList = ChannelCacheUtils.getSearchChannelList(
                                getApplicationContext(), searchText, searchContent);
                        adapter.notifyDataSetChanged();
                        break;
                    case SEARCH_CHANNELGROUP:
                        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                            searchChannelGroupList = ChannelGroupCacheUtils
                                    .getSearchChannelGroupSearchModelList(MyApplication.getInstance(),
                                            searchText);
                        } else {
                            searchChannelGroupList = ConversationCacheUtils.getSearchConversationSearchModelList(MyApplication.getInstance(), searchText);
                        }

                        adapter.notifyDataSetChanged();
                        break;

                    case SEARCH_CONTACT:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                searchContactList = ContactUserCacheUtils.getSearchContact(searchText,
                                        excludeContactList, 25);
                                if (handler != null) {
                                    handler.sendEmptyMessage(REFRESH_CONTACT_DATA);
                                }
                            }
                        }).start();
                        break;

                    default:
                        break;
                }

            } else {
                returnSelectData();
            }
        }

    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (searchArea == SEARCH_RECENT) {
                return searchRecentList.size();
            } else if (searchArea == SEARCH_CHANNELGROUP) {
                return searchChannelGroupList.size();
            } else {
                return searchContactList.size();
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
                        .findViewById(R.id.tv_name);
                viewHolder.photoImg = (CircleTextImageView) convertView.findViewById(R.id.img_photo);
                viewHolder.selectedImg = (ImageView) convertView
                        .findViewById(R.id.selected_img);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            convertView.setBackgroundColor(Color.parseColor("#F4F4F4"));
            SearchModel searchModel = null;
            if (searchArea == SEARCH_RECENT) {
                Channel channel = searchRecentList.get(position);
                searchModel = new SearchModel(channel);
            } else if (searchArea == SEARCH_CHANNELGROUP) {
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

        @Override
        public int getItemCount() {
            // TODO Auto-generated method stub
            return groupTextList.size();

        }

        @Override
        public void onBindViewHolder(MyViewHolder arg0, int arg1) {
            // TODO Auto-generated method stub
            int count = getItemCount();
            arg0.titleText.setText(groupTextList.get(arg1).getName());

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
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
            // TODO Auto-generated method stub
            LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = mInflater.inflate(R.layout.contact_header_item_view,
                    arg0, false);
            // view.setBackgroundColor(Color.RED);
            MyViewHolder viewHolder = new MyViewHolder(view);
            return viewHolder;
        }
    }

}
