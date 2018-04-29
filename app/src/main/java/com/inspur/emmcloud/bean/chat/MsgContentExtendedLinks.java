package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/4/29.
 */

public class MsgContentExtendedLinks {
    private String poster;
    private String title;
    private String subtitle;
    private String url;
    private List<RelatedLink> relatedLinkList = new ArrayList<>();

    public MsgContentExtendedLinks(String content) {
        poster = JSONUtils.getString(content, "poster", "");
        title = JSONUtils.getString(content, "title", "");
        subtitle = JSONUtils.getString(content, "subtitle", "");
        url = JSONUtils.getString(content, "url", "");
        JSONArray array = JSONUtils.getJSONArray(content, "relatedLinks", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            RelatedLink relatedLink = new RelatedLink(JSONUtils.getJSONObject(array, i, new JSONObject()));
            relatedLinkList.add(relatedLink);
        }
    }

    public MsgContentExtendedLinks() {
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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<RelatedLink> getRelatedLinkList() {
        return relatedLinkList;
    }

    public void setRelatedLinkList(List<RelatedLink> relatedLinkList) {
        this.relatedLinkList = relatedLinkList;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("poster", poster);
            obj.put("title", title);
            obj.put("subtitle", subtitle);
            obj.put("url", url);
            JSONArray array = new JSONArray();
            for (RelatedLink relatedLink:relatedLinkList){
                array.put(relatedLink.toJSonObject());
            }
            obj.put("relatedLinks", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
