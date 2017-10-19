package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.AppException;
import com.inspur.emmcloud.bean.SplashPageBean;
import com.inspur.emmcloud.callback.CommonCallBack;
import com.inspur.emmcloud.config.MyAppConfig;

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
            String command = splashPageBean.getCommand();
            if (command.equals("FORWARD")) {
                String screenType = AppUtils.getScreenType(context);
                SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean = splashPageBean.getPayload()
                        .getResource().getDefaultX();
                if (screenType.equals("2k")) {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getXxxhdpi()), defaultBean.getXxxhdpi(),splashPageBean);
                } else if (screenType.equals("xxhdpi")) {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getXxhdpi()), defaultBean.getXxhdpi(),splashPageBean);
                } else if (screenType.equals("xhdpi")) {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getXhdpi()), defaultBean.getXhdpi(),splashPageBean);
                } else {
                    downloadSplashPage(UriUtils.getPreviewUri(defaultBean.getHdpi()), defaultBean.getHdpi(),splashPageBean);
                }
            }
    }

    /**
     * 下载闪屏页
     *
     * @param url
     */
    private void downloadSplashPage(final String url, String fileName, final SplashPageBean splashPageBean) {
        DownLoaderUtils downloaderUtils = new DownLoaderUtils();
        downloaderUtils.startDownLoad(url, MyAppConfig.getSplashPageImageShowPath(context,
                ((MyApplication) context.getApplicationContext()).getUid(), "splash/" + fileName), new APIDownloadCallBack(context, url) {

            @Override
            public void callbackStart() {

            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {

            }

            @Override
            public void callbackSuccess(File file) {
                if (file.exists()) {
                    String filelSha256 = FileSafeCode.getFileSHA256(file);
                    String screenType = AppUtils.getScreenType(context);
                    String sha256Code = "";
                    if (screenType.equals("2k")) {
                        sha256Code = splashPageBean.getPayload().getXxxhdpiHash().split(":")[1];
                    } else if (screenType.equals("xxhdpi")) {
                        sha256Code = splashPageBean.getPayload().getXxhdpiHash().split(":")[1];
                    } else if (screenType.equals("xhdpi")) {
                        sha256Code = splashPageBean.getPayload().getXhdpiHash().split(":")[1];
                    } else {
                        sha256Code = splashPageBean.getPayload().getHdpiHash().split(":")[1];
                    }
                    if (filelSha256.equals(sha256Code)) {
                        String splashInfoOld = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info_old", "");
                        SplashPageBean splashPageBeanLocalOld = new SplashPageBean(splashInfoOld);
                        writeBackSplashPageLog("FORWARD", splashPageBeanLocalOld.getId().getVersion()
                                ,splashPageBean.getId().getVersion());
                        if (splashPageBean.getCommand().equals("FORWARD")) {
                            //先把上次的信息取出来，作为旧版数据存储
                            String splashPageInfoOld = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info", "");
                            PreferencesByUserAndTanentUtils.putString(context, "splash_page_info_old", splashPageInfoOld);
                            //存完旧数据后把本次数据存到更新里
                            PreferencesByUserAndTanentUtils.putString(context, "splash_page_info", splashPageBean.getResponse());
                        }
                    } else {
                        saveFileCheckException(context, url, "splash sha256 Error", 2);
                    }

                }
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
            }

            @Override
            public void callbackCanceled(CancelledException e) {
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

    /**
     * 记录文件下载后验证异常
     *
     * @param context
     * @param url
     * @param error
     * @param errorLevel
     */
    private void saveFileCheckException(Context context, String url, String error, int errorLevel) {
        if (!AppUtils.isApkDebugable(context)) {
            AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(context), errorLevel, url, error, 0);
            AppExceptionCacheUtils.saveAppException(context, appException);
        }
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
