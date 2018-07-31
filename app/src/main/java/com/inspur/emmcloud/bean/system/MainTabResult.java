package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/7/12.
 */

public class MainTabResult {
    private String name;
    private String type;
    private String icon;
    private String uri;
    private boolean selected;
    private MainTabTitleResult mainTabTitleResult;
    private MainTabProperty mainTabProperty;
    public MainTabResult(JSONObject jsonObject){
        this.name = JSONUtils.getString(jsonObject,"name","");
        this.type = JSONUtils.getString(jsonObject,"type","");
        this.icon = JSONUtils.getString(jsonObject,"ico","");
        this.uri = JSONUtils.getString(jsonObject,"uri","");
        this.selected = JSONUtils.getBoolean(jsonObject,"selected",false);
        this.mainTabTitleResult = new MainTabTitleResult(JSONUtils.getString(jsonObject,"title",""));
        this.mainTabProperty = new MainTabProperty(JSONUtils.getString(jsonObject,"properties",""));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public MainTabTitleResult getMainTabTitleResult() {
        return mainTabTitleResult;
    }

    public void setMainTabTitleResult(MainTabTitleResult mainTabTitleResult) {
        this.mainTabTitleResult = mainTabTitleResult;
    }

    public MainTabProperty getMainTabProperty() {
        return mainTabProperty;
    }

    public void setMainTabProperty(MainTabProperty mainTabProperty) {
        this.mainTabProperty = mainTabProperty;
    }
}
