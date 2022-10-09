/**
 * ECMChatInputMenu.java
 * classes : com.inspur.emmcloud.widget.ECMChatInputMenu
 * V 1.0.0
 * Create at 2016年11月24日 上午10:25:52
 */
package com.inspur.emmcloud.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czt.mp3recorder.MP3Recorder;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.util.AppRoleUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClickRuleUtil;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;
import com.inspur.emmcloud.basemodule.util.pictureselector.PictureSelectorUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.basemodule.widget.richedit.InsertModel;
import com.inspur.emmcloud.bean.chat.InputTypeBean;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.chat.emotion.EmotionAdapter;
import com.inspur.emmcloud.ui.chat.emotion.EmotionRecentManager;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.util.privates.MediaPlayerUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationManager;
import com.inspur.emmcloud.util.privates.audioformat.AndroidMp3ConvertUtils;
import com.inspur.emmcloud.widget.audiorecord.AudioDialogManager;
import com.inspur.emmcloud.widget.audiorecord.AudioRecordButton;
import com.inspur.emmcloud.widget.filemanager.NativeVolumeFileManagerActivity;
import com.inspur.emmcloud.widget.waveprogress.VoiceCompleteView;
import com.inspur.emmcloud.widget.waveprogress.WaterWaveProgress;
import com.itheima.roundedimageview.RoundedImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import lbc.com.denosex.denosexUtil;

import static com.inspur.emmcloud.basemodule.ui.BaseActivity.THEME_DARK;


/**
 * com.inspur.emmcloud.widget.ECMChatInputMenu create at 2016年11月24日 上午10:25:52
 */
public class ECMChatInputMenu extends LinearLayout {

    public static final String VOICE_CALL = "voice_call";
    public static final String VIDEO_CALL = "video_call";
    public final static int AUDIO_SAMPLE_RATE = 16000;  //44.1KHz,普遍使用的频率
    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int CHOOSE_FILE = 4;
    private static final int MENTIONS_RESULT = 5;
    private static final long MENTIONS_BASE_TIME = 1515513600000L;
    private static final int TAG_KEYBOARD_INPUT = 0;
    private static final int TAG_VOICE_INPUT = 1;
    private static final int TOPDELY_TIMES = 17;
    private static final int VOICE_INPUT_STATUS_NORMAL = 1;
    private static final int VOICE_INPUT_STATUS_STOP = 2;
    private static final int VOICE_INPUT_STATUS_SPEAKING = 5;
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
    @BindView(R.id.voice_input_language)
    TextView languageTv;
    @BindView(R.id.voice_input_edit_text)
    EditText voiceInputEt;
    @BindView(R.id.voice_input_close_img)
    ImageView voiceInputCloseImg;
    @BindView(R.id.voice_input_speak_tip)
    TextView voiceInputSpeakTipTv;
    @BindView(R.id.voice_level_img)
    ImageView voiceInputLevelImg;
    @BindView(R.id.volume_level_img_shade)
    RoundedImageView voiceInputLevelImgShade;   //伴随音量大小
    @BindView(R.id.volume_level_img_complete)
    VoiceCompleteView voiceInputCompleteView;
    @BindView(R.id.voice_input_clear)
    TextView voiceInputClean;
    @BindView(R.id.voice_input_send)
    TextView voiceInputSend;
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
    ArrayList<String> recentEmotionList = new ArrayList<>();
    private int voiceInputStatus = 1;
    private boolean isGroup = false;
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
    private List<String> languageList = new ArrayList<>();
    private ValueAnimator animator;
    private boolean displayingWhisperOrBurnView = false;

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

    /**
     * 输出WAV文件
     *
     * @param out           WAV输出文件流
     * @param totalAudioLen 整个音频PCM数据大小
     * @param totalDataLen  整个数据大小
     * @param sampleRate    采样率
     * @param channels      声道数
     * @param byteRate      采样字节byte率
     * @throws IOException
     */
    private static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                            long totalDataLen, int sampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    private void initView(final Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(context).inflate(R.layout.communication_widget_chat_input_menu, this, true);
        ButterKnife.bind(this, view);
        initInputEdit();
        initVoiceInput();
        initAudioRecord();
        initEmotion();
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
        inputEdit.setEditTextActionWatcher(new ChatInputEdit.EditTextActionWatcher() {
            @Override
            public void onKeycodeEnter() {
                hideAddMenuLayout();
            }
        });
        inputEdit.setInputWatcher(new ChatInputEdit.InputWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isContentBlank = StringUtils.isBlank(s.toString());
                if (isContentBlank) {
                    chatInputMenuListener.onChatDraftsClear();
                }
                sendMsgBtn.setVisibility(isContentBlank ? GONE : VISIBLE);
                sendMsgBtn.setEnabled(!isContentBlank);
                sendMsgBtn.setBackgroundResource(isContentBlank ? R.drawable.bg_chat_input_send_btn_disable : R.drawable.bg_chat_input_send_btn_enable);
                addBtn.setVisibility(isContentBlank && !displayingWhisperOrBurnView ? VISIBLE : GONE);
                if (isGroup && count == 1) {
                    String inputWord = s.toString().substring(start, start + count);
                    if (inputWord.equals("@")) {
                        openMentionPage(true);
                    }
                }
            }
        });
    }

    public ChatInputEdit getInputEdit() {
        return inputEdit;
    }

    /**
     * 语音降噪算法 pcm文件
     **/
    public void deNoseX(String rawAudioFilePath, String pcmAudioFilePath) {
        int createStatus = -1;
        denosexUtil nsUtils = null;
        try {
            nsUtils = new denosexUtil();
            createStatus = nsUtils.denoseXCreate();  //去噪创建
            int initStatus = nsUtils.denoseXIni(createStatus, 16000); //去噪初始化 参数说明 创建状态，采样率
            int setStatus = nsUtils.denoseXPolicy(createStatus, 1); // 去噪 Policy 参数说明
            File fileRaw = new File(rawAudioFilePath);
            File filePcm = new File(pcmAudioFilePath);
            if (!fileRaw.exists()) {
                return; //如果不存在 退出
            }
            FileInputStream fInt = new FileInputStream(rawAudioFilePath);
            FileOutputStream fOut = new FileOutputStream(pcmAudioFilePath);
            byte[] buffer = new byte[640];
            while (fInt.read(buffer) != -1) {
                short[] inputData = new short[320];
                short[] outData = new short[320];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
                outData = nsUtils.denoseXProcess(createStatus, inputData);
                fOut.write(toByteArray(outData));
            }

            fInt.close();
            fOut.close();
            fileRaw.delete();
            File fileNew = new File(rawAudioFilePath);
            filePcm.renameTo(fileNew);
            LogUtils.LbcDebug("new File Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (createStatus == 0 && nsUtils != null) {
            nsUtils.denoseXFree();
        }
    }

    public byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i]);
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
        }
        return dest;
    }

    /**
     * 语音增强算法（原始数据文件pcm）
     **/
    public void voiceAgc(String rawAudioFilePath, String pcmAudioFilePath) {
        int createStatus = -1;
        denosexUtil agcUtils = null;
        try {
            agcUtils = new denosexUtil();
            createStatus = agcUtils.noseAgcCreate();
            int iniState = agcUtils.noseAgcIni(createStatus, 0, 255, 3, 16000);
            int configState = agcUtils.noseAgcSetConfig(createStatus, 30, 1, 3);
            File fileRaw = new File(rawAudioFilePath);
            File filePcm = new File(pcmAudioFilePath);
            if (!fileRaw.exists()) {
                return; //如果不存在 退出
            }
            FileInputStream fInt = new FileInputStream(rawAudioFilePath);
            FileOutputStream fOut = new FileOutputStream(pcmAudioFilePath);
            byte[] buffer = new byte[320];
            while (fInt.read(buffer) != -1) {
                short[] inputData = new short[160];
                short[] outData = new short[160];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
                outData = agcUtils.noseAgcProcess(createStatus, inputData, 160);
                fOut.write(toByteArray(outData));
            }
            fInt.close();
            fOut.close();
            fileRaw.delete();
            File fileNew = new File(rawAudioFilePath);
            filePcm.renameTo(fileNew);
            LogUtils.LbcDebug("new File Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (createStatus == 0 && agcUtils != null) {
            agcUtils.noseAgcFree();
        }
    }

    /**
     * PCM文件转WAV文件
     *
     * @param inPcmFilePath  输入PCM文件路径
     * @param outWavFilePath 输出WAV文件路径
     * @param sampleRate     采样率，例如15000
     * @param channels       声道数 单声道：1或双声道：2
     * @param bitNum         采样位数，8或16
     */
    public void convertPcmToWav(String inPcmFilePath, String outWavFilePath, int sampleRate,
                                int channels, int bitNum) {
        FileInputStream in = null;
        FileOutputStream out = null;
        byte[] data = new byte[1024];

        try {
            //采样字节byte率
            long byteRate = sampleRate * channels * bitNum / 8;

            in = new FileInputStream(inPcmFilePath);
            out = new FileOutputStream(outWavFilePath);

            //PCM文件大小
            long totalAudioLen = in.getChannel().size();

            //总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
            long totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen, sampleRate, channels, byteRate);

            int length = 0;
            while ((length = in.read(data)) > 0) {
                out.write(data, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initAudioRecord() {
        audioRecordBtn.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {

            @Override
            public void onStartRecordingVoice() {
            }

            /****/
            @Override
            public void onFinished(final float seconds, String filePathRaw) {
                /**文件操作**/
                if (FileUtils.getFileSize(filePathRaw) > 0) {
                    try {
                        String BasePath = MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
                        String id = UUID.randomUUID().toString();
                        String namePcm = id + ".pcm";
                        String filePcmNew = BasePath + namePcm;
                        voiceAgc(filePathRaw, filePcmNew);
                        deNoseX(filePathRaw, filePcmNew);
                        if ((FileUtils.getFileSize(filePathRaw) <= 0)) {
                            return;
                        } else {
                            File tempFile = new File(filePcmNew);
                            String wavName = filePathRaw.replace(".raw", ".wav");
                            convertPcmToWav(filePathRaw, wavName, 16000, 1, 16);
                            tempFile.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // TODO Auto-generated method stub
                if (AppUtils.getIsVoiceWordOpen()) {
                    if (FileUtils.getFileSize(filePathRaw) <= 0) {
                        if (audioDialogManager != null) {
                            audioDialogManager.dismissVoice2WordProgressDialog();
                        }
                        return;
                    }
                    audioDialogManager = new AudioDialogManager(getContext());
                    audioDialogManager.showVoice2WordProgressDialog();
                    //转写和转文件格式同时进行
                    voice2StringMessageUtils.setNeedChangeLanguage(false);
                    voice2StringMessageUtils.startVoiceListeningByVoiceFile(seconds, filePathRaw);
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
                    }).setRawPathAndMp3Path(filePathRaw, filePathRaw.replace(".raw", ".mp3")).startConvert();
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
                    }).setRawPathAndMp3Path(filePathRaw, filePathRaw.replace(".raw", ".mp3")).startConvert();

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

    /**
     * 初始化语言输入相关
     */
    private void initVoiceInput() {
        mediaPlayerUtils = new MediaPlayerUtils(getContext());
        voice2StringMessageUtils = new Voice2StringMessageUtils(getContext());
        initLanguageData();
        voiceInputStatus = VOICE_INPUT_STATUS_NORMAL;
        initVoiceInputView();
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
                Log.d("zhang", "onVoiceLevelChange: volume = " + volume);
                int level = (volume + 2) / 7;
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
//        languageList.add(getContext().getString(R.string.voice_input_language_cantonese));  //粵語
    }

    /**
     * 初始化表情相关
     */
    private void initEmotion() {
        int currentThemeNo = PreferencesUtils.getInt(BaseApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        emotionDeleteImg.setImageDrawable(currentThemeNo != THEME_DARK ? getResources().getDrawable(R.drawable.emotion_delete)
                : getResources().getDrawable(R.drawable.emotion_delete_dark));
        EmotionRecentManager recentManager = EmotionRecentManager.getInstance(getContext());
        recentEmotionList.addAll(recentManager);
        emotionRecentAdapter = new EmotionAdapter(getContext(), 1, recentEmotionList);
        emotionRecentGrid.setAdapter(emotionRecentAdapter);
        emotionRecentGrid.setOnItemClickListener(new OnEmotionItemClickListener());

        List<String> resList = EmotionUtil.getInstance(getContext()).getExpressionRes();
        emotionAdapter = new EmotionAdapter(getContext(), 1, resList);
        emotionGrid.setAdapter(emotionAdapter);
        emotionGrid.setOnItemClickListener(new OnEmotionItemClickListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initVoiceInputView() {
        Log.d("zhang", "initVoiceInputView: voiceInputStatus = " + voiceInputStatus);
        switch (voiceInputStatus) {
            case VOICE_INPUT_STATUS_NORMAL:
                voiceInputEt.setText("");
                voiceInputEt.setHint(getContext().getString(R.string.voice_input_hint_prepare));
                languageTv.setVisibility(VISIBLE);
            case VOICE_INPUT_STATUS_STOP:
                stopVoiceCompleteAnim();
                String text = voiceInputEt.getText().toString();
                Log.d("zhang", "initVoiceInputView: text = " + text);
                voiceInputEt.setVisibility(StringUtils.isBlank(text) ? INVISIBLE : VISIBLE);
                languageTv.setVisibility(StringUtils.isBlank(text) ? VISIBLE : INVISIBLE);
                voiceInputClean.setVisibility(StringUtils.isBlank(text) ? INVISIBLE : VISIBLE);
                voiceInputSend.setVisibility(StringUtils.isBlank(text) ? INVISIBLE : VISIBLE);
                voiceInputSpeakTipTv.setVisibility(StringUtils.isBlank(text) ? VISIBLE : INVISIBLE);
                voiceInputCloseImg.setVisibility(StringUtils.isBlank(text) ? VISIBLE : INVISIBLE);
//                voiceInputLevelImgShade.setVisibility(INVISIBLE);
                break;
            case VOICE_INPUT_STATUS_SPEAKING:
                stopVoiceCompleteAnim();
                voiceInputEt.setVisibility(VISIBLE);
                languageTv.setVisibility(INVISIBLE);
                voiceInputSpeakTipTv.setVisibility(INVISIBLE);
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
        animator = ValueAnimator.ofFloat(0, 100f);
        animator.setDuration(3000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(-1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                voiceInputCompleteView.setProgress((int) value);
            }
        });
        animator.start();
    }

    /**
     * 录音结束动画
     */
    private void stopVoiceCompleteAnim() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        if (voiceInputStatus != VOICE_INPUT_STATUS_SPEAKING) {
            voiceInputLevelImgShade.setVisibility(INVISIBLE);
        }
        voiceInputCompleteView.setVisibility(INVISIBLE);
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
                Log.d("zhang", "handleVoiceResult: voiceResult.getXunFeiError()");
                voiceInputStatus = VOICE_INPUT_STATUS_STOP;
                initVoiceInputView();
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
                    int index = voiceInputEt.getSelectionEnd();
                    Editable voiceEditable = voiceInputEt.getText();
                    voiceEditable.insert(index, results);
                    Log.d("zhang", "handleVoiceResult: index = " + index + ", results = " + results);
                }

            }
        }
        voiceInputStatus = VOICE_INPUT_STATUS_STOP;
        initVoiceInputView();
    }

    /**
     * 设置是否可以@
     *
     * @param isGroup
     * @param cid
     */
    public void setIsGroup(boolean isGroup, String cid) {
        this.isGroup = isGroup;
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
     * 调整这里时要注意逐位对应，新旧版本兼容
     * 每一位bit代表的意义为（高位）location mail videocall voicecall null(废弃) null(废弃) video(暂不开放) voice command file photo text (低位)
     *
     * @param inputs
     */
    public void setInputLayout(String inputs, boolean forceHideWhisperOrBurn) {
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
                    R.drawable.ic_chat_input_add_mention, R.drawable.ic_chat_input_add_voice_call, R.drawable.ic_chat_input_add_send_email,
                    R.drawable.ic_chat_input_add_video, R.drawable.ic_chat_input_read_disppear, R.drawable.ic_chat_input_whisper};
            String[] functionNameArray = {getContext().getString(R.string.album),
                    getContext().getString(R.string.take_photo),
                    getContext().getString(R.string.file), getContext().getString(R.string.voice_input),
                    getContext().getString(R.string.mention),
                    getContext().getString(R.string.voice_call),
                    getContext().getString(R.string.send_email),
                    getContext().getString(R.string.video_call),
                    getContext().getString(R.string.read_disappear),
                    getContext().getString(R.string.voice_whisper)};
            String[] functionActionArray = {"gallery", "camera", "file", "voice_input", "mention", VOICE_CALL, "send_email", VIDEO_CALL, "read_disappear", "whisper"};
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
            int length = inputControl.length() > 12 ? 12 : inputControl.length();
            boolean isInputTextEnable = false;
            boolean isInputPhotoEnable = false;
            boolean isInputFileEnable = false;
            boolean isInputVoiceEnable = false;
            boolean isVoiceCallEnable = false;
            boolean isSendEmailEnable = false;
            boolean isVideoCallEnable = false;
            boolean isWhisperEnable = false;
            boolean isBurnEnable = false;

            for (int i = 0; i < length; i++) {
                String controlValue = inputControl.charAt(i) + "";
                switch (i) {
                    case 0:
                        isInputTextEnable = controlValue.equals("1");
                        isWhisperEnable = controlValue.endsWith("1");
                        isBurnEnable = controlValue.endsWith("1");
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
                    //废弃
//                    case 5:
//                        isVoiceCallEnable = controlValue.endsWith("1");
//                        break;
                    //废弃
//                    case 6:
//                        isVideoCallEnable = controlValue.endsWith("1");
//                        break;
                    //屏蔽语音通话
                    case 8:
                        isVoiceCallEnable = controlValue.equals("1");
                        break;
                    //屏蔽视频通话
//                    case 9:
//                        isVideoCallEnable = controlValue.equals("1");
//                        break;
                    case 10:
                        isSendEmailEnable = controlValue.equals("1");
                        break;
                    default:
                        break;
                }
            }
            if (forceHideWhisperOrBurn) {
                isBurnEnable = false;
                isWhisperEnable = false;
            }

            if (isInputPhotoEnable && AppRoleUtils.isCanSendImage()) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[0], functionNameArray[0], functionActionArray[0]));
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[1], functionNameArray[1], functionActionArray[1]));
            }
            if (isInputFileEnable && AppRoleUtils.isCanSendFile()) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[2], functionNameArray[2], functionActionArray[2]));
            }
            if (isInputVoiceEnable && !displayingWhisperOrBurnView) {
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
            if (isGroup) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[4], functionNameArray[4], functionActionArray[4]));
                if (isWhisperEnable) {
                    inputTypeBeanList.add(new InputTypeBean(functionIconArray[9], functionNameArray[9], functionActionArray[9]));
                }
            } else if (isBurnEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[8], functionNameArray[8], functionActionArray[8]));
            }

            if (isVoiceCallEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[5], functionNameArray[5], functionActionArray[5]));
            }

            if (isSendEmailEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[6], functionNameArray[6], functionActionArray[6]));
            }

            if (isVideoCallEnable) {
                inputTypeBeanList.add(new InputTypeBean(functionIconArray[7], functionNameArray[7], functionActionArray[7]));
            }

            if (inputTypeBeanList.size() > 0) {
                addBtn.setVisibility(displayingWhisperOrBurnView ? GONE : VISIBLE);
                sendMsgBtn.setVisibility(GONE);
            }

            viewpagerLayout.setOnGridItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    InputTypeBean inputTypeBean = inputTypeBeanList.get(position);
                    switch (inputTypeBean.getAction()) {
                        case "gallery":
//                            AppUtils.openGallery((Activity) getContext(), 5, GELLARY_RESULT, true);
                            PictureSelectorUtils.getInstance().openGallery(getContext());
                            break;
                        case "camera":
//                            String fileName = System.currentTimeMillis() + ".jpg";
//                            PreferencesUtils.putString(getContext(), "capturekey", fileName);
//                            AppUtils.openCamera((Activity) getContext(), fileName, CAMERA_RESULT);
                            startCamera();
                            break;
                        case "file":
                            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), Permissions.STORAGE, new PermissionRequestCallback() {
                                @Override
                                public void onPermissionRequestSuccess(List<String> permissions) {
                                    Intent intent = new Intent(getContext(), NativeVolumeFileManagerActivity.class);
                                    ((Activity) getContext()).startActivityForResult(intent, CHOOSE_FILE);
                                }

                                @Override
                                public void onPermissionRequestFail(List<String> permissions) {
                                    ToastUtils.show(getContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getContext(), permissions));
                                }
                            });
                            break;
                        case "mention":
                            openMentionPage(false);
                            break;
                        case "voice_input":     //语音输入
                            if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && WebSocketPush.getInstance().isSocketConnect()) {
                                if (VoiceCommunicationManager.getInstance().isVoiceBusy()) {
                                    ToastUtils.show(R.string.voice_communication_voice_busy_tip);
                                    return;
                                }
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
                        case VOICE_CALL:
                            //检查网络和能否发起电话，提示在方法内处理
                            if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && checkCanMakeCall()) {
                                if (PermissionRequestManagerUtils.getInstance().isHasPermission(getContext(), Permissions.RECORD_AUDIO)) {
                                    startVoiceCall(VOICE_CALL);
                                } else {
                                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
                                        @Override
                                        public void onPermissionRequestSuccess(List<String> permissions) {

                                        }

                                        @Override
                                        public void onPermissionRequestFail(List<String> permissions) {
                                            ToastUtils.show(getContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getContext(), permissions));
                                        }
                                    });
                                }
                            }
                            break;
                        case VIDEO_CALL:
                            if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && checkCanMakeCall()) {
                                String[] videoPermissions = new String[]{Permissions.RECORD_AUDIO, Permissions.CAMERA};
                                if (PermissionRequestManagerUtils.getInstance().isHasPermission(getContext(), videoPermissions)) {
                                    startVoiceCall(VIDEO_CALL);
                                } else {
                                    PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), videoPermissions, new PermissionRequestCallback() {
                                        @Override
                                        public void onPermissionRequestSuccess(List<String> permissions) {

                                        }

                                        @Override
                                        public void onPermissionRequestFail(List<String> permissions) {
                                            ToastUtils.show(getContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getContext(), permissions));
                                        }
                                    });
                                }
                            }
                            break;
                        case "send_email":
                            inputMenuClickCallback.onInputMenuClick("mail");
                            break;
                        case "read_disappear":
                            inputMenuClickCallback.onInputMenuClick("read_disappear");
                            break;
                        case "whisper":
                            inputMenuClickCallback.onInputMenuClick("whisper");
                            break;
                        default:
                            break;
                    }
                }
            });
            viewpagerLayout.setInputTypeBeanList(inputTypeBeanList);
        }
    }

    // 拍照/摄像
    private void startCamera() {
        // 判断存储卡是否可以用，可用进行存储
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String[] permissions = new String[]{Permissions.CAMERA, Permissions.RECORD_AUDIO, Permissions.READ_EXTERNAL_STORAGE,
                    Permissions.WRITE_EXTERNAL_STORAGE};
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), permissions,
                    new PermissionRequestCallback() {
                        @Override
                        public void onPermissionRequestSuccess(List<String> permissions) {
//                            File appDir = new File(Environment.getExternalStorageDirectory(), "DCIM");
//                            if (!appDir.exists()) {
//                                appDir.mkdir();
//                            }
                            Intent intent = new Intent(getContext(), CommunicationRecordActivity.class);
//                            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_DIRECTORY_PATH, appDir.getAbsolutePath());
//                            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_NAME, picPath);
                            ((Activity)getContext()).startActivityForResult(intent, CAMERA_RESULT);
                            ((Activity)getContext()).overridePendingTransition(R.anim.ps_anim_up_in, R.anim.ps_anim_fade_in);
                        }

                        @Override
                        public void onPermissionRequestFail(List<String> permissions) {
                            ToastUtils.show(getContext(), PermissionRequestManagerUtils.getInstance()
                                    .getPermissionToast(getContext(), permissions));
                        }
                    });
        } else {
            ToastUtils.show(getContext(), R.string.baselib_sd_not_exist);
        }
    }

    /**
     * 检查是否有发起电话的条件
     *
     * @return
     */
    private boolean checkCanMakeCall() {
        if (AppUtils.isPhoneInUse()) {
            ToastUtils.show(R.string.voice_communication_calling);
            return false;
        }
        if (VoiceCommunicationManager.getInstance().isVoiceBusy()) {
            ToastUtils.show(R.string.voice_communication_voice_busy_tip);
            return false;
        }
        //当没有悬浮窗权限或者小米手机上没有后台弹出界面权限时先请求权限
        if ((Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(getContext())) ||
                (Build.VERSION.SDK_INT >= 19 && !AppUtils.canBackgroundStart(getContext()))) {
            chatInputMenuListener.onNoSmallWindowPermission();
            return false;
        }
        return true;
    }

    public void updateVoiceAndMoreLayout(boolean show) {
        displayingWhisperOrBurnView = !show;
        addBtn.setVisibility(show ? VISIBLE : GONE);
        voiceBtn.setVisibility(show ? VISIBLE : GONE);
        if (!show) {
            if (viewpagerLayout.getVisibility() == View.VISIBLE) {
                setOtherLayoutHeightLock(true);
                setAddMenuLayoutShow(false);
                setOtherLayoutHeightLock(false);
            } else {
                changeAddMenuLayoutContent(false);
            }
        }
    }

    private void startVoiceCall(String type) {
        //语音通话
        if (!isGroup) {
            if (type.equals(VOICE_CALL)) {
                chatInputMenuListener.onVoiceCommucaiton();
            } else if (type.equals(VIDEO_CALL)) {
                chatInputMenuListener.onVideoCommucaiton();
            }
        } else {
            chatInputMenuListener.onVoiceCommucaiton();
        }
    }

    private void startVoice2Word() {
//        inputEdit.setVisibility(INVISIBLE);
        hideAddMenuLayout();
        voiceInputLayout.setVisibility(View.VISIBLE);
        voiceInputStatus = VOICE_INPUT_STATUS_NORMAL;
        initVoiceInputView();
        lastVolumeLevel = 0;
        waterWaveProgress.setProgress(0);
//        mediaPlayerUtils.playVoiceOn();
        voice2StringMessageUtils.initVoiceParam();
        setLanguageText();
    }

    private void setLanguageText() {
        String language = LanguageManager.getInstance().getVoiceInputLanguage();
        if (StringUtils.isBlank(language)) {
            language = LanguageManager.getInstance().getCurrentAppLanguage();
        }
        languageTv.setText(language.equals("en") ? R.string.voice_input_language_english : R.string.voice_input_language_mandarin);
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
            voiceBtn.setImageResource(ResourceUtils.getResValueOfAttr(getContext(), tag == 0 ? R.attr.ic_chat_voice_input : R.attr.ic_chat_input_keyboards));
            inputEdit.setVisibility((tag == 0) ? VISIBLE : GONE);
            audioRecordBtn.setVisibility((tag == 0) ? GONE : VISIBLE);
            if (tag != 0) {
                PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
                    @Override
                    public void onPermissionRequestSuccess(List<String> permissions) {

                    }

                    @Override
                    public void onPermissionRequestFail(List<String> permissions) {
                        ToastUtils.show(BaseApplication.getInstance(),
                                PermissionRequestManagerUtils.getInstance().
                                        getPermissionToast(BaseApplication.getInstance(), permissions));
                        setVoiceInputStatus(0);
                    }
                });
            }
        }
    }

    @OnClick({R.id.voice_btn, R.id.send_msg_btn, R.id.add_btn, R.id.voice_input_close_img, R.id.voice_input_language,
            R.id.voice_input_clear, R.id.voice_input_send, R.id.emotion_btn, R.id.emotion_delete})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_btn:
                if (view.getTag() == null || (int) view.getTag() == TAG_KEYBOARD_INPUT) {
                    setVoiceInputStatus(TAG_VOICE_INPUT);
                    if (addMenuLayout.isShown()) {
                        hideAddMenuLayout();
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
                    if (viewpagerLayout.getVisibility() == View.VISIBLE) {
                        setOtherLayoutHeightLock(true);
                        setAddMenuLayoutShow(false);
                        setOtherLayoutHeightLock(false);
                    } else {
                        changeAddMenuLayoutContent(false);
                    }

                } else if (InputMethodUtils.isSoftInputShow((Activity) getContext())) {
                    setOtherLayoutHeightLock(true);
                    setAddMenuLayoutShow(true);
                    setOtherLayoutHeightLock(false);
                    changeAddMenuLayoutContent(false);
                } else {
                    setAddMenuLayoutShow(true);
                    changeAddMenuLayoutContent(false);
                }
                setVoiceInputStatus(TAG_KEYBOARD_INPUT);
                break;
            case R.id.emotion_btn:
                handleEmotionStatus();
                break;
            case R.id.voice_input_close_img:
//                inputEdit.setVisibility(VISIBLE);
                voiceInputEt.setText("");
                voiceInputLayout.setVisibility(View.GONE);
                voice2StringMessageUtils.stopListening();
                break;
            case R.id.voice_input_language:
                showLanguageDialog();
                break;
            case R.id.voice_input_clear:
                voiceInputEt.setText("");
                stopVoiceInput();
                break;
            case R.id.voice_input_send:
                inputEdit.setVisibility(VISIBLE);
                String results = voiceInputEt.getText().toString();
                if (chatInputMenuListener != null && !StringUtils.isBlank(results)) {
                    chatInputMenuListener.onSendMsg(results, null, null, null);
                }
                voiceInputEt.setText("");
                stopVoiceInput();
                break;

            case R.id.emotion_delete:  //表情删除
                EmotionUtil.getInstance(getContext()).deleteSingleEmojcon(inputEdit);
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
            EmotionRecentManager recentManager = EmotionRecentManager.getInstance(getContext());
            recentEmotionList.clear();
            recentEmotionList.addAll(recentManager);
            emotionRecentLayout.setVisibility(recentManager.size() > 0 ? VISIBLE : GONE);
            emotionRecentAdapter.notifyDataSetChanged();
        }
        if (addMenuLayout.isShown()) {
            if (viewpagerLayout.getVisibility() == View.VISIBLE) {
                changeAddMenuLayoutContent(true);
            } else {
                setOtherLayoutHeightLock(true);
                setAddMenuLayoutShow(false);
                setOtherLayoutHeightLock(false);
            }

        } else if (InputMethodUtils.isSoftInputShow((Activity) getContext())) {
            setOtherLayoutHeightLock(true);
            setAddMenuLayoutShow(true);
            setOtherLayoutHeightLock(false);
            changeAddMenuLayoutContent(true);
        } else {
            setAddMenuLayoutShow(true);
            changeAddMenuLayoutContent(true);
        }
        setVoiceInputStatus(TAG_KEYBOARD_INPUT);
    }

    /**
     * 更改添加Menu
     *
     * @param isShowEmotion
     */
    private void changeAddMenuLayoutContent(boolean isShowEmotion) {
        viewpagerLayout.setVisibility(isShowEmotion ? View.GONE : View.VISIBLE);
        emotionLayout.setVisibility(isShowEmotion ? View.VISIBLE : View.GONE);
        emotionLayout.setBackgroundColor(DarkUtil.getTextContainerLevelTwoColor());
        emotionBtn.setImageResource(ResourceUtils.getResValueOfAttr(getContext(), isShowEmotion ? R.attr.ic_chat_input_keyboards : R.attr.ic_chat_button_emotion));
    }

    @OnTouch({R.id.voice_level_img})
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ClickRuleUtil.isFastClick()) return false;
                voiceInputStatus = VOICE_INPUT_STATUS_SPEAKING;
                initVoiceInputView();
                voiceInputEt.setHint(getContext().getString(R.string.voice_input_hint_prepare));
                mediaPlayerUtils.playVoiceOn();
                voice2StringMessageUtils.setNeedChangeLanguage(true);
                voice2StringMessageUtils.startVoiceListening();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mediaPlayerUtils.playVoiceOff();
                voice2StringMessageUtils.stopListening();
                voiceInputLevelImgShade.setVisibility(INVISIBLE);
                if (voiceInputStatus != VOICE_INPUT_STATUS_STOP) {
                    startVoiceCompleteAnim();
                }
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
                hideVoiceInputLayout();
                if (addMenuLayout.getVisibility() != View.GONE) {
                    hideAddMenuLayout();
                }
                InputMethodUtils.hide((Activity) getContext());
                return false;
            }
        });
        if (listContentView != null) {
            listContentView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideVoiceInputLayout();
                    hideAddMenuLayout();
                    InputMethodUtils.hide((Activity) getContext());
                    return false;
                }
            });
        }

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
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, softInputHeight);
            addMenuLayout.setLayoutParams(params);
            addMenuLayout.setVisibility(View.VISIBLE);
        } else if (addMenuLayout.isShown()) {
            hideAddMenuLayout();
            InputMethodUtils.display((Activity) getContext(), inputEdit, 0);
            emotionBtn.setImageResource(ResourceUtils.getResValueOfAttr(getContext(), R.attr.ic_chat_button_emotion));
        }

    }

    public boolean isVoiceInputLayoutShow() {
        return voiceInputLayout.isShown();
    }

    public void hideAddMenuLayout() {
        // addMenuLayout.setLayoutParams(params); 用于解决横屏时部分手机无法隐藏addMenuLayout
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
        addMenuLayout.setLayoutParams(params);
        addMenuLayout.setVisibility(View.GONE);
        emotionBtn.setImageResource(ResourceUtils.getResValueOfAttr(getContext(), R.attr.ic_chat_button_emotion));
    }

    public void hideVoiceInputLayout() {
        voiceInputLayout.setVisibility(GONE);
    }

    /**
     * 释放MediaPlay资源
     */
    public void releaseVoiceInput() {
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
        if (volume > 7) {
            volume = 7;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) voiceInputLevelImgShade.getLayoutParams();
        params.height = DensityUtil.dip2px(getContext(), 100) + DensityUtil.dip2px(getContext(), volume * 3);
        params.width = DensityUtil.dip2px(getContext(), 100) + DensityUtil.dip2px(getContext(), volume * 3);
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
        mediaPlayerUtils.playVoiceOff();
        voiceInputStatus = VOICE_INPUT_STATUS_STOP;
        initVoiceInputView();
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
                voice2StringMessageUtils.setNeedChangeLanguage(true);
                dialog.dismiss();
            }
        };

        ActionSheetDialog.ActionListSheetBuilder builder = new ActionSheetDialog.ActionListSheetBuilder(getContext());
        for (int i = 0; i < languageList.size(); i++) {
            builder.addItem(languageList.get(i));
        }
        builder.setOnSheetItemClickListener(onSheetItemClickListener)
                .setItemColor(DarkUtil.getTextColor())
                .build()
                .show();
    }

    public void setInputMenuClickCallback(ECMChatInputMenuCallback inputMenuClickCallback) {
        this.inputMenuClickCallback = inputMenuClickCallback;
    }

    public interface ChatInputMenuListener {
        void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap);

        void onSendVoiceRecordMsg(String results, float seconds, String filePath);

        void onVoiceCommucaiton();//语音通话

        void onVideoCommucaiton();//视频通话

        void onChatDraftsClear();

        void onNoSmallWindowPermission();
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
}
