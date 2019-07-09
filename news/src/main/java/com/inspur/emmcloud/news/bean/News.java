package com.inspur.emmcloud.news.bean;

import org.json.JSONObject;

public class News {

    public static final String TAG = "News";
    private String nid = "";
    private String title = "";
    private String poster = "";
    private String time = "";
    private String digest = "";
    private String url = "";
    private String publisher = "";
    private String author = "";

    private String timestamp = "";
    private String to = "";
    private String body = "";
    private String posttime = "";
    private String from = "";
    private String avatar = "";
    private String uid = "";
    private String fromTitle = "";
    private String mid = "";
    private String type = "";


    public News(JSONObject jsonObject) {

        try {

            //公共部分
            if (jsonObject.has("timestamp")) {
                this.timestamp = jsonObject.getString("timestamp");
            }
            if (jsonObject.has("to")) {
                this.to = jsonObject.getString("to");
            }
            if (jsonObject.has("mid")) {
                this.mid = jsonObject.getString("mid");
            }
            if (jsonObject.has("type")) {
                this.type = jsonObject.getString("type");
            }

            //from部分
            if (jsonObject.has("from")) {
                String jsonfromString = jsonObject.getString("from");
                JSONObject jsonFrom = new JSONObject(jsonfromString);

                if (jsonFrom.has("avatar")) {
                    this.avatar = jsonFrom.getString("avatar");
                }

                if (jsonFrom.has("uid")) {
                    this.uid = jsonFrom.getString("uid");
                }

                if (jsonFrom.has("title")) {
                    this.fromTitle = jsonFrom.getString("title");
                }

            }

            //body部分
            if (jsonObject.has("body")) {

                String jsonBodyString = jsonObject.getString("body");
                JSONObject jsonBody = new JSONObject(jsonBodyString);

                if (jsonBody.has("author")) {
                    this.author = jsonBody.getString("author");
                }

                if (jsonBody.has("digest")) {
                    this.digest = jsonBody.getString("digest");
                }

                if (jsonBody.has("title")) {
                    this.title = jsonBody.getString("title");
                }

                if (jsonBody.has("posttime")) {
                    this.posttime = jsonBody.getString("posttime");
                }

                if (jsonBody.has("url")) {
                    this.url = jsonBody.getString("url");
                }

                if (jsonBody.has("nid")) {
                    this.nid = jsonBody.getString("nid");
                }
                if (jsonBody.has("publisher")) {
                    this.publisher = jsonBody.getString("publisher");
                }
            }
//			if(jsonObject.has("nid")){
//				nid = jsonObject.getString("nid");
//			}
//			if(jsonObject.has("title")){
//				title = jsonObject.getString("title");
//			}
//			if(jsonObject.has("poster")){
//				poster = jsonObject.getString("poster");
//			}
//			if(jsonObject.has("time")){
//				time = jsonObject.getString("time");
//			}
//			if(jsonObject.has("digest")){
//				digest = jsonObject.getString("digest");
//			}
//			if(jsonObject.has("url")){
//				url = jsonObject.getString("url");
//			}
//			if(jsonObject.has("publisher")){
//				publisher = jsonObject.getString("publisher");
//			}
//			if(jsonObject.has("author")){
//				author = jsonObject.getString("author");
//			}
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }

    public String getPosttime() {
        return posttime;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUid() {
        return uid;
    }

    public String getFromTitle() {
        return fromTitle;
    }

    public String getMid() {
        return mid;
    }

    public String getType() {
        return type;
    }


}
