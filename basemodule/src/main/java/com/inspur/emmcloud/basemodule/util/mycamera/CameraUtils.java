package com.inspur.emmcloud.basemodule.util.mycamera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chenmch on 2017/6/22.
 */

public class CameraUtils {
    private static CameraUtils myCamPara = null;
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();

    private CameraUtils() {

    }

    public static CameraUtils getInstance() {
        if (myCamPara == null) {
            myCamPara = new CameraUtils();
            return myCamPara;
        } else {
            return myCamPara;
        }
    }

    /**
     * 获取preview的size
     *
     * @param list
     * @param th   最小尺寸
     * @return
     */
    public Size getPreviewSize(List<Size> list, int th, float rate) {
        Collections.sort(list, sizeComparator);
//        for (Size s : list) {
//            LogUtils.jasonDebug(s.width + "*" + s.height);
//        }
        Size size = null;
        for (Size s : list) {
            if (s.width >= th && s.height >= th && equalRateLevel0(s, rate)) {
                size = s;
                break;
            }
        }
        if (size == null) {
            for (Size s : list) {
                if (s.width >= th && s.height >= th && equalRateLevel1(s, rate)) {
                    size = s;
                    break;
                }
            }
        }

        if (size == null) {
            for (Size s : list) {
                if (s.width >= th && s.height >= th && equalRateLevel2(s, rate)) {
                    size = s;
                    break;
                }
            }
        }
        if (size == null) {
            return list.get(list.size() - 1);
        }
        return size;
    }

    /**
     * 获取图片的大小
     *
     * @param list
     * @param th   最小尺寸
     * @return
     */
    public Size getPictureSize(List<Size> list, int th, float rate) {
        Collections.sort(list, sizeComparator);
//        for (Size s : list) {
//            LogUtils.jasonDebug(s.width + "*" + s.height);
//        }
        Size size = null;
        for (Size s : list) {
            if (s.width >= th && s.height >= th && equalRateLevel0(s, rate)) {
                size = s;
                break;
            }
        }
        if (size == null) {
            for (Size s : list) {
                if (s.width >= th && s.height >= th && equalRateLevel1(s, rate)) {
                    size = s;
                    break;
                }
            }
        }

        if (size == null) {
            for (Size s : list) {
                if (s.width >= th && s.height >= th && equalRateLevel2(s, rate)) {
                    size = s;
                    break;
                }
            }
        }
        if (size == null) {
            return list.get(list.size() - 1);
        }
        return size;
    }

    public boolean equalRateLevel0(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.01;
    }

    public boolean equalRateLevel1(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.2;
    }

    public boolean equalRateLevel2(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.5;
    }


    public boolean isSupportedFocusMode(List<String> focusList, String focusMode) {
        for (int i = 0; i < focusList.size(); i++) {
            if (focusMode.equals(focusList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int jpeg) {
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            if (jpeg == supportedPictureFormats.get(i)) {
                return true;
            }
        }
        return false;
    }

    public int getCameraDisplayOrientation(Context context, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    public class CameraSizeComparator implements Comparator<Size> {
        //按升序排列
        @Override
        public int compare(Size lhs, Size rhs) {
            // TODO Auto-generated method stub
            if (lhs.width == rhs.width && lhs.height == rhs.height) {
                return 0;
            } else if (lhs.width > rhs.width || (lhs.width == rhs.width && lhs.height > rhs.height)) {
                return 1;
            } else {
                return -1;
            }
        }

    }

}
