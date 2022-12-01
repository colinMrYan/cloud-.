package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.state;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.CameraInterface;
import com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary.JCameraView;


class PreviewState implements State {
    public static final String TAG = "PreviewState";

    private CameraMachine machine;

    PreviewState(CameraMachine machine) {
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
    }

    @Override
    public void stop() {
        CameraInterface.getInstance().doStopPreview();
    }


    @Override
    public void focus(float x, float y, CameraInterface.FocusCallback callback, boolean isShowFocusView) {
        if (isShowFocusView) {
            if (machine.getView().handlerFoucs(x, y)) {
                CameraInterface.getInstance().handleFocus(machine.getContext(), x, y, callback);
            }
        } else {
            CameraInterface.getInstance().handleFocus(machine.getContext(), x, y, callback);
        }

    }

    @Override
    public void switchCamera(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().switchCamera(holder, screenProp);
    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {
        // 打开闪光灯后能够拍照，设置延迟时间300ms使硬件灯光打开
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback() {
                    @Override
                    public void captureResult(Bitmap bitmap, boolean isVertical) {
                        machine.getView().showPicture(bitmap, isVertical);
                        machine.setState(machine.getBorrowPictureState());
                        CameraInterface.getInstance().turnLightOff();
                    }
                });
            }
        }, 300);

    }

    @Override
    public void record(Surface surface, float screenProp) {
        CameraInterface.getInstance().startRecord(surface, screenProp, null);
    }

    @Override
    public void stopRecord(final boolean isShort, long time) {
        CameraInterface.getInstance().stopRecord(isShort, new CameraInterface.StopRecordCallback() {
            @Override
            public void recordResult(String url, Bitmap firstFrame) {
                if (isShort) {
                    machine.getView().resetState(JCameraView.TYPE_SHORT);
                } else {
                    machine.getView().playVideo(firstFrame, url);
                    machine.setState(machine.getBorrowVideoState());
                }
            }
        });
    }

    @Override
    public void cancel(SurfaceHolder holder, float screenProp) {
    }

    @Override
    public void confirm() {
    }

    @Override
    public void zoom(float zoom, int type) {
        CameraInterface.getInstance().setZoom(zoom, type);
    }

    @Override
    public void flash(String mode) {
        CameraInterface.getInstance().setCameraFlashMode(mode, null);
    }
}
