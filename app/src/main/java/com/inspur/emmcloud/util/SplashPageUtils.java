package com.inspur.emmcloud.util;

import android.app.Activity;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.SplashPageBean;
import com.inspur.emmcloud.callback.CommonCallBack;
import com.inspur.emmcloud.config.MyAppConfig;

import org.xutils.common.Callback;

import java.io.File;

/**
 * Created by chenmch on 2017/10/10.
 */

public class SplashPageUtils {
    private Activity context;
    public SplashPageUtils(Activity context){
        this.context = context;
    }

    public void update(){
        new ClientIDUtils(context, new CommonCallBack() {
            @Override
            public void execute() {
                if (NetUtils.isNetworkConnected(context, false)) {
                    AppAPIService apiService = new AppAPIService(context);
                    apiService.setAPIInterface(new WebService());
                    String splashInfo = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info", "");
                    SplashPageBean splashPageBean = new SplashPageBean(splashInfo);
                    String clientId = PreferencesUtils.getString(context, UriUtils.tanent + ((MyApplication)context.getApplicationContext()).getUid() + "react_native_clientid", "");
                    apiService.getSplashPageInfo(clientId, splashPageBean.getId().getVersion());

                }
            }
        }).getClientID();

    }


    /**
     * 根据命令更新闪屏页
     *
     * @param splashPageBean
     */
    private void updateSplashPageWithOrder(SplashPageBean splashPageBean) {
        try {
            String command = splashPageBean.getCommand();
            if (command.equals("FORWARD")) {
                String screenType = AppUtils.getScreenType(context);
                SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean = splashPageBean.getPayload()
                        .getResource().getDefaultX();
                if (screenType.equals("2k")) {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getXxxhdpi()), defaultBean.getXxxhdpi());
                } else if (screenType.equals("xxhdpi")) {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getXxhdpi()), defaultBean.getXxhdpi());
                } else if (screenType.equals("xhdpi")) {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getXhdpi()), defaultBean.getXhdpi());
                } else {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getHdpi()), defaultBean.getHdpi());
                }
            } else if (command.equals("ROLLBACK")) {
//            ReactNativeFlow.moveFolder(MyAppConfig.getSplashPageImageLastVersionPath(context,userId),
//                    MyAppConfig.getSplashPageImageShowPath(context,
//                            userId, "splash"));
            } else if (command.equals("STANDBY")) {
            } else {
                LogUtils.YfcDebug("当做STANDBY");
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * 下载闪屏页
     *
     * @param url
     */
    private void downloadSplashPage(String url, String fileName) {
        LogUtils.YfcDebug("下载文件名称：" + fileName);
        DownLoaderUtils downloaderUtils = new DownLoaderUtils();
        LogUtils.YfcDebug("下载到的路径：" + MyAppConfig.getSplashPageImageShowPath(context,
                ((MyApplication) context.getApplication()).getUid(), "splash/" + fileName));
        downloaderUtils.startDownLoad(url, MyAppConfig.getSplashPageImageShowPath(context,
                ((MyApplication) context.getApplication()).getUid(), "splash/" + fileName), new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long l, long l1, boolean b) {

            }

            @Override
            public void onSuccess(File file) {
                String splashInfoOld = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info_old", "");
                SplashPageBean splashPageBeanLocalOld = new SplashPageBean(splashInfoOld);
                String splashInfoShowing = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info", "");
                SplashPageBean splashPageBeanLocalShowing = new SplashPageBean(splashInfoShowing);
                if (file.exists()) {
                    String filelSha256 = FileSafeCode.getFileSHA256(file);
                    String screenType = AppUtils.getScreenType(context);
                    String sha256Code = "";
                    if (screenType.equals("2k")) {
                        sha256Code = splashPageBeanLocalShowing.getPayload().getXxxhdpiHash().split(":")[1];
                    } else if (screenType.equals("xxhdpi")) {
                        sha256Code = splashPageBeanLocalShowing.getPayload().getXxhdpiHash().split(":")[1];
                    } else if (screenType.equals("xhdpi")) {
                        sha256Code = splashPageBeanLocalShowing.getPayload().getXhdpiHash().split(":")[1];
                    } else {
                        sha256Code = splashPageBeanLocalShowing.getPayload().getHdpiHash().split(":")[1];
                    }
                    if (filelSha256.equals(sha256Code)) {
                        writeBackSplashPageLog("FORWARD", splashPageBeanLocalOld.getId().getVersion()
                                , splashPageBeanLocalShowing.getId().getVersion());
                    } else {
                        LogUtils.YfcDebug("Sha256验证出错：" + filelSha256 + "从更新信息获取到的sha256" + sha256Code);
                    }

                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {

            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
    /**
     * 写回闪屏日志
     *
     * @param s
     */
    private void writeBackSplashPageLog(String s, String preversion, String currentVersion) {
        if (NetUtils.isNetworkConnected(context,false)){
            String uid = ((MyApplication)context.getApplicationContext()).getUid();
            String clientId = PreferencesUtils.getString(context, UriUtils.tanent + uid +
                    "react_native_clientid", "");
            ReactNativeAPIService reactNativeAPIService = new ReactNativeAPIService(context);
            reactNativeAPIService.setAPIInterface(new WebService());
            reactNativeAPIService.writeBackSplashPageVersionChange(preversion, currentVersion, clientId, s);
        }
    }

    /**
     * 检查是否有可以展示的图片
     *
     * @return
     */
    public boolean checkIfShowSplashPage() {
        boolean flag = false;
        String splashInfo = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info");
        if (!StringUtils.isBlank(splashInfo)) {
            SplashPageBean splashPageBeanLoacal = new SplashPageBean(splashInfo);
            SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean = splashPageBeanLoacal.getPayload()
                    .getResource().getDefaultX();
            String splashImgPath = getSplashPagePath(defaultBean);
            long startTime = splashPageBeanLoacal.getPayload().getEffectiveDate();
            long endTime = splashPageBeanLoacal.getPayload().getExpireDate();
            long nowTime = System.currentTimeMillis();
            flag = FileUtils.isFileExist(splashImgPath) &&
                    ((nowTime > startTime) && (nowTime < endTime));
        }
        return flag;
    }

    /**
     * 闪屏文件路径
     *
     * @param defaultBean
     * @return
     */
    private String getSplashPagePath(SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean) {
        String screenType = AppUtils.getScreenType(context);
        String name = "";
        if (screenType.equals("2k")) {
            name = MyAppConfig.getSplashPageImageShowPath(context,
                    ((MyApplication) context.getApplicationContext()).getUid(), "splash/" + defaultBean.getXxxhdpi());
        } else if (screenType.equals("xxhdpi")) {
            name = MyAppConfig.getSplashPageImageShowPath(context,
                    ((MyApplication) context.getApplicationContext()).getUid(), "splash/" + defaultBean.getXxhdpi());
        } else if (screenType.equals("xhdpi")) {
            name = MyAppConfig.getSplashPageImageShowPath(context,
                    ((MyApplication) context.getApplicationContext()).getUid(), "splash/" + defaultBean.getXhdpi());
        } else {
            name = MyAppConfig.getSplashPageImageShowPath(context,
                    ((MyApplication) context.getApplicationContext()).getUid(), "splash/" + defaultBean.getHdpi());
        }
        return name;
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnSplashPageInfoSuccess(SplashPageBean splashPageBean) {
            updateSplashPageWithOrder(splashPageBean);
        }

        @Override
        public void returnSplashPageInfoFail(String error, int errorCode) {

        }
    }
}
