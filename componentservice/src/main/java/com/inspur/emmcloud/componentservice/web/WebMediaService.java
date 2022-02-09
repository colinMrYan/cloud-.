package com.inspur.emmcloud.componentservice.web;

import com.inspur.emmcloud.componentservice.CoreService;

public interface WebMediaService extends CoreService {

    void startAudioRecord();

    String stopAudioRecord();

    void playAudio(String path);

    void stopAudio(String path);

    void uploadAudioFile();
}
