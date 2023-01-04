package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMessageAdapter;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.UIMessage;

/**
 * Created by chenmch on 2018/8/21.
 */

public class Design3DisplayMediaVoiceMsg {

    public static final int VOICE_NOT_DOWNLOAD = 0;
    public static final int VOICE_DOWNLOADING = 1;
    public static final int VOICE_DOWNLOAD_FINISH = 2;
    public static final int VOICE_NOT_PLAY = 3;
    public static final int VOICE_PLAYING = 4;
    public static final int VOICE_PLAY_COMPELTE = 5;
    public static final int VOICE_PLAY_STOP = 6;

    public static View getView(final Context context, final UIMessage uiMessage, final ChannelMessageAdapter.MyItemClickListener mItemClickListener) {
        final Message message = uiMessage.getMessage();
        final boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        final View cardContentView = LayoutInflater.from(context).inflate(R.layout.design3_chat_msg_card_child_media_voice_view, null);
        final BubbleLayout voiceBubbleLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_voice);
        voiceBubbleLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        voiceBubbleLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_th06)
                : ResourceUtils.getResValueOfAttr(context, R.attr.design3_color_ne15)));
        voiceBubbleLayout.setStrokeWidth(0);
        final View voiceAnimView = isMyMsg ? cardContentView.findViewById(R.id.v_voice_anim_right) : cardContentView.findViewById(R.id.v_voice_anim_left);
        voiceAnimView.setVisibility(View.VISIBLE);
        final View unPackView = cardContentView.findViewById(R.id.v_pack);
        unPackView.setVisibility(!isMyMsg && (message.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)
                && message.getLifeCycleState() == Message.MESSAGE_LIFE_PACK) ? View.VISIBLE : View.GONE);
        final CustomLoadingView downloadLoadingView = (CustomLoadingView) cardContentView.findViewById(isMyMsg ? R.id.qlv_downloading_left : R.id.qlv_downloading_right);
        TextView durationText = (TextView) cardContentView.findViewById(isMyMsg ? R.id.tv_duration_left : R.id.tv_duration_right);
        //校正UI，因消息部分未支持撤回机制，暂不开放，控制两分以内发送的消息才显示校正
        //TextView correctedSpeechInputText = (TextView) cardContentView.findViewById(R.id.tv_corrected_speech_input);
        //correctedSpeechInputText.setVisibility(((System.currentTimeMillis() - message.getCreationDate() <= 120 * 1000) && isMyMsg)?View.VISIBLE:View.GONE);
        final String fileSavePath = MyAppConfig.getCacheVoiceFilePath(message.getChannel(), message.getId());
        durationText.setVisibility(View.VISIBLE);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setVoiceAnimViewBgByPlayStatus(voiceAnimView, uiMessage.getVoicePlayState() == VOICE_PLAYING, isMyMsg);
            }
        });

        MsgContentMediaVoice msgContentMediaVoice = message.getMsgContentMediaVoice();
        TextView speechText = (TextView) cardContentView.findViewById(R.id.tv_voice_card_word);
        int duration = msgContentMediaVoice.getDuration();
        durationText.setText(duration + "''");
        speechText.setVisibility(View.VISIBLE);
        speechText.setText(msgContentMediaVoice.getResult());
        if (StringUtils.isBlank(msgContentMediaVoice.getResult())) {
            int widthDip = 90 + duration;
            if (widthDip > 230) {
                widthDip = 230;
            }
            LinearLayout.LayoutParams voiceBubbleLayoutParams = new LinearLayout.LayoutParams(DensityUtil.dip2px(context, widthDip), LinearLayout.LayoutParams.WRAP_CONTENT);
            voiceBubbleLayout.setLayoutParams(voiceBubbleLayoutParams);
            RelativeLayout voiceLayout = (RelativeLayout) cardContentView.findViewById(R.id.rl_voice);
            FrameLayout.LayoutParams voiceLayoutParams = (FrameLayout.LayoutParams) voiceLayout.getLayoutParams();
            voiceLayoutParams.gravity = isMyMsg ? Gravity.RIGHT : Gravity.LEFT;
            voiceLayout.setLayoutParams(voiceLayoutParams);
        }
        voiceAnimView.setBackgroundResource(isMyMsg ? ResourceUtils.getResValueOfAttr(context, R.attr.design3_icon_chat_voice_mine) :
                ResourceUtils.getResValueOfAttr(context, R.attr.design3_icon_chat_voice));
        voiceBubbleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadLoadingView.setVisibility(FileUtils.isFileExist(fileSavePath) ? View.GONE : View.VISIBLE);
                if (mItemClickListener != null) {
                    mItemClickListener.onCardItemClick(cardContentView, uiMessage);
                }
            }
        });

        voiceBubbleLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onCardItemLongClick(cardContentView, uiMessage);
                }
                return false;
            }
        });
        return cardContentView;
    }


    /**
     * 根据播放状态设置VoiceAnimView的背景
     *
     * @param voiceAnimView
     * @param isPlaying
     * @param isMyMsg
     */
    private static void setVoiceAnimViewBgByPlayStatus(View voiceAnimView, boolean isPlaying, boolean isMyMsg) {
        if (voiceAnimView != null) {
            if (isPlaying) {
                voiceAnimView.setBackgroundResource(isMyMsg ? ResourceUtils.getResValueOfAttr(voiceAnimView.getContext(), R.attr.design3_anim_chat_voice_mine) :
                        ResourceUtils.getResValueOfAttr(voiceAnimView.getContext(), R.attr.design3_anim_chat_voice));
                final AnimationDrawable drawable = (AnimationDrawable) voiceAnimView.getBackground();
                if (drawable.isRunning()) {
                    drawable.stop();
                }
                drawable.start();
            } else {
                voiceAnimView.setBackgroundResource(isMyMsg ? ResourceUtils.getResValueOfAttr(voiceAnimView.getContext(), R.attr.design3_icon_chat_voice_mine) :
                        ResourceUtils.getResValueOfAttr(voiceAnimView.getContext(), R.attr.design3_icon_chat_voice));
            }
        }
    }

}
