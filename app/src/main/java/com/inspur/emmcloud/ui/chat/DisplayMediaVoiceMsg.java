package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;
import com.qmuiteam.qmui.widget.QMUILoadingView;

import java.io.File;

/**
 * Created by chenmch on 2018/8/21.
 */

public class DisplayMediaVoiceMsg {
    public static final int IS_VOICE_WORD_OPEN = 1;
    public static final int IS_VOICE_WORD_CLOUSE = 0;
    public static View getView(final Context context, final Message message) {
        final boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        View cardContentView = LayoutInflater.from(context).inflate(R.layout.chat_msg_card_child_media_voice_view, null);
        BubbleLayout voiceLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_voice);
        voiceLayout.setArrowDirection(isMyMsg? ArrowDirection.RIGHT:ArrowDirection.LEFT);
        voiceLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.white));
        voiceLayout.setStrokeWidth(isMyMsg ?0: 0.5f);
        final View voiceAnimView = isMyMsg?cardContentView.findViewById(R.id.v_voice_anim_right):cardContentView.findViewById(R.id.v_voice_anim_left);
        voiceAnimView.setVisibility(View.VISIBLE);
        final QMUILoadingView downloadLoadingView = (QMUILoadingView) cardContentView.findViewById(isMyMsg ? R.id.qlv_downloading_left : R.id.qlv_downloading_right);
        TextView durationText = (TextView) cardContentView.findViewById(isMyMsg ? R.id.tv_duration_left : R.id.tv_duration_right);
        //校正UI，因消息部分未支持撤回机制，暂不开放，控制两分以内发送的消息才显示校正
        //TextView correctedSpeechInputText = (TextView) cardContentView.findViewById(R.id.tv_corrected_speech_input);
        //correctedSpeechInputText.setVisibility(((System.currentTimeMillis() - message.getCreationDate() <= 120 * 1000) && isMyMsg)?View.VISIBLE:View.GONE);
        durationText.setVisibility(View.VISIBLE);
        MsgContentMediaVoice msgContentMediaVoice = message.getMsgContentMediaVoice();
        TextView speechText = (TextView) cardContentView.findViewById(R.id.tv_voice_card_word);
        speechText.setText(msgContentMediaVoice.getResult());
        speechText.setTextColor(isMyMsg? Color.parseColor("#FFFFFF"):Color.parseColor("#666666"));
        int duration = msgContentMediaVoice.getDuration();
        durationText.setText(duration + "''");
        //控制是否打开显示文字的功能，打开和不打开分两种UI控制逻辑
        switch (PreferencesByUserAndTanentUtils.getInt(context,Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH,IS_VOICE_WORD_OPEN)){
            case IS_VOICE_WORD_OPEN:
                speechText.setVisibility(View.VISIBLE);
                break;
            case IS_VOICE_WORD_CLOUSE:
                speechText.setVisibility(View.GONE);
                int widthDip = 90 + duration;
                if (widthDip > 230) {
                    widthDip = 230;
                }
                LinearLayout.LayoutParams voiceLayoutParams = new LinearLayout.LayoutParams(DensityUtil.dip2px(context, widthDip), LinearLayout.LayoutParams.WRAP_CONTENT);
                voiceLayout.setLayoutParams(voiceLayoutParams);
                break;
            default:
                break;
        }


        voiceAnimView.setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_card_voice_right_level_3 : R.drawable.ic_chat_msg_card_voice_left_level_3);
        voiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getSendStatus() != 1) {
                    return;
                }
                if (downloadLoadingView.getVisibility() == View.VISIBLE) {
                    return;
                }
                final String fileSavePath = MyAppConfig.getCacheVoiceFilePath(message.getChannel(), message.getId());
                if (!FileUtils.isFileExist(fileSavePath)) {
                    downloadLoadingView.setVisibility(View.VISIBLE);
                    String source = APIUri.getChatVoiceFileResouceUrl(message.getChannel(), message.getMsgContentMediaVoice().getMedia());
                    new DownLoaderUtils().startDownLoad(source, fileSavePath, new APIDownloadCallBack(source) {

                        @Override
                        public void callbackSuccess(File file) {
                            downloadLoadingView.setVisibility(View.GONE);
                            //当下载完成时如果mediaplayer没有被占用则播放语音
                            if (!MediaPlayerManagerUtils.getManager().isPlaying()) {
                                setVoiceAnimViewBgByPlayStatus(voiceAnimView, true, isMyMsg);
                                playVoiceFile(fileSavePath, voiceAnimView, isMyMsg);
                            }

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
                    setVoiceAnimViewBgByPlayStatus(voiceAnimView, true, isMyMsg);
                    playVoiceFile(fileSavePath, voiceAnimView, isMyMsg);
                }
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
        //当此语音正在播放时，用户点击会暂停播放
        if (MediaPlayerManagerUtils.getManager().isPlaying(fileSavePath)) {
            MediaPlayerManagerUtils.getManager().stop();
        } else {
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


}
