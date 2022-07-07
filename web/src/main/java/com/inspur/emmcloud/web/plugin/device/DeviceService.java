package com.inspur.emmcloud.web.plugin.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.TimeZone;

/**
 * 设备信息
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class DeviceService extends ImpPlugin {

    public static final String TAG = "Device";
    private static final String ANDROID_PLATFORM = "Android";
    private static final String AMAZON_PLATFORM = "amazon-fireos";
    private static final String AMAZON_DEVICE = "Amazon";
    // Device OS
    public static String platform;
    // Device UUID
    public static String uuid;
    private String successCb, failCb;

    // beep组件的响的次数
    private int count;
    private Camera m_Camera;
    private CameraManager manager;
    private int mBackCameraId = 0;
    private int mFrontCameraId = 1;

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        String res = "";
        if ("getInfo".equals(action)) {
            res = conbineDeviceInfo().toString();
        } else {
            showCallIMPMethodErrorDlg();
        }
        return res;
    }

    /**
     * 组装设备信息
     *
     * @return
     */
    private JSONObject conbineDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        String res = "";
        // 检查网络连接
        try {
            // 设备操作系统版本
            jsonObject
                    .put("version", String.valueOf(this.getOSVersion()));
            // 获取操作系统
            jsonObject.put("platform", String.valueOf(this.getPlatform()));
            // 获取设备国际唯一标识码
            jsonObject.put("uuid", AppUtils.getMyUUID(getFragmentContext()).hashCode() + "");
            jsonObject.put("model", getModel());
            jsonObject.put("bundleId", AppUtils.getPackageName(getFragmentContext()));
            jsonObject.put("appVersion", AppUtils.getVersion(getFragmentContext()));
            int width = ResolutionUtils.getWidth(getActivity());
            int height = ResolutionUtils.getHeight(getActivity());
            jsonObject.put("resolution", width < height ? (width + "," + height) : (height + "," + width));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    @Override
    public void execute(String action, JSONObject jsonObject) {
        successCb = JSONUtils.getString(jsonObject, "success", "");
        failCb = JSONUtils.getString(jsonObject, "fail", "");
        if ("getInfo".equals(action)) {
            jsCallback(successCb, conbineDeviceInfo());
        } else if ("beep".equals(action)) {
            beep(jsonObject);
        } else if (action.equals("startVibrate")) {
            openVibrate(jsonObject);
        } else if (action.equals("closeVibrate")) {
            closeVibrator();
        } else if (action.equals("openFlashlamp")) {
            openFlashLamp();
        } else if (action.equals("closeFlashlamp")) {
            closeFlashLamp();
        } else if (action.equals("openWebRevolve")) {
            openWebRevolve();
        } else if (action.equals("closeWebRevolve")) {
            closeWebRevolve();
        } else if (action.equals("clearWebCache")) {
            clearWebCache();
        }else {
            showCallIMPMethodErrorDlg();
        }

    }

    /**
     * 得到设备的platform
     *
     * @return 平台信息
     */
    public String getPlatform() {
        String platform;
        if (isAmazonDevice()) {
            platform = AMAZON_PLATFORM;
        } else {
            platform = ANDROID_PLATFORM;
        }
        return platform;
    }


    public String getModel() {
        String model = android.os.Build.MODEL;
        return model;
    }

    public String getProductName() {
        String productname = android.os.Build.PRODUCT;
        return productname;
    }

    /**
     * 得到设备的系统版本
     *
     * @return
     */
    public String getOSVersion() {
        String osversion = android.os.Build.VERSION.RELEASE;
        return osversion;
    }

    public String getSDKVersion() {
        @SuppressWarnings("deprecation")
        String sdkversion = android.os.Build.VERSION.SDK;
        return sdkversion;
    }

    /**
     * 检查设备是否由Amzaon生产
     *
     * @return
     */
    public boolean isAmazonDevice() {
        return android.os.Build.MANUFACTURER.equals(AMAZON_DEVICE);
    }

    private String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * Description notification中的beep实现与设定
     *
     * @param paramsObject JSON串
     */
    private void beep(JSONObject paramsObject) {
        try {
            if (!paramsObject.isNull("count"))
                count = paramsObject.getInt("count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 获得铃声的uri
        Uri ringtone = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 调用系统功能中的蜂鸣
        Ringtone notification = RingtoneManager.getRingtone(getActivity()
                .getBaseContext(), ringtone);
        // 如果不能调到系统蜂鸣则使用默认蜂鸣
        if (notification == null)
            notification = RingtoneManager.getRingtone(getFragmentContext(),
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        if (notification != null)
            for (long i = 0L; i < count; i++) {
                notification.play();
                long timeout = 5000L;
                // 将蜂鸣提示线程分离，实现暂停（这是一种实现均衡间隔的设计）
                while ((notification.isPlaying()) && (timeout > 0L)) {
                    timeout -= 100L;
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
    }

    /**
     * Description vibration中的vibrate实现与设定
     *
     * @param
     */
    @SuppressLint("MissingPermission")
    private void openVibrate(JSONObject jsonObject) {
        long time = 0l;
        boolean repeat = true;
        JSONObject optionObj = jsonObject.optJSONObject("options");
        try {
            if (!optionObj.isNull("time")) {
                time = optionObj.getLong("time");
            }
            if (!optionObj.isNull("repeat")) {
                repeat = optionObj.getBoolean("repeat");
            }
        } catch (JSONException e) {
            jsCallback(failCb, e.getMessage());
        }
        if (time == 0L)
            time = 500L;
        Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        long[] patter = {time, 1000L, time, 1000L};
        vibrator.vibrate(patter, repeat ? 0 : -1);
        jsCallback(successCb);
    }

    /**
     * Description vibration中的vibrate实现与设定
     *
     * @param
     */
    @SuppressLint("MissingPermission")
    private void closeVibrator() {
        Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
        jsCallback(successCb);
    }

    /**
     * 初始化摄像头信息。
     */
    private void initCameraInfo() {
        //callback成员变量初始化

        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 后置摄像头信息
                mBackCameraId = cameraId;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // 前置摄像头信息
                mFrontCameraId = cameraId;
            }
        }
    }

    private void openFlashLamp() {
        String[] cameraPermissions = new String[]{Permissions.FLASHLIGHT, Permissions.CAMERA};
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), cameraPermissions, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        if (manager == null) {
                            manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                        }
                        manager.setTorchMode("0", true);
                        jsCallback(successCb, "");
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        jsCallback(failCb, e.getMessage());
                    }
                } else {
                    try {
                        initCameraInfo();
                        if (m_Camera == null) {
                            m_Camera = Camera.open(mBackCameraId);
                        }
                        Camera.Parameters mParameters = m_Camera.getParameters();
                        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        m_Camera.setParameters(mParameters);
                        m_Camera.startPreview();
                        jsCallback(successCb, "");
                    } catch (Exception ex) {
                        if (m_Camera != null) {
                            m_Camera.setPreviewCallback(null);
                            m_Camera.stopPreview();
                            m_Camera.release();
                        }
                        m_Camera = null;
                        jsCallback(failCb, ex.getMessage());
                    }
                }

            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(getActivity(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getActivity(), permissions));
            }
        });
    }

    private void closeFlashLamp() {

        String[] cameraPermissions = new String[]{Permissions.FLASHLIGHT, Permissions.CAMERA};
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), cameraPermissions, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        if (manager == null) {
                            manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
                        }
                        manager.setTorchMode("0", false);
                        jsCallback(successCb, "");
                    } catch (CameraAccessException e) {
                        jsCallback(failCb, e.getMessage());
                    }
                } else {
                    try {
                        if (m_Camera == null) {
                            m_Camera = Camera.open(mBackCameraId);
                        }
                        Camera.Parameters mParameters = m_Camera.getParameters();
                        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        m_Camera.setParameters(mParameters);
                        m_Camera.setPreviewCallback(null);
                        m_Camera.stopPreview();
                        m_Camera.release();
                        m_Camera = null;
                        jsCallback(successCb, "");
                    } catch (Exception ex) {
                        if (m_Camera != null) {
                            m_Camera.setPreviewCallback(null);
                            m_Camera.stopPreview();
                            m_Camera.release();
                        }
                        m_Camera = null;
                        jsCallback(failCb, ex.getMessage());
                    }
                }
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(getActivity(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getActivity(), permissions));
            }
        });

    }

    private void openWebRevolve() {
        Router router = Router.getInstance();
        if (router.getService(SettingService.class) != null) {
            SettingService service = router.getService(SettingService.class);
            if (service.openWebRotate()) {
                JSONObject json = new JSONObject();
                try {
                    json.put("status", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!StringUtils.isEmpty(successCb)){
                    jsCallback(successCb, json);
                }
            } else {
                if (!StringUtils.isEmpty(failCb)){
                    jsCallback(failCb, "openWebRotate fail!!");
                }
            }
        }
    }

    private void closeWebRevolve() {
        Router router = Router.getInstance();
        if (router.getService(SettingService.class) != null) {
            SettingService service = router.getService(SettingService.class);
            if (service.closeWebRotate()) {
                JSONObject json = new JSONObject();
                try {
                    json.put("status", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!StringUtils.isEmpty(successCb)){
                    jsCallback(successCb, json);
                }
            } else {
                if (!StringUtils.isEmpty(failCb)){
                    jsCallback(failCb, "closeWebRevolve fail!!");
                }
            }
        }
    }

    private void clearWebCache() {
        Router router = Router.getInstance();
        if (router.getService(SettingService.class) != null) {
            SettingService service = router.getService(SettingService.class);
            if (service.clearWebCache()) {
                JSONObject json = new JSONObject();
                try {
                    json.put("status", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!StringUtils.isEmpty(successCb)){
                    jsCallback(successCb, json);
                }
            } else {
                if (!StringUtils.isEmpty(failCb)){
                    jsCallback(failCb, "clearWebCache fail!!");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (m_Camera != null) {
            m_Camera.setPreviewCallback(null);
            m_Camera.stopPreview();
            m_Camera.release();
            m_Camera = null;
        }
    }

}
