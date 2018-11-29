package com.inspur.imp.engine.webview;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.AppRedirectResult;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.imp.api.ImpCallBackInterface;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * 如果页面中链接，如果希望点击链接继续在当前browser中响应， 而不是新开Android的系统browser中响应该链接， 必须覆盖
 * webview的WebViewClient对象。
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class ImpWebViewClient extends WebViewClient {
    private String urlparam = "";
    private final String F_UEX_SCRIPT_SELF_FINISH = "javascript:if(window.startWebSocket){window.startWebSocket();}";
    private ImpWebView myWebView;
    private String errolUrl = "file:///android_asset/error/error.html";
    private LinearLayout loadFailLayout;
    private Handler mHandler = null;
    private Runnable runnable = null;
    private ImpCallBackInterface impCallBackInterface;

    public ImpWebViewClient(LinearLayout loadFailLayout,ImpCallBackInterface impCallBackInterface) {
        this.loadFailLayout = loadFailLayout;
        this.impCallBackInterface = impCallBackInterface;
        handMessage();
        initRunnable();
    }

    private void handMessage() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        myWebView.reload();
                        break;
                    default:
                        break;

                }
            }
        };
    }

    private void initRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);
            }
        };
    }

//	private Handler mHandler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			switch (msg.what) {
//				case 1:
//					// 页面加载超时加载错误页面
//					if (myWebView.getProgress() < 100) {
//						myWebView.stopLoading();
//						onReceivedError(myWebView, -6,
//								"The connection to the server was unsuccessful.",
//								errolUrl);
//					}
//			}
//		}
//
//	};

    /*
     * 开始加载网页的操作
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        urlparam = url;
        myWebView = (ImpWebView) view;
        if (runnable != null && url.startsWith("http://baoxiao.inspur.com")) {
            mHandler.postDelayed(runnable, 2000);
        }
    }

    /*
     * 网页加载成功
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if(impCallBackInterface != null){
            impCallBackInterface.onInitWebViewGoBackOrClose();
        }
        ImpWebView webview = (ImpWebView) view;
        if (webview.destroyed) {
            return;
        }
        webview.setVisibility(View.VISIBLE);
        //为了获取网页的html内容
        String  script = "javascript:window.getContent.onGetHtmlContent("
                + "document.getElementsByTagName('html')[0].innerHTML" + ");";
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
            view.evaluateJavascript(F_UEX_SCRIPT_SELF_FINISH, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        }else {
            view.loadUrl(script);
            webview.loadUrl(F_UEX_SCRIPT_SELF_FINISH);
        }
        String c = CookieManager.getInstance().getCookie(url);
        PreferencesUtils.putString(view.getContext(), "web_cookie", c);
        CookieSyncManager.getInstance().sync();
    }

    /*
     * 网页加载失败，取消加载，并清理当前的view
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if(impCallBackInterface != null){
            impCallBackInterface.onLoadingDlgDimiss();
        }
        loadFailLayout.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (request.isForMainFrame() && request.getUrl().toString().equals(view.getUrl())) {// 在这里加上个判断
            if (runnable != null) {
                mHandler.removeCallbacks(runnable);
                runnable = null;
            }
            if(impCallBackInterface != null){
                impCallBackInterface.onLoadingDlgDimiss();
            }
            loadFailLayout.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl().toString());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (url.contains(APIUri.getWebLoginUrl()) || url.contains("https://id.inspur.com/")) {
            handleReDirectURL(url, view);
            return true;
        }
        if (!url.startsWith("http") && !url.startsWith("ftp")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                intent.setComponent(null);
                myWebView.getContext().startActivity(intent);
                MyApplication.getInstance().setEnterSystemUI(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        view.loadUrl(url, getWebViewHeaders(url));
        return super.shouldOverrideUrlLoading(view, url);
    }

    /**
     * 获取Header
     *
     * @return
     */
    private Map<String, String> getWebViewHeaders(String url) {
        return myWebView == null ? new HashMap<String, String>() : ((impCallBackInterface != null)? impCallBackInterface.onGetWebViewHeaders(url):new HashMap<String, String>());
    }

    /**
     * 处理重定向的URL
     *
     * @param url
     */
    private void handleReDirectURL(String url, WebView view) {
        URL urlWithParams = null;
        try {
            urlWithParams = new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String requestUrl = urlWithParams.getProtocol() + "://" + urlWithParams.getHost();
        MyAppAPIService appAPIService = new MyAppAPIService(view.getContext());
        appAPIService.setAPIInterface(new WebService(view));
        if (NetUtils.isNetworkConnected(view.getContext())) {
            appAPIService.getAuthCode(requestUrl, urlWithParams.getQuery());
        }

    }

    class WebService extends APIInterfaceInstance {
        private WebView webView;

        public WebService(WebView webView) {
            this.webView = webView;
        }

        @Override
        public void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult) {
            if (NetUtils.isNetworkConnected(webView.getContext())) {
                String redirectUri = appRedirectResult.getRedirect_uri();
                webView.loadUrl(redirectUri, getWebViewHeaders(redirectUri));
            }
            super.returnGetAppAuthCodeResultSuccess(appRedirectResult);
        }

        @Override
        public void returnGetAppAuthCodeResultFail(String error, int errorCode) {
            super.returnGetAppAuthCodeResultFail(error, errorCode);
        }
    }
}
