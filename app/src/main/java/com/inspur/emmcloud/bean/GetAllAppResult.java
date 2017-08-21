package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取应用中心所有应用数据解析类
 * 
 * @author Administrator
 *
 */
public class GetAllAppResult {
	private static final String TAG = "GetAllAppResult";
	private List<AppAdsBean> adsList = new ArrayList<AppAdsBean>();
	private List<App> hotRecommendList = new ArrayList<App>();
	private List<App> classicalRecommendList = new ArrayList<App>();
	private List<AppGroupBean> categoriesGroupBeanList = new ArrayList<AppGroupBean>();
	
	public GetAllAppResult(String response) {

		try {
//			JSONArray array = JSONUtils.getJSONArray(response, "recommend",
//					new JSONArray());
//			for (int i = 0; i < array.length(); i++) {
//				JSONObject obj = array.getJSONObject(i);
//				recommandAppList.add(new App(obj));
//			}
			JSONArray hotRecommendArray = JSONUtils.getJSONArray(response,"hotRecommend",new JSONArray());
			for(int i = 0; i < hotRecommendArray.length(); i++){
				LogUtils.YfcDebug("hotRecommend："+hotRecommendArray.getJSONObject(i));
				JSONObject obj = hotRecommendArray.getJSONObject(i);
				hotRecommendList.add(new App(obj));
			}
			JSONArray adsArray = JSONUtils.getJSONArray(response,"ads",new JSONArray());
			for(int i = 0; i < adsArray.length(); i++){
				LogUtils.YfcDebug("ads："+adsArray.getJSONObject(i));
				JSONObject obj = adsArray.getJSONObject(i);
				adsList.add(new AppAdsBean(obj));
			}

			JSONArray classicalRecommendArray = JSONUtils.getJSONArray(response,"classicalRecommend",new JSONArray());
			for(int i = 0; i < classicalRecommendArray.length(); i++){
				LogUtils.YfcDebug("classicalRecommend："+classicalRecommendArray.getJSONObject(i));
				JSONObject obj = classicalRecommendArray.getJSONObject(i);
				classicalRecommendList.add(new App(obj));
			}
			JSONArray arrayCategories = JSONUtils.getJSONArray(response, "categories", new JSONArray());
			for (int i = 0; i < arrayCategories.length(); i++) {
				JSONObject obj = arrayCategories.getJSONObject(i);
				LogUtils.YfcDebug("应用中的一个分组："+obj);
				AppGroupBean groupBean = new AppGroupBean(obj);
				categoriesGroupBeanList.add(groupBean);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<AppAdsBean> getAdsList() {
		return adsList;
	}

	public List<App> getHotRecommendList() {
		return hotRecommendList;
	}

	public List<App> getClassicalRecommendList() {
		return classicalRecommendList;
	}

	public List<AppGroupBean> getCategoriesGroupBeanList() {
		return categoriesGroupBeanList;
	}

}