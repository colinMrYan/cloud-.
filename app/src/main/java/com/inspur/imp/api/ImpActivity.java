package com.inspur.imp.api;

import android.app.Activity;
import android.content.Context;
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
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.imp.engine.webview.ImpWebChromeClient;
import com.inspur.imp.engine.webview.ImpWebView;
import com.inspur.imp.plugin.camera.PublicWay;
import com.inspur.imp.plugin.file.FileService;
import com.inspur.mdm.MDM;

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
	private TextView headerText;
	private LinearLayout loadFailLayout;
	private boolean isMDM = false;//mdm页面

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(Res.getLayoutID("activity_imp"));
		progressLayout = (RelativeLayout) findViewById(Res
				.getWidgetID("progress_layout"));
		loadFailLayout = (LinearLayout)findViewById(Res.getWidgetID("load_error_layout")) ;
		webView = (ImpWebView) findViewById(Res.getWidgetID("webview"));
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
		LogUtils.jasonDebug("url="+url);
		if (getIntent().hasExtra("appName")) {
			headerText = (TextView) findViewById(Res.getWidgetID("header_text"));
			webView.setProperty(progressLayout,headerText,loadFailLayout);
			initWebViewGoBackOrClose();
			( findViewById(Res.getWidgetID("header_layout")))
					.setVisibility(View.VISIBLE);
			headerText.setText(getIntent().getExtras().getString("appName"));
		}else {
			webView.setProperty(progressLayout,null,loadFailLayout);
		}

		String token = ((MyApplication)getApplicationContext())
				.getToken();
		isMDM = getIntent().hasExtra("function")&&getIntent().getStringExtra("function").equals("mdm");
		if (isMDM){
			token = PreferencesUtils.getString(this,"mdm_accessToken");
			LogUtils.jasonDebug("token="+token);
		}
		setOauthHeader(token);
		setLangHeader(UriUtils.getLanguageCookie(this));
		setUserAgent("/emmcloud/" + AppUtils.getVersion(this));
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

		(findViewById(Res.getWidgetID("refresh_text"))).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadFailLayout.setVisibility(View.GONE);
				webView.reload();
			}
		});
		webView.loadUrl(url, extraHeaders);
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
			if (getIntent().hasExtra("function")&&getIntent().getStringExtra("function").equals("mdm")){
				new MDM().getMDMListener().MDMStatusNoPass();
			}
		}
	}

//	/**
//	 * 设置cookie
//	 */
//	private void setCookies(String url, String cookie) {
//		// TODO Auto-generated method stub
//		CookieManager cookieManager = CookieManager.getInstance();
//		cookieManager.setAcceptCookie(true);
//		cookieManager.acceptCookie();
//		cookieManager.setCookie(url, cookie);
//	}

	private void setUserAgent(String userAgentExtra) {
		// TODO Auto-generated method stub
		WebSettings settings = webView.getSettings();
		String userAgent = settings.getUserAgentString();
		userAgent = userAgent + userAgentExtra;
		settings.setUserAgentString(userAgent);
		settings.enableSmoothTransition();
		settings.setJavaScriptEnabled(true);
		//禁用缩放
		settings.setBuiltInZoomControls(false);
		settings.setSupportZoom(false);
		settings.setDisplayZoomControls(false);
		settings.setGeolocationEnabled(true);
		settings.setDatabaseEnabled(true);
		String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		settings.setGeolocationDatabasePath(dir);
		settings.setDomStorageEnabled(true);



	}

	private void setOauthHeader(String OauthHeader) {
		extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Authorization", OauthHeader);
	}

	private void setLangHeader(String langHeader){
		extraHeaders.put("lang", langHeader);
	}

	public void onClick(View v) {
		goBack();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				LogUtils.jasonDebug("canGoBack");
				webView.goBack();// 返回上一页面
				return true;
			} else {
				LogUtils.jasonDebug("not------canGoBack");
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
