package com.inspur.emmcloud.broadcastreceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;


/**
 * Created by: yufuchang
 * Date: 2019/10/24
 */
public class VoiceCommunicationHeadSetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            //插入和拔出耳机会触发此广播
            case Intent.ACTION_HEADSET_PLUG:
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    MediaPlayerManagerUtils.getManager().changeToHeadsetMode();
                }
                break;
            //拔出耳机会触发此广播,拔出不会触发,且此广播比上一个早,故可在此暂停播放,收到上一个广播时在恢复播放
            case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                MediaPlayerManagerUtils.getManager().changeToSpeakerMode();
                break;
            case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                    MediaPlayerManagerUtils.getManager().changeToSpeakerMode();
                } else {
                    MediaPlayerManagerUtils.getManager().changeToHeadsetMode();
                }
                break;
            default:
                break;
        }
    }
}
