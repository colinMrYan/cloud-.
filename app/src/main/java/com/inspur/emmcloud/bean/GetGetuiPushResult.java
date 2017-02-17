package com.inspur.emmcloud.bean;

import org.json.JSONException;
import org.json.JSONObject;



import android.nfc.Tag;

public class GetGetuiPushResult {

	private static final String  TAG = "GetGetuiPush";
	private String type = "";
	private String title = "";
	private String body = "";
	private String uri = "";
	
	public GetGetuiPushResult(String response){
		

		

		
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(response);
			
			if (jsonObject.has("type")) {
				this.type = jsonObject.getString("type");
			}
			if(jsonObject.has("title")){
				this.title = jsonObject.getString("title");
			}
			if(jsonObject.has("body")){
				this.body = jsonObject.getString("body");
				JSONObject bodyJsonObject = new JSONObject(body);
				if(bodyJsonObject.has("uri")){
					this.uri = bodyJsonObject.getString("uri");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	private String toJson(String reponse){
		
		String[] arg = reponse.split(",");
		for (int i = 0; i < arg.length; i++) {
			
		}
		return null;
	}
}
