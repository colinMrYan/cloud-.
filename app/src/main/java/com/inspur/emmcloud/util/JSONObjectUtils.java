package com.inspur.emmcloud.util;


public class JSONObjectUtils {

	private String jsonRaw = "";
	public JSONObjectUtils(String response) {
		this.jsonRaw = response;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return JSONUtils.getString(jsonRaw, key, "");
	}
}
