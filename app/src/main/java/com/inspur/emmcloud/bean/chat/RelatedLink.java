package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/4/29.
 */

public class RelatedLink {
    private String poster;
    private String title;
    private String url;

    public RelatedLink(JSONObject obj) {
        poster = JSONUtils.getString(obj, "poster", "");
        title = JSONUtils.getString(obj, "title", "");
        url = JSONUtils.getString(obj, "url", "");
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject toJSonObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("poster", poster);
            object.put("title", title);
            object.put("url", url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }
}
