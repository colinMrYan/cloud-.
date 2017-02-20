package com.inspur.emmcloud.bean;

import org.json.JSONObject;

public class GroupNews {

	private String author;
	private String category;
	private String digest;
	private String needpush;
	private String nid;
	private String posttime;
	private String publisher;
	private String title;
	private String url;
	private String poster;
	private boolean important;

	public GroupNews(JSONObject jsonObject) {

		try {

			if (jsonObject.has("author")) {
				this.author = jsonObject.getString("author");
			}
			if (jsonObject.has("title")) {
				this.title = jsonObject.getString("title");
			}
			if (jsonObject.has("category")) {
				this.category = jsonObject.getString("category");
			}
			if (jsonObject.has("digest")) {
				this.digest = jsonObject.getString("digest");
			}
			if (jsonObject.has("needpush")) {
				this.needpush = jsonObject.getString("needpush");
			}
			if (jsonObject.has("nid")) {
				this.nid = jsonObject.getString("nid");
			}
			if (jsonObject.has("posttime")) {
				this.posttime = jsonObject.getString("posttime");
			}
			if (jsonObject.has("publisher")) {
				this.publisher = jsonObject.getString("publisher");
			}
			if (jsonObject.has("url")) {
				this.url = jsonObject.getString("url");
			}
			if(jsonObject.has("poster")){
				this.poster = jsonObject.getString("poster");
			}
			if(jsonObject.has("important")){
				this.important = jsonObject.getBoolean("important");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getAuthor() {
		return author;
	}

	public String getCategory() {
		return category;
	}

	public String getDigest() {
		return digest;
	}

	public String getNeedpush() {
		return needpush;
	}

	public String getNid() {
		return nid;
	}

	public String getPosttime() {
		return posttime;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getPoster() {
		if(poster != null){
			poster = poster.trim();
		}else {
			poster = "";
		}
		return poster;
	}

	public boolean isImportant() {
		return important;
	}
	
	
}
