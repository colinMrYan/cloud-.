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
//	private List<List<AppItem>> appList = new ArrayList<List<AppItem>>();
	public GetAppGroupResult(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				AppGroupBean appGroupBean = new AppGroupBean(jsonArray.getJSONObject(i).toString());
				appGroupBeanList.add(appGroupBean);
			}
//			for (int i = 0; i < appGroupBeanList.size(); i++) {
//				int countApp = appGroupBeanList.get(i).getAppList().size();
//				List<App> appGroupList = appGroupBeanList.get(i).getAppList();
//				List<AppItem> appItemList = new ArrayList<AppItem>();
//				for (int j = 0; j < countApp; j++) {
//					AppItem appItem = new AppItem();
//					App app = appGroupList.get(j);
//					appItem.setAppID(app.getAppID());
//					appItem.setAppIcon(app.getAppIcon());
//					appItem.setAppName(app.getAppName());
//					appItem.setOrderId("1000");
//					appItem.setGroupId(appGroupBeanList.get(i).getCategoryID());
//					appItemList.add(appItem);
//				}
//				appList.add(appItemList);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public List<AppGroupBean> getAppGroupBeanList() {
		return appGroupBeanList;
	}
//	public List<List<AppItem>> getAppList() {
//		return appList;
//	}
}
 