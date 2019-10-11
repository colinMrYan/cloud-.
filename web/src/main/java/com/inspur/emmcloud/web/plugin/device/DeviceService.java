package com.inspur.emmcloud.web.plugin.device;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

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
        } else
            // 使用notification中的beep组件
            if ("beep".equals(action)) {
                beep(jsonObject);
            }
            // 震动
            else if (action.equals("vibrate")) {
                vibrate(jsonObject);
            } else {
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
    private void vibrate(JSONObject jsonObject) {
        long time = 0l;
        try {
            if (!jsonObject.isNull("time")) {

                time = jsonObject.getLong("time");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (time == 0L)
            time = 500L;
        // 调用系统服务的vibrator组件
        Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(
                Context.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
    }

    @Override
    public void onDestroy() {
    }

}
