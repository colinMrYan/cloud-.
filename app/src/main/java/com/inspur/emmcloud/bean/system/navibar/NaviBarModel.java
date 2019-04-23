package com.inspur.emmcloud.bean.system.navibar;

/**
 * Created by yufuchang on 2019/4/12.
 */
/*{
        "command": "FORWARD",
        "version": "v1.0.1",
        "payload": {
        "schemes": [{
        "name": "standard",
        "tabs": [{
        "ico": "communicate",
        "uri": "native://communicate",
        "name": "communicate",
        "type": "native",
        "title": {
        "en-US": "Messages",
        "zh-Hans": "沟通",
        "zh-Hant": "溝通"
        },
        "selected": false,
        "properties": {
        "canOpenContact": true,
        "canCreateChannel": true
        }
        }, {
        "ico": "application",
        "uri": "native://application",
        "name": "application",
        "type": "native",
        "title": {
        "en-US": "Apps",
        "zh-Hans": "应用",
        "zh-Hant": "應用"
        },
        "selected": true
        }, {
        "ico": "me",
        "uri": "native://me",
        "name": "me",
        "type": "native",
        "title": {
        "en-US": "Me",
        "zh-Hans": "我",
        "zh-Hant": "我"
        },
        "selected": false,
        "properties": {
        "tablist": [
        ["my_personalInfo_function"],
        ["my_setting_function"],
        ["my_cardbox_function"],
        ["my_aboutUs_function"]
        ]
        }
        }],
        "title": {
        "en-US": "Standard Layout",
        "zh-Hans": "标准布局"
        },
        "defaultTab": "application"
        }, {
        "name": "socailized",
        "tabs": [{
        "ico": "communicate",
        "uri": "native://communicate",
        "name": "communicate",
        "type": "native",
        "title": {
        "en-US": "Messages",
        "zh-Hans": "沟通",
        "zh-Hant": "溝通"
        },
        "selected": false,
        "properties": {
        "canOpenContact": true,
        "canCreateChannel": true
        }
        }, {
        "ico": "application",
        "uri": "native://application",
        "name": "application",
        "type": "native",
        "title": {
        "en-US": "Apps",
        "zh-Hans": "应用",
        "zh-Hant": "應用"
        },
        "selected": true
        }, {
        "ico": "contact",
        "uri": "native://contact",
        "name": "contact",
        "type": "native",
        "title": {
        "en-US": "Contacts",
        "zh-Hans": "通讯录",
        "zh-Hant": "通訊錄"
        },
        "selected": false
        }, {
        "ico": "me",
        "uri": "native://me",
        "name": "me",
        "type": "native",
        "title": {
        "en-US": "Me",
        "zh-Hans": "我",
        "zh-Hant": "我"
        },
        "selected": false,
        "properties": {
        "tablist": [
        ["my_personalInfo_function"],
        ["my_setting_function"],
        ["my_cardbox_function"],
        ["my_aboutUs_function"]
        ]
        }
        }],
        "title": {
        "en-US": "Socailized Layout",
        "zh-Hans": "社交布局"
        }
        }],
        "defaultScheme": "standard"
        }
        }*/
public class NaviBarModel {
    private String response = "";
    private NaviBarPayload naviBarPayload;
    public NaviBarModel(String response){
        this.response = response;
//        this.command = JSONUtils.getString(response,"command","");
//        this.version = JSONUtils.getString(response,"version","");
        this.naviBarPayload = new NaviBarPayload(response);
    }


    public NaviBarPayload getNaviBarPayload() {
        return naviBarPayload;
    }

    public void setNaviBarPayload(NaviBarPayload naviBarPayload) {
        this.naviBarPayload = naviBarPayload;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
