package com.inspur.emmcloud.bean.appcenter.mail;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by chenmch on 2018/12/24.
 */
@Table(name = "MailFolder")
public class MailFolder {
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "totalCount")
    private int totalCount;
    @Column(name = "unreadCount")
    private int unreadCount;
    @Column(name = "folderClass")
    private String folderClass;
    @Column(name = "displayName")
    private String displayName;
    @Column(name = "childFolderCount")
    private int childFolderCount;
    @Column(name = "parentFolderId")
    private String parentFolderId;
    @Column(name = "folderType")
    private int folderType;
    @Column(name = "sort")
    private int sort = 0;

    public MailFolder() {

    }

    public MailFolder(JSONObject object, int sort) {
        id = JSONUtils.getString(object, "id", "");
        folderClass = JSONUtils.getString(object, "folderClass", "");
        displayName = JSONUtils.getString(object, "displayName", "");
        totalCount = JSONUtils.getInt(object, "totalCount", 0);
        unreadCount = JSONUtils.getInt(object, "unreadCount", 0);
        childFolderCount = JSONUtils.getInt(object, "childFolderCount", 0);
        parentFolderId = JSONUtils.getString(object, "parentFolderId", "");
        folderType = JSONUtils.getInt(object, "folderType", 0);
        this.sort = sort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getFolderClass() {
        return folderClass;
    }

    public void setFolderClass(String folderClass) {
        this.folderClass = folderClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getChildFolderCount() {
        return childFolderCount;
    }

    public void setChildFolderCount(int childFolderCount) {
        this.childFolderCount = childFolderCount;
    }

    public String getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public int getFolderType() {
        return folderType;
    }

    public void setFolderType(int folderType) {
        this.folderType = folderType;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}
