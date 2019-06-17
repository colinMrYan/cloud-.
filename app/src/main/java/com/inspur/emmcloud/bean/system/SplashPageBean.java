package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2017/5/18.
 */

public class SplashPageBean {
    /**
     * id : {"namespace":"com.inspur.ecc.core.preferences","domain":"launch-screen","version":"v1.0.0"}
     * command : FORWARD
     * payload : {"version":"v1.0.0","state":"ACTIVED","effectiveDate":1495393588000,"expireDate":1495825594000,"res1xHash":"1","res2xHash":"1","res3xHash":"1","mdpiHash":"1","hdpiHash":"1","xhdpiHash":"1","xxhdpiHash":"1","xxxhdpiHash":"1","resource":{"default":{"res1xHash":"1","mdpi":"IZHJ301KAYD.png","xxhdpi":"JQBJ301LZB0.png","hdpi":"UP7J301KYCA.png","xhdpi":"YQ4J301LFRX.png","mdpiHash":"1","res2x":"K3ZJ301JFB1.png","xxhdpiHash":"1","xxxhdpi":"U6PJ301MD1G.png","res1x":"CJBJ301IWU2.png","res3xHash":"1","hdpiHash":"1","res3x":"G0RJ301JU13.png","res2xHash":"1","xhdpiHash":"1","xxxhdpiHash":"1"}}}
     */

    private SplashIdBean id;
    private String command = "STANDBY";
    private SplashPayloadBean payload;
    private String response = "";

    public SplashPageBean(String response) {
        this.response = response;
        this.command = JSONUtils.getString(response, "command", "");
        String idBean = JSONUtils.getString(response, "id", "");
        String payLoadBean = JSONUtils.getString(response, "payload", "");
        this.id = new SplashIdBean(idBean);
        this.payload = new SplashPayloadBean(payLoadBean);
    }

    public SplashIdBean getId() {
        return id;
    }

    public void setId(SplashIdBean id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public SplashPayloadBean getPayload() {
        return payload;
    }

    public void setPayload(SplashPayloadBean payload) {
//        if(payload == null){
//            this.payload = new PayloadBean();
//        }
        this.payload = payload;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
