package com.inspur.emmcloud.util;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.ui.find.ScanResultActivity;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.api.ImpActivity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by yufuchang on 2017/7/21.
 * 封装扫描处理模块，用来处理扫一扫功能
 */

public class ScanQrCodeUtils {
    private static ScanQrCodeUtils scanQrCodeUtils;
    private Context context;
    private LoadingDialog loadingDialog;
    public static ScanQrCodeUtils getScanQrCodeUtilsInstance(Context context){
        if(scanQrCodeUtils == null){
            synchronized (ScanQrCodeUtils.class){
                if(scanQrCodeUtils == null){
                    scanQrCodeUtils = new ScanQrCodeUtils(context);
                }
            }
        }
        return scanQrCodeUtils;
    }

    private ScanQrCodeUtils(Context context){
        this.context = context;
        loadingDialog = new LoadingDialog(context);
    }

    public void handleActionWithMsg(String msg){
        String urlHost = "";
//        ToastUtils.show(context,"扫描到的信息是："+msg);
//        if(符合登录桌面版接口){
//            loginDesktopCloudPlus(msg);
//        }else if(符合打开某个应用){
//            UriUtils.openApp(getActivity(),app);
//        }else{
//            ToastUtils.show(getActivity(),msg);
//        }
//        ToastUtils.show(getActivity(),"扫描到的信息是："+msg);


        try {
            URL url = new URL(msg);
            urlHost = url.getHost();
            LogUtils.YfcDebug("url的host："+url.getHost());
            LogUtils.YfcDebug("url的protocol："+url.getProtocol());
        } catch (MalformedURLException e) {
            LogUtils.YfcDebug("解析出现异常："+e.getMessage());
            e.printStackTrace();
        }

        Pattern pattern = Pattern.compile(URLMatcher.URL_PATTERN);
//        !StringUtils.isBlank(urlHost)&&urlHost.equals("id.inspur.com")
        if(msg.startsWith("ecc-compont://auth")){
            LogUtils.YfcDebug("扫描到登录桌面版的路径");
//            loginDesktopCloudPlus(msg);
        }else if(pattern.matcher(msg).matches()){
            Intent intent = new Intent();
            intent.setClass(context, ImpActivity.class);
            intent.putExtra("uri",msg);
            context.startActivity(intent);
        }else {
            showUnKnownMsg(msg);
        }
    }

    /**
     * 登录云+桌面版
     *
     * @param msg
     */
    private void loginDesktopCloudPlus(String msg) {
        AppAPIService appAPIService = new AppAPIService(context);
        appAPIService.setAPIInterface(new WebService());
        if(NetUtils.isNetworkConnected(context)){
            loadingDialog.show();
            appAPIService.sendLoginDesktopCloudPlusInfo(msg);
        }
    }

    /**
     * 展示扫描到的信息
     * @param msg
     */
    private void showUnKnownMsg(String msg){
        Intent intent = new Intent();
        intent.putExtra("result",msg);
        intent.setClass(context, ScanResultActivity.class);
        context.startActivity(intent);
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnLoginDesktopCloudPlusSuccess(LoginDesktopCloudPlusBean loginDesktopCloudPlusBean) {
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            ToastUtils.show(context,"登录成功");
        }

        @Override
        public void returnLoginDesktopCloudPlusFail(String error, int errorCode) {
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(context,error,errorCode);
        }
    }
}
