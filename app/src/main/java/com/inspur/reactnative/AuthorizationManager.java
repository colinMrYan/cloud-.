package com.inspur.reactnative;

import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yufuchang on 2017/2/20.
 */

public class AuthorizationManager extends ReactContextBaseJavaModule {

    public AuthorizationManager(ReactApplicationContext reactContext){
        super(reactContext);
    }
    @Override
    public String getName() {
        return "AuthorizationManger";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        return constants;
    }

    /**
     * 获取token方法提供给
     * @return
     */
    @ReactMethod
    public void getOAuth20AccessToken() {
        Toast.makeText(getReactApplicationContext(),"回调获取token方法成功，token："+getToken(), Toast.LENGTH_SHORT).show();
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

    @ReactMethod
    public void getOAuth20AccessToken(
            Callback errorCallback,
            Callback successCallback) {
        try {
            successCallback.invoke(getToken());
        } catch (IllegalViewOperationException e) {
            errorCallback.invoke(e.getMessage());
        }
    }

}
