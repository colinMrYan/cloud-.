package com.inspur.emmcloud.news.bean;

import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONObject;

import java.io.Serializable;

public class GroupNews implements Serializable {

    private String author = "";
    private String category = "";
    private String digest = "";
    private String needpush = "";
    private String nid = "";
    private String posttime = "0";
    private String publisher = "";
    private String title = "";
    private String url = "";
    private String poster = "";
    private boolean important = false;
    private String summary = "";
    private String creationDate = "";
    private String resource = "";
    private String id = "";
    private boolean hasExtraPermission = false;
    private String editorComment = "";
    private String approvedDate = "";
    private boolean editorCommentCreated = false;
    private String originalEditorComment = "";

    public GroupNews() {
    }

    public GroupNews(JSONObject jsonObject) {
        try {
            if (jsonObject.has("id")) {
                this.id = jsonObject.getString("id");
            }
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
//			if (jsonObject.has("resource")) {
//				this.url = jsonObject.getString("resource");
//			}
            if (jsonObject.has("resource")) {
                this.resource = jsonObject.getString("resource");
            }
            if (jsonObject.has("poster")) {
                this.poster = jsonObject.getString("poster");
            }
            if (jsonObject.has("editorComment")) {
                if (!StringUtils.isBlank(jsonObject.getString("editorComment"))) {
                    this.editorComment = jsonObject.getString("editorComment");
                    this.important = true;
                }
            }
            if (jsonObject.has("summary")) {
                this.summary = jsonObject.getString("summary");
            }
            if (jsonObject.has("creationDate")) {
                this.creationDate = jsonObject.getString("creationDate");
            }
            if (jsonObject.has("hasExtraPermission")) {
                this.hasExtraPermission = jsonObject.getBoolean("hasExtraPermission");
            }
            if (jsonObject.has("approvedDate")) {
                this.approvedDate = jsonObject.getString("approvedDate");
            }
            if (jsonObject.has("editorCommentCreated")) {
                this.editorCommentCreated = jsonObject.getBoolean("editorCommentCreated");
            }
            if (jsonObject.has("originalEditorComment")) {
                this.originalEditorComment = jsonObject.getString("originalEditorComment");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public void setDigest(String digest) {
        this.digest = digest;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEditorComment() {
        return editorComment;
    }

    public void setEditorComment(String editorComment) {
        this.editorComment = editorComment;
    }


//	public String getUrl() {
//		return url;
//	}

    public String getPoster() {
        if (poster != null) {
            poster = poster.trim();
        } else {
            poster = "";
        }
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public boolean isImportant() {
        return important;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHasExtraPermission() {
        return hasExtraPermission;
    }

    public void setHasExtraPermission(boolean hasExtraPermission) {
        this.hasExtraPermission = hasExtraPermission;
    }

    public String getOriginalEditorComment() {
        return originalEditorComment;
    }

    public void setOriginalEditorComment(String originalEditorComment) {
        this.originalEditorComment = originalEditorComment;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(String approvedDate) {
        this.approvedDate = approvedDate;
    }

    public boolean isEditorCommentCreated() {
        return editorCommentCreated;
    }

    public void setEditorCommentCreated(boolean editorCommentCreated) {
        this.editorCommentCreated = editorCommentCreated;
    }
}
