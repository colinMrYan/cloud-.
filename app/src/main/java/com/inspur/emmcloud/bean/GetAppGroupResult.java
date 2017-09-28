package com.inspur.emmcloud.bean; 

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

/**
 * classes : com.inspur.emmcloud.bean.GetAppGroupResult
 * Create at 2016年12月14日 下午7:49:58
 */
public class GetAppGroupResult {

	private List<AppGroupBean> appGroupBeanList = new ArrayList<AppGroupBean>();
	public GetAppGroupResult(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				AppGroupBean appGroupBean = new AppGroupBean(jsonArray.getJSONObject(i).toString());
				appGroupBeanList.add(appGroupBean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public List<AppGroupBean> getAppGroupBeanList() {
		return appGroupBeanList;
	}
}
 