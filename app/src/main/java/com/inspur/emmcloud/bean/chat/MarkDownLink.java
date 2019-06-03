package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by libaochao on 2019/5/22.
 */
@Table(name = "MarkDownLink")
public class MarkDownLink implements Serializable {
    @Column(name = "id",isId = true)
    private String id;
    @Column(name = "mid")
    private String mid;
    @Column(name = "link")
    private String link;

    public MarkDownLink(){}

    public MarkDownLink(String id ,String mid,String link){
        this.id=id;
        this.mid=mid;
        this.link=link;
    }

    public MarkDownLink(JSONObject obj){
        this.id = JSONUtils.getString(obj, "id", "");
        this.mid = JSONUtils.getString(obj, "mid", "");
        this.link = JSONUtils.getString(obj, "link", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
