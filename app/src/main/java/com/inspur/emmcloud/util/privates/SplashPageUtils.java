package com.inspur.emmcloud.util.privates;

import android.app.Activity;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.bean.ClientConfigItem;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.system.SplashDefaultBean;
import com.inspur.emmcloud.bean.system.SplashPageBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/10/10.
 */

public class SplashPageUtils {
    private Activity context;
    private String saveConfigVersion = "";

    public SplashPageUtils(Activity context) {
        this.context = context;
    }

    public void update() {
        saveConfigVersion = ClientConfigUpdateUtils.getInstance().getItemNewVersion(ClientConfigItem.CLIENT_CONFIG_SPLASH);

        new ClientIDUtils(MyApplication.getInstance(), new ClientIDUtils.OnGetClientIdListener() {
            @Override
            public void getClientIdSuccess(String chatClientId) {
                if (NetUtils.isNetworkConnected(context, false)) {
                    AppAPIService apiService = new AppAPIService(context);
                    apiService.setAPIInterface(new WebService());
                    String splashInfo = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info", "");
                    SplashPageBean splashPageBean = new SplashPageBean(splashInfo);
                    String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CLIENTID, "");
                    apiService.getSplashPageInfo(clientId, splashPageBean.getId().getVersion());
                }
            }

            @Override
            public void getClientIdFail() {
            }
        }).getClientId();
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
            SplashDefaultBean defaultBean = splashPageBean.getPayload()
                    .getResource().getDefaultX();
            switch (screenType) {
                case "2k":
                    downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getXxxhdpi()), defaultBean.getXxxhdpi(), splashPageBean);
                    break;
                case "xxhdpi":
                    downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getXxhdpi()), defaultBean.getXxhdpi(), splashPageBean);
                    break;
                case "xhdpi":
                    downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getXhdpi()), defaultBean.getXhdpi(), splashPageBean);
                    break;
                default:
                    downloadSplashPage(APIUri.getPreviewUrl(defaultBean.getHdpi()), defaultBean.getHdpi(), splashPageBean);
                    break;
            }
        } else {
            ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_SPLASH, saveConfigVersion);
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
                    switch (screenType) {
                        case "2k":
                            sha256Code = splashPageBean.getPayload().getXxxhdpiHash().split(":")[1];
                            break;
                        case "xxhdpi":
                            sha256Code = splashPageBean.getPayload().getXxhdpiHash().split(":")[1];
                            break;
                        case "xhdpi":
                            sha256Code = splashPageBean.getPayload().getXhdpiHash().split(":")[1];
                            break;
                        default:
                            sha256Code = splashPageBean.getPayload().getHdpiHash().split(":")[1];
                            break;
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
                                ((MyApplication) context.getApplicationContext()).getUid(), "splash/"), protectedFileNameList);
                        ClientConfigUpdateUtils.getInstance().saveItemLocalVersion(ClientConfigItem.CLIENT_CONFIG_SPLASH, saveConfigVersion);
                    } else {
                        AppExceptionCacheUtils.saveAppException(context, 2, url, "splash sha256 Error", 0);
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
    private String getCurrentSplashFileName(SplashDefaultBean defaultBean) {
        String screenType = AppUtils.getScreenType(context);
        String name = "";
        switch (screenType) {
            case "2k":
                name = defaultBean.getXxxhdpi();
                break;
            case "xxhdpi":
                name = defaultBean.getXxhdpi();
                break;
            case "xhdpi":
                name = defaultBean.getXhdpi();
                break;
            default:
                name = defaultBean.getHdpi();
                break;
        }
        return name;
    }

    /**
     * 写回闪屏日志
     *
     * @param s
     */
    private void writeBackSplashPageLog(String s, String currentVersion) {
        if (NetUtils.isNetworkConnected(context, false)) {
            String splashInfoOld = PreferencesByUserAndTanentUtils.getString(context, "splash_page_info_old", "");
            SplashPageBean splashPageBeanLocalOld = new SplashPageBean(splashInfoOld);
            String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CLIENTID, "");
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
            SplashDefaultBean defaultBean = splashPageBeanLoacal.getPayload()
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
    private String getSplashPagePath(SplashDefaultBean defaultBean) {
        String screenType = AppUtils.getScreenType(context);
        String fileName = "";
        switch (screenType) {
            case "2k":
                fileName = defaultBean.getXxxhdpi();
                break;
            case "xxhdpi":
                fileName = defaultBean.getXxhdpi();
                break;
            case "xhdpi":
                fileName = defaultBean.getXhdpi();
                break;
            default:
                fileName = defaultBean.getHdpi();
                break;
        }
        String filePath = MyAppConfig.getSplashPageImageShowPath(context,
                MyApplication.getInstance().getUid(), "splash/" + fileName);
        return filePath;
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
