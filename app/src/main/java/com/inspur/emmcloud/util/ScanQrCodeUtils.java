package com.inspur.emmcloud.util;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.bean.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.ui.find.ScanResultActivity;

/**
 * Created by yufuchang on 2017/7/21.
 * 封装扫描处理模块，用来处理扫一扫功能
 */

public class ScanQrCodeUtils {
    private static ScanQrCodeUtils scanQrCodeUtils;
    private Context context;
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
    }

    public void handleActionWithMsg(String msg){
//        ToastUtils.show(context,"扫描到的信息是："+msg);
//        if(符合登录桌面版接口){
//            loginDesktopCloudPlus(msg);
//        }else if(符合打开某个应用){
//            UriUtils.openApp(getActivity(),app);
//        }else{
//            ToastUtils.show(getActivity(),msg);
//        }
//        ToastUtils.show(getActivity(),"扫描到的信息是："+msg);
        showUnKnownMsg(msg);
    }

    /**
     * 登录云+桌面版
     *
     * @param msg
     */
    private void loginDesktopCloudPlus(String msg) {
//        AppAPIService appAPIService = new AppAPIService(context);
//        appAPIService.setAPIInterface(new WebService());
//        if(NetUtils.isNetworkConnected(context)){
//            appAPIService.sendLoginDesktopCloudPlusInfo();
//        }
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
            super.returnLoginDesktopCloudPlusSuccess(loginDesktopCloudPlusBean);
        }

        @Override
        public void returnLoginDesktopCloudPlusFail(String error, int errorCode) {
            super.returnLoginDesktopCloudPlusFail(error, errorCode);
        }
    }
}
