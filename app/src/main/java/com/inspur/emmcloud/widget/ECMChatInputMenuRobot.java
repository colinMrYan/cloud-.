/**
 * ECMChatInputMenu.java
 * classes : com.inspur.emmcloud.widget.ECMChatInputMenu
 * V 1.0.0
 * Create at 2016年11月24日 上午10:25:52
 */
package com.inspur.emmcloud.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.MediaPlayerUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * com.inspur.emmcloud.widget.ECMChatInputMenu create at 2016年11月24日 上午10:25:52
 */
public class ECMChatInputMenuRobot extends LinearLayout {

    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int CHOOSE_FILE = 4;
    private static final int MENTIONS_RESULT = 5;

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

    private boolean isChannelGroup = false;
    private ChatInputMenuListener chatInputMenuListener;
    private List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
    private View otherLayoutView;
    private Voice2StringMessageUtils voice2StringMessageUtils;
    private MediaPlayerUtils mediaPlayerUtils;
    private String botUid = "";

    public ECMChatInputMenuRobot(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuRobot(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public ECMChatInputMenuRobot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(final Context context) {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(context).inflate(R.layout.ecm_widget_chat_input_menu_robot, this, true);
        x.view().inject(view);
        initInputEdit();
        initVoiceInput();
        initViewpageLayout();
    }

    public void setBotUid(String botUid) {
        this.botUid = botUid;
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
                sendMsgBtn.setVisibility(isContentBlank ? View.GONE : VISIBLE);
                addBtn.setVisibility(isContentBlank ? VISIBLE : GONE);
            }
        });
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
                if (!StringUtils.isBlank(results)){
                    chatInputMenuListener.onSendMsg(results);
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

    private void initViewpageLayout() {
//        InputTypeBean inputTypeBean = new InputTypeBean(R.drawable.ic_chat_input_add_gallery, "远程控制");
//        List<InputTypeBean> inputTypeBeanList = new ArrayList<>();
//        inputTypeBeanList.add(inputTypeBean);
//        viewpagerLayout.setInputTypeBeanList(inputTypeBeanList);
    }

    @Event({R.id.voice_input_btn, R.id.send_msg_btn, R.id.add_btn, R.id.voice_input_close_img})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_input_btn:
//                Calendar calendar = Calendar.getInstance();
//                calendar.add(Calendar.HOUR,23);
//                Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
//                alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, "提醒消息 下午去XXX开会");
//                alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
//                alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE) + 1);
//                alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
//                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getContext().startActivity(alarmIntent);
                if (addMenuLayout.isShown()) {
                    addMenuLayout.setVisibility(View.GONE);
                } else if (InputMethodUtils.isSoftInputShow((Activity) getContext())) {
                    InputMethodUtils.hide((Activity) getContext());
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        voiceInputLayout.setVisibility(View.VISIBLE);
                        volumeLevelImg.setImageLevel(0);
                        mediaPlayerUtils.playVoiceOn();
                        voice2StringMessageUtils.startVoiceListening();
                    }
                }, 100);
                break;
            case R.id.send_msg_btn:
                if (NetUtils.isNetworkConnected(getContext())) {
                    String content = inputEdit.getRichContent();
                    chatInputMenuListener.onSendMsg(content);
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
            default:
                break;
        }
    }

    public boolean isVoiceInput(){
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
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) otherLayoutView
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

    public boolean isAddMenuLayoutShow(){
        return addMenuLayout.isShown();
    }


    public void hideAddMenuLayout(){
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
        void onSendMsg(String content);
    }


}
