package com.inspur.emmcloud.interf;

/**
 * Created by yufuchang on 2018/1/18.
 */

public interface OnVoiceResultCallback {
    void onVoiceStart();
    void onVoiceResult(String results, boolean isLast);
    void onVoiceFinish();
    void onVoiceLevelChange(int volume);
}
