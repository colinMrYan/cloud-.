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
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MsgInputAddItemAdapter;
import com.inspur.emmcloud.bean.chat.InsertModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.OnStartListeningListener;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * com.inspur.emmcloud.widget.ECMChatInputMenu create at 2016年11月24日 上午10:25:52
 */
public class ECMChatInputMenu extends LinearLayout {

    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int CHOOSE_FILE = 4;
    private static final int MENTIONS_RESULT = 5;
    private Context context;
    private LayoutInflater layoutInflater;
    private ChatInputEdit inputEdit;
    private ImageView addImg;
    private Button sendMsgBtn;
    private RelativeLayout addMenuLayout;
    private boolean isChannelGroup = false;
    private String cid = "";
    private InputMethodManager mInputManager;
    private ChatInputMenuListener chatInputMenuListener;
    private MsgInputAddItemAdapter msgInputAddItemAdapter;
    private List<Integer> imgList = new ArrayList<>();
    private boolean isSetWindowListener = true;//是否监听窗口变化自动跳转输入框ui
    private OnStartListeningListener onStartListeningListener;

    // private View view ;

    public ECMChatInputMenu(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context, null);
    }

    public ECMChatInputMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context, attrs);
    }

    public ECMChatInputMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setOnStartListeningListener(OnStartListeningListener onStartListeningListener) {
        this.onStartListeningListener = onStartListeningListener;
    }

    private void init(final Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ECMChatInputMenu);
        String layoutType = a.getString(R.styleable.ECMChatInputMenu_layoutType);
        if (layoutType != null && layoutType.equals("img_comment")) {
            layoutInflater.inflate(R.layout.ecm_widget_chat_input_menu_img_comment, this);
        } else {
            layoutInflater.inflate(R.layout.ecm_widget_chat_input_menu, this);
        }
        a.recycle();
        mInputManager = (InputMethodManager) context
                .getSystemService(context.INPUT_METHOD_SERVICE);
        addImg = (ImageView) findViewById(R.id.add_img);
        addMenuLayout = (RelativeLayout) findViewById(R.id.add_menu_layout);
        sendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
        sendMsgBtn.setEnabled(false);
        sendMsgBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (NetUtils.isNetworkConnected(context)) {
                    List<String> urlList = getContentUrlList(inputEdit.getText().toString());
                    String content = inputEdit.getRichContent();
                    List<String> mentionsUidList = getContentMentionUidList();
                    chatInputMenuListener.onSendMsg(content, mentionsUidList, urlList);
                    inputEdit.setText("");
                }
            }
        });
        addImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (addMenuLayout.isShown()) {
                    lockContentHeight();
                    hideAddItemLayout(true);
                    unlockContentHeight();
                } else if (isSoftInputShown()) {
                    lockContentHeight();
                    showAddItemLayout();
                    unlockContentHeight();
                } else {
                    showAddItemLayout();
                }
            }
        });
        initInputEdit();
        initMenuGrid();

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

    public void initInputEdit() {
        inputEdit = (ChatInputEdit) findViewById(R.id.input_edit);
        inputEdit.setFocusable(true);
        inputEdit.setFocusableInTouchMode(true);
        inputEdit.requestFocus();
        inputEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isSetWindowListener) {
                    if (event.getAction() == MotionEvent.ACTION_UP
                            && addMenuLayout.isShown()) {
                        lockContentHeight();
                        hideAddItemLayout(true);
                        unlockContentHeight();
                    }
                }
                return false;
            }
        });
        inputEdit.setInputWatcher(new ChatInputEdit.InputWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isContentBlank = (s.length() == 0);
                sendMsgBtn.setEnabled(!isContentBlank);
                sendMsgBtn.setBackgroundResource(isContentBlank ? R.drawable.bg_chat_input_send_btn_disable : R.drawable.bg_chat_input_send_btn_enable);
                if (isChannelGroup && count == 1) {
                    String inputWord = s.toString().substring(start, start + count);
                    if (inputWord.equals("@")) {
                        openMention(true);
                    }
                }
            }
        });
    }

    public void setWindowListener(boolean isSetWindowListener) {
        this.isSetWindowListener = isSetWindowListener;
    }

    public EditText getEdit() {
        return inputEdit;
    }


    private void lockContentHeight() {
        chatInputMenuListener.onSetContentViewHeight(true);
    }

    private void unlockContentHeight() {
        chatInputMenuListener.onSetContentViewHeight(false);
    }

    public void showAddBtn(boolean isShowHideBtn) {
        addImg.setVisibility(isShowHideBtn ? View.VISIBLE : View.GONE);
    }


    public void hideAddItemLayout(boolean showSoftInput) {
        if (addMenuLayout.isShown()) {
            addMenuLayout.setVisibility(View.GONE);
            if (showSoftInput) {
                showSoftInput();
            }
        }
    }

    public void showAddItemLayout() {
        int softInputHeight = getSupportSoftInputHeight();
        if (softInputHeight == 0) {
            softInputHeight = PreferencesUtils.getInt(context, Constant.PREF_SOFT_INPUT_HEIGHT,
                    DensityUtil.dip2px(context, 274));
        }
        if (isSetWindowListener) {
            hideSoftInput();
        }
        addMenuLayout.getLayoutParams().height = softInputHeight;
        addMenuLayout.setVisibility(View.VISIBLE);
    }

    public void showSoftInput() {
        inputEdit.requestFocus();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mInputManager.showSoftInput(inputEdit, 0);
            }
        });
    }

    public void hideSoftInput() {
        mInputManager.hideSoftInputFromWindow(inputEdit.getWindowToken(), 0);
    }


    private boolean isSoftInputShown() {
        return getSupportSoftInputHeight() != 0;
    }

    private int getSupportSoftInputHeight() {
        Rect r = new Rect();
        ((Activity) context).getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(r);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int softInputHeight = screenHeight - r.bottom;
        if (softInputHeight < 0) {
            Log.w("EmotionInputDetector",
                    "Warning: value of softInputHeight is below zero!");
        }
        if (softInputHeight > 0) {
            PreferencesUtils.putInt(context, Constant.PREF_SOFT_INPUT_HEIGHT, softInputHeight);
        }
        return softInputHeight;
    }

    /**
     * 初始化消息发送的UI
     */
    private void initMenuGrid() {
        GridView addItemGrid = (GridView) findViewById(R.id.add_menu_grid);
        msgInputAddItemAdapter = new MsgInputAddItemAdapter(context);
        addItemGrid.setAdapter(msgInputAddItemAdapter);
        addItemGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int clickItem = imgList.get(position);
                switch (clickItem) {
                    case R.drawable.ic_chat_input_add_gallery:
                        AppUtils.openGallery((Activity)context,5,GELLARY_RESULT);
                        break;
                    case R.drawable.ic_chat_input_add_camera:
                        String fileName = new Date().getTime() + ".jpg";
                        PreferencesUtils.putString(context, "capturekey", fileName);
                        AppUtils.openCamera((Activity)context,fileName,CAMERA_RESULT);
                        break;
                    case R.drawable.ic_chat_input_add_file:
                       AppUtils.openFileSystem((Activity)context,CHOOSE_FILE);
                        break;
                    case R.drawable.ic_chat_input_add_mention:
                        openMention(false);
                        break;
                    case R.drawable.ic_chat_input_add_voice:
                        onStartListeningListener.onStartListening();
                        break;
                    default:
                        break;
                }

            }
        });
    }

    /**
     * 是否是输入了关键字@字符打开mention页
     *
     * @param isInputKeyWord
     */
    private void openMention(boolean isInputKeyWord) {
        Intent intent = new Intent();
        intent.setClass(context, MembersActivity.class);
        intent.putExtra("title", context.getString(R.string.friend_list));
        intent.putExtra("cid", cid);
        intent.putExtra("isInputKeyWord", isInputKeyWord);
        ((Activity) context).overridePendingTransition(
                R.anim.activity_open, 0);

        ((Activity) context).startActivityForResult(intent,
                MENTIONS_RESULT);

    }

    public void setChatInputMenuListener(
            ChatInputMenuListener chatInputMenuListener) {
        this.chatInputMenuListener = chatInputMenuListener;
    }

    public interface ChatInputMenuListener {
        void onSetContentViewHeight(boolean isLock);

        void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList);

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
            inputEdit.insertSpecialStr(isInputKeyWord, new InsertModel("@", uid, name, "#99CCFF"));
        }
    }

    public void setIsChannelGroup(boolean isChannelGroup, String cid) {
        this.cid = cid;
        this.isChannelGroup = isChannelGroup;
    }

    public boolean hideAddMenuLayout() {
        if (addMenuLayout.getVisibility() != View.GONE) {
            addMenuLayout.setVisibility(View.GONE);
            return true;
        }
        return false;

    }

    /**
     * 根据二进制字符串更新菜单视图
     *
     */
    public void updateMenuGrid(String inputs) {
        String binaryString  = "-1";
        if (!StringUtils.isBlank(inputs)) {
            binaryString = Integer.toBinaryString(Integer.parseInt(inputs));
        }
        int[] imgArray = {R.drawable.ic_chat_input_add_gallery, R.drawable.ic_chat_input_add_camera, R.drawable.ic_chat_input_add_file,R.drawable.ic_chat_input_add_voice, R.drawable.ic_chat_input_add_mention};
        String[] functionNameArray = {context.getString(R.string.album), context.getString(R.string.take_photo), context.getString(R.string.file),"语音", "@"};
        imgList.clear();
        List<String> textList = new ArrayList<>();
        int menuGridSize = binaryString.length() - 1;
        if (binaryString.length() > imgArray.length) {
            menuGridSize = imgArray.length - 2;
        }
        if (binaryString.equals("-1")) {
            menuGridSize = imgArray.length - 2;
            binaryString = "1111";
        }
        for (int i = menuGridSize; i >= 0; i--) {
            if ((binaryString.charAt(i) + "").equals("1")) {
                imgList.add(imgArray[menuGridSize - i]);
                textList.add(functionNameArray[menuGridSize - i]);
            }
        }
        //如果是群组的话添加@功能
        if (isChannelGroup) {
            imgList.add(imgArray[4]);
            textList.add(functionNameArray[4]);
        }
        msgInputAddItemAdapter.updateGridView(imgList, textList);
    }
}
