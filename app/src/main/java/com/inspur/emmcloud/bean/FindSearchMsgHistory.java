/**
 * 
 * FindSearchNews.java
 * classes : com.inspur.emmcloud.bean.FindSearchNews
 * V 1.0.0
 * Create at 2016年10月18日 下午6:07:58
 */
package com.inspur.emmcloud.bean;

import org.json.JSONObject;

/**
 * com.inspur.emmcloud.bean.FindSearchNews create at 2016年10月18日 下午6:07:58
 */
public class FindSearchMsgHistory {
	private String id = ""; 
	private String oid = ""; 
	private String userId = "";
	private String timestamp = "";
	private String body = ""; 
	private String userName = "";
	private String type = "";
	private String toName = "";
	private String channelFacet = "";
	private String toId = ""; 
	private String dataType = "";
	private String version_ = "";

	public FindSearchMsgHistory(JSONObject obj) {
		try {
			if (obj.has("id")) {
				id = obj.getString("id");
			}
			if (obj.has("oid")) {
				oid = obj.getString("oid");
			}
			if (obj.has("userid")) {
				userId = obj.getString("userid");
			}
			if (obj.has("timestamp")) {
				timestamp = obj.getString("timestamp");
			}
			if (obj.has("body")) {
				body = obj.getString("body");
			}
			if (obj.has("username")) {
				userName = obj.getString("username");
			}
			if (obj.has("type")) {
				type = obj.getString("type");
			}
			if (obj.has("toname")) {
				toName = obj.getString("toname");
			}
			if (obj.has("channel_facet")) {
				channelFacet = obj.getString("channel_facet");
			}
			if (obj.has("toid")) {
				toId = obj.getString("toid");
			}
			if (obj.has("datatype")) {
				dataType = obj.getString("datatype");
			}
			if (obj.has("version_")) {
				version_ = obj.getString("version_");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public String getId() {
		return id;
	};

	public String getOid() {
		return oid;
	};

	public String getUserId() {
		return userId;
	};

	public String getTimestamp() {
		return timestamp;
	};

	public String getBody() {
		return body;
	};

	public String getUserName() {
		return userName;
	};

	public String getType() {
		return type;
	};

	public String getToId() {
		return toId;
	};

	public String getToName() {
		return toName;
	};

	public String getChannelFacet() {
		return channelFacet;
	};


	public String getDataType() {
		return dataType;
	};


	public String getVersion() {
		return version_;
	};

}
