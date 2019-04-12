package com.inspur.emmcloud.bean.system.navibar;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2019/4/12.
 */

public class NaviBarPayload {
    private String defaultScheme = "";
    private List<NaviBarScheme> naviBarSchemeList = new ArrayList<>();

    public NaviBarPayload(String navibarPayload) {
        this.defaultScheme = JSONUtils.getString(navibarPayload, "defaultScheme", "");
        JSONArray jsonArray = JSONUtils.getJSONArray(navibarPayload, "schemes", new JSONArray());
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                naviBarSchemeList.add(new NaviBarScheme(jsonArray.getString(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDefaultScheme() {
        return defaultScheme;
    }

    public void setDefaultScheme(String defaultScheme) {
        this.defaultScheme = defaultScheme;
    }

    public List<NaviBarScheme> getNaviBarSchemeList() {
        return naviBarSchemeList;
    }

    public void setNaviBarSchemeList(List<NaviBarScheme> naviBarSchemeList) {
        this.naviBarSchemeList = naviBarSchemeList;
    }
}
