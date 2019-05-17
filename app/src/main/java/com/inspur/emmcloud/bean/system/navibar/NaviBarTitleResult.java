package com.inspur.emmcloud.bean.system.navibar;

import com.inspur.emmcloud.util.common.JSONUtils;

/**
 * Created by yufuchang on 2019/4/12.
 */

public class NaviBarTitleResult {
    private String zhHans;
    private String zhHant;
    private String enUS;

    public NaviBarTitleResult() {
    }

    ;

    public NaviBarTitleResult(String response) {
        this.zhHans = JSONUtils.getString(response, "zh-Hans", "");
        this.zhHant = JSONUtils.getString(response, "zh-Hant", "");
        this.enUS = JSONUtils.getString(response, "en-US", "");
    }

    public String getZhHans() {
        return zhHans;
    }

    public void setZhHans(String zhHans) {
        this.zhHans = zhHans;
    }

    public String getZhHant() {
        return zhHant;
    }

    public void setZhHant(String zhHant) {
        this.zhHant = zhHant;
    }

    public String getEnUS() {
        return enUS;
    }

    public void setEnUS(String enUS) {
        this.enUS = enUS;
    }

    public String getTabTileByLanguage(String language) {
        String title = null;
        switch (language.toLowerCase()) {
            case "zh-hant":
                title = getZhHant();
                break;
            case "en":
            case "en-us":
                title = getEnUS();
                break;
            default:
                title = getZhHans();
                break;
        }
        return title;
    }
}
