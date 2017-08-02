package com.inspur.imp.plugin;

import android.content.Context;
import android.util.Log;

import com.inspur.imp.api.iLog;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;




/**
 * 后台接口管理类
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class PluginMgr {

	private static Context context;

	private static ImpWebView webView;

	private static String TAG = "PLUGIN_MGR";
	private static IPlugin plugin;

	// 缓存功能类实例
	private static final HashMap<String, IPlugin> entries = new HashMap<String, IPlugin>();

	/**
	 * 初始化PluginMgr，包括初始化PluginEntry和解析plugin.xml来获取service名称
	 * 
	 * @param ctx
	 * @param
	 */
	public static void init(Context ctx, ImpWebView ImpWebView) {
		context = ctx;
		webView = ImpWebView;
	}

	/**
	 * 执行对插件的操作，无返回值
	 * 
	 * @param serviceName
	 *            服务名
	 * @param action
	 *            操作名
	 * @param params
	 *            操作参数
	 */
	public static void execute(String serviceName, final String action,
			final String params) {
		if (serviceName.equals("LoadingDialogService")){
			serviceName = "com.inspur.imp.plugin.loadingdialog."+serviceName;
		}
		Log.d("jason", "serviceName="+serviceName);
		Log.d("jason", "action="+action);
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
		}
	}

	/**
	 * 执行对插件的操作，有返回值
	 * 
	 * @param serviceName
	 *            服务名
	 * @param action
	 *            操作名
	 * @param params
	 *            操作参数
	 */
	public static String executeAndReturn(final String serviceName,
			final String action, final String params) {
		IPlugin plugin = null;
		String res = "";
		Log.d("jason", "serviceName="+serviceName);
		Log.d("jason", "action="+action);
		if (!entries.containsKey(serviceName)) {
			plugin = createPlugin(serviceName);
			entries.put(serviceName, plugin);
			Log.d("jason", "0---");
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
			try {
				Log.d("jason", "1---");
				res = plugin.executeAndReturn(action, jo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.d("jason", "2---");
		return res;
	}

	/**
	 * 获取插件
	 * 
	 * @param service
	 * @return IPlugin
	 */
	public static IPlugin getPlugin(String service) {
		IPlugin plugin = entries.get(service);

		// 页面切换时切换webView
		if ("com.inspur.imp.plugin.window.WindowService".equals(service)
				|| "com.inspur.imp.plugin.scroll.ScrollService".equals(service)
				||"com.inspur.imp.plugin.app.AppService".equals(service)
				||"com.inspur.imp.plugin.transfer.FileTransferService".equals(service)) {
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
	private static IPlugin createPlugin(String clssName) {
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
	private static Class getClassByName(final String clazz)
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
	public static void onDestroy() {
		for (IPlugin plugin : entries.values()) {
			if (plugin != null) {
				plugin.onDestroy();
			}
		}
		entries.clear();
	}

	/**
	 * activity onResume事件
	 */
	public static  void onResume(){
		for (IPlugin plugin : entries.values()) {
			if (plugin != null) {
				plugin.onActivityResume();
			}
		}
	}

	/**
	 * activity onPause事件
	 */
	public static  void onPause(){
		for (IPlugin plugin : entries.values()) {
			if (plugin != null) {
				plugin.onActivityPause();
			}
		}
	}
}
