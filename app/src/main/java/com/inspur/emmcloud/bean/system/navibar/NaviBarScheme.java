package com.inspur.emmcloud.bean.system.navibar;

import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/4/12.
 */

public class NaviBarScheme {
    private String name = "";
    private String defaultTab = "";
    private List<MainTabResult> mainTabResultList = new ArrayList<>();

    public NaviBarScheme(String naviBarScheme) {
        this.name = JSONUtils.getString(naviBarScheme, "name", "");
        this.defaultTab = JSONUtils.getString(naviBarScheme, "defaultTab", "");
        JSONArray jsonArray = JSONUtils.getJSONArray(naviBarScheme, "tabs", new JSONArray());
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                mainTabResultList.add(new MainTabResult(JSONUtils.getJSONObject(jsonArray,i,new JSONObject())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultTab() {
        return defaultTab;
    }

    public void setDefaultTab(String defaultTab) {
        this.defaultTab = defaultTab;
    }

    public List<MainTabResult> getMainTabResultList() {
        return mainTabResultList;
    }

    public void setMainTabResultList(List<MainTabResult> mainTabResultList) {
        this.mainTabResultList = mainTabResultList;
    }
}
