package com.inspur.emmcloud.bean.appcenter;

public class GetIDResult {
    private String id = "";

    public GetIDResult(String response) {
//        try {
//            JSONObject obj = new JSONObject(response);
//            if (obj.has("id")) {
//
//                id = obj.getString("id");
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        this.id = response.replaceAll("\"", "");
    }

    public String getId() {
        return id;
    }
}
