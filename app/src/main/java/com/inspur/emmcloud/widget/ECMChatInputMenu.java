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
import android.text.Editable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.chat.InputTypeBean;
import com.inspur.emmcloud.bean.chat.InsertModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.MediaPlayerUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;

import java.util.ArrayList;
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
    private LayoutInflater layoutInflater;
    private ChatInputEdit inputEdit;
    private ImageView addImg;
    private Button sendMsgBtn;
    private RelativeLayout addMenuLayout;
    private boolean isChannelGroup = false;
    private String cid = "";
    private InputMethodManager mInputManager;
    private ChatInputMenuListener chatInputMenuListener;
    private boolean isSetWindowListener = true;//是否监听窗口变化自动跳转输入框ui
    private  ImageView voiceMicroPhoneImg, voicePackUpImg;
    private List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
    private MediaPlayerUtils mediaPlayerUtils;
    private ECMChatInputMenuViewpageLayout viewpagerLayout;
    private Voice2StringMessageUtils voice2StringMessageUtils;

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

    private void init(final Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
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
                    setContentHeightLock(true);
                    hideAddItemLayout(true);
                    setContentHeightLock(false);
                } else if (isSoftInputShown()) {
                    setContentHeightLock(true);
                    showAddItemLayout();
                    setContentHeightLock(false);
                } else {
                    showAddItemLayout();
                }
            }
        });
        initInputEdit();
        initVoiceInput();
    }

    /**
     * 初始化语言输入相关
     */
    private void initVoiceInput(){
        voiceMicroPhoneImg = (ImageView) findViewById(R.id.voice_volume_img);
        voicePackUpImg = (ImageView) findViewById(R.id.voice_back_img);
        viewpagerLayout = (ECMChatInputMenuViewpageLayout)findViewById(R.id.viewpager_layout);
        voiceMicroPhoneImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceMicroPhoneImg.setImageLevel(0);
                mediaPlayerUtils.playVoiceOn();
                voice2StringMessageUtils.startVoiceListening();
            }
        });
        voicePackUpImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewpagerLayout.setVisibility(View.VISIBLE);
                voiceMicroPhoneImg.setVisibility(View.GONE);
                voicePackUpImg.setVisibility(View.GONE);
                voice2StringMessageUtils.stopListening();
            }
        });
        mediaPlayerUtils = new MediaPlayerUtils(getContext());
        voice2StringMessageUtils = new Voice2StringMessageUtils(getContext());
        voice2StringMessageUtils.setOnVoiceResultCallback(new OnVoiceResultCallback() {
            @Override
            public void onVoiceStart() {

            }

            @Override
            public void onVoiceResult(String results, boolean isLast) {
                if (results.length() == 1 && StringUtils.isSymbol(results)){
                    results = "";
                }
                int index = inputEdit.getSelectionStart();
                Editable editable = inputEdit.getText();
                editable.insert(index, results);
            }

            @Override
            public void onVoiceFinish() {
                stopVoiceReleaseMediaPlay();
            }

            @Override
            public void onVoiceLevelChange(int volume) {
                setVoiceImageViewLevel(volume);
            }
        });
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
                        setContentHeightLock(true);
                        hideAddItemLayout(true);
                        setContentHeightLock(false);
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

    /**
     * 是否是输入了关键字@字符打开mention页
     *
     * @param isInputKeyWord
     */
    private void openMention(boolean isInputKeyWord) {
        Intent intent = new Intent();
        intent.setClass(getContext(), MembersActivity.class);
        intent.putExtra("title", getContext().getString(R.string.friend_list));
        intent.putExtra("cid", cid);
        intent.putExtra("isInputKeyWord", isInputKeyWord);
        ((Activity) getContext()).overridePendingTransition(
                R.anim.activity_open, 0);

        ((Activity) getContext()).startActivityForResult(intent,
                MENTIONS_RESULT);

    }

    public void setWindowListener(boolean isSetWindowListener) {
        this.isSetWindowListener = isSetWindowListener;
    }

    public EditText getEdit() {
        return inputEdit;
    }


    private void setContentHeightLock(boolean isLock) {
        if (chatInputMenuListener != null){
            chatInputMenuListener.onSetContentViewHeight(isLock);
        }
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
            softInputHeight = PreferencesUtils.getInt(getContext(), Constant.PREF_SOFT_INPUT_HEIGHT,
                    DensityUtil.dip2px(getContext(), 274));
        }
        if (isSetWindowListener) {
            hideSoftInput();
        }
        addMenuLayout.getLayoutParams().height = softInputHeight;
        addMenuLayout.setVisibility(View.VISIBLE);

        viewpagerLayout.setVisibility(View.VISIBLE);
        voiceMicroPhoneImg.setVisibility(View.GONE);
        voicePackUpImg.setVisibility(View.GONE);
    }

    public void showSoftInput() {
        voice2StringMessageUtils.stopListening();
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
        ((Activity) getContext()).getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(r);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int softInputHeight = screenHeight - r.bottom;
        if (softInputHeight < 0) {
            Log.w("EmotionInputDetector",
                    "Warning: value of softInputHeight is below zero!");
        }
        if (softInputHeight > 0) {
            PreferencesUtils.putInt(getContext(), Constant.PREF_SOFT_INPUT_HEIGHT, softInputHeight);
        }
        return softInputHeight;
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
     * 释放MediaPlay资源
     */
    public void releaseVoliceInput(){
        if (voice2StringMessageUtils.getSpeechRecognizer() != null){
            mediaPlayerUtils.release();
            voice2StringMessageUtils.getSpeechRecognizer().cancel();
            voice2StringMessageUtils.getSpeechRecognizer().destroy();
        }
    }

    /**
     * 设置音量
     * @param volume
     */
    public void setVoiceImageViewLevel(int volume){
        if(volume <= 5){
            voiceMicroPhoneImg.setImageLevel(0);
        }else if(volume <= 8){
            voiceMicroPhoneImg.setImageLevel(1);
        }else if(volume <= 10){
            voiceMicroPhoneImg.setImageLevel(2);
        }else if(volume <= 13){
            voiceMicroPhoneImg.setImageLevel(3);
        }else if(volume <= 15){
            voiceMicroPhoneImg.setImageLevel(4);
        }else if(volume <= 18){
            voiceMicroPhoneImg.setImageLevel(5);
        }else if(volume <= 20){
            voiceMicroPhoneImg.setImageLevel(6);
        }else if(volume <= 23){
            voiceMicroPhoneImg.setImageLevel(7);
        }else if(volume <= 25){
            voiceMicroPhoneImg.setImageLevel(8);
        }else{
            voiceMicroPhoneImg.setImageLevel(9);
        }
    }

    /**
     * 停止识别，并播放停止提示音
     */
    public void stopVoiceReleaseMediaPlay(){
        voiceMicroPhoneImg.setImageLevel(10);
        mediaPlayerUtils.playVoiceOff();
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
     * 此处与IOS客户端略有不同，IOS客户端当inputs为"2"时则隐藏整个输入面板，没有任何输入入口
     * 服务端允许输入类型1支持，0不支持
     * 每一位bit代表的意义为（高位）video，voice，command，file，photo，text（低位）
     *
     * @param inputs
     */
    public void updateCommonMenuLayout(String inputs) {
        //功能组的图标，名称
        int[] functionIconArray = {R.drawable.ic_chat_input_add_gallery,
                R.drawable.ic_chat_input_add_camera, R.drawable.ic_chat_input_add_file,
                R.drawable.ic_chat_input_add_voice,R.drawable.ic_chat_input_add_mention};
        String[] functionNameArray = {getContext().getString(R.string.album),
                getContext().getString(R.string.take_photo),
                getContext().getString(R.string.file),
                getContext().getString(R.string.voice_input),"@"};
        String[] actionArray={"gallery","camera","file","voice2word","mention"};
        String binaryString  = "-1";
        //如果第一位是且只能是1即 "1" 如果inputs是其他，例如"2"则走下面逻辑
        //这种情况是只开放了输入文字的权限
        if(!StringUtils.isBlank(inputs)){
            if(inputs.equals("1")){
                addImg.setVisibility(View.GONE);
                return;
            }
            binaryString = new StringBuffer(Integer.toBinaryString(Integer.parseInt(inputs))).reverse().toString();
        }
        //处理默认情况，也就是普通频道的情况
        if (binaryString.equals("-1")) {
            //目前开放三位，有可能扩展
            binaryString = "1111";
        }
        //控制binaryString长度，防止穿的数字过大
        int binaryLength = binaryString.length() > 4 ? 4 : binaryString.length();
        for(int i=0; i < binaryLength; i++){
            //第一位已经处理过了，这里不再处理
            //这里如果禁止输入文字时，inputEdit设置Enabled
            if (i == 0) {
                inputEdit.setEnabled((binaryString.charAt(0) + "").equals("1"));
                continue;
            }
            if ((binaryString.charAt(i) + "").equals("1")) {
                //对于第二位特殊处理，如果第二位是"1"则添加相册，拍照两个功能，与服务端确认目前这样实现
                //存在的疑问，如果仅显示相册或仅显示拍照应该如何处理？
                if (i == 1) {
                    InputTypeBean inputTypeBeanGallery = new InputTypeBean(functionIconArray[0], functionNameArray[0],actionArray[0]);
                    inputTypeBeanList.add(inputTypeBeanGallery);
                    InputTypeBean inputTypeBeanCamera = new InputTypeBean(functionIconArray[1], functionNameArray[1],actionArray[1]);
                    inputTypeBeanList.add(inputTypeBeanCamera);
                } else {
                    InputTypeBean inputTypeBean = new InputTypeBean(functionIconArray[i], functionNameArray[i],actionArray[i]);
                    inputTypeBeanList.add(inputTypeBean);
                }
            }
        }
        //如果是群组的话添加@功能
        if (isChannelGroup) {
            InputTypeBean inputTypeBean = new InputTypeBean(functionIconArray[4], functionNameArray[4],actionArray[4]);
            inputTypeBeanList.add(inputTypeBean);
        }
        viewpagerLayout.setOnGridItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputTypeBean inputTypeBean = inputTypeBeanList.get(position);
                switch (inputTypeBean.getAction()) {
                    case "gallery":
                        AppUtils.openGallery((Activity) getContext(), 5, GELLARY_RESULT);
                        break;
                    case "camera":
                        String fileName = System.currentTimeMillis() + ".jpg";
                        PreferencesUtils.putString(getContext(), "capturekey", fileName);
                        AppUtils.openCamera((Activity) getContext(), fileName, CAMERA_RESULT);
                        break;
                    case "file":
                        AppUtils.openFileSystem((Activity) getContext(), CHOOSE_FILE);
                        break;
                    case "mention":
                        openMention(false);
                        break;
                    case "voice2word":
                        if(NetUtils.isNetworkConnected(getContext())){
                            mediaPlayerUtils.playVoiceOn();
                            voice2StringMessageUtils.startVoiceListening();
                            viewpagerLayout.setVisibility(View.GONE);
                            voiceMicroPhoneImg.setImageLevel(0);
                            voiceMicroPhoneImg.setVisibility(View.VISIBLE);
                            voicePackUpImg.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        viewpagerLayout.setInputTypeBeanList(inputTypeBeanList);
    }
}
