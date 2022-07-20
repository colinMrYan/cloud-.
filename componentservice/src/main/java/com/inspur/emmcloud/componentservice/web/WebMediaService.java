package com.inspur.emmcloud.componentservice.web;

import android.os.Handler;

import com.inspur.emmcloud.componentservice.CoreService;

public interface WebMediaService extends CoreService {

    void startAudioRecord(Handler handler);

    void stopAudioRecord(WebMediaCallbackImpl callback);

    void playAudio(String path);

    void stopAudio(String path);

    void uploadAudioFile(String uploadPath, String source, WebMediaCallbackImpl callback);
}
