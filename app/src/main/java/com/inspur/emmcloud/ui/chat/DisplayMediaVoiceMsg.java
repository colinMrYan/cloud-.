package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;
import com.qmuiteam.qmui.widget.QMUILoadingView;

import java.io.File;

/**
 * Created by chenmch on 2018/8/21.
 */

public class DisplayMediaVoiceMsg {
    public static View getView(final Context context, final Message message) {
        final boolean isMyMsg = message.getFromUser().equals(MyApplication.getInstance().getUid());
        View cardContentView = LayoutInflater.from(context).inflate(R.layout.chat_msg_card_child_media_voice_view, null);
        BubbleLayout voiceLayout = (BubbleLayout) cardContentView.findViewById(R.id.bl_voice);
        voiceLayout.setArrowDirection(isMyMsg? ArrowDirection.RIGHT:ArrowDirection.LEFT);
        voiceLayout.setBubbleColor(context.getResources().getColor(isMyMsg ? R.color.bg_my_card : R.color.white));
        final View voiceAnimView = cardContentView.findViewById(R.id.v_voice_anim);
        final QMUILoadingView downloadLoadingView = (QMUILoadingView) cardContentView.findViewById(isMyMsg ? R.id.qlv_downloading_left : R.id.qlv_downloading_right);
        TextView durationText = (TextView) cardContentView.findViewById(isMyMsg ? R.id.tv_duration_left : R.id.tv_duration_right);
        durationText.setVisibility(View.VISIBLE);
        MsgContentMediaVoice msgContentMediaVoice = message.getMsgContentMediaVoice();
        int duration = msgContentMediaVoice.getDuration();
        durationText.setText(duration + "''");
        int widthDip = 90 + duration;
        if (widthDip > 230) {
            widthDip = 230;
        }
        LinearLayout.LayoutParams voiceLayoutParams = new LinearLayout.LayoutParams(DensityUtil.dip2px(context, widthDip), DensityUtil.dip2px(context, 42));
        voiceLayout.setLayoutParams(voiceLayoutParams);
        FrameLayout.LayoutParams voiceAnimViewLayoutParams = (FrameLayout.LayoutParams) voiceAnimView.getLayoutParams();
        voiceAnimViewLayoutParams.gravity=(isMyMsg? Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        voiceAnimView.setLayoutParams(voiceAnimViewLayoutParams);
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
