package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
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
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

import java.io.File;

/**
 * Created by chenmch on 2018/8/21.
 */

public class DisplayMediaVoiceMsg {
    public static final boolean IS_VOICE_WORD_OPEN = true;
    public static final boolean IS_VOICE_WORD_CLOUSE = false;

    public static View getView(final Context context, final UIMessage uiMessage, final ChannelMessageAdapter.MyItemClickListener mItemClickListener) {
        final Message message = uiMessage.getMessage();
        final boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        final View cardContentView = LayoutInflater.from(context).inflate(R.layout.chat_msg_card_child_media_voice_view, null);
        final BubbleLayout voiceBubbleLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_voice);
        voiceBubbleLayout.setArrowDirection(isMyMsg ? ArrowDirection.RIGHT : ArrowDirection.LEFT);
        voiceBubbleLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.bg_other_card));
        voiceBubbleLayout.setStrokeWidth(isMyMsg ? 0 : 0.5f);
        final View voiceAnimView = isMyMsg ? cardContentView.findViewById(R.id.v_voice_anim_right) : cardContentView.findViewById(R.id.v_voice_anim_left);
        voiceAnimView.setVisibility(View.VISIBLE);
        final CustomLoadingView downloadLoadingView = (CustomLoadingView) cardContentView.findViewById(isMyMsg ? R.id.qlv_downloading_left : R.id.qlv_downloading_right);
        TextView durationText = (TextView) cardContentView.findViewById(isMyMsg ? R.id.tv_duration_left : R.id.tv_duration_right);
        //校正UI，因消息部分未支持撤回机制，暂不开放，控制两分以内发送的消息才显示校正
        //TextView correctedSpeechInputText = (TextView) cardContentView.findViewById(R.id.tv_corrected_speech_input);
        //correctedSpeechInputText.setVisibility(((System.currentTimeMillis() - message.getCreationDate() <= 120 * 1000) && isMyMsg)?View.VISIBLE:View.GONE);
        durationText.setVisibility(View.VISIBLE);
        MsgContentMediaVoice msgContentMediaVoice = message.getMsgContentMediaVoice();
        TextView speechText = (TextView) cardContentView.findViewById(R.id.tv_voice_card_word);
        int duration = msgContentMediaVoice.getDuration();
        durationText.setText(duration + "''");
        speechText.setVisibility(View.VISIBLE);
        speechText.setText(msgContentMediaVoice.getResult());
        speechText.setTextColor(isMyMsg ? Color.parseColor("#FFFFFF") : Color.parseColor("#666666"));
        final View unPackView = cardContentView.findViewById(R.id.v_pack);
        unPackView.setVisibility(!isMyMsg && (message.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE) && message.getLifeCycleState() == Message.MESSAGE_LIFE_PACK) ? View.VISIBLE : View.GONE);
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
        voiceAnimView.setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_card_voice_right_level_3 : R.drawable.ic_chat_msg_card_voice_left_level_3);
        voiceBubbleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uiMessage.getSendStatus() != 1) {
                    return;
                }
                if (downloadLoadingView.getVisibility() == View.VISIBLE) {
                    return;
                }
                final String fileSavePath = MyAppConfig.getCacheVoiceFilePath(message.getChannel(), message.getId());
                if (MediaPlayerManagerUtils.getManager().isPlaying(fileSavePath)) {
                    MediaPlayerManagerUtils.getManager().stop();
                    return;
                }
                if (MediaPlayerManagerUtils.getManager().isPlaying()) {
                    MediaPlayerManagerUtils.getManager().stop();
                }


                if (!FileUtils.isFileExist(fileSavePath)) {
                    downloadLoadingView.setVisibility(View.VISIBLE);
                    String source = APIUri.getChatVoiceFileResouceUrl(message.getChannel(), message.getMsgContentMediaVoice().getMedia());
                    new DownLoaderUtils().startDownLoad(source, fileSavePath, new APIDownloadCallBack(source) {

                        @Override
                        public void callbackSuccess(File file) {
                            downloadLoadingView.setVisibility(View.GONE);
                            //当下载完成时如果mediaplayer没有被占用则播放语音
                            if (!MediaPlayerManagerUtils.getManager().isPlaying()) {
                                playVoiceFile(fileSavePath, voiceAnimView, isMyMsg);
                                setVoiceAnimViewBgByPlayStatus(voiceAnimView, true, isMyMsg);
                            }
                            setVoiceUnPack(unPackView, context, message);
                        }

                        @Override
                        public void callbackError(Throwable arg0, boolean arg1) {
                            downloadLoadingView.setVisibility(View.GONE);
                            ToastUtils.show(MyApplication.getInstance(), R.string.play_fail);
                        }

                        @Override
                        public void onCancelled(CancelledException e) {
                        }
                    });
                } else {
                    playVoiceFile(fileSavePath, voiceAnimView, isMyMsg);
                    setVoiceAnimViewBgByPlayStatus(voiceAnimView, true, isMyMsg);
                    setVoiceUnPack(unPackView, context, message);
                }
            }
        });
        voiceBubbleLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //当此语音正在播放时，用户点击会暂停播放
                if (MediaPlayerManagerUtils.getManager().isPlaying()) {
                    MediaPlayerManagerUtils.getManager().stop();
                }
                if (mItemClickListener != null) {
                    mItemClickListener.onMediaVoiceReRecognize(uiMessage, cardContentView, downloadLoadingView);
                }
                setVoiceUnPack(cardContentView.findViewById(R.id.v_pack), context, message);
                return false;
            }
        });
        return cardContentView;
    }

    /**
     * 设置消息已经拆包
     *
     * @param context
     * @param message
     */
    private static void setVoiceUnPack(View unPackView, Context context, Message message) {
        message.setLifeCycleState(Message.MESSAGE_LIFE_UNPACK);
        MessageCacheUtil.saveMessageLifeCycleState(context, message);
        unPackView.setVisibility(View.GONE);
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
                voiceAnimView.setBackgroundResource(isMyMsg ? R.drawable.chat_voice_message_play_right : R.drawable.chat_voice_message_play_left);
                AnimationDrawable drawable = (AnimationDrawable) voiceAnimView
                        .getBackground();
                if (drawable.isRunning()) {
                    drawable.stop();
                }
                drawable.start();
            } else {
                voiceAnimView.setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_card_voice_right_level_3 : R.drawable.ic_chat_msg_card_voice_left_level_3);
            }
        }
    }

    /**
     * 播放语音
     *
     * @param fileSavePath
     * @param voiceAnimView
     * @param isMyMsg
     */
    private static void playVoiceFile(String fileSavePath, final View voiceAnimView, final boolean isMyMsg) {
        MediaPlayerManagerUtils.getManager().play(fileSavePath, new MediaPlayerManagerUtils.PlayCallback() {
            @Override
            public void onPrepared() {
            }

            @Override
            public void onComplete() {
                setVoiceAnimViewBgByPlayStatus(voiceAnimView, false, isMyMsg);
            }

            @Override
            public void onStop() {
                setVoiceAnimViewBgByPlayStatus(voiceAnimView, false, isMyMsg);
            }
        });
    }


}
