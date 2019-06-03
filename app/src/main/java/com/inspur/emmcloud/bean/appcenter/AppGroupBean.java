package com.inspur.emmcloud.bean.appcenter;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * classes : com.inspur.emmcloud.bean.appcenter.AppGroupBean Create at 2016年12月14日
 * 下午5:36:46
 */
public class AppGroupBean {

    private String categoryID = "";
    private String categoryName = "";
    private List<App> appItemList = new ArrayList<App>();
    private String categoryIco = "";

    public AppGroupBean() {
    }

    public AppGroupBean(String response) {
        this(JSONUtils.getJSONObject(response));
    }

    public AppGroupBean(JSONObject jsonObject) {
        categoryID = JSONUtils.getString(jsonObject, "categoryID", "");
        categoryName = JSONUtils.getString(jsonObject, "categoryName", "");
        if (jsonObject.has("appList")) {
            JSONArray jsonArray = JSONUtils.getJSONArray(jsonObject, "appList", new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                App app = new App(JSONUtils.getJSONObject(jsonArray, i, new JSONObject()));
                app.setCategoryID(categoryID);
                app.setOrderId(1000);
                appItemList.add(app);
            }
        }
        categoryIco = JSONUtils.getString(jsonObject, "category_ico", "");
        if (jsonObject.has("groupList")) {
            JSONArray jsonArray = JSONUtils.getJSONArray(jsonObject, "groupList", new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = JSONUtils.getJSONObject(jsonArray, i, new JSONObject());
                App app = new App(obj);
                app.setCategoryID(categoryID);
                app.setOrderId(1000);
                app.setAppName(JSONUtils.getString(obj, "name", ""));
                appItemList.add(app);
            }
        }
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

//	public List<App> getAppList() {
//		return appList;
//	}

    public List<App> getAppItemList() {
        return appItemList;
    }

    public void setAppItemList(List<App> appItemList) {
        this.appItemList = appItemList;
    }

    public String getCategoryIco() {
        return categoryIco;
    }

    public void setCategoryIco(String categoryIco) {
        this.categoryIco = categoryIco;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof AppGroupBean)) {
            return false;
        }
        AppGroupBean appGroupBean = (AppGroupBean) other;
        //此处从==判断是否相等  改为equals
        return getCategoryID().equals(appGroupBean.getCategoryID());
    }
}
