package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.system.ClientConfigItem;
import com.inspur.emmcloud.bean.system.GetAllConfigVersionResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

/**
 * Created by chenmch on 2018/7/25.
 */

public class ClientConfigUpdateUtils extends APIInterfaceInstance {
    private Context context;
    private CommonCallBack callBack;
    private boolean isFirstGetAllConfigVersion = false;

    public ClientConfigUpdateUtils(Context context, CommonCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
    }


    public void getAllConfigUpdate() {
        if (!NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, "");
            if (callBack != null) {
                callBack.execute();
            }
            return;
        }
        String cacheAllConfigVersionResult = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, "");
        isFirstGetAllConfigVersion = StringUtils.isBlank(cacheAllConfigVersionResult);
        if (!isFirstGetAllConfigVersion && callBack != null) {
            callBack.execute();
        }
        AppAPIService apiService = new AppAPIService(context);
        apiService.setAPIInterface(this);
        String localLangVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_LANGUAGE.getValue(), "");
        String localMainTabVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_MAINTAB.getValue(), "");
        String localSplashVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_SPLASH.getValue(), "");
        String localRouterVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), ClientConfigItem.CLIENT_CONFIG_ROUTER.getValue(), "");
        JSONObject clientConfigVersionObj = new JSONObject();
        try {
            clientConfigVersionObj.put("lang", localLangVersion);
            clientConfigVersionObj.put("maintab", localMainTabVersion);
            clientConfigVersionObj.put("ad", localSplashVersion);
            clientConfigVersionObj.put("router", localRouterVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
        apiService.getAllConfigVersion(clientConfigVersionObj);
    }

    public static boolean isItemNeedUpdate(ClientConfigItem clientConfigItem) {
        return isItemNeedUpdate(clientConfigItem, getCacheAllConfigVersionResult());
    }

    public static boolean isItemNeedUpdate(ClientConfigItem clientConfigItem, GetAllConfigVersionResult getAllConfigVersionResult) {
        if (getAllConfigVersionResult == null){
            return true;
        }
        String itemLocalVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), clientConfigItem.getValue(), "");
        String itemNewVersion = getAllConfigVersionResult.getItemVersion(clientConfigItem);
        return StringUtils.isBlank(itemLocalVersion) || StringUtils.isBlank(itemNewVersion) || !itemLocalVersion.equals(itemNewVersion);
    }

    public static String getItemNewVersion(ClientConfigItem clientConfigItem){
        String  itemNewVersion = "";
        if (getCacheAllConfigVersionResult() != null){
            itemNewVersion = getCacheAllConfigVersionResult().getItemVersion(clientConfigItem);
        }
        return  itemNewVersion;
    }

    public static void saveItemLocalVersion(ClientConfigItem clientConfigItem,String version) {
            if (!StringUtils.isBlank(version)){
                PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), clientConfigItem.getValue(), version);
            }
    }


    private static GetAllConfigVersionResult getCacheAllConfigVersionResult() {
        String commonNewVersion = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, "");
        if (!StringUtils.isBlank(commonNewVersion)){
            return new GetAllConfigVersionResult(commonNewVersion);
        }
        return null;

    }


    @Override
    public void returnAllConfigVersionSuccess(GetAllConfigVersionResult getAllConfigVersionResult) {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, getAllConfigVersionResult.getResponse());
        if (isFirstGetAllConfigVersion && callBack != null) {
            callBack.execute();
        }
        EventBus.getDefault().post(getAllConfigVersionResult);
    }

    @Override
    public void returnAllConfigVersionFail(String error, int errorCode) {
        PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_V_CONFIG_ALL, "");
        EventBus.getDefault().post(new GetAllConfigVersionResult(""));
    }
}
