package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Calendar;


public class SensorController implements SensorEventListener {
    public static final int STATUS_NONE = 0;
    public static final int STATUS_STATIC = 1;
    public static final int STATUS_MOVE = 2;
    // 缩短自动聚焦时间间隔
    public static final int DELAY_DURATION = 50;
    private static SensorController mInstance;
    private final double moveIs = 0.1;
    boolean canFocus = false;
    boolean canFocusIn = false;
    boolean isFocusing = false;
    Calendar mCalendar;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private CameraFocusListener mCameraFocusListener;
    private CameraFlashLightListener cameraFlashLightListener;
    private int mX, mY, mZ;
    private int STATUE = STATUS_NONE;
    private long lastStaticStamp = 0;

    private SensorController(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        start();
    }

    public static SensorController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SensorController(context);
        }
        return mInstance;
    }

    public void setCameraFocusListener(CameraFocusListener mCameraFocusListener) {
        this.mCameraFocusListener = mCameraFocusListener;
    }

    public void setCameraFlashLightListener(CameraFlashLightListener cameraFlashLightListener) {
        this.cameraFlashLightListener = cameraFlashLightListener;
    }

    public void start() {
        restParams();
        canFocus = true;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        mSensorManager.unregisterListener(this, mSensor);
        canFocus = false;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float light_strength = event.values[0];//光线强度
            if (cameraFlashLightListener != null) {
                cameraFlashLightListener.onChanged((light_strength < 50));
            }
        }
        if (isFocusing) {
            restParams();
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            mCalendar = Calendar.getInstance();
            long stamp = mCalendar.getTimeInMillis();
            int second = mCalendar.get(Calendar.SECOND);
            if (STATUE != STATUS_NONE) {
                int px = Math.abs(mX - x);
                int py = Math.abs(mY - y);
                int pz = Math.abs(mZ - z);
                double value = Math.sqrt(px * px + py * py + pz * pz);
                if (value >= moveIs) {
                    STATUE = STATUS_MOVE;
                } else {
                    if (STATUE == STATUS_MOVE) {
                        lastStaticStamp = stamp;
                        canFocusIn = true;
                    }

                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELAY_DURATION) {
                            //移动后静止一段时间，可以发生对焦行为
                            if (!isFocusing) {
                                canFocusIn = false;
//                                onCameraFocus();
                                if (mCameraFocusListener != null) {
                                    mCameraFocusListener.onFocus();
                                }
                            }
                        }
                    }

                    STATUE = STATUS_STATIC;
                }
            } else {
                lastStaticStamp = stamp;
                STATUE = STATUS_STATIC;
            }

            mX = x;
            mY = y;
            mZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void restParams() {
        STATUE = STATUS_NONE;
        canFocusIn = false;
        mX = 0;
        mY = 0;
        mZ = 0;
    }

    /**
     * 对焦是否被锁定
     *
     * @return
     */
    public boolean isFocusLocked() {
        return canFocus && isFocusing;
    }

    /**
     * 锁定对焦
     */
    public void lockFocus() {
        isFocusing = true;
    }

    /**
     * 解锁对焦
     */
    public void unlockFocus() {
        isFocusing = false;
    }

    public void restFocus() {
        isFocusing = false;
    }

    public interface CameraFocusListener {
        /**
         * 相机对焦中
         */
        void onFocus();
    }

    public interface CameraFlashLightListener {
        /**
         * 相机对焦中
         */
        void onChanged(boolean needOpenFlash);
    }
}
