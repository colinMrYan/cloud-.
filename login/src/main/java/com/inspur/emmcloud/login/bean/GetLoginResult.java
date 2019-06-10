package com.inspur.emmcloud.login.bean;

import org.json.JSONObject;

/**
 * 登录信息返回解析类
 */
public class GetLoginResult {
    /**
     * {"access_token":"AT-7-y9j30oMPlJJCdM2oPZ7QQyfEZscTXfnSiVp",
     * "refresh_token":"RT-7-7nr2y1MOHjIjHZ5HjzSnSrX1dCaevB14jby",
     * "keep_alive":7200000,"token_type":"bearer","expires_in":28800000}
     */
    private static final String TAG = "GetLoginResult";
    private String userName = "";
    private String inspurID = "";
    private String email = "";
    private String mobile = "";
    private String head = "";
    private String userRealID = "";
    private String orgname;
    private String tenantid;
    private String tenantname;
    private String accessToken;
    private String refreshToken;
    private int keepAlive;
    private String tokenType;
    private int expiresIn;

    public GetLoginResult(String response) {
        try {
            JSONObject jObject = new JSONObject(response);
            if (jObject.has("access_token")) {
                this.accessToken = jObject.getString("access_token");
            }
            if (jObject.has("refresh_token")) {
                this.refreshToken = jObject.getString("refresh_token");
            }
            if (jObject.has("keep_alive")) {
                this.keepAlive = jObject.getInt("keep_alive");
            }
            if (jObject.has("token_type")) {
                this.tokenType = jObject.getString("token_type");
            }
            if (jObject.has("expires_in")) {
                this.expiresIn = jObject.getInt("expires_in");
            }
            if (jObject.has("real_name")) {
                this.userName = jObject.getString("real_name");
            }
            if (jObject.has("inspur_id")) {
                this.inspurID = jObject.getString("inspur_id");
            }
            if (jObject.has("email")) {
                this.email = jObject.getString("email");
            }
            if (jObject.has("mobile")) {
                this.mobile = jObject.getString("mobile");
            }
            if (jObject.has("head")) {
                this.head = jObject.getString("head");
            }
            if (jObject.has("user_id")) {
                this.userRealID = jObject.getString("user_id");
            }
            if (jObject.has("org_name")) {
                this.orgname = jObject.getString("org_name");
            }
            if (jObject.has("tenant_id")) {
                this.tenantid = jObject.getString("tenant_id");
            }
            if (jObject.has("tenant_name")) {
                this.tenantname = jObject.getString("tenant_name");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getUserName() {
        return userName;
    }

    public String getInspurID() {
        return inspurID;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getHead() {
        return head;
    }

    public String getUserRealID() {
        return userRealID;
    }

    public String getOrgname() {
        return orgname;
    }

    public String getTenantid() {
        return tenantid;
    }

    public String getTenantname() {
        return tenantname;
    }

}
