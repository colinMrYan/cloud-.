package com.inspur.imp.engine.webview;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.AppRedirectResult;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ToastUtils;

import java.net.MalformedURLException;
import java.net.URL;


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
	private ImpWebView myWebView;
	private String errolUrl = "file:///android_asset/error/error.html";
	private LinearLayout loadFailLayout;
	public ImpWebViewClient(LinearLayout loadFailLayout){
		this.loadFailLayout = loadFailLayout;
	}

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
		ToastUtils.show(view.getContext(),"onPageStarted");
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
		ToastUtils.show(view.getContext(),"onPageFinished");
		ImpWebView webview = (ImpWebView) view;
		if (webview.destroyed || url.contains("error")){
			ToastUtils.show(view.getContext(),"webview.destroyed或url.contains(\"error\")");
			return;
		}
		//为了获取网页的title
		view.loadUrl("javascript:window.getTitle.onGetTitle("
				+ "document.getElementsByTagName('title')[0].innerHTML" + ");");
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
		ToastUtils.show(view.getContext(),"web打开失败："+description);
		loadFailLayout.setVisibility(View.VISIBLE);
//		final ImpWebView webview = (ImpWebView) view;
//		removeAllSessionCookie();
		//延迟一秒钟解决无法清除历史的问题
//		webview.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				// 清理缓存
//				webview.clearCache(true);
//				webview.clearHistory();
//			}
//		}, 600);
//		 webview.loadUrl(errolUrl);
	}

	/**
	 * 清除所有的SessionCookie
	 */
	public void removeAllSessionCookie(){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().removeSessionCookies(null);
			CookieManager.getInstance().flush();
		}else {
			CookieSyncManager cookieSyncMngr =
					CookieSyncManager.createInstance(myWebView.getContext());
			CookieManager.getInstance().removeSessionCookie();
		}
	}


	/*
			 * 对网页中超链接按钮的响应
			 */
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		handleReDirectURL(url,view);
		return super.shouldOverrideUrlLoading(view, url);

	}

	/**
	 * 处理重定向的URL
	 * @param url
	 */
	private void handleReDirectURL(String url, WebView view) {
		if(url.contains("https://id.inspur.com/oauth2.0/authorize")){
			ToastUtils.show(view.getContext(),"进行URL重定向");
			URL urlWithParams = null;
			try {
				urlWithParams = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			MyAppAPIService appAPIService = new MyAppAPIService(view.getContext());
			appAPIService.setAPIInterface(new WebService(view));
			if(NetUtils.isNetworkConnected(view.getContext())){
				appAPIService.getAuthCode(urlWithParams.getQuery());
			}
		}

	}

	class WebService extends APIInterfaceInstance {
		private WebView webView;
		public WebService(WebView webView){
			this.webView = webView;
		}

		@Override
		public void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult) {
			ToastUtils.show(webView.getContext(),"URL重定向成功");
			if(NetUtils.isNetworkConnected(webView.getContext())){
				webView.loadUrl(appRedirectResult.getRedirect_uri());
			}
			super.returnGetAppAuthCodeResultSuccess(appRedirectResult);
		}

		@Override
		public void returnGetAppAuthCodeResultFail(String error) {
			ToastUtils.show(webView.getContext(),"URL重定向失败");
			super.returnGetAppAuthCodeResultFail(error);
		}
	}
}
