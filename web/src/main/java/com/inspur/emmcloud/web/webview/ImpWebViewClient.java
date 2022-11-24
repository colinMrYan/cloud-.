package com.inspur.emmcloud.web.webview;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.web.api.WebAPIInterfaceImpl;
import com.inspur.emmcloud.web.api.WebAPIService;
import com.inspur.emmcloud.web.api.WebAPIUri;
import com.inspur.emmcloud.web.bean.AppRedirectResult;
import com.inspur.emmcloud.web.ui.ImpCallBackInterface;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 如果页面中链接，如果希望点击链接继续在当前browser中响应， 而不是新开Android的系统browser中响应该链接， 必须覆盖
 * webview的WebViewClient对象。
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class ImpWebViewClient extends WebViewClient {
    // 设置微信 H5 支付调用 loadDataWithBaseURL 的标记位，避免循环调用，
    // 再次进入微信 H5 支付流程时记得重置此标记位状态
    boolean firstVisitWXH5PayUrl = true;
    private ImpWebView myWebView;
    private Handler mHandler = null;
    private Runnable runnable = null;
    private ImpCallBackInterface impCallBackInterface;
    private String url = "";
    private boolean isLogin = false;
    private String referer = "https://sy.517na.com";

    public ImpWebViewClient(ImpCallBackInterface impCallBackInterface) {
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

    /*
     * 开始加载网页的操作
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        this.url = url;
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
        if (impCallBackInterface != null) {
            impCallBackInterface.onInitWebViewGoBackOrClose();
        }
        ImpWebView webview = (ImpWebView) view;
        if (webview.destroyed) {
            return;
        }
        webview.setVisibility(View.VISIBLE);
        //为了获取网页的html内容
        String script = "javascript:window.getContent.onGetHtmlContent("
                + "document.getElementsByTagName('html')[0].innerHTML" + ");";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                }
            });
        } else {
            view.loadUrl(script);
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
        if (impCallBackInterface != null) {
            impCallBackInterface.onLoadingDlgDimiss();
            impCallBackInterface.showLoadFailLayout(failingUrl, description);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        // 在这里加上个判断,防止资源文件等错误导致显示错误页
        if (request.isForMainFrame() && request.getUrl().toString().equals(url) && !(error.getErrorCode() == WebViewClient.ERROR_UNKNOWN)) {
            if (runnable != null) {
                mHandler.removeCallbacks(runnable);
                runnable = null;
            }
            if (impCallBackInterface != null) {
                impCallBackInterface.onLoadingDlgDimiss();
                impCallBackInterface.showLoadFailLayout(url, error.getDescription().toString());
            }
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.toLowerCase().contains("/favicon.ico")) {
            try {
                return new WebResourceResponse("image/png", null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.shouldInterceptRequest(view, url);
    }


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !request.isForMainFrame() &&
                !StringUtils.isBlank(request.getUrl().getPath()) && request.getUrl().getPath().equals("/favicon.ico")) {
            return shouldInterceptRequest(view, request.getUrl().toString());
        }
        return super.shouldInterceptRequest(view, request);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, final WebResourceRequest request) {
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (!filterUrl(request.getUrl().toString(), view)) {
            WebResourceRequest newRequest = new WebResourceRequest() {
                @Override
                public Uri getUrl() {
                    return request.getUrl();
                }

                @Override
                public boolean isForMainFrame() {
                    return request.isForMainFrame();
                }

                @Override
                public boolean isRedirect() {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        return request.isRedirect();
                    }
                    return false;
                }

                @Override
                public boolean hasGesture() {
                    return request.hasGesture();
                }

                @Override
                public String getMethod() {
                    return request.getMethod();
                }

                @Override
                public Map<String, String> getRequestHeaders() {
                    Map<String, String> headers = getWebViewHeaders(request.getUrl().toString());
                    if (url.contains("wx.tenpay.com")) {
                        headers.put("Referer", referer);
                    }
                    if (BaseApplication.getInstance().getCurrentEnterprise() != null) {
                        headers.put("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
                    }
                    return headers;
                }
            };
            return super.shouldOverrideUrlLoading(view, newRequest);
        }
        return true;


    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return false;
        }
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
            runnable = null;
        }
        if (!filterUrl(url, view)) {
            if (url.contains("wx.tenpay.com")) {

                // 兼容 Android 4.4.3 和 4.4.4 两个系统版本设置 referer 无效的问题
                if (("4.4.3".equals(android.os.Build.VERSION.RELEASE))
                        || ("4.4.4".equals(android.os.Build.VERSION.RELEASE))) {
                    if (firstVisitWXH5PayUrl) {
                        view.loadDataWithBaseURL(referer, "<script>window.location.href=\"" + url + "\";</script>",
                                "text/html", "utf-8", null);
                        // 修改标记位状态，避免循环调用
                        // 再次进入微信H5支付流程时记得重置状态 firstVisitWXH5PayUrl = true
                        firstVisitWXH5PayUrl = false;
                    }
                    // 返回 false 由系统 WebView 自己处理该 url
                    return false;
                } else {
                    // HashMap 指定容量初始化，避免不必要的内存消耗
                    HashMap<String, String> map = new HashMap<>(1);
                    map.put("Referer", referer);
                    view.loadUrl(url, map);
                    return true;
                }
            } else {
                view.loadUrl(url, getWebViewHeaders(url));
            }

        }
        return true;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
        if (isLogin) {
            view.clearHistory();
            isLogin = false;
        }
    }

    /**
     * 过滤url
     *
     * @return 是否被过滤掉
     */
    private boolean filterUrl(String url, WebView webView) {
        if (url.startsWith(WebAPIUri.getWebLoginUrl()) || url.startsWith("https://id.inspur.com/oauth2.0/authorize")) {
            handleReDirectURL(url, webView);
            return true;
        }
        if (!url.startsWith("http") && !url.startsWith("ftp")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                intent.setComponent(null);
                List<ResolveInfo> resolveInfoList = BaseApplication.getInstance().getPackageManager().queryIntentActivities(intent, 0);
                boolean isMyAppScheme = false;
                if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("geo:") || url.startsWith("sms:") || url.startsWith("smsto:") || url.startsWith("tel:") || url.startsWith("alipays:") || url.startsWith("alipay") || url.startsWith("weixin")) {
                    isMyAppScheme = true;
                } else if (resolveInfoList != null && resolveInfoList.size() > 0) {
                    for (ResolveInfo resolveInfo : resolveInfoList) {
                        if (resolveInfo.activityInfo.packageName.equals(BaseApplication.getInstance().getPackageName())) {
                            isMyAppScheme = true;
                            break;
                        }
                    }
                }
                if (isMyAppScheme) {
                    webView.getContext().startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 获取Header
     *
     * @return
     */
    private Map<String, String> getWebViewHeaders(String url) {
        return myWebView == null ? new HashMap<String, String>() : ((impCallBackInterface != null) ? impCallBackInterface.onGetWebViewHeaders(url) : new HashMap<String, String>());
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
        int port = urlWithParams.getPort();
        String requestUrl = urlWithParams.getProtocol() + "://" + urlWithParams.getHost() + (port == -1 ? "" : ":" + port);
        WebAPIService appAPIService = new WebAPIService(BaseApplication.getInstance());
        appAPIService.setAPIInterface(new WebService(view));
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            appAPIService.getAuthCode(requestUrl, urlWithParams.getQuery());
        }

    }

    class WebService extends WebAPIInterfaceImpl {
        private WebView webView;

        public WebService(WebView webView) {
            this.webView = webView;
        }

        @Override
        public void returnGetAppAuthCodeResultSuccess(AppRedirectResult appRedirectResult) {
            if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
                isLogin = true;
                String redirectUri = appRedirectResult.getRedirect_uri();
                Map<String, String> webViewHeaders = getWebViewHeaders(redirectUri);
                if (!webViewHeaders.containsKey("X-ECC-Current-Enterprise") && BaseApplication.getInstance().getCurrentEnterprise() != null) {
                    webViewHeaders.put("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
                }
                webView.loadUrl(redirectUri, webViewHeaders);
            }
        }

        @Override
        public void returnGetAppAuthCodeResultFail(String error, int errorCode) {
            super.returnGetAppAuthCodeResultFail(error, errorCode);
        }
    }
}
