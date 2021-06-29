package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ClientConfigItem;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.config.Constant;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

/**
 * Created by chenmch on 2018/7/25.
 */

public class ClientConfigUpdateUtils extends BaseModuleAPIInterfaceInstance {
    private static ClientConfigUpdateUtils mInstance;
    private boolean isCheckClientConfigUpdate = false; //是否正在检查更新中

    public static ClientConfigUpdateUtils getInstance() {
        if (mInstance == null) {
            synchronized (ClientConfigUpdateUtils.class) {
                if (mInstance == null) {
                    mInstance = new ClientConfigUpdateUtils();
                }
            }
        }
        return mInstance;
    }

    public void getAllConfigUpdate() {
        isCheckClientConfigUpdate = true;
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            BaseModuleApiService apiService = new BaseModuleApiService(BaseApplication.getInstance());
            apiService.setAPIInterface(this);
            String localLangVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_LANGUAGE.getValue(), "");
            String localMainTabVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MAINTAB.getValue(), "");
            String localSplashVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_SPLASH.getValue(), "");
            String localRouterVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_ROUTER.getValue(), "");
            String localMyAppVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
            String localContactUserVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_USER.getValue(), "");
            String localContactOrgVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG.getValue(), "");
            String localNaviTabVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_NAVI_TAB.getValue(), "");
            JSONObject clientConfigVersionObj = new JSONObject();
            try {
                clientConfigVersionObj.put("lang", localLangVersion);
                clientConfigVersionObj.put("maintab", localMainTabVersion);
                clientConfigVersionObj.put("ad", localSplashVersion);
                clientConfigVersionObj.put("router", localRouterVersion);
                clientConfigVersionObj.put("app", localMyAppVersion);
                clientConfigVersionObj.put("contact_user", localContactUserVersion);
                clientConfigVersionObj.put("contact_org", localContactOrgVersion);
                clientConfigVersionObj.put("multipleLayout",localNaviTabVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
            apiService.getAllConfigVersion(clientConfigVersionObj);
        } else {
            getAllConfigVersionFail();
        }

    }

    public boolean isCheckClientConfigUpdate() {
        return isCheckClientConfigUpdate;
    }

    public boolean isItemNeedUpdate(ClientConfigItem clientConfigItem) {
        return isItemNeedUpdate(clientConfigItem, getCacheAllConfigVersionResult());
    }

    public boolean isItemNeedUpdate(ClientConfigItem clientConfigItem, GetAllConfigVersionResult getAllConfigVersionResult) {
        if (getAllConfigVersionResult == null) {
            return true;
        }
        String itemLocalVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), clientConfigItem.getValue(), "");
        String itemNewVersion = getAllConfigVersionResult.getItemVersion(clientConfigItem);
        return StringUtils.isBlank(itemLocalVersion) || StringUtils.isBlank(itemNewVersion) || !itemLocalVersion.equals(itemNewVersion);
    }

    public String getItemNewVersion(ClientConfigItem clientConfigItem) {
        String itemNewVersion = "";
        if (getCacheAllConfigVersionResult() != null) {
            itemNewVersion = getCacheAllConfigVersionResult().getItemVersion(clientConfigItem);
        }
        return itemNewVersion;
    }

    public void saveItemLocalVersion(ClientConfigItem clientConfigItem, String version) {
        if (!StringUtils.isBlank(version)) {
            PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), clientConfigItem.getValue(), version);
        }
    }


    private GetAllConfigVersionResult getCacheAllConfigVersionResult() {
        String commonNewVersion = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, "");
        if (!StringUtils.isBlank(commonNewVersion)) {
            return new GetAllConfigVersionResult(commonNewVersion);
        }
        return null;

    }

    /**
     * 当清除所有缓存的时候清空以db形式存储数据的configVersion
     */
    public void clearDbDataConfigWithClearAllCache() {
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_USER.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MAINTAB.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_NAVI_TAB.getValue(), "");
    }

    public void clearDbDataConfigWithMyApp() {
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
    }


    @Override
    public void returnAllConfigVersionSuccess(GetAllConfigVersionResult getAllConfigVersionResult) {
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, getAllConfigVersionResult.getResponse());
        sendClientConfigUpdateInfo(getAllConfigVersionResult);
    }

    @Override
    public void returnAllConfigVersionFail(String error, int errorCode) {
        getAllConfigVersionFail();
    }

    private void getAllConfigVersionFail() {
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_USER.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MAINTAB.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_LANGUAGE.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_SPLASH.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_ROUTER.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_NAVI_TAB.getValue(), "");
        GetAllConfigVersionResult allConfigVersionResult = getCacheAllConfigVersionResult();
        if (allConfigVersionResult == null) {
            allConfigVersionResult = new GetAllConfigVersionResult("");
        }
        sendClientConfigUpdateInfo(allConfigVersionResult);
    }


    /**
     * 发送客户端统一更新信息，各个功能分别以eventbus接收处理
     *
     * @param getAllConfigVersionResult
     */
    private void sendClientConfigUpdateInfo(GetAllConfigVersionResult getAllConfigVersionResult) {
        isCheckClientConfigUpdate = false;
        EventBus.getDefault().post(getAllConfigVersionResult);
    }
}
