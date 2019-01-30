package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/7/31.
 */

public class MineLayoutItem {
    private String id="";
    private String ico = "";
    private String uri="";
    private String title="";
    public MineLayoutItem(String content){
        boolean isJSonObject = JSONUtils.isJSONObject(content);
        if (isJSonObject){
            JSONObject object = JSONUtils.getJSONObject(content);
            id=JSONUtils.getString(object,"id","");
            ico=JSONUtils.getString(object,"ico","");
            uri=JSONUtils.getString(object,"uri","");
            title=JSONUtils.getString(object,"title","");
        }else {
            id=content;
        }
    }

    public MineLayoutItem(String id,String ico,String uri,String title){
        this.id=id;
        this.ico = ico;
        this.uri = uri;
        this.title = title;
    }

    public MineLayoutItem(){

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleByLanguage(){
        String  TitleByLanguage = JSONUtils.getString(title,)
    }
}
