package com.inspur.emmcloud.componentservice.application.maintab;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/7/31.
 */

public class MineLayoutItem {
    private String id = "";
    private String ico = "";
    private String uri = "";
    private String title = "";

    public MineLayoutItem(String content) {
        boolean isJSonObject = JSONUtils.isJSONObject(content);
        if (isJSonObject) {
            JSONObject object = JSONUtils.getJSONObject(content);
            id = JSONUtils.getString(object, "id", "");
            ico = JSONUtils.getString(object, "ico", "");
            uri = JSONUtils.getString(object, "uri", "");
            title = JSONUtils.getString(object, "title", "");
        } else {
            id = content;
        }
    }

    public MineLayoutItem(String id, String ico, String uri, String title) {
        this.id = id;
        this.ico = ico;
        this.uri = uri;
        this.title = title;
    }

    public MineLayoutItem() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle(String language) {
        String titleByLanguage = "";
        if (!StringUtils.isBlank(title)) {
            switch (language) {
                case "zh-Hant":
                    titleByLanguage = JSONUtils.getString(title, "zh-Hans", "");
                    break;
                case "en-US":
                case "en":
                    titleByLanguage = JSONUtils.getString(title, "en-US", "");
                    break;
                default:
                    titleByLanguage = JSONUtils.getString(title, "zh-Hans", "");
                    break;

            }
        }
        return titleByLanguage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
