package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.system.AppException;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/10/10.
 */

public class SplashPageUtils {
    private Activity context;

    public SplashPageUtils(Activity context) {
        this.context = context;
    }

    public void update() {
        new ClientIDUtils(context, new CommonCallBack() {
            @Override
            public void execute() {
                if (NetUtils.isNetworkConnected(context, false)) {
                    AppAPIService apiService = new AppAPIService(context);
                    apiService.setAPIInterface(new WebService());
                    String splashInfo = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info", "");
                    SplashPageBean splashPageBean = new SplashPageBean(splashInfo);
                    String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_REACT_NATIVE_CLIENTID, "");
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
                downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getXxxhdpi()), defaultBean.getXxxhdpi(), splashPageBean);
            } else if (screenType.equals("xxhdpi")) {
                downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getXxhdpi()), defaultBean.getXxhdpi(), splashPageBean);
            } else if (screenType.equals("xhdpi")) {
                downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getXhdpi()), defaultBean.getXhdpi(), splashPageBean);
            } else {
                downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getHdpi()), defaultBean.getHdpi(), splashPageBean);
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

                        writeBackSplashPageLog("FORWARD"
                                , splashPageBean.getId().getVersion());
                        //先把上次的信息取出来，作为旧版数据存储
                        String splashPageInfoOld = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info", "");
                        PreferencesByUserAndTanentUtils.putString(context, "splash_page_info_old", splashPageInfoOld);
                        //存完旧数据后把本次数据存到更新里
                        PreferencesByUserAndTanentUtils.putString(context, "splash_page_info", splashPageBean.getResponse());
                        List<String> protectedFileNameList = new ArrayList<String>();
                        protectedFileNameList.add(getCurrentSplashFileName(splashPageBean.getPayload().getResource().getDefaultX()));
                        FileUtils.delFilesExceptNameList(MyAppConfig.getSplashPageImageShowPath(context,
                                ((MyApplication) context.getApplicationContext()).getUid(), "splash/"),protectedFileNameList);
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
     * 闪屏文件路径
     *
     * @param defaultBean
     * @return
     */
    private String getCurrentSplashFileName(SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean) {
        String screenType = AppUtils.getScreenType(context);
        String name = "";
        if (screenType.equals("2k")) {
            name = defaultBean.getXxxhdpi();
        } else if (screenType.equals("xxhdpi")) {
            name = defaultBean.getXxhdpi();
        } else if (screenType.equals("xhdpi")) {
            name = defaultBean.getXhdpi();
        } else {
            name = defaultBean.getHdpi();
        }
        return name;
    }

    /**
     * 写回闪屏日志
     *
     * @param s
     */
    private void writeBackSplashPageLog(String s,  String currentVersion) {
        if (NetUtils.isNetworkConnected(context, false)) {
            String splashInfoOld = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info_old", "");
            SplashPageBean splashPageBeanLocalOld = new SplashPageBean(splashInfoOld);
            String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_REACT_NATIVE_CLIENTID, "");
            ReactNativeAPIService reactNativeAPIService = new ReactNativeAPIService(context);
            reactNativeAPIService.setAPIInterface(new WebService());
            reactNativeAPIService.writeBackSplashPageVersionChange(splashPageBeanLocalOld.getId().getVersion(), currentVersion, clientId, s);
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
                    ((nowTime >= startTime) && (nowTime <= endTime));
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
