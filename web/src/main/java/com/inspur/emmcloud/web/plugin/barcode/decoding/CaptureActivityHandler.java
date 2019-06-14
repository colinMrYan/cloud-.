/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inspur.emmcloud.web.plugin.barcode.decoding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.web.plugin.barcode.camera.CameraManager;
import com.inspur.emmcloud.web.plugin.barcode.scan.CaptureActivity;
import com.inspur.emmcloud.web.plugin.barcode.view.ViewfinderResultPointCallback;

import java.util.Vector;


/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    boolean isDecodingFromServer = false;
    private State state;

    public CaptureActivityHandler(CaptureActivity activity, Vector<BarcodeFormat> decodeFormats,
                                  String characterSet) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
                new ViewfinderResultPointCallback(activity.getViewfinderView()));
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        try {
            if (Res.getWidgetID("auto_focus") == message.what) {
                if (state == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, Res.getWidgetID("auto_focus"));
                }
            } else if (Res.getWidgetID("restart_preview") == message.what) {
                Log.d(TAG, "Got restart preview message");
                restartPreviewAndDecode();
            } else if (Res.getWidgetID("decode_succeeded") == message.what) {
                Log.d(TAG, "Got decode succeeded message");
                String str_result = ((Result) message.obj).getText();
                // 返回结果的回调函数
                //将内容区域的回车换行去除
                str_result = str_result.replaceAll("[\\t\\n\\r]", "");
                if (str_result == null || str_result.equals("")) {
                    state = State.PREVIEW;
                    CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), Res.getWidgetID("decode"));
                } else {
                    state = State.SUCCESS;
                    activity.handDecodeResult(str_result);
                }
            } else if (Res.getWidgetID("decode_failed") == message.what) {
                // We're decoding as fast as possible, so when one decode fails, startWebSocket another.
                if (message.obj != null && NetUtils.isNetworkConnected(activity, false)) {
                    Bitmap cropBitmap = (Bitmap) message.obj;
                    activity.uploadImgToDecodeByServer(cropBitmap);
                }
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), Res.getWidgetID("decode"));
            } else if (Res.getWidgetID("return_scan_result") == message.what) {
                Log.d(TAG, "Got return scan result message");
                activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
                activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), Res.getWidgetID("quit"));
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(Res.getWidgetID("decode_succeeded"));
        //removeMessages(R.id.return_scan_result);
        removeMessages(Res.getWidgetID("decode_failed"));
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), Res.getWidgetID("decode"));
            CameraManager.get().requestAutoFocus(this, Res.getWidgetID("auto_focus"));
            activity.drawViewfinder();
        }
    }

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

}
