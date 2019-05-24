package com.inspur.emmcloud;

import android.bluetooth.BluetoothHeadset;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;

import com.inspur.emmcloud.broadcastreceiver.HeadsetReceiver;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.util.common.MediaPlayerManagerUtils;

/**
 * Created by chenmch on 2018/8/25.
 */

public abstract class MediaPlayBaseActivity extends BaseActivity implements SensorEventListener {
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private Sensor sensor;
    private MediaPlayerManagerUtils playerManager;
    private HeadsetReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerManager = MediaPlayerManagerUtils.getManager();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        receiver = new HeadsetReceiver();
        playerManager.setWakeLockReleaseListener(new CommonCallBack() {
            @Override
            public void execute() {
                releaseWakeLock();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //耳机模式下直接返回
        if (playerManager.getCurrentMode() == MediaPlayerManagerUtils.MODE_HEADSET) {
            return;
        }
        float value = event.values[0];
        if (playerManager.isPlaying()) {
            if (value == sensor.getMaximumRange()) {
                playerManager.changeToSpeakerMode();
                setScreenOn();
            } else {
                playerManager.changeToEarpieceMode();
                setScreenOff();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                MediaPlayerManagerUtils.getManager().raiseVolume();
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                MediaPlayerManagerUtils.getManager().lowerVolume();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setScreenOff() {
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, this.getClass().getName());
        }
        wakeLock.acquire();
    }

    private void setScreenOn() {
        if (wakeLock != null) {
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
            wakeLock = null;
        }
    }

    public void releaseWakeLock() {
        //释放息屏
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }


    }


    @Override
    protected void onStop() {
        super.onStop();
        playerManager.stop();
        releaseWakeLock();
        sensorManager.unregisterListener(this);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
