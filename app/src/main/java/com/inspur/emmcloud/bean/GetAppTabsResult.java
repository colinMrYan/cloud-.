package com.inspur.emmcloud.bean; 

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;



/**
 * classes : com.inspur.emmcloud.bean.GetAppTabsResult
 * Create at 2016年12月13日 下午3:29:05
 */
public class GetAppTabsResult {

	private ArrayList<AppTabBean> appTabBeanList = new ArrayList<AppTabBean>();
	public GetAppTabsResult(String response){
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				AppTabBean appTabBean = new AppTabBean(jsonObject.toString());
				appTabBeanList.add(appTabBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<AppTabBean> getAppTabBeanList() {
		return appTabBeanList;
	}
	
	public void setAppTabBeanList(ArrayList<AppTabBean> appTabBeanList) {
		this.appTabBeanList = appTabBeanList;
	}
}
 