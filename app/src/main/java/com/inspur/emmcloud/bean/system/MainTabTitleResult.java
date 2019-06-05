package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/7/12.
 */

public class MainTabTitleResult {
    private String zhHans;
    private String zhHant;
    private String enUS;

    public MainTabTitleResult() {
    }

    ;

    public MainTabTitleResult(String response) {
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
