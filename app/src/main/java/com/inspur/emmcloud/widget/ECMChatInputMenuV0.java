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
import android.os.Handler;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.bean.chat.InputTypeBean;
import com.inspur.emmcloud.bean.chat.InsertModel;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.InputMethodUtils;
import com.inspur.emmcloud.util.privates.MediaPlayerUtils;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;
import com.inspur.emmcloud.util.privates.systool.emmpermission.Permissions;
import com.inspur.emmcloud.util.privates.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.util.privates.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.widget.waveprogress.WaterWaveProgress;

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
public class ECMChatInputMenuV0 extends LinearLayout {

    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int CHOOSE_FILE = 4;
    private static final int MENTIONS_RESULT = 5;
    private static final int TOPDELY_TIMES = 17;
    private static final long MENTIONS_BASE_TIME = 1515513600000L;

    @BindView(R.id.input_edit)
    ChatInputEdit inputEdit;

    @BindView(R.id.voice_btn)
    ImageButton voiceImgBtn;

    @BindView(R.id.add_btn)
    ImageButton addBtn;

    @BindView(R.id.send_msg_btn)
    Button sendMsgBtn;

    @BindView(R.id.add_menu_layout)
    RelativeLayout addMenuLayout;

    @BindView(R.id.viewpager_layout)
    ECMChatInputMenuViewpageLayout viewpagerLayout;

    @BindView(R.id.wave_progress_input)
    WaterWaveProgress waterWaveProgress;

    @BindView(R.id.voice_input_layout)
    RelativeLayout voiceInputLayout;

    private boolean canMentions = false;
    private ChatInputMenuListener chatInputMenuListener;
    private List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
    private View otherLayoutView;
    private Voice2StringMessageUtils voice2StringMessageUtils;
    private MediaPlayerUtils mediaPlayerUtils;
    private String cid = "";
    private String inputs = "";
    private boolean isSpecialUser = false; //小智机器人进行特殊处理
    private int lastVolumeLevel = 0;
    private int delayTimes = 0;

    public ECMChatInputMenuV0(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuV0(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuV0(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(final Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ECMChatInputMenu);
        View view = LayoutInflater.from(context).inflate(R.layout.communication_widget_chat_input_menu, this, true);
        ButterKnife.bind(this, view);
        initInputEdit();
        initVoiceInput();
        initViewpageLayout();
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
                if (isContentBlank) {
                    chatInputMenuListener.onChatDraftsClear();
                }
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
            insertModel = new InsertModel("@", uid, name);
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
        } else if (inputs.equals("1")) {
            sendMsgBtn.setVisibility(VISIBLE);
            sendMsgBtn.setEnabled(false);
            addBtn.setVisibility(View.GONE);
        } else {
            //功能组的图标，名称
            int[] functionIconArray = {R.drawable.ic_chat_input_add_gallery,
                    R.drawable.ic_chat_input_add_camera, R.drawable.ic_chat_input_add_file,
                    R.drawable.ic_chat_input_add_mention, R.drawable.ic_chat_input_add_voice_call};
            String[] functionNameArray = {getContext().getString(R.string.album),
                    getContext().getString(R.string.take_photo),
                    getContext().getString(R.string.file),
                    getContext().getString(R.string.mention), getContext().getString(R.string.voice_call)};
            String[] functionActionArray = {"gallery", "camera", "file", "mention", "voice_call"};
            String binaryString = "-1";
            if (!StringUtils.isBlank(inputs)) {
                try {
                    binaryString = new StringBuffer(Integer.toBinaryString(Integer.parseInt(inputs))).reverse().toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //处理默认情况，也就是普通频道的情况
            if (binaryString.equals("-1")) {
                //目前开放三位，有可能扩展
                binaryString = "111";
            }
            //控制binaryString长度，防止穿的数字过大
            int binaryLength = binaryString.length() > 3 ? 3 : binaryString.length();
            for (int i = 0; i < binaryLength; i++) {
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
                        InputTypeBean inputTypeBeanGallery = new InputTypeBean(functionIconArray[0], functionNameArray[0], functionActionArray[0]);
                        inputTypeBeanList.add(inputTypeBeanGallery);
                        InputTypeBean inputTypeBeanCamera = new InputTypeBean(functionIconArray[1], functionNameArray[1], functionActionArray[1]);
                        inputTypeBeanList.add(inputTypeBeanCamera);
                    } else {
                        InputTypeBean inputTypeBean = new InputTypeBean(functionIconArray[i], functionNameArray[i], functionActionArray[i]);
                        inputTypeBeanList.add(inputTypeBean);
                    }
                }
            }
            //如果是群组的话添加@功能
            if (canMentions) {
                InputTypeBean inputTypeBean = new InputTypeBean(functionIconArray[3], functionNameArray[3], functionActionArray[3]);
                inputTypeBeanList.add(inputTypeBean);
            }
            //语音通话图标开启处
//            InputTypeBean inputTypeBean = new InputTypeBean(functionIconArray[4],functionNameArray[4],functionActionArray[4]);
//            inputTypeBeanList.add(inputTypeBean);
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
                        case "voice_call":
                            //语音通话
                            if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                                PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
                                    @Override
                                    public void onPermissionRequestSuccess(List<String> permissions) {
                                        startVoiceCall();
                                    }

                                    @Override
                                    public void onPermissionRequestFail(List<String> permissions) {
                                        ToastUtils.show(getContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getContext(), permissions));
                                    }
                                });
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

    private void startVoiceCall() {
        //语音通话
        if (!canMentions) {
            chatInputMenuListener.onVoiceCommucaiton();
        } else {
            AppUtils.openChannelMemeberSelect((Activity) getContext(), cid, 6);
        }
    }

    public void setChatDrafts(String drafts) {
        inputEdit.setText(drafts);
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
        waterWaveProgress.setShowProgress(false);
        waterWaveProgress.setShowNumerical(false);
        waterWaveProgress.setWaveSpeed(0.02F);
        waterWaveProgress.setAmplitude(5.0F);
        lastVolumeLevel = 0;
        voiceImgBtn.setImageResource(R.drawable.ic_chat_input_voice_v0);
        mediaPlayerUtils = new MediaPlayerUtils(getContext());
        voice2StringMessageUtils = new Voice2StringMessageUtils(getContext());
        voice2StringMessageUtils.setOnVoiceResultCallback(new OnVoiceResultCallback() {
            @Override
            public void onVoiceStart() {
            }

            @Override
            public void onVoiceResultSuccess(VoiceResult voiceResult, boolean isLast) {
                String results = voiceResult.getResults();
                if (results.length() == 1 && StringUtils.isSymbol(results)) {
                    results = "";
                }
                if (!StringUtils.isBlank(results)) {
                    if (isSpecialUser) {
                        inputEdit.clearInsertModelList();
                        chatInputMenuListener.onSendMsg(results, null, null, null);
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

            @Override
            public void onVoiceResultError(VoiceResult errorResult) {
                stopVoiceInput();
                if (errorResult.getXunFeiPermissionError() == Voice2StringMessageUtils.MSG_XUNFEI_PERMISSION_ERROR) {
                    ToastUtils.show(MyApplication.getInstance(), getContext().getString(R.string.voice_audio_record_unavailiable));
                }
            }
        });
    }

    private void initViewpageLayout() {
//        InputTypeBean inputTypeBean = new InputTypeBean(R.drawable.ic_chat_input_add_gallery, "远程控制");
//        List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
//        inputTypeBeanList.add(inputTypeBean);
//        viewpagerLayout.setInputTypeBeanList(inputTypeBeanList);
    }

    @OnClick({R.id.voice_btn, R.id.send_msg_btn, R.id.add_btn, R.id.voice_input_close_img})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.voice_btn:
                if (addMenuLayout.isShown()) {
                    addMenuLayout.setVisibility(View.GONE);
                } else if (InputMethodUtils.isSoftInputShow((Activity) getContext())) {
                    InputMethodUtils.hide((Activity) getContext());
                }
                lastVolumeLevel = 0;
                waterWaveProgress.setProgress(0);
                mediaPlayerUtils.playVoiceOn();
                voice2StringMessageUtils.startVoiceListening();
                voiceInputLayout.setVisibility(View.VISIBLE);
                //为了防止在键盘弹出时显示语言输入界面导致页面闪动
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        voiceInputLayout.setVisibility(View.VISIBLE);
                    }
                }, 100);
                break;
            case R.id.send_msg_btn:
                if (NetUtils.isNetworkConnected(getContext())) {
                    List<String> urlList = null;
                    String content = inputEdit.getRichContent(true);
                    Map<String, String> mentionsMap = null;
                    urlList = getContentUrlList(inputEdit.getText().toString());
                    chatInputMenuListener.onSendMsg(content, getContentMentionUidList(), urlList, mentionsMap);
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

                break;
            case R.id.voice_input_close_img:
                voiceInputLayout.setVisibility(View.GONE);
                voice2StringMessageUtils.stopListening();
                break;
        }
    }

    public boolean isVoiceInput() {
        return voiceInputLayout.getVisibility() == View.VISIBLE;
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


    public void setOtherLayoutView(View otherLayoutView) {
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
    }

    public void setChatInputMenuListener(
            ChatInputMenuListener chatInputMenuListener) {
        this.chatInputMenuListener = chatInputMenuListener;
    }

    private void setOtherLayoutHeightLock(boolean isLock) {
        if (otherLayoutView != null) {
            final LayoutParams params = (LayoutParams) otherLayoutView
                    .getLayoutParams();
            if (isLock) {
                params.height = otherLayoutView.getHeight();
                params.weight = 0.0F;
            } else {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        params.weight = 1.0F;
                    }
                });
            }
        }
    }

    public void showSoftInput(boolean isShow) {
        if (isShow) {
            InputMethodUtils.display((Activity) getContext(), inputEdit, 0);
        } else {
            InputMethodUtils.hide((Activity) getContext());
        }

    }

    public boolean isAddMenuLayoutShow() {
        return addMenuLayout.isShown();
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
     * 功能描述：采用十级，新采样数据比当前数据小时，
     * 延迟预定周期以一定速度下降，
     * 下降过程中新采样数据大于下降当前值时继续上升
     *
     * @param volume
     */
    public void setVoiceImageViewLevel(int volume) {

        int currentLevel = 0;
        if (0 == volume) {
            currentLevel = 0;
        } else {
            currentLevel = volume / 3 + 1;
        }
        int showLevel = (currentLevel + lastVolumeLevel) / 2;
        if (currentLevel >= lastVolumeLevel) {
            delayTimes = TOPDELY_TIMES;
            if ((showLevel < 4) && (showLevel > 0)) {
                waterWaveProgress.setProgress(4);
            }
            waterWaveProgress.setProgress(showLevel);
            lastVolumeLevel = currentLevel;
        } else {
            //判断延时时间
            if (delayTimes > 0) {
                delayTimes = delayTimes - 1;
            } else {
                lastVolumeLevel = lastVolumeLevel - 1;
            }
            waterWaveProgress.setProgress(lastVolumeLevel);
        }
    }

    /**
     * 停止识别，并播放停止提示音
     */
    public void stopVoiceInput() {
        voiceInputLayout.setVisibility(View.GONE);
        voice2StringMessageUtils.stopListening();
        mediaPlayerUtils.playVoiceOff();
    }

    public String getInputContent() {
        return inputEdit.getText().toString().trim();
    }


    public interface ChatInputMenuListener {
        void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap);

        void onVoiceCommucaiton();

        void onChatDraftsClear();

    }


}
