package com.inspur.emmcloud.bean.appcenter.news;

/**
 * Created by yufuchang on 2017/5/11.
 */

public class NewsIntrcutionUpdateEvent {
    private String id;
    private boolean editorCommentCreated = false;
    private String originalEditorComment = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEditorCommentCreated() {
        return editorCommentCreated;
    }

    public void setEditorCommentCreated(boolean editorCommentCreated) {
        this.editorCommentCreated = editorCommentCreated;
    }

    public String getOriginalEditorComment() {
        return originalEditorComment;
    }

    public void setOriginalEditorComment(String originalEditorComment) {
        this.originalEditorComment = originalEditorComment;
    }
}
