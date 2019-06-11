package com.inspur.emmcloud.bean.schedule.task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

//数据
//[
//		{
//		"id": "ATM:ac99b3e1faed458fabad28d46a563d71",
//		"name": "magazine-unlock-04-2.3.773-_f54546e2fd724d73b51bd846e80198fd.jpg",
//		"uri": "NVRJ8ZKWNPI.jpg",
//		"category": "IMAGE",
//		"type": "JPEG"
//		}
//		]
public class Attachment implements Serializable {

    private String id = "";
    private String name = "";
    private String uri = "";
    private String category = "";
    private String type = "";

    public Attachment(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
//			JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            if (jsonObject.has("id")) {
                this.id = jsonObject.getString("id");
            }
            if (jsonObject.has("name")) {
                this.name = jsonObject.getString("name");
            }
            if (jsonObject.has("id")) {
                this.uri = jsonObject.getString("uri");
            }
            if (jsonObject.has("id")) {
                this.category = jsonObject.getString("category");
            }
            if (jsonObject.has("type")) {
                this.type = jsonObject.getString("type");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    public Attachment(JSONObject jsonObject) {
        try {
            if (jsonObject.has("id")) {
                this.id = jsonObject.getString("id");
            }
            if (jsonObject.has("name")) {
                this.name = jsonObject.getString("name");
            }
            if (jsonObject.has("id")) {
                this.uri = jsonObject.getString("uri");
            }
            if (jsonObject.has("id")) {
                this.category = jsonObject.getString("category");
            }
            if (jsonObject.has("type")) {
                this.type = jsonObject.getString("type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
