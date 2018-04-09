package com.inspur.emmcloud.bean.system;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/4/26.
 */

public class AppTabAutoBean {

    /**
     * id : {"namespace":"com.inspur.ecc.core.preferences","domain":"main-tab","version":"v1.0.0"}
     * command : FORWARD
     * payload : {"version":"v1.0.0","name":"云+tabbar","state":"PENDING","creationDate":1493186738081,"selected":"application","tabs":[{"id":1,"key":"application","component":"main-tab","icon":"hello-app","selected":true,"title":{"zh-Hans":"应用","zh-Hant":"應用","en-US":"App"}}]}
     */

    private AppTabIdBean id;
    private String command = "";
    private AppTabPayloadBean payload;

    public AppTabAutoBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("id")) {
                id = new AppTabIdBean(jsonObject.getString("id"));
            }
            if (jsonObject.has("payload")) {
                payload = new AppTabPayloadBean(jsonObject.getString("payload"));
            }
            if (jsonObject.has("command")) {
                this.command = jsonObject.getString("command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AppTabIdBean getId() {
        return id;
    }

    public void setId(AppTabIdBean id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public AppTabPayloadBean getPayload() {
        return payload;
    }

    public void setPayload(AppTabPayloadBean payload) {
        this.payload = payload;
    }

}
