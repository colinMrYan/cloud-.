package com.inspur.emmcloud.basemodule.app.maintab;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/7/12.
 * <p>
 * <p>
 * <p>
 * {
 * "icon": "communicate",
 * "mainTabProperty": {
 * "canContact": true,
 * "canCreate": true,
 * "haveNavbar": false,
 * "mainTabMenuList": [],
 * "mineLayoutItemGroupList": []
 * },
 * "mainTabTitleResult": {
 * "enUS": "Messages",
 * "zhHans": "沟通",
 * "zhHant": "溝通"
 * },
 * "name": "communicate",
 * "selected": false,
 * "type": "native",
 * "uri": "native://communicate"
 * }
 * <p>
 * {
 * "icon": "work",
 * "mainTabProperty": {
 * "canContact": true,
 * "canCreate": true,
 * "haveNavbar": false,
 * "mainTabMenuList": [],
 * "mineLayoutItemGroupList": []
 * },
 * "mainTabTitleResult": {
 * "enUS": "Works",
 * "zhHans": "工作",
 * "zhHant": "工作"
 * },
 * "name": "work",
 * "selected": false,
 * "type": "native",
 * "uri": "native://work"
 * }
 * <p>
 * <p>
 * {
 * "icon": "application",
 * "mainTabProperty": {
 * "canContact": true,
 * "canCreate": true,
 * "haveNavbar": false,
 * "mainTabMenuList": [],
 * "mineLayoutItemGroupList": []
 * },
 * "mainTabTitleResult": {
 * "enUS": "Apps",
 * "zhHans": "应用",
 * "zhHant": "應用"
 * },
 * "name": "application",
 * "selected": true,
 * "type": "native",
 * "uri": "native://application"
 * }
 * <p>
 * {
 * "icon": "moment",
 * "mainTabProperty": {
 * "canContact": true,
 * "canCreate": true,
 * "haveNavbar": true,
 * "mainTabMenuList": [{
 * "action": "imp.iWindow.open({url: 'http://sns.ecm1.inspuronline.com/app/sns/web/m/home/index.html?spaceID=fd325341-e550-41bb-b464-17b5f22140b4#/Mypage'})",
 * "ico": "https://www.inspuronline.com/yjapp/images/sf.png",
 * "text": ""
 * }],
 * "mineLayoutItemGroupList": []
 * },
 * "mainTabTitleResult": {
 * "enUS": "Moments",
 * "zhHans": "动态",
 * "zhHant": "動態"
 * },
 * "name": "moment",
 * "selected": false,
 * "type": "web",
 * "uri": "http://sns.ecm1.inspuronline.com/app/sns/web/m/home/index.html?spaceID=fd325341-e550-41bb-b464-17b5f22140b4"
 * }
 * <p>
 * {
 * "icon": "me",
 * "mainTabProperty": {
 * "canContact": true,
 * "canCreate": true,
 * "haveNavbar": false,
 * "mainTabMenuList": [],
 * "mineLayoutItemGroupList": [{
 * "mineLayoutItemList": ["my_personalInfo_function"]
 * }, {
 * "mineLayoutItemList": ["my_setting_function"]
 * }, {
 * "mineLayoutItemList": ["my_cardbox_function"]
 * }, {
 * "mineLayoutItemList": ["my_aboutUs_function"]
 * }]
 * },
 * "mainTabTitleResult": {
 * "enUS": "Me",
 * "zhHans": "我",
 * "zhHant": "我"
 * },
 * "name": "me",
 * "selected": false,
 * "type": "native",
 * "uri": "native://me"
 * }
 */

public class MainTabResult {
    private String name;
    private String type;
    private String icon;
    private String uri;
    private boolean selected;
    private MainTabTitleResult mainTabTitleResult;
    private MainTabProperty mainTabProperty;

    public MainTabResult() {
    }

    public MainTabResult(JSONObject jsonObject) {
        this.name = JSONUtils.getString(jsonObject, "name", "");
        this.type = JSONUtils.getString(jsonObject, "type", "");
        this.icon = JSONUtils.getString(jsonObject, "ico", "");
        this.uri = JSONUtils.getString(jsonObject, "uri", "");
        this.selected = JSONUtils.getBoolean(jsonObject, "selected", false);
        this.mainTabTitleResult = new MainTabTitleResult(JSONUtils.getString(jsonObject, "title", ""));
        this.mainTabProperty = new MainTabProperty(JSONUtils.getString(jsonObject, "properties", ""));
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
