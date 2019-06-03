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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.bean.chat.InsertModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.privates.InputMethodUtils;
import com.inspur.emmcloud.util.privates.NetUtils;

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
public class ECMChatInputMenuImgCommentV0 extends LinearLayout {

    private static final int MENTIONS_RESULT = 5;
    @BindView(R.id.input_edit)
    ChatInputEdit inputEdit;


    @BindView(R.id.bt_send)
    Button sendMsgBtn;
    @BindView(R.id.rl_add_menu)
    RelativeLayout addMenuLayout;

    private boolean canMentions = false;
    private ChatInputMenuListener chatInputMenuListener;
    private String cid = "";
    private boolean isMessageV0 = true;

    public ECMChatInputMenuImgCommentV0(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuImgCommentV0(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuImgCommentV0(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(final Context context) {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(context).inflate(R.layout.communication__widget_chat_input_menu_img_commentv0, this, true);
        ButterKnife.bind(this, view);
        initInputEdit();
    }

    private void initInputEdit() {
        inputEdit.setFocusable(true);
        inputEdit.setFocusableInTouchMode(true);
        inputEdit.requestFocus();
        inputEdit.setInputWatcher(new ChatInputEdit.InputWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isContentBlank = (s.length() == 0);
                sendMsgBtn.setEnabled(!isContentBlank);
//                sendMsgBtn.setBackgroundResource(isContentBlank ? R.drawable.bg_chat_input_send_btn_disable : R.drawable.bg_chat_input_send_btn_enable);
                sendMsgBtn.setTextColor(isContentBlank ? Color.parseColor("#999999") : Color.parseColor("#333333"));
                if (canMentions && count == 1) {
                    String inputWord = s.toString().substring(start, start + count);
                    if (inputWord.equals("@")) {
                        openMentionPage(true);
                    }
                }
            }
        });
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
            insertModel = new InsertModel("@", uid, name);
            inputEdit.insertSpecialStr(isInputKeyWord, insertModel);
        }
    }

    @OnClick({R.id.bt_send, R.id.bt_cancel})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.bt_send:
                if (NetUtils.isNetworkConnected(getContext())) {
                    List<String> urlList = null;
                    String content = inputEdit.getRichContent(isMessageV0);
                    Map<String, String> mentionsMap = null;
                    if (isMessageV0) {
                        urlList = getContentUrlList(inputEdit.getText().toString());
                    } else {
                        mentionsMap = inputEdit.getMentionsMap();
                    }
                    chatInputMenuListener.onSendMsg(content, getContentMentionUidList(), urlList, mentionsMap);

                    inputEdit.setText("");
                }
                break;
            case R.id.bt_cancel:
                if (chatInputMenuListener != null) {
                    chatInputMenuListener.hideChatInputMenu();
                }
                break;
        }
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


    public interface ChatInputMenuListener {
        void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap);

        void hideChatInputMenu();
    }


}
