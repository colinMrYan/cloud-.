package com.inspur.imp.engine.webview;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.inspur.emmcloud.util.PreferencesUtils;


/**
 * 如果页面中链接，如果希望点击链接继续在当前browser中响应， 而不是新开Android的系统browser中响应该链接， 必须覆盖
 * webview的WebViewClient对象。
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class ImpWebViewClient extends WebViewClient {
	private String urlparam = "";
	private final String F_UEX_SCRIPT_SELF_FINISH = "javascript:if(window.init){window.init();}";
	private String mCurrentUrl;
	private ImpWebView impView;
	private ImpWebView myWebView;
	private String errolUrl = "file:///android_asset/error/error.html";

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				// 页面加载超时加载错误页面
				if (myWebView.getProgress() < 100) {
					myWebView.stopLoading();
					onReceivedError(myWebView, -6,
							"The connection to the server was unsuccessful.",
							errolUrl);
				}
			}
		}

	};

	/*
	 * 开始加载网页的操作
	 */
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		urlparam = url;
		myWebView = (ImpWebView) view;
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(2000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// Message m = new Message();
		// m.what = 1;
		// mHandler.sendMessage(m);
		// }
		// }).start();

	}

	/*
	 * 网页加载成功
	 */
	@Override
	public void onPageFinished(WebView view, String url) {
		ImpWebView webview = (ImpWebView) view;
		if (webview.destroyed || url.contains("error"))
			return;
		webview.loadUrl(F_UEX_SCRIPT_SELF_FINISH);
		String c = CookieManager.getInstance().getCookie(url);
		PreferencesUtils.putString(view.getContext(),"web_cookie",c);
		CookieSyncManager.getInstance().sync();
		webview.initPlugin();
	}

	/*
	 * 网页加载失败，取消加载，并清理当前的view
	 */
	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
		ImpWebView webview = (ImpWebView) view;
		// 清理缓存
		webview.clearCache(true);
		webview.clearHistory();
		CookieManager.getInstance().removeSessionCookie();

		// if (impView != null && webview != null && webview == impView
		// && window != null) {
		// window.close();
		// return;
		// }
		// impView = webview;
		// webview.loadUrl(errolUrl);
	}

	/*
	 * 对网页中超链接按钮的响应
	 */
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		ImpWebView impView = (ImpWebView) view;
		if (url.contains("http")) {
			int index = url.indexOf("http");
			url = url.substring(index);
		}
		if (mCurrentUrl != null && url != null && url.equals(mCurrentUrl)) {
			impView.goBack();
			return true;
		}
		view.loadUrl(url);
		mCurrentUrl = url;
		return true;

	}
}
