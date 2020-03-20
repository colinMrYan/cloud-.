package com.inspur.emmcloud.componentservice.application.maintab;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/7/12.
 */

public class MainTabTitleResult {
    private String zhHans;
    private String zhHant;
    private String enUS;

    private String headerZhHans;
    private String headerZhHant;
    private String headerEnUs;

    public MainTabTitleResult() {
    }

    ;

    public MainTabTitleResult(String response) {
        this.zhHans = JSONUtils.getString(response, "zh-Hans", "");
        this.zhHant = JSONUtils.getString(response, "zh-Hant", "");
        this.enUS = JSONUtils.getString(response, "en-US", "");

        this.headerEnUs = JSONUtils.getString(response,"header-zh-US","");
        this.headerZhHant = JSONUtils.getString(response,"header-zh-Hant","");
        this.headerZhHans = JSONUtils.getString(response,"header-zh-Hans","");
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

    public String getHeaderZhHans() {
        return headerZhHans;
    }

    public void setHeaderZhHans(String headerZhHans) {
        this.headerZhHans = headerZhHans;
    }

    public String getHeaderZhHant() {
        return headerZhHant;
    }

    public void setHeaderZhHant(String headerZhHant) {
        this.headerZhHant = headerZhHant;
    }

    public String getHeaderEnUs() {
        return headerEnUs;
    }

    public void setHeaderEnUs(String headerEnUs) {
        this.headerEnUs = headerEnUs;
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
