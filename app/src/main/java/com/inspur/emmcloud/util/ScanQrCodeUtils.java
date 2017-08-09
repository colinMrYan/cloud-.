package com.inspur.emmcloud.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.inspur.emmcloud.ui.find.ScanResultActivity;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.api.ImpActivity;

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
//        String urlHost = "";
//        try {
//            URL url = new URL(msg);
//            urlHost = url.getHost();
//            LogUtils.YfcDebug("url的host："+url.getHost());
//            LogUtils.YfcDebug("url的protocol："+url.getProtocol());
//        } catch (MalformedURLException e) {
//            LogUtils.YfcDebug("解析出现异常："+e.getMessage());
//            e.printStackTrace();
//        }
//        ToastUtils.show(context,msg);
        LogUtils.YfcDebug("扫描到的信息是："+msg);

        Pattern pattern = Pattern.compile(URLMatcher.URL_PATTERN);
//        !StringUtils.isBlank(urlHost)&&urlHost.equals("id.inspur.com")
        msg = msg.trim();
        if(msg.startsWith("ecc-compont://auth")){
            LogUtils.YfcDebug("扫描到登录桌面版的路径");
//            Intent intent = new Intent();
//            intent.setClass(context, ScanQrCodeLoginActivity.class);
//            intent.putExtra("scanMsg",msg);
//            context.startActivity(intent);
//            loginDesktopCloudPlus(msg);
//            pattern.matcher(msg).matches()
//            msg.startsWith("http")
        }else if(msg.startsWith("ecm-contact")){
            Uri uri = Uri.parse(msg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
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
     * 展示扫描到的信息
     * @param msg
     */
    private void showUnKnownMsg(String msg){
        Intent intent = new Intent();
        intent.putExtra("result",msg);
        intent.setClass(context, ScanResultActivity.class);
        context.startActivity(intent);
    }


}
