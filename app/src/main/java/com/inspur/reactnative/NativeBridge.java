package com.inspur.reactnative;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yufuchang on 2017/2/20.
 */

public class NativeBridge extends ReactContextBaseJavaModule {

    public NativeBridge(ReactApplicationContext reactContext){
        super(reactContext);
    }

    @Override
    public String getName() {
        return "NativeBridge";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        return constants;
    }

    /**
     * 获取token
     * @return
     */
    public String getToken() {
        String token = PreferencesUtils.getString(getReactApplicationContext(), "accessToken","");
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return "Bearer" + " " + token;
    }

    /**
     * 获取token
     * @param promise
     */
    @ReactMethod
    public void getOAuth20AccessToken(Promise promise){
        try {
            promise.resolve(getToken());
        }catch (IllegalViewOperationException e){
            promise.reject(e);
        }
    }

    /**
     * 获取Profile
     * @param promise
     */
    @ReactMethod
    public void getCurrentUserProfie(Promise promise){
        String myInfo = PreferencesUtils.getString(getReactApplicationContext(),
                "myInfo", "");
        try {
            JSONObject myprofile = new JSONObject(myInfo);
            promise.resolve(myprofile);
        }catch (Exception e){
            promise.reject(e);
        }
    }

    /**
     * 获取企业信息
     * @param promise
     */
    @ReactMethod
    public void getCurrentEnterprise(Promise promise){
        String myInfo = PreferencesUtils.getString(getReactApplicationContext(),
                "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",getMyInfoResult.getEnterpriseId());
            jsonObject.put("name",getMyInfoResult.getEnterpriseName());
            jsonObject.put("code",getMyInfoResult.getEnterpriseCode());
            promise.resolve(jsonObject);
        }catch (Exception e){
            promise.reject(e);
        }
    }

    /**
     * 结束
     */
    @ReactMethod
    public void exit(){
        LogUtils.YfcDebug("调用Exit");
        getCurrentActivity().finish();
    }

}
