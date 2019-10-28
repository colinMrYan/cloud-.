/**
 * ECMChatInputMenu.java
 * classes : com.inspur.emmcloud.widget.ECMChatInputMenu
 * V 1.0.0
 * Create at 2016年11月24日 上午10:25:52
 */
package com.inspur.emmcloud.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.widget.richedit.InsertModel;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.chat.emotion.EmotionAdapter;
import com.inspur.emmcloud.ui.chat.emotion.EmotionRecentManager;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * com.inspur.emmcloud.widget.ECMChatInputMenu create at 2016年11月24日 上午10:25:52
 */
public class ECMChatInputMenuImgComment extends LinearLayout {

    private static final int MENTIONS_RESULT = 5;
    private static final long MENTIONS_BASE_TIME = 1515513600000L;
    @BindView(R.id.input_edit)
    ChatInputEdit inputEdit;

    @BindView(R.id.bt_send)
    Button sendBtn;

    @BindView(R.id.rl_add_menu)
    RelativeLayout addMenuLayout;
    /**
     * 表情相关
     */
    @BindView(R.id.emotion_container)
    View emotionLayout;
    @BindView(R.id.emotion_delete)
    ImageView emotionDeleteImg;
    @BindView(R.id.emotion_recent_layout)
    View emotionRecentLayout;
    @BindView(R.id.emotion_recent_grid)
    NoScrollGridView emotionRecentGrid;
    @BindView(R.id.emotion_grid)
    NoScrollGridView emotionGrid;
    @BindView(R.id.emotion_btn)
    ImageButton emotionBtn;
    EmotionAdapter emotionAdapter;
    EmotionAdapter emotionRecentAdapter;
    @BindView(R.id.at_people_btn)
    ImageButton atPeopleBtn;

    private boolean canMentions = false;
    private ChatInputMenuListener chatInputMenuListener;
    private String cid = "";

    public ECMChatInputMenuImgComment(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuImgComment(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuImgComment(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(final Context context) {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(context).inflate(R.layout.communication_widget_chat_input_menu_img_comment, this, true);
        ButterKnife.bind(this, view);
        initInputEdit();
        initEmotion();
        sendBtn.setEnabled(false);
    }

    private void initInputEdit() {
        inputEdit.setFocusable(true);
        inputEdit.setFocusableInTouchMode(true);
        inputEdit.requestFocus();
        inputEdit.setInputWatcher(new ChatInputEdit.InputWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isContentBlank = (s.length() == 0);
                sendBtn.setEnabled(!isContentBlank);
                sendBtn.setTextColor(isContentBlank ? Color.parseColor("#999999") : Color.parseColor("#000000"));
                if (canMentions && count == 1) {
                    String inputWord = s.toString().substring(start, start + count);
                    if (inputWord.equals("@")) {
                        openMentionPage(true);
                    }
                }
            }
        });
    }

    private void initEmotion() {
        EmotionRecentManager recentManager = EmotionRecentManager.getInstance(getContext());
        emotionRecentAdapter = new EmotionAdapter(getContext(), 1, recentManager);
        emotionRecentGrid.setAdapter(emotionRecentAdapter);
        emotionRecentGrid.setOnItemClickListener(new OnEmotionItemClickListener());

        List<String> resList = EmotionUtil.getInstance(getContext()).getExpressionRes();
        emotionAdapter = new EmotionAdapter(getContext(), 1, resList);
        emotionGrid.setAdapter(emotionAdapter);
        emotionGrid.setOnItemClickListener(new OnEmotionItemClickListener());
    }

    public ChatInputEdit getChatInputEdit() {
        return inputEdit;
    }

    /**
     * 是否是输入了关键字@字符打开mention页
     *
     * @param isInputKeyWord
     */
    private void openMentionPage(boolean isInputKeyWord) {
        Intent intent = new Intent();
        intent.setClass(getContext(), MembersActivity.class);
        intent.putExtra("title", getContext().getString(R.string.friend_list));
        intent.putExtra("cid", cid);
        intent.putExtra(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.MENTIONS_STATE);
        intent.putExtra("isInputKeyWord", isInputKeyWord);
        ((Activity) getContext()).overridePendingTransition(
                R.anim.activity_open, 0);

        ((Activity) getContext()).startActivityForResult(intent,
                MENTIONS_RESULT);

    }

    public void setChatInputMenuListener(
            ChatInputMenuListener chatInputMenuListener) {
        this.chatInputMenuListener = chatInputMenuListener;
    }

    public void setAddMenuLayoutShow(boolean isShow) {
        if (isShow) {
            int softInputHeight = InputMethodUtils.getSupportSoftInputHeight((Activity) getContext());
            if (softInputHeight == 0) {
                softInputHeight = PreferencesUtils.getInt(getContext(), Constant.PREF_SOFT_INPUT_HEIGHT,
                        DensityUtil.dip2px(getContext(), 274));
            }
            addMenuLayout.getLayoutParams().height = softInputHeight;
            addMenuLayout.setVisibility(View.VISIBLE);
        } else if (addMenuLayout.isShown()) {
            addMenuLayout.setVisibility(View.GONE);
            InputMethodUtils.hide((Activity) getContext());
        }

    }

    /**
     * 设置是否可以@
     *
     * @param canMentions
     * @param cid
     */
    public void setCanMentions(boolean canMentions, String cid) {
        this.canMentions = canMentions;
        this.cid = cid;
        atPeopleBtn.setVisibility(canMentions ? VISIBLE : GONE);
    }

    public void showSoftInput(boolean isShow) {
        if (isShow) {
            InputMethodUtils.display((Activity) getContext(), inputEdit, 0);
        } else {
            InputMethodUtils.hide((Activity) getContext());
        }

    }

    /**
     * 添加mentions
     *
     * @param uid
     * @param name
     * @param isInputKeyWord
     */
    public void addMentions(String uid, String name, boolean isInputKeyWord) {
        if (uid != null && name != null) {
            InsertModel insertModel;
            insertModel = new InsertModel("@", (System.currentTimeMillis() - MENTIONS_BASE_TIME) + "", name, uid);
            inputEdit.insertSpecialStr(isInputKeyWord, insertModel);
        }
    }

    @OnClick({R.id.bt_send, R.id.bt_cancel, R.id.emotion_btn, R.id.emotion_delete, R.id.at_people_btn})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.bt_send:
                if (NetUtils.isNetworkConnected(getContext())) {
                    List<String> urlList = null;
                    String content = inputEdit.getRichContent(false);
                    Map<String, String> mentionsMap = null;
                    mentionsMap = inputEdit.getMentionsMap();
                    chatInputMenuListener.onSendMsg(content, getContentMentionUidList(), urlList, mentionsMap);
                    inputEdit.setText("");
                }
                break;
            case R.id.bt_cancel:
                if (chatInputMenuListener != null) {
                    chatInputMenuListener.hideChatInputMenu();
                }
                break;
            case R.id.emotion_btn:
                handleEmotionStatus();
                break;
            case R.id.emotion_delete:  //表情删除
                EmotionUtil.getInstance(getContext()).deleteSingleEmojcon(inputEdit);
                break;
            case R.id.at_people_btn:    //@某人
                if (canMentions) {
                    openMentionPage(true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 点击menu里的表情
     */
    private void handleEmotionStatus() {
        if (emotionRecentAdapter != null) {
            emotionRecentLayout.setVisibility(EmotionRecentManager.getInstance(getContext()).size() > 0 ? VISIBLE : GONE);
            emotionRecentAdapter.notifyDataSetChanged();
        }

        emotionLayout.setVisibility(VISIBLE);
    }


    /**
     * 获取content中urlList
     *
     * @param content
     * @return
     */
    private List<String> getContentUrlList(String content) {
        Pattern pattern = Pattern.compile(Constant.PATTERN_URL);
        ArrayList<String> urlList = new ArrayList<>();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            urlList.add(matcher.group(0));
        }
        return urlList;
    }

    /**
     * 获取mentions Uid List
     *
     * @return
     */
    private List<String> getContentMentionUidList() {
        List<String> mentionsUidList = new ArrayList<>();
        List<InsertModel> insertModelList = inputEdit.getRichInsertList();
        for (int i = 0; i < insertModelList.size(); i++) {
            InsertModel insertModel = insertModelList.get(i);
            mentionsUidList.add(insertModel.getInsertId());
        }
        return mentionsUidList;
    }

    class OnEmotionItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String filename = (parent.getId() == R.id.emotion_recent_grid ?
                    emotionRecentAdapter.getItem(position) : emotionAdapter.getItem(position));
            int selectionStart = inputEdit.getSelectionStart();// 获取光标的位置
            try {
                Class clz = Class.forName("com.inspur.emmcloud.ui.chat.emotion.EmotionUtil");
                Field field = clz.getField(filename);
                Spannable span = EmotionUtil.getInstance(getContext()).getSmiledText((String) field.get(null), inputEdit.getTextSize());
                if (selectionStart < 0 || selectionStart >= inputEdit.length()) {
                    inputEdit.getEditableText().append(span);
                } else {
                    inputEdit.getEditableText().insert(selectionStart, span);
                }
                EmotionRecentManager.getInstance(getContext()).addItem(filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public interface ChatInputMenuListener {
        void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap);

        void hideChatInputMenu();
    }


}
