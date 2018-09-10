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
import android.os.Handler;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.MediaPlayerUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;
import com.inspur.emmcloud.widget.audiorecord.AudioRecordButton;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * com.inspur.emmcloud.widget.ECMChatInputMenu create at 2016年11月24日 上午10:25:52
 */
public class ECMChatInputMenu extends LinearLayout {

    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int CHOOSE_FILE = 4;
    private static final int MENTIONS_RESULT = 5;
    private static final long MENTIONS_BASE_TIME = 1515513600000L;
    private static final int TAG_KEYBOARD_INPUT = 0;
    private static final int TAG_VOICE_INPUT = 1;

    @ViewInject(R.id.input_edit)
    private ChatInputEdit inputEdit;

    @ViewInject(R.id.add_btn)
    private ImageButton addBtn;

    @ViewInject(R.id.send_msg_btn)
    private Button sendMsgBtn;

    @ViewInject(R.id.add_menu_layout)
    private RelativeLayout addMenuLayout;

    @ViewInject(R.id.viewpager_layout)
    private ECMChatInputMenuViewpageLayout viewpagerLayout;

    @ViewInject(R.id.volume_level_img)
    private ImageView volumeLevelImg;

    @ViewInject(R.id.voice_input_layout)
    private LinearLayout voiceInputLayout;

    @ViewInject(R.id.voice_btn)
    private ImageButton voiceBtn;

    @ViewInject(R.id.bt_audio_record)
    private AudioRecordButton audioRecordBtn;


    private boolean canMentions = false;
    private ChatInputMenuListener chatInputMenuListener;
    private List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
    private View otherLayoutView;
    private Voice2StringMessageUtils voice2StringMessageUtils;
    private MediaPlayerUtils mediaPlayerUtils;
    private String cid = "";
    private String inputs = "";
    private boolean isSpecialUser = false; //小智机器人进行特殊处理

    public ECMChatInputMenu(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(final Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(context).inflate(R.layout.ecm_widget_chat_input_menu, this, true);
        x.view().inject(view);
        initInputEdit();
        initVoiceInput();
        initAudioRecord();
    }

    private void initInputEdit() {
        inputEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP
                        && addMenuLayout.isShown()) {
                    setOtherLayoutHeightLock(true);
                    setAddMenuLayoutShow(false);
                    setOtherLayoutHeightLock(false);
                }
                return false;
            }
        });
        inputEdit.setInputWatcher(new ChatInputEdit.InputWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isContentBlank = (s.length() == 0);
                sendMsgBtn.setVisibility(isContentBlank ? (inputs.equals("1")) ? VISIBLE : GONE : VISIBLE);
                sendMsgBtn.setEnabled(!isContentBlank);
                sendMsgBtn.setBackgroundResource(isContentBlank ? R.drawable.bg_chat_input_send_btn_disable : R.drawable.bg_chat_input_send_btn_enable);
                if (StringUtils.isBlank(inputs) || (!StringUtils.isBlank(inputs) && !inputs.equals("1"))) {
                    addBtn.setVisibility(isContentBlank ? VISIBLE : GONE);
                }
                if (canMentions && count == 1) {
                    String inputWord = s.toString().substring(start, start + count);
                    if (inputWord.equals("@")) {
                        openMentionPage(true);
                    }
                }
            }
        });
    }

    private void initAudioRecord() {
        audioRecordBtn.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {

            @Override
            public void onStartRecordingVoice() {
                LogUtils.YfcDebug("开始录音");
                voice2StringMessageUtils.startVoiceListening();
            }

            @Override
            public void onFinished(float seconds, String filePath) {
                // TODO Auto-generated method stub
                if (chatInputMenuListener != null) {
                    chatInputMenuListener.onSendVoiceRecordMsg(seconds, filePath);
                }
            }

            @Override
            public void onErrorRecordingVoice() {
                LogUtils.YfcDebug("结束录音");
                voice2StringMessageUtils.stopListening();
            }
        });
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

    /**
     * 设置是否区分对待
     *
     * @param isSpecialUser
     */
    public void setSpecialUser(boolean isSpecialUser) {
        this.isSpecialUser = isSpecialUser;
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

    /**
     * 根据二进制字符串更新菜单视图
     * 此处与IOS客户端略有不同，IOS客户端当inputs为"2"时则隐藏整个输入面板，没有任何输入入口
     * 服务端允许输入类型1支持，0不支持
     * 每一位bit代表的意义为（高位）video，voice，command，file，photo，text（低位）
     *
     * @param inputs
     */
    public void setInputLayout(String inputs) {
        inputTypeBeanList.clear();
        inputEdit.clearInsertModelList();
        this.inputs = inputs;
        if (inputs.equals("0")) {
            this.setVisibility(View.GONE);
        } else {
            //功能组的图标，名称
            int[] functionIconArray = {R.drawable.ic_chat_input_add_gallery,
                    R.drawable.ic_chat_input_add_camera, R.drawable.ic_chat_input_add_file, R.drawable.ic_chat_input_add_voice_2_word,
                    R.drawable.ic_chat_input_add_mention,R.drawable.ic_chat_input_add_voice_call};
            String[] functionNameArray = {getContext().getString(R.string.album),
                    getContext().getString(R.string.take_photo),
                    getContext().getString(R.string.file), getContext().getString(R.string.voice_input), "@",getContext().getString(R.string.voice_call)};
            String[] functionActionArray = {"gallery", "camera", "file", "voice_input", "mention","voice_call"};
            String inputControl = "-1";
            if (!StringUtils.isBlank(inputs)) {
                try {
                    inputControl = new StringBuffer(Integer.toBinaryString(Integer.parseInt(inputs))).reverse().toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //处理默认情况，也就是普通频道的情况
            if (inputControl.equals("-1")) {
                //目前开放三位，有可能扩展
                inputControl = "11101";
            }
            //控制binaryString长度，防止穿的数字过大
            int length = inputControl.length() > 5 ? 5 : inputControl.length();
            boolean isInputTextEnable = true;
            boolean isInputPhotoEnable = true;
            boolean isInputFileEnable = true;
            boolean isInputVoiceEnable = true;
            boolean isVoiceCallEnable = true;

            for (int i = 0; i < length; i++) {
                String controlValue = inputControl.charAt(i) + "";
                switch (i) {
                    case 0:
                        isInputTextEnable = controlValue.equals("1");
                        break;
                    case 1:
                        isInputPhotoEnable = controlValue.equals("1");
                        break;
                    case 2:
                        isInputFileEnable = controlValue.equals("1");
                        break;
                    case 4:
                        isInputVoiceEnable = controlValue.equals("1");
                        break;
                }
            }

            if (isInputPhotoEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[0], functionNameArray[0], functionActionArray[0]));
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[1], functionNameArray[1], functionActionArray[1]));
            }
            if (isInputFileEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[2], functionNameArray[2], functionActionArray[2]));
            }
            if (isInputVoiceEnable) {
                voiceBtn.setVisibility(VISIBLE);
            }
            if (isInputTextEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[3], functionNameArray[3], functionActionArray[3]));
            } else {
                sendMsgBtn.setEnabled(false);
            }
            //如果是群组的话添加@功能
            if (canMentions) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[4], functionNameArray[4], functionActionArray[4]));
            }

            if (inputTypeBeanList.size() > 0) {
                addBtn.setVisibility(VISIBLE);
                sendMsgBtn.setVisibility(GONE);
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
                            openMentionPage(false);
                            break;
                        case "voice_input":
                            addMenuLayout.setVisibility(GONE);
                            voiceInputLayout.setVisibility(View.VISIBLE);
                            volumeLevelImg.setImageLevel(0);
                            mediaPlayerUtils.playVoiceOn();
                            voice2StringMessageUtils.startVoiceListening();
                            break;
                        case "voice_call":
                            //语音通话
                            if(!canMentions){
                                chatInputMenuListener.onVoiceCommucaiton();
                            }else{
                                AppUtils.openChannelMemeberSelect((Activity)getContext(),cid,6);
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

    /**
     * 初始化语言输入相关
     */
    private void initVoiceInput() {
        mediaPlayerUtils = new MediaPlayerUtils(getContext());
        voice2StringMessageUtils = new Voice2StringMessageUtils(getContext());
        voice2StringMessageUtils.setOnVoiceResultCallback(new OnVoiceResultCallback() {
            @Override
            public void onVoiceStart() {
            }

            @Override
            public void onVoiceResult(String results, boolean isLast) {
                if (results.length() == 1 && StringUtils.isSymbol(results)) {
                    results = "";
                }
                if (!StringUtils.isBlank(results)) {
                    if (isSpecialUser) {
                        inputEdit.clearInsertModelList();
                        if (chatInputMenuListener != null) {
                            chatInputMenuListener.onSendMsg(results, null, null, null);
                        }
                    } else {
                        int index = inputEdit.getSelectionStart();
                        Editable editable = inputEdit.getText();
                        editable.insert(index, results);
                    }

                }
            }

            @Override
            public void onVoiceFinish() {
                stopVoiceInput();
            }

            @Override
            public void onVoiceLevelChange(int volume) {

                setVoiceImageViewLevel(volume);
            }
        });
    }


    private void setVoiceInputStatus(int tag) {
        if (voiceBtn.getTag() == null || (int) voiceBtn.getTag() != tag) {
            voiceBtn.setTag(tag);
            voiceBtn.setImageResource((tag == 0) ? R.drawable.ic_chat_input_voice : R.drawable.ic_chat_input_keyboard);
            inputEdit.setVisibility((tag == 0) ? VISIBLE : GONE);
            audioRecordBtn.setVisibility((tag == 0) ? GONE : VISIBLE);
        }
    }

    @Event({R.id.voice_btn, R.id.send_msg_btn, R.id.add_btn, R.id.voice_input_close_img})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_btn:
                if (view.getTag() == null || (int) view.getTag() == TAG_KEYBOARD_INPUT) {
                    setVoiceInputStatus(TAG_VOICE_INPUT);
                    if (addMenuLayout.isShown()) {
                        addMenuLayout.setVisibility(View.GONE);
                    } else if (InputMethodUtils.isSoftInputShow((Activity) getContext())) {
                        InputMethodUtils.hide((Activity) getContext());
                    }
                } else {
                    setVoiceInputStatus(TAG_KEYBOARD_INPUT);
                    InputMethodUtils.display((Activity) getContext(), inputEdit, 0);
                }
                break;
            case R.id.send_msg_btn:
                if (NetUtils.isNetworkConnected(getContext())) {
                    List<String> urlList = null;
                    String content = inputEdit.getRichContent(false);
                    Map<String, String> mentionsMap = null;
                    mentionsMap = inputEdit.getMentionsMap();
                    if (chatInputMenuListener != null) {
                        chatInputMenuListener.onSendMsg(content, getContentMentionUidList(), urlList, mentionsMap);
                    }
                    inputEdit.clearInsertModelList();
                    inputEdit.setText("");
                }
                break;
            case R.id.add_btn:
                if (addMenuLayout.isShown()) {
                    setOtherLayoutHeightLock(true);
                    setAddMenuLayoutShow(false);
                    setOtherLayoutHeightLock(false);
                } else if (InputMethodUtils.isSoftInputShow((Activity) getContext())) {
                    setOtherLayoutHeightLock(true);
                    setAddMenuLayoutShow(true);
                    setOtherLayoutHeightLock(false);
                } else {
                    setAddMenuLayoutShow(true);
                }
                setVoiceInputStatus(TAG_KEYBOARD_INPUT);
                break;
            case R.id.voice_input_close_img:
                voiceInputLayout.setVisibility(View.GONE);
                voice2StringMessageUtils.stopListening();
                break;
            default:
                break;
        }
    }

    public boolean isVoiceInput() {
        return voiceInputLayout.getVisibility() == View.VISIBLE;
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


    public void setOtherLayoutView(View otherLayoutView, View listContentView) {
        this.otherLayoutView = otherLayoutView;
        //当View有touch事件时把软键盘和输入菜单隐藏
        otherLayoutView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (addMenuLayout.getVisibility() != View.GONE) {
                    addMenuLayout.setVisibility(View.GONE);
                }
                InputMethodUtils.hide((Activity) getContext());
                return false;
            }
        });

        listContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideAddMenuLayout();
                InputMethodUtils.hide((Activity) getContext());
                return false;
            }
        });
    }

    public void setChatInputMenuListener(
            ChatInputMenuListener chatInputMenuListener) {
        this.chatInputMenuListener = chatInputMenuListener;
    }

    private void setOtherLayoutHeightLock(boolean isLock) {
        if (otherLayoutView != null) {
            final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) otherLayoutView
                    .getLayoutParams();
            if (isLock) {
                params.height = otherLayoutView.getHeight();
                params.weight = 0.0F;
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        params.weight = 1.0F;
                    }
                }, 200);
            }
        }
    }

    public void setAddMenuLayoutShow(boolean isShow) {
        if (isShow) {
            int softInputHeight = InputMethodUtils.getSupportSoftInputHeight((Activity) getContext());
            if (softInputHeight == 0) {
                softInputHeight = PreferencesUtils.getInt(getContext(), Constant.PREF_SOFT_INPUT_HEIGHT,
                        DensityUtil.dip2px(getContext(), 274));
            }
            InputMethodUtils.hide((Activity) getContext());
            addMenuLayout.getLayoutParams().height = softInputHeight;
            addMenuLayout.setVisibility(View.VISIBLE);
        } else if (addMenuLayout.isShown()) {
            addMenuLayout.setVisibility(View.GONE);
             InputMethodUtils.display((Activity) getContext(), inputEdit, 0);
        }

    }

    public boolean isAddMenuLayoutShow() {
        return addMenuLayout.isShown();
    }


    public void hideAddMenuLayout() {
        addMenuLayout.setVisibility(View.GONE);
    }

    /**
     * 释放MediaPlay资源
     */
    public void releaseVoliceInput() {
        if (voice2StringMessageUtils.getSpeechRecognizer() != null) {
            mediaPlayerUtils.release();
            voice2StringMessageUtils.getSpeechRecognizer().cancel();
            voice2StringMessageUtils.getSpeechRecognizer().destroy();
        }
    }

    /**
     * 设置音量
     *
     * @param volume
     */
    public void setVoiceImageViewLevel(int volume) {
        if (volume <= 5) {
            volumeLevelImg.setImageLevel(0);
        } else if (volume <= 8) {
            volumeLevelImg.setImageLevel(1);
        } else if (volume <= 10) {
            volumeLevelImg.setImageLevel(2);
        } else if (volume <= 13) {
            volumeLevelImg.setImageLevel(3);
        } else if (volume <= 15) {
            volumeLevelImg.setImageLevel(4);
        } else if (volume <= 18) {
            volumeLevelImg.setImageLevel(5);
        } else if (volume <= 20) {
            volumeLevelImg.setImageLevel(6);
        } else if (volume <= 23) {
            volumeLevelImg.setImageLevel(7);
        } else if (volume <= 25) {
            volumeLevelImg.setImageLevel(8);
        } else {
            volumeLevelImg.setImageLevel(9);
        }
    }

    /**
     * 停止识别，并播放停止提示音
     */
    public void stopVoiceInput() {
        voiceInputLayout.setVisibility(View.GONE);
        voice2StringMessageUtils.stopListening();
        volumeLevelImg.setImageLevel(10);
        mediaPlayerUtils.playVoiceOff();
    }


    public interface ChatInputMenuListener {
        void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap);

        void onSendVoiceRecordMsg(float seconds, String filePath);


        void onVoiceCommucaiton();
    }


}
