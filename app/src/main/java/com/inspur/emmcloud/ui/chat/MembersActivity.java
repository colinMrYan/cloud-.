package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMemberListAdapter;
import com.inspur.emmcloud.bean.chat.PersonDto;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.slidebar.CharacterParser;
import com.inspur.emmcloud.widget.slidebar.SideBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MembersActivity extends BaseActivity implements
        SideBar.OnTouchingLetterChangedListener, TextWatcher {

    private SideBar mSideBar;
    private TextView mDialog;
    private ListView mListView;
    private TextView mHeadText;
    private EditText mSearchInput;
    private CharacterParser characterParser;// 汉字转拼音
    private PinyinComparator pinyinComparator;// 根据拼音来排列ListView里面的数据类
    private ChannelMemberListAdapter mAdapter;
    private JSONObject jsonResult;
    private String channelID = "";
    private Handler handler;
    private LoadingDialog loadingDlg;
    private List<PersonDto> filterList = new ArrayList<PersonDto>();
    private List<PersonDto> personDtoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        mListView = (ListView) findViewById(R.id.channel_member);
        mSideBar = (SideBar) findViewById(R.id.channel_sidrbar);
        mDialog = (TextView) findViewById(R.id.channel_dialog);
        mSearchInput = (EditText) findViewById(R.id.channel_member_search_input);
        mHeadText = (TextView) findViewById(R.id.header_text);
//        mSearchLayout = (RelativeLayout) findViewById(R.id.search_layout);
        mSideBar.setTextView(mDialog);

        mSideBar.setOnTouchingLetterChangedListener(this);
        channelID = getIntent().getStringExtra("cid");
        loadingDlg = new LoadingDialog(this);
        final String userid = PreferencesUtils.getString(this, "userID");
        loadingDlg.show();

        Runnable runnable = new Runnable() {

            @Override
            public void run() {

                if (!StringUtils.isBlank(channelID)) {
                    List<String> uidList = ChannelGroupCacheUtils.getMemberUidList(MembersActivity.this, channelID, 0);
                    personDtoList = ContactUserCacheUtils.getShowMemberList(uidList);
                } else if (getIntent().getStringArrayListExtra("uidList") != null) {
                    personDtoList = ContactUserCacheUtils.getShowMemberList(getIntent().getStringArrayListExtra("uidList"));
                }

                Iterator<PersonDto> personDtoIterator = personDtoList.iterator();
                while (personDtoIterator.hasNext()) {
                    PersonDto personDto = personDtoIterator.next();
                    if (personDto.getUid().contains(userid) && (!getIntent().hasExtra("search"))) {
                        personDtoIterator.remove();
                    }
                }
                handler.sendMessage(handler.obtainMessage(0));
            }
        };

        try {
            final long start = System.currentTimeMillis();
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {
                        initData();
                        if (loadingDlg.isShowing()) {
                            loadingDlg.dismiss();
                        }
                    } else {
                        if (loadingDlg.isShowing()) {
                            loadingDlg.dismiss();
                        }
                        Toast.makeText(MembersActivity.this, "加载数据出错",
                                Toast.LENGTH_SHORT).show();
                    }

                }

            };
            new Thread(runnable).start();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        mHeadText.setText(getIntent().getStringExtra("title"));
        if (getIntent().hasExtra("search")) {
//			if (getIntent().getStringExtra("search").equals("1")) {
//				mSearchLayout.setVisibility(View.GONE);
//			}

            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    String uid = "";
                    if (mSearchInput.getText().toString().length() > 0) {
                        uid = filterList.get(position).getUid();
                    } else {
                        uid = personDtoList.get(position).getUid();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("uid", uid);
                    if (uid.startsWith("BOT")) {
                        intent.setClass(getApplicationContext(), RobotInfoActivity.class);
                    } else {
                        intent.setClass(getApplicationContext(),
                                UserInfoActivity.class);
                    }
                    startActivity(intent);
                }
            });
        } else {
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    jsonResult = new JSONObject();
                    try {
                        String uid, name;
                        uid = (filterList.size() != 0) ? filterList.get(position).getUid() : personDtoList.get(position).getUid();
                        name = (filterList.size() != 0) ? filterList.get(position).getName() : personDtoList.get(position).getName();
                        jsonResult.put("uid", uid);
                        jsonResult.put("name", name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("searchResult", jsonResult.toString());
                    boolean isInputKeyWord = getIntent().getBooleanExtra("isInputKeyWord",false);
                    intent.putExtra("isInputKeyWord",isInputKeyWord);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
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
        mAdapter = new ChannelMemberListAdapter(MembersActivity.this,
                personDtoList);
        mListView.setAdapter(mAdapter);
        mSearchInput.addTextChangedListener(this);
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = 0;
        // 该字母首次出现的位置
        if (mAdapter != null) {
            position = mAdapter.getPositionForSection(s.charAt(0));
        }
        if (position != -1) {
            mListView.setSelection(position);
        }
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
        if (TextUtils.isEmpty(filterStr)) {
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
        mAdapter.updateListView(filterDateList);
        mListView.setSelection(0);
    }

    /**
     * 填充数据
     *
     * @param list
     */
    private void fillData(List<PersonDto> list) {
        ArrayList<String> indexList = new ArrayList<>();
        for (PersonDto cUserInfoDto : list) {
            if (cUserInfoDto != null && cUserInfoDto.getName() != null) {
                String pinyin = cUserInfoDto.getPinyinFull();
                String sortString = pinyin.substring(0, 1).toUpperCase();
                indexList.add(sortString);
//                mSideBar.setIndexArray(indexList);
//                mSideBar.invalidate();
                if ("1".equals(cUserInfoDto.getUtype())) {// 判断是否是管理员
                    cUserInfoDto.setSortLetters("☆");
                } else if (sortString.matches("[A-Z]")) {// 正则表达式，判断首字母是否是英文字母   jason修改crash
                    cUserInfoDto.setSortLetters(sortString);
                } else {
                    cUserInfoDto.setSortLetters("#");
                }

            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
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
