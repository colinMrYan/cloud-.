package com.inspur.imp.api;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.imp.engine.webview.ImpWebChromeClient;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.engine.webview.ImpWebViewClient;
import com.inspur.imp.plugin.camera.PublicWay;
import com.inspur.imp.plugin.file.FileService;

import java.util.HashMap;
import java.util.Map;


public class ImpActivity extends ImpBaseActivity {

	public static final String USERAGENT = "Mozilla/5.0 (Linux; U; isInImp; Android "
			+ Build.VERSION.RELEASE
			+ "; en-us; "
			+ Build.MODEL
			+ " Build/FRF91) AppleWebKit/533.1 "
			+ "(KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	public static final int FILE_CHOOSER_RESULT_CODE = 5173;
	private ImpWebView webView;
	// 浏览文件resultCode
	private int FILEEXPLOER_RESULTCODE = 4;
	private RelativeLayout progressLayout;
	private Map<String, String> extraHeaders;
	private Button buttonBack;
	private Button buttonClose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(Res.getLayoutID("activity_imp"));
		progressLayout = (RelativeLayout) findViewById(Res
				.getWidgetID("progress_layout"));
		webView = (ImpWebView) findViewById(Res.getWidgetID("webview"));
		webView.setProperty(progressLayout);
		webView.setWebViewClient(new ImpWebViewClient());
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		String url = "";
		Uri uri = getIntent().getData();
		if (uri != null){
			String host = uri.getHost();
			url = "https://emm.inspur.com/ssohandler/gs_msg/"+host;
		}else{
			url = getIntent().getExtras().getString("uri");
		}
		if (getIntent().hasExtra("appName")) {
			initWebViewGoBackOrClose();
			( findViewById(Res.getWidgetID("header_layout")))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(Res.getWidgetID("header_text")))
					.setText(getIntent().getExtras().getString("appName"));
		}
		String token = ((MyApplication)getApplicationContext())
				.getToken();
		setOauthHeader(url, token);
		setUserAgent("/emmcloud/" + AppUtils.getVersion(this));
		setCookies(url, UriUtils.getLanguageCookie(this));
//		if (getIntent().hasExtra("userAgentExtra")) {
//			String userAgentExtra = getIntent().getExtras().getString(
//					"userAgentExtra");
//			setUserAgent(userAgentExtra);
//		}
//
//		if (getIntent().hasExtra("Authorization")) {
//			String OauthHeader = getIntent().getExtras().getString(
//					"Authorization");
//			setOauthHeader(url, OauthHeader);
//		}
//
//		if (getIntent().hasExtra("cookie")) {
//			String cookie = getIntent().getExtras().getString("cookie");
//			setCookies(url, cookie);
//		}

		webView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});

		webView.loadUrl(url, extraHeaders);
//		webView.computeScroll();
		progressLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * 初始化原生WebView的返回和关闭
	 * （不是GS应用，GS应用有重定向，不容易实现返回）
	 */
	private void initWebViewGoBackOrClose() {
		buttonBack = (Button) findViewById(Res.getWidgetID("imp_back_btn"));
		buttonClose = (Button) findViewById(Res.getWidgetID("imp_close_btn"));
		buttonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();
			}
		});
		buttonClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		webView.getWebChromeClient().setOnFinishLoadUrlListener(
				new ImpWebChromeClient.OnFinishLoadUrlListener() {
					@Override
					public void OnFinishLoadUrlListener(boolean isFinish) {
						((RelativeLayout) findViewById(Res
								.getWidgetID("header_layout")))
								.setVisibility(View.VISIBLE);
						if (webView.canGoBack()) {
							buttonClose.setVisibility(View.VISIBLE);
						} else {
							buttonClose.setVisibility(View.GONE);
						}
					}
				});
	}

	/**
	 * 返回
	 */
	private void goBack() {
		if (webView.canGoBack()) {
			webView.goBack();// 返回上一页面
		} else {
			finish();// 退出程序
		}
	}

	/**
	 * 设置cookie
	 */
	private void setCookies(String url, String cookie) {
		// TODO Auto-generated method stub
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.acceptCookie();
		cookieManager.setCookie(url, cookie);
	}

	private void setUserAgent(String userAgentExtra) {
		// TODO Auto-generated method stub
		WebSettings settings = webView.getSettings();
		String userAgent = settings.getUserAgentString();
		userAgent = userAgent + userAgentExtra;
		settings.setUserAgentString(userAgent);
		settings.enableSmoothTransition();
		settings.setJavaScriptEnabled(true);
	}

	private void setOauthHeader(String url, String OauthHeader) {
		extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Authorization", OauthHeader);
	}

	public void onClick(View v) {
		goBack();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				webView.goBack();// 返回上一页面
				return true;
			} else {
				finish();// 退出程序
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (webView != null) {
			webView.removeAllViews();
			webView.destroy();
		}
	}


	/**
	 * 自定义WebViewClient在应用中打开页面
	 */
	private class webViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (PublicWay.photoService != null) {
			PublicWay.photoService.onActivityResult(requestCode, -2, data);
			return;
		}
		if (PublicWay.uploadPhotoService != null) {
			PublicWay.uploadPhotoService.onActivityResult(requestCode, resultCode, data);
			return;
		}
		// 获取选择的文件
		else if (resultCode == FILEEXPLOER_RESULTCODE) {
			FileService.fileService.getAudioFilePath(data
					.getStringExtra("filePath"));
		} else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
			Uri uri = data == null || resultCode != Activity.RESULT_OK ? null
					: data.getData();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

				ValueCallback<Uri[]> mUploadCallbackAboveL = webView
						.getWebChromeClient().getValueCallbackAboveL();
				if (null == mUploadCallbackAboveL)
					return;
				if (uri == null) {
					mUploadCallbackAboveL.onReceiveValue(null);
				} else {
					Uri[] uris = new Uri[] { uri };
					mUploadCallbackAboveL.onReceiveValue(uris);
				}
				mUploadCallbackAboveL = null;
			} else {
				ValueCallback<Uri> mUploadMessage = webView
						.getWebChromeClient().getValueCallback();
				if (null == mUploadMessage)
					return;
				mUploadMessage.onReceiveValue(uri);
				mUploadMessage = null;
			}
		}
	}




}
