package com.inspur.emmcloud.bean.chat;

import org.json.JSONObject;

public class GetWebSocketUrlResult {

    public static final String Tag = "GetWebSocketUrlResult";
    private String url = "";
    private String path = "";

    public GetWebSocketUrlResult(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("url")) {
                this.url = jsonObject.getString("url");
            }
            if (jsonObject.has("path")) {
                this.path = jsonObject.getString("path");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String getUrl() {
        if (url.startsWith("ws://")) {
            url = url.replaceFirst("ws://", "http://");
        } else if (url.startsWith("wss://")) {
            url = url.replaceFirst("wss://", "https://");
        }

        return url;
    }

    public String getPath() {
        return path;
    }

}
