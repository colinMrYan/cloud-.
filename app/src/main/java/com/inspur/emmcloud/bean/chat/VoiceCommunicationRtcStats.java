package com.inspur.emmcloud.bean.chat;

/**
 * Created by yufuchang on 2018/8/16.
 */

public class VoiceCommunicationRtcStats {
    public int totalDuration;
    public int txBytes;
    public int rxBytes;
    public int txKBitRate;
    public int rxKBitRate;
    public int txAudioKBitRate;
    public int rxAudioKBitRate;
    public int txVideoKBitRate;
    public int rxVideoKBitRate;
    public int users;
    public double cpuTotalUsage;
    public double cpuAppUsage;

    public VoiceCommunicationRtcStats() {
    }
}