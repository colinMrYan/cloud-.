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

package com.inspur.emmcloud.web.plugin.barcode.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;


/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

    static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
    private static final String TAG = CameraManager.class.getSimpleName();
    private static final int MIN_FRAME_WIDTH = 360;
    private static final int MIN_FRAME_HEIGHT = 360;
    private static final int MAX_FRAME_WIDTH = 720;
    private static final int MAX_FRAME_HEIGHT = 540;
    private static CameraManager cameraManager;

    static {
        int sdkInt;
        try {
            sdkInt = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException nfe) {
            // Just to be safe
            sdkInt = 10000;
        }
        SDK_INT = sdkInt;
    }

    private final Context context;
    private final CameraConfigurationManager configManager;
    private final boolean useOneShotPreviewCallback;
    /**
     * Preview frames are delivered here, which we pass on to the registered
     * handler. Make sure to clear the handler so it will only receive one
     * message.
     */
    private final PreviewCallback previewCallback;
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which
     * requested them.
     */
    private final AutoFocusCallback autoFocusCallback;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;

    private CameraManager(Context context) {

        this.context = context;
        this.configManager = new CameraConfigurationManager(context);

        // Camera.setOneShotPreviewCallback() has a race condition in Cupcake,
        // so we use the older
        // Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later,
        // we need to use
        // the more efficient one shot callback, as the older one can swamp the
        // system and cause it
        // to run out of memory. We can't use SDK_INT because it was introduced
        // in the Donut SDK.
        // useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) >
        // Build.VERSION_CODES.CUPCAKE;
        useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3
        // =
        // Cupcake

        previewCallback = new PreviewCallback(configManager,
                useOneShotPreviewCallback);
        autoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Initializes this static object with the Context of the calling Activity.
     *
     * @param context The Activity which wants to use the camera.
     */
    public static void init(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }
    }

    /**
     * Gets the CameraManager singleton instance.
     *
     * @return Locale reference to the CameraManager singleton.
     */
    public static CameraManager get() {
        return cameraManager;
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames
     *               into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public void openDriver(SurfaceHolder holder) throws IOException {
        if (camera == null) {
            camera = Camera.open();
            if (camera == null) {
                throw new IOException();
            }
            camera.setPreviewDisplay(holder);

            if (!initialized) {
                initialized = true;
                configManager.initFromCameraParameters(camera);
            }
            configManager.setDesiredCameraParameters(camera);

            // SharedPreferences prefs =
            // PreferenceManager.getDefaultSharedPreferences(context);
            // �Ƿ�ʹ��ǰ��
            // if (prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false))
            // {
            // FlashlightManager.enableFlashlight();
            // }
            FlashlightManager.enableFlashlight();
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (camera != null) {
            FlashlightManager.disableFlashlight();
            camera.release();
            camera = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public void startPreview() {
        if (camera != null && !previewing) {
            camera.startPreview();
            previewing = true;
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public void stopPreview() {
        if (camera != null && previewing) {
            if (!useOneShotPreviewCallback) {
                camera.setPreviewCallback(null);
            }
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    /**
     * Locale single preview frame will be returned to the handler supplied. The data
     * will arrive as byte[] in the message.obj field, with width and height
     * encoded as message.arg1 and message.arg2, respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message);
            if (useOneShotPreviewCallback) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message);
            // Log.d(TAG, "Requesting auto-focus callback");
            camera.autoFocus(autoFocusCallback);
        }
    }

    /**
     * 是否打开闪光灯
     *
     * @param isTorch
     */
    public void setTorch(boolean isTorch) {

        FlashlightManager.enableFlashlight();
        try {
            if (context.getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH)) {
                Parameters p = camera.getParameters();
                if (isTorch) {
                    p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                } else {
                    p.setFlashMode(Parameters.FLASH_MODE_OFF);
                }

                camera.setParameters(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 放大或缩小图像
     */
    public void changeZoom() {
        try {
            if (camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                if (parameters.isZoomSupported()) {
                    int maxZoom = parameters.getMaxZoom();
                    int zoom = parameters.getZoom();
                    if (zoom == 0) {
                        parameters.setZoom(maxZoom);
                    } else {
                        parameters.setZoom(0);
                    }
                }

                camera.setParameters(parameters);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Calculates the framing rect which the UI should draw to show the user
     * where to place the barcode. This target helps with alignment as well as
     * forces the user to hold the device far enough away to ensure the image
     * will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            Point screenResolution = configManager.getScreenResolution();
            if (screenResolution == null) {
                // Called early, before startWebSocket even finished
                return null;
            }

            // int width = findDesiredDimensionInRange(screenResolution.x,
            // MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            // int height = findDesiredDimensionInRange(screenResolution.y,
            // MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

			/* ɨ����޸� */
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.6);
            int height = width;

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (int) ((screenResolution.y - height) / 2.5);
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
                    topOffset + height);
            Log.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview
     * frame, not UI / screen.
     */
    public Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect rect = new Rect(getFramingRect());
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            // rect.left = rect.left * cameraResolution.x / screenResolution.x;
            // rect.right = rect.right * cameraResolution.x /
            // screenResolution.x;
            // rect.top = rect.top * cameraResolution.y / screenResolution.y;
            // rect.bottom = rect.bottom * cameraResolution.y /
            // screenResolution.y;
            rect.left = rect.left * cameraResolution.y / screenResolution.x - 50;
            rect.right = rect.right * cameraResolution.y / screenResolution.x + 50;
            rect.top = rect.top * cameraResolution.x / screenResolution.y - 50;
            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y + 50;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    /**
     * Converts the result points from still resolution coordinates to screen
     * coordinates.
     *
     * @param points
     *            The points returned by the Reader subclass through
     *            Result.getResultPoints().
     * @return An array of Points scaled to the size of the framing rect and
     *         offset appropriately so they can be drawn in screen coordinates.
     */
    /*
     * public Point[] convertResultPoints(ResultPoint[] points) { Rect frame =
	 * getFramingRectInPreview(); int count = points.length; Point[] output =
	 * new Point[count]; for (int x = 0; x < count; x++) { output[x] = new
	 * Point(); output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
	 * output[x].y = frame.top + (int) (points[x].getY() + 0.5f); } return
	 * output; }
	 */

    /**
     * Locale factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   Locale preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return Locale PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
                                                         int width, int height) {

        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left,
                rect.top, rect.width(), rect.height());


//		
//		Rect rect = getFramingRectInPreview();
//		int previewFormat = configManager.getPreviewFormat();
//		String previewFormatString = configManager.getPreviewFormatString();
//		switch (previewFormat) {
//		// This is the standard Android format which all devices are REQUIRED to
//		// support.
//		// In theory, it's the only one we should ever care about.
//		case PixelFormat.YCbCr_420_SP:
//			// This format has never been seen in the wild, but is compatible as
//			// we only care
//			// about the Y channel, so allow it.
//		case PixelFormat.YCbCr_422_SP:
//			return new PlanarYUVLuminanceSource(data, width, height, rect.left,
//					rect.top, rect.width(), rect.height());
//		default:
//			// The Samsung Moment incorrectly uses this variant instead of the
//			// 'sp' version.
//			// Fortunately, it too has all the Y data up front, so we can read
//			// it.
//			if ("yuv420p".equals(previewFormatString)) {
//				return new PlanarYUVLuminanceSource(data, width, height,
//						rect.left, rect.top, rect.width(), rect.height());
//			}
//		}
//		throw new IllegalArgumentException("Unsupported picture format: "
//				+ previewFormat + '/' + previewFormatString);
    }

}
