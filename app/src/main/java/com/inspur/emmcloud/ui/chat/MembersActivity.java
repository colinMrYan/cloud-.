package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMemberListAdapter;
import com.inspur.emmcloud.adapter.MemberSelectGridAdapter;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.PinyinUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.FlowLayout;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.MaxHightScrollView;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.PersonDto;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.widget.slidebar.CharacterParser;
import com.inspur.emmcloud.widget.slidebar.SideBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

@Route(path = Constant.AROUTER_CLASS_COMMUNICATION_MEMBER)
public class MembersActivity extends BaseActivity implements TextWatcher {
    public static final String MEMBER_PAGE_STATE = "member_page_state";
    public static final int SELECT_STATE = 1;//选择人员
    public static final int MENTIONS_STATE = 2;//@人员选择
    public static final int CHECK_STATE = 3;//查看人员
    private static final int TOTAL_MEMBERS_NUM = 9;//最多可选择的人数配置，修改这个配置应当同时修改toast提示里的配置数量
    @BindView(R.id.sidrbar_channel_member_select)
    SideBar lettersSideBar;
    @BindView(R.id.tv_ok)
    TextView okTv;
    @BindView(R.id.lv_channel_member_select)
    ListView allMemberListView;
    @BindView(R.id.recyclerview_voice_communication_select_members)
    RecyclerView selectedMemberRecylerView;
    @BindView(R.id.header_text)
    TextView userHeadText;
    @BindView(R.id.ev_channel_member_search_input)
    EditText searchInputEv;
    @BindView(R.id.tv_more_select)
    TextView moreSelectText;
    @BindView(R.id.flow_layout)
    FlowLayout flowLayout;
    @BindView(R.id.search_edit_layout)
    MaxHightScrollView searchEditLayout;

    EditText editText;
    private CharacterParser characterParser;// 汉字转拼音
    private PinyinComparator pinyinComparator;// 根据拼音来排列ListView里面的数据类
    private ChannelMemberListAdapter channelMemberListAdapter;
    private String channelId = "";
    private Handler handler;
    private LoadingDialog loadingDlg;
    private List<PersonDto> filterList = new ArrayList<PersonDto>();
    private List<PersonDto> personDtoList = new ArrayList<>();
    private MemberSelectGridAdapter selectGridAdapter;
    private List<ContactUser> allReadySelectUserList = new ArrayList<>();
    private List<PersonDto> newSelectUserList = new ArrayList<>();//这次刚选的群成员list
    private List<PersonDto> selectedUserList = new ArrayList<>();//选中的群成员list  ///
    private List<PersonDto> allReadySelectPersonDtoList = new ArrayList<>();
    private int state = -1;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
        initChannelMemberDataInThread();
        initListener();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_member;
    }

    private void initViews() {
        state = getIntent().getIntExtra(MEMBER_PAGE_STATE, -1);
        if (state == MENTIONS_STATE) {  ///////////////////////////
            lettersSideBar.setVisibility(View.GONE);
            moreSelectText.setVisibility(View.VISIBLE);
            okTv.setVisibility(View.GONE);
            flowAddEdit();
        } else {
            selectedUserList.add(tranContactUser2PersonDto(ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid())));
        }
        loadingDlg = new LoadingDialog(this);
        loadingDlg.show();
        selectedMemberRecylerView = (RecyclerView) findViewById(R.id.recyclerview_voice_communication_select_members);
        okTv.setVisibility(state == SELECT_STATE ? View.VISIBLE : View.GONE);
        allReadySelectUserList = new ArrayList<>();
        allReadySelectUserList.add(ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid()));
        allReadySelectPersonDtoList = tranContactUserList2PersonDtoList(allReadySelectUserList);
        selectGridAdapter = new MemberSelectGridAdapter(this, allReadySelectPersonDtoList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        selectedMemberRecylerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        selectedMemberRecylerView.setLayoutManager(gridLayoutManager);
        selectedMemberRecylerView.setAdapter(selectGridAdapter);
        selectedMemberRecylerView.setVisibility((state == SELECT_STATE && allReadySelectUserList.size() > 0) ? View.VISIBLE : View.GONE);
        lettersSideBar.setVisibility(state == SELECT_STATE || state == MENTIONS_STATE ? View.GONE : View.VISIBLE);
        channelId = getIntent().getStringExtra("cid");
        userHeadText.setText(getIntent().getStringExtra("title"));
    }

    /**
     * 在线程里组织群成员data
     */
    private void initChannelMemberDataInThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!StringUtils.isBlank(channelId)) {
                    List<String> uidList = null;
                    if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
                        Conversation conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), channelId);
                        uidList = conversation.getMemberList();
                    } else {
                        uidList = ChannelGroupCacheUtils.getMemberUidList(MembersActivity.this, channelId, 0);
                    }
                    personDtoList = ContactUserCacheUtils.getShowMemberList(uidList);
                } else if (getIntent().getStringArrayListExtra("uidList") != null) {
                    personDtoList = ContactUserCacheUtils.getShowMemberList(getIntent().getStringArrayListExtra("uidList"));
                }
                if (state == MENTIONS_STATE) {
                    PersonDto personDtoAdd = new PersonDto();
                    personDtoAdd.setName(getString(R.string.chat_search_mention_all));
                    personDtoAdd.setUid("10");
                    personDtoAdd.setPinyinFull("#quanbuchengyuan");
                    personDtoAdd.setSuoxie("#quanbuchengyuan");
                    personDtoAdd.setUtype("");
                    personDtoAdd.setSortLetters("#");
                    personDtoList.add(0, personDtoAdd);
                    Iterator<PersonDto> personDtoIterator = personDtoList.iterator();
                    while (personDtoIterator.hasNext()) {
                        PersonDto personDto = personDtoIterator.next();
                        if (personDto.getUid().equals(MyApplication.getInstance().getUid())) {
                            personDtoIterator.remove();
                            break;
                        }
                    }
                }
                handler.sendMessage(handler.obtainMessage(0));
            }
        };
        try {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {
                        initData();
                    } else {
                        ToastUtils.show(MembersActivity.this, getString(R.string.load_data_failed));
                    }
                    LoadingDialog.dimissDlg(loadingDlg);
                }
            };
            new Thread(runnable).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        fillData(personDtoList);
        // 根据a-z进行排序源数据
        Collections.sort(personDtoList, pinyinComparator);
//        ArrayList<String> letterIndexList = getSideBarLetterList();
//        lettersSideBar.setIndexArray(letterIndexList);
        if (state == SELECT_STATE) {
            channelMemberListAdapter = new ChannelMemberListAdapter(MembersActivity.this,
                    personDtoList, allReadySelectPersonDtoList);
        } else {
            channelMemberListAdapter = new ChannelMemberListAdapter(MembersActivity.this,
                    personDtoList);
        }
        lettersSideBar.invalidate();
        allMemberListView.setAdapter(channelMemberListAdapter);
        searchInputEv.addTextChangedListener(this);
    }

    /**
     * 刷新FlowLayout
     */
    private void notifyFlowLayoutDataChange() {
        editText.setText("");
        flowLayout.removeAllViews();
        for (int i = 0; i < selectedUserList.size(); i++) {
            final PersonDto personDto = selectedUserList.get(i);
            TextView searchResultText = new TextView(this);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = DensityUtil.dip2px(MyApplication.getInstance(), 5);
            params.topMargin = DensityUtil.dip2px(MyApplication.getInstance(), 2);
            params.bottomMargin = params.topMargin;
            searchResultText.setLayoutParams(params);
            int piddingTop = DensityUtil.dip2px(this.getApplicationContext(), 1);
            int piddingLeft = DensityUtil.dip2px(this.getApplicationContext(), 5);
            searchResultText.setPadding(piddingLeft, piddingTop, piddingLeft, piddingTop);
            searchResultText.setGravity(Gravity.CENTER);
            searchResultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            searchResultText.setTextColor(Color.parseColor("#0F7BCA"));
            searchResultText.setText(selectedUserList.get(i).getName());
            searchResultText.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    changePersonDtos(personDto);
                }
            });
            flowLayout.addView(searchResultText);
        }
        flowAddEdit();
    }

    private void changePersonDtos(PersonDto personDto) {
        if (selectedUserList.contains(personDto)) {
            selectedUserList.remove(personDto);
            channelMemberListAdapter.updateSelectListViewData(selectedUserList);
            selectGridAdapter.setAndRefreshSelectMemberData(selectedUserList);
            notifyFlowLayoutDataChange();
        }
    }

    private void flowAddEdit() {
        if (editText == null) {
            editText = new EditText(this);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
                    this.getApplicationContext(), ViewGroup.LayoutParams.WRAP_CONTENT));
            params.topMargin = DensityUtil.dip2px(this.getApplicationContext(), 2);
            params.bottomMargin = params.topMargin;
            int piddingTop = DensityUtil.dip2px(MyApplication.getInstance(), 1);
            int piddingLeft = DensityUtil.dip2px(MyApplication.getInstance(), 10);
            editText.setPadding(piddingLeft, piddingTop, piddingLeft, piddingTop);
            editText.setLayoutParams(params);
            editText.setSingleLine(true);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            editText.setBackground(null);
            editText.setHint(getString(R.string.msg_key_search_member));
            editText.addTextChangedListener(this);
        }

        if (editText.getParent() == null) {
            flowLayout.addView(editText);
        }
    }

    /**
     * 备用方法，获取索引letters
     *
     * @return
     */
    private ArrayList<String> getSideBarLetterList() {
        ArrayList<String> letterIndexList = new ArrayList<>();
        for (PersonDto personDto : personDtoList) {
            String sortLetter = personDto.getSortLetters();
            if (!letterIndexList.contains(sortLetter)) {
                letterIndexList.add(sortLetter);
            }
        }
        letterIndexList.add("#");
        return letterIndexList;
    }

    /**
     * 设置监听器
     */
    private void initListener() {
        allMemberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent();
                switch (state) {
                    case SELECT_STATE:
                        PersonDto dto = (filterList.size() != 0) ? filterList.get(position) : personDtoList.get(position);
                        if (!allReadySelectPersonDtoList.contains(dto)) {
                            if (selectedUserList.size() < TOTAL_MEMBERS_NUM) {
                                if (!selectedUserList.contains(dto)) {
                                    selectedUserList.add(dto);
                                    newSelectUserList.add(dto);
                                    updateView(view, View.VISIBLE);
                                } else {
                                    selectedUserList.remove(dto);
                                    newSelectUserList.remove(dto);
                                    updateView(view, View.GONE);
                                }
                                channelMemberListAdapter.updateSelectListViewData(selectedUserList);
                                selectGridAdapter.setAndRefreshSelectMemberData(selectedUserList);
                            } else {
                                ToastUtils.show(MembersActivity.this, getString(R.string.voice_communication_support_nine_members));
                            }
                        }
                        break;
                    case MENTIONS_STATE:
                        PersonDto MentionDto = (filterList.size() != 0) ? filterList.get(position) : personDtoList.get(position);
                        if (moreSelectText.getVisibility() == View.VISIBLE || MentionDto.getUid().equals("10")) {
                            JSONArray jsonArray = new JSONArray();
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("uid", MentionDto.getUid());
                                jsonObject.put("name", MentionDto.getName());
                                jsonArray.put(jsonObject);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            intent.putExtra("searchResult", jsonArray.toString());
                            boolean isInputKeyWord = getIntent().getBooleanExtra("isInputKeyWord", false);
                            intent.putExtra("isInputKeyWord", isInputKeyWord);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            if (!allReadySelectPersonDtoList.contains(MentionDto)) {
                                if (!selectedUserList.contains(MentionDto)) {
                                    selectedUserList.add(MentionDto);
                                    newSelectUserList.add(MentionDto);
                                    updateView(view, View.VISIBLE);
                                } else {
                                    selectedUserList.remove(MentionDto);
                                    newSelectUserList.remove(MentionDto);
                                    updateView(view, View.GONE);
                                }
                                channelMemberListAdapter.updateSelectListViewData(selectedUserList);
                                selectGridAdapter.setAndRefreshSelectMemberData(selectedUserList);
                                allMemberListView.setSelection(position);
                            }
                            notifyFlowLayoutDataChange();
                        }
                        break;
                    case CHECK_STATE:
                        String uid = "";
                        if (searchInputEv.getText().toString().length() > 0) {
                            uid = filterList.get(position).getUid();
                        } else {
                            uid = personDtoList.get(position).getUid();
                        }
                        intent.putExtra("uid", uid);
                        if (uid.startsWith("BOT")) {
                            intent.setClass(getApplicationContext(), RobotInfoActivity.class);
                        } else {
                            intent.setClass(getApplicationContext(),
                                    UserInfoActivity.class);
                        }
                        startActivity(intent);
                        break;
                    default:
                        break;
                }

            }
        });

        lettersSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = 0;
                // 该字母首次出现的位置
                if (channelMemberListAdapter != null) {
                    position = channelMemberListAdapter.getPositionForSection(s.charAt(0));
                }
                if (position != -1) {
                    allMemberListView.setSelection(position);
                }
            }
        });
    }

    /**
     * 更新view
     *
     * @param view
     * @param visible
     */
    private void updateView(View view, int visible) {
        ImageView imageView = view.findViewById(R.id.img_member_selected);
        imageView.setVisibility(visible);
        imageView.setImageResource(R.drawable.icon_other_selected);
    }


    /**
     * 数据转换器
     *
     * @param contactUserList
     * @return
     */
    private List<PersonDto> tranContactUserList2PersonDtoList(List<ContactUser> contactUserList) {
        List<PersonDto> resultList = new ArrayList<>();
        if (contactUserList != null) {
            Iterator<ContactUser> contactListIterator = contactUserList.iterator();
            while (contactListIterator.hasNext()) {
                ContactUser contactUser = contactListIterator.next();
                resultList.add(tranContactUser2PersonDto(contactUser));
            }
        }
        return resultList;
    }

    /**
     * 单个数据转换
     *
     * @param contactUser
     * @return
     */
    private PersonDto tranContactUser2PersonDto(ContactUser contactUser) {
        if (contactUser == null) return null;
        PersonDto personDto = new PersonDto();
        personDto.setName(contactUser.getName());
        personDto.setUid(contactUser.getId());
        personDto.setSortLetters(contactUser.getPinyin().substring(0, 1));
        personDto.setPinyinFull(contactUser.getPinyin());
        personDto.setSuoxie(PinyinUtils.getPinYinHeadChar(contactUser
                .getName()));
        personDto.setUtype("contact");
        return personDto;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
        loadingDlg.show();
        filterData(s.toString(), personDtoList);
        if (loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr, List<PersonDto> list) {
        List<PersonDto> filterDateList = new ArrayList<PersonDto>();
        if (StringUtils.isEmpty(filterStr)) {
            filterDateList = list;
        } else {
            filterDateList.clear();
            for (PersonDto sortModel : list) {
                String name = sortModel.getName();
                String suoxie = sortModel.getSuoxie();
                if (name.indexOf(filterStr.toString()) != -1
                        || suoxie.indexOf(filterStr.toString()) != -1
                        || characterParser.getSelling(name).startsWith(
                        filterStr.toString())
                        || sortModel.getPinyinFull().toLowerCase().contains(
                        filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }
        filterList = filterDateList;
        if (filterList.size() == 0) {
            ToastUtils.show(MembersActivity.this, getString(R.string.no_search_term));
        }
        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        channelMemberListAdapter.updateListView(filterDateList);
        if (state != MENTIONS_STATE) {
            allMemberListView.setSelection(0);
        }
    }

    /**
     * 填充数据
     *
     * @param list
     */
    private void fillData(List<PersonDto> list) {
        ArrayList<String> indexList = new ArrayList<>();
        for (PersonDto userInfoDto : list) {
            if (userInfoDto != null && userInfoDto.getName() != null) {
                String pinyin = userInfoDto.getPinyinFull();
                String sortString = pinyin.substring(0, 1).toUpperCase();
                indexList.add(sortString);
                if ("1".equals(userInfoDto.getUtype())) {// 判断是否是管理员
                    userInfoDto.setSortLetters("☆");
                } else if (sortString.matches("[A-Z]")) {// 正则表达式，判断首字母是否是英文字母   jason修改crash
                    userInfoDto.setSortLetters(sortString);
                } else {
                    userInfoDto.setSortLetters("#");
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_ok:
                if (state == MENTIONS_STATE) {
                    Intent intent = new Intent();
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < selectedUserList.size(); i++) {
                        JSONObject jsonResult1 = new JSONObject();
                        try {
                            jsonResult1.put("uid", selectedUserList.get(i).getUid());
                            jsonResult1.put("name", selectedUserList.get(i).getName());
                            jsonArray.put(jsonResult1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    intent.putExtra("searchResult", jsonArray.toString());
                    boolean isInputKeyWord = getIntent().getBooleanExtra("isInputKeyWord", false);
                    intent.putExtra("isInputKeyWord", isInputKeyWord);
                    setResult(RESULT_OK, intent);
                    finish();
                    finish();
                } else {
                    startCommunication();
                }

                break;
            case R.id.tv_more_select:
                okTv.setVisibility(View.VISIBLE);
                moreSelectText.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * 邀请开始通话
     */
    private void startCommunication() {
        List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
        for (int i = 0; i < selectedUserList.size(); i++) {
            VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = new VoiceCommunicationJoinChannelInfoBean();
            voiceCommunicationJoinChannelInfoBean.setUserName(selectedUserList.get(i).getName());
            voiceCommunicationJoinChannelInfoBean.setUserId(selectedUserList.get(i).getUid());
            voiceCommunicationUserInfoBeanList.add(voiceCommunicationJoinChannelInfoBean);
        }
        Intent intent = new Intent();
        intent.setClass(MembersActivity.this, ChannelVoiceCommunicationActivity.class);
        intent.putExtra("userList", (Serializable) voiceCommunicationUserInfoBeanList);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE, ChannelVoiceCommunicationActivity.INVITER_LAYOUT_STATE);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class PinyinComparator implements Comparator<PersonDto> {
        @Override
        public int compare(PersonDto o1, PersonDto o2) {
            if (o1.getSortLetters().equals("☆")) {
                return -1;
            } else if (o2.getSortLetters().equals("☆")) {
                return 1;
            } else if (o1.getSortLetters().equals("#")) {
                return -1;
            } else if (o2.getSortLetters().equals("#")) {
                return 1;
            } else {
                String o1Name = o1.getName();
                String o2Name = o2.getName();
                String o1First = o1.getPinyinFull().subSequence(0, 1).toString();
                String o2First = o2.getPinyinFull().subSequence(0, 1).toString();
                if (!o1First.equals(o2First)) {
                    return Collator.getInstance().compare(o1First, o2First);
                } else if (StringUtils.isFirstCharEnglish(o1Name) && StringUtils.isFirstCharEnglish(o2Name) || !StringUtils.isFirstCharEnglish(o1Name) && !StringUtils.isFirstCharEnglish(o2Name)) {
                    return Collator.getInstance(Locale.CHINA).compare(o1Name, o2Name);
                } else if (StringUtils.isFirstCharEnglish(o1Name)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }
}
