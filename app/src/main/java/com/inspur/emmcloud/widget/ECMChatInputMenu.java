/**
 * ECMChatInputMenu.java
 * classes : com.inspur.emmcloud.widget.ECMChatInputMenu
 * V 1.0.0
 * Create at 2016年11月24日 上午10:25:52
 */
package com.inspur.emmcloud.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czt.mp3recorder.MP3Recorder;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.basemodule.widget.richedit.InsertModel;
import com.inspur.emmcloud.bean.chat.InputTypeBean;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.privates.MediaPlayerUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;
import com.inspur.emmcloud.util.privates.audioformat.AndroidMp3ConvertUtils;
import com.inspur.emmcloud.widget.audiorecord.AudioDialogManager;
import com.inspur.emmcloud.widget.audiorecord.AudioRecordButton;
import com.inspur.emmcloud.widget.waveprogress.VoiceCompleteView;
import com.inspur.emmcloud.widget.waveprogress.WaterWaveProgress;
import com.itheima.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;


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
    private static final int TOPDELY_TIMES = 17;
    @BindView(R.id.input_edit)
    ChatInputEdit inputEdit;

    @BindView(R.id.add_btn)
    ImageButton addBtn;

    @BindView(R.id.send_msg_btn)
    Button sendMsgBtn;

    @BindView(R.id.add_menu_layout)
    RelativeLayout addMenuLayout;

    @BindView(R.id.viewpager_layout)
    ECMChatInputMenuViewpageLayout viewpagerLayout;

    @BindView(R.id.voice_input_layout)
    RelativeLayout voiceInputLayout;

    @BindView(R.id.voice_btn)
    ImageButton voiceBtn;

    @BindView(R.id.bt_audio_record)
    AudioRecordButton audioRecordBtn;

    @BindView(R.id.wave_progress_input)
    WaterWaveProgress waterWaveProgress;
    private static final int VOICE_INPUT_STATUS_NORMAL = 1;
    private static final int VOICE_INPUT_STATUS_STOP = 2;
    private static final int VOICE_INPUT_STATUS_SPEAKING = 5;
    @BindView(R.id.voice_input_language)
    TextView languageTv;
    @BindView(R.id.voice_input_edit_text)
    EditText voiceInputEt;
    @BindView(R.id.voice_input_close_img)
    ImageView voiceInputCloseImg;
    @BindView(R.id.volume_level_img)
    ImageView voiceInputLevelImg;
    @BindView(R.id.volume_level_img_shade)
    RoundedImageView voiceInputLevelImgShade;   //伴随音量大小

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
    private List<VoiceResult> voiceResultList = new ArrayList<>();
    private List<String> mp3FilePathList = new ArrayList<>();
    private Map<String, Boolean> voiceBooleanMap = new HashMap<>();
    private AudioDialogManager audioDialogManager;
    private ECMChatInputMenuCallback inputMenuClickCallback;
    @BindView(R.id.volume_level_img_complete)
    VoiceCompleteView voiceInputCompleteView;

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
        View view = LayoutInflater.from(context).inflate(R.layout.communication_widget_chat_input_menu, this, true);
        ButterKnife.bind(this, view);
        initInputEdit();
        initVoiceInput();
        initAudioRecord();
    }

    //initVoiceInput
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
                sendMsgBtn.setVisibility(isContentBlank ? GONE : VISIBLE);
                sendMsgBtn.setEnabled(!isContentBlank);
                sendMsgBtn.setBackgroundResource(isContentBlank ? R.drawable.bg_chat_input_send_btn_disable : R.drawable.bg_chat_input_send_btn_enable);
                addBtn.setVisibility(isContentBlank ? VISIBLE : GONE);
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
            }

            @Override
            public void onFinished(final float seconds, String filePath) {
                // TODO Auto-generated method stub
                if (AppUtils.getIsVoiceWordOpen()) {
                    if (FileUtils.getFileSize(filePath) <= 0) {
                        if (audioDialogManager != null) {
                            audioDialogManager.dismissVoice2WordProgressDialog();
                        }
                        return;
                    }
                    audioDialogManager = new AudioDialogManager(getContext());
                    audioDialogManager.showVoice2WordProgressDialog();
                    //转写和转文件格式同时进行
                    voice2StringMessageUtils.startVoiceListeningByVoiceFile(seconds, filePath);
                    AndroidMp3ConvertUtils.with(getContext()).setCallBack(new AndroidMp3ConvertUtils.AndroidMp3ConvertCallback() {
                        @Override
                        public void onSuccess(String mp3FilePath) {
                            String fileName = FileUtils.getFileNameWithoutExtension(mp3FilePath);
                            mp3FilePathList.add(mp3FilePath);
                            if (voiceBooleanMap.get(fileName) == null || !voiceBooleanMap.get(fileName)) {
                                voiceBooleanMap.put(fileName, true);
                            } else {
                                callBackVoiceMessage(fileName);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (audioDialogManager != null) {
                                audioDialogManager.dismissVoice2WordProgressDialog();
                            }
                        }
                    }).setRawPathAndMp3Path(filePath.replace(".wav", ".raw"), filePath.replace(".wav", ".mp3")).startConvert();
                } else {
                    AndroidMp3ConvertUtils.with(getContext()).setCallBack(new AndroidMp3ConvertUtils.AndroidMp3ConvertCallback() {
                        @Override
                        public void onSuccess(String mp3FilePath) {
                            if (chatInputMenuListener != null) {
                                chatInputMenuListener.onSendVoiceRecordMsg("", seconds, mp3FilePath);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    }).setRawPathAndMp3Path(filePath.replace(".wav", ".raw"), filePath.replace(".wav", ".mp3")).startConvert();

                }
            }

            @Override
            public void onErrorRecordingVoice(int errorType) {
                if (errorType == MP3Recorder.ERROR_TYPE) {
                    ToastUtils.show(MyApplication.getInstance(), getContext().getString(R.string.voice_audio_record_unavailiable));
                }
                voice2StringMessageUtils.stopListening();
                return;
            }
        });
    }

    @BindView(R.id.voice_input_clear)
    TextView voiceInputClean;
    @BindView(R.id.voice_input_send)
    TextView voiceInputSend;
    private List<String> languageList = new ArrayList<>();

    /**
     * 初始化语言输入相关
     */
    private void initVoiceInput() {
        mediaPlayerUtils = new MediaPlayerUtils(getContext());
        voice2StringMessageUtils = new Voice2StringMessageUtils(getContext());
        initLanguageData();
        voiceInputCompleteView.setProgress(50);
        initVoiceInputView(VOICE_INPUT_STATUS_NORMAL);
//        waterWaveProgress.setShowProgress(false);
//        waterWaveProgress.setShowNumerical(false);
//        waterWaveProgress.setWaveSpeed(0.02F);
//        waterWaveProgress.setAmplitude(5.0F);
        lastVolumeLevel = 0;
        voice2StringMessageUtils.setOnVoiceResultCallback(new OnVoiceResultCallback() {
            @Override
            public void onVoiceStart() {
                Log.d("zhang", "onVoiceStart: ");
                voiceInputEt.setHint(getContext().getString(R.string.voice_input_hint_speak_now));
            }

            @Override
            public void onVoiceResultSuccess(VoiceResult voiceResult, boolean isLast) {
                Log.d("zhang", "onVoiceResultSuccess: isLast = " + isLast);
                handleVoiceResult(voiceResult);
            }

            @Override
            public void onVoiceFinish() {
                Log.d("zhang", "onVoiceFinish: ");
//                stopVoiceInput();
            }

            @Override
            public void onVoiceLevelChange(int volume) {
                int level = (volume + 2) / 5;
                setVoiceImageViewLevel(level);
            }

            @Override
            public void onVoiceResultError(VoiceResult errorResult) {
                handleVoiceResult(errorResult);
            }
        });
    }

    /**
     * 初始化语言数据
     */
    private void initLanguageData() {
        languageList.add(getContext().getString(R.string.voice_input_language_mandarin));   //普通話
        languageList.add(getContext().getString(R.string.voice_input_language_english));    //英語
        languageList.add(getContext().getString(R.string.voice_input_language_cantonese));  //粵語
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initVoiceInputView(int status) {
        Log.d("zhang", "initVoiceInputView: status = " + status);
        voiceInputCompleteView.setVisibility(INVISIBLE);
        switch (status) {
            case VOICE_INPUT_STATUS_NORMAL:
                voiceInputEt.setHint(getContext().getString(R.string.voice_input_hint_prepare));
            case VOICE_INPUT_STATUS_STOP:
                String text = voiceInputEt.getText().toString();
                Log.d("zhang", "initVoiceInputView: text = " + text);
                if (StringUtils.isBlank(text)) {
                    voiceInputEt.setVisibility(INVISIBLE);
                    languageTv.setVisibility(VISIBLE);
                    voiceInputClean.setVisibility(INVISIBLE);
                    voiceInputSend.setVisibility(INVISIBLE);
                } else {
                    voiceInputEt.setVisibility(VISIBLE);
                    languageTv.setVisibility(INVISIBLE);
                    voiceInputClean.setVisibility(VISIBLE);
                    voiceInputSend.setVisibility(VISIBLE);
                    startVoiceCompleteAnim();
                }
                voiceInputCloseImg.setVisibility(VISIBLE);
                voiceInputLevelImgShade.setVisibility(INVISIBLE);
                break;
            case VOICE_INPUT_STATUS_SPEAKING:
                voiceInputEt.setVisibility(VISIBLE);
                languageTv.setVisibility(INVISIBLE);
                voiceInputCloseImg.setVisibility(INVISIBLE);
                voiceInputClean.setVisibility(INVISIBLE);
                voiceInputSend.setVisibility(INVISIBLE);
                voiceInputLevelImgShade.setVisibility(VISIBLE);
                break;
        }
    }

    /**
     * 录音完成动画
     */
    private void startVoiceCompleteAnim() {
        voiceInputCompleteView.setVisibility(VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 100f);
        animator.setDuration(1000);         //时间需要优化  TODO fuchang
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                voiceInputCompleteView.setProgress((int) value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                voiceInputCompleteView.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    /**
     * 处理返回结果
     *
     * @param voiceResult
     */
    private void handleVoiceResult(VoiceResult voiceResult) {
        if (voiceResult.getMsgState() == Voice2StringMessageUtils.MSG_FROM_CUSTOM) {
            String fileName = FileUtils.getFileNameWithoutExtension(voiceResult.getFilePath());
            voiceResultList.add(voiceResult);
            if (voiceBooleanMap.get(fileName) == null || !voiceBooleanMap.get(fileName)) {
                voiceBooleanMap.put(fileName, new Boolean(true));
            } else {
                callBackVoiceMessage(fileName);
            }
        } else {
            if (voiceResult.getXunFeiError() == Voice2StringMessageUtils.MSG_XUNFEI_ERROR) {
//                stopVoiceInput();
                if (audioDialogManager != null) {
                    audioDialogManager.dismissVoice2WordProgressDialog();
                }
                if (voiceResult.getXunFeiPermissionError() == Voice2StringMessageUtils.MSG_XUNFEI_PERMISSION_ERROR) {
                    ToastUtils.show(MyApplication.getInstance(), getContext().getString(R.string.voice_audio_record_unavailiable));
                }
                return;
            }
            String results = voiceResult.getResults();
            if (results.length() == 1 && StringUtils.isSymbol(results)) {
                results = "";
            }
            if (!StringUtils.isBlank(results)) {
                Log.d("zhang", "handleVoiceResult: isSpecialUser = " + isSpecialUser);
                if (isSpecialUser) {
                    inputEdit.clearInsertModelList();
                    if (chatInputMenuListener != null) {
                        chatInputMenuListener.onSendMsg(results, null, null, null);
                    }
                } else {
                    int index = inputEdit.getSelectionStart();
//                    Editable editable = inputEdit.getText();
//                    editable.insert(index, results);
                    Editable voiceEditable = voiceInputEt.getText();
                    voiceEditable.insert(index, results);
                    Log.d("zhang", "handleVoiceResult: index = " + index + ", results = " + results);
                }

            }
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
    public void setInputLayout(String inputs, boolean isChannelTypeService) {
        //每一位（bit）分别代表：（高位）video voice command file photo text （低位）
        inputTypeBeanList.clear();
        inputEdit.clearInsertModelList();
        this.inputs = inputs;
        if (inputs.equals("0")) {
            this.setVisibility(View.GONE);
        } else {
            this.setVisibility(View.VISIBLE);
            //功能组的图标，名称
            int[] functionIconArray = {R.drawable.ic_chat_input_add_gallery,
                    R.drawable.ic_chat_input_add_camera, R.drawable.ic_chat_input_add_file, R.drawable.ic_chat_input_add_voice_2_word,
                    R.drawable.ic_chat_input_add_mention, R.drawable.ic_chat_input_add_voice_call, R.drawable.ic_chat_input_add_send_email};
            String[] functionNameArray = {getContext().getString(R.string.album),
                    getContext().getString(R.string.take_photo),
                    getContext().getString(R.string.file), getContext().getString(R.string.voice_input), getContext().getString(R.string.mention), getContext().getString(R.string.voice_call), getContext().getString(R.string.send_email)};
            String[] functionActionArray = {"gallery", "camera", "file", "voice_input", "mention", "voice_call", "send_email"};
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
            int length = inputControl.length() > 7 ? 7 : inputControl.length();
            boolean isInputTextEnable = false;
            boolean isInputPhotoEnable = false;
            boolean isInputFileEnable = false;
            boolean isInputVoiceEnable = false;
            boolean isVoiceCallEnable = false;
            boolean isSendEmailEnable = false;

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
                    case 5:
                        isVoiceCallEnable = controlValue.endsWith("1");
                        break;
                    case 3:
                        isSendEmailEnable = controlValue.endsWith("1");
                        break;
                    default:
                        break;
                }
            }
            if (isChannelTypeService) {
                isInputVoiceEnable = false;
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
            } else {
                voiceBtn.setVisibility(GONE);
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

            if (isVoiceCallEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[5], functionNameArray[5], functionActionArray[5]));
            }

            if (isSendEmailEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[6], functionNameArray[6], functionActionArray[6]));
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
                            AppUtils.openGallery((Activity) getContext(), 5, GELLARY_RESULT, true);
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
                        case "voice_input":     //语音输入
                            if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                                PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
                                    @Override
                                    public void onPermissionRequestSuccess(List<String> permissions) {
                                        startVoice2Word();
                                    }

                                    @Override
                                    public void onPermissionRequestFail(List<String> permissions) {
                                        ToastUtils.show(getContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getContext(), permissions));
                                    }
                                });
                            }
                            break;
                        case "voice_call":
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
                        case "send_email":
                            inputMenuClickCallback.onInputMenuClick("mail");
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
            Intent intent = new Intent();
            intent.setClass(getContext(), MembersActivity.class);
            intent.putExtra("title", getContext().getString(R.string.voice_communication_choice_members));
            intent.putExtra(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.SELECT_STATE);
            intent.putExtra("cid", cid);
            getContext().startActivity(intent);
        }
    }

    private void startVoice2Word() {
        inputEdit.setVisibility(INVISIBLE);
        addMenuLayout.setVisibility(GONE);
        voiceInputLayout.setVisibility(View.VISIBLE);
        lastVolumeLevel = 0;
        waterWaveProgress.setProgress(0);
//        mediaPlayerUtils.playVoiceOn();
//        voice2StringMessageUtils.startVoiceListening();
    }

    public void setChatDrafts(String drafts) {
        inputEdit.setText(drafts);
        voiceInputEt.setText(drafts);
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
     * 发送语音消息
     *
     * @param fileNameWithoutExtension
     */
    private void callBackVoiceMessage(String fileNameWithoutExtension) {
        VoiceResult voiceResult = getVoiceResult(fileNameWithoutExtension);
        String mp3VoiceFilePath = getVoiceFilePath(fileNameWithoutExtension);
        if (chatInputMenuListener != null) {
            chatInputMenuListener.onSendVoiceRecordMsg(voiceResult.getResults(), voiceResult.getSeconds(), mp3VoiceFilePath);
        }
        if (audioDialogManager != null) {
            audioDialogManager.dismissVoice2WordProgressDialog();
        }
        removeDataFromList(fileNameWithoutExtension);
    }

    /**
     * 查找转写内容
     *
     * @param fileNameWithoutExtension
     * @return
     */
    private VoiceResult getVoiceResult(String fileNameWithoutExtension) {
        VoiceResult voiceResult = new VoiceResult();
        voiceResult.setResults("");
        for (int i = 0; i < voiceResultList.size(); i++) {
            if (fileNameWithoutExtension.equals(FileUtils.getFileNameWithoutExtension(voiceResultList.get(i).getFilePath()))) {
                voiceResult = voiceResultList.get(i);
                break;
            }
        }
        return voiceResult;
    }

    /**
     * 查找文件路径
     *
     * @param fileNameWithoutExtension
     * @return
     */
    private String getVoiceFilePath(String fileNameWithoutExtension) {
        for (int i = 0; i < mp3FilePathList.size(); i++) {
            if (fileNameWithoutExtension.equals(FileUtils.getFileNameWithoutExtension(mp3FilePathList.get(i)))) {
                return mp3FilePathList.get(i);
            }
        }
        return "";
    }

    /**
     * 根据名称删除list里的数据
     *
     * @param fileNameWithoutExtension
     */
    private void removeDataFromList(String fileNameWithoutExtension) {
        //移除标志位
        voiceBooleanMap.remove(fileNameWithoutExtension);
        //移除voice转写结果
        Iterator<VoiceResult> voiceResultIterator = voiceResultList.iterator();
        while (voiceResultIterator.hasNext()) {
            VoiceResult voiceResult = voiceResultIterator.next();
            if (fileNameWithoutExtension.equals(FileUtils.getFileNameWithoutExtension(voiceResult.getFilePath()))) {
                voiceResultIterator.remove();
                break;
            }
        }
        //移除mp3文件路径
        Iterator<String> mp3FilePathIterator = mp3FilePathList.iterator();
        while (mp3FilePathIterator.hasNext()) {
            String mp3FilePath = mp3FilePathIterator.next();
            if (fileNameWithoutExtension.equals(FileUtils.getFileNameWithoutExtension(mp3FilePath))) {
                mp3FilePathIterator.remove();
                break;
            }
        }
    }

    private void setVoiceInputStatus(int tag) {
        if (voiceBtn.getTag() == null || (int) voiceBtn.getTag() != tag) {
            voiceBtn.setTag(tag);
            voiceBtn.setImageResource((tag == 0) ? R.drawable.ic_chat_input_voice : R.drawable.ic_chat_input_keyboard);
            inputEdit.setVisibility((tag == 0) ? VISIBLE : GONE);
            audioRecordBtn.setVisibility((tag == 0) ? GONE : VISIBLE);
        }
    }

    @OnClick({R.id.voice_btn, R.id.send_msg_btn, R.id.add_btn, R.id.voice_input_close_img, R.id.voice_input_language,
            R.id.voice_input_clear, R.id.voice_input_send})
    public void onClick(View view) {
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
                List<String> urlList = null;
                String content = inputEdit.getRichContent(false);
                Map<String, String> mentionsMap = null;
                mentionsMap = inputEdit.getMentionsMap();
                if (chatInputMenuListener != null) {
                    chatInputMenuListener.onSendMsg(content, getContentMentionUidList(), urlList, mentionsMap);
                }
                inputEdit.clearInsertModelList();
                inputEdit.setText("");
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
                inputEdit.setVisibility(VISIBLE);
                voiceInputLayout.setVisibility(View.GONE);
                voice2StringMessageUtils.stopListening();
                break;
            case R.id.voice_input_language:
                showLanguageDialog();
                break;
            case R.id.voice_input_clear:
                voiceInputEt.setText("");
                initVoiceInputView(VOICE_INPUT_STATUS_STOP);
                stopVoiceInput();
                break;
            case R.id.voice_input_send:
                //需检查chatInputMenuListener 及 results是否为理想值  TODO fuchang
                inputEdit.setVisibility(VISIBLE);
                voiceInputEt.setText("");
                initVoiceInputView(VOICE_INPUT_STATUS_STOP);
                stopVoiceInput();
                String results = voiceInputEt.getText().toString();
                if (chatInputMenuListener != null && !StringUtils.isBlank(results)) {
                    chatInputMenuListener.onSendMsg(results, null, null, null);
                }
                break;
            default:
                break;
        }
    }

    @OnTouch({R.id.volume_level_img})
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initVoiceInputView(VOICE_INPUT_STATUS_SPEAKING);
                voiceInputEt.setHint(getContext().getString(R.string.voice_input_hint_prepare));
                mediaPlayerUtils.playVoiceOn();
                voice2StringMessageUtils.startVoiceListening();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                initVoiceInputView(VOICE_INPUT_STATUS_STOP);
                mediaPlayerUtils.playVoiceOff();
                break;
        }

        return false;
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

        listContentView.setOnTouchListener(new OnTouchListener() {
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
        if (volume > 5) {
            volume = 5;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) voiceInputLevelImgShade.getLayoutParams();
        params.height = DensityUtil.dip2px(getContext(), 70) + DensityUtil.dip2px(getContext(), volume * 4);
        params.width = DensityUtil.dip2px(getContext(), 70) + DensityUtil.dip2px(getContext(), volume * 4);
        voiceInputLevelImgShade.setLayoutParams(params);
        voiceInputLevelImgShade.setCornerRadius(params.height / 2);

        //回调函数30多毫秒执行一次
//        int currentLevel = 0;
//        if (0 == volume) {
//            currentLevel = 0;
//        } else {
//            currentLevel = volume / 3 + 1;
//        }
//        int showLevel = (currentLevel + lastVolumeLevel) / 2;
//        if (currentLevel >= lastVolumeLevel) {
//            delayTimes = TOPDELY_TIMES;
//            if ((showLevel < 4) && (showLevel > 0)) {
//                waterWaveProgress.setProgress(4);
//            }
//            waterWaveProgress.setProgress(showLevel);
//            lastVolumeLevel = currentLevel;
//        } else {
//            //判断延时时间
//            if (delayTimes > 0) {
//                delayTimes = delayTimes - 1;
//            } else {
//                lastVolumeLevel = lastVolumeLevel - 1;
//            }
//            waterWaveProgress.setProgress(lastVolumeLevel);
//        }
    }

    /**
     * 停止识别，并播放停止提示音
     */
    public void stopVoiceInput() {
//        voiceInputLayout.setVisibility(View.GONE);
        voice2StringMessageUtils.stopListening();
        initVoiceInputView(VOICE_INPUT_STATUS_STOP);
//        mediaPlayerUtils.playVoiceOff();
    }

    public String getInputContent() {
        return inputEdit.getText().toString().trim();
    }

    private void showLanguageDialog() {
        ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener onSheetItemClickListener = new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                String tag = (String) itemView.getTag();
                languageTv.setText(tag);
                if (tag.equals(getContext().getString(R.string.voice_input_language_mandarin))) {
                    LanguageManager.getInstance().setVoiceInputLanguage("zh-Hans");
                } else if (tag.equals(getContext().getString(R.string.voice_input_language_cantonese))) {
                    LanguageManager.getInstance().setVoiceInputLanguage("zh-Hans-Cantonese");
                } else if (tag.equals(getContext().getString(R.string.voice_input_language_english))) {
                    LanguageManager.getInstance().setVoiceInputLanguage("en");
                }
                voice2StringMessageUtils.setLanguage(LanguageManager.getInstance().getVoiceInputLanguage());
                dialog.dismiss();
            }
        };

        ActionSheetDialog.ActionListSheetBuilder builder = new ActionSheetDialog.ActionListSheetBuilder(getContext());
        for (int i = 0; i < languageList.size(); i++) {
            builder.addItem(languageList.get(i));
        }
        builder.setOnSheetItemClickListener(onSheetItemClickListener)
                .build()
                .show();
    }

    public interface ChatInputMenuListener {
        void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap);

        void onSendVoiceRecordMsg(String results, float seconds, String filePath);


        void onVoiceCommucaiton();

        void onChatDraftsClear();
    }

    public void setInputMenuClickCallback(ECMChatInputMenuCallback inputMenuClickCallback) {
        this.inputMenuClickCallback = inputMenuClickCallback;
    }
}
