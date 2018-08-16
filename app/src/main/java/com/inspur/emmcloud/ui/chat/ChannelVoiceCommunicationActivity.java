package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;

import org.xutils.view.annotation.ContentView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/14.
 */
@ContentView(R.layout.activity_voice_channel)
public class ChannelVoiceCommunicationActivity extends BaseActivity{
    private VoiceCommunicationUtils voiceCommunicationUtils;
    private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
    private static final int CHOOSE_MEMBERS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voiceCommunicationUtils = new VoiceCommunicationUtils(this);
        voiceCommunicationUtils.initializeAgoraEngine();
        initCallbacks();
        int joinChannel = voiceCommunicationUtils.joinChannel(null, "10011", "Extra Optional Data", Integer.parseInt(MyApplication.getInstance().getUid()));
        LogUtils.YfcDebug("加入channel返回值："+joinChannel);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_MEMBERS){
            if (data.getExtras().containsKey("selectMemList")) {
                selectMemList = (List<SearchModel>) data.getExtras()
                        .getSerializable("selectMemList");
            }
            LogUtils.YfcDebug("已经选择的人员数量："+selectMemList.size());
        }
    }

    /**
     * 初始化回调
     */
    private void initCallbacks() {
        voiceCommunicationUtils.setOnVoiceCommunicationCallbacks(new OnVoiceCommunicationCallbacksImpl() {
            @Override
            public void onUserOffline(int uid, int reason) {

            }

            @Override
            public void onUserJoined(int uid, int elapsed) {

            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                LogUtils.YfcDebug("1111111111onJoinChannelSuccess  channel:"+channel);
                LogUtils.YfcDebug("1111111111onJoinChannelSuccess uid:"+uid);
                LogUtils.YfcDebug("1111111111onJoinChannelSuccess eslapsed:"+elapsed);
            }

            @Override
            public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {

            }

            @Override
            public void onUserMuteAudio(int uid, boolean muted) {

            }

            @Override
            public void onError(int err) {

            }

            @Override
            public void onConnectionLost() {

            }
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
            case R.id.btn_add_member:
                Intent intent = new Intent();
                intent.putExtra("select_content", 2);
                intent.putExtra("isMulti_select", true);
                intent.putExtra("isContainMe", true);
                intent.putExtra("title", "选择人员");
                if (selectMemList != null) {
                    intent.putExtra("hasSearchResult", (Serializable) selectMemList);
                }
                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intent, CHOOSE_MEMBERS);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceCommunicationUtils.destroy();
    }
}
