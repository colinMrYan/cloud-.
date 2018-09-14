package com.inspur.emmcloud.interf;

import com.inspur.emmcloud.bean.system.VoiceResult;

/**
 * Created by yufuchang on 2018/1/18.
 */

public interface OnVoiceResultCallback {
    void onVoiceStart();
    void onVoiceResult(VoiceResult results, boolean isLast);
    void onVoiceFinish();
    void onVoiceLevelChange(int volume);
    void onError(VoiceResult errorResult);
}
