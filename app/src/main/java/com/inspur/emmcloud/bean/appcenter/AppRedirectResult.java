package com.inspur.emmcloud.bean.appcenter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/3/13.
 */

public class AppRedirectResult {

    private String code;
    private String redirect_uri;

    /**
     * code : OC-1-7cRqHyFGz5WOUceqU7gR5REsvwiY9mseO6h
     * redirect_uri : http://thinklancer.com/index.php/Phone/Oauth/auth?code=OC-1-7cRqHyFGz5WOUceqU7gR5REsvwiY9mseO6h
     */

    public AppRedirectResult(String response) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
            if (jsonObject.has("code")) {
                this.code = jsonObject.getString("code");
            }
            if (jsonObject.has("redirect_uri")) {
                this.redirect_uri = jsonObject.getString("redirect_uri");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirect_uri() {
        return redirect_uri;
    }

    public void setRedirect_uri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }
}
