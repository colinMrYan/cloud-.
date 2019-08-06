package com.inspur.emmcloud.web.plugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.web.plugin.amaplocation.AmapLocateService;
import com.inspur.emmcloud.web.plugin.app.AppService;
import com.inspur.emmcloud.web.plugin.barcode.scan.BarCodeService;
import com.inspur.emmcloud.web.plugin.broadcast.BroadcastService;
import com.inspur.emmcloud.web.plugin.camera.CameraService;
import com.inspur.emmcloud.web.plugin.datetime.DatePickerService;
import com.inspur.emmcloud.web.plugin.datetime.TimePickerService;
import com.inspur.emmcloud.web.plugin.device.DeviceService;
import com.inspur.emmcloud.web.plugin.dialog.DialogService;
import com.inspur.emmcloud.web.plugin.emm.EMMService;
import com.inspur.emmcloud.web.plugin.filetransfer.FileTransferService;
import com.inspur.emmcloud.web.plugin.gps.GpsService;
import com.inspur.emmcloud.web.plugin.loadingdialog.LoadingDialogService;
import com.inspur.emmcloud.web.plugin.map.MapService;
import com.inspur.emmcloud.web.plugin.network.NetworkService;
import com.inspur.emmcloud.web.plugin.photo.PhotoService;
import com.inspur.emmcloud.web.plugin.sms.SmsService;
import com.inspur.emmcloud.web.plugin.staff.SelectStaffService;
import com.inspur.emmcloud.web.plugin.staff.StuffInformationService;
import com.inspur.emmcloud.web.plugin.startapp.StartAppService;
import com.inspur.emmcloud.web.plugin.telephone.TelephoneService;
import com.inspur.emmcloud.web.plugin.video.VideoRecordService;
import com.inspur.emmcloud.web.plugin.window.WindowService;
import com.inspur.emmcloud.web.ui.ImpCallBackInterface;
import com.inspur.emmcloud.web.ui.iLog;
import com.inspur.emmcloud.web.util.StrUtil;
import com.inspur.emmcloud.web.webview.ImpWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * 后台接口管理类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class PluginMgr {

    private static final String TAG = "PLUGIN_MGR";
    private Context context;
    private ImpWebView webView;
    private IPlugin plugin;

    // 缓存功能类实例
    private HashMap<String, IPlugin> entries = new HashMap<String, IPlugin>();

    private ImpCallBackInterface impCallBackInterface;

    public PluginMgr(Context ctx, ImpWebView ImpWebView) {
        context = ctx;
        webView = ImpWebView;
    }

//	/**
//	 * 初始化PluginMgr，包括初始化PluginEntry和解析plugin.xml来获取service名称
//	 *
//	 * @param ctx
//	 * @param
//	 */
//	public static void startWebSocket(Context ctx, ImpWebView ImpWebView) {
//		context = ctx;
//		webView = ImpWebView;
//	}

    /**
     * 执行对插件的操作，无返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    public void execute(final String serviceName, final String action,
                        final String params) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Handler mainThread = new Handler(Looper.getMainLooper());
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    executeOnMainThread(serviceName, action, params);
                }
            });
        } else {
            executeOnMainThread(serviceName, action, params);
        }

    }

    private void executeOnMainThread(String serviceName, final String action,
                                     final String params) {
        serviceName = getReallyServiceName(serviceName);
        if (serviceName != null) {
            LogUtils.jasonDebug("serviceName=" + serviceName);
            LogUtils.jasonDebug("action=" + action);
            IPlugin plugin = getPlugin(serviceName);
            // 将传递过来的参数转换为JSON
            JSONObject jo = null;
            if (StrUtil.strIsNotNull(params)) {
                try {
                    jo = new JSONObject(params);
                } catch (JSONException e) {
                    LogUtils.jasonDebug("e==" + e.toString());
                    iLog.e(TAG, "组装Json对象出现异常!");
                }
            }
            // 执行接口的execute方法
            if (plugin != null) {
                plugin.execute(action, jo);
            } else {
                if (impCallBackInterface != null) {
                    impCallBackInterface.onShowImpDialog();
                }
            }
        } else {
            if (impCallBackInterface != null) {
                impCallBackInterface.onShowImpDialog();
            }
        }
    }

    /**
     * 执行对插件的操作，有返回值
     *
     * @param serviceName 服务名
     * @param action      操作名
     * @param params      操作参数
     */
    public String executeAndReturn(final String serviceName,
                                   final String action, final String params) {
        String res = "";
        if (Looper.myLooper() != Looper.getMainLooper() && !serviceName.endsWith("EMMService") && !serviceName.endsWith("DeviceService")) {
            Handler mainThread = new Handler(Looper.getMainLooper());
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    executeAndReturnOnMainThead(serviceName, action, params);
                }
            });
        } else {
            res = executeAndReturnOnMainThead(serviceName, action, params);
        }
        return res;
    }


    private String executeAndReturnOnMainThead(final String serviceName,
                                               final String action, final String params) {
        String reallyServiceName = getReallyServiceName(serviceName);
        String res = "";
        if (reallyServiceName != null) {
            LogUtils.jasonDebug("serviceName=" + reallyServiceName);
            LogUtils.jasonDebug("action=" + action);
            IPlugin plugin = getPlugin(reallyServiceName);
            // 将传递过来的参数转换为JSON
            JSONObject jo = null;
            if (StrUtil.strIsNotNull(params)) {
                try {
                    jo = new JSONObject(params);
                } catch (JSONException e) {
                    iLog.e(TAG, "组装Json对象出现异常!");
                }
            }
            // 执行接口的execute方法
            if (plugin != null) {
                try {
                    res = plugin.executeAndReturn(action, jo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (impCallBackInterface != null) {
                    impCallBackInterface.onShowImpDialog();
                }
            }
        } else {
            if (impCallBackInterface != null) {
                impCallBackInterface.onShowImpDialog();
            }
        }
        return res;

    }

    private String getReallyServiceName(String serviceName) {
        if (serviceName != null) {
            if (serviceName.endsWith("LoadingDialogService")) {
                serviceName = LoadingDialogService.class.getCanonicalName();
            } else if (serviceName.endsWith("FileTransferService")) {
                serviceName = FileTransferService.class.getCanonicalName();
            } else if (serviceName.endsWith("OCRService")) {
                // serviceName = "com.inspur.imp.plugin.ocr.OCRService";
            } else if (serviceName.endsWith("StartAppService")) {
                serviceName = StartAppService.class.getCanonicalName();
            } else if (serviceName.endsWith("WindowService")) {
                serviceName = WindowService.class.getCanonicalName();
            } else if (serviceName.endsWith("DeviceService")) {
                serviceName = DeviceService.class.getCanonicalName();
            } else if (serviceName.endsWith("StuffInformationService")) {
                serviceName = StuffInformationService.class.getCanonicalName();
            } else if (serviceName.endsWith("AmapLocateService")) {
                serviceName = AmapLocateService.class.getCanonicalName();
            } else if (serviceName.endsWith("AppService")) {
                serviceName = AppService.class.getCanonicalName();
            } else if (serviceName.endsWith("BarCodeService")) {
                serviceName = BarCodeService.class.getCanonicalName();
            } else if (serviceName.endsWith("BroadcastService")) {
                serviceName = BroadcastService.class.getCanonicalName();
            } else if (serviceName.endsWith("CameraService")) {
                serviceName = CameraService.class.getCanonicalName();
            } else if (serviceName.endsWith("DatePickerService")) {
                serviceName = DatePickerService.class.getCanonicalName();
            } else if (serviceName.endsWith("TimePickerService")) {
                serviceName = TimePickerService.class.getCanonicalName();
            } else if (serviceName.endsWith("DialogService")) {
                serviceName = DialogService.class.getCanonicalName();
            } else if (serviceName.endsWith("EMMService")) {
                serviceName = EMMService.class.getCanonicalName();
            } else if (serviceName.endsWith("GpsService")) {
                serviceName = GpsService.class.getCanonicalName();
            } else if (serviceName.endsWith("MapService")) {
                serviceName = MapService.class.getCanonicalName();
            } else if (serviceName.endsWith("NetworkService")) {
                serviceName = NetworkService.class.getCanonicalName();
            } else if (serviceName.endsWith("PhotoService")) {
                serviceName = PhotoService.class.getCanonicalName();
            } else if (serviceName.endsWith("SmsService")) {
                serviceName = SmsService.class.getCanonicalName();
            } else if (serviceName.endsWith("SelectStaffService")) {
                serviceName = SelectStaffService.class.getCanonicalName();
            } else if (serviceName.endsWith("StuffInformationService")) {
                serviceName = StuffInformationService.class.getCanonicalName();
            } else if (serviceName.endsWith("TelephoneService")) {
                serviceName = TelephoneService.class.getCanonicalName();
            } else if (serviceName.endsWith("VideoRecordService")) {
                serviceName = VideoRecordService.class.getCanonicalName();
            }
            LogUtils.jasonDebug("serviceName==" + serviceName);
        }
        return serviceName;
    }

    public ImpCallBackInterface getImpCallBackInterface() {
        return impCallBackInterface;
    }

    public void setImpCallBackInterface(ImpCallBackInterface impCallBackInterface) {
        this.impCallBackInterface = impCallBackInterface;
    }

    /**
     * 获取插件
     *
     * @param service
     * @return IPlugin
     */
    public IPlugin getPlugin(String service) {
        service = service.trim();
        IPlugin plugin = null;
        Log.d("jason", "serviceName=" + service);
        if (!entries.containsKey(service) || service.equals(FileTransferService.class.getCanonicalName())) {
            plugin = createPlugin(service);
            if (plugin != null) {
                entries.put(service, plugin);
            }
        } else {
            plugin = entries.get(service);
        }
        return plugin;
    }

    /**
     * 创建IPlugin对象 如果对象已经被创建则返回接口
     *
     * @return The plugin object
     */
    private IPlugin createPlugin(String clssName) {
        try {
            @SuppressWarnings("rawtypes")
            Class c = getClassByName(clssName);
            plugin = (IPlugin) c.newInstance();
            plugin.init(context, webView, impCallBackInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plugin;
    }

    /**
     * 获取到功能类
     *
     * @param clazz
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("rawtypes")
    private Class getClassByName(String clazz)
            throws ClassNotFoundException {
        Class c = null;
        if (clazz != null) {
            c = Class.forName(clazz);
        }
        return c;
    }

    /**
     * activity关闭之前调用方法关闭相应的空间
     */
    public void onDestroy() {
        for (IPlugin plugin : entries.values()) {
            if (plugin != null) {
                plugin.onDestroy();
            }
        }
        entries.clear();
        entries = null;
    }

    /**
     * activity onResume事件
     */
    public void onResume() {
        for (IPlugin plugin : entries.values()) {
            if (plugin != null) {
                plugin.onActivityResume();
            }
        }
    }

    /**
     * activity onPause事件
     */
    public void onPause() {
        for (IPlugin plugin : entries.values()) {
            if (plugin != null) {
                plugin.onActivityPause();
            }
        }
    }
}
