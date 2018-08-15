package com.inspur.emmcloud.util.privates;

import android.content.Context;

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
    private int userCount = 1;

    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        //用户离线回调
        @Override
        public void onUserOffline(int uid, int reason) {
            LogUtils.YfcDebug("用户离线uid:"+uid);
            LogUtils.YfcDebug("用户离线reason:"+reason);
            userCount = userCount - 1;
            if(userCount < 2){
                destroy();
            }
        }

        //用户加入频道回调
        @Override
        public void onUserJoined(int uid, int elapsed) {
            LogUtils.YfcDebug("用户上线："+uid);
            LogUtils.YfcDebug("用户上线："+elapsed);
        }

        //加入频道成功
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            LogUtils.YfcDebug("onJoinChannelSuccess  channel:"+channel);
            LogUtils.YfcDebug("onJoinChannelSuccess uid:"+uid);
            LogUtils.YfcDebug("onJoinChannelSuccess eslapsed:"+elapsed);
        }

        //重新加入频道成功
        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            LogUtils.YfcDebug("onRejoinChannelSuccess");
        }

        //每隔两秒钟返回一次频道内的状态信息
        @Override
        public void onRtcStats(RtcStats stats) {
            LogUtils.YfcDebug("RtcStats:"+stats.users);
        }

        //静音监听
        @Override
        public void onUserMuteAudio(int uid, boolean muted) {

        }

        //warning信息
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            LogUtils.YfcDebug("warning信息："+warn);
        }

        //error信息
        @Override
        public void onError(int err) {
            super.onError(err);
            LogUtils.YfcDebug("error信息："+err);
        }

        //失去连接信息
        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
        }

        @Override
        public void onConnectionBanned() {
            super.onConnectionBanned();
        }

        //网络质量回调
        @Override
        public void onLastmileQuality(int quality) {
            super.onLastmileQuality(quality);
            LogUtils.YfcDebug("网络质量："+quality);
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            super.onAudioVolumeIndication(speakers, totalVolume);
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

    /**
     * 加入频道
     * @param token
     * @param channelName
     * @param optionalInfo
     * @param optionalUid
     * @return
     */
    public int joinChannel(String  token, String  channelName, String  optionalInfo, int  optionalUid) {
        // 如果不指定optionalUid将自动生成一个
        return mRtcEngine.joinChannel(token, channelName, optionalInfo, optionalUid);
    }

    /**
     * 离开频道，不让外部主动调用，外部可以主动调用destroy方法
     */
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    /**
     * 设置频道模式
     * @param profile
     */
    public void setChannelProfile(int profile){
        mRtcEngine.setChannelProfile(profile);
    }

    /**
     * 打开外放
     * @param isSpakerphoneOpen
     */
    public void onSwitchSpeakerphoneClicked(boolean isSpakerphoneOpen) {
        mRtcEngine.setEnableSpeakerphone(isSpakerphoneOpen);
    }

    /**
     * 离开频道销毁资源
     */
    public void destroy(){
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

}
