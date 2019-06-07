/**
 * Language.java
 * classes : com.inspur.emmcloud.basemodule.bean.Language
 * V 1.0.0
 * Create at 2016年10月9日 下午5:17:46
 */
package com.inspur.emmcloud.basemodule.bean;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * com.inspur.emmcloud.basemodule.bean.Language create at 2016年10月9日 下午5:17:46
 */
public class Language implements Serializable {
    // {"label":"中文简体","iana":"zh-Hans","iso":"zh-CN","gsp":"zh-Hans","gsp60":"zh-Hans","gsp61":"zh-Hans"}
    private String label = "";
    private String iso = "";
    private String iana = "";
    private String gsp = "";
    private String gsp60 = "";
    private String gsp61 = "";

    public Language() {

    }

    public Language(String label, String iso, String iana, String gsp, String gsp60, String gsp61) {
        this.label = label;
        this.iso = iso;
        this.iana = iana;
        this.gsp = gsp;
        this.gsp60 = gsp60;
        this.gsp61 = gsp61;
    }

    public Language(JSONObject obj) {
        try {
            if (obj.has("label")) {
                this.label = obj.getString("label");
            }
            if (obj.has("iso")) {
                this.iso = obj.getString("iso");
            }
            if (obj.has("iana")) {
                this.iana = obj.getString("iana");
            }
            if (obj.has("gsp")) {
                this.gsp = obj.getString("gsp");
            }
            if (obj.has("gsp60")) {
                this.gsp60 = obj.getString("gsp60");
            }
            if (obj.has("gsp61")) {
                this.gsp61 = obj.getString("gsp61");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public Language(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has("label")) {
                this.label = obj.getString("label");
            }
            if (obj.has("iso")) {
                this.iso = obj.getString("iso");
            }
            if (obj.has("iana")) {
                this.iana = obj.getString("iana");
            }
            if (obj.has("gsp")) {
                this.gsp = obj.getString("gsp");
            }
            if (obj.has("gsp60")) {
                this.gsp60 = obj.getString("gsp60");
            }
            if (obj.has("gsp61")) {
                this.gsp61 = obj.getString("gsp61");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getGsp() {
        return gsp;
    }

    public void setGsp(String gsp) {
        this.gsp = gsp;
    }

    public String getIana() {
        return iana;
    }

    public void setIana(String iana) {
        this.iana = iana;
    }

    public String getGsp60() {
        return gsp60;
    }

    public void setGsp60(String gsp60) {
        this.gsp60 = gsp60;
    }

    public String getGsp61() {
        return gsp61;
    }

    public void setGsp61(String gsp61) {
        this.gsp61 = gsp61;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        String json = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("label", label);
            jsonObject.put("iso", iso);
            jsonObject.put("iana", iana);
            jsonObject.put("gsp", gsp);
            jsonObject.put("gsp60", gsp60);
            jsonObject.put("gsp61", gsp61);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        json = jsonObject.toString();
        return json;
    }

}
