package com.inspur.imp.plugin;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.imp.api.iLog;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.util.DialogUtil;
import com.inspur.imp.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * 后台接口管理类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class PluginMgr {

    private Context context;

    private ImpWebView webView;

    private static final String TAG = "PLUGIN_MGR";
    private IPlugin plugin;

    // 缓存功能类实例
    private HashMap<String, IPlugin> entries = new HashMap<String, IPlugin>();

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
//	public static void init(Context ctx, ImpWebView ImpWebView) {
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
    public void execute(String serviceName, final String action,
                        final String params) {
        serviceName = getReallyServiceName(serviceName);
        Log.d("jason", "serviceName=" + serviceName);
        Log.d("jason", "action=" + action);
        IPlugin plugin = null;
        if (!entries.containsKey(serviceName)) {
            plugin = createPlugin(serviceName);
            entries.put(serviceName, plugin);
        } else {
            plugin = getPlugin(serviceName);
        }
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
            plugin.execute(action, jo);
        }else{
            DialogUtil.getInstance((Activity)context).show();
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
        String reallyServiceName = getReallyServiceName(serviceName);
        String res = "";
        if (reallyServiceName != null){
            IPlugin plugin = null;
            Log.d("jason", "serviceName=" + reallyServiceName);
            Log.d("jason", "action=" + action);
            if (!entries.containsKey(reallyServiceName)) {
                plugin = createPlugin(reallyServiceName);
                entries.put(reallyServiceName, plugin);
            } else {
                plugin = getPlugin(reallyServiceName);
            }
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
            }
        }else{
            DialogUtil.getInstance((Activity)context).show();
        }
        return res;

    }

    private String getReallyServiceName(String serviceName){
        if (serviceName != null){
            if (serviceName.endsWith("LoadingDialogService")) {
                serviceName = "com.inspur.imp.plugin.loadingdialog.LoadingDialogService";
            }else if (serviceName.endsWith("FileTransferService")) {
                serviceName = "com.inspur.imp.plugin.filetransfer.FileTransferService";
            }else if (serviceName.endsWith("OCRService")){
                serviceName = "com.inspur.imp.plugin.ocr.OCRService";
            }
        }
        return serviceName;
    }

    /**
     * 获取插件
     *
     * @param service
     * @return IPlugin
     */
    public IPlugin getPlugin(String service) {
        IPlugin plugin = entries.get(service);

        // 页面切换时切换webView
        if ("com.inspur.imp.plugin.window.WindowService".equals(service)
                || "com.inspur.imp.plugin.scroll.ScrollService".equals(service)
                || "com.inspur.imp.plugin.app.AppService".equals(service)
                || "com.inspur.imp.plugin.transfer.FileTransferService".equals(service)
                ) {
            plugin = createPlugin(service);
        } else {
            plugin.init(context, webView);
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
            plugin.init(context, webView);
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
