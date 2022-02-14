package com.inspur.emmcloud.componentservice.web;

import com.inspur.emmcloud.componentservice.CoreService;
import com.inspur.emmcloud.componentservice.download.ProgressCallback;

public interface WebMediaService extends CoreService {

    void startAudioRecord();

    void stopAudioRecord(WebMediaCallbackImpl callback);

    void playAudio(String path);

    void stopAudio(String path);

    void uploadAudioFile(String uploadPath, String source, WebMediaCallbackImpl callback);
}
