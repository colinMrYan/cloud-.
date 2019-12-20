package com.inspur.emmcloud.application.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 获取应用中心所有应用数据解析类
 *
 * @author Administrator
 */
public class GetAllAppResult {
    private List<AppAdsBean> adsList = new ArrayList<AppAdsBean>();
    private List<AppGroupBean> categoriesGroupBeanList = new ArrayList<AppGroupBean>();
    private List<List<App>> recommendList = new ArrayList<>();

    public GetAllAppResult(String response) {
        try {
            //分类
            JSONArray arrayCategories = JSONUtils.getJSONArray(response, "categories", new JSONArray());
            for (int i = 0; i < arrayCategories.length(); i++) {
                JSONObject obj = arrayCategories.getJSONObject(i);
                AppGroupBean groupBean = new AppGroupBean(obj);
                categoriesGroupBeanList.add(groupBean);
            }
            //广告
            JSONArray adsArray = JSONUtils.getJSONArray(response, "ads", new JSONArray());
            for (int i = 0; i < adsArray.length(); i++) {
                JSONObject obj = adsArray.getJSONObject(i);
                adsList.add(new AppAdsBean(obj));
            }
            //推荐
            JSONObject recommendJsonObj = JSONUtils.getJSONObject(response, "recommend", new JSONObject());
            if (recommendJsonObj != null) {
                Iterator<String> keySet = recommendJsonObj.keys();
                while (keySet.hasNext()) {
                    String key = keySet.next();
                    JSONArray jsonArray = recommendJsonObj.getJSONArray(key);
                    if (jsonArray != null && (jsonArray.length() > 0)) {
                        List<App> appList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            App app = new App(obj);
                            appList.add(app);
                        }
                        if (appList.size() > 0) {
                            recommendList.add(appList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<AppAdsBean> getAdsList() {
        return adsList;
    }

    public List<List<App>> getRecommendList() {
        return recommendList;
    }

    public List<AppGroupBean> getCategoriesGroupBeanList() {
        return categoriesGroupBeanList;
    }

}