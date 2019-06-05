package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.bean.system.ClientConfigItem;
import com.inspur.emmcloud.bean.system.GetAllConfigVersionResult;
import com.inspur.emmcloud.config.Constant;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

/**
 * Created by chenmch on 2018/7/25.
 */

public class ClientConfigUpdateUtils extends APIInterfaceInstance {
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
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            AppAPIService apiService = new AppAPIService(MyApplication.getInstance());
            apiService.setAPIInterface(this);
            String localLangVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_LANGUAGE.getValue(), "");
            String localMainTabVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MAINTAB.getValue(), "");
            String localSplashVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_SPLASH.getValue(), "");
            String localRouterVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_ROUTER.getValue(), "");
            String localMyAppVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
            String localContactUserVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_USER.getValue(), "");
            String localContactOrgVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG.getValue(), "");
            String localNaviTabVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_NAVI_TAB.getValue(),"");
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
        String itemLocalVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), clientConfigItem.getValue(), "");
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
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), clientConfigItem.getValue(), version);
        }
    }


    private GetAllConfigVersionResult getCacheAllConfigVersionResult() {
        String commonNewVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, "");
        if (!StringUtils.isBlank(commonNewVersion)) {
            return new GetAllConfigVersionResult(commonNewVersion);
        }
        return null;

    }

    /**
     * 当清除所有缓存的时候清空以db形式存储数据的configVersion
     */
    public void clearDbDataConfigWithClearAllCache() {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_USER.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
    }

    public void clearDbDataConfigWithMyApp() {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
    }


    @Override
    public void returnAllConfigVersionSuccess(GetAllConfigVersionResult getAllConfigVersionResult) {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, getAllConfigVersionResult.getResponse());
        sendClientConfigUpdateInfo(getAllConfigVersionResult);
    }

    @Override
    public void returnAllConfigVersionFail(String error, int errorCode) {
        getAllConfigVersionFail();
    }

    private void getAllConfigVersionFail() {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_USER.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_CONTACT_ORG.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MY_APP.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MAINTAB.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_LANGUAGE.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_SPLASH.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_ROUTER.getValue(), "");
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_NAVI_TAB.getValue(), "");
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
