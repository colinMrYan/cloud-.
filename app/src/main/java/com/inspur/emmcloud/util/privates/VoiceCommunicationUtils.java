package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

/**
 * Created by yufuchang on 2018/8/13.
 */

public class VoiceCommunicationUtils {

    private Context context;
    private RtcEngine mRtcEngine;

    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserOffline(int uid, int reason) {
//            super.onUserOffline(uid, reason);
            LogUtils.YfcDebug("用户离线："+uid);
            LogUtils.YfcDebug("用户离线："+reason);
        }

        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            super.onUserMuteAudio(uid, muted);
        }
    };

    public VoiceCommunicationUtils(Context context){
        this.context = context;
    }

    public void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            LogUtils.YfcDebug("初始化异常："+e.getMessage());
        }
    }

    // Tutorial Step 2
    public int joinChannel(String  token, String  channelName, String  optionalInfo, int  optionalUid) {
        // 如果不指定optionalUid将自动生成一个
        return mRtcEngine.joinChannel(token, channelName, optionalInfo, optionalUid);
    }

    /**
     * 离开频道
     */
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 4
    private void onRemoteUserLeft(int uid, int reason) {
    }

    // Tutorial Step 5
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(context.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.setEnableSpeakerphone(view.isSelected());
    }

    // Tutorial Step 6
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {

    }

    public void destroy(){
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

}
