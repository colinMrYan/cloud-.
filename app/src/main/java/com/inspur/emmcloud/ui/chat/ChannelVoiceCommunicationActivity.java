package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;

import org.xutils.view.annotation.ContentView;

/**
 * Created by yufuchang on 2018/8/14.
 */
@ContentView(R.layout.activity_voice_channel)
public class ChannelVoiceCommunicationActivity extends BaseActivity{
    VoiceCommunicationUtils voiceCommunicationUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voiceCommunicationUtils = new VoiceCommunicationUtils(this);
        voiceCommunicationUtils.initializeAgoraEngine();
        int joinChannel = voiceCommunicationUtils.joinChannel(null, "voiceDemoChannel1", "Extra Optional Data", 0);
        LogUtils.YfcDebug("加入channel返回值："+joinChannel);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceCommunicationUtils.destroy();
    }
}
