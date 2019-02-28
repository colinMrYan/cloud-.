package com.inspur.emmcloud.bean.work;

import org.json.JSONObject;

public class GetIsAdmin {

    private boolean isAdmin = false;

    public GetIsAdmin(String response) {
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(response);
            if (jsonObject.has("isAdmin")) {
                this.isAdmin = jsonObject.getBoolean("isAdmin");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

}
